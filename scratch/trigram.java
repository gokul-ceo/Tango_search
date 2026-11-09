import java.io.*;
import java.util.*;

public class TrigramFunction {

    public static List<String> generateDocuments(int count) {
        String[] words = {
            "retry",
            "batch",
            "job",
            "config",
            "failed",
            "server",
            "database",
            "queue",
            "worker",
            "timeout",
            "request",
            "process",
            "cache",
            "network",
            "random",
            "hello",
            "world",
        };

        Random random = new Random(42);
        List<String> docs = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            int wordCount = 3 + random.nextInt(8); // 3-10 words

            StringBuilder doc = new StringBuilder();

            for (int j = 0; j < wordCount; j++) {
                if (j > 0) {
                    doc.append(" ");
                }

                String word = words[random.nextInt(words.length)];
                doc.append(word);
            }

            docs.add(doc.toString());
        }

        return docs;
    }

    static List<String> trigrams(String text) {
        List<String> result = new ArrayList<>();
        if (text.length() < 3) {
            return result;
        }
        if (text.length() == 3) {
            result.add(text);
            return result;
        }
        if (text.length() > 3) {
            for (int i = 0; i < text.length(); i++) {
                if (i + 3 <= text.length()) {
                    String tempTrigram = text.substring(i, i + 3);
                    result.add(tempTrigram);
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        List<String> generatedDoc = generateDocuments(100_000);
        generatedDoc.set(54_321, "zebracrossing hello world");

        Map<String, Set<Integer>> index = new HashMap<>();

        int docIndex = 0;
        for (String doc : generatedDoc) {
            List<String> perDoc = new ArrayList<>();
            String[] splittedString = doc.split(" ");
            for (int i = 0; i < splittedString.length; i++) {
                List<String> trigramList = trigrams(splittedString[i]);
                for (String tri : trigramList) {
                    Set<Integer> setint = new HashSet<>();
                    if (index.containsKey(tri)) {
                        setint = index.get(tri);
                        setint.add(Integer.valueOf(docIndex));
                        index.put(tri, setint);
                    } else {
                        setint.add(Integer.valueOf(docIndex));
                        index.put(tri, setint);
                    }
                }
                // System.out.println(trigramList);

                perDoc.addAll(trigramList);
            }
            docIndex++;
        }

        System.out.println(search("zebracrossing", index, generatedDoc));
        System.out.println(scan("zebracrossing", generatedDoc));

        for (int run = 0; run < 3; run++) {
            long t1 = System.nanoTime();
            List<Integer> a = scan("zebracrossing", generatedDoc);
            long scanMs = (System.nanoTime() - t1) / 1_000_000;

            long t2 = System.nanoTime();
            List<Integer> b = search("zebracrossing", index, generatedDoc);
            long indexMs = (System.nanoTime() - t2) / 1_000_000;

            System.out.println(
                "rare:   scan " +
                    scanMs +
                    " ms, index " +
                    indexMs +
                    " ms, same result: " +
                    a.equals(b)
            );
        }

        for (int run = 0; run < 3; run++) {
            long t1 = System.nanoTime();
            List<Integer> a = scan("retry", generatedDoc);
            long scanMs = (System.nanoTime() - t1) / 1_000_000;

            long t2 = System.nanoTime();
            List<Integer> b = search("retry", index, generatedDoc);
            long indexMs = (System.nanoTime() - t2) / 1_000_000;

            System.out.println(
                "common: scan " +
                    scanMs +
                    " ms, index " +
                    indexMs +
                    " ms, same result: " +
                    a.size() +
                    " vs " +
                    b.size()
            );
        }

        System.out.println("---- smart search benchmarks ----");

        System.out.println(smartSearch("zebracrossing", index, generatedDoc));
        System.out.println(
            smartSearch("retry", index, generatedDoc).size() ==
                scan("retry", generatedDoc).size()
        );
        System.out.println(
            smartSearch("re", index, generatedDoc).size() ==
                scan("re", generatedDoc).size()
        );

        String[] queries = { "zebracrossing", "retry" };
        for (String q : queries) {
            for (int run = 0; run < 3; run++) {
                long t1 = System.nanoTime();
                scan(q, generatedDoc);
                long scanMs = (System.nanoTime() - t1) / 1_000_000;

                long t2 = System.nanoTime();
                search(q, index, generatedDoc);
                long indexMs = (System.nanoTime() - t2) / 1_000_000;

                long t3 = System.nanoTime();
                smartSearch(q, index, generatedDoc);
                long smartMs = (System.nanoTime() - t3) / 1_000_000;

                System.out.println(
                    q +
                        ": scan " +
                        scanMs +
                        " ms, index " +
                        indexMs +
                        " ms, smart " +
                        smartMs +
                        " ms"
                );
            }
        }
    }

    static List<Integer> search(
        String query,
        Map<String, Set<Integer>> index,
        List<String> docs
    ) {
        List<Integer> results = new ArrayList<>();

        Set<Integer> candidates = null;
        for (String q : trigrams(query)) {
            Set<Integer> hits = index.get(q);
            if (null == hits) {
                return results;
            }
            if (null == candidates) {
                candidates = new HashSet<>(hits);
            } else {
                candidates.retainAll(hits);
            }
        }
        if (candidates == null) {
            return results;
        }
        for (int id : candidates) {
            if (docs.get(id).contains(query)) {
                results.add(id);
            }
        }
        return results;
    }

    static List<Integer> scan(String query, List<String> docs) {
        List<Integer> results = new ArrayList<>();
        for (int id = 0; id < docs.size(); id++) {
            if (docs.get(id).contains(query)) {
                results.add(id);
            }
        }
        return results;
    }

    static List<Integer> smartSearch(
        String query,
        Map<String, Set<Integer>> index,
        List<String> docs
    ) {
        if (null == query) {
            return new ArrayList<>();
        }
        if (query.length() < 3) {
            return scan(query, docs);
        }
        int smallest = Integer.MAX_VALUE;
        for (String qt : trigrams(query)) {
            Set<Integer> hits = index.get(qt);
            if (null == hits) {
                return new ArrayList<>();
            }
            if (hits.size() < smallest) {
                smallest = hits.size();
            }
        }

        if (smallest > docs.size() / 10) {
            return scan(query, docs);
        } else {
            return search(query, index, docs);
        }
    }
}
