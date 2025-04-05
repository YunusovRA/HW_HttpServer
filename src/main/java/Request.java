import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class Request {
    private String method;
    private String rawPath;
    private String path;
    private Map<String, String> queryParams;
    private Map<String, String> headers;
    private String body;

    public Request(String method, String rawPath, Map<String, String> headers, String body) {
        this.method = method;
        this.rawPath = rawPath;
        this.headers = headers;
        this.body = body;
        parsePath();
    }

    private void parsePath() {
        String[] parts = rawPath.split("\\?", 2);
        path = parts[0];

        if (parts.length > 1) {
            String query = parts[1];
            queryParams = new HashMap<>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length > 0) {
                    String key = kv[0];
                    String value = kv.length > 1 ? kv[1] : "";
                    queryParams.put(key, value);
                }
            }
        } else {
            queryParams = new HashMap<>();
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}