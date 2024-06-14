package net.openhft.posix;

/**
 * This enum represents the different memory protection options for mmap.
 * It defines the protection levels for memory mapping operations, including read, write, execute, and none.
 */
public enum MMapProt {
    // Memory protection to allow read access
    PROT_READ(1),

    // Memory protection to allow write access
    PROT_WRITE(2),

    // Memory protection to allow both read and write access
    PROT_READ_WRITE(3),

    // Memory protection to allow execute access
    PROT_EXEC(4),

    // Memory protection to allow both execute and read access
    PROT_EXEC_READ(5),

    // Memory protection to allow no access
    PROT_NONE(8);

    // The integer value representing the memory protection level
    final int value;

    /**
     * Constructor for MMapProt.
     *
     * @param value The integer value representing the memory protection level
     */
    MMapProt(int value) {
        this.value = value;
    }

    /**
     * This method is a getter for the value instance variable.
     * It returns the current integer value of this MMapProt object.
     *
     * @return The current integer value of this MMapProt object
     */
    public int value() {
        return value;
    }
}
