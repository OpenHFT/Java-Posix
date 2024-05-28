package net.openhft.posix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.*;

public final class ProcMaps {
    private final List<Mapping> mappingList = new ArrayList<>();

    private ProcMaps(Object proc) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/" + proc + "/maps"))) {
            for (String line; (line = br.readLine()) != null; ) {
                mappingList.add(new Mapping(line));
            }
        }
    }

    public static ProcMaps forSelf() throws IOException {
        return new ProcMaps("self");
    }

    public static ProcMaps forPID(int pid) throws IOException {
        return new ProcMaps(pid);
    }

    public List<Mapping> list() {
        return unmodifiableList(mappingList);
    }

    public Mapping findFirst(Predicate<? super Mapping> test) {
        return mappingList.stream()
                .filter(test)
                .findFirst()
                .get();
    }

    public List<Mapping> findAll(Predicate<? super Mapping> test) {
        return mappingList.stream()
                .filter(test)
                .collect(toList());
    }
}
