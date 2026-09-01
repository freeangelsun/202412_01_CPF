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
    private static final Pattern ENTRY = Pattern.compile(
            "\\{[^{}]*\"target\"\\s*:\\s*\"([^\"]+)\"[^{}]*\\}", Pattern.DOTALL);

    private CpfRuntimeTargets() { }

    /** 하나의 실행 Target. */
    record Target(String name, String owner, String capability, String provision, String description) {
        String describe() {
            return String.format(Locale.ROOT, "  %-20s %s", name, description == null ? "" : description);
        }
    }

    static Path catalogPath(Path root) {
        return root.resolve(CATALOG_RELATIVE);
    }

    /** catalog 의 정적 Target 과 discovery 로 찾은 동적 Domain Target 을 합쳐 돌려준다. */
    static List<Target> resolveAll(Path root) throws IOException {
        List<Target> targets = new ArrayList<>(staticTargets(root));
        targets.addAll(discoveredDomainTargets(root));
        targets.sort((a, b) -> a.name().compareTo(b.name()));
        return targets;
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
            if (!Files.isDirectory(root.resolve(field(block, "owner")))) {
                // 선택 Component(Backoffice 등)가 없으면 Target 도 노출하지 않는다.
                continue;
            }
            targets.add(new Target(name, field(block, "owner"), field(block, "capability"),
                    field(block, "provision"), field(block, "description")));
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
                if (domain.isEmpty()) continue;
                Path settings = dir.resolve("settings.gradle");
                String settingsText = Files.isRegularFile(settings)
                        ? Files.readString(settings, StandardCharsets.UTF_8) : "";
                for (Map.Entry<String, String> module : moduleCapabilities().entrySet()) {
                    String name = module.getKey();
                    if (!settingsText.contains("include '" + name + "'")) continue;
                    if (!Files.isDirectory(dir.resolve(name))) continue;
                    targets.add(new Target(domain + "-" + name, dir.getFileName() + "/" + name,
                            module.getValue(), "source",
                            "Generated Domain " + domain + " " + name + " Runtime"));
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

    private static String field(String block, String key) {
        Matcher matcher = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(block);
        return matcher.find() ? matcher.group(1) : "";
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
