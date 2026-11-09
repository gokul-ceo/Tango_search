package tangosearch.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchEngine {

    private final Path root;
    private final List<Path> files;
    private final Map<String, Set<Integer>> index;

    public SearchEngine(Path root) throws IOException {
        this.root = root;
        try (Stream<Path> walk = Files.walk(root)) {
            this.files = walk
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(Collectors.toList());
        }
        this.index = TrigramIndex.build(this.files);
    }

    public SearchResult search(String query, int maxTokens) {
        Set<Integer> c = TrigramIndex.candidate(query, index, files);
        List<Hit> found = Verifier.verify(query, c, files, root);
        found.sort((a, b) ->
            Integer.compare(Ranker.rank(b, query), Ranker.rank(a, query))
        );

        // pack the hits into the budget: your loop from TangoFormatter.toJson goes here.
        // It builds `packed` (a List<Hit>), `used`, and `shown`.
        List<Hit> packed = new ArrayList<>();
        int used = 0;
        for (Hit h : found) {
            int cost = h.format().length() / 4 + 1;
            if (used + cost > maxTokens) {
                break;
            }
            packed.add(h);
            used += cost;
        }

        return new SearchResult(
            query,
            found.size(),
            packed.size(),
            used,
            maxTokens,
            packed
        );
    }
}
