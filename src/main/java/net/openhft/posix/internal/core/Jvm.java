package net.openhft.posix.internal.core;

import net.openhft.posix.internal.UnsafeMemory;

/**
 * Utility class to access information about the JVM.
 */
public final class Jvm {

    // Suppresses default constructor, ensuring non-instantiability
    private Jvm() {
    }

    // The architecture of the operating system
    static final String OS_ARCH = System.getProperty("os.arch", "?");

    // The vendor of the Java Virtual Machine
    static final String VM_VENDOR = System.getProperty("java.vm.vendor", "?");

    /**
     * Checks if the JVM is running on an ARM architecture.
     *
     * @return true if the JVM is running on an ARM architecture, false otherwise.
     */
    public static boolean isArm() {
        return Boolean.parseBoolean(System.getProperty("jvm.isarm")) ||
                OS_ARCH.startsWith("arm") || OS_ARCH.startsWith("aarch");
    }

    /**
     * Checks if the JVM is 64-bit.
     *
     * @return true if the JVM is 64-bit, false otherwise.
     */
    public static boolean is64bit() {
        return UnsafeMemory.IS64BIT;
    }

    /**
     * Checks if the JVM is provided by Azul Systems.
     *
     * @return true if the JVM vendor is Azul Systems, false otherwise.
     */
    public static boolean isAzul() {
        return VM_VENDOR.startsWith("Azul ");
    }
}
