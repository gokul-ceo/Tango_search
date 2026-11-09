package tangosearch.engine;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TrigramIndex {

    public static Map<String, Set<Integer>> build(List<Path> files) {
        Map<String, Set<Integer>> index = new HashMap<>();
        int skippedFiles = 0;

        for (int i = 0; i < files.size(); i++) {
            String text;
            try {
                text = Files.readString(files.get(i));
            } catch (IOException e) {
                skippedFiles++;
                continue;
            }

            for (String t : TrigramBuilder.resolve(text)) {
                index.computeIfAbsent(t, k -> new HashSet<>()).add(i);
            }
        }
        System.err.println("skipped " + skippedFiles + " unreadable files");
        return index;
    }

    public static Set<Integer> candidate(
        String query,
        Map<String, Set<Integer>> index,
        List<Path> files
    ) {
        if (null == query) {
            return new HashSet<>();
        }

        Set<Integer> candidates = null;
        for (String q : TrigramBuilder.resolve(query)) {
            Set<Integer> hits = index.get(q);
            if (null == hits) {
                return new HashSet<>();
            }
            if (null == candidates) {
                candidates = new HashSet<>(hits);
            } else {
                candidates.retainAll(hits);
            }
        }
        if (candidates == null) {
            Set<Integer> all = new HashSet<>();
            for (int i = 0; i < files.size(); i++) {
                all.add(i);
            }
            return all;
        }

        return candidates;
    }
}
