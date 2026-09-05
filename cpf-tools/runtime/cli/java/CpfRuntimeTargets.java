import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
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
    private static final Pattern GROUP_ENTRY = Pattern.compile(
            "\\{[^{}]*\"group\"\\s*:\\s*\"([^\"]+)\"[^{}]*\\}", Pattern.DOTALL);
    private static final Pattern ENTRY = Pattern.compile(
            "\\{[^{}]*\"target\"\\s*:\\s*\"([^\"]+)\"[^{}]*\\}", Pattern.DOTALL);

    private CpfRuntimeTargets() { }

    /** 하나의 실행 Target. */
    record Target(String name, String owner, String capability, String provision, String description,
                  String artifactId, int port, String portEnv, String healthPath,
                  String architectureRole, List<String> runtimeGroups, List<String> dependsOn,
                  String buildSurface) {
        /** Group 소속 판정. metadata 만 본다. 이름으로 추론하지 않는다. */
        boolean memberOf(String selector, String value) {
            if ("every-resolved-target".equals(selector)) return true;
            if ("runtimeGroups".equals(selector)) return runtimeGroups.contains(value);
            if ("architectureRole".equals(selector)) return architectureRole.equals(value);
            return false;
        }
    }

    /** 사용자에게 노출되는 논리 Runtime Group. catalog 가 정본이다. */
    record Group(String name, String label, String selector, String value) { }

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
                    number(block, "port"), field(block, "portEnv"), field(block, "healthPath"),
                    field(block, "architectureRole"), stringArray(block, "runtimeGroups"),
                    stringArray(block, "dependsOn"), field(block, "buildSurface")));
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
                for (Map.Entry<String, String> module : moduleCapabilities(root).entrySet()) {
                    String name = module.getKey();
                    if (!settingsText.contains("include '" + name + "'")) continue;
                    if (!Files.isDirectory(dir.resolve(name))) continue;
                    // Domain Runtime 의 Port 는 Bootstrap 이 Domain 상태에서 배정한다. catalog 에
                    // 고정 Port 를 두면 Domain 을 추가할 때마다 catalog 를 고쳐야 한다.
                    String dynamic = dynamicSection(root);
                    // 사용자에게 Domain 은 하나의 Runtime 이다. primary module 은 Domain 이름을
                    // 그대로 쓰고, 성격이 다른 나머지 module 만 이름을 붙여 구분한다.
                    boolean primary = name.equals(field(dynamic, "primaryModule"));
                    String targetName = primary ? domain : domain + "-" + name;
                    targets.add(new Target(targetName, dir.getFileName() + "/" + name,
                            module.getValue(), "source",
                            "Generated Domain " + domain + " " + name + " Runtime",
                            "", 0, primary ? systemCode + "_ONLINE_PORT" : "",
                            dynamicHealthPath(root, name),
                            field(dynamic, "architectureRole"),
                            moduleGroups(dynamic, name),
                            stringArray(dynamic, "dependsOn"), field(dynamic, "buildSurface")));
                }
            }
        }
        return targets;
    }

    /**
     * Domain module 과 그 Runtime Capability. catalog 의 dynamicRuntimes.capabilityByModule 이 정본이다.
     *
     * <p>여기에 module 목록을 복제하면 catalog 와 두 벌이 되고, 한쪽만 고칠 때 Target 이 조용히
     * 사라진다.</p>
     */
    private static Map<String, String> moduleCapabilities(Path root) throws IOException {
        String dynamic = dynamicSection(root);
        int start = dynamic.indexOf("\"capabilityByModule\"");
        if (start < 0) throw new IOException("dynamicRuntimes.capabilityByModule is missing");
        int end = dynamic.indexOf('}', start);
        if (end < 0) throw new IOException("dynamicRuntimes.capabilityByModule is malformed");
        Map<String, String> capabilities = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"")
                .matcher(dynamic.substring(dynamic.indexOf('{', start) + 1, end));
        while (matcher.find()) capabilities.put(matcher.group(1), matcher.group(2));
        if (capabilities.isEmpty()) throw new IOException("dynamicRuntimes.capabilityByModule declares no module");
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

    /**
     * 사용자에게 노출되는 Group 목록.
     *
     * <p>catalog 의 runtimeGroups.groups 만 읽는다. CLI/launcher/문서가 Group 이름을 복제하면
     * Runtime 이 늘어날 때마다 세 곳을 고쳐야 하고 결국 서로 어긋난다.</p>
     */
    static List<Group> groups(Path root) throws IOException {
        String json = Files.readString(catalogPath(root), StandardCharsets.UTF_8);
        // 개별 Runtime 도 runtimeGroups 키를 가지므로 authority 는 contract 표식으로 찾는다.
        int start = json.indexOf("CPF_RUNTIME_GROUP_AUTHORITY");
        if (start < 0) throw new IOException("Runtime Target Catalog has no runtimeGroups authority");
        int listStart = json.indexOf("\"groups\"", start);
        if (listStart < 0) throw new IOException("Runtime Group authority has no groups section");
        List<Group> groups = new ArrayList<>();
        Matcher matcher = GROUP_ENTRY.matcher(json.substring(listStart));
        while (matcher.find()) {
            String block = matcher.group(0);
            groups.add(new Group(matcher.group(1), field(block, "label"),
                    field(block, "selector"), field(block, "value")));
        }
        if (groups.isEmpty()) throw new IOException("Runtime Group authority declares no group");
        return groups;
    }

    /** architectureRole 별 기동 우선순위. 동순위 정렬의 기준이며 catalog 가 정본이다. */
    private static List<String> architectureRoleOrder(Path root) throws IOException {
        String json = Files.readString(catalogPath(root), StandardCharsets.UTF_8);
        int start = json.indexOf("\"architectureRoleOrder\"");
        if (start < 0) return List.of();
        int end = json.indexOf(']', start);
        if (end < 0) return List.of();
        List<String> order = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]+)\"").matcher(json.substring(json.indexOf('[', start) + 1, end));
        while (matcher.find()) order.add(matcher.group(1));
        return order;
    }

    /**
     * 선택자(Group 이름 또는 Runtime target 이름)를 실행 순서가 정해진 Target 목록으로 바꾼다.
     *
     * <p>Group 은 대상 집합만 정하고 dependsOn 은 그 집합 안의 순서만 정한다. Group 이 의존 대상을
     * 자동으로 끌어오면 {@code cpf start domains} 가 Platform 까지 띄우게 되어 사용자가 요청하지 않은
     * Runtime 이 올라간다.</p>
     *
     * @return 기동 순서대로 정렬된 Target. 알 수 없는 선택자면 빈 Optional.
     */
    static Optional<List<Target>> select(Path root, String selector) throws IOException {
        String requested = selector == null ? "" : selector.trim();
        if (requested.isEmpty()) return Optional.empty();
        List<Target> all = resolveAll(root);
        for (Group group : groups(root)) {
            if (!group.name().equalsIgnoreCase(requested)) continue;
            List<Target> selected = new ArrayList<>();
            for (Target target : all) {
                if (target.memberOf(group.selector(), group.value())) selected.add(target);
            }
            return Optional.of(startOrder(selected, architectureRoleOrder(root)));
        }
        for (Target target : all) {
            if (target.name().equals(requested)) return Optional.of(List.of(target));
        }
        return Optional.empty();
    }

    /** 요청한 이름이 Group 이면 true. 결과 집계 방식이 달라지므로 CLI 가 구분해야 한다. */
    static boolean isGroup(Path root, String selector) throws IOException {
        for (Group group : groups(root)) {
            if (group.name().equalsIgnoreCase(selector == null ? "" : selector.trim())) return true;
        }
        return false;
    }

    /**
     * dependsOn 위상정렬. 순환이면 fail-closed 한다.
     *
     * <p>선택 집합 밖의 의존은 순서 계산에서 무시한다. 그 Runtime 은 이번 요청 대상이 아니다.</p>
     */
    static List<Target> startOrder(List<Target> selected, List<String> roleOrder) {
        Map<String, Target> byName = new LinkedHashMap<>();
        for (Target target : selected) byName.put(target.name(), target);
        List<Target> pending = new ArrayList<>(selected);
        pending.sort((a, b) -> {
            int left = roleOrder.indexOf(a.architectureRole());
            int right = roleOrder.indexOf(b.architectureRole());
            if (left < 0) left = roleOrder.size();
            if (right < 0) right = roleOrder.size();
            return left != right ? Integer.compare(left, right) : a.name().compareTo(b.name());
        });
        List<Target> ordered = new ArrayList<>();
        Set<String> placed = new LinkedHashSet<>();
        while (!pending.isEmpty()) {
            boolean progressed = false;
            for (Iterator<Target> it = pending.iterator(); it.hasNext(); ) {
                Target candidate = it.next();
                boolean ready = true;
                for (String dependency : candidate.dependsOn()) {
                    if (byName.containsKey(dependency) && !placed.contains(dependency)) { ready = false; break; }
                }
                if (!ready) continue;
                ordered.add(candidate);
                placed.add(candidate.name());
                it.remove();
                progressed = true;
                // 준비된 첫 후보 하나만 배치하고 처음부터 다시 훑는다. 한 번의 통과에서 여러 개를
                // 잡으면 의존이 늦게 풀린 Target 이 목록 끝으로 밀려 순서 의미가 깨진다.
                break;
            }
            if (!progressed) {
                List<String> cycle = new ArrayList<>();
                for (Target target : pending) cycle.add(target.name());
                throw new IllegalStateException("runtime dependency cycle: " + cycle);
            }
        }
        return ordered;
    }

    /**
     * module 별 Group 소속.
     *
     * <p>같은 Domain 이라도 online 과 batch 는 사용자에게 다른 성격이다. {@code cpf start domains}
     * 가 one-shot batch 까지 실행하면 사용자가 요청하지 않은 작업이 돈다.</p>
     */
    private static List<String> moduleGroups(String dynamic, String module) {
        int start = dynamic.indexOf("\"runtimeGroupsByModule\"");
        if (start < 0) return List.of();
        int end = dynamic.indexOf('}', start);
        if (end < 0) return List.of();
        return stringArray(dynamic.substring(start, end), module);
    }

    /** JSON 배열 필드를 읽는다. 배열이 없으면 빈 목록이다. */
    private static List<String> stringArray(String block, String key) {
        Matcher array = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[([^\\]]*)\\]").matcher(block);
        if (!array.find()) return List.of();
        List<String> values = new ArrayList<>();
        Matcher element = Pattern.compile("\"([^\"]+)\"").matcher(array.group(1));
        while (element.find()) values.add(element.group(1));
        return List.copyOf(values);
    }

    /** dynamicRuntimes 블록 원문. Generated Domain 의 group metadata 정본이다. */
    private static String dynamicSection(Path root) throws IOException {
        String json = Files.readString(catalogPath(root), StandardCharsets.UTF_8);
        int start = json.indexOf("\"dynamicRuntimes\"");
        if (start < 0) return "";
        int end = json.indexOf("\"portPolicy\"", start);
        return end < 0 ? json.substring(start) : json.substring(start, end);
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
