package tangosearch.output;

import java.util.List;
import tangosearch.engine.Hit;
import tangosearch.engine.SearchResult;

public class TangoFormatter {

    public static String esc(String s) {
        return s
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\t", "\\t");
    }

    public static String toJson(SearchResult r) {
        StringBuilder hitsJson = new StringBuilder();
        List<Hit> hits = r.hits();
        for (int i = 0; i < hits.size(); i++) {
            Hit h = hits.get(i);
            if (i > 0) {
                hitsJson.append(",");
            }
            hitsJson
                .append("{\"path\":\"")
                .append(esc(h.path()))
                .append("\",\"line\":")
                .append(h.line())
                .append(",\"text\":\"")
                .append(esc(h.text()))
                .append("\"}");
        }

        return (
            "{\"query\":\"" +
            esc(r.query()) +
            "\"," +
            "\"total\":" +
            r.total() +
            "," +
            "\"shown\":" +
            r.shown() +
            "," +
            "\"budget_tokens\":" +
            r.budgetTokens() +
            "," +
            "\"used_tokens\":" +
            r.usedTokens() +
            "," +
            "\"hits\":[" +
            hitsJson +
            "]}"
        );
    }

    public static String toJson(String query, List<Hit> found, int maxTokens) {
        StringBuilder hitsJson = new StringBuilder();
        int used = 0;
        int shown = 0;

        for (Hit h : found) {
            int cost = h.format().length() / 4 + 1;
            if (used + cost > maxTokens) {
                break;
            }
            if (shown > 0) {
                hitsJson.append(",");
            }
            hitsJson
                .append("{\"path\":\"")
                .append(esc(h.path()))
                .append("\",\"line\":")
                .append(h.line())
                .append(",\"text\":\"")
                .append(esc(h.text()))
                .append("\"}");
            used += cost;
            shown++;
        }

        return (
            "{\"query\":\"" +
            esc(query) +
            "\"," +
            "\"total\":" +
            found.size() +
            "," +
            "\"shown\":" +
            shown +
            "," +
            "\"budget_tokens\":" +
            maxTokens +
            "," +
            "\"used_tokens\":" +
            used +
            "," +
            "\"hits\":[" +
            hitsJson +
            "]}"
        );
    }
}
