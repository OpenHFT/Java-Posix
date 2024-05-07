package net.openhft.posix.internal.jnr;

//import net.openhft.affinity.AffinityLock;

import net.openhft.posix.*;
import net.openhft.posix.util.Histogram;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/*
     380 MB/s -> 31 delays of 100+ us.
     550 MB/s -> 51 delays of 100+ us.
     800 MB/s -> 75 delays of 100+ us.
   1000 MB/s -> 12 delays of 100+ us. (109 without affinity)
   1450 MB/s -> 196 delays of 100+ us (331 without affinity)

   no-delay
   50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.030 / 0.040  0.050 / 0.401  0.430 / 0.651  0.781 / 0.901  1.390 / 5.22  7.30 / 11.44 - 76.7

Messages; 2147483648 rate: 250000/second
read only time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.110 / 0.290  0.290 / 0.321  0.971 / 1.050  1.062 / 1.130  1.262 / 1.342  2.140 / 5.93 - 394
write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.090 / 0.210  0.261 / 0.281  0.470 / 0.591  0.691 / 0.771  0.911 / 0.991  1.230 / 4.15 - 26.3
read to write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.210 / 0.541  0.701 / 6.90  12.50 / 30.0  153.9 / 193.8  209.2 / 260  367 / 1219 - 7,560
Messages; 2147483648 rate: 500000/second
read only time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.110 / 0.290  0.290 / 0.581  1.030 / 1.050  1.122 / 1.250  1.342 / 2.460  5.29 / 11.31 - 362
write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.090 / 0.220  0.261 / 0.341  0.611 / 0.711  0.821 / 0.971  1.762 / 3.91  4.58 / 5.74 - 30.6
read to write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.401 / 0.561  2.132 / 11.95  18.34 / 32.0  146.7 / 190.7  208.1 / 346  1161 / 1518 - 22,640
Messages; 2147483648 rate: 1000000/second
write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.130 / 0.220  0.300 / 0.611  0.701 / 0.821  0.991 / 1.242  3.77 / 4.34  5.03 / 5.54 - 35.0
read only time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.391 / 0.401  1.062 / 1.150  1.170 / 1.270  1.382 / 1.510  2.452 / 4.50  7.32 / 13.01 - 263
read to write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.581 / 1.130  1.350 / 1.610  3.35 / 4.30  6.12 / 11.57  20.00 / 70.5  167.7 / 202.5 - 5,640
Messages; 838860800 rate: 1501502/second
read only time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.150 / 0.401  0.841 / 1.090  1.162 / 1.182  1.310 / 1.522  3.63 / 6.31  16.48 / 199.4 - 2025
write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.130 / 0.220  0.510 / 0.631  0.721 / 0.841  0.991 / 1.090  1.750 / 4.08  5.27 / 5.69 - 32.5
read to write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.581 / 1.290  1.982 / 3.55  5.99 / 123.0  184.1 / 201.5  334 / 1059  1804 / 5,510 - 21,400

Messages; 25769803776 rate: 1000000/second
write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.001 / 0.110  0.120 / 0.160  0.300 / 0.500  0.601 / 1.222  4.00 / 9.62  11.98 / 12.85 - 114.3
read only time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.001 / 0.130  0.210 / 0.210  0.531 / 0.911  0.971 / 1.142  4.23 / 4.97  10.54 / 12.78 - 1341
read to write time: 50/90 97/99 99.7/99.9 99.97/99.99 99.997/99.999 99.9997/99.9999 - worst was 0.001 / 0.391  1.142 / 10.83  14.96 / 21.09  28.6 / 45.6  166.7 / 206.6  386 / 1403 - 5,150

 */
public class BenchmarkMain {
    static final Unsafe UNSAFE;
    private static final long THROUGHPUT = Long.getLong("throughput", 1_400_000);
    static int[] blackhole = new int[512 / 4];

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final JNRPosixAPI jnr = new JNRPosixAPI();
        final String filename = "/data/tmp/mmap.file";
        final File file = new File(filename);
        file.delete();
        file.createNewFile();
        final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
        final long length = 1L << 30;
        int err = jnr.ftruncate(fd, length);
        assertEquals(0, err);

        assertEquals(0, jnr.du(filename));

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
        assertNotEquals(-1, addr);

        int err4 = jnr.madvise(addr, length, MAdviseFlag.MADV_SEQUENTIAL);
        assertEquals(0, err4);
        int err5 = jnr.madvise(addr, length, MAdviseFlag.MADV_HUGEPAGE);
        assertEquals(0, err5);

        ProcMaps procMaps = ProcMaps.forSelf();
        final List<Mapping> list = procMaps.findAll(m -> m.path().contains(filename));
        assertEquals(1, list.size());
        final Mapping mapping = list.get(0);
        System.out.println(mapping);
        assertEquals(addr, mapping.addr());
        assertEquals(length, mapping.length());
        assertEquals(0L, mapping.offset());

        assertEquals(0, jnr.du(filename));

        AtomicLong upto = new AtomicLong();
        Thread msync = new Thread(() -> {
            jnr.sched_setaffinity_as(jnr.gettid(), 15);
            try {
                long prev = 0, next = 0;
                while (true) {
                    boolean busy = false;
                    long latest = upto.get();
                    long next0 = ((latest >> 21) + 2) << 21;
                    if (next0 != next) {
                        // allocate a huge page in advance.
                        int err3 = jnr.fallocate(fd, 0, next0, 1L << 21);
                        assertEquals(0, err3);
                        next = next0;
                        busy = true;
                    }

                    long len = latest - prev;
                    if (len > 0) {
                        busy = true;
                        // flush every huge page so far.
                        final int err0 = jnr.msync(addr + prev, len, MSyncFlag.MS_ASYNC);
                        if (err0 == 0) {
                            prev = latest;
                        } else {
                            System.err.println("msync: errno= " + err0);
                            break;
                        }
                    }
                    if (!busy)
                        Thread.sleep(1);
                }
            } catch (InterruptedException ie) {
                System.out.println("msync done.");
            }
        });
        msync.setDaemon(true);
        msync.start();

        final int SIZE = 256;
        Thread reader = new Thread(() -> {
            jnr.sched_setaffinity_as(jnr.gettid(), 14);

            long warmup = 1_000_000;
            final int rfd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
            long raddr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, rfd, 0L);
            Histogram readToWrite = new Histogram();
            Histogram readTime = new Histogram();
            for (long i = 0; i < length; i += SIZE) {
                if (i % (2 << 20) == 0 && i % 100000 == 0) {
                    System.out.println("read to write time: " + readToWrite.toLongMicrosFormat());
                }
                for (int len; (len = UNSAFE.getIntVolatile(null, raddr + i)) == 0; ) {
                    if (Thread.currentThread().isInterrupted())
                        throw new AssertionError("length: " + i);
                }
                long rstart = System.nanoTime();
                long wstart = UNSAFE.getLong(raddr + i + 4);
                for (int y = 12; y < SIZE; y += 4) {
                    blackhole[y / 4] = UNSAFE.getInt(raddr + i + y);
                }
                long rend = System.nanoTime();
                readToWrite.sample(rend - wstart);
                readTime.sample(rend - rstart);
                if (--warmup == 0) {
                    System.out.println("Warm up complete");
                    readTime.reset();
                    readToWrite.reset();
                }
            }
            System.out.println("read only time: " + readTime.toLongMicrosFormat());
            System.out.println("read to write time: " + readToWrite.toLongMicrosFormat());
        });
        reader.start();

        jnr.sched_setaffinity_as(jnr.gettid(), 13);
        Histogram histogram = new Histogram();
        long next = System.nanoTime();
        long start0 = next;
        long interval = (long) (1e9 / THROUGHPUT);
        System.out.println("Interval: " + interval);
        long warmup = 1_000_000;
        for (long i = 0; i < length; i += 2L << 20) {
            for (int j = 0; j < 2L << 20; j += 4 << 10) {
                // fault in one page in advance.
                final long fault = addr + i + j;
//                System.out.println("Fault: "+(fault - addr));
                UNSAFE.compareAndSwapLong(null, fault, 0, 0);
                for (int x = 0; x < (4 << 10); x += SIZE) {
                    final long raddr = addr + i + j + x;
                    long start = System.nanoTime();
                    UNSAFE.putLong(raddr + 4, start);
                    for (int y = 12; y < SIZE; y += 4) {
                        // int does a little more work than long.
                        UNSAFE.putInt(raddr + y, y);
                    }
//                    System.out.println("header " + (raddr - addr));
                    UNSAFE.putIntVolatile(null, raddr, SIZE);
                    long time = System.nanoTime() - start;
                    histogram.sample(time);
                    if (--warmup == 0)
                        histogram.reset();
                    if (time > 20_000)
                        System.out.println("i: " + i + ", took " + time / 1000 + " us.");
                    next += interval;
                    while (System.nanoTime() < next) {
                    }
                }
            }
            upto.set(i + (1L << 21));
        }
        final long messages = length / SIZE;
        final long time = System.nanoTime() - start0;
        System.out.println("Messages; " + messages + " rate: " + Math.round(1e9 * messages / time) + "/second");
        System.out.println("write time: " + histogram.toLongMicrosFormat());
        msync.interrupt();
        msync.join();
        reader.interrupt();
        reader.join();
        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1);
        int err2 = jnr.close(fd);
        assertEquals(0, err2);
        assertTrue(file.exists());
        file.delete();
    }
}
