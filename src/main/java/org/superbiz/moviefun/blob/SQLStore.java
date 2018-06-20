package org.superbiz.moviefun.blob;

import java.io.IOException;
import java.util.Optional;

public class SQLStore implements BlobStore {
    @Override
    public void put(Blob blob) throws IOException {

    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        return Optional.empty();
    }

    @Override
    public void deleteAll() {

    }
}
