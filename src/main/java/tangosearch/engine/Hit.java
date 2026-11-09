package tangosearch.engine;

public record Hit(String path, int line, String text) {
    public String format() {
        return path + ":" + line + ": " + text;
    }
}
