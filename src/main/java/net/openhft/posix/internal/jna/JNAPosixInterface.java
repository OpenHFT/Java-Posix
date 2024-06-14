package net.openhft.posix.internal.jna;

import com.sun.jna.Pointer;

/**
 * This class defines the native methods for POSIX-like operations using JNA (Java Native Access).
 * It provides methods for memory mapping operations.
 */
public class JNAPosixInterface {

    /**
     * Maps files or devices into memory.
     *
     * @param addr   The address where the mapping starts.
     * @param length The length of the mapping.
     * @param prot   The desired memory protection.
     * @param flags  The flags for the mapping.
     * @param fd     The file descriptor.
     * @param offset The offset into the file.
     * @return The starting address of the mapped area.
     */
    public native long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset);
}
