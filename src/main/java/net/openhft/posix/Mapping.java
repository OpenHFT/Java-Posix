package net.openhft.posix;

import net.openhft.posix.internal.UnsafeMemory;

/**
 * This class represents a memory mapping in the /proc/[pid]/maps file.
 * It parses and stores the details of a single memory mapping.
 */
public final class Mapping {
    // The start address of the memory mapping
    private final long addr;

    // The length of the memory mapping
    private final long length;

    // The offset into the file/VM object to which the memory mapping refers
    private final long offset;

    // The inode on the device
    private final long inode;

    // The permissions of the memory mapping (e.g., r-xp)
    private final String perms;

    // The device (major:minor)
    private final String device;

    // The file path associated with the memory mapping
    private final String path;

    // The original line from the /proc/[pid]/maps file
    private final String toString;

    /**
     * Constructs a Mapping object by parsing a line from the /proc/[pid]/maps file.
     *
     * @param line A line from the /proc/[pid]/maps file.
     */
    public Mapping(String line) {
        String[] parts = line.split(" +");
        String[] addrs = parts[0].split("\\-");
        long addr0 = Long.parseUnsignedLong(addrs[0], 16);
        addr = UnsafeMemory.IS32BIT ? (int) addr0 : addr0;
        length = Long.parseUnsignedLong(addrs[1], 16) - addr0;
        perms = parts[1];
        offset = Long.parseUnsignedLong(parts[2], 16);
        device = parts[3];
        inode = Long.parseLong(parts[4]);
        path = parts.length >= 6 ? parts[5] : "";
        toString = line;
    }

    /**
     * Returns the start address of the memory mapping.
     *
     * @return The start address of the memory mapping.
     */
    public long addr() {
        return addr;
    }

    /**
     * Returns the length of the memory mapping.
     *
     * @return The length of the memory mapping.
     */
    public long length() {
        return length;
    }

    /**
     * Returns the offset into the file/VM object to which the memory mapping refers.
     *
     * @return The offset into the file/VM object.
     */
    public long offset() {
        return offset;
    }

    /**
     * Returns the inode on the device.
     *
     * @return The inode on the device.
     */
    public long inode() {
        return inode;
    }

    /**
     * Returns the permissions of the memory mapping.
     *
     * @return The permissions of the memory mapping.
     */
    public String perms() {
        return perms;
    }

    /**
     * Returns the device (major:minor) of the memory mapping.
     *
     * @return The device of the memory mapping.
     */
    public String device() {
        return device;
    }

    /**
     * Returns the file path associated with the memory mapping.
     *
     * @return The file path associated with the memory mapping.
     */
    public String path() {
        return path;
    }

    /**
     * Returns the original line from the /proc/[pid]/maps file.
     *
     * @return The original line from the /proc/[pid]/maps file.
     */
    @Override
    public String toString() {
        return toString;
    }
}
