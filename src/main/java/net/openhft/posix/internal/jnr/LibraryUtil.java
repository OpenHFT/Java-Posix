package net.openhft.posix.internal.jnr;

import jnr.ffi.LibraryLoader;

/**
 * Utility class for loading native libraries using JNR (Java Native Runtime).
 */
final class LibraryUtil {

    // Private constructor to prevent instantiation
    private LibraryUtil() {
    }

    /**
     * Loads a native library for the specified interface type and library name.
     *
     * @param <T>         The type of the interface for the native library.
     * @param type        The class of the interface for the native library.
     * @param libraryName The name of the native library to load.
     * @return An instance of the specified interface type.
     * @throws RuntimeException If the native library cannot be loaded.
     */
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
