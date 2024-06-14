package net.openhft.posix;

/**
 * This enum represents the different flags for mmap operations.
 * It defines the flags for memory mapping operations, including shared and private mappings.
 */
public enum MMapFlag {
    // Memory mapping to be shared with other processes
    SHARED(1),

    // Memory mapping to be private to the process
    PRIVATE(2);

    // The integer value representing the mmap flag
    private int value;

    /**
     * Constructor for MMapFlag.
     *
     * @param value The integer value representing the mmap flag
     */
    MMapFlag(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this MMapFlag object.
     *
     * @return The current integer value of this MMapFlag object
     */
    public int value() {
        return value;
    }
}
