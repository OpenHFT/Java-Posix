/**
 * This package defines the core public interfaces, classes, and enums for the OpenHFT Posix library.
 * <p>
 * It includes:
 * <ul>
 *     <li>{@link net.openhft.posix.PosixAPI} — the central interface for interacting with POSIX-like
 *     operations (open, close, read, write, memory mapping, CPU affinity, etc.).</li>
 *     <li>Enums such as {@link net.openhft.posix.OpenFlag}, {@link net.openhft.posix.LockfFlag},
 *     and others that mirror POSIX constants for file handling, memory advice, and more.</li>
 *     <li>Supporting classes for exception handling (e.g. {@link net.openhft.posix.PosixRuntimeException}).</li>
 * </ul>
 * <p>
 * <strong>Note:</strong> All methods exposed by this package are for advanced usage. Some features
 * may be unsupported on certain platforms or JVM distributions and can throw
 * {@link java.lang.UnsupportedOperationException} if not available.
 */
package net.openhft.posix;
