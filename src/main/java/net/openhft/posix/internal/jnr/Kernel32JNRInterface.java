package net.openhft.posix.internal.jnr;

/**
 * This interface defines the native methods for interacting with the Kernel32 library on Windows using JNR (Java Native Runtime).
 */
public interface Kernel32JNRInterface {
    boolean SetFilePointerEx(long hFile,
                             long lDistanceToMove,
                             long /* or Pointer */ lpNewFilePointer,
                             int dwMoveMethod);

    boolean SetEndOfFile(long hFile);

    /**
     * Retrieves the process identifier of the calling process.
     *
     * @return The process identifier of the calling process.
     */
    int GetCurrentProcessId();
    /**
     * Retrieves the thread identifier of the calling thread.
     *
     * @return The thread identifier of the calling thread.
     */
    int GetCurrentThreadId();
}
