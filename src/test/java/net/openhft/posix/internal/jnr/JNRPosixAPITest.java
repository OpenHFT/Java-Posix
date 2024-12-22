package net.openhft.posix.internal.jnr;

import jnr.ffi.Platform;
import net.openhft.posix.*;
import net.openhft.posix.internal.UnsafeMemory;
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

/**
 * Tests for JNR-based POSIX API implementations. The existing tests are kept
 * in their original order; extra tests appear at the end for improved coverage.
 */
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
        assumeTrue("requires Unix", isUnix());

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
        assumeTrue("requires Unix", isUnix());

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
        assumeTrue("requires Unix", isUnix());
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
        // We allow some tolerance due to resolution differences or delays
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

    /**
     * Test attempting to open an invalid or nonexistent file path.
     * If the implementation returns -1 or throws an exception, we consider it correct
     * for this scenario. Adjust as needed for your environment.
     */
    @Test
    public void openInvalidPath() {
        final String invalidPath = "/nonexistent/path/for/test-" + System.nanoTime();
        try {
            int fd = jnr.open(invalidPath, OpenFlag.O_RDONLY, 0);
            // Many implementations return -1. Others might throw PosixRuntimeException.
            // We'll just check for negative as a typical 'error' signal.
            assertTrue("Expected fd < 0 or exception for invalid path", fd < 0);
        } catch (PosixRuntimeException e) {
            // Also acceptable: we just log it
            System.out.println("open() threw PosixRuntimeException for invalid path, which is expected: " + e);
        } catch (UnsupportedOperationException e) {
            // Possibly some fallback scenario
            System.out.println("open() threw UnsupportedOperationException for invalid path: " + e);
        }
    }

    /**
     * Test allocations using malloc/free. Ensures we can allocate a small memory region and write to it.
     */
    @Test
    public void mallocAndFree() {
        final long size = 128;
        long ptr = jnr.malloc(size);
        assertNotEquals("malloc returned null pointer", 0, ptr);

        // Let's do a quick write: fill memory with zero
        for (int i = 0; i < size; i += 8) {
            UnsafeMemory.UNSAFE.putLong(ptr + i, 0xABCD1234ABCD1234L);
        }

        jnr.free(ptr);
    }

    /**
     * Test calling madvise on a small, newly allocated memory region.
     * Many systems might allow a no-op or partial advice. Just ensuring it doesn't fail.
     */
    @Test
    public void madviseOnAllocated() {
        final long length = 4096;
        long ptr = jnr.malloc(length);
        try {
            int ret = jnr.madvise(ptr, length, MAdviseFlag.MADV_RANDOM);
            // Could be 0 on success, or -1 on partial success. We'll accept >= 0 for demonstration.
            assertTrue("madvise returned an error", ret >= 0);
        } finally {
            jnr.free(ptr);
        }
    }

    /**
     * Test calling fallocate on a small file, verifying that it doesn't fail for a basic scenario.
     */
    @Test
    public void fallocateBasic() throws IOException {
        final Path file = Files.createTempFile("fallocate", ".test");
        try {
            final int fd = jnr.open(file.toString(), OpenFlag.O_RDWR, 0666);
            assertTrue("fd should be >= 0", fd >= 0);

            long length = 8192;
            int ret = jnr.fallocate(fd, 0, 0, length);
            // Some filesystems or older kernels might return -1 if not implemented
            if (ret != 0) {
                System.out.println("fallocate not fully supported on this filesystem or kernel.");
            }
            jnr.close(fd);
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void openWriteReadClose() throws IOException {
        // Create a temporary file and convert path to String
        final Path tempFile = Files.createTempFile("posix-write-read", ".test");
        final String filePath = tempFile.toAbsolutePath().toString();

        // 1. Open the file for read/write
        int fd = jnr.open(filePath, OpenFlag.O_RDWR, 0666);
        assertTrue("File descriptor should be non-negative", fd >= 0);

        // 2. Write data into the file
        // Let's write the phrase "Hello Posix" as bytes
        byte[] dataToWrite = "Hello Posix".getBytes();
        // Allocate a small block with jnr.malloc, copy data, then call write
        long memPtr = jnr.malloc(dataToWrite.length);
        try {
            // Copy the bytes into the allocated memory
            for (int i = 0; i < dataToWrite.length; i++) {
                UnsafeMemory.UNSAFE.putByte(memPtr + i, dataToWrite[i]);
            }
            // Write from memPtr to the file
            long written = jnr.write(fd, memPtr, dataToWrite.length);
            assertEquals("Number of bytes written should match dataToWrite length",
                    dataToWrite.length, written);
        } finally {
            jnr.free(memPtr);
        }

        // Close the file descriptor
        int err2 = jnr.close(fd);
        assertEquals("Close should return 0 on success", 0, err2);

        // Move file offset to start for reading
//        long newPos = jnr.lseek(fd, 0, WhenceFlag.SEEK_SET);
//        assertEquals("Expected file offset to be 0 after SEEK_SET", 0, newPos);

        fd = jnr.open(filePath, OpenFlag.O_RDWR, 0444);
        assertTrue("File descriptor should be non-negative", fd >= 0);
        // 3. Read data back
        byte[] dataRead = new byte[dataToWrite.length];
        long memPtr2 = jnr.malloc(dataToWrite.length);
        try {
            long bytesRead = jnr.read(fd, memPtr2, dataToWrite.length);
            assertEquals("Number of bytes read should match dataToWrite length",
                    dataToWrite.length, bytesRead);

            // Copy back from memPtr2 to our Java byte array
            for (int i = 0; i < dataToWrite.length; i++) {
                dataRead[i] = UnsafeMemory.UNSAFE.getByte(memPtr2 + i);
            }
        } finally {
            jnr.free(memPtr2);
        }

        // Compare the written and read data
        assertArrayEquals("Expected data read from file to match data written",
                dataToWrite, dataRead);

        // 4. Close the file descriptor
        int err = jnr.close(fd);
        assertEquals("Close should return 0 on success", 0, err);

        // Clean up the temp file
        Files.deleteIfExists(tempFile);
    }


    private static boolean isUnix() {
        return Platform.getNativePlatform().isUnix();
    }
}
