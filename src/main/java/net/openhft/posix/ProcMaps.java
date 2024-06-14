package net.openhft.posix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * This class provides methods to read and parse the memory mappings from the /proc filesystem on Linux.
 * It allows users to retrieve memory mappings for the current process or a specified process ID (PID).
 */
public final class ProcMaps {
    // A list to hold the memory mappings
    private final List<Mapping> mappingList = new ArrayList<>();

    /**
     * Private constructor to initialize ProcMaps with mappings from the specified process.
     *
     * @param proc The process identifier (could be "self" for the current process).
     * @throws IOException If an I/O error occurs reading from the /proc filesystem.
     */
    private ProcMaps(Object proc) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/" + proc + "/maps"))) {
            for (String line; (line = br.readLine()) != null; ) {
                mappingList.add(new Mapping(line));
            }
        }
    }

    /**
     * Factory method to create a ProcMaps instance for the current process.
     *
     * @return A ProcMaps instance for the current process.
     * @throws IOException If an I/O error occurs reading from the /proc filesystem.
     */
    public static ProcMaps forSelf() throws IOException {
        return new ProcMaps("self");
    }

    /**
     * Factory method to create a ProcMaps instance for a specified process ID (PID).
     *
     * @param pid The process ID to read memory mappings for.
     * @return A ProcMaps instance for the specified PID.
     * @throws IOException If an I/O error occurs reading from the /proc filesystem.
     */
    public static ProcMaps forPID(int pid) throws IOException {
        return new ProcMaps(pid);
    }

    /**
     * Returns an unmodifiable list of memory mappings.
     *
     * @return An unmodifiable list of memory mappings.
     */
    public List<Mapping> list() {
        return unmodifiableList(mappingList);
    }

    /**
     * Finds the first memory mapping that matches the given predicate.
     *
     * @param test The predicate to test memory mappings.
     * @return The first matching memory mapping.
     */
    public Mapping findFirst(Predicate<? super Mapping> test) {
        return mappingList.stream()
                .filter(test)
                .findFirst()
                .get();
    }

    /**
     * Finds all memory mappings that match the given predicate.
     *
     * @param test The predicate to test memory mappings.
     * @return A list of all matching memory mappings.
     */
    public List<Mapping> findAll(Predicate<? super Mapping> test) {
        return mappingList.stream()
                .filter(test)
                .collect(toList());
    }
}
