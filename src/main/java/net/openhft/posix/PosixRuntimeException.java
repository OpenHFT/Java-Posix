package net.openhft.posix;

/**
 * This class represents a runtime exception specific to POSIX operations.
 * It extends the standard {@link RuntimeException} to provide more specific error handling for POSIX-related errors.
 */
public class PosixRuntimeException extends RuntimeException {
    // Serialization version UID for ensuring compatibility during deserialization
    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new PosixRuntimeException with the specified detail message.
     *
     * @param message The detail message for the exception.
     */
    public PosixRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new PosixRuntimeException with the specified cause.
     *
     * @param cause The cause of the exception.
     */
    public PosixRuntimeException(Throwable cause) {
        super(cause);
    }
}
