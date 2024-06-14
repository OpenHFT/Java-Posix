package net.openhft.posix;

/**
 * This enum represents the different flags for memory synchronization (msync) operations.
 * It defines the flags used to control the behavior of memory synchronization operations.
 */
public enum MSyncFlag {
    /**
     * Sync memory asynchronously.
     */
    MS_ASYNC(1),

    /**
     * Invalidate the caches.
     */
    MS_INVALIDATE(2),

    /**
     * Synchronous memory sync.
     */
    MS_SYNC(4);

    // The integer value representing the msync flag
    private final int value;

    /**
     * Constructor for MSyncFlag.
     *
     * @param value The integer value representing the msync flag
     */
    MSyncFlag(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this MSyncFlag object.
     *
     * @return The current integer value of this MSyncFlag object
     */
    public int value() {
        return value;
    }
}
