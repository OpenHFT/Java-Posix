package net.openhft.posix.internal.jnr;

import jnr.ffi.LibraryLoader;
import jnr.ffi.Platform;
import jnr.ffi.provider.FFIProvider;
import net.openhft.posix.PosixAPI;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

public class WinJNRPosixAPI implements PosixAPI {

    static final jnr.ffi.Runtime RUNTIME = FFIProvider.getSystemProvider().getRuntime();
    static final Platform NATIVE_PLATFORM = Platform.getNativePlatform();
    static final String STANDARD_C_LIBRARY_NAME = NATIVE_PLATFORM.getStandardCLibraryName();

    private final WinJNRPosixInterface jnr;
    private final Kernel32JNRInterface kernel32;

    public WinJNRPosixAPI() {
        LibraryLoader<WinJNRPosixInterface> loader = LibraryLoader.create(WinJNRPosixInterface.class);
        loader.library(STANDARD_C_LIBRARY_NAME);
        jnr = loader.load();
        LibraryLoader<Kernel32JNRInterface> loader2 = LibraryLoader.create(Kernel32JNRInterface.class);
        loader2.library("Kernel32");
        kernel32 = loader2.load();
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        return jnr._open(path, flags, perm);
    }

    @Override
    public long lseek(int fd, long offset, int whence) {
        return -1;
    }

    @Override
    public int lockf(int fd, int cmd, long len) {
        return -1;
    }

    @Override
    public long read(int fd, long dst, long len) {
        return jnr._read(fd, dst, len);
    }

    @Override
    public long write(int fd, long src, long len) {
        return jnr._write(fd, src, len);
    }


    @Override
    public int close(int fd) {
        return jnr._close(fd);
    }

    @Override
    public int getpid() {
        return jnr._getpid();
    }

    @Override
    public int gettid() {
        return kernel32.GetCurrentThreadId();
    }

    @Override
    public int madvise(long addr, long length, int advise) {
        return 0;
    }

    @Override
    public int msync(long address, long length, int flags) {
        return 0;
    }

    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        return 0;
    }

    @Override
    public long malloc(long size) {
        return UNSAFE.allocateMemory(size);
    }

    @Override
    public void free(long ptr) {
        UNSAFE.freeMemory(ptr);
    }

    @Override
    public long clock_gettime() {
        return System.currentTimeMillis() * 1_000_000;
    }

    @Override
    public int get_nprocs() {
        return get_nprocs_conf();
    }

    @Override
    public int get_nprocs_conf() {
        return Runtime.getRuntime().availableProcessors();
    }

    @Override
    public int ftruncate(int fd, long offset) {
        return 0;
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        return 0;
    }

    @Override
    public int munmap(long addr, long length) {
        return 0;
    }

    @Override
    public int gettimeofday(long timeval) {
        long now = System.currentTimeMillis();
        UNSAFE.putLong(timeval, now / 1000);
        UNSAFE.putLong(timeval + 8, (now % 1000) * 1000);
        return 0;
    }

    @Override
    public int sched_setaffinity(int pid, int cpusetsize, long mask) {
        return -1;
    }

    @Override
    public int sched_getaffinity(int pid, int cpusetsize, long mask) {
        return -1;
    }

    @Override
    public int lastError() {
        return RUNTIME.getLastError();
    }

    @Override
    public long clock_gettime(int clockId) throws IllegalArgumentException {
        return System.currentTimeMillis() * 1_000_000;
    }

    @Override
    public String strerror(int errno) {
        return jnr.strerror(errno);
    }
}
