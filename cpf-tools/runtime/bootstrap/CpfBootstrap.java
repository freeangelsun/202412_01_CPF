import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.*;

/** CPF Public Developer Workspace의 OS-neutral Local Bootstrap Engine. Java 25 source launcher로 실행합니다. */
public final class CpfBootstrap {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final Path root;
    private final Path logDir;
    private final PrintWriter log;
    private final int timeoutSeconds;
    private final Map<String,String> baseEnv = new HashMap<>(System.getenv());
    private final Properties workspace = new Properties();
    private final List<Domain> domains = new ArrayList<>();
    private String requestedDbVendor = "";
    private String localDbPassword;
    private boolean runtimeEnvironmentReady = false;
    // Platform DB 는 moduleCode 기준이며 SystemCode namespace 와 섞지 않는다.
    private static final String PLATFORM_MODULE_CODE = "CPF";
    private static final String PLATFORM_DATABASE = "cpfDB";
    private static final String PLATFORM_ASSET_ROOT = "deploy/local/db/platform";
    private static final String VENDOR_PACK_ROOT = "deploy/local/db/vendor";
    private static final String DOMAIN_ASSET_ROOT = "deploy/local/db/domain";
    private DbBinding platformDb;

    private record DbBinding(String vendor, String host, int port, String databaseName, String serviceName, String schemaName,
                             String migrationUser, String runtimeUser, String migrationSecretEnv, String runtimeSecretEnv, Path profile) {}
    private record Domain(String name, String systemCode, Path contract, Path project, Map<String,String> features, int localOnlinePort, DbBinding db) {}
    private record ExecResult(int exit, String output) {}

    private CpfBootstrap(Path root, int timeoutSeconds) throws Exception {
        this.root = root.toAbsolutePath().normalize();
        this.timeoutSeconds = timeoutSeconds;
        this.logDir = this.root.resolve("build/cpf-bootstrap/CPF_BOOTSTRAP_" + TS.format(LocalDateTime.now()));
        Files.createDirectories(logDir);
        this.log = new PrintWriter(Files.newBufferedWriter(logDir.resolve("bootstrap.log"), StandardCharsets.UTF_8));
        Path properties = root.resolve("config/cpf-workspace.properties");
        if (Files.isRegularFile(properties)) try (Reader r = Files.newBufferedReader(properties, StandardCharsets.UTF_8)) { workspace.load(r); }
    }

    public static void main(String[] args) {
        int code = 1;
        CpfBootstrap app = null;
        try {
            Path root = locateRoot();
            Map<String,String> options = parseArgs(args);
            int timeout = Integer.parseInt(options.getOrDefault("timeout", readDefaultTimeout(root)));
            app = new CpfBootstrap(root, timeout);
            code = app.execute(options);
        } catch (Throwable e) {
            System.err.println("[CPF][BOOTSTRAP] FAIL: " + sanitize(e.getMessage()));
            if (app != null) app.logException(e);
        } finally {
            if (app != null) app.close();
        }
        System.exit(code);
    }

    private int execute(Map<String,String> options) throws Exception {
        String command = options.getOrDefault("command", "bootstrap");
        step("00", "Workspace", "root=" + root);
        if (command.equals("stop") || command.equals("reset")) prepareLocalSecret();
        if (command.equals("stop")) return stop(options);
        if (command.equals("reset")) return reset(options);
        if (command.equals("runtime")) return runtimeLifecycle(options);
        if (!command.equals("bootstrap")) throw new IllegalArgumentException("unsupported command=" + command);

        checkPrerequisites();
        resolveBinaryRepository();
        String requestedDb = options.getOrDefault("db", "").trim();
        if (!requestedDb.isBlank()) requestedDbVendor = normalizeDb(requestedDb);
        prepareLocalSecret();
        discoverDomains();
        prepareDatabase();
        prepareMiddleware();
        applyPlatformDatabase();
        applyDomainDatabases();
        writeRuntimeEnvironment();
        runWorkspaceVerification(options.containsKey("full"));
        if (options.containsKey("run")) startRuntimes();
        ready("CPF LOCAL DEVELOPMENT READY");
        return 0;
    }

    private void checkPrerequisites() throws Exception {
        int feature = Runtime.version().feature();
        if (feature != 25) throw new IllegalStateException("Java 25 required, actual=" + Runtime.version());
        pass("01", "Java 25", Runtime.version().toString());
        requireCommand("git", List.of("git", "--version"), false);
        requireCommand("docker", List.of("docker", "version", "--format", "{{.Server.Version}}"), false);
        ExecResult compose = run(List.of("docker", "compose", "version"), Map.of(), 30, null, false, true);
        if (compose.exit != 0) throw new IllegalStateException("Docker Compose plugin is required");
        pass("02", "Git/Docker", "docker-compose=PASS");
        if (Files.isRegularFile(root.resolve("cpf-backoffice-web/frontend/package.json"))) {
            requireCommand("node", List.of("node", "--version"), false);
            pass("03", "Node", "required by cpf-backoffice-web frontend");
        } else {
            skip("03", "Node", "frontend reference not selected");
        }
    }

    private void resolveBinaryRepository() throws Exception {
        // Public Artifact version은 Release가 currentize한 workspace 정본이다. 상위 Shell의
        // CPF_VERSION이 이를 조용히 바꾸면 Fresh Consumer가 존재하지 않는 SNAPSHOT 좌표를
        // 조회해, 선언상 정상인 Test/Runtime classpath까지 깨질 수 있다. 외부 값은 선택
        // override가 아니라 정본과의 일치 검증에만 허용한다.
        String version = workspace.getProperty("cpf.version", "").trim();
        String suppliedVersion = System.getenv("CPF_VERSION");
        if (suppliedVersion != null && !suppliedVersion.isBlank() && !suppliedVersion.trim().equals(version)) {
            throw new IllegalStateException("CPF_VERSION does not match canonical config/cpf-workspace.properties cpf.version");
        }
        String repo = firstNonBlank(System.getenv("CPF_MAVEN_REPOSITORY_URL"), System.getenv("CPF_ARTIFACT_REPOSITORY_URL"), workspace.getProperty("cpf.maven.repository.url", ""));
        if (repo.isBlank()) {
            // 공개 배포본은 Binary Repository 를 checkout 안에 함께 싣는다. 그래서 사용자는
            // 별도 Repository 주소를 설정하지 않고 clone -> bootstrap 만으로 시작할 수 있어야 한다
            // (공개 README 계약). 번들이 있는데도 주소를 요구하면 처음 사용하는 고객이 첫 명령에서
            // 막힌다. 사내 Repository 를 쓰려면 환경변수로 덮어쓴다.
            Path bundled = root.resolve("binary-repository");
            if (Files.isDirectory(bundled)) repo = bundled.toUri().toString();
        }
        if (repo.isBlank()) throw new IllegalStateException(
                "CPF_MAVEN_REPOSITORY_URL is required when the workspace has no bundled binary-repository");
        if (version.isBlank()) throw new IllegalStateException("config/cpf-workspace.properties is missing cpf.version");
        baseEnv.put("CPF_VERSION", version);
        baseEnv.put("CPF_MAVEN_REPOSITORY_URL", repo);
        URI uri = URI.create(repo.endsWith("/") ? repo : repo + "/");
        String bomRel = "com/cpf/cpf-platform-bom/" + version + "/cpf-platform-bom-" + version + ".pom";
        requireRepositoryArtifact(uri, bomRel, "com.cpf:cpf-platform-bom:" + version);
        String classifier = publicBinaryClassifier();
        String generatorRel = "com/cpf/tooling/cpf-generator-cli/" + version + "/cpf-generator-cli-" + version + "-" + classifier + ".zip";
        requireRepositoryArtifact(uri, generatorRel, "com.cpf.tooling:cpf-generator-cli:" + version + ":" + classifier);
        requireRepositoryArtifact(uri, generatorRel + ".sha256", "cpf-generator-cli checksum");
        pass("04", "Binary Repository", "version=" + version + " generator=" + classifier + " repository=" + safeUri(repo));
    }

    private void requireRepositoryArtifact(URI repository, String relative, String label) throws Exception {
        if ("file".equalsIgnoreCase(repository.getScheme())) {
            Path file = Paths.get(repository).resolve(relative);
            if (!Files.isRegularFile(file)) throw new IllegalStateException("CPF Binary Repository artifact missing: " + label + " path=" + file);
            return;
        }
        if (repository.getScheme() == null || !(repository.getScheme().equals("http") || repository.getScheme().equals("https"))) {
            throw new IllegalStateException("unsupported CPF_MAVEN_REPOSITORY_URL scheme: " + repository.getScheme());
        }
        URI target = repository.resolve(relative);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(Math.min(30, timeoutSeconds))).build();
        HttpRequest.Builder request = HttpRequest.newBuilder(target).timeout(Duration.ofSeconds(Math.min(60, timeoutSeconds))).GET();
        String user = firstNonBlank(System.getenv("CPF_MAVEN_REPOSITORY_USER"), System.getenv("CPF_ARTIFACT_REPOSITORY_USER"));
        if (!user.isBlank()) {
            String password = firstNonBlank(System.getenv("CPF_MAVEN_REPOSITORY_PASSWORD"), System.getenv("CPF_ARTIFACT_REPOSITORY_PASSWORD"));
            request.header("Authorization", "Basic " + Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8)));
        }
        HttpResponse<Void> response = client.send(request.build(), HttpResponse.BodyHandlers.discarding());
        if (response.statusCode() < 200 || response.statusCode() >= 400) {
            throw new IllegalStateException("CPF Binary Repository resolution failed: artifact=" + label + " http=" + response.statusCode() + " repository=" + safeUri(repository.toString()));
        }
    }

    private static String publicBinaryClassifier() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        String osPart = os.contains("win") ? "windows" : os.contains("linux") ? "linux" : "";
        String archPart = (arch.equals("amd64") || arch.equals("x86_64")) ? "x64" : (arch.equals("aarch64") || arch.equals("arm64")) ? "arm64" : "";
        if (osPart.isBlank() || archPart.isBlank()) throw new IllegalStateException("unsupported Public Generator OS/arch=" + os + "/" + arch);
        return osPart + "-" + archPart;
    }

    private void discoverDomains() throws Exception {
        Set<String> names = new HashSet<>(), codes = new HashSet<>();
        try (DirectoryStream<Path> children = Files.newDirectoryStream(root, "cpf-*")) {
            for (Path project : children) {
                Path contract = project.resolve("gradle.properties");
                if (!Files.isRegularFile(contract) || !Files.isRegularFile(project.resolve("settings.gradle"))) continue;
                Properties values = new Properties();
                try (Reader reader = Files.newBufferedReader(contract, StandardCharsets.UTF_8)) { values.load(reader); }
                if (!"1".equals(values.getProperty("cpf.domain.contractVersion"))) continue;
                String name = values.getProperty("cpf.domain.name", "").trim();
                String code = values.getProperty("cpf.domain.systemCode", "").trim();
                if (!name.matches("[a-z][a-z0-9-]*") || !code.matches("[A-Z][A-Z0-9]{2}"))
                    throw new IllegalStateException("invalid domain name/systemCode: " + contract);
                if (!names.add(name)) throw new IllegalStateException("duplicate domain name=" + name);
                if (!codes.add(code)) throw new IllegalStateException("duplicate systemCode=" + code);
                if (!project.getFileName().toString().equals("cpf-" + name)) throw new IllegalStateException("domain root/name mismatch: " + project);
                Map<String,String> features = new HashMap<>();
                for (String feature : List.of("persistence","cache","messaging")) features.put(feature, values.getProperty("cpf.domain." + feature, "none"));
                int localOnlinePort = Integer.parseInt(values.getProperty("cpf.domain.localOnlinePort", "0"));
                if (localOnlinePort < 18080 || localOnlinePort > 18999) throw new IllegalStateException("runtime.localOnlinePort out of range: " + localOnlinePort + " file=" + contract);
                DbBinding db = features.getOrDefault("persistence", "none").equals("none") ? null : loadDbBinding(name, code);
                domains.add(new Domain(name, code, contract, project, features, localOnlinePort, db));
            }
        }
        domains.sort(Comparator.comparing(Domain::name));
        if (domains.isEmpty()) throw new IllegalStateException("no generated/reference domain discovered");
        Set<Integer> ports = new HashSet<>();
        for (Domain d : domains) if (!ports.add(d.localOnlinePort)) throw new IllegalStateException("duplicate runtime.localOnlinePort=" + d.localOnlinePort);
        pass("05", "Workspace Discovery", "domains=" + domains.stream().map(d -> d.systemCode + ":" + d.name).toList());
    }

    /** 생성 파일은 플랫폼과 무관하게 같은 바이트여야 한다. */
    private static final String NEWLINE = "\n";

    /** Vendor 별 기본 Port. DB 구성 namespace 의 값이며 SystemCode 와 무관하다. */
    private static int defaultDbPort(String vendor) {
        return switch (vendor) {
            case "mariadb" -> 3306;
            case "postgresql" -> 5432;
            case "oracle" -> 1521;
            default -> throw new IllegalStateException("unsupported CPF default DB vendor: " + vendor);
        };
    }

    /**
     * local 전용 Domain DB Profile 을 canonical 기본값으로 생성한다.
     *
     * <p>공개 Consumer 는 Fresh Clone 후 bootstrap 만으로 local 실행까지 도달해야 한다. Domain 별
     * 프로필을 사람이 먼저 만들어야 한다면 첫 실행 경험이 성립하지 않는다. 대상 Domain 은
     * Workspace Domain 계약에서 발견된 것들이며 이름 목록을 코드에 적지 않는다.</p>
     *
     * <p>다음은 절대 하지 않는다.</p>
     * <ul>
     *   <li>비밀번호/secret 생성 또는 파일 기록 — env reference 만 둔다.</li>
     *   <li>local 이 아닌 environment 나 외부 DB 를 명시한 구성에서의 fallback — fail-closed 한다.</li>
     *   <li>SystemCode 를 DB vendor/host/port/schema 의 근거로 사용 — 서로 다른 namespace 다.</li>
     * </ul>
     */
    private void createLocalDomainDbProfile(Path profile, String name, String systemCode) throws Exception {
        String environment = envOrProperty("CPF_ENVIRONMENT", "cpf.environment", "local").trim().toLowerCase(Locale.ROOT);
        if (!environment.equals("local")) {
            throw new IllegalStateException("Domain DB profile is required outside the local environment: environment="
                    + environment + " path=" + root.relativize(profile));
        }
        // 외부 DB 를 명시한 구성은 자동 생성 대상이 아니다. 필수 값이 없으면 그대로 실패한다.
        String externalHost = firstNonBlank(System.getenv("CPF_DB_HOST"), System.getenv("CPF_DB_URL"));
        if (!externalHost.isBlank()) {
            throw new IllegalStateException("external DB configuration requires an explicit Domain DB profile: "
                    + root.relativize(profile));
        }
        String declared = firstNonBlank(System.getenv("CPF_DB_VENDOR"),
                workspace.getProperty("cpf.default-db", ""), requestedDbVendor);
        if (declared.isBlank()) {
            throw new IllegalStateException("cpf.default-db is required to prepare a local Domain DB profile: "
                    + root.relativize(profile));
        }
        String vendor = normalizeDb(declared);
        String lower = systemCode.toLowerCase(Locale.ROOT);
        String upper = systemCode.toUpperCase(Locale.ROOT);
        int port = defaultDbPort(vendor);
        String databaseEntry = vendor.equals("oracle")
                ? "    \"serviceName\": \"" + name + "_db\""
                : "    \"databaseName\": \"" + name + "_db\"";
        String json = String.join(NEWLINE,
                "{",
                "  \"profileVersion\": 2,",
                "  \"profileName\": \"" + name + "-local\",",
                "  \"environment\": \"local\",",
                "  \"domain\": {",
                "    \"name\": \"" + name + "\",",
                "    \"systemCode\": \"" + systemCode + "\"",
                "  },",
                "  \"database\": {",
                "    \"vendor\": \"" + vendor + "\",",
                "    \"host\": \"127.0.0.1\",",
                "    \"port\": " + port + ",",
                "    \"logicalDatabase\": \"" + lower + "DB\",",
                "    \"schemaName\": \"" + name + "\",",
                "    \"migration\": {",
                "      \"username\": \"cpf_" + lower + "_migration\",",
                "      \"password\": { \"env\": \"" + upper + "_DB_MIGRATION_PASSWORD\" }",
                "    },",
                "    \"runtime\": {",
                "      \"username\": \"cpf_" + lower + "_runtime\",",
                "      \"password\": { \"env\": \"" + upper + "_DB_RUNTIME_PASSWORD\" }",
                "    },",
                databaseEntry,
                "  },",
                "  \"sourceControlled\": false,",
                "  \"secretPolicy\": \"ENV_REFERENCE_ONLY\"",
                "}",
                "");
        Files.createDirectories(profile.getParent());
        Files.writeString(profile, json, StandardCharsets.UTF_8);
        progress("06", "Domain DB Profile", "created local default " + root.relativize(profile)
                + " vendor=" + vendor);
    }

    private DbBinding loadDbBinding(String name, String systemCode) throws Exception {
        Path profile = root.resolve("build/cpf-local").resolve(name).resolve("cpf-db-profile.local.json");
        if (!Files.isRegularFile(profile)) {
            // Fresh Clone -> bootstrap -> local 실행까지 사용자가 별도 설정 없이 갈 수 있어야 한다.
            // 프로필이 없으면 local 에 한해 canonical default-db 계약으로 생성한다.
            // 기존 프로필은 절대 덮어쓰지 않는다(있으면 아래 canonical validation 으로 검증만 한다).
            createLocalDomainDbProfile(profile, name, systemCode);
        }
        String text = Files.readString(profile, StandardCharsets.UTF_8);
        if (!text.contains("\"profileVersion\": 2")) throw new IllegalStateException("unsupported DB profileVersion: " + profile);
        if (!text.contains("\"name\": \""+name+"\"") || !text.contains("\"systemCode\": \""+systemCode+"\""))
            throw new IllegalStateException("DB profile Domain identity mismatch: " + profile);
        String vendor = normalizeDb(jsonString(text,"vendor"));
        if (!requestedDbVendor.isBlank() && !requestedDbVendor.equals(vendor))
            throw new IllegalStateException("--db is a compatibility constraint only and conflicts with Domain profile: domain="+systemCode+" requested="+requestedDbVendor+" profile="+vendor);
        String host = jsonString(text,"host");
        int port = jsonInt(text,"port");
        if (port < 1 || port > 65535) throw new IllegalStateException("DB profile port out of range: " + profile);
        String logicalDatabase = jsonString(text,"logicalDatabase");
        String databaseName = jsonStringOptional(text,"databaseName");
        String serviceName = jsonStringOptional(text,"serviceName");
        String schemaName = jsonString(text,"schemaName");
        List<String> users = jsonAllStrings(text,"username");
        List<String> secrets = jsonAllStrings(text,"env");
        if (users.size() != 2 || secrets.size() != 2) throw new IllegalStateException("DB profile requires exactly migration/runtime accounts and secret references: " + profile);
        if (users.get(0).equals(users.get(1))) throw new IllegalStateException("migration/runtime DB accounts must differ: " + profile);
        for (String secret : secrets) if (!secret.matches("[A-Z][A-Z0-9_]{2,127}"))
            throw new IllegalStateException("DB profile secret must be ENV reference only: " + profile);
        if (vendor.equals("oracle") && serviceName.isBlank()) throw new IllegalStateException("Oracle DB profile requires serviceName: " + profile);
        if (!vendor.equals("oracle") && databaseName.isBlank()) throw new IllegalStateException(vendor + " DB profile requires databaseName: " + profile);
        return new DbBinding(vendor, host, port, databaseName, serviceName, schemaName, users.get(0), users.get(1), secrets.get(0), secrets.get(1), profile);
    }

    private static String jsonString(String text,String key){ String v=jsonStringOptional(text,key); if(v.isBlank()) throw new IllegalStateException("DB profile field missing="+key); return v; }
    private static String jsonStringOptional(String text,String key){ Matcher m=Pattern.compile("\\\""+Pattern.quote(key)+"\\\"\\s*:\\s*\\\"([^\\\"]*)\\\"").matcher(text); return m.find()?m.group(1).trim():""; }
    private static int jsonInt(String text,String key){ Matcher m=Pattern.compile("\\\""+Pattern.quote(key)+"\\\"\\s*:\\s*(\\d+)").matcher(text); if(!m.find()) throw new IllegalStateException("DB profile field missing="+key); return Integer.parseInt(m.group(1)); }
    private static List<String> jsonAllStrings(String text,String key){ List<String> out=new ArrayList<>(); Matcher m=Pattern.compile("\\\""+Pattern.quote(key)+"\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(text); while(m.find())out.add(m.group(1).trim()); return out; }

    private void prepareLocalSecret() throws Exception {
        Path secretFile = root.resolve("build/cpf-bootstrap/local-secrets.properties");
        Files.createDirectories(secretFile.getParent());
        Properties p = new Properties();
        if (Files.isRegularFile(secretFile)) try (Reader r = Files.newBufferedReader(secretFile, StandardCharsets.UTF_8)) { p.load(r); }
        localDbPassword = p.getProperty("local.db.password", "").trim();
        if (localDbPassword.isBlank()) {
            byte[] bytes = new byte[24]; new SecureRandom().nextBytes(bytes);
            localDbPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            p.setProperty("local.db.password", localDbPassword);
            try (Writer w = Files.newBufferedWriter(secretFile, StandardCharsets.UTF_8)) { p.store(w, "Local-only generated secret. build/ is not source-controlled."); }
        }
        baseEnv.put("CPF_LOCAL_DB_ADMIN_PASSWORD", localDbPassword);
        // Docker Compose 는 서비스를 하나만 올려도 파일 전체를 보간한다. 미들웨어 변수를
        // 나중 단계에서만 넣으면, DB 만 띄우는 단계가 "다른 서비스의 필수 변수 없음"으로 실패한다.
        // 값은 이미 만든 local 전용 생성 시크릿과 같으며 저장소에 남지 않는다.
        baseEnv.put("CPF_LOCAL_MIDDLEWARE_PASSWORD", localDbPassword);
        // Platform Runtime 이 기동 자체에 요구하는 local 전용 Secret 도 같은 방식으로 준비한다.
        // 값은 저장소가 아니라 build/ 아래에만 남고, 환경이 이미 값을 주면 생성하지 않는다.
        // 이것이 없으면 공개 Consumer 는 ADM 기동에서 "approval proof key must be valid Base64" 로 막힌다.
        ensureLocalSecret(secretFile, p, "local.adm.approvalProofKeyBase64",
                "CPF_ADM_APPROVAL_PROOF_KEY_BASE64", 32);
    }

    /**
     * local 전용 생성 Secret 하나를 보장한다.
     *
     * <p>환경이 값을 주면 그대로 쓰고 저장하지 않는다. 없을 때만 build/ 아래 local secret 파일에
     * 생성해 둔다. Source 나 Release 산출물에는 어떤 경우에도 남기지 않는다.</p>
     */
    private void ensureLocalSecret(Path secretFile, Properties store, String key, String envName, int byteLength)
            throws Exception {
        String supplied = firstNonBlank(System.getenv(envName));
        if (!supplied.isEmpty()) { baseEnv.put(envName, supplied); return; }
        String value = store.getProperty(key, "").trim();
        if (value.isBlank()) {
            byte[] bytes = new byte[byteLength]; new SecureRandom().nextBytes(bytes);
            value = Base64.getEncoder().encodeToString(bytes);
            store.setProperty(key, value);
            try (Writer w = Files.newBufferedWriter(secretFile, StandardCharsets.UTF_8)) {
                store.store(w, "Local-only generated secret. build/ is not source-controlled.");
            }
        }
        baseEnv.put(envName, value);
    }

    private void prepareDatabase() throws Exception {
        Set<String> vendors = new TreeSet<>();
        for (Domain d : domains) if (d.db != null) vendors.add(d.db.vendor);
        if (vendors.isEmpty()) { skip("06", "Database", "no persistent domain selected"); return; }
        Path compose = root.resolve("deploy/local/compose.yaml");
        if (!Files.isRegularFile(compose)) throw new IllegalStateException("local compose asset missing: " + compose);
        for (String vendor : vendors) {
            String service = switch (vendor) { case "postgresql" -> "cpf-postgresql"; case "mariadb" -> "cpf-mariadb"; case "oracle" -> "cpf-oracle"; default -> throw new IllegalStateException(); };
            String container = switch (vendor) { case "postgresql" -> "cpf-public-postgresql"; case "mariadb" -> "cpf-public-mariadb"; default -> "cpf-public-oracle"; };
            step("06", "Database", "START vendor=" + vendor);
            runChecked(List.of("docker", "compose", "-f", compose.toString(), "up", "-d", service), baseEnv, timeoutSeconds, null, true);
            waitForHealthy(container);
            pass("06", "Database", vendor + " HEALTH=PASS");
        }
    }

    private void waitForHealthy(String container) throws Exception { waitForHealthy(container, "06", "Database Health"); }

    private void waitForHealthy(String container, String stepNo, String label) throws Exception {
        long start = System.nanoTime();
        while (Duration.ofNanos(System.nanoTime() - start).toSeconds() < timeoutSeconds) {
            ExecResult status = run(List.of("docker", "inspect", "--format={{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}", container), Map.of(), 20, null, false, true);
            String value = status.output.trim();
            if (status.exit == 0 && (value.equals("healthy") || value.equals("running"))) return;
            long elapsed = Duration.ofNanos(System.nanoTime() - start).toSeconds();
            progress(stepNo, label, "container=" + container + " state=" + (value.isBlank() ? "unknown" : value) + " elapsed=" + elapsed + "s/" + timeoutSeconds + "s");
            Thread.sleep(5000);
        }
        throw new IllegalStateException(label + " timeout container=" + container + " timeout=" + timeoutSeconds + "s");
    }

    private void prepareMiddleware() throws Exception {
        Set<String> services = new TreeSet<>();
        Set<String> capabilities = new TreeSet<>();
        for (Domain d : domains) {
            String cache = d.features.getOrDefault("cache", "none");
            String messaging = d.features.getOrDefault("messaging", "none");
            switch (cache) {
                case "none", "caffeine" -> { if (cache.equals("caffeine")) capabilities.add("cache:caffeine(in-process)"); }
                case "redis" -> { services.add("cpf-redis"); capabilities.add("cache:redis"); }
                case "valkey" -> { services.add("cpf-valkey"); capabilities.add("cache:valkey"); }
                default -> throw new IllegalStateException("unsupported cache capability=" + cache + " domain=" + d.systemCode);
            }
            switch (messaging) {
                case "none" -> { }
                case "kafka" -> { services.add("cpf-kafka"); capabilities.add("messaging:kafka"); }
                case "rabbitmq" -> { services.add("cpf-rabbitmq"); capabilities.add("messaging:rabbitmq"); }
                case "jms" -> { services.add("cpf-artemis"); capabilities.add("messaging:jms(artemis)"); }
                case "ibm-mq" -> {
                    if (!"true".equalsIgnoreCase(System.getenv("CPF_IBM_MQ_ACCEPT_LICENSE"))) {
                        throw new IllegalStateException("IBM MQ local runtime requires explicit CPF_IBM_MQ_ACCEPT_LICENSE=true; license acceptance is never implicit");
                    }
                    services.add("cpf-ibm-mq"); capabilities.add("messaging:ibm-mq");
                }
                default -> throw new IllegalStateException("unsupported messaging capability=" + messaging + " domain=" + d.systemCode);
            }
        }
        if (services.isEmpty()) {
            if (capabilities.isEmpty()) skip("07", "Middleware", "selected capabilities require none");
            else pass("07", "Middleware", "external containers=none capabilities=" + capabilities);
            return;
        }
        baseEnv.put("CPF_LOCAL_MIDDLEWARE_PASSWORD", localDbPassword);
        Path compose = root.resolve("deploy/local/compose.yaml");
        for (String service : services) {
            List<String> command = new ArrayList<>(List.of("docker", "compose", "-f", compose.toString()));
            if (service.equals("cpf-ibm-mq")) command.addAll(List.of("--profile", "ibm-mq"));
            command.addAll(List.of("up", "-d", service));
            runChecked(command, baseEnv, timeoutSeconds, null, true);
            waitForHealthy(middlewareContainer(service), "07", "Middleware Health");
        }
        configureMiddlewareEnvironment(capabilities);
        pass("07", "Middleware", "services=" + services + " capabilities=" + capabilities);
    }

    private static String middlewareContainer(String service) {
        return switch (service) {
            case "cpf-redis" -> "cpf-public-redis";
            case "cpf-valkey" -> "cpf-public-valkey";
            case "cpf-kafka" -> "cpf-public-kafka";
            case "cpf-rabbitmq" -> "cpf-public-rabbitmq";
            case "cpf-artemis" -> "cpf-public-artemis";
            case "cpf-ibm-mq" -> "cpf-public-ibm-mq";
            default -> throw new IllegalArgumentException("unknown middleware service=" + service);
        };
    }

    private void configureMiddlewareEnvironment(Set<String> capabilities) {
        if (capabilities.contains("cache:redis")) {
            baseEnv.put("SPRING_DATA_REDIS_HOST", "127.0.0.1");
            baseEnv.put("SPRING_DATA_REDIS_PORT", envOrDefault("CPF_REDIS_PORT", "6379"));
            baseEnv.put("SPRING_DATA_REDIS_PASSWORD", localDbPassword);
            baseEnv.put("CPF_DATA_CACHE_REDIS_ENABLED", "true");
        }
        if (capabilities.contains("cache:valkey")) {
            baseEnv.put("SPRING_DATA_REDIS_HOST", "127.0.0.1");
            baseEnv.put("SPRING_DATA_REDIS_PORT", envOrDefault("CPF_VALKEY_PORT", "6380"));
            baseEnv.put("SPRING_DATA_REDIS_PASSWORD", localDbPassword);
            baseEnv.put("CPF_DATA_CACHE_VALKEY_ENABLED", "true");
        }
        if (capabilities.contains("messaging:kafka")) baseEnv.put("SPRING_KAFKA_BOOTSTRAP_SERVERS", "127.0.0.1:" + envOrDefault("CPF_KAFKA_PORT", "9092"));
        if (capabilities.contains("messaging:rabbitmq")) {
            baseEnv.put("SPRING_RABBITMQ_HOST", "127.0.0.1");
            baseEnv.put("SPRING_RABBITMQ_PORT", envOrDefault("CPF_RABBITMQ_PORT", "5672"));
            baseEnv.put("SPRING_RABBITMQ_USERNAME", envOrDefault("CPF_RABBITMQ_USER", "cpf"));
            baseEnv.put("SPRING_RABBITMQ_PASSWORD", localDbPassword);
            baseEnv.put("SPRING_RABBITMQ_VIRTUAL_HOST", envOrDefault("CPF_RABBITMQ_VHOST", "/cpf"));
        }
        if (capabilities.contains("messaging:jms(artemis)")) {
            baseEnv.put("SPRING_ARTEMIS_MODE", "native");
            baseEnv.put("SPRING_ARTEMIS_BROKER_URL", "tcp://127.0.0.1:" + envOrDefault("CPF_ARTEMIS_PORT", "61616"));
            baseEnv.put("SPRING_ARTEMIS_USER", envOrDefault("CPF_ARTEMIS_USER", "cpf"));
            baseEnv.put("SPRING_ARTEMIS_PASSWORD", localDbPassword);
        }
        if (capabilities.contains("messaging:ibm-mq")) {
            baseEnv.put("CPF_IBM_MQ_QUEUE_MANAGER", envOrDefault("CPF_IBM_MQ_QMGR", "QM1"));
            baseEnv.put("CPF_IBM_MQ_CONNECTION_NAME", "127.0.0.1(" + envOrDefault("CPF_IBM_MQ_PORT", "1414") + ")");
        }
    }

    /**
     * Platform DB(CPF_PLATFORM_DB) 의 local Binding.
     *
     * <p>증상 근거: 공개 bootstrap 은 Generated Domain DB 만 만들었다. 그래서 발행된 ADM 을
     * 단독 기동하면 {@code CPF DataSource is required for role: CPF_PLATFORM_DB} 로 죽었다.
     * ADM/Gateway/Batch 는 Domain 이 아니라 Platform 스키마를 쓴다.</p>
     *
     * <p>이 Binding 은 DB 구성 namespace 이며 SystemCode 와 무관하다. Platform 은 SystemCode 를
     * 갖지 않으므로 moduleCode(CPF) 기준 이름만 쓴다.</p>
     */
    private DbBinding platformBinding() throws Exception {
        if (platformDb != null) return platformDb;
        String environment = envOrProperty("CPF_ENVIRONMENT", "cpf.environment", "local").trim().toLowerCase(Locale.ROOT);
        if (!environment.equals("local")) {
            throw new IllegalStateException("Platform DB binding must be configured explicitly outside local: environment=" + environment);
        }
        String externalHost = firstNonBlank(System.getenv("CPF_DB_HOST"), System.getenv("CPF_DB_URL"));
        if (!externalHost.isBlank()) {
            throw new IllegalStateException("external DB configuration requires an explicit Platform DB configuration");
        }
        String declared = firstNonBlank(System.getenv("CPF_DB_VENDOR"),
                workspace.getProperty("cpf.default-db", ""), requestedDbVendor);
        if (declared.isBlank()) throw new IllegalStateException("cpf.default-db is required to prepare the local Platform DB");
        String vendor = normalizeDb(declared);
        String database = PLATFORM_DATABASE;
        boolean oracle = vendor.equals("oracle");
        platformDb = new DbBinding(vendor, "127.0.0.1", defaultDbPort(vendor),
                oracle ? "" : database, oracle ? database : "",
                vendor.equals("postgresql") ? "public" : database,
                "cpf_migration", "cpf_app",
                "CPF_PLATFORM_DB_MIGRATION_PASSWORD", "CPF_PLATFORM_DB_RUNTIME_PASSWORD",
                root.resolve(PLATFORM_ASSET_ROOT));
        return platformDb;
    }

    /** Platform 은 Generated Domain 이 아니다. Domain 절차를 재사용하되 Domain 목록에는 넣지 않는다. */
    private Domain platformDomain() throws Exception {
        return new Domain("platform", PLATFORM_MODULE_CODE, null, null, Map.of(), 0, platformBinding());
    }

    /**
     * Vendor SQL Pack 의 공개 배포 경로.
     *
     * <p>Platform Runtime 은 module-local SQL fallback 을 지원하지 않고 외부 Pack 만 사용한다
     * (cpf.db.resource-root). 이 자산이 없으면 어떤 Runtime 도 SqlSessionFactory 를 만들지 못하고
     * 기동에 실패하므로, 경로를 추론하지 않고 없으면 즉시 멈춘다.</p>
     */
    private Path vendorPackRoot(String vendor) {
        Path pack = root.resolve(VENDOR_PACK_ROOT).resolve(vendor);
        if (!Files.isDirectory(pack) || !Files.isRegularFile(pack.resolve("pack.json"))) {
            throw new IllegalStateException("DB Vendor SQL Pack is missing: " + root.relativize(pack));
        }
        return pack;
    }

    private void applyPlatformDatabase() throws Exception {
        Domain platform = platformDomain();
        step("08", "DB Lifecycle", "PLATFORM " + PLATFORM_MODULE_CODE + " vendor=" + platform.db.vendor);
        switch (platform.db.vendor) {
            case "postgresql" -> applyPostgresql(platform);
            case "mariadb" -> applyMariaDb(platform);
            case "oracle" -> applyOracle(platform);
        }
    }

    /**
     * Platform Runtime 이 읽을 접속 정보를 실행 환경에 반영한다.
     *
     * <p>비밀 값은 파일로 남기지 않고 ENV 참조 표기만 남긴다.</p>
     */
    private void bindPlatformRuntimeEnvironment(List<String> lines) throws Exception {
        DbBinding b = platformBinding();
        String url;
        String driver;
        switch (b.vendor) {
            case "postgresql" -> { url = "jdbc:postgresql://" + b.host + ":" + b.port + "/" + b.databaseName + "?currentSchema=" + b.schemaName; driver = "org.postgresql.Driver"; }
            case "mariadb" -> { url = "jdbc:mariadb://" + b.host + ":" + b.port + "/" + b.databaseName; driver = "org.mariadb.jdbc.Driver"; }
            case "oracle" -> { url = "jdbc:oracle:thin:@//" + b.host + ":" + b.port + "/" + b.serviceName; driver = "oracle.jdbc.OracleDriver"; }
            default -> throw new IllegalStateException();
        }
        // ADM/Gateway/Batch are Platform consumers. Their shared MyBatis SQL pack reads the
        // canonical generic cpf.db.vendor, not a domain-specific datasource prefix.
        // 지금까지는 One-WAS 통합 Runtime 이 batch runtime-support 설정으로 이 값을 줬기 때문에
        // 단독 기동 경로에서는 vendor 가 비어 있었다.
        lines.add("CPF_DB_VENDOR=" + b.vendor);
        lines.add("CPF_DB_RESOURCE_ROOT=" + vendorPackRoot(b.vendor));
        lines.add("CPF_PLATFORM_DB_URL=" + url);
        lines.add("CPF_PLATFORM_DB_DRIVER=" + driver);
        lines.add("CPF_PLATFORM_DB_USERNAME=" + b.runtimeUser);
        lines.add("CPF_PLATFORM_DB_PASSWORD=<secret:" + b.runtimeSecretEnv + ">");
        baseEnv.put("CPF_DB_VENDOR", b.vendor);
        baseEnv.put("CPF_DB_RESOURCE_ROOT", vendorPackRoot(b.vendor).toString());
        baseEnv.put("CPF_PLATFORM_DB_URL", url);
        baseEnv.put("CPF_PLATFORM_DB_DRIVER", driver);
        baseEnv.put("CPF_PLATFORM_DB_USERNAME", b.runtimeUser);
        baseEnv.put("CPF_PLATFORM_DB_PASSWORD", localSecret(b.runtimeSecretEnv));
    }

    private void applyDomainDatabases() throws Exception {
        int applied=0;
        for (Domain domain : domains) {
            if (domain.db == null) continue;
            applied++;
            step("08", "DB Lifecycle", domain.systemCode + " " + domain.name + " vendor=" + domain.db.vendor);
            switch (domain.db.vendor) {
                case "postgresql" -> applyPostgresql(domain);
                case "mariadb" -> applyMariaDb(domain);
                case "oracle" -> applyOracle(domain);
            }
        }
        if (applied==0) skip("08", "DB Lifecycle", "no persistent domain selected");
        else pass("08", "DB Lifecycle", "persistentDomains=" + applied);
    }

    private String localSecret(String envName) {
        String explicit=System.getenv(envName);
        if (explicit!=null && !explicit.isBlank()) return explicit;
        return localDbPassword;
    }

    private void applyPostgresql(Domain d) throws Exception {
        DbBinding b=d.db; String db=b.databaseName; String migration=b.migrationUser; String runtime=b.runtimeUser;
        String mp=localSecret(b.migrationSecretEnv), rp=localSecret(b.runtimeSecretEnv);
        // Local container volume은 bootstrap 간에 살아 있을 수 있다. CREATE ... IF NOT EXISTS만
        // 하면 새 local secret과 기존 role credential이 달라져 실제 Runtime TCP 접속만 실패한다.
        // 이 경로는 local-only generated secret / cpf-public-postgresql에만 적용되므로, 매 bootstrap이
        // migration/runtime role credential을 현재 local secret으로 명시적으로 수렴시킨다.
        String setup = "SELECT format('CREATE ROLE %I LOGIN PASSWORD %L',"+sqlLiteral(migration)+","+sqlLiteral(mp)+") WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname="+sqlLiteral(migration)+")\\gexec\n" +
                "SELECT format('CREATE ROLE %I LOGIN PASSWORD %L',"+sqlLiteral(runtime)+","+sqlLiteral(rp)+") WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname="+sqlLiteral(runtime)+")\\gexec\n" +
                "SELECT format('ALTER ROLE %I LOGIN PASSWORD %L',"+sqlLiteral(migration)+","+sqlLiteral(mp)+")\\gexec\n" +
                "SELECT format('ALTER ROLE %I LOGIN PASSWORD %L',"+sqlLiteral(runtime)+","+sqlLiteral(rp)+")\\gexec\n" +
                "SELECT format('CREATE DATABASE %I OWNER %I',"+sqlLiteral(db)+","+sqlLiteral(migration)+") WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname="+sqlLiteral(db)+")\\gexec\n";
        runChecked(List.of("docker","exec","-i","cpf-public-postgresql","psql","-v","ON_ERROR_STOP=1","-U","postgres","-d","postgres"), baseEnv, 60, setup, true);
        runChecked(List.of("docker","exec","-i","-e","PGPASSWORD="+mp,"cpf-public-postgresql","psql","-v","ON_ERROR_STOP=1","-U",migration,"-d",db), Map.of(), 60,
                "CREATE SCHEMA IF NOT EXISTS \""+b.schemaName+"\" AUTHORIZATION \""+migration+"\"; CREATE TABLE IF NOT EXISTS \""+b.schemaName+"\".cpf_bootstrap_schema_history(script_name VARCHAR(300) PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP); GRANT USAGE ON SCHEMA \""+b.schemaName+"\" TO \""+runtime+"\";"
                // DDL 은 schema 를 한정하지 않는다. 적용 세션의 기본 schema 를 Domain schema 로 고정하지
                // 않으면 테이블이 public 에 만들어지고 Runtime 이 자기 schema 에서 찾지 못한다.
                + " ALTER ROLE \""+migration+"\" IN DATABASE \""+db+"\" SET search_path TO \""+b.schemaName+"\",public;", true);
        applyTrackedSql(d, db, migration, "cpf-public-postgresql", "postgresql", mp);
        reconcilePostgresqlRuntimePrivileges(b, db, migration, runtime, mp);
    }

    private void applyMariaDb(Domain d) throws Exception {
        DbBinding b=d.db; String db=b.databaseName; String migration=b.migrationUser; String runtime=b.runtimeUser;
        String mp=localSecret(b.migrationSecretEnv), rp=localSecret(b.runtimeSecretEnv);
        // PostgreSQL과 같은 local-volume 재사용 경로다. 존재하는 account도 local secret으로
        // reconcile하지 않으면 next bootstrap의 Runtime credential과 갈라진다.
        String setup = "CREATE DATABASE IF NOT EXISTS `"+db+"`; CREATE USER IF NOT EXISTS '"+migration+"'@'%' IDENTIFIED BY '"+mp+"'; CREATE USER IF NOT EXISTS '"+runtime+"'@'%' IDENTIFIED BY '"+rp+"'; ALTER USER '"+migration+"'@'%' IDENTIFIED BY '"+mp+"'; ALTER USER '"+runtime+"'@'%' IDENTIFIED BY '"+rp+"'; GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,DROP,INDEX,REFERENCES ON `"+db+"`.* TO '"+migration+"'@'%'; GRANT SELECT,INSERT,UPDATE,DELETE,EXECUTE ON `"+db+"`.* TO '"+runtime+"'@'%'; FLUSH PRIVILEGES;";
        runChecked(List.of("docker","exec","-i","-e","MYSQL_PWD="+localDbPassword,"cpf-public-mariadb","mariadb","-uroot"), Map.of(), 60, setup, true);
        runChecked(List.of("docker","exec","-i","-e","MYSQL_PWD="+mp,"cpf-public-mariadb","mariadb","-u"+migration,db), Map.of(), 60,
                "CREATE TABLE IF NOT EXISTS cpf_bootstrap_schema_history(script_name VARCHAR(300) PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3));", true);
        applyTrackedSql(d, db, migration, "cpf-public-mariadb", "mariadb", mp);
    }

    private void applyOracle(Domain d) throws Exception {
        DbBinding b=d.db; String migration=b.migrationUser.toUpperCase(Locale.ROOT), runtime=b.runtimeUser.toUpperCase(Locale.ROOT);
        String mp=localSecret(b.migrationSecretEnv), rp=localSecret(b.runtimeSecretEnv);
        String service=b.serviceName;
        String setup = "WHENEVER SQLERROR EXIT SQL.SQLCODE\nALTER SESSION SET CONTAINER="+service+";\n" +
                "DECLARE n NUMBER; BEGIN SELECT COUNT(*) INTO n FROM dba_users WHERE username='"+migration+"'; IF n=0 THEN EXECUTE IMMEDIATE 'CREATE USER "+migration+" IDENTIFIED BY \\\""+mp+"\\\"'; EXECUTE IMMEDIATE 'GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE TRIGGER, CREATE PROCEDURE TO "+migration+"'; EXECUTE IMMEDIATE 'ALTER USER "+migration+" QUOTA UNLIMITED ON USERS'; END IF; SELECT COUNT(*) INTO n FROM dba_users WHERE username='"+runtime+"'; IF n=0 THEN EXECUTE IMMEDIATE 'CREATE USER "+runtime+" IDENTIFIED BY \\\""+rp+"\\\"'; EXECUTE IMMEDIATE 'GRANT CREATE SESSION TO "+runtime+"'; END IF; END; /\n" +
                // Existing local Oracle accounts must be reconciled too; CREATE USER is not a credential update.
                "ALTER USER "+migration+" IDENTIFIED BY \\\""+mp+"\\\";\nALTER USER "+runtime+" IDENTIFIED BY \\\""+rp+"\\\";\n" +
                "CONNECT "+migration+"/\\\""+mp+"\\\"@"+service+"\nBEGIN EXECUTE IMMEDIATE 'CREATE TABLE cpf_bootstrap_schema_history (script_name VARCHAR2(300) PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL)'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END; /\nEXIT\n";
        runChecked(List.of("docker","exec","-i","cpf-public-oracle","sqlplus","-s","/","as","sysdba"), Map.of(), 90, setup, true);
        applyTrackedSql(d, service, migration, "cpf-public-oracle", "oracle", mp);
        reconcileOracleRuntimePrivileges(service, migration, runtime, mp);
    }

    /**
     * The migration role owns local PostgreSQL tables.  Each bootstrap must converge both existing
     * objects and future migration objects to the runtime role; schema USAGE alone cannot read a table.
     */
    private void reconcilePostgresqlRuntimePrivileges(DbBinding binding, String database, String migration,
                                                        String runtime, String migrationPassword) throws Exception {
        String schema = binding.schemaName;
        String grants = "GRANT USAGE ON SCHEMA \""+schema+"\" TO \""+runtime+"\";" +
                "GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA \""+schema+"\" TO \""+runtime+"\";" +
                "GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA \""+schema+"\" TO \""+runtime+"\";" +
                "ALTER DEFAULT PRIVILEGES IN SCHEMA \""+schema+"\" GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO \""+runtime+"\";" +
                "ALTER DEFAULT PRIVILEGES IN SCHEMA \""+schema+"\" GRANT USAGE, SELECT ON SEQUENCES TO \""+runtime+"\";";
        runChecked(List.of("docker", "exec", "-i", "-e", "PGPASSWORD="+migrationPassword,
                        "cpf-public-postgresql", "psql", "-v", "ON_ERROR_STOP=1", "-U", migration, "-d", database),
                Map.of(), 60, grants, true);
    }

    /** Oracle has no database-wide runtime DML grant; converge every migration-owned product object. */
    private void reconcileOracleRuntimePrivileges(String service, String migration, String runtime,
                                                   String migrationPassword) throws Exception {
        String grants = "BEGIN " +
                "FOR item IN (SELECT table_name FROM user_tables) LOOP EXECUTE IMMEDIATE 'GRANT SELECT, INSERT, UPDATE, DELETE ON ' || item.table_name || ' TO "+runtime+"'; END LOOP; " +
                "FOR item IN (SELECT sequence_name FROM user_sequences) LOOP EXECUTE IMMEDIATE 'GRANT SELECT ON ' || item.sequence_name || ' TO "+runtime+"'; END LOOP; " +
                "FOR item IN (SELECT object_name FROM user_objects WHERE object_type IN ('FUNCTION','PROCEDURE','PACKAGE')) LOOP EXECUTE IMMEDIATE 'GRANT EXECUTE ON ' || item.object_name || ' TO "+runtime+"'; END LOOP; " +
                "END; /";
        executeSqlFile(service, migration, "cpf-public-oracle", "oracle", grants, false, migrationPassword);
    }

    /** Render a PostgreSQL SQL literal without exposing a local secret in a log or evidence file. */
    private static String sqlLiteral(String value) {
        return "'" + value.replace("'", "''") + "'";
    }

    private void applyTrackedSql(Domain d, String db, String user, String container, String vendor, String password) throws Exception {
        String history = historyTable(vendor, d.db.schemaName);
        Path base;
        Path shippedDomainAsset = root.resolve(DOMAIN_ASSET_ROOT).resolve(d.name).resolve(vendor);
        if (d.contract == null) {
            // Platform DDL 은 Domain Generator 산출물이 아니라 배포본에 실려 오는 정본 자산이다.
            base = root.resolve(PLATFORM_ASSET_ROOT).resolve(vendor);
            if (!Files.isDirectory(base)) throw new IllegalStateException("Platform DB asset is missing: " + root.relativize(base));
        } else if (Files.isDirectory(shippedDomainAsset)) {
            // Prebuilt Domain(예: Backoffice)의 스키마는 Generator 가 매번 만들지 않는다. 배포본이
            // 싣고 온 정본 DDL 을 그대로 적용한다. 이것이 없으면 Domain DB 가 빈 채로 남아
            // Runtime 이 자기 테이블을 찾지 못한다.
            base = shippedDomainAsset;
        } else {
            base = root.resolve("build/cpf-local").resolve(d.name).resolve("db3").resolve(vendor);
            Files.createDirectories(base);
            List<String> render=List.of(javaExecutable(), "-Dfile.encoding=UTF-8", "-cp", root.resolve("bin/lib/cpf-cli.jar").toString(), "CpfGeneratorLauncher", "db", "render", "--file", root.relativize(d.contract).toString(), "--vendor", vendor, "--output", base.toString());
            runChecked(render, baseEnv, Math.max(timeoutSeconds,120), null, true);
        }
        List<Path> migrations;
        try(var stream=Files.list(base)){ migrations=stream.filter(p->p.getFileName().toString().startsWith("V")&&p.getFileName().toString().endsWith(".sql")).sorted().toList(); }
        for (Path sql : migrations) applyOneTrackedSql(d, db, user, container, vendor, sql, password, history);
        Path seed=base.resolve("20_product_seed.sql"); if(Files.isRegularFile(seed)) applyOneTrackedSql(d, db, user, container, vendor, seed, password, history);
        Path verify=base.resolve("90_verify.sql"); if(Files.isRegularFile(verify)) executeSqlFile(db,user,container,vendor,Files.readString(verify,StandardCharsets.UTF_8),false,password);
    }

    private void applyOneTrackedSql(Domain d, String db, String user, String container, String vendor, Path sql, String password, String history) throws Exception {
        String name = root.relativize(sql).toString().replace('\\','/');
        String checksum = sha256(sql);
        String existing = queryHistory(db, user, container, vendor, name, password, history);
        if (!existing.isBlank()) {
            if (!existing.equalsIgnoreCase(checksum)) throw new IllegalStateException("immutable DB migration checksum mismatch: " + name);
            progress("08", "DB Lifecycle", d.systemCode + " SKIP already-applied " + sql.getFileName());
            return;
        }
        executeSqlFile(db, user, container, vendor, Files.readString(sql, StandardCharsets.UTF_8), true, password);
        recordHistory(db, user, container, vendor, name, checksum, password, history);
    }

    /**
     * 마이그레이션 이력 테이블 이름.
     *
     * <p>PostgreSQL 은 search_path 에 따라 같은 이름이 다른 schema 로 해석된다. 이름을 한정하지
     * 않으면 회차마다 다른 schema 의 이력 테이블을 보게 되어 이미 적용한 DDL 을 다시 실행한다.</p>
     */
    private static String historyTable(String vendor, String schemaName) {
        return vendor.equals("postgresql") ? "\"" + schemaName + "\".cpf_bootstrap_schema_history"
                : "cpf_bootstrap_schema_history";
    }

    private String queryHistory(String db, String user, String container, String vendor, String name, String password, String history) throws Exception {
        String safe = name.replace("'", "''");
        if (vendor.equals("postgresql")) return run(List.of("docker","exec","-e","PGPASSWORD="+password,container,"psql","-At","-U",user,"-d",db,"-c","SELECT checksum FROM "+history+" WHERE script_name='"+safe+"'"), Map.of(), 30, null, true, true).output.trim();
        if (vendor.equals("mariadb")) return run(List.of("docker","exec","-e","MYSQL_PWD="+password,container,"mariadb","-N","-s","-u"+user,db,"-e","SELECT checksum FROM "+history+" WHERE script_name='"+safe+"'"), Map.of(), 30, null, true, true).output.trim();
        String script = "WHENEVER SQLERROR EXIT SQL.SQLCODE\nALTER SESSION SET CONTAINER="+db+";\nCONNECT "+user+"/\""+password+"\"@"+db+"\nSET HEADING OFF FEEDBACK OFF PAGESIZE 0\nSELECT checksum FROM "+history+" WHERE script_name='"+safe+"';\nEXIT\n";
        return run(List.of("docker","exec","-i",container,"sqlplus","-s","/","as","sysdba"), Map.of(), 40, script, true, true).output.trim();
    }

    private void recordHistory(String db, String user, String container, String vendor, String name, String checksum, String password, String history) throws Exception {
        String sql = "INSERT INTO "+history+"(script_name,checksum) VALUES ('"+name.replace("'","''")+"','"+checksum+"');";
        executeSqlFile(db, user, container, vendor, sql, false, password);
    }

    private void executeSqlFile(String db, String user, String container, String vendor, String sql, boolean lifecycle, String password) throws Exception {
        if (vendor.equals("postgresql")) {
            runChecked(List.of("docker","exec","-i","-e","PGPASSWORD="+password,container,"psql","-v","ON_ERROR_STOP=1","-U",user,"-d",db), Map.of(), 90, sql, true); return;
        }
        if (vendor.equals("mariadb")) {
            runChecked(List.of("docker","exec","-i","-e","MYSQL_PWD="+password,container,"mariadb","-u"+user,db), Map.of(), 90, sql, true); return;
        }
        String script = "WHENEVER SQLERROR EXIT SQL.SQLCODE\nALTER SESSION SET CONTAINER="+db+";\nCONNECT "+user+"/\""+password+"\"@"+db+"\n" + sql + "\nEXIT\n";
        runChecked(List.of("docker","exec","-i",container,"sqlplus","-s","/","as","sysdba"), Map.of(), 120, script, true);
    }

    private void writeRuntimeEnvironment() throws Exception {
        Path env = logDir.resolve("runtime.env");
        Files.write(env, bindDomainRuntimeEnvironment(), StandardCharsets.UTF_8);
        pass("09", "Runtime Config", "generated=" + root.relativize(env) + " per-domain DB binding=PASS");
    }

    /**
     * Domain 별 DB Binding 과 Port 를 실행 환경(baseEnv)에 반영하고, 비밀이 없는 요약 줄을 돌려준다.
     *
     * <p>bootstrap 과 runtime 이 같은 계산을 공유해야 한다. 계산이 갈라지면 bootstrap 이 만든
     * DB 와 실제 Runtime 이 접속하는 DB 가 달라진다.</p>
     */
    private List<String> bindDomainRuntimeEnvironment() throws Exception {
        List<String> lines = new ArrayList<>();
        bindPlatformRuntimeEnvironment(lines);
        lines.add("CPF_VERSION=" + baseEnv.get("CPF_VERSION"));
        lines.add("CPF_MAVEN_REPOSITORY_URL=" + baseEnv.get("CPF_MAVEN_REPOSITORY_URL"));
        for (Domain d : domains) {
            if (d.db == null) { baseEnv.put(d.systemCode+"_ONLINE_PORT", Integer.toString(d.localOnlinePort)); continue; }
            DbBinding b=d.db; String p=d.systemCode;
            String runtimePassword=localSecret(b.runtimeSecretEnv);
            String url,driver;
            switch (b.vendor) {
                case "postgresql" -> { url="jdbc:postgresql://"+b.host+":"+b.port+"/"+b.databaseName+"?currentSchema="+b.schemaName; driver="org.postgresql.Driver"; }
                case "mariadb" -> { url="jdbc:mariadb://"+b.host+":"+b.port+"/"+b.databaseName; driver="org.mariadb.jdbc.Driver"; }
                case "oracle" -> { url="jdbc:oracle:thin:@//"+b.host+":"+b.port+"/"+b.serviceName; driver="oracle.jdbc.OracleDriver"; }
                default -> throw new IllegalStateException();
            }
            lines.add(p+"_DB_VENDOR="+b.vendor);
            lines.add(p+"_DATASOURCE_URL="+url);
            lines.add(p+"_DATASOURCE_DRIVER="+driver);
            lines.add(p+"_DATASOURCE_USERNAME="+b.runtimeUser);
            lines.add(p+"_DATASOURCE_PASSWORD=<secret:"+b.runtimeSecretEnv+">");
            baseEnv.put(p+"_DB_VENDOR",b.vendor); baseEnv.put(p+"_DATASOURCE_URL",url); baseEnv.put(p+"_DATASOURCE_DRIVER",driver);
            baseEnv.put(p+"_DATASOURCE_USERNAME",b.runtimeUser); baseEnv.put(p+"_DATASOURCE_PASSWORD",runtimePassword);
            baseEnv.put(p+"_ONLINE_PORT", Integer.toString(d.localOnlinePort));
        }
        return lines;
    }

    private void runWorkspaceVerification(boolean full) throws Exception {
        Path gradlew = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
        if (!Files.isRegularFile(gradlew)) throw new IllegalStateException("Gradle wrapper missing");
        if (!isWindows()) gradlew.toFile().setExecutable(true);
        List<String> command = new ArrayList<>(); command.add(gradlew.toString()); command.add("cpfVerify"); command.add("--no-daemon");
        if (!full) command.add("--max-workers=2");
        // Domain build 는 DB Vendor 를 fail-closed 로 요구한다. bootstrap 은 방금 Domain DB Profile 로
        // Vendor 를 확정했으므로 그 값을 그대로 넘긴다. 사용자가 -PcpfDbVendor 를 직접 붙여야만
        // cpf bootstrap 이 통과하는 구조는 Golden Path 가 아니다.
        Map<String,String> verifyEnv = new LinkedHashMap<>(baseEnv);
        Set<String> vendors = new TreeSet<>();
        for (Domain d : domains) if (d.db != null) vendors.add(d.db.vendor);
        if (vendors.size() == 1) {
            verifyEnv.put("CPF_DB_VENDOR", vendors.iterator().next());
        } else if (vendors.size() > 1) {
            // 하나의 Gradle 실행은 하나의 Vendor 만 표현할 수 있다. 임의로 고르지 않는다.
            throw new IllegalStateException(
                    "workspace domains declare multiple DB vendors; run cpf verify per vendor: " + vendors);
        }
        runChecked(command, verifyEnv, Math.max(timeoutSeconds, 600), null, true);
        pass("10", "Build/Test", full ? "FULL PASS" : "FAST PASS");
    }

    /**
     * Generated Domain Online Runtime 을 기동한다.
     *
     * <p>증상 근거: {@code cpf bootstrap --run} 은 {@code gradlew :online:bootRun} 으로 Runtime 을
     * 띄우고 그 wrapper 의 pid 를 상태 파일에 적었다. wrapper 가 종료하면 상태 파일의 pid 는 이미
     * 죽었거나 재사용된 프로세스를 가리키므로 {@code cpf stop} 이 실제 Runtime 을 멈추지 못하고
     * "refusing to stop reused/non-CPF pid" 로 끝난다. 그 다음 기동은 포트 충돌로 막힌다.</p>
     *
     * <p>Runtime Lifecycle(runtimeStart)과 같은 모델로 맞춘다. 먼저 실행물을 만들고 그 실행물을
     * 직접 띄운다. 그러면 상태 파일의 pid 가 곧 Runtime 이다.</p>
     *
     * <p>되돌리면 재발할 증상: bootstrap 으로 띄운 Domain Runtime 이 stop 되지 않아 반복 기동이 막힌다.</p>
     */
    private void startRuntimes() throws Exception {
        Path runDir = logDir.resolve("runtime"); Files.createDirectories(runDir);
        Path state = root.resolve("build/cpf-bootstrap/current-runtime.properties");
        if (Files.exists(state)) throw new IllegalStateException("runtime state already exists; run cpf-stop before starting another local runtime set: " + root.relativize(state));
        Properties running = new Properties();
        running.setProperty("workspace", root.toString());
        running.setProperty("startedAt", Instant.now().toString());
        Path gradlew = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
        for (Domain d : domains) {
            Path runnable = buildDomainRuntimeJar(gradlew, d);
            ProcessBuilder pb = new ProcessBuilder(javaExecutable(), "-jar", runnable.toString());
            pb.directory(root.toFile());
            pb.environment().putAll(baseEnv);
            pb.environment().put("SPRING_PROFILES_ACTIVE", "local");
            pb.environment().put(d.systemCode + "_ONLINE_PORT", Integer.toString(d.localOnlinePort));
            // 한 Host 에서 Domain 을 여러 개 띄운다. instanceId 가 겹치면 두 번째 Runtime 이 Runtime
            // Control fence 에 걸려 기동하지 못한다. Domain 별로 안정적이고 유일한 값을 준다.
            pb.environment().put("CPF_RUNTIME_INSTANCE_ID", runtimeHostName() + "-" + d.systemCode);
            pb.redirectErrorStream(true);
            Path runtimeLog = runDir.resolve(d.systemCode + "-online.log");
            pb.redirectOutput(runtimeLog.toFile());
            // 부모(launcher/pwsh/CI runner)가 끝날 때 표준 입력 종료가 Runtime 까지 전달되면 안 된다.
            pb.redirectInput(new File(isWindows() ? "NUL" : "/dev/null"));
            Process process = pb.start();
            running.setProperty(d.systemCode + ".pid", Long.toString(process.pid()));
            running.setProperty(d.systemCode + ".port", Integer.toString(d.localOnlinePort));
            running.setProperty(d.systemCode + ".log", runtimeLog.toString());
            storePropertiesAtomic(state, running, "CPF local runtime process state. Stop uses only this current state.");
            waitForRuntimeHealth(d, process, runtimeLog);
        }
        pass("11", "Runtime", "started=" + domains.size() + " state=" + root.relativize(state));
    }

    /**
     * Generated Domain Online 의 실행물을 만들어 돌려준다. 실행물을 직접 띄워야 상태 파일의 pid 가
     * 곧 Runtime 이 된다({@link #startRuntimes()} 의 증상 근거 참고).
     */
    private Path buildDomainRuntimeJar(Path gradlew, Domain domain) throws Exception {
        Map<String,String> buildEnv = new LinkedHashMap<>(baseEnv);
        if (domain.db != null) buildEnv.put("CPF_DB_VENDOR", domain.db.vendor);
        runChecked(List.of(gradlew.toString(), "-p", domain.project.toString(), ":online:bootJar",
                        "--no-daemon", "--max-workers=2"),
                buildEnv, Math.max(timeoutSeconds, 1800), null, false);
        Path libs = domain.project.resolve("online/build/libs");
        if (!Files.isDirectory(libs)) throw new IllegalStateException("domain runtime build output is missing: " + libs);
        try (var files = Files.list(libs)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".jar"))
                    // plain jar 는 실행물이 아니다. Spring Boot 실행 jar 만 고른다.
                    .filter(file -> !file.getFileName().toString().endsWith("-plain.jar"))
                    .max(java.util.Comparator.comparingLong(file -> file.toFile().lastModified()))
                    .orElseThrow(() -> new IllegalStateException("runnable domain runtime jar is missing: " + libs));
        }
    }

    private void waitForRuntimeHealth(Domain domain, Process process, Path runtimeLog) throws Exception {
        URI health = URI.create("http://127.0.0.1:" + domain.localOnlinePort + "/actuator/health");
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        long started = System.nanoTime();
        while (Duration.ofNanos(System.nanoTime() - started).toSeconds() < timeoutSeconds) {
            if (!process.isAlive()) throw new IllegalStateException("runtime exited before READY: systemCode=" + domain.systemCode + " log=" + root.relativize(runtimeLog));
            try {
                HttpRequest req = HttpRequest.newBuilder(health).timeout(Duration.ofSeconds(5)).GET().build();
                HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 && response.body().contains("\"status\"") && response.body().toUpperCase(Locale.ROOT).contains("UP")) {
                    verifyOperationManifestPresent(domain);
                    pass("11", "Runtime Health", domain.systemCode + " port=" + domain.localOnlinePort + " health=UP operationManifest=PASS");
                    return;
                }
            } catch (IOException ignored) { }
            long elapsed = Duration.ofNanos(System.nanoTime() - started).toSeconds();
            progress("11", "Runtime Health", "systemCode=" + domain.systemCode + " elapsed=" + elapsed + "s/" + timeoutSeconds + "s");
            Thread.sleep(3000);
        }
        throw new IllegalStateException("runtime health timeout systemCode=" + domain.systemCode + " port=" + domain.localOnlinePort + " log=" + root.relativize(runtimeLog));
    }

    private void verifyOperationManifestPresent(Domain domain) throws Exception {
        Path manifest = domain.project.resolve("online/build/generated/cpf-operation-manifest/META-INF/cpf/business-operation-manifest.json");
        if (!Files.isRegularFile(manifest)) throw new IllegalStateException("generated domain operation manifest missing: " + manifest);
        String text = Files.readString(manifest, StandardCharsets.UTF_8);
        if (!text.contains(domain.systemCode) || !text.contains("operationId")) throw new IllegalStateException("generated domain operation manifest is not canonical for " + domain.systemCode);
    }

    private int stop(Map<String,String> options) throws Exception {
        Path compose = root.resolve("deploy/local/compose.yaml");
        Path state = root.resolve("build/cpf-bootstrap/current-runtime.properties");
        if (Files.isRegularFile(state)) {
            Properties p = new Properties();
            try (Reader r = Files.newBufferedReader(state, StandardCharsets.UTF_8)) { p.load(r); }
            if (!root.toString().equals(p.getProperty("workspace"))) throw new IllegalStateException("runtime state workspace mismatch: " + state);
            for (String key : p.stringPropertyNames()) {
                if (!key.endsWith(".pid")) continue;
                long pid = Long.parseLong(p.getProperty(key));
                ProcessHandle.of(pid).ifPresent(handle -> {
                    String command = handle.info().command().orElse("").toLowerCase(Locale.ROOT);
                    if (command.contains("java") || command.contains("gradle")) handle.destroy();
                    else throw new IllegalStateException("refusing to stop reused/non-CPF pid=" + pid + " command=" + command);
                });
            }
            Files.delete(state);
        }
        if (Files.isRegularFile(compose)) runChecked(List.of("docker","compose","-f",compose.toString(),"stop"), baseEnv, timeoutSeconds, null, true);
        ready("CPF LOCAL DEVELOPMENT STOPPED (volumes preserved)"); return 0;
    }

    private static void storePropertiesAtomic(Path target, Properties value, String comment) throws Exception {
        Files.createDirectories(target.getParent());
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        try (Writer w = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) { value.store(w, comment); }
        try { Files.move(temp, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
        catch (AtomicMoveNotSupportedException ex) { Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING); }
    }

    /**
     * 공개 배포본의 Runtime Lifecycle 진입점.
     *
     * <p>증상 근거: Fresh Consumer 가 공개 launcher 로 ADM 을 띄우면
     * {@code CPF-CLI-ENGINE-MISSING cpf-tools/runtime/tools/cpf_local_runtime.py} 로 죽었다.
     * 공개 배포본은 {@code cpf-tools/} 를 싣지 않으므로 내부 엔진 위임은 공개 사용자에게 영구히
     * 실패한다. 그래서 실행 진입점을 공개 CLI 안에 둔다.</p>
     *
     * <p>되돌리면 재발할 증상: 공개 사용자가 Clone 과 bootstrap 까지 마치고도 어떤 Runtime 도
     * 기동하지 못한다.</p>
     */
    private int runtimeLifecycle(Map<String,String> options) throws Exception {
        String action = options.getOrDefault("action", "").trim();
        String requested = options.getOrDefault("target", "").trim();
        if (action.isEmpty()) throw new IllegalArgumentException("runtime action is required");
        if (requested.isEmpty()) throw new IllegalArgumentException("runtime --target is required");
        CpfRuntimeTargets.Target target = null;
        for (CpfRuntimeTargets.Target row : CpfRuntimeTargets.resolveAll(root)) {
            if (row.name().equals(requested)) { target = row; break; }
        }
        if (target == null) throw new IllegalStateException("unknown runtime target=" + requested);
        final CpfRuntimeTargets.Target resolved = target;
        return switch (action) {
            case "start" -> runtimeStart(resolved);
            case "stop" -> runtimeStop(resolved, true);
            case "status" -> runtimeStatus(resolved);
            case "health" -> runtimeHealth(resolved);
            case "log" -> runtimeLog(resolved);
            case "restart" -> { runtimeStop(resolved, false); yield runtimeStart(resolved); }
            default -> throw new IllegalArgumentException("unsupported runtime action=" + action);
        };
    }

    private Path runtimeStateFile(CpfRuntimeTargets.Target target) {
        return root.resolve("build/cpf-runtime").resolve(target.name() + ".properties");
    }

    private Path runtimeLogFile(CpfRuntimeTargets.Target target) {
        return root.resolve("build/cpf-runtime/logs").resolve(target.name() + ".log");
    }

    private Properties runtimeState(CpfRuntimeTargets.Target target) throws Exception {
        Properties state = new Properties();
        Path file = runtimeStateFile(target);
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) { state.load(reader); }
        }
        return state;
    }

    /** 상태 파일의 pid 가 아직 살아 있을 때만 실행 중으로 인정한다. */
    private Optional<ProcessHandle> runtimeProcess(Properties state) {
        String pid = state.getProperty("pid", "").trim();
        if (pid.isEmpty()) return Optional.empty();
        return ProcessHandle.of(Long.parseLong(pid)).filter(handle -> handle.isAlive());
    }

    /**
     * Runtime 실행 환경을 준비한다.
     *
     * <p>Runtime 은 bootstrap 이 만든 DB 에 접속해야 하므로 같은 Binding 계산을 재사용한다.
     * 여기서 다시 계산하지 않으면 Runtime 이 어떤 DB 를 볼지 알 수 없다.</p>
     */
    private void prepareRuntimeEnvironment() throws Exception {
        if (runtimeEnvironmentReady) return;
        prepareLocalSecret();
        resolveBinaryRepository();
        discoverDomains();
        bindDomainRuntimeEnvironment();
        runtimeEnvironmentReady = true;
    }

    /** Port 정본은 catalog 다. Generated Domain 만 Bootstrap 이 배정한 Port 를 쓴다. */
    private int runtimePort(CpfRuntimeTargets.Target target) {
        if (target.port() > 0) return target.port();
        for (Domain d : domains) {
            if (target.name().equals(d.name + "-online")) return d.localOnlinePort;
        }
        return 0;
    }

    private Path runtimeProjectDir(CpfRuntimeTargets.Target target) {
        String owner = target.owner();
        int slash = owner.indexOf(47);
        return root.resolve(slash < 0 ? owner : owner.substring(0, slash));
    }

    /**
     * Binary 로 배포된 Runtime 의 실행물을 찾는다.
     *
     * <p>좌표는 catalog 의 artifactId 를 그대로 쓴다. 이름을 추론하면 Runtime 이 하나 늘 때마다
     * Launcher 가 조용히 틀린다.</p>
     */
    private Path publishedRuntimeJar(CpfRuntimeTargets.Target target) throws Exception {
        String artifactId = target.artifactId();
        if (artifactId.isBlank()) throw new IllegalStateException("runtime target declares no artifactId: " + target.name());
        String version = firstNonBlank(baseEnv.get("CPF_VERSION"));
        if (version.isEmpty()) throw new IllegalStateException("CPF_VERSION is not resolved");
        String repository = firstNonBlank(baseEnv.get("CPF_MAVEN_REPOSITORY_URL"));
        if (repository.isEmpty()) throw new IllegalStateException("Binary Repository is not resolved");
        URI uri = URI.create(repository.endsWith("/") ? repository : repository + "/");
        if (!"file".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalStateException("binary runtime requires a local Binary Repository: " + safeUri(repository));
        }
        Path jar = Path.of(uri).resolve("com/cpf/runtime").resolve(artifactId).resolve(version)
                .resolve(artifactId + "-" + version + ".jar");
        if (!Files.isRegularFile(jar)) throw new IllegalStateException("published runtime jar is missing: " + jar);
        return jar;
    }

    private List<String> runtimeCommand(CpfRuntimeTargets.Target target) throws Exception {
        Path jar = "binary".equals(target.provision())
                ? publishedRuntimeJar(target)
                : buildSourceRuntimeJar(target);
        return List.of(javaExecutable(), "-jar", jar.toString());
    }

    /**
     * Source 로 배포된 Runtime 의 실행물을 빌드해 돌려준다.
     *
     * <p>증상 근거: 이전에는 {@code gradlew bootRun} 으로 띄웠다. 그 경우 Launcher 가 기록하는 pid 는
     * Gradle wrapper 프로세스이고, wrapper 가 끝나면 상태 파일의 pid 는 이미 죽은(또는 재사용된)
     * 프로세스를 가리킨다. 그래서 {@code cpf runtime stop} 이 실제 Runtime 을 멈추지 못하고
     * "refusing to stop reused/non-CPF pid" 로 끝나며, 다음 기동은 포트 충돌로 막힌다.</p>
     *
     * <p>Binary Runtime 과 같은 모델로 맞춘다. 먼저 실행물을 만들고, 그 실행물을 직접 띄운다.
     * 그러면 상태 파일의 pid 가 곧 Runtime 이다.</p>
     */
    private Path buildSourceRuntimeJar(CpfRuntimeTargets.Target target) throws Exception {
        Path project = runtimeProjectDir(target);
        Path gradlew = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
        if (!Files.isRegularFile(gradlew)) throw new IllegalStateException("gradle wrapper is missing: " + gradlew);
        String owner = target.owner();
        int slash = owner.indexOf(47);
        String task = slash < 0 ? "bootJar" : ":" + owner.substring(slash + 1) + ":bootJar";
        step("10", "Runtime Build", target.name() + " " + task);
        runChecked(List.of(gradlew.toString(), "-p", project.toString(), task, "--no-daemon", "--max-workers=2"),
                baseEnv, Math.max(timeoutSeconds, 1800), null, false);
        Path libs = root.resolve(owner.replace('/', java.io.File.separatorChar)).resolve("build/libs");
        if (!Files.isDirectory(libs)) throw new IllegalStateException("runtime build output is missing: " + root.relativize(libs));
        try (var files = Files.list(libs)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().endsWith(".jar"))
                    // plain jar 는 실행물이 아니다. Spring Boot 실행 jar 만 고른다.
                    .filter(file -> !file.getFileName().toString().endsWith("-plain.jar"))
                    .max(java.util.Comparator.comparingLong(file -> file.toFile().lastModified()))
                    .orElseThrow(() -> new IllegalStateException("runnable runtime jar is missing: " + root.relativize(libs)));
        }
    }


    /** Runtime instanceId 의 Host 부분. 확인할 수 없으면 고정 대체값을 쓴다. */
    private static String runtimeHostName() {
        try {
            String host = java.net.InetAddress.getLocalHost().getHostName();
            return host == null || host.isBlank() ? "localhost" : host.trim();
        } catch (Exception unavailable) {
            return "localhost";
        }
    }

    private int runtimeStart(CpfRuntimeTargets.Target target) throws Exception {
        Optional<ProcessHandle> running = runtimeProcess(runtimeState(target));
        if (running.isPresent()) {
            System.out.println("CPF_RUNTIME=ALREADY_RUNNING target=" + target.name() + " pid=" + running.get().pid());
            return 0;
        }
        prepareRuntimeEnvironment();
        int port = runtimePort(target);
        if ("http-server".equals(target.capability()) && port <= 0) {
            throw new IllegalStateException("runtime target has no resolvable port: " + target.name());
        }
        Path logFile = runtimeLogFile(target);
        Files.createDirectories(logFile.getParent());
        ProcessBuilder builder = new ProcessBuilder(runtimeCommand(target));
        builder.directory(root.toFile());
        builder.environment().putAll(baseEnv);
        builder.environment().put("SPRING_PROFILES_ACTIVE", envOrDefault("SPRING_PROFILES_ACTIVE", "local"));
        if (port > 0 && !target.portEnv().isBlank()) {
            builder.environment().put(target.portEnv(), Integer.toString(port));
        }
        // 한 Host 에서 여러 Runtime 을 띄우므로 instanceId 가 겹치면 두 번째 Runtime 이 Runtime Control
        // fence 에 걸려 기동하지 못한다. Target 별로 안정적이고 유일한 값을 준다(재기동 시에도 같은 값).
        builder.environment().put("CPF_RUNTIME_INSTANCE_ID",
                envOrDefault("CPF_RUNTIME_INSTANCE_ID", runtimeHostName() + "-" + target.name()));
        builder.redirectErrorStream(true);
        builder.redirectOutput(logFile.toFile());
        // Runtime 은 이 명령이 끝난 뒤에도 계속 떠 있어야 한다. 표준 입력을 부모에게서 물려받으면
        // 부모(launcher/pwsh/CI runner)가 끝날 때 stream 종료가 Runtime 까지 전달될 수 있다.
        builder.redirectInput(new File(isWindows() ? "NUL" : "/dev/null"));
        Process process = builder.start();
        Properties state = new Properties();
        state.setProperty("workspace", root.toString());
        state.setProperty("target", target.name());
        state.setProperty("pid", Long.toString(process.pid()));
        state.setProperty("port", Integer.toString(port));
        state.setProperty("log", logFile.toString());
        state.setProperty("startedAt", Instant.now().toString());
        storePropertiesAtomic(runtimeStateFile(target), state, "CPF public runtime process state");
        awaitRuntimeReady(target, process, port, logFile);
        System.out.println("CPF_RUNTIME=STARTED target=" + target.name() + " port=" + port + " pid=" + process.pid());
        return 0;
    }

    /**
     * 기동 완료 판정.
     *
     * <p>catalog 의 lifecycleCapabilities 가 정한 대로 http-server 만 HTTP 200 으로 판정한다.
     * worker/one-shot 을 같은 방식으로 판정하면 거짓 PASS 가 된다.</p>
     */
    private void awaitRuntimeReady(CpfRuntimeTargets.Target target, Process process, int port, Path logFile) throws Exception {
        if (!"http-server".equals(target.capability())) return;
        if (target.healthPath().isBlank()) {
            throw new IllegalStateException("runtime target declares no healthPath: " + target.name());
        }
        int limit = workspaceInt("cpf.runtime.ready-timeout-seconds");
        URI health = URI.create("http://127.0.0.1:" + port + target.healthPath());
        Instant deadline = Instant.now().plusSeconds(limit);
        while (Instant.now().isBefore(deadline)) {
            if (!process.isAlive()) {
                throw new IllegalStateException("runtime exited before becoming ready: " + target.name()
                        + " log=" + root.relativize(logFile));
            }
            if (probeHealth(health) == 200) return;
            Thread.sleep(2000);
        }
        throw new IllegalStateException("runtime did not become ready within " + limit + "s: " + target.name()
                + " log=" + root.relativize(logFile));
    }

    private int probeHealth(URI uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(5)).GET().build();
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
        } catch (Exception ignored) {
            return -1;
        }
    }

    private int runtimeStop(CpfRuntimeTargets.Target target, boolean report) throws Exception {
        Properties state = runtimeState(target);
        Optional<ProcessHandle> handle = runtimeProcess(state);
        if (handle.isPresent()) {
            ProcessHandle process = handle.get();
            String command = process.info().command().orElse("").toLowerCase(Locale.ROOT);
            if (!command.contains("java") && !command.contains("gradle")) {
                throw new IllegalStateException("refusing to stop reused/non-CPF pid=" + process.pid());
            }
            process.descendants().forEach(ProcessHandle::destroy);
            process.destroy();
            Instant deadline = Instant.now().plusSeconds(workspaceInt("cpf.runtime.stop-timeout-seconds"));
            while (process.isAlive() && Instant.now().isBefore(deadline)) Thread.sleep(200);
            if (process.isAlive()) {
                process.descendants().forEach(ProcessHandle::destroyForcibly);
                process.destroyForcibly();
            }
        }
        Files.deleteIfExists(runtimeStateFile(target));
        if (report) System.out.println("CPF_RUNTIME=STOPPED target=" + target.name());
        return 0;
    }

    private int runtimeStatus(CpfRuntimeTargets.Target target) throws Exception {
        Properties state = runtimeState(target);
        Optional<ProcessHandle> handle = runtimeProcess(state);
        if (handle.isEmpty()) {
            System.out.println("CPF_RUNTIME_STATUS=STOPPED target=" + target.name());
            return 0;
        }
        int port = Integer.parseInt(state.getProperty("port", "0"));
        System.out.println("CPF_RUNTIME_STATUS=RUNNING target=" + target.name()
                + " pid=" + handle.get().pid() + " port=" + port);
        if ("http-server".equals(target.capability()) && port > 0 && !target.healthPath().isBlank()) {
            System.out.println("health=" + probeHealth(URI.create("http://127.0.0.1:" + port + target.healthPath())));
        }
        return 0;
    }

    private int runtimeHealth(CpfRuntimeTargets.Target target) throws Exception {
        if (!"http-server".equals(target.capability())) {
            // catalog 계약상 단순 HTTP 200 은 이 capability 의 준비 완료가 아니다. 거짓 PASS 를 만들지 않는다.
            System.out.println("CPF_RUNTIME_HEALTH=CONTRACT_NOT_HTTP target=" + target.name()
                    + " capability=" + target.capability());
            return 0;
        }
        Properties state = runtimeState(target);
        int port = Integer.parseInt(state.getProperty("port", Integer.toString(target.port())));
        if (port <= 0 || target.healthPath().isBlank()) {
            throw new IllegalStateException("runtime target has no health endpoint: " + target.name());
        }
        int code = probeHealth(URI.create("http://127.0.0.1:" + port + target.healthPath()));
        System.out.println("CPF_RUNTIME_HEALTH=" + (code == 200 ? "PASS" : "FAIL")
                + " target=" + target.name() + " status=" + code);
        return code == 200 ? 0 : 1;
    }

    private int runtimeLog(CpfRuntimeTargets.Target target) throws Exception {
        Path logFile = runtimeLogFile(target);
        if (!Files.isRegularFile(logFile)) {
            System.out.println("CPF_RUNTIME_LOG=ABSENT target=" + target.name());
            return 0;
        }
        List<String> lines = Files.readAllLines(logFile, StandardCharsets.UTF_8);
        int limit = workspaceInt("cpf.runtime.log-lines");
        for (String line : lines.subList(Math.max(0, lines.size() - limit), lines.size())) System.out.println(line);
        return 0;
    }

    /** 운영 시간값의 정본은 config/cpf-workspace.properties 다. 값이 없으면 기본값으로 대체하지 않고 멈춘다. */
    private int workspaceInt(String key) {
        String value = workspace.getProperty(key, "").trim();
        if (value.isEmpty()) throw new IllegalStateException("config/cpf-workspace.properties is missing " + key);
        return Integer.parseInt(value);
    }

    private int reset(Map<String,String> options) throws Exception {
        if (!options.containsKey("confirm-local-reset")) throw new IllegalStateException("reset requires --confirm-local-reset; volumes/data are destructive");
        Path compose = root.resolve("deploy/local/compose.yaml");
        if (!Files.isRegularFile(compose)) throw new IllegalStateException("local compose asset missing");
        runChecked(List.of("docker","compose","-f",compose.toString(),"down","-v"), baseEnv, timeoutSeconds, null, true);
        ready("CPF LOCAL DEVELOPMENT RESET COMPLETED"); return 0;
    }

    private void requireCommand(String label, List<String> command, boolean sensitive) throws Exception {
        ExecResult result = run(command, Map.of(), 30, null, sensitive, true);
        if (result.exit != 0) throw new IllegalStateException(label + " prerequisite unavailable. Install/enable it, then re-run cpf-bootstrap.");
    }

    private ExecResult run(List<String> command, Map<String,String> env, int timeout, String input, boolean sensitive, boolean capture) throws Exception {
        List<String> safeCommand = sensitive ? command.stream().map(x -> x.contains(localDbPassword == null ? "\u0000" : localDbPassword) ? x.replace(localDbPassword,"***") : x).toList() : command;
        logLine("RUN " + String.join(" ", safeCommand));
        ProcessBuilder pb = new ProcessBuilder(command); pb.directory(root.toFile()); pb.redirectErrorStream(true); pb.environment().putAll(env);
        Process process = pb.start();
        if (input != null) { try (Writer w = new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8)) { w.write(input); } }
        else process.getOutputStream().close();
        StringBuilder out = new StringBuilder();
        Thread reader = Thread.ofPlatform().start(() -> {
            try (BufferedReader br = process.inputReader(StandardCharsets.UTF_8)) {
                for (String line; (line=br.readLine()) != null;) {
                    String safe = sensitive && localDbPassword != null ? line.replace(localDbPassword,"***") : line;
                    synchronized(out) { out.append(safe).append('\n'); }
                    logLine(safe);
                    if (!capture) System.out.println(safe);
                }
            } catch (IOException ignored) {}
        });
        boolean done = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!done) { process.destroyForcibly(); reader.join(3000); throw new IllegalStateException("command timeout=" + timeout + "s: " + String.join(" ",safeCommand)); }
        reader.join(3000);
        return new ExecResult(process.exitValue(), out.toString());
    }

    private void runChecked(List<String> command, Map<String,String> env, int timeout, String input, boolean sensitive) throws Exception {
        ExecResult r = run(command,env,timeout,input,sensitive,false);
        if (r.exit != 0) throw new IllegalStateException("command failed exit="+r.exit+": "+String.join(" ",command.stream().map(x -> sensitive && localDbPassword != null ? x.replace(localDbPassword,"***") : x).toList()));
    }

    private static List<Path> listSql(Path dir) throws IOException { if (!Files.isDirectory(dir)) return List.of(); try (var s=Files.list(dir)) { return s.filter(p->p.getFileName().toString().endsWith(".sql")).sorted().toList(); } }
    private static <T> List<T> concat(List<T> a,List<T> b){ List<T> x=new ArrayList<>(a);x.addAll(b);return x; }
    private static String sha256(Path path) throws Exception { MessageDigest md=MessageDigest.getInstance("SHA-256"); md.update(Files.readAllBytes(path)); return HexFormat.of().formatHex(md.digest()); }
    private static String normalizeDb(String db) { String v=db.trim().toLowerCase(Locale.ROOT); if (!Set.of("postgresql","mariadb","oracle").contains(v)) throw new IllegalArgumentException("unsupported local DB="+db); return v; }
    private String envOrProperty(String env,String key,String fallback){ return firstNonBlank(System.getenv(env),workspace.getProperty(key,""),fallback); }
    private static String envOrDefault(String env,String fallback){ return firstNonBlank(System.getenv(env),fallback); }
    private static String firstNonBlank(String... values){ for(String v:values) if(v!=null&&!v.isBlank()) return v.trim(); return ""; }
    private static boolean isWindows(){ return System.getProperty("os.name","").toLowerCase(Locale.ROOT).contains("win"); }

    private static String javaExecutable(){
        Path java=Path.of(System.getProperty("java.home"),"bin",isWindows()?"java.exe":"java");
        if(!Files.isRegularFile(java)) throw new IllegalStateException("Java executable missing: "+java);
        return java.toString();
    }

    private static Path locateRoot(){ Path p=Path.of(System.getProperty("user.dir")).toAbsolutePath(); while(p!=null){ if(Files.isRegularFile(p.resolve("settings.gradle"))&&Files.isDirectory(p.resolve("config"))) return p; p=p.getParent(); } throw new IllegalStateException("CPF Public Workspace root not found"); }
    private static String readDefaultTimeout(Path root){ Properties p=new Properties(); Path f=root.resolve("config/cpf-workspace.properties"); try{ if(Files.isRegularFile(f)) try(Reader r=Files.newBufferedReader(f)){p.load(r);} }catch(Exception ignored){} return p.getProperty("cpf.bootstrap.timeout-seconds","300"); }
    private static Map<String,String> parseArgs(String[] args){ Map<String,String> m=new LinkedHashMap<>(); String command="bootstrap"; List<String> positional=new ArrayList<>(); for(int i=0;i<args.length;i++){ String a=args[i]; if(!a.startsWith("--")&&!a.startsWith("-")){positional.add(a);continue;} if(a.equals("--db")&&i+1<args.length)m.put("db",args[++i]); else if(a.equals("--timeout")&&i+1<args.length)m.put("timeout",args[++i]); else if(a.equals("--run"))m.put("run","true"); else if(a.equals("--full"))m.put("full","true"); else if(a.equals("--confirm-local-reset"))m.put("confirm-local-reset","true"); else if(a.equals("--target")&&i+1<args.length)m.put("target",args[++i]); else throw new IllegalArgumentException("unknown option="+a);} // runtime 처럼 "명령 + 동작" 두 단어를 쓰는 진입점이 있으므로 위치 인자를 순서대로 해석한다.
        if(!positional.isEmpty())command=positional.get(0); if(positional.size()>1)m.put("action",positional.get(1)); if(positional.size()>2)throw new IllegalArgumentException("unexpected argument="+positional.get(2)); m.put("command",command); return m; }
    private static String safeUri(String uri){ try{ URI u=URI.create(uri); return new URI(u.getScheme(),null,u.getHost(),u.getPort(),u.getPath(),null,null).toString(); }catch(Exception e){ return "<configured>"; } }
    private static String sanitize(String s){ if(s==null)return "unknown error"; return s.replaceAll("(?i)(password|token|secret)=\\S+","$1=***"); }
    private void step(String n,String label,String detail){ out("["+n+"] "+label+" .... "+detail); }
    private void progress(String n,String label,String detail){ out("["+n+"] "+label+" .... "+detail); }
    private void pass(String n,String label,String detail){ out("["+n+"] "+label+" .... PASS "+detail); }
    private void skip(String n,String label,String detail){ out("["+n+"] "+label+" .... SKIP "+detail); }
    private void ready(String text){ out("[CPF][BOOTSTRAP] "+text); out("[CPF][BOOTSTRAP] log="+root.relativize(logDir)); }
    private void out(String s){ System.out.println(s); logLine(s); }
    private synchronized void logLine(String s){ log.println(Instant.now()+" "+sanitize(s)); log.flush(); }
    private void logException(Throwable e){ logLine("FAIL "+e); e.printStackTrace(log); log.flush(); }
    private void close(){ log.close(); }
}
