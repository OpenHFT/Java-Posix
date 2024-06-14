package net.openhft.posix;

/**
 * This enum represents the different flags for seeking within a file (whence).
 * It defines the options used to set the file offset for read/write operations.
 */
public enum WhenceFlag {
    /**
     * The offset is set to offset bytes.
     */
    SEEK_SET(1),

    /**
     * The offset is set to its current location plus offset bytes.
     */
    SEEK_CUR(2),

    /**
     * The offset is set to the size of the file plus offset bytes.
     */
    SEEK_END(3),

    /**
     * Adjust the file offset to the next location in the file greater than or equal to offset containing data.
     * If offset points to data, then the file offset is set to offset.
     */
    SEEK_DATA(4),

    /**
     * Adjust the file offset to the next hole in the file greater than or equal to offset.
     * If offset points into the middle of a hole, then the file offset is set to offset.
     * If there is no hole past offset, then the file offset is adjusted to the end of the file (i.e., there is an implicit hole at the end of any file).
     */
    SEEK_HOLE(5);

    // The integer value representing the whence flag
    private final int value;

    /**
     * Constructor for WhenceFlag.
     *
     * @param value The integer value representing the whence flag
     */
    WhenceFlag(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this WhenceFlag object.
     *
     * @return The current integer value of this WhenceFlag object
     */
    public int value() {
        return value;
    }
}
