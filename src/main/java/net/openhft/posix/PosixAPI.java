package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;
import net.openhft.posix.internal.UnsafeMemory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

/**
 * This interface provides a set of methods for interacting with POSIX APIs.
 * It includes methods for file operations, memory management, and process scheduling.
 */
public interface PosixAPI {

    /**
     * @return The fastest available PosixAPI implementation.
     */
    static PosixAPI posix() {
        PosixAPIHolder.loadPosixApi();
        return PosixAPIHolder.POSIX_API;
    }

    /**
     * Sets the PosixAPI to a no-op implementation.
     */
    static void useNoOpPosixApi() {
        PosixAPIHolder.useNoOpPosixApi();
    }

    /**
     * Closes a file descriptor.
     *
     * @param fd The file descriptor to close.
     * @return 0 on success, -1 on error.
     */
    int close(int fd);

    /**
     * Allocates space for a file descriptor.
     *
     * @param fd     The file descriptor.
     * @param mode   The allocation mode.
     * @param offset The offset in the file.
     * @param length The length of the allocation.
     * @return 0 on success, -1 on error.
     */
    int fallocate(int fd, int mode, long offset, long length);

    /**
     * Truncates a file descriptor to a specified length.
     *
     * @param fd     The file descriptor.
     * @param offset The length to truncate to.
     * @return 0 on success, -1 on error.
     */
    int ftruncate(int fd, long offset);

    /**
     * Repositions the read/write file offset.
     *
     * @param fd     The file descriptor.
     * @param offset The offset to seek to.
     * @param whence The directive for how to seek.
     * @return The resulting offset location measured in bytes from the beginning of the file.
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
     * @return The resulting offset location measured in bytes from the beginning of the file.
     */
    long lseek(int fd, long offset, int whence);

    /**
     * Locks a section of a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param cmd The command to perform.
     * @param len The length of the section to lock.
     * @return 0 on success, -1 on error.
     */
    int lockf(int fd, int cmd, long len);

    /**
     * Advises the kernel about how to handle paging input/output.
     *
     * @param addr   The address.
     * @param length The length.
     * @param advice The advice directive.
     * @return 0 on success, -1 on error.
     */
    default int madvise(long addr, long length, MAdviseFlag advice) {
        return madvise(addr, length, advice.value());
    }

    /**
     * Advises the kernel about how to handle paging input/output.
     *
     * @param addr   The address.
     * @param length The length.
     * @param advice The advice directive.
     * @return 0 on success, -1 on error.
     */
    int madvise(long addr, long length, int advice);

    /**
     * Maps files or devices into memory.
     *
     * @param addr   The address.
     * @param length The length.
     * @param prot   The desired memory protection.
     * @param flags  The flags.
     * @param fd     The file descriptor.
     * @param offset The offset.
     * @return The starting address of the mapped area.
     */
    default long mmap(long addr, long length, MMapProt prot, MMapFlag flags, int fd, long offset) {
        return mmap(addr, length, prot.value(), flags.value(), fd, offset);
    }

    /**
     * Maps files or devices into memory.
     *
     * @param addr   The address.
     * @param length The length.
     * @param prot   The desired memory protection.
     * @param flags  The flags.
     * @param fd     The file descriptor.
     * @param offset The offset.
     * @return The starting address of the mapped area.
     */
    long mmap(long addr, long length, int prot, int flags, int fd, long offset);

    /**
     * Locks a range of the process's virtual address space into RAM.
     *
     * @param addr   The address.
     * @param length The length.
     * @return false, indicating the operation is not supported.
     */
    default boolean mlock(long addr, long length) {
        return false;
    }

    /**
     * Locks a range of the process's virtual address space into RAM.
     *
     * @param addr        The address.
     * @param length      The length.
     * @param lockOnFault Whether to lock on fault.
     * @return false, indicating the operation is not supported.
     */
    default boolean mlock2(long addr, long length, boolean lockOnFault) {
        return false;
    }

    /**
     * Locks all current and future pages into RAM.
     *
     * @param flags The flags.
     */
    default void mlockall(MclFlag flags) {
        mlockall(flags.code());
    }

    /**
     * Locks all current and future pages into RAM.
     *
     * @param flags The flags.
     */
    default void mlockall(int flags) {
    }

    /**
     * Synchronizes changes to a file with the storage device.
     *
     * @param address The address.
     * @param length  The length.
     * @param flags   The flags.
     * @return 0 on success, -1 on error.
     */
    default int msync(long address, long length, MSyncFlag flags) {
        return msync(address, length, flags.value());
    }

    /**
     * Synchronizes changes to a file with the storage device.
     *
     * @param address The address.
     * @param length  The length.
     * @param mode    The synchronization mode.
     * @return 0 on success, -1 on error.
     */
    int msync(long address, long length, int mode);

    /**
     * Unmaps files or devices from memory.
     *
     * @param addr   The address.
     * @param length The length.
     * @return 0 on success, -1 on error.
     */
    int munmap(long addr, long length);

    /**
     * Opens a file descriptor.
     *
     * @param path  The path to the file.
     * @param flags The flags.
     * @param perm  The permissions.
     * @return The file descriptor.
     */
    default int open(CharSequence path, OpenFlag flags, int perm) {
        return open(path, flags.value(), perm);
    }

    /**
     * Opens a file descriptor.
     *
     * @param path  The path to the file.
     * @param flags The flags.
     * @param perm  The permissions.
     * @return The file descriptor.
     */
    int open(CharSequence path, int flags, int perm);

    /**
     * Reads from a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param dst The destination address.
     * @param len The number of bytes to read.
     * @return The number of bytes read.
     */
    long read(int fd, long dst, long len);

    /**
     * Writes to a file descriptor.
     *
     * @param fd  The file descriptor.
     * @param src The source address.
     * @param len The number of bytes to write.
     * @return The number of bytes written.
     */
    long write(int fd, long src, long len);

    /**
     * Calculates disk usage for a given filename.
     *
     * @param filename The filename to calculate disk usage for.
     * @return The disk usage in bytes.
     * @throws IOException If an I/O error occurs.
     */
    default long du(String filename) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("du", filename);
        pb.redirectErrorStream(true);
        final Process process = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = br.readLine();
            return Long.parseUnsignedLong(line.split("\\s+")[0]);
        }
    }

    /**
     * Gets the current time of day.
     *
     * @param timeval The address of the timeval structure.
     * @return 0 on success, -1 on error.
     */
    int gettimeofday(long timeval);

    /**
     * Sets the CPU affinity for a process.
     *
     * @param pid        The process ID.
     * @param cpusetsize The size of the CPU set.
     * @param mask       The CPU set mask.
     * @return 0 on success, -1 on error.
     */
    int sched_setaffinity(int pid, int cpusetsize, long mask);

    /**
     * Gets the CPU affinity for a process.
     *
     * @param pid        The process ID.
     * @param cpusetsize The size of the CPU set.
     * @param mask       The CPU set mask.
     * @return 0 on success, -1 on error.
     */
    int sched_getaffinity(int pid, int cpusetsize, long mask);

    /**
     * Returns a summary of the CPU affinity for a given process ID.
     *
     * @param pid The process ID.
     * @return A summary of the CPU affinity as a string.
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
                final int b = UNSAFE.getInt(ptr + i / 32);
                if (((b >> i) & 1) != 0) {
                    if (set) {
                        // nothing.
                    } else {
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
     * Returns the last error code.
     *
     * @return The last error code.
     */
    int lastError();

    /**
     * Sets the CPU affinity for a process to a specific CPU.
     *
     * @param pid The process ID.
     * @param cpu The CPU to set affinity to.
     * @return 0 on success, -1 on error.
     */
    default int sched_setaffinity_as(int pid, int cpu) {
        final int nprocs_conf = get_nprocs_conf();
        final int size = Math.max(8, (nprocs_conf + 7) / 64 * 8);
        long ptr = malloc(size);
        try {
            for (int i = 0; i < size; i += 4)
                UNSAFE.putInt(ptr + i, 0);

            UNSAFE.putByte(ptr + cpu / 8,
                    (byte) (1 << (cpu & 7)));
            return sched_setaffinity(pid, size, ptr);
        } finally {
            free(ptr);
        }
    }

    /**
     * Sets the CPU affinity for a process to a range of CPUs.
     *
     * @param pid  The process ID.
     * @param from The starting CPU.
     * @param to   The ending CPU.
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
                UNSAFE.putInt(ptr + i / 32,
                        UNSAFE.getInt(ptr + i / 32) | (1 << i));
            }
            return sched_setaffinity(pid, size, ptr);
        } finally {
            free(ptr);
        }
    }

    /**
     * Returns the current wall clock time in microseconds.
     * Note that clock_gettime() is more accurate if available.
     *
     * @return The wall clock time in microseconds.
     */
    default long gettimeofday() {
        long ptr = malloc(16);
        try {
            if (gettimeofday(ptr) != 0)
                return 0;
            if (UnsafeMemory.IS32BIT)
                return (UNSAFE.getInt(ptr) & 0xFFFFFFFFL) * 1_000_000L + UNSAFE.getInt(ptr + 4);
            return UNSAFE.getLong(ptr) * 1_000_000 + UNSAFE.getInt(ptr + 8);
        } finally {
            free(ptr);
        }
    }

    /**
     * Returns the current wall clock time in nanoseconds.
     *
     * @return The wall clock time in nanoseconds.
     */
    default long clock_gettime() {
        return clock_gettime(0 /* CLOCK_REALTIME */);
    }

    /**
     * Returns the current wall clock time for a specific clock ID in nanoseconds.
     *
     * @param clockId The clock ID.
     * @return The wall clock time in nanoseconds.
     * @throws IllegalArgumentException If the clock ID is invalid.
     */
    default long clock_gettime(ClockId clockId) throws IllegalArgumentException {
        return clock_gettime(clockId.value());
    }

    /**
     * Returns the current wall clock time for a specific clock ID in nanoseconds.
     *
     * @param clockId The clock ID.
     * @return The wall clock time in nanoseconds.
     * @throws IllegalArgumentException If the clock ID is invalid.
     */
    long clock_gettime(int clockId) throws IllegalArgumentException;

    /**
     * Allocates memory of a specified size.
     *
     * @param size The size of the memory to allocate.
     * @return The address of the allocated memory.
     */
    long malloc(long size);

    /**
     * Frees allocated memory.
     *
     * @param ptr The address of the memory to free.
     */
    void free(long ptr);

    /**
     * Returns the number of available processors.
     *
     * @return The number of available processors.
     */
    int get_nprocs();

    /**
     * Returns the number of configured processors.
     *
     * @return The number of configured processors.
     */
    int get_nprocs_conf();

    /**
     * Returns the process ID.
     *
     * @return The process ID.
     */
    int getpid();

    /**
     * Returns the thread ID.
     *
     * @return The thread ID.
     */
    int gettid();

    /**
     * Returns the error message for a given error code.
     *
     * @param errno The error code.
     * @return The error message.
     */
    String strerror(int errno);

    /**
     * Returns the last error message.
     *
     * @return The last error message.
     */
    default String lastErrorStr() {
        return strerror(lastError());
    }
}
