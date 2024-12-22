package net.openhft.posix.internal.jnr;

import jnr.ffi.Pointer;

/**
 * This interface defines the native methods for POSIX-like operations on Windows using JNR (Java Native Runtime).
 */
public interface WinJNRPosixInterface {
    /**
     * Closes a file descriptor.
     *
     * @param fd The file descriptor to close.
     * @return 0 on success, -1 on error.
     */
    int _close(int fd);

    /**
     * Opens a file.
     *
     * @param path  The path to the file.
     * @param flags The flags for opening the file.
     * @param perm  The permissions for the file.
     * @return The file descriptor.
     */
    int _open(CharSequence path, int flags, int perm);

    /**
     * Sets the file pointer to a specified location.
     *
     * @param fd     The file descriptor.
     * @param offset The offset to set the pointer to.
     * @param origin The origin from where to set the offset.
     * @return The new file pointer location.
     */
    long _lseeki64(int fd, long offset, int origin);

    /**
     * Reads from a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param dst The destination address.
     * @param len The number of bytes to read.
     * @return The number of bytes read.
     */
    long _read(int fd, Pointer dst, int len);

    /**
     * Writes to a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param src The source address.
     * @param len The number of bytes to write.
     * @return The number of bytes written.
     */
    long _write(int fd, Pointer src, int len);

    /**
     * Returns the error message for a given error code.
     *
     * @param errno The error code.
     * @return The error message.
     */
    String strerror(int errno);

    // Method to convert fd -> Windows HANDLE
    long _get_osfhandle(int fd);
}
