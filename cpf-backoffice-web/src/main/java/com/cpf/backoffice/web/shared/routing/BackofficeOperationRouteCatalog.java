package com.cpf.backoffice.web.shared.routing;

import com.cpf.backoffice.web.shared.error.UnsupportedBackofficeOperationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public final class BackofficeOperationRouteCatalog {
    private final List<Route> routes;

    public BackofficeOperationRouteCatalog() {
        try (var stream = BackofficeOperationRouteCatalog.class.getResourceAsStream("/backoffice-routes.tsv")) {
            if (stream == null) throw new IllegalStateException("backoffice-routes.tsv missing");
            List<Route> loaded = new ArrayList<>();
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || line.startsWith("#")) continue;
                    String[] parts = line.split("\\t", -1);
                    if (parts.length != 3) throw new IllegalStateException("invalid Backoffice route catalog row: " + line);
                    loaded.add(Route.of(parts[0], parts[1], parts[2]));
                }
            }
            loaded.sort(Comparator.comparingInt(Route::staticSegments).reversed().thenComparingInt(r -> -r.template().length()));
            routes = List.copyOf(loaded);
            if (routes.isEmpty()) throw new IllegalStateException("Backoffice route catalog is empty");
        } catch (java.io.IOException e) {
            throw new IllegalStateException("failed to load Backoffice route catalog", e);
        }
    }

    public Route require(String method, String path) {
        return routes.stream().filter(r -> r.method().equalsIgnoreCase(method) && r.pattern().matcher(path).matches())
                .findFirst().orElseThrow(() -> new UnsupportedBackofficeOperationException(method + " " + path));
    }

    public int size() { return routes.size(); }

    public record Route(String method, String template, String operationId, Pattern pattern, int staticSegments) {
        static Route of(String method, String template, String operationId) {
            if (!method.matches("GET|POST|PUT|PATCH|DELETE")) throw new IllegalArgumentException("invalid HTTP method");
            if (!template.startsWith("/api/v1/backoffice/")) throw new IllegalArgumentException("route outside Backoffice public boundary");
            if (operationId.isBlank()) throw new IllegalArgumentException("operationId required");
            String[] segments = template.split("/");
            int staticCount = 0;
            StringBuilder regex = new StringBuilder("^");
            for (String segment : segments) {
                if (segment.isEmpty()) continue;
                regex.append('/');
                if (segment.startsWith("{") && segment.endsWith("}")) regex.append("[^/]+");
                else { regex.append(Pattern.quote(segment)); staticCount++; }
            }
            regex.append("$");
            return new Route(method, template, operationId, Pattern.compile(regex.toString()), staticCount);
        }
    }
}
