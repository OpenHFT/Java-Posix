package net.openhft.posix;

public enum MSyncFlag {
    //#define MS_ASYNC        1               /* sync memory asynchronously */
    /**
     * sync memory asynchronously
     */
    MS_ASYNC(1),
    //#define MS_INVALIDATE   2               /* invalidate the caches */
    /**
     * invalidate the caches
     */
    MS_INVALIDATE(2),
    //#define MS_SYNC         4               /* synchronous memory sync */
    /**
     * synchronous memory sync
     */
    MS_SYNC(4);

    private final int value;

    MSyncFlag(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }
}
