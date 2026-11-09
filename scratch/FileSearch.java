import java.io.*;
import java.nio.file.*;
import java.util.*;
import tangosearch.engine.*;
import tangosearch.output.TangoFormatter;

public class FileSearch {

    static void run(SearchEngine engine, String q) {
        long t = System.nanoTime();
        SearchResult r = engine.search(q, 500);
        System.out.println(TangoFormatter.toJson(r));
        System.out.flush();
        System.err.println(
            "query took " + (System.nanoTime() - t) / 1_000_000 + " ms"
        );
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println(
                "Usage: FileSearch <root-directory> [queries...]"
            );
            return;
        }

        Path root = Paths.get(args[0]);
        List<String> queryArgs = new ArrayList<>(
            Arrays.asList(args).subList(1, args.length)
        );

        long start = System.nanoTime();
        if (args.length == 2 && args[1].equals("--mcp")) {
            new tangosearch.mcp.McpServer().run(System.in, System.out);
            return;
        }
        SearchEngine engine = new SearchEngine(root);
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.err.println("index built in " + ms + " ms");

        if (!queryArgs.isEmpty()) {
            for (String q : queryArgs) {
                if (!q.trim().isEmpty()) {
                    run(engine, q.trim());
                }
            }
        } else {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in)
            );
            String line;
            while ((line = in.readLine()) != null) {
                String q = line.trim();
                if (!q.isEmpty()) {
                    run(engine, q);
                }
            }
        }
    }
}
