package tangosearch.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import tangosearch.engine.SearchEngine;
import tangosearch.engine.SearchResult;
import tangosearch.output.TangoFormatter;

public class McpServer {

    private static final Set<String> SUPPORTED = Set.of(
        "2025-11-25",
        "2025-06-18",
        "2025-03-26",
        "2024-11-05"
    );

    private static final String LATEST = "2025-11-25";

    private final ObjectMapper mapper = new ObjectMapper();

    public void run(InputStream in, OutputStream out) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(in, StandardCharsets.UTF_8)
        );

        String line;

        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }

            try {
                JsonNode request = mapper.readTree(line);

                ObjectNode reply = handle(request);

                if (reply != null) {
                    String json = mapper.writeValueAsString(reply) + "\n";

                    out.write(json.getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
            } catch (JsonProcessingException e) {
                System.err.println("Invalid JSON: " + e.getMessage());
            }
        }
    }

    ObjectNode handle(JsonNode msg) {
        String method = msg.path("method").asText("");

        if (method.isEmpty() || !msg.has("id")) {
            return null;
        }

        return switch (method) {
            case "initialize" -> initializeReply(msg);
            case "tools/list" -> listTools(msg);
            case "tools/call" -> handleToolCall(msg);
            default -> error(
                msg.get("id"),
                -32601,
                "Method not found: " + method
            );
        };
    }

    ObjectNode listTools(JsonNode msg) {
        ObjectNode reply = mapper.createObjectNode();
        reply.put("jsonrpc", "2.0");
        reply.set("id", msg.get("id"));

        ObjectNode result = reply.putObject("result");
        ArrayNode tools = result.putArray("tools");

        ObjectNode searchTool = tools.addObject();

        searchTool.put("name", "search");
        searchTool.put(
            "description",
            "Search Java source files using trigram-based code search"
        );

        ObjectNode inputSchema = searchTool.putObject("inputSchema");
        inputSchema.put("type", "object");

        ObjectNode properties = inputSchema.putObject("properties");

        properties.putObject("query").put("type", "string");

        properties.putObject("path").put("type", "string");

        ArrayNode required = inputSchema.putArray("required");
        required.add("query");
        required.add("path");

        return reply;
    }

    private ObjectNode handleToolCall(JsonNode msg) {
        String toolName = msg.path("params").path("name").asText("");

        JsonNode args = msg.path("params").path("arguments");

        if (!"search".equals(toolName)) {
            return error(msg.get("id"), -32602, "Unknown tool: " + toolName);
        }

        ObjectNode reply = mapper.createObjectNode();
        reply.put("jsonrpc", "2.0");
        reply.set("id", msg.get("id"));

        ObjectNode result = reply.putObject("result");
        ArrayNode content = result.putArray("content");

        try {
            String query = args.path("query").asText("").trim();
            String pathValue = args.path("path").asText("").trim();

            if (query.isEmpty()) {
                throw new IllegalArgumentException(
                    "Missing required argument: query"
                );
            }

            if (pathValue.isEmpty()) {
                throw new IllegalArgumentException(
                    "Missing required argument: path"
                );
            }

            Path root = Paths.get(pathValue)
                .toAbsolutePath()
                .normalize()
                .toRealPath();

            if (!Files.isDirectory(root)) {
                throw new IllegalArgumentException(
                    "Path is not a directory: " + root
                );
            }

            SearchEngine engine = new SearchEngine(root);

            SearchResult searchResult = engine.search(query, 500);

            String json = TangoFormatter.toJson(searchResult);

            content.addObject().put("type", "text").put("text", json);

            result.put("isError", false);
        } catch (Exception e) {
            String message =
                e.getMessage() != null ? e.getMessage() : "Search failed";

            content.addObject().put("type", "text").put("text", message);

            result.put("isError", true);
        }

        return reply;
    }

    ObjectNode initializeReply(JsonNode msg) {
        String asked = msg.path("params").path("protocolVersion").asText("");

        String version = SUPPORTED.contains(asked) ? asked : LATEST;

        ObjectNode reply = mapper.createObjectNode();
        reply.put("jsonrpc", "2.0");
        reply.set("id", msg.get("id"));

        ObjectNode result = reply.putObject("result");

        result.put("protocolVersion", version);

        result.putObject("capabilities").putObject("tools");

        ObjectNode serverInfo = result.putObject("serverInfo");

        serverInfo.put("name", "tangosearch");
        serverInfo.put("version", "0.1.0");

        return reply;
    }

    ObjectNode error(JsonNode id, int code, String message) {
        ObjectNode reply = mapper.createObjectNode();
        reply.put("jsonrpc", "2.0");

        reply.set("id", id);

        ObjectNode err = reply.putObject("error");

        err.put("code", code);
        err.put("message", message);

        return reply;
    }
}
