package net.openhft.posix.internal.noop;

import net.openhft.posix.PosixAPI;
import net.openhft.posix.PosixRuntimeException;

/**
 * A no-op PosixAPI used if either no library is available or environment variables request it.
 * Each method either does nothing and returns 0 (where trivial) or throws an exception if the function
 * is categorically unsupported.
 */
public class NoOpPosixAPI implements PosixAPI {
    private final String reason;

    /**
     * Constructs a NoOpPosixAPI with a specified reason.
     *
     * @param reason The reason for using this No-Op implementation.
     */
    public NoOpPosixAPI(String reason) {
        this.reason = reason;
    }

    @Override
    public int close(int fd) {
        throw posixImplementationMissing();
    }

    /**
     * Throws a PosixRuntimeException indicating that the POSIX implementation is missing.
     *
     * @return A PosixRuntimeException indicating the missing implementation.
     */
    private PosixRuntimeException posixImplementationMissing() {
        return new PosixRuntimeException("POSIX implementation missing " + reason);
    }

    // inefficient if not provided, but calling code should tolerate a no-op
    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        // Inefficient if not provided, but calling code should tolerate a no-op
        return 0;
    }

    // inefficient if not provided, but calling code should tolerate a no-op
    @Override
    public int ftruncate(int fd, long offset) {
        return 0;
    }

    @Override
    public long lseek(int fd, long offset, int whence) {
        throw posixImplementationMissing();
    }

    @Override
    public int lockf(int fd, int cmd, long len) {
        throw posixImplementationMissing();
    }

    // possibly inefficient if not provided, but safe to no-op
    @Override
    public int madvise(long addr, long length, int advice) {
        return 0;
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        throw posixImplementationMissing();
    }

    // possibly inefficient if not provided, but safe to no-op (on linux, mac; windows tbc)
    @Override
    public int msync(long address, long length, int mode) {
        return 0;
    }

    @Override
    public int munmap(long addr, long length) {
        throw posixImplementationMissing();
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        throw posixImplementationMissing();
    }

    @Override
    public long read(int fd, long dst, long len) {
        throw posixImplementationMissing();
    }

    @Override
    public long write(int fd, long src, long len) {
        throw posixImplementationMissing();
    }

    @Override
    public int gettimeofday(long timeval) {
        throw posixImplementationMissing();
    }

    // possibly inefficient if not provided, but calling code should tolerate a no-op
    @Override
    public int sched_setaffinity(int pid, int cpusetsize, long mask) {
        return 0;
    }

    // "all-cores" for no-op. possibly inefficient, but calling code should tolerate this return
    @Override
    public int sched_getaffinity(int pid, int cpusetsize, long mask) {
        return -1;
    }

    @Override
    public int lastError() {
        return 0;
    }

    @Override
    public long clock_gettime(int clockId) throws IllegalArgumentException {
        throw posixImplementationMissing();
    }

    @Override
    public long malloc(long size) {
        throw posixImplementationMissing();
    }

    @Override
    public void free(long ptr) {
        throw posixImplementationMissing();
    }

    @Override
    public int get_nprocs() {
        throw posixImplementationMissing();
    }

    @Override
    public int get_nprocs_conf() {
        throw posixImplementationMissing();
    }

    @Override
    public int getpid() {
        throw posixImplementationMissing();
    }

    @Override
    public int gettid() {
        throw posixImplementationMissing();
    }

    // slight loss of info on no-op
    @Override
    public String strerror(int errno) {
        return null;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }
}
