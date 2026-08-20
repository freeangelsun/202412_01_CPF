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
    private static final Pattern DOMAIN_BLOCK = Pattern.compile("(?ms)^domain:\\s*\\n((?:^[ ]{2}.+\\n?)*)");
    private static final Pattern DOMAIN_NAME = Pattern.compile("(?m)^\\s{2}name:\\s*([a-z][a-z0-9-]*)\\s*$");
    private static final Pattern SYSTEM_CODE = Pattern.compile("(?m)^\\s{2}systemCode:\\s*([A-Z][A-Z0-9]{2})\\s*$");
    private static final Pattern FEATURE_VALUE = Pattern.compile("(?m)^\\s{2}(persistence|cache|messaging):\\s*([a-zA-Z0-9-]+)\\s*$");
    private static final Pattern LOCAL_ONLINE_PORT = Pattern.compile("(?m)^\\s{2}localOnlinePort:\\s*(\\d+)\\s*$");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final Path root;
    private final Path logDir;
    private final PrintWriter log;
    private final int timeoutSeconds;
    private final Map<String,String> baseEnv = new HashMap<>(System.getenv());
    private final Properties workspace = new Properties();
    private final List<Domain> domains = new ArrayList<>();
    private String defaultDbVendor;
    private String localDbPassword;

    private record DbBinding(String vendor, String host, int port, String databaseName, String serviceName, String schemaName,
                             String migrationUser, String runtimeUser, String migrationSecretEnv, String runtimeSecretEnv, Path profile) {}
    private record Domain(String name, String systemCode, Path definition, Path project, Map<String,String> features, int localOnlinePort, DbBinding db) {}
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
        if (!command.equals("bootstrap")) throw new IllegalArgumentException("unsupported command=" + command);

        checkPrerequisites();
        resolveBinaryRepository();
        defaultDbVendor = normalizeDb(options.getOrDefault("db", envOrProperty("CPF_LOCAL_DB", "cpf.default-db", "postgresql")));
        prepareLocalSecret();
        discoverDomains();
        prepareDatabase();
        prepareMiddleware();
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
        String version = envOrProperty("CPF_VERSION", "cpf.version", "").trim();
        String repo = firstNonBlank(System.getenv("CPF_MAVEN_REPOSITORY_URL"), System.getenv("CPF_ARTIFACT_REPOSITORY_URL"), workspace.getProperty("cpf.maven.repository.url", ""));
        if (repo.isBlank()) throw new IllegalStateException("CPF_MAVEN_REPOSITORY_URL is required for Public Workspace");
        if (version.isBlank()) throw new IllegalStateException("CPF_VERSION is required for Public Workspace");
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
        Path defs = root.resolve("domains");
        if (!Files.isDirectory(defs)) throw new IllegalStateException("public domain catalog missing: " + defs);
        Set<String> names = new HashSet<>(), codes = new HashSet<>();
        try (DirectoryStream<Path> children = Files.newDirectoryStream(defs)) {
            for (Path dir : children) {
                Path definition = dir.resolve("cpf-domain.yaml");
                if (!Files.isRegularFile(definition)) continue;
                String text = Files.readString(definition, StandardCharsets.UTF_8);
                Matcher block = DOMAIN_BLOCK.matcher(text);
                if (!block.find()) throw new IllegalStateException("domain block missing: " + definition);
                String body = block.group(1);
                Matcher nm = DOMAIN_NAME.matcher(body), cm = SYSTEM_CODE.matcher(body);
                if (!nm.find() || !cm.find()) throw new IllegalStateException("invalid domain name/systemCode: " + definition);
                String name = nm.group(1), code = cm.group(1);
                if (!names.add(name)) throw new IllegalStateException("duplicate domain name=" + name);
                if (!codes.add(code)) throw new IllegalStateException("duplicate systemCode=" + code);
                Path project = root.resolve("cpf-" + name);
                if (!Files.isRegularFile(project.resolve("settings.gradle"))) throw new IllegalStateException("domain dependency requires physical project: " + name + " -> " + project);
                Map<String,String> features = new HashMap<>();
                Matcher fm = FEATURE_VALUE.matcher(text);
                while (fm.find()) features.put(fm.group(1), fm.group(2));
                Matcher pm = LOCAL_ONLINE_PORT.matcher(text);
                if (!pm.find()) throw new IllegalStateException("runtime.localOnlinePort missing: " + definition);
                int localOnlinePort = Integer.parseInt(pm.group(1));
                if (localOnlinePort < 18080 || localOnlinePort > 18999) throw new IllegalStateException("runtime.localOnlinePort out of range: " + localOnlinePort + " file=" + definition);
                DbBinding db = features.getOrDefault("persistence", "none").equals("none") ? null : loadOrCreateDbBinding(name, code);
                domains.add(new Domain(name, code, definition, project, features, localOnlinePort, db));
            }
        }
        domains.sort(Comparator.comparing(Domain::name));
        if (domains.isEmpty()) throw new IllegalStateException("no generated/reference domain discovered");
        Set<Integer> ports = new HashSet<>();
        for (Domain d : domains) if (!ports.add(d.localOnlinePort)) throw new IllegalStateException("duplicate runtime.localOnlinePort=" + d.localOnlinePort);
        pass("05", "Workspace Discovery", "domains=" + domains.stream().map(d -> d.systemCode + ":" + d.name).toList());
    }

    private DbBinding loadOrCreateDbBinding(String name, String systemCode) throws Exception {
        Path profile = root.resolve("build/cpf-local").resolve(name).resolve("cpf-db-profile.local.json");
        if (!Files.isRegularFile(profile)) {
            Files.createDirectories(profile.getParent());
            String logical = systemCode.toLowerCase(Locale.ROOT) + "DB";
            int port = switch (defaultDbVendor) { case "postgresql" -> Integer.parseInt(envOrDefault("CPF_POSTGRES_PORT","5432")); case "mariadb" -> Integer.parseInt(envOrDefault("CPF_MARIADB_PORT","3306")); default -> Integer.parseInt(envOrDefault("CPF_ORACLE_PORT","1521")); };
            String migrationUser = "cpf_" + systemCode.toLowerCase(Locale.ROOT) + "_migration";
            String runtimeUser = "cpf_" + systemCode.toLowerCase(Locale.ROOT) + "_runtime";
            String migrationSecret = systemCode + "_DB_MIGRATION_PASSWORD";
            String runtimeSecret = systemCode + "_DB_RUNTIME_PASSWORD";
            String databaseField = defaultDbVendor.equals("oracle") ? "\"serviceName\": \"FREEPDB1\"" : "\"databaseName\": \"" + logical + "\"";
            String json = "{\n  \"profileVersion\": 2,\n  \"profileName\": \""+name+"-local\",\n  \"environment\": \"local\",\n  \"sourceControlled\": false,\n  \"database\": {\n    \"vendor\": \""+defaultDbVendor+"\",\n    \"host\": \"127.0.0.1\",\n    \"port\": "+port+",\n    "+databaseField+",\n    \"schemaName\": \""+logical+"\",\n    \"logicalDatabase\": \""+logical+"\",\n    \"migration\": {\"username\": \""+migrationUser+"\", \"password\": {\"env\": \""+migrationSecret+"\"}},\n    \"runtime\": {\"username\": \""+runtimeUser+"\", \"password\": {\"env\": \""+runtimeSecret+"\"}}\n  }\n}\n";
            Files.writeString(profile, json, StandardCharsets.UTF_8);
            progress("05", "Workspace Discovery", "generated local-only DB binding=" + root.relativize(profile) + " vendor=" + defaultDbVendor);
        }
        String text = Files.readString(profile, StandardCharsets.UTF_8);
        String vendor = normalizeDb(jsonString(text,"vendor"));
        String host = jsonString(text,"host");
        int port = jsonInt(text,"port");
        String databaseName = jsonStringOptional(text,"databaseName");
        String serviceName = jsonStringOptional(text,"serviceName");
        String schemaName = jsonString(text,"schemaName");
        List<String> users = jsonAllStrings(text,"username");
        List<String> secrets = jsonAllStrings(text,"env");
        if (users.size() < 2 || secrets.size() < 2) throw new IllegalStateException("DB profile requires distinct migration/runtime accounts and secret references: " + profile);
        if (users.get(0).equals(users.get(1))) throw new IllegalStateException("migration/runtime DB accounts must differ: " + profile);
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
        String setup = "SELECT format('CREATE ROLE %I LOGIN PASSWORD %L','"+migration+"','"+mp+"') WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='"+migration+"')\\gexec\n" +
                "SELECT format('CREATE ROLE %I LOGIN PASSWORD %L','"+runtime+"','"+rp+"') WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname='"+runtime+"')\\gexec\n" +
                "SELECT format('CREATE DATABASE %I OWNER %I','"+db+"','"+migration+"') WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname='"+db+"')\\gexec\n";
        runChecked(List.of("docker","exec","-i","cpf-public-postgresql","psql","-v","ON_ERROR_STOP=1","-U","postgres","-d","postgres"), baseEnv, 60, setup, true);
        runChecked(List.of("docker","exec","-i","-e","PGPASSWORD="+mp,"cpf-public-postgresql","psql","-v","ON_ERROR_STOP=1","-U",migration,"-d",db), Map.of(), 60,
                "CREATE SCHEMA IF NOT EXISTS \""+b.schemaName+"\" AUTHORIZATION \""+migration+"\"; CREATE TABLE IF NOT EXISTS cpf_bootstrap_schema_history(script_name VARCHAR(300) PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP); GRANT USAGE ON SCHEMA \""+b.schemaName+"\" TO \""+runtime+"\";", true);
        applyTrackedSql(d, db, migration, "cpf-public-postgresql", "postgresql", mp);
    }

    private void applyMariaDb(Domain d) throws Exception {
        DbBinding b=d.db; String db=b.databaseName; String migration=b.migrationUser; String runtime=b.runtimeUser;
        String mp=localSecret(b.migrationSecretEnv), rp=localSecret(b.runtimeSecretEnv);
        String setup = "CREATE DATABASE IF NOT EXISTS `"+db+"`; CREATE USER IF NOT EXISTS '"+migration+"'@'%' IDENTIFIED BY '"+mp+"'; CREATE USER IF NOT EXISTS '"+runtime+"'@'%' IDENTIFIED BY '"+rp+"'; GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,DROP,INDEX,REFERENCES ON `"+db+"`.* TO '"+migration+"'@'%'; GRANT SELECT,INSERT,UPDATE,DELETE,EXECUTE ON `"+db+"`.* TO '"+runtime+"'@'%'; FLUSH PRIVILEGES;";
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
                "CONNECT "+migration+"/\\\""+mp+"\\\"@"+service+"\nBEGIN EXECUTE IMMEDIATE 'CREATE TABLE cpf_bootstrap_schema_history (script_name VARCHAR2(300) PRIMARY KEY, checksum CHAR(64) NOT NULL, applied_at TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP NOT NULL)'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -955 THEN RAISE; END IF; END; /\nEXIT\n";
        runChecked(List.of("docker","exec","-i","cpf-public-oracle","sqlplus","-s","/","as","sysdba"), Map.of(), 90, setup, true);
        applyTrackedSql(d, service, migration, "cpf-public-oracle", "oracle", mp);
    }

    private void applyTrackedSql(Domain d, String db, String user, String container, String vendor, String password) throws Exception {
        Path base = root.resolve("build/cpf-local").resolve(d.name).resolve("db3").resolve(vendor);
        Files.createDirectories(base);
        List<String> render=List.of("java", root.resolve("bin/CpfGeneratorLauncher.java").toString(), "db", "render", "--file", root.relativize(d.definition).toString(), "--vendor", vendor, "--output", base.toString());
        runChecked(render, baseEnv, Math.max(timeoutSeconds,120), null, true);
        List<Path> migrations;
        try(var stream=Files.list(base)){ migrations=stream.filter(p->p.getFileName().toString().startsWith("V")&&p.getFileName().toString().endsWith(".sql")).sorted().toList(); }
        for (Path sql : migrations) applyOneTrackedSql(d, db, user, container, vendor, sql, password);
        Path seed=base.resolve("20_product_seed.sql"); if(Files.isRegularFile(seed)) applyOneTrackedSql(d, db, user, container, vendor, seed, password);
        Path verify=base.resolve("90_verify.sql"); if(Files.isRegularFile(verify)) executeSqlFile(db,user,container,vendor,Files.readString(verify,StandardCharsets.UTF_8),false,password);
    }

    private void applyOneTrackedSql(Domain d, String db, String user, String container, String vendor, Path sql, String password) throws Exception {
        String name = root.relativize(sql).toString().replace('\\','/');
        String checksum = sha256(sql);
        String existing = queryHistory(db, user, container, vendor, name, password);
        if (!existing.isBlank()) {
            if (!existing.equalsIgnoreCase(checksum)) throw new IllegalStateException("immutable DB migration checksum mismatch: " + name);
            progress("08", "DB Lifecycle", d.systemCode + " SKIP already-applied " + sql.getFileName());
            return;
        }
        executeSqlFile(db, user, container, vendor, Files.readString(sql, StandardCharsets.UTF_8), true, password);
        recordHistory(db, user, container, vendor, name, checksum, password);
    }

    private String queryHistory(String db, String user, String container, String vendor, String name, String password) throws Exception {
        String safe = name.replace("'", "''");
        if (vendor.equals("postgresql")) return run(List.of("docker","exec","-e","PGPASSWORD="+password,container,"psql","-At","-U",user,"-d",db,"-c","SELECT checksum FROM cpf_bootstrap_schema_history WHERE script_name='"+safe+"'"), Map.of(), 30, null, true, true).output.trim();
        if (vendor.equals("mariadb")) return run(List.of("docker","exec","-e","MYSQL_PWD="+password,container,"mariadb","-N","-s","-u"+user,db,"-e","SELECT checksum FROM cpf_bootstrap_schema_history WHERE script_name='"+safe+"'"), Map.of(), 30, null, true, true).output.trim();
        String script = "WHENEVER SQLERROR EXIT SQL.SQLCODE\nALTER SESSION SET CONTAINER="+db+";\nCONNECT "+user+"/\""+password+"\"@"+db+"\nSET HEADING OFF FEEDBACK OFF PAGESIZE 0\nSELECT checksum FROM cpf_bootstrap_schema_history WHERE script_name='"+safe+"';\nEXIT\n";
        return run(List.of("docker","exec","-i",container,"sqlplus","-s","/","as","sysdba"), Map.of(), 40, script, true, true).output.trim();
    }

    private void recordHistory(String db, String user, String container, String vendor, String name, String checksum, String password) throws Exception {
        String sql = "INSERT INTO cpf_bootstrap_schema_history(script_name,checksum) VALUES ('"+name.replace("'","''")+"','"+checksum+"');";
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
        List<String> lines = new ArrayList<>();
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
        Files.write(env, lines, StandardCharsets.UTF_8);
        pass("09", "Runtime Config", "generated=" + root.relativize(env) + " per-domain DB binding=PASS");
    }

    private void runWorkspaceVerification(boolean full) throws Exception {
        Path gradlew = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
        if (!Files.isRegularFile(gradlew)) throw new IllegalStateException("Gradle wrapper missing");
        if (!isWindows()) gradlew.toFile().setExecutable(true);
        List<String> command = new ArrayList<>(); command.add(gradlew.toString()); command.add("cpfVerify"); command.add("--no-daemon");
        if (!full) command.add("--max-workers=2");
        runChecked(command, baseEnv, Math.max(timeoutSeconds, 600), null, true);
        pass("10", "Build/Test", full ? "FULL PASS" : "FAST PASS");
    }

    private void startRuntimes() throws Exception {
        Path runDir = logDir.resolve("runtime"); Files.createDirectories(runDir);
        Path state = root.resolve("build/cpf-bootstrap/current-runtime.properties");
        if (Files.exists(state)) throw new IllegalStateException("runtime state already exists; run cpf-stop before starting another local runtime set: " + root.relativize(state));
        Properties running = new Properties();
        running.setProperty("workspace", root.toString());
        running.setProperty("startedAt", Instant.now().toString());
        Path gradlew = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
        for (Domain d : domains) {
            ProcessBuilder pb = new ProcessBuilder(gradlew.toString(), "-p", d.project.toString(), ":online:bootRun", "--no-daemon");
            pb.environment().putAll(baseEnv);
            pb.environment().put("SPRING_PROFILES_ACTIVE", "local");
            pb.environment().put(d.systemCode + "_ONLINE_PORT", Integer.toString(d.localOnlinePort));
            pb.redirectErrorStream(true);
            Path runtimeLog = runDir.resolve(d.systemCode + "-online.log");
            pb.redirectOutput(runtimeLog.toFile());
            Process process = pb.start();
            running.setProperty(d.systemCode + ".pid", Long.toString(process.pid()));
            running.setProperty(d.systemCode + ".port", Integer.toString(d.localOnlinePort));
            running.setProperty(d.systemCode + ".log", runtimeLog.toString());
            storePropertiesAtomic(state, running, "CPF local runtime process state. Stop uses only this current state.");
            waitForRuntimeHealth(d, process, runtimeLog);
        }
        pass("11", "Runtime", "started=" + domains.size() + " state=" + root.relativize(state));
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
    private static Path locateRoot(){ Path p=Path.of(System.getProperty("user.dir")).toAbsolutePath(); while(p!=null){ if(Files.isRegularFile(p.resolve("settings.gradle"))&&Files.isDirectory(p.resolve("domains"))) return p; p=p.getParent(); } throw new IllegalStateException("CPF Public Workspace root not found"); }
    private static String readDefaultTimeout(Path root){ Properties p=new Properties(); Path f=root.resolve("config/cpf-workspace.properties"); try{ if(Files.isRegularFile(f)) try(Reader r=Files.newBufferedReader(f)){p.load(r);} }catch(Exception ignored){} return p.getProperty("cpf.bootstrap.timeout-seconds","300"); }
    private static Map<String,String> parseArgs(String[] args){ Map<String,String> m=new LinkedHashMap<>(); String command="bootstrap"; for(int i=0;i<args.length;i++){ String a=args[i]; if(!a.startsWith("--")&&!a.startsWith("-")){command=a;continue;} if(a.equals("--db")&&i+1<args.length)m.put("db",args[++i]); else if(a.equals("--timeout")&&i+1<args.length)m.put("timeout",args[++i]); else if(a.equals("--run"))m.put("run","true"); else if(a.equals("--full"))m.put("full","true"); else if(a.equals("--confirm-local-reset"))m.put("confirm-local-reset","true"); else throw new IllegalArgumentException("unknown option="+a);} m.put("command",command); return m; }
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
