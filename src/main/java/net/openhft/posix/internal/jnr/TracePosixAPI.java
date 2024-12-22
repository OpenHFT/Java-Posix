package net.openhft.posix.internal.jnr;

import jnr.constants.platform.Errno;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import net.openhft.posix.*;
import net.openhft.posix.internal.UnsafeMemory;
import net.openhft.posix.internal.core.Jvm;
import net.openhft.posix.internal.core.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

/**
 * A Unix-like {@link PosixAPI} wrapper that logs trace information for each call.
 */
public final class TracePosixAPI implements PosixAPI {

    private static final Logger LOGGER = LoggerFactory.getLogger(TracePosixAPI.class);

    private final PosixAPI posix;

    public TracePosixAPI(PosixAPI posix) {
        this.posix = posix;
    }

    @Override
    public int open(CharSequence path, int flags, int perm) {
        try {
            int open = posix.open(path, flags, perm);
            LOGGER.trace("Opening file: path={}, flags={}, perm={} returned {}", path, flags, perm, open);
            return open;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("open symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("Opening file: path={}, flags={}, perm={} threw {}", path, flags, perm, e.toString());
            throw new PosixRuntimeException("open call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public long lseek(int fd, long offset, int whence) {
        try {
            long ret = posix.lseek(fd, offset, whence);
            LOGGER.trace("Seeking fd={}, offset={}, whence={} returned {}", fd, offset, whence, ret);
            return ret;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("lseek symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("Seeking fd={}, offset={}, whence={} threw {}", fd, offset, whence, e.toString());
            throw new PosixRuntimeException("lseek call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int ftruncate(int fd, long offset) {
        try {
            int ftruncate = posix.ftruncate(fd, offset);
            LOGGER.trace("ftruncate fd={}, offset={} returned {}", fd, offset, ftruncate);
            return ftruncate;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("ftruncate symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("ftruncate fd={}, offset={} threw {}", fd, offset, e.toString());
            throw new PosixRuntimeException("ftruncate call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int lockf(int fd, int cmd, long len) {
        try {
            int lockf = posix.lockf(fd, cmd, len);
            LOGGER.trace("Locking fd={} cmd={} len={} returned {}", fd, cmd, len, lockf);
            return lockf;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("lockf symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("Locking fd={} cmd={} len={} threw {}", fd, cmd, len, e.toString());
            throw new PosixRuntimeException("lockf call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int close(int fd) {
        try {
            int close = posix.close(fd);
            LOGGER.trace("Closing fd={} returned {}", fd, close);
            return close;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("close symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("Closing fd={} threw {}", fd, e.toString());
            throw new PosixRuntimeException("close call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public long mmap(long addr, long length, int prot, int flags, int fd, long offset) {
        try {
        long mmap = posix.mmap(addr, length, prot, flags, fd, offset);
        LOGGER.trace("mmap addr={}, length={}, prot={}, flags={}, fd={}, offset={} returned {}",
                addr, length, prot, flags, fd, offset, mmap);
        return mmap;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("mmap symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("mmap addr={}, length={}, prot={}, flags={}, fd={}, offset={} threw {}",
                    addr, length, prot, flags, fd, offset, e.toString());
            throw new PosixRuntimeException("mmap call failed: " + e.getMessage(), e);
        }

    }

    @Override
    public boolean mlock(long addr, long length) {
        try {
        boolean mlock = posix.mlock(addr, length);
        LOGGER.trace("mlock addr={}, length={} returned {}", addr, length, mlock);
        return mlock;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("close symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("mlock addr={}, length={} threw {}", addr, length, e.toString());
            throw new PosixRuntimeException("mmap call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean mlock2(long addr, long length, boolean lockOnFault) {
        try {
        boolean mlock2 = posix.mlock2(addr, length, lockOnFault);
        LOGGER.trace("mlock2 addr={}, length={}, lockOnFault={} returned {}", addr, length, lockOnFault, mlock2);
        return mlock2;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("mlock2 symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("mlock2 addr={}, length={}, lockOnFault={} threw {}", addr, length, lockOnFault, e.toString());
            throw new PosixRuntimeException("mlock2 call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void mlockall(int flags) {
        try {
        posix.mlockall(flags);
        LOGGER.trace("mlockall flags={} returned", flags);
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("mlockall symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("mlockall flags={} threw {}", flags, e.toString());
            throw new PosixRuntimeException("mlockall call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int munmap(long addr, long length) {
        try {
        int munmap = posix.munmap(addr, length);
        LOGGER.trace("munmap addr={}, length={} returned {}", addr, length, munmap);
        return munmap;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("munmap symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("munmap addr={}, length={} threw {}", addr, length, e.toString());
            throw new PosixRuntimeException("munmap call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int msync(long address, long length, int flags) {
        try {
        int msync = posix.msync(address, length, flags);
        LOGGER.trace("msync address={}, length={}, flags={} returned {}", address, length, flags, msync);
        return msync;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("msync symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("msync address={}, length={}, flags={} threw {}", address, length, flags, e.toString());
            throw new PosixRuntimeException("msync call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int fallocate(int fd, int mode, long offset, long length) {
        try {
        int fallocate = posix.fallocate(fd, mode, offset, length);
        LOGGER.trace("fallocate fd={}, mode={}, offset={}, length={} returned {}", fd, mode, offset, length, fallocate);
        return fallocate;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("fallocate symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("fallocate fd={}, mode={}, offset={}, length={} threw {}", fd, mode, offset, length, e.toString());
            throw new PosixRuntimeException("fallocate call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int madvise(long addr, long length, int advice) {
        try {
        int madvise = posix.madvise(addr, length, advice);
        LOGGER.trace("madvise addr={}, length={}, advice={} returned {}", addr, length, advice, madvise);
        return madvise;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("madvise symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("madvise addr={}, length={}, advice={} threw {}", addr, length, advice, e.toString());
            throw new PosixRuntimeException("madvise call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public long read(int fd, long dst, long len) {
        try {
        long read = posix.read(fd, dst, len);
        LOGGER.trace("read fd={}, dst={}, len={} returned {}", fd, dst, len, read);
        return read;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("read symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("read fd={}, dst={}, len={} threw {}", fd, dst, len, e.toString());
            throw new PosixRuntimeException("read call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public long write(int fd, long src, long len) {
        try {
        long write = posix.write(fd, src, len);
        LOGGER.trace("write fd={}, src={}, len={} returned {}", fd, src, len, write);
        return write;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("write symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("write fd={}, src={}, len={} threw {}", fd, src, len, e.toString());
            throw new PosixRuntimeException("write call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int gettimeofday(long timeval) {
        try {
        int gettimeofday = posix.gettimeofday(timeval);
        LOGGER.trace("gettimeofday timeval={} returned {}", timeval, gettimeofday);
        return gettimeofday;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("gettimeofday symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("gettimeofday timeval={} threw {}", timeval, e.toString());
            throw new PosixRuntimeException("gettimeofday call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public long malloc(long size) {
        try {
        long malloc = posix.malloc(size);
        LOGGER.trace("malloc size={} returned {}", size, malloc);
        return malloc;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("malloc symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("malloc size={} threw {}", size, e.toString());
            throw new PosixRuntimeException("malloc call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void free(long ptr) {
        try {
        posix.free(ptr);
        LOGGER.trace("free ptr={} returned", ptr);
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("free symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("free ptr={} threw {}", ptr, e.toString());
            throw new PosixRuntimeException("free call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int get_nprocs() {
        try {
        int get_nprocs = posix.get_nprocs();
        LOGGER.trace("get_nprocs returned {}", get_nprocs);
        return get_nprocs;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("get_nprocs symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("get_nprocs threw {}", e.toString());
            throw new PosixRuntimeException("get_nprocs call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int get_nprocs_conf() {
        try {
        int get_nprocs_conf = posix.get_nprocs_conf();
        LOGGER.trace("get_nprocs_conf returned {}", get_nprocs_conf);
        return get_nprocs_conf;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("get_nprocs_conf symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("get_nprocs_conf threw {}", e.toString());
            throw new PosixRuntimeException("get_nprocs_conf call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int sched_setaffinity(int pid, int cpusetsize, long mask) {
        try {
        int sched_setaffinity = posix.sched_setaffinity(pid, cpusetsize, mask);
        LOGGER.trace("sched_setaffinity pid={}, cpusetsize={}, mask={} returned {}", pid, cpusetsize, mask, sched_setaffinity);
        return sched_setaffinity;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("sched_setaffinity symbol not found or linking failed", ule);
        } catch (IllegalArgumentException iae) {
            LOGGER.trace("sched_setaffinity pid={}, cpusetsize={}, mask={} threw {}", pid, cpusetsize, mask, iae.toString());
            throw iae;
        } catch (Exception e) {
            LOGGER.trace("sched_setaffinity pid={}, cpusetsize={}, mask={} threw {}", pid, cpusetsize, mask, e.toString());
            throw new PosixRuntimeException("sched_setaffinity call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int sched_getaffinity(int pid, int cpusetsize, long mask) {
        try {
        int sched_getaffinity = posix.sched_getaffinity(pid, cpusetsize, mask);
        LOGGER.trace("sched_getaffinity pid={}, cpusetsize={}, mask={} returned {}", pid, cpusetsize, mask, sched_getaffinity);
        return sched_getaffinity;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("sched_getaffinity symbol not found or linking failed", ule);
        }catch (IllegalArgumentException iae) {
            LOGGER.trace("sched_getaffinity pid={}, cpusetsize={}, mask={} threw {}", pid, cpusetsize, mask, iae.toString());
            throw iae;
        } catch (Exception e) {
            LOGGER.trace("sched_getaffinity pid={}, cpusetsize={}, mask={} threw {}", pid, cpusetsize, mask, e.toString());
            throw new PosixRuntimeException("sched_getaffinity call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int getpid() {
        try {
        int getpid = posix.getpid();
        LOGGER.trace("getpid returned {}", getpid);
        return getpid;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("getpid symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("getpid threw {}", e.toString());
            throw new PosixRuntimeException("getpid call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int gettid() {
        try {
        int gettid = posix.gettid();
        LOGGER.trace("gettid returned {}", gettid);
        return gettid;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("gettid symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("gettid threw {}", e.toString());
            throw new PosixRuntimeException("gettid call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int lastError() {
        return Runtime.getSystemRuntime().getLastError();
    }

    @Override
    public String strerror(int errno) {
        return posix.strerror(errno);
    }

    @Override
    public long clock_gettime(int clockId) {
        try {
        long clock_gettime = posix.clock_gettime(clockId);
        LOGGER.trace("clock_gettime clockId={} returned {}", clockId, clock_gettime);
        return clock_gettime;
        } catch (UnsatisfiedLinkError ule) {
            throw new UnsupportedOperationException("clock_gettime symbol not found or linking failed", ule);
        } catch (Exception e) {
            LOGGER.trace("clock_gettime clockId={} threw {}", clockId, e.toString());
            throw new PosixRuntimeException("clock_gettime call failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }
}
