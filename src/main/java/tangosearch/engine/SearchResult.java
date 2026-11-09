package tangosearch.engine;

import java.util.List;

public record SearchResult(
    String query,
    int total,
    int shown,
    int usedTokens,
    int budgetTokens,
    List<Hit> hits
) {}
