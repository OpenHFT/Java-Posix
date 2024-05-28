package net.openhft.posix.internal.jnr;

import jnr.ffi.LibraryLoader;

final class LibraryUtil {

    private LibraryUtil() {
    }

    static <T> T load(final Class<T> type,
                      final String libraryName) {
        final LibraryLoader<T> loader = LibraryLoader.create(type);
        loader.library(libraryName);
        try {
            return loader.load();
        } catch (Exception e) {
            System.err.println("Unable to load native lib: " + libraryName);
            throw e;
        }
    }
}
