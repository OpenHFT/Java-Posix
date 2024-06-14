package net.openhft.posix;

/**
 * This enum represents the different flags for memory locking (mlockall) operations.
 * It defines the flags used to control how pages are locked in memory.
 */
public enum MclFlag {
    // Lock all current pages in memory
    MclCurrent(1),

    // Lock all future pages in memory
    MclFuture(2),

    // Lock all current pages in memory on fault
    MclCurrentOnFault(1 + 4),

    // Lock all future pages in memory on fault
    MclFutureOnFault(2 + 4);

    // The integer code representing the mlockall flag
    private int code;

    /**
     * Constructor for MclFlag.
     *
     * @param code The integer code representing the mlockall flag
     */
    MclFlag(int code) {
        this.code = code;
    }

    /**
     * This method is a getter for the code instance variable.
     * It returns the current integer code of this MclFlag object.
     *
     * @return The current integer code of this MclFlag object
     */
    public int code() {
        return code;
    }
}
