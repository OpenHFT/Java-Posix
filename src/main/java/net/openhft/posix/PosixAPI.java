package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;
import net.openhft.posix.internal.UnsafeMemory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

/**
 * An interface to unify all POSIX method signatures (open, close, mmap, read, etc.).
 * <p>
 * <strong>Important:</strong> All methods are intended for *advanced usage only* and may:
 * <ul>
 *     <li>throw {@link PosixRuntimeException} if an underlying system call fails at runtime,</li>
 *     <li>throw {@link UnsupportedOperationException} if the current platform or distribution
 *     does not support the feature,</li>
 *     <li>degrade to a no-op if intentionally forced by environment variables or fallback logic.</li>
 * </ul>
 */
public interface PosixAPI {

    /**
     * Acquire the platform-specific POSIX instance based on loaded libraries or environment settings.
     *
     * @return the chosen {@code PosixAPI} implementation, possibly a no-op fallback.
     */
    static PosixAPI posix() {
        PosixAPIHolder.loadPosixApi();
        return PosixAPIHolder.POSIX_API;
    }

    /**
     * Forces the {@link PosixAPI} to switch to a no-op implementation.
     * <p>
     * After this call, subsequent {@link #posix()} invocations will return the no-op variant,
     * ignoring any previous OS-specific or environment-driven logic.
     */
    static void useNoOpPosixApi() {
        PosixAPIHolder.useNoOpPosixApi();
    }

    /**
     * Closes a file descriptor previously opened by {@link #open}.
     *
     * @param fd The file descriptor to close.
     * @return 0 on success, or -1 if an error occurs.
     * @throws PosixRuntimeException         if a system call fails.
     * @throws UnsupportedOperationException if not available on the current platform.
     */
    int close(int fd);

    /**
     * Allocates file storage space.
     * <p>
     * This may not be supported on all platforms, and certain filesystems may silently ignore it.
     *
     * @param fd     The file descriptor to allocate space for.
     * @param mode   The allocation mode (platform-dependent).
     * @param offset The starting offset in the file.
     * @param length The amount of space to allocate.
     * @return 0 on success, -1 if an error occurs.
     * @throws PosixRuntimeException         if the call fails at the system level.
     * @throws UnsupportedOperationException if the call is not supported.
     */
    int fallocate(int fd, int mode, long offset, long length);

    /**
     * Truncates a file to a specified length.
     *
     * @param fd     The file descriptor.
     * @param offset The length to truncate to.
     * @return 0 on success, -1 on error.
     * @throws PosixRuntimeException         if the call fails at the system level.
     * @throws UnsupportedOperationException if the call is not supported by the platform.
     */
    int ftruncate(int fd, long offset);

    /**
     * Repositions the read/write file offset using a {@link WhenceFlag}.
     * <p>
     * This default method provides an overload that accepts a {@code WhenceFlag},
     * delegating to the integer-based {@link #lseek(int, long, int)}.
     *
     * @param fd     The file descriptor.
     * @param offset The offset to seek to.
     * @param whence The directive for how to seek (e.g. {@code SEEK_SET}).
     * @return The resulting offset (in bytes) from the start of the file, or -1 if error.
     */
    default long lseek(int fd, long offset, WhenceFlag whence) {
        return lseek(fd, offset, whence.value());
    }

    /**
     * Repositions the read/write file offset.
     *
     * @param fd     The file descriptor.
     * @param offset The offset to seek to.
     * @param whence The directive for how to seek.
     * @return The resulting offset (in bytes) from the start of the file, or -1 if error.
     * @throws PosixRuntimeException         if the underlying system call fails.
     * @throws UnsupportedOperationException if lseek is unsupported on this platform.
     */
    long lseek(int fd, long offset, int whence);

    /**
     * Locks a portion of a file for exclusive or shared use (depending on command).
     *
     * @param fd  The file descriptor.
     * @param cmd The lock command (see {@link LockfFlag}).
     * @param len The length of the file section to lock.
     * @return 0 on success, or -1 if an error occurs.
     * @throws UnsupportedOperationException if locking is unavailable.
     */
    int lockf(int fd, int cmd, long len);

    /**
     * Advises the kernel on how to handle paging I/O for a given memory region
     * using a higher-level enum argument.
     *
     * @param addr   The starting address.
     * @param length The length of the region.
     * @param advice The {@link MAdviseFlag}.
     * @return 0 on success, -1 on error.
     */
    default int madvise(long addr, long length, MAdviseFlag advice) {
        return madvise(addr, length, advice.value());
    }

    /**
     * Advises the kernel on how to handle paging I/O for a given memory region.
     *
     * @param addr   The starting address.
     * @param length The length of the region.
     * @param advice An integer representing the advice directive.
     * @return 0 on success, -1 on error.
     * @throws PosixRuntimeException         if the system call fails.
     * @throws UnsupportedOperationException if the platform does not support this feature.
     */
    int madvise(long addr, long length, int advice);

    /**
     * Maps files or devices into memory using a higher-level protection and flags enum.
     *
     * @param addr   The preferred address (or 0 for unspecified).
     * @param length The length to map.
     * @param prot   The desired memory protection (e.g., READ/WRITE).
     * @param flags  The mmap flags (e.g., SHARED, PRIVATE).
     * @param fd     The file descriptor to map.
     * @param offset The offset in the file to start mapping.
     * @return The starting address of the mapped area, or -1 on failure.
     */
    default long mmap(long addr, long length, MMapProt prot, MMapFlag flags, int fd, long offset) {
        return mmap(addr, length, prot.value(), flags.value(), fd, offset);
    }

    /**
     * Maps files or devices into memory.
     *
     * @param addr   The preferred address (or 0 for unspecified).
     * @param length The length to map.
     * @param prot   Integer representing the desired memory protection.
     * @param flags  Integer representing the mmap flags.
     * @param fd     The file descriptor.
     * @param offset The offset in the file to start mapping.
     * @return The starting address of the mapped area, or -1 on failure.
     * @throws PosixRuntimeException         if the system call fails.
     * @throws UnsupportedOperationException if not supported by the OS.
     */
    long mmap(long addr, long length, int prot, int flags, int fd, long offset);

    /**
     * Tries to lock pages in the process's virtual address space.
     * <p>
     * By default, returns {@code false} in this interface to indicate it is unimplemented here.
     * Implementations may override to provide true locking.
     *
     * @param addr   The starting address.
     * @param length The length of memory to lock.
     * @return {@code false} by default, or {@code true} if successfully locked by an implementation.
     */
    default boolean mlock(long addr, long length) {
        return false;
    }

    /**
     * Attempts to lock pages in memory on first fault, if supported.
     *
     * @param addr        The starting address.
     * @param length      The length of memory to lock.
     * @param lockOnFault If {@code true}, lock pages only when they are faulted in.
     * @return {@code false} by default, or {@code true} if implemented by a subclass.
     */
    default boolean mlock2(long addr, long length, boolean lockOnFault) {
        return false;
    }

    /**
     * Locks all current and future pages into RAM, by default no-op.
     *
     * @param flags The {@link MclFlag} to define how the pages are locked.
     */
    default void mlockall(MclFlag flags) {
        mlockall(flags.code());
    }

    /**
     * Locks all current and future pages into RAM, by default no-op.
     *
     * @param flags An integer-based flag code representing how pages are locked.
     */
    default void mlockall(int flags) {
        // no-op by default
    }

    /**
     * Synchronizes changes to a file with the storage device using a higher-level enum.
     *
     * @param address The start of the memory region.
     * @param length  The length of the region.
     * @param flags   The {@link MSyncFlag} directive.
     * @return 0 on success, -1 if an error occurs.
     */
    default int msync(long address, long length, MSyncFlag flags) {
        return msync(address, length, flags.value());
    }

    /**
     * Synchronizes changes to a file with the storage device.
     *
     * @param address The start of the memory region.
     * @param length  The length of the region.
     * @param mode    The sync mode as an integer (e.g., MS_ASYNC).
     * @return 0 on success, -1 if an error occurs.
     * @throws PosixRuntimeException         if the sync call fails.
     * @throws UnsupportedOperationException if not supported on this OS.
     */
    int msync(long address, long length, int mode);

    /**
     * Unmaps a memory-mapped region.
     *
     * @param addr   The address at which it was mapped.
     * @param length The length to unmap.
     * @return 0 on success, -1 on error.
     * @throws PosixRuntimeException         if the unmap call fails.
     * @throws UnsupportedOperationException if unmapping isn't supported.
     */
    int munmap(long addr, long length);

    /**
     * Opens a file descriptor, using a higher-level enum for flags.
     *
     * @param path  The file path.
     * @param flags The {@link OpenFlag} enum describing open modes.
     * @param perm  The file permissions (e.g., 0666).
     * @return A non-negative file descriptor, or negative on error.
     */
    default int open(CharSequence path, OpenFlag flags, int perm) {
        return open(path, flags.value(), perm);
    }

    /**
     * Opens a file descriptor.
     *
     * @param path  The path to the file.
     * @param flags The integer flags.
     * @param perm  The file permissions.
     * @return A file descriptor, or negative on error.
     * @throws PosixRuntimeException         if the system call fails.
     * @throws UnsupportedOperationException if file opening isn't supported by this platform.
     */
    int open(CharSequence path, int flags, int perm);

    /**
     * Reads bytes from a file descriptor into a memory address.
     *
     * @param fd  The file descriptor.
     * @param dst The destination address in memory.
     * @param len The number of bytes to read.
     * @return The actual number of bytes read, or -1 on error.
     * @throws PosixRuntimeException         if the system call fails.
     * @throws UnsupportedOperationException if read is not supported.
     */
    long read(int fd, long dst, long len);

    /**
     * Writes bytes from a memory address to a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param src The source address in memory.
     * @param len The number of bytes to write.
     * @return The actual number of bytes written, or -1 on error.
     * @throws PosixRuntimeException         if the system call fails.
     * @throws UnsupportedOperationException if write is not supported.
     */
    long write(int fd, long src, long len);

    /**
     * Calculates the disk usage of a specified filename by invoking the {@code du} command.
     * <p>
     * Implementations may override this for native usage if preferred. By default, it spawns
     * a process to call {@code du}, which may not exist on all operating systems (notably Windows).
     *
     * @param filename The filename to check.
     * @return The disk usage in bytes, or 0 if unavailable.
     * @throws IOException on I/O issues during process interaction.
     */
    default long du(String filename) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("du", filename);
        pb.redirectErrorStream(true);
        final Process process = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = br.readLine();
            if (line == null || line.isEmpty()) {
                return 0;
            }
            return Long.parseUnsignedLong(line.split("\\s+")[0]);
        }
    }

    /**
     * Retrieves the current time of day in a {@code timeval} struct (seconds + microseconds).
     *
     * @param timeval The address of the timeval structure in memory.
     * @return 0 on success, -1 on error.
     * @throws PosixRuntimeException         if the underlying call fails.
     * @throws UnsupportedOperationException if not supported.
     */
    int gettimeofday(long timeval);

    /**
     * Sets the CPU affinity for a process, forcing that process to run on
     * a given set of CPUs.
     *
     * @param pid        The process ID.
     * @param cpusetsize The size of the CPU set bitmask in bytes.
     * @param mask       The pointer to the CPU set bitmask.
     * @return 0 on success, -1 on error.
     * @throws UnsupportedOperationException if CPU affinity is not supported on this OS.
     */
    int sched_setaffinity(int pid, int cpusetsize, long mask);

    /**
     * Retrieves the CPU affinity for a process, storing the result in the provided bitmask.
     *
     * @param pid        The process ID.
     * @param cpusetsize The size of the bitmask in bytes.
     * @param mask       The pointer to the bitmask, which the implementation fills.
     * @return 0 on success, -1 on error.
     * @throws UnsupportedOperationException if CPU affinity is not supported on this OS.
     */
    int sched_getaffinity(int pid, int cpusetsize, long mask);

    /**
     * Provides a human-readable summary of which CPUs a process is allowed to run on,
     * based on its CPU affinity settings.
     * <p>
     * By default, reads the affinity bitmask via {@link #sched_getaffinity} and
     * composes a range string (e.g., "0-3,5-7").
     *
     * @param pid The process ID to query.
     * @return A summary string like "0-3", or "na: [last error]" on failure.
     */
    default String sched_getaffinity_summary(int pid) {
        final int nprocs_conf = get_nprocs_conf();
        final int size = Math.max(8, (nprocs_conf + 7) / 64 * 8);
        long ptr = malloc(size);
        boolean set = false;
        int start = 0;
        StringBuilder sb = new StringBuilder();
        try {
            final int ret = sched_getaffinity(pid, size, ptr);
            if (ret != 0)
                return "na: " + lastError();
            for (int i = 0; i < nprocs_conf; i++) {
                final int bits = UNSAFE.getInt(ptr + i / 32);
                if (((bits >> i) & 1) != 0) {
                    if (!set) {
                        start = i;
                        set = true;
                    }
                } else {
                    if (set) {
                        if (sb.length() > 0)
                            sb.append(',');
                        sb.append(start).append('-').append(i - 1);
                        set = false;
                    }
                }
            }
            if (set) {
                if (sb.length() > 0)
                    sb.append(',');
                sb.append(start).append('-').append(nprocs_conf - 1);
            }
            return sb.toString();
        } finally {
            free(ptr);
        }
    }

    /**
     * Returns the most recent error code encountered by a system call,
     * which is often stored in {@code errno}.
     *
     * @return The last error code, or 0 if none.
     */
    int lastError();

    /**
     * Helper for setting CPU affinity to one CPU, overwriting any existing mask.
     *
     * @param pid The process ID.
     * @param cpu The CPU index to bind the process to.
     * @return 0 on success, -1 on error.
     */
    default int sched_setaffinity_as(int pid, int cpu) {
        final int nprocs_conf = get_nprocs_conf();
        final int size = Math.max(8, (nprocs_conf + 7) / 64 * 8);
        long ptr = malloc(size);
        try {
            for (int i = 0; i < size; i += 4)
                UNSAFE.putInt(ptr + i, 0);

            UNSAFE.putByte(ptr + cpu / 8, (byte) (1 << (cpu & 7)));
            return sched_setaffinity(pid, size, ptr);
        } finally {
            free(ptr);
        }
    }

    /**
     * Helper for setting CPU affinity to a range of CPUs (inclusive).
     *
     * @param pid  The process ID.
     * @param from The first CPU in the range.
     * @param to   The last CPU in the range.
     * @return 0 on success, -1 on error.
     */
    default int sched_setaffinity_range(int pid, int from, int to) {
        final int nprocs_conf = get_nprocs_conf();
        final int size = Math.max(8, (nprocs_conf + 7) / 64 * 8);
        long ptr = malloc(size);
        try {
            for (int i = 0; i < size; i += 4)
                UNSAFE.putInt(ptr + i, 0);

            for (int i = from; i <= to; i++) {
                int oldVal = UNSAFE.getInt(ptr + i / 32);
                UNSAFE.putInt(ptr + i / 32, oldVal | (1 << i));
            }
            return sched_setaffinity(pid, size, ptr);
        } finally {
            free(ptr);
        }
    }

    /**
     * Returns the current wall clock time in microseconds using {@link #gettimeofday(long)}.
     * <p>
     * For higher precision, consider using {@link #clock_gettime()} if available.
     *
     * @return The time in microseconds since the Unix epoch, or 0 if the call fails.
     */
    default long gettimeofday() {
        long ptr = malloc(16);
        try {
            if (gettimeofday(ptr) != 0) {
                return 0;
            }
            if (UnsafeMemory.IS32BIT) {
                // upper 32 bits = seconds, lower 32 bits = microseconds
                return (UNSAFE.getInt(ptr) & 0xFFFFFFFFL) * 1_000_000L
                     + UNSAFE.getInt(ptr + 4);
            }
            // upper 64 bits = seconds, lower 32 bits = microseconds
            return UNSAFE.getLong(ptr) * 1_000_000
                 + UNSAFE.getInt(ptr + 8);
        } finally {
            free(ptr);
        }
    }

    /**
     * Retrieves the current real-time clock value in nanoseconds using {@link #clock_gettime(int)}.
     * <p>
     * Defaults to {@code CLOCK_REALTIME} with an ID of 0.
     *
     * @return The real-time clock in nanoseconds, or 0 if the call fails.
     */
    default long clock_gettime() {
        return clock_gettime(0 /* CLOCK_REALTIME */);
    }

    /**
     * Returns the current wall clock time for a specific {@link ClockId} in nanoseconds.
     *
     * @param clockId The desired clock ID.
     * @return The time in nanoseconds, or 0 if the call fails.
     * @throws IllegalArgumentException if the provided clockId is invalid.
     */
    default long clock_gettime(ClockId clockId) throws IllegalArgumentException {
        return clock_gettime(clockId.value());
    }

    /**
     * Returns the current wall clock time for a specific clock ID in nanoseconds.
     *
     * @param clockId The integer ID of the clock (e.g., 0 for {@code CLOCK_REALTIME}).
     * @return The time in nanoseconds, or 0 on error.
     * @throws IllegalArgumentException if {@code clockId} is invalid or unsupported.
     */
    long clock_gettime(int clockId) throws IllegalArgumentException;

    /**
     * Allocates a block of memory of a specified size.
     * <p>
     * May rely on platform-specific calls (e.g. {@code malloc}) or custom logic
     * in the underlying implementation.
     *
     * @param size The number of bytes to allocate.
     * @return The address of the allocated memory, or 0 if allocation failed.
     * @throws UnsupportedOperationException if allocation is unavailable.
     */
    long malloc(long size);

    /**
     * Frees a block of previously allocated memory.
     *
     * @param ptr The address of the block to free.
     * @throws PosixRuntimeException         if freeing fails at the system level.
     * @throws UnsupportedOperationException if memory management is unsupported.
     */
    void free(long ptr);

    /**
     * Returns the number of available processors (online processors), if supported.
     *
     * @return The number of available (online) processors, or 1 if unknown.
     */
    int get_nprocs();

    /**
     * Returns the number of configured processors on the system, if supported.
     *
     * @return The total number of processors configured, or 1 if unknown.
     */
    int get_nprocs_conf();

    /**
     * Retrieves the current process ID.
     *
     * @return The PID, or a placeholder if not supported.
     * @throws UnsupportedOperationException if not implemented on the OS.
     */
    int getpid();

    /**
     * Retrieves the current thread ID.
     *
     * @return The TID, or a placeholder if not supported.
     * @throws UnsupportedOperationException if not implemented on the OS.
     */
    int gettid();

    /**
     * Translates an error code (e.g. from {@link #lastError()}) into a human-readable message.
     *
     * @param errno The error code.
     * @return A string describing the error, or {@code null} if no message is available.
     */
    String strerror(int errno);

    /**
     * Convenience method to retrieve a string describing the last error code
     * returned by {@link #lastError()}.
     *
     * @return The error message, or an empty string if none is found.
     */
    default String lastErrorStr() {
        String msg = strerror(lastError());
        return msg == null ? "" : msg;
    }
}
