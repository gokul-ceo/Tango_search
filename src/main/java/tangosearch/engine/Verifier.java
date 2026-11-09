package tangosearch.engine;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Verifier {

    public static List<Hit> verify(
        String query,
        Set<Integer> candidates,
        List<Path> files,
        Path root
    ) {
        List<Hit> hits = new ArrayList<>();

        for (int id : new TreeSet<>(candidates)) {
            Path p = files.get(id);
            List<String> lines;
            try {
                lines = Files.readAllLines(p);
            } catch (IOException e) {
                continue;
            }

            for (int n = 0; n < lines.size(); n++) {
                if (lines.get(n).contains(query)) {
                    hits.add(
                        new Hit(
                            root.relativize(p).toString(),
                            n + 1,
                            lines.get(n).trim()
                        )
                    );
                }
            }
        }
        return hits;
    }
}
