package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blob.Blob;
import org.superbiz.moviefun.blob.BlobStore;
import org.superbiz.moviefun.blob.FileStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private FileStore blobstore = new FileStore();
    private ResourceLoader resourceLoader;

    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
    }

    @Autowired
    BlobStore cloudS3BlobStore;
    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")

        public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
            Blob blob = new Blob(Long.toString(albumId), uploadedFile.getInputStream(),uploadedFile.getContentType());
            cloudS3BlobStore.put(blob);
            return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        Optional<Blob> opt = cloudS3BlobStore.get((Long.toString(albumId)));
        Blob actual;
        if (opt.isPresent() == false)
        {
            InputStream inputStream = resourceLoader.getResource("classpath:/default-cover.jpg").getInputStream();
            actual = new Blob("default-cover.jpg", inputStream, new Tika().detect(inputStream));
            cloudS3BlobStore.put(actual);
            actual = cloudS3BlobStore.get(actual.getName()).get();
        }
        else
        {
            actual = opt.get();
        }

        byte[] imageBytes = IOUtils.toByteArray(actual.inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(actual.getContentType()));
        headers.setContentLength(imageBytes.length);

        return new HttpEntity<>(imageBytes,headers);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    @DeleteMapping("/covers")
    public String deleteCovers() {
        cloudS3BlobStore.deleteAll();
        return "redirect:/albums";
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            URL resource = Thread.currentThread().getContextClassLoader().getResource("default-cover.jpg");
            coverFilePath = Paths.get(resource.toURI());
        }

        return coverFilePath;
    }
}
