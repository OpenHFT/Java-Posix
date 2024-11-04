package net.openhft.posix.internal.jnr;

import jnr.ffi.Pointer;

/**
 * This interface defines the native methods for POSIX-like operations using JNR (Java Native Runtime).
 * It provides methods for file and memory operations, process scheduling, and system information retrieval.
 */
public interface JNRPosixInterface {
    int open(CharSequence path, int flags, int perm);

    long read(int fd, long dst, long len);

    long write(int fd, long src, long len);

    long lseek(int fd, long offset, int whence);

    int lockf(int fd, int cmd, long len);
    int flock(int fd, int operation);

    int ftruncate(int fd, long offset);

    int fallocate(int fd, int mode, long offset, long length);
    int fallocate64(int fd, int mode, long offset, long length);
    int posix_fallocate(int fd, long offset, long length);

    int close(int fd);

    int madvise(long addr, long length, int advise);

    long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset);

    int munmap(long addr, long length);

    int msync(long address, long length, int flags);

    int gettimeofday(long timeval, long alwaysNull);

    long malloc(long size);

    void free(long ptr);

    int get_nprocs();

    int get_nprocs_conf();

    int sched_setaffinity(int pid, int cpusetsize, Pointer mask);

    int sched_getaffinity(int pid, int cpusetsize, Pointer mask);

    int getpid();

    int gettid();

    String strerror(int errno);

    int clock_gettime(int clockId, long ptr);

    int mlock(long addr, long length);
    int mlock2(long addr, long length, int flags);

    int mlockall(int flags);

    int syscall(int number);

    int syscall(int number, long arg1, long arg2, int arg3);
}
