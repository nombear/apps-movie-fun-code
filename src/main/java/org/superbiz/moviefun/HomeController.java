package org.superbiz.moviefun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private TransactionOperations transactionOperationsForAlbums;
    @Autowired
    private TransactionOperations transactionOperationsForMovies;

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        for (Movie movie : movieFixtures.load()) {
            transactionOperationsForMovies.execute(new TransactionCallback<MoviesBean>() {

                @Override
                public MoviesBean doInTransaction(TransactionStatus transactionStatus) {
                    moviesBean.addMovie(movie);
                    return moviesBean;
                }
            });
        }

        for (Album album : albumFixtures.load()) {
            transactionOperationsForAlbums.execute(new TransactionCallback<AlbumsBean>() {
                @Override
                public AlbumsBean doInTransaction(TransactionStatus transactionStatus) {
                    albumsBean.addAlbum(album);
                    return albumsBean;
                }
            });
        }

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
