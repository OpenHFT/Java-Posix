package net.openhft.posix.internal.jnr;

/**
 * This interface defines the native methods for interacting with the Kernel32 library on Windows using JNR (Java Native Runtime).
 */
public interface Kernel32JNRInterface {

    /**
     * Retrieves the thread identifier of the calling thread.
     *
     * @return The thread identifier of the calling thread.
     */
    int GetCurrentThreadId();

    /**
     * Retrieves information about the current system.
     *
     * @param addr The address of the SYSTEM_INFO structure that receives the information.
     */
    void GetNativeSystemInfo(long addr);
}
