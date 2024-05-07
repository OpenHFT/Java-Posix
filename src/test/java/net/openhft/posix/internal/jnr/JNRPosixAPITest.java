package net.openhft.posix.internal.jnr;

import jnr.ffi.Platform;
import net.openhft.posix.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Supplier;

import static net.openhft.posix.internal.core.OS.isMacOSX;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class JNRPosixAPITest {

    static final PosixAPI jnr = isUnix() ? new JNRPosixAPI() : new WinJNRPosixAPI();

    @Test
    public void open() throws IOException {
        final Path file = Files.createTempFile("open", ".test");
        final int fd = jnr.open(file.toString(), OpenFlag.O_RDWR, 0666);
        assertEquals(0, jnr.lseek(fd, 0, WhenceFlag.SEEK_SET));
        assertEquals(-1, jnr.lseek(fd, 16, WhenceFlag.SEEK_DATA));
        assertEquals(0, jnr.ftruncate(fd, 4096));
        // 'lseek' on Windows and macOS doesn't support following behavior
        if (isUnix() && !isMacOSX()) {
            assertEquals(16, jnr.lseek(fd, 16, WhenceFlag.SEEK_DATA));
            assertEquals(4095, jnr.lseek(fd, 4095, WhenceFlag.SEEK_DATA));
            assertEquals(-1, jnr.lseek(fd, 4096, WhenceFlag.SEEK_DATA));
        }

        int err = jnr.close(fd);
        assertEquals(0, err);
        assertTrue(file.toFile().exists());
        file.toFile().delete();
    }

    @Test
    public void close() {
        int err = jnr.close(-1);
        assertEquals(-1, err);
    }

    @Test
    public void mmap_sync() throws IOException {
        assumeTrue(new File("/proc/self").exists());
        final Path file = Files.createTempFile("mmap", ".test");
        final String filename = file.toAbsolutePath().toString();
        final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
        final long length = 1L << 16;
        int err = jnr.ftruncate(fd, length);
        assertEquals(0, err);

        assertEquals(0, jnr.du(filename));

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
        assertNotEquals(-1, addr);

        int err4 = jnr.madvise(addr, length, MAdviseFlag.MADV_SEQUENTIAL);
        assertEquals(0, err4);

        ProcMaps procMaps = ProcMaps.forSelf();
        final List<Mapping> list = procMaps.findAll(m -> filename.equals(m.path()));
        assertEquals(1, list.size());
        final Mapping mapping = list.get(0);
        assertEquals(addr, mapping.addr());
        assertEquals(length, mapping.length());
        assertEquals(0L, mapping.offset());

        assertEquals(0, jnr.du(filename));
        int err3 = jnr.fallocate(fd, 0, 0, length);
        assertEquals(0, err3);
        assertEquals(length >> 10, jnr.du(filename));

        final int err0 = jnr.msync(addr, length, MSyncFlag.MS_ASYNC);
        assertEquals(0, err0);

        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1);
        int err2 = jnr.close(fd);
        assertEquals(0, err2);
        assertTrue(file.toFile().exists());
        file.toFile().delete();
    }

    @Test
    public void mlockall() {
        assumeFalse("macOS doesn't support 'mlockall'", isMacOSX());

        PosixAPI.posix().mlockall(MclFlag.MclCurrent);
    }

    @Test
    public void mlock() throws IOException {
        assumeTrue(new File("/proc/self").exists());
        final Path file = Files.createTempFile("mmap", ".test");
        final String filename = file.toAbsolutePath().toString();
        final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
        final long length = 1L << 16;
        int err = jnr.ftruncate(fd, length);
        assertEquals(0, err);

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
        assertNotEquals(-1, addr);

        jnr.mlock(addr, length);

        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1);
        int err2 = jnr.close(fd);
        assertEquals(0, err2);
        assertTrue(file.toFile().exists());
        file.toFile().delete();
    }

    @Test
    public void mlock2() throws IOException {
        assumeTrue(new File("/proc/self").exists());
        final Path file = Files.createTempFile("mmap", ".test");
        final String filename = file.toAbsolutePath().toString();
        final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
        final long length = 1L << 16;
        int err = jnr.ftruncate(fd, length);
        assertEquals(0, err);

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
        assertNotEquals(-1, addr);

        jnr.mlock2(addr, length, true);

        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1);
        int err2 = jnr.close(fd);
        assertEquals(0, err2);
        assertTrue(file.toFile().exists());
        file.toFile().delete();
    }

    @Test
    public void gettimeofday() {
        long firstCallIsSlow1 = jnr.gettimeofday();
        long firstCallIsSlow2 = jnr.clock_gettime();

        long time = jnr.gettimeofday();
        long clock_gettime = jnr.clock_gettime();
        assertNotEquals(0, time);
        assertEquals(System.currentTimeMillis() * 1_000L, time, 2_000);
        assertEquals(clock_gettime / 1000.0, time, 1_000);
    }

    @Test
    public void get_nprocs() {
        assumeFalse("macOS doesn't support 'get_nprocs'", isMacOSX());

        final int nprocs = jnr.get_nprocs();
        assertTrue(nprocs > 0);
        final int nprocs_conf = jnr.get_nprocs_conf();
        assertTrue(nprocs <= nprocs_conf);
    }

    /**
     * Applies the given int supplier N times over N threads, adding each result to a set
     * @return - the size of the set
     */
    int poolIntReduce(int N, Supplier<Integer> r) throws InterruptedException {
        final ConcurrentSkipListSet<Integer> items = new ConcurrentSkipListSet<>();
        final ArrayList<Thread> threads = new ArrayList<>();

        for (int i = 0; i < N; ++i) {
            Thread t = new Thread(() -> items.add(r.get()));
            t.start();
            threads.add(t);
        }
        for (Thread t : threads) t.join();

        return items.size();
    }

    @Test
    public void getpid() throws InterruptedException {
        assumeFalse("macOS doesn't support 'getpid'", isMacOSX());

        final int N = jnr.get_nprocs();
        assertEquals(1, poolIntReduce(N, jnr::getpid));
    }

    @Test
    public void gettid() throws InterruptedException {
        assumeFalse("macOS doesn't support 'gettid'", isMacOSX());

        final int N = jnr.get_nprocs();
        assertEquals(N, poolIntReduce(N, jnr::gettid));

        if (new File("/proc").isDirectory()) {
            final int gettid = jnr.gettid();
            assertTrue(new File("/proc/self/task/" + gettid).exists());
        }
    }

    @Test
    public void setaffinity() {
        assumeTrue("Windows and macOS doesn't support 'setaffinity'", isUnix() && !isMacOSX());

        int gettid = jnr.gettid();
        assertEquals(0, jnr.sched_setaffinity_as(gettid, 1));
        assertEquals("1-1", jnr.sched_getaffinity_summary(gettid));
        assertEquals(0, jnr.sched_setaffinity_range(gettid, 2, 3));
        assertEquals("2-3", jnr.sched_getaffinity_summary(gettid));
        assertEquals(0, jnr.sched_setaffinity_range(gettid, 0, jnr.get_nprocs_conf()));
    }

    @Test
    public void clocks() {
        assumeTrue(isUnix());

        for (ClockId value : ClockId.values()) {
            try {
                final long gettime = jnr.clock_gettime(value);
                System.out.println(value + ": " + gettime);
            } catch (IllegalArgumentException e) {
                System.out.println(value + ": " + e);
            }
        }
    }

    private static boolean isUnix() {
        return Platform.getNativePlatform().isUnix();
    }
}
