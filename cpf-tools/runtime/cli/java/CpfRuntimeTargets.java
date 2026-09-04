import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CPF Runtime Target 해석기.
 *
 * <p>Gradle Task, Public CLI, Windows/Linux launcher 가 각자 Target 목록을 들고 있으면 의미가
 * 갈라진다. 세 소비자 모두 {@code cpf-tools/runtime/cpf-runtime-target-catalog.json} 하나만 읽는다.
 * Generated Domain 처럼 존재 여부가 변하는 Component 는 catalog 에 이름을 두지 않고 catalog 가
 * 정의한 discovery 규칙으로 찾아내므로, Domain 을 추가하거나 지워도 launcher 수정이 필요 없다.</p>
 */
final class CpfRuntimeTargets {
    private static final String CATALOG_RELATIVE = "cpf-tools/runtime/cpf-runtime-target-catalog.json";
    /**
     * 공개 배포본의 Runtime Target Catalog 위치.
     *
     * <p>Open Git checkout 은 {@code cpf-tools/} 를 포함하지 않는다. 정본 경로만 읽으면 공개
     * 사용자는 어떤 Target 도 해석하지 못하고 {@code cpf runtime targets} 가 "catalog missing"
     * 으로 죽는다. Release 는 이 위치로 공개 Catalog 를 투영한다.</p>
     */
    private static final String PUBLIC_CATALOG_RELATIVE = "config/cpf-runtime-target-catalog.json";
    private static final Pattern ENTRY = Pattern.compile(
            "\\{[^{}]*\"target\"\\s*:\\s*\"([^\"]+)\"[^{}]*\\}", Pattern.DOTALL);

    private CpfRuntimeTargets() { }

    /** 하나의 실행 Target. */
    record Target(String name, String owner, String capability, String provision, String description,
                  String artifactId, int port, String portEnv, String healthPath) {
        String describe() {
            return String.format(Locale.ROOT, "  %-20s %s", name, description == null ? "" : description);
        }
    }

    static Path catalogPath(Path root) {
        Path canonical = root.resolve(CATALOG_RELATIVE);
        if (Files.isRegularFile(canonical)) return canonical;
        Path published = root.resolve(PUBLIC_CATALOG_RELATIVE);
        if (Files.isRegularFile(published)) return published;
        return canonical;
    }

    /** catalog 의 정적 Target 과 discovery 로 찾은 동적 Domain Target 을 합쳐 돌려준다. */
    static List<Target> resolveAll(Path root) throws IOException {
        List<Target> targets = new ArrayList<>(staticTargets(root));
        targets.addAll(discoveredDomainTargets(root));
        targets.sort((a, b) -> a.name().compareTo(b.name()));
        return targets;
    }

    /**
     * 공개 배포본에서 binary Runtime 의 실행물이 실제로 있는지 확인한다.
     *
     * <p>좌표는 catalog 의 {@code artifactId} 를 그대로 쓴다. 이름을 추론하지 않는다.</p>
     */
    private static boolean hasPublishedRuntime(Path root, String artifactId) {
        if (artifactId == null || artifactId.isBlank()) return false;
        Path base = root.resolve("binary-repository/com/cpf/runtime").resolve(artifactId);
        if (!Files.isDirectory(base)) return false;
        try (var versions = Files.list(base)) {
            return versions.filter(Files::isDirectory).anyMatch(version -> {
                try (var files = Files.list(version)) {
                    return files.anyMatch(f -> f.getFileName().toString().endsWith(".jar"));
                } catch (IOException e) {
                    return false;
                }
            });
        } catch (IOException e) {
            return false;
        }
    }

    static List<Target> staticTargets(Path root) throws IOException {
        Path catalog = catalogPath(root);
        if (!Files.isRegularFile(catalog)) {
            throw new IOException("Runtime Target Catalog is missing: " + catalog);
        }
        String json = Files.readString(catalog, StandardCharsets.UTF_8);
        int start = json.indexOf("\"runtimes\"");
        if (start < 0) throw new IOException("Runtime Target Catalog has no runtimes section");
        List<Target> targets = new ArrayList<>();
        Matcher matcher = ENTRY.matcher(json.substring(start));
        while (matcher.find()) {
            String block = matcher.group(0);
            String name = matcher.group(1);
            String provision = field(block, "provision");
            String owner = field(block, "owner");
            // 존재 판정 기준은 provision 이 정한다.
            //  - source: 그 Component 의 Source 가 checkout 에 있어야 실행할 수 있다.
            //  - binary: 실행물은 Binary Repository 가 공급한다. Source 디렉터리 존재로 판정하면
            //    ADM 처럼 Binary 로만 배포되는 Runtime 이 공개 배포본에서 영구히 보이지 않는다
            //    (jar 는 있는데 실행 진입점이 끊긴다). 반대로 아무 조건 없이 노출하면 실행물이
            //    없는 Target 을 안내하게 되므로, 실제 실행물 존재로 판정한다.
            boolean available = Files.isDirectory(root.resolve(owner));
            if (!available && "binary".equals(provision)) {
                available = hasPublishedRuntime(root, field(block, "artifactId"));
            }
            if (!available) {
                continue;
            }
            targets.add(new Target(name, owner, field(block, "capability"),
                    provision, field(block, "description"), field(block, "artifactId"),
                    number(block, "port"), field(block, "portEnv"), field(block, "healthPath")));
        }
        return targets;
    }

    /**
     * Generated Domain Target 을 discovery 로 찾는다.
     *
     * <p>catalog 의 domain discovery 규칙(gradle.properties 의 contract key, settings.gradle 의
     * module include)을 그대로 적용한다. Domain 이름은 어디에도 하드코딩하지 않는다.</p>
     */
    static List<Target> discoveredDomainTargets(Path root) throws IOException {
        List<Target> targets = new ArrayList<>();
        try (var entries = Files.list(root)) {
            for (Path dir : entries.filter(Files::isDirectory).toList()) {
                Path properties = dir.resolve("gradle.properties");
                if (!Files.isRegularFile(properties)) continue;
                Properties props = new Properties();
                try (var in = Files.newInputStream(properties)) {
                    props.load(in);
                }
                if (!"1".equals(props.getProperty("cpf.domain.contractVersion"))) continue;
                // prebuilt Domain(예: Backoffice)은 catalog 의 static entry 가 이미 소유한다.
                // 같은 cpf.domain.* 계약을 공유한다는 이유로 여기서 다시 추가하면 Target 이 중복된다.
                if ("prebuilt".equals(trim(props.getProperty("cpf.domain.generationMode")))) continue;
                String domain = trim(props.getProperty("cpf.domain.name"));
                String systemCode = trim(props.getProperty("cpf.domain.systemCode"));
                if (domain.isEmpty()) continue;
                Path settings = dir.resolve("settings.gradle");
                String settingsText = Files.isRegularFile(settings)
                        ? Files.readString(settings, StandardCharsets.UTF_8) : "";
                for (Map.Entry<String, String> module : moduleCapabilities().entrySet()) {
                    String name = module.getKey();
                    if (!settingsText.contains("include '" + name + "'")) continue;
                    if (!Files.isDirectory(dir.resolve(name))) continue;
                    // Domain Runtime 의 Port 는 Bootstrap 이 Domain 상태에서 배정한다. catalog 에
                    // 고정 Port 를 두면 Domain 을 추가할 때마다 catalog 를 고쳐야 한다.
                    targets.add(new Target(domain + "-" + name, dir.getFileName() + "/" + name,
                            module.getValue(), "source",
                            "Generated Domain " + domain + " " + name + " Runtime",
                            "", 0, "online".equals(name) ? systemCode + "_ONLINE_PORT" : "",
                            dynamicHealthPath(root, name)));
                }
            }
        }
        return targets;
    }

    private static Map<String, String> moduleCapabilities() {
        Map<String, String> capabilities = new LinkedHashMap<>();
        capabilities.put("online", "http-server");
        capabilities.put("batch", "one-shot");
        return capabilities;
    }

    /** catalog 의 dynamicRuntimes 규칙에서 module 별 health 경로를 읽는다. */
    private static String dynamicHealthPath(Path root, String module) throws IOException {
        Path catalog = catalogPath(root);
        if (!Files.isRegularFile(catalog)) return "";
        String json = Files.readString(catalog, StandardCharsets.UTF_8);
        int start = json.indexOf("\"healthPathByModule\"");
        if (start < 0) return "";
        int end = json.indexOf('}', start);
        if (end < 0) return "";
        return field(json.substring(start, end), module);
    }

    private static int number(String block, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(block);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private static String field(String block, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(block);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
