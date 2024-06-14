package net.openhft.posix.internal;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * This enum provides access to the {@link Unsafe} class and some system memory properties.
 * It allows low-level, unsafe operations on memory, which are normally not accessible through standard Java APIs.
 */
public enum UnsafeMemory {
    // Empty enum to prevent instantiation
    ;

    // The Unsafe instance for performing low-level operations
    public static final Unsafe UNSAFE;

    static {
        try {
            // Access the Unsafe instance via reflection
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    // Indicates if the JVM is running in a 32-bit environment
    public static final boolean IS32BIT = UNSAFE.addressSize() == Integer.BYTES;

    // Indicates if the JVM is running in a 64-bit environment
    public static final boolean IS64BIT = UNSAFE.addressSize() == Long.BYTES;
}
