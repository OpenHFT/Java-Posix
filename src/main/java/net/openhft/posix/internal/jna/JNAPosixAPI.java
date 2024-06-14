package net.openhft.posix.internal.jna;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import net.openhft.posix.PosixAPI;

/**
 * Abstract class implementing {@link PosixAPI} using JNA (Java Native Access).
 * It provides methods for memory mapping operations, leveraging the JNA library.
 */
public abstract class JNAPosixAPI implements PosixAPI {
    private static final Pointer NULL = Pointer.createConstant(0);

    // JNA interface for POSIX functions
    private final JNAPosixInterface jna = new JNAPosixInterface();

    /**
     * Constructs a JNAPosixAPI instance and initializes the JNA interface.
     */
    public JNAPosixAPI() {
        NativeLibrary clib = NativeLibrary.getInstance(Platform.C_LIBRARY_NAME);
        Native.register(JNAPosixInterface.class, clib);
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        return jna.mmap(addr == 0 ? NULL : Pointer.createConstant(addr), length, prot, flags, fd, offset);
    }
}
