package net.openhft.posix;

/**
 * This enum represents the different flags for opening files (open).
 * It defines the flags used to control the behavior of file opening operations.
 */
public enum OpenFlag {
    // Open for reading only
    O_RDONLY(0x0000),

    // Open for writing only
    O_WRONLY(0x0001),

    // Open for reading and writing
    O_RDWR(0x0002),

    // No delay (non-blocking mode)
    O_NONBLOCK(0x0004),

    // Set append mode
    O_APPEND(0x0008),

    // Open with shared file lock
    O_SHLOCK(0x0010),

    // Open with exclusive file lock
    O_EXLOCK(0x0020),

    // Signal pgrp when data is ready (asynchronous mode)
    O_ASYNC(0x0040),

    // Synchronous writes
    O_FSYNC(0x0080),

    // Create if non-existent
    O_CREAT(0x0200),

    // Truncate to zero length
    O_TRUNC(0x0400),

    // Error if already exists
    O_EXCL(0x0800);

    // The integer value representing the open flag
    final int value;

    /**
     * Constructor for OpenFlag.
     *
     * @param value The integer value representing the open flag
     */
    OpenFlag(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this OpenFlag object.
     *
     * @return The current integer value of this OpenFlag object
     */
    public int value() {
        return value;
    }
}
