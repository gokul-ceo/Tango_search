package tangosearch.engine;

import java.io.*;
import java.util.*;

public class TrigramBuilder {

    public static List<String> resolve(String text) {
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
}
