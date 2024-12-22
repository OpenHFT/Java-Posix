package net.openhft.posix.internal.jnr;

import jnr.constants.platform.Errno;
import jnr.ffi.Platform;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import jnr.ffi.provider.FFIProvider;
import net.openhft.posix.*;
import net.openhft.posix.internal.UnsafeMemory;
import net.openhft.posix.internal.core.Jvm;
import net.openhft.posix.internal.core.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.IntSupplier;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

/**
 * A Unix-like {@link PosixAPI} implementation using JNR (Java Native Runtime).
 * <p>
 * This class bridges to native POSIX calls via JNR. If a method cannot
 * be mapped on a particular system or distro (e.g., missing symbols),
 * we throw {@link UnsupportedOperationException}.
 */
public final class JNRPosixAPI implements PosixAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(JNRPosixAPI.class);

    /**
     * The JNR {@link jnr.ffi.Runtime} used for Unix-like POSIX calls.
     */
    static final jnr.ffi.Runtime RUNTIME = FFIProvider.getSystemProvider().getRuntime();

    /**
     * The detected native platform, typically used for library resolution.
     */
    static final Platform NATIVE_PLATFORM = Platform.getNativePlatform();

    /**
     * The standard C library name according to JNR's platform detection.
     */
    static final String STANDARD_C_LIBRARY_NAME = NATIVE_PLATFORM.getStandardCLibraryName();
    static final Pointer NULL = Pointer.wrap(RUNTIME, 0);

    static final int LOCK_EX = 2;
    static final int LOCK_UN = 8;
    static final int MLOCK_ONFAULT = 1;
    static final int SYS_mlock2; // mlock2 syscall value

    static {
        // These cover the main cases. Full list under https://github.com/torvalds/linux/tree/master/arch
        SYS_mlock2 = Jvm.isArm() ? 390
                : Jvm.is64bit() ? 325 : 376;
    }

    // JNR interface for POSIX functions
    private final JNRPosixInterface jnr;

    // Supplier for gettid method
    private final IntSupplier gettid;

    /**
     * Constructs a JNRPosixAPI instance and initializes the JNR interface and gettid supplier.
     */
    public JNRPosixAPI() {
        jnr = LibraryUtil.load(JNRPosixInterface.class, STANDARD_C_LIBRARY_NAME);
        gettid = getGettid();
    }

    // Cached number of processors
    private int get_nprocs_conf = 0;

    /**
     * Determines the appropriate method for getting the thread ID (gettid).
     *
     * @return A supplier for the gettid method.
     */
    private IntSupplier getGettid() {
        try {
            jnr.gettid();
            return jnr::gettid;
        } catch (UnsatisfiedLinkError expected) {
            // ignored
        }
        if (UnsafeMemory.IS32BIT)
            return () -> jnr.syscall(224);
        return () -> jnr.syscall(186);
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        return jnr.open(path, flags, perm);
    }

    @Override
    public long lseek(int fd, long offset, int whence) {
        return jnr.lseek(fd, offset, whence);
    }

    @Override
    public int ftruncate(int fd, long offset) {
        return jnr.ftruncate(fd, offset);
    }

    @Override
    public int lockf(int fd, int cmd, long len) {
        return jnr.lockf(fd, cmd, len);
    }

    @Override
    public int close(int fd) {
        return jnr.close(fd);
    }

    static final boolean MOCKALL_DUMP = Boolean.getBoolean("mlockall.dump");

    /**
     * Throws a {@link PosixRuntimeException} with a specified message and the last error.
     *
     * @param msg The message to include in the exception.
     * @return A {@link PosixRuntimeException} with the specified message and last error.
     */
    private static RuntimeException throwPosixException(String msg) {
        final int lastError = RUNTIME.getLastError();
        for (Errno errno : Errno.values()) {
            if (errno.intValue() == lastError)
                throw new PosixRuntimeException(msg + "error " + errno);
        }
        throw new PosixRuntimeException(msg + "unknown error " + lastError);
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {

        final Pointer wrap = addr == 0 ? NULL : Pointer.wrap(RUNTIME, addr);
        final long mmap = jnr.mmap(wrap, length, prot, flags, fd, offset);
        if (mmap == 0 || mmap == -1) {
            final int lastError = RUNTIME.getLastError();
            for (Errno errno : Errno.values()) {
                if (errno.intValue() == lastError)
                    throw new PosixRuntimeException(errno.toString());
            }
        }
        return mmap;
    }

    /**
     * Performs the mlock2 system call.
     *
     * @param addr        The address to lock.
     * @param length      The length of the memory to lock.
     * @param lockOnFault Whether to lock on fault.
     * @return The result of the mlock2 system call.
     */
    private int mlock2_(long addr, long length, boolean lockOnFault) {
        // Degrade to mlock for all platforms if lockOnFault not set
        // or always for macos which doesn't support mlock2 at all
        if (!lockOnFault || OS.isMacOSX())
            return jnr.mlock(addr, length);

        // Older glibc versions do not include a wrapper for mlock2, so use syscall for generality
        return jnr.syscall(SYS_mlock2, addr, length, MLOCK_ONFAULT);
    }

    @Override
    public boolean mlock(long addr, long length) {
        if (Jvm.isAzul()) {
            LOGGER.warn("mlock called but ignored for Azul");
            return true; // no-op on Azul, ignore
        }

        int err = jnr.mlock(addr, length);
        if (err == 0)
            return true;
        if (err == Errno.ENOMEM.intValue())
            return false;
        throw throwPosixException("mlock length: " + length + " ");
    }

    @Override
    public boolean mlock2(long addr, long length, boolean lockOnFault) {
        if (Jvm.isAzul()) {
            LOGGER.warn("mlock2 called but ignored for Azul");
            return true; // no-op on Azul, ignore
        }
        int err = mlock2_(addr, length, lockOnFault);
        if (err == 0)
            return true;
        if (err == Errno.ENOMEM.intValue())
            return false;
        throw throwPosixException("mlock2 length: " + length + " ");
    }

    @Override
    public void mlockall(int flags) {
        if (flags == MclFlag.MclCurrent.code()
                || flags == MclFlag.MclCurrentOnFault.code()) {
            tryMLockAll(flags);
            return;
        }
        int err = jnr.mlockall(flags);
        if (err == 0)
            return;
        throw throwPosixException("mlockall ");
    }

    /**
     * Attempts to lock all memory mappings of the current process.
     *
     * @param flags The mlockall flags.
     */
    private void tryMLockAll(int flags) {
        try {
            ProcMaps map = ProcMaps.forSelf();
            boolean onFault = flags == MclFlag.MclCurrentOnFault.code();
            for (Mapping mapping : map.list()) {
                if (mapping.perms().equals("---p"))
                    continue;
                int ret = mlock2_(mapping.addr(), mapping.length(), onFault);
                if (!MOCKALL_DUMP)
                    continue;
                final long kb = mapping.length() / 1024;
                if (ret != 0) {
                    final int lastError = RUNTIME.getLastError();
                    for (Errno errno : Errno.values()) {
                        if (errno.intValue() == lastError)
                            System.out.println(mapping + "len: " + kb + " KiB " + " " + errno);
                    }
                } else {
                    System.out.println(mapping + "len: " + kb + " KiB " + (onFault ? " mlocked (on fault)" : " mlocked (current pages)"));
                }
            }
        } catch (IOException ioe) {
            throw new PosixRuntimeException(ioe);
        }
    }

    @Override
    public int munmap(long addr, long length) {
        return jnr.munmap(addr, length);
    }

    @Override
    public int msync(long address, long length, int flags) {
        return jnr.msync(address, length, flags);
    }

    public class FileLocker implements AutoCloseable {
        private final int fd;

        public FileLocker(int fd) throws IOException {
            this.fd = fd;
            int ret = jnr.flock(fd, LOCK_EX);
            if (ret != 0) {
                throw new IOException("Failed to acquire lock");
            }
        }

        @Override
        public void close() throws IOException {
            int ret = jnr.flock(fd, LOCK_UN);
            if (ret != 0) {
                throw new IOException("Failed to release lock");
            }
        }
    }

    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        // fallocate support across environments/file systems is patchy
        // try a couple of approaches in order of preference

        // for 64-bit systems, fallocate64 is often available, but not always
        // try fallocate64 first, falling back to fallocate if any issue
        if (UnsafeMemory.IS64BIT) {
            try {
                int ret = jnr.fallocate64(fd, mode, offset, length);
                if (ret == 0)
                    return ret;
            } catch (Throwable ignored) {
            }
        }

        // for 32-bit systems, and 64-bit without a functioning fallocate64
        try {
            int ret = jnr.fallocate(fd, mode, offset, length);
            if (ret == 0)
                return ret;
        } catch (Throwable e) {
            if (mode != 0)
                throw e;
        }

        // if both fallocate attempts fail, then revert to posix_ftruncate when mode = 0
        // NB: this use case uses cooperative locking to help close a small race window
        if (mode == 0) {
            try (FileLocker lock = new FileLocker(fd)) {
                int ret = jnr.posix_fallocate(fd, offset, length);
                if (ret == 0)
                    return ret;
            } catch (Throwable ignored) {
            }
        }

        // out of options. report error
        return -1;
    }

    @Override
    public int madvise(long addr, long length, int advice) {
        return jnr.madvise(addr, length, advice);
    }

    @Override
    public long read(int fd, long dst, long len) {
        return jnr.read(fd, dst, len);
    }

    @Override
    public long write(int fd, long src, long len) {
        return jnr.write(fd, src, len);
    }

    @Override
    public int gettimeofday(long timeval) {
        return jnr.gettimeofday(timeval, 0L);
    }

    @Override
    public long malloc(long size) {
        return jnr.malloc(size);
    }

    @Override
    public void free(long ptr) {
        jnr.free(ptr);
    }

    @Override
    public int get_nprocs() {
        return jnr.get_nprocs();
    }

    @Override
    public int get_nprocs_conf() {
        if (get_nprocs_conf == 0)
            get_nprocs_conf = jnr.get_nprocs_conf();
        return get_nprocs_conf;
    }

    @Override
    public int sched_setaffinity(int pid, int cpusetsize, long mask) {
        int ret = jnr.sched_setaffinity(pid, cpusetsize, Pointer.wrap(Runtime.getSystemRuntime(), mask));
        if (ret != 0)
            throw new IllegalArgumentException(lastErrorStr() + ", ret: " + ret);
        return ret;
    }

    @Override
    public int sched_getaffinity(int pid, int cpusetsize, long mask) {
        int ret = jnr.sched_getaffinity(pid, cpusetsize, Pointer.wrap(Runtime.getSystemRuntime(), mask));
        if (ret != 0)
            throw new IllegalArgumentException(lastErrorStr() + ", ret: " + ret);
        return ret;
    }

    @Override
    public int getpid() {
        return jnr.getpid();
    }

    @Override
    public int gettid() {
        int ret = gettid.getAsInt();
        if (ret < 0)
            throw new IllegalArgumentException(lastErrorStr() + ", ret: " + ret);
        return ret;
    }

    @Override
    public int lastError() {
        return Runtime.getSystemRuntime().getLastError();
    }

    @Override
    public String strerror(int errno) {
        return jnr.strerror(errno);
    }

    @Override
    public long clock_gettime(int clockId) {
        long ptr = malloc(16);
        try {
            int ret = jnr.clock_gettime(clockId, ptr);
            if (ret != 0)
                throw new IllegalArgumentException(lastErrorStr() + ", ret: " + ret);
            if (UnsafeMemory.IS32BIT)
                return (UNSAFE.getInt(ptr) & 0xFFFFFFFFL) * 1_000_000_000L + UNSAFE.getInt(ptr + 4);
            return UNSAFE.getLong(ptr) * 1_000_000_000L + UNSAFE.getInt(ptr + 8);
        } finally {
            free(ptr);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return LOGGER.isTraceEnabled();
    }
}
