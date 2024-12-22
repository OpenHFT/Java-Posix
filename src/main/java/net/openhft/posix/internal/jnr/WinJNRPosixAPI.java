package net.openhft.posix.internal.jnr;

import jnr.ffi.Platform;
import jnr.ffi.Runtime;
import jnr.ffi.provider.FFIProvider;
import net.openhft.posix.PosixAPI;
import net.openhft.posix.PosixRuntimeException;
import net.openhft.posix.internal.UnsafeMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

/**
 * A Windows-specific {@link PosixAPI} implementation using JNR (Java Native Runtime).
 * <p>
 * Many POSIX calls are not natively supported on Windows. Methods that do have
 * partial analogues are implemented; unsupported features throw
 * {@link UnsupportedOperationException}. Default or no-op implementations
 * may also be used where Windows lacks a direct equivalent.
 */
public final class WinJNRPosixAPI implements PosixAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(WinJNRPosixAPI.class);

    /**
     * The JNR {@link jnr.ffi.Runtime} used for system-level calls on Windows.
     */
    static final jnr.ffi.Runtime RUNTIME = FFIProvider.getSystemProvider().getRuntime();

    /**
     * The detected native platform, typically used for resolving library names on Windows.
     */
    static final Platform NATIVE_PLATFORM = Platform.getNativePlatform();

    /**
     * The standard C library name according to the JNR platform detection.
     */
    static final String STANDARD_C_LIBRARY_NAME = NATIVE_PLATFORM.getStandardCLibraryName();

    /**
     * JNR interface for POSIX functions on Windows.
     */
    private final WinJNRPosixInterface jnrInterface;

    /**
     * JNR interface for Kernel32 functions on Windows (e.g. thread ID retrieval).
     */
    private final Kernel32JNRInterface kernel32;

    /**
     * Constructs a WinJNRPosixAPI instance and initializes the JNR interfaces.
     */
    public WinJNRPosixAPI() {
        this.jnrInterface = LibraryUtil.load(WinJNRPosixInterface.class, STANDARD_C_LIBRARY_NAME);
        kernel32 = LibraryUtil.load(Kernel32JNRInterface.class, "Kernel32");
        LOGGER.info("WinJNRPosixAPI loaded successfully on a Windows platform.");
    }

    // --------------------------------------------------
    //  Minimal implementations or stubs for POSIX calls
    // --------------------------------------------------

    /**
     * Attempts to open a file descriptor on Windows. Windows does not map POSIX flags directly, so {@code flags} and {@code perm} usage may be limited.
     *
     * @param path  The path to open.
     * @param flags POSIX-style integer flags (e.g. {@code O_RDWR}).
     * @param perm  File permission bits (ignored on most Windows filesystems).
     * @return A valid file descriptor, or negative on error.
     */
    @Override
    public int open(CharSequence path, int flags, int perm) {
        try {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("Attempting to open file on Windows: path={}, flags={}, perm={}", path, flags, perm);
            return jnrInterface._open(path.toString(), flags, perm);
        } catch (UnsatisfiedLinkError e) {
            throw new UnsupportedOperationException("Windows open() not available or linking failed.", e);
        } catch (Exception e) {
            throw new PosixRuntimeException("Failed to open file: " + e.getMessage(), e);
        }
    }

    /**
     * Closes a previously opened file descriptor.
     *
     * @param fd The file descriptor to close.
     * @return 0 on success, or -1 if an error occurs.
     */
    @Override
    public int close(int fd) {
        try {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("Closing file descriptor on Windows: fd={}", fd);
            return jnrInterface._close(fd);
        } catch (UnsatisfiedLinkError e) {
            throw new UnsupportedOperationException("Windows close() not available or linking failed.", e);
        } catch (Exception e) {
            throw new PosixRuntimeException("Failed to close fd=" + fd + ": " + e.getMessage(), e);
        }
    }

    /**
     * Reads data from a file descriptor into the specified memory address.
     *
     * @param fd  The file descriptor to read from.
     * @param dst The destination address in memory.
     * @param len The number of bytes to read.
     * @return The number of bytes actually read, or -1 if an error occurs.
     */
    @Override
    public long read(int fd, long dst, long len) {
        try {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("Reading from fd={}, to memory @ {}, length={}", fd, dst, len);
            return jnrInterface._read(fd, jnr.ffi.Pointer.wrap(Runtime.getSystemRuntime(), dst), (int) len);
        } catch (UnsatisfiedLinkError e) {
            throw new UnsupportedOperationException("Windows read() not available or linking failed.", e);
        } catch (Exception e) {
            throw new PosixRuntimeException("Reading from fd=" + fd + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * Writes data to a file descriptor from the specified memory address.
     *
     * @param fd  The file descriptor to write to.
     * @param src The source address in memory.
     * @param len The number of bytes to write.
     * @return The number of bytes actually written, or -1 if an error occurs.
     */
    @Override
    public long write(int fd, long src, long len) {
        try {
            if (LOGGER.isTraceEnabled())
                LOGGER.trace("Writing to fd={}, from memory @ {}, length={}", fd, src, len);
            return jnrInterface._write(fd, jnr.ffi.Pointer.wrap(Runtime.getSystemRuntime(), src), (int) len);
        } catch (UnsatisfiedLinkError e) {
            throw new UnsupportedOperationException("Windows write() not available or linking failed.", e);
        } catch (Exception e) {
            throw new PosixRuntimeException("Writing to fd=" + fd + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * Sets the file pointer to a specific location using Windows' 64-bit {@code _lseeki64} call.
     * <p>
     * This partially mimics the POSIX {@code lseek}, though flag usage may differ.
     *
     * @param fd     The file descriptor.
     * @param offset The position to set.
     * @param whence The reference position (e.g. SEEK_SET).
     * @return The new file pointer position, or -1 if an error occurs.
     */
    @Override
    public long lseek(int fd, long offset, int whence) {
        return jnrInterface._lseeki64(fd, offset, whence);
    }

    // --------------------------------------------------
    //  Unimplemented or partially implemented features
    // --------------------------------------------------

    /**
     * A stub for file locking. Windows does not support {@code lockf} in a direct POSIX manner.
     * Returns {@code -1} to indicate failure/unimplemented.
     *
     * @param fd  The file descriptor.
     * @param cmd The lock command.
     * @param len The length of the section to lock.
     * @return Always -1 on Windows.
     */
    @Override
    public int lockf(int fd, int cmd, long len) {
        // Windows doesn't support lockf in a direct POSIX manner. We throw:
        LOGGER.warn("lockf is not supported on Windows. fd={}, cmd={}, len={}", fd, cmd, len);
        return -1; // lock failed
    }

    /**
     * Retrieves the current process ID {@link Kernel32JNRInterface#GetCurrentProcessId()}.
     *
     * @return The process ID, or throws an exception on failure.
     */
    @Override
    public int getpid() {
        try {
            return kernel32.GetCurrentProcessId();
        } catch (UnsatisfiedLinkError e) {
            throw new UnsupportedOperationException("Windows GetCurrentProcessId() not available or linking failed.", e);
        } catch (Exception e) {
            throw new PosixRuntimeException("Failed to getpid: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the current thread ID using {@link Kernel32JNRInterface#GetCurrentThreadId()}.
     *
     * @return The thread ID.
     */
    @Override
    public int gettid() {
        return kernel32.GetCurrentThreadId();
    }

    /**
     * A stub for {@code madvise}, returning 0 by default. Windows typically does not implement
     * POSIX memory advisory calls.
     *
     * @param addr   The address in memory.
     * @param length The length of the region.
     * @param advise The advice flag.
     * @return Always returns 0.
     */
    @Override
    public int madvise(long addr, long length, int advise) {
        return 0;
    }

    /**
     * A stub for {@code msync}, returning 0 by default. Windows does not implement
     * POSIX synchronous memory sync in the same way.
     *
     * @param address The address in memory.
     * @param length  The length of the region.
     * @param flags   The flags for syncing.
     * @return Always returns 0.
     */
    @Override
    public int msync(long address, long length, int flags) {
        return 0;
    }

    /**
     * A stub for {@code fallocate}, returning 0 by default. Windows does not implement
     * POSIX file allocation in the same way.
     *
     * @param fd     The file descriptor.
     * @param mode   The allocation mode.
     * @param offset The starting offset.
     * @param length The length of space to allocate.
     * @return Always returns -1.
     */
    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        return -1;
    }

    /**
     * Allocates memory using {@link sun.misc.Unsafe#allocateMemory(long)} on Windows.
     *
     * @param size The number of bytes to allocate.
     * @return The address of the allocated memory, or 0 if it fails.
     */
    @Override
    public long malloc(long size) {
        return UNSAFE.allocateMemory(size);
    }

    /**
     * Frees memory previously allocated with {@code malloc} using
     * {@link sun.misc.Unsafe#freeMemory(long)}.
     *
     * @param ptr The base address of the memory to free.
     */
    @Override
    public void free(long ptr) {
        UNSAFE.freeMemory(ptr);
    }

    /**
     * Returns a millisecond-based approximation of clock_gettime() using
     * {@link System#currentTimeMillis()}. This is not strictly POSIX-compliant.
     *
     * @return A nanosecond representation of the current system time.
     */
    @Override
    public long clock_gettime() {
        return System.currentTimeMillis() * 1_000_000;
    }

    /**
     * Returns the number of currently available processors, falling back to
     * the Java runtime's {@link Runtime#availableProcessors()}.
     *
     * @return The number of available processors.
     */
    @Override
    public int get_nprocs() {
        return get_nprocs_conf();
    }

    /**
     * Returns the total number of processors configured on the system, typically
     * equal to {@link Runtime#availableProcessors()} on Windows.
     *
     * @return The total number of processors.
     */
    @Override
    public int get_nprocs_conf() {
        return java.lang.Runtime.getRuntime().availableProcessors();
    }

    /**
     * A stub for {@code ftruncate}, returning 0 by default. Windows does not implement
     * this POSIX call directly. If necessary, you might use {@code SetEndOfFile}.
     *
     * @param fd     The file descriptor.
     * @param offset The new size of the file.
     * @return Always -1.
     */
    @Override
    public int ftruncate(int fd, long offset) {
        try {
            // 1. Convert the POSIX file descriptor to a Windows HANDLE.
            //    Your WinJNRPosixInterface might provide a helper like `_get_osfhandle(int fd)`.
            //    Or you could do something else if the handle is already known.
            long handle = jnrInterface._get_osfhandle(fd);  // hypothetical native method

            if (handle == -1) {
                LOGGER.warn("ftruncate: invalid Windows handle for fd={}", fd);
                return -1; // Or throw an exception; depends on your design
            }

            // 2. Move the file pointer to the `offset` position from FILE_BEGIN (0).
            //    The JNR interface for Kernel32 might look like:
            //    boolean SetFilePointerEx(long handle, long distance, Pointer newFilePointer, int moveMethod);
            //    For FILE_BEGIN, we typically use 0.
            boolean pointerOk = kernel32.SetFilePointerEx(handle, offset, 0, 0 /* FILE_BEGIN */);
            if (!pointerOk) {
                int lastErr = RUNTIME.getLastError();
                LOGGER.error("SetFilePointerEx failed, fd={}, offset={}, lastError={}", fd, offset, lastErr);
                return -1;
            }

            // 3. Truncate the file at the current file pointer via SetEndOfFile.
            //    boolean SetEndOfFile(long handle);
            boolean endOk = kernel32.SetEndOfFile(handle);
            if (!endOk) {
                int lastErr = RUNTIME.getLastError();
                LOGGER.error("SetEndOfFile failed, fd={}, offset={}, lastError={}", fd, offset, lastErr);
                return -1;
            }

            // 4. Success
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("ftruncate succeeded: fd={}, offset={}", fd, offset);
            }
            return 0;
        } catch (UnsatisfiedLinkError e) {
            throw new UnsupportedOperationException("ftruncate on Windows linking failed.", e);
        } catch (Exception e) {
            throw new PosixRuntimeException("ftruncate error: " + e.getMessage(), e);
        }
    }

    /**
     * A stub for {@code mmap}, returning 0 by default. Windows does not implement
     * mmap in the same way, but you could potentially use {@code CreateFileMapping}.
     *
     * @param addr   Preferred address (ignored).
     * @param length The length of the mapping (ignored).
     * @param prot   Protection flags (ignored).
     * @param flags  Mapping flags (ignored).
     * @param fd     File descriptor (ignored).
     * @param offset Offset in the file (ignored).
     * @return Always -1.
     */
    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        return -1;
    }

    /**
     * A stub for {@code munmap}, returning 0 by default. Windows does not implement
     * {@code munmap} in the same way; one might use {@code UnmapViewOfFile}.
     *
     * @param addr   The address to unmap.
     * @param length The length of the region.
     * @return Always -1.
     */
    @Override
    public int munmap(long addr, long length) {
        return -1;
    }

    /**
     * Fills a timeval structure with the current system time.
     * The seconds field is placed in the first 8 bytes, and microseconds in the next 8 bytes.
     *
     * @param timeval Address of the timeval structure.
     * @return 0 on success.
     */
    @Override
    public int gettimeofday(long timeval) {
        long now = System.currentTimeMillis();
        if (UnsafeMemory.IS32BIT) {
            UNSAFE.putInt(timeval, (int) (now / 1000));
            UNSAFE.putInt(timeval + 4, (int) ((now % 1000) * 1000));
        } else {
            // upper 64 bits = seconds, lower 32 bits = microseconds
            UNSAFE.putLong(timeval, now / 1000);
            UNSAFE.putInt(timeval + 8, (int) ((now % 1000) * 1000));
        }
        return 0;
    }

    /**
     * A stub for {@code sched_setaffinity}, returning -1 by default. Windows does
     * not implement this call in a POSIX manner; you might need to use {@code SetThreadAffinityMask}.
     *
     * @param pid        Process ID (not typically used in Windows the same way).
     * @param cpusetsize Byte size of the CPU mask.
     * @param mask       Pointer to the CPU mask.
     * @return Always -1.
     */
    @Override
    public int sched_setaffinity(int pid, int cpusetsize, long mask) {
        return -1;
    }

    /**
     * A stub for {@code sched_getaffinity}, returning -1 by default. Windows does
     * not implement this call in a POSIX manner; you might need to use {@code GetProcessAffinityMask}.
     *
     * @param pid        The process ID.
     * @param cpusetsize Byte size of the CPU mask.
     * @param mask       Pointer to the CPU mask (to be populated).
     * @return Always -1.
     */
    @Override
    public int sched_getaffinity(int pid, int cpusetsize, long mask) {
        return -1;
    }

    /**
     * Returns the last error code stored by JNR, which maps to the Windows
     * native {@code GetLastError()} in many cases.
     *
     * @return The last error code, or 0 if none is recorded.
     */
    @Override
    public int lastError() {
        return RUNTIME.getLastError();
    }

    /**
     * Returns a nanosecond timestamp based on {@link System#currentTimeMillis()}.
     * <p>
     * The provided clock ID is ignored for now, as Windows does not provide
     * a direct analogue to POSIX clock IDs. This method always uses system time.
     *
     * @param clockId The integer ID for the clock (ignored).
     * @return A nanosecond representation of the current system time.
     * @throws IllegalArgumentException Never thrown in this stub.
     */
    @Override
    public long clock_gettime(int clockId) throws IllegalArgumentException {
        return System.currentTimeMillis() * 1_000_000;
    }

    /**
     * Retrieves a string describing the specified error code. If no message is
     * found, may return a default message.
     *
     * @param errno The error code for which to retrieve a message.
     * @return A human-readable error message, or a fallback if not found.
     */
    @Override
    public String strerror(int errno) {
        return jnrInterface.strerror(errno);
    }
}
