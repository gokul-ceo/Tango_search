package tangosearch.engine;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class Ranker {

    public static int rank(Hit h, String query) {
        String t = h.text();
        int s = 5;
        if (t.startsWith("import ")) {
            s = 0;
        }

        if (t.startsWith("//") || t.startsWith("*") || t.startsWith("/*")) {
            s = 1;
        }

        boolean declaresType =
            t.contains("class ") ||
            t.contains("interface ") ||
            t.contains("enum ") ||
            t.contains("record ");

        if (declaresType && t.contains(query)) {
            s += 10;
        }
        Pattern declaresExact = Pattern.compile(
            "\\b(class|interface|enum|record)\\s+" +
                Pattern.quote(query) +
                "\\b"
        );
        if (declaresExact.matcher(t).find()) {
            s += 20;
        }
        return s;
    }
}
