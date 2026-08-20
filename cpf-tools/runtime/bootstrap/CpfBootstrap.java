import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CPF Local Bootstrap의 Windows/Linux 공용 엔진입니다.
 *
 * <p>OS wrapper에는 lifecycle 의미를 두지 않고 이 엔진만 다음 순서를 소유합니다.
 * prerequisite -> selected DB -> actual DB health -> DB3 render/migration/seed/verify
 * -> capability middleware -> domain discovery -> build/test -> runtime start/health.</p>
 */
public final class CpfBootstrap {
    private static final Set<String> DB3 = Set.of("postgresql", "mariadb", "oracle");
    private static final Pattern DOMAIN_NAME = Pattern.compile("(?m)^\\s{2}name:\\s*([a-z][a-z0-9-]*)\\s*$");
    private static final Pattern SYSTEM_CODE = Pattern.compile("(?m)^\\s{2}systemCode:\\s*([A-Z][A-Z0-9]{2})\\s*$");
    private static final Pattern ONLINE_PORT = Pattern.compile("(?m)^\\s{2}localOnlinePort:\\s*(\\d+)\\s*$");
    private static final Pattern PERSISTENCE = Pattern.compile("(?m)^\\s{2}persistence:\\s*([a-z0-9-]+)\\s*$");
    private static final Pattern CACHE = Pattern.compile("(?m)^\\s{2}cache:\\s*([a-z0-9-]+)\\s*$");
    private static final Pattern MESSAGING = Pattern.compile("(?m)^\\s{2}messaging:\\s*([a-z0-9-]+)\\s*$");
    private static final String REGISTRY = "build/cpf-local/bootstrap/runtime-registry.properties";
    private static final String STATE = "build/cpf-local/bootstrap/bootstrap-state.properties";

    record Domain(String name, String systemCode, int port, String persistence, String cache, String messaging, Path definition, Path project) {}
    record Options(Path workspace, String action, String db, int timeoutSeconds, boolean skipBuild, boolean skipTest, boolean startRuntime, boolean confirmReset) {}

    private final Options options;
    private final Path workspace;
    private final Path compose;
    private final Map<String,String> env;

    private CpfBootstrap(Options options) {
        this.options = options;
        this.workspace = options.workspace.toAbsolutePath().normalize();
        this.compose = resolveCompose(workspace);
        this.env = System.getenv();
    }

    public static void main(String[] args) {
        Instant started = Instant.now();
        int code = 0;
        String action = "bootstrap";
        try {
            Options options = parse(args);
            action = options.action;
            new CpfBootstrap(options).execute();
        } catch (Exception e) {
            code = 1;
            System.err.println("[CPF][BOOTSTRAP][FAIL] " + e.getMessage());
            if (Boolean.parseBoolean(System.getenv().getOrDefault("CPF_BOOTSTRAP_DEBUG", "false"))) e.printStackTrace(System.err);
        } finally {
            System.out.printf(Locale.ROOT,
                    "[CPF][BOOTSTRAP][FINAL] action=%s result=%s exitCode=%d started=%s finished=%s%n",
                    action, code == 0 ? "PASS" : "FAIL", code, started, Instant.now());
        }
        if (code != 0) System.exit(code);
    }

    private void execute() throws Exception {
        requireWorkspace();
        switch (options.action) {
            case "bootstrap" -> bootstrap();
            case "build" -> { prerequisite(false); build(discoverDomains()); }
            case "test" -> { prerequisite(false); test(discoverDomains()); }
            case "stop" -> stop(false);
            case "reset" -> reset();
            case "status" -> status();
            default -> throw new IllegalArgumentException("지원하지 않는 action: " + options.action);
        }
    }

    private void bootstrap() throws Exception {
        stage("PREREQUISITE", () -> prerequisite(true));
        List<Domain> domains = stageValue("DOMAIN_DISCOVERY", this::discoverDomains);
        String db = selectedDb();
        List<String> middleware = requiredMiddleware(domains);
        if (domains.stream().anyMatch(d -> !"none".equals(d.persistence))) {
            stage("DB_START", () -> composeUp(List.of(dbService(db))));
            stage("DB_HEALTH", () -> waitHealthy(dbService(db)));
            stage("DB_BINDING", () -> ensureLocalBindings(domains, db));
            stage("DB_RENDER_MIGRATE_SEED_VERIFY", () -> prepareDomainDatabases(domains, db));
        }
        if (!middleware.isEmpty()) {
            stage("MIDDLEWARE_START", () -> composeUp(middleware));
            stage("MIDDLEWARE_HEALTH", () -> { for (String s : middleware) waitHealthy(s); });
        }
        if (!options.skipBuild) stage("BUILD", () -> build(domains));
        if (!options.skipTest) stage("TEST", () -> test(domains));
        if (options.startRuntime) {
            stage("RUNTIME_START", () -> startRuntime(domains, db));
            stage("RUNTIME_HEALTH", () -> waitRuntimeHealth(domains));
        }
        writeState(domains, db, middleware);
        System.out.printf("[CPF][BOOTSTRAP][PASS] db=%s domains=%d middleware=%s runtime=%s%n", db, domains.size(), middleware, options.startRuntime);
    }

    private void prerequisite(boolean containerRequired) throws Exception {
        requireCommand("git", "--version");
        ProcessResult java = run(List.of(javaCommand(), "-version"), workspace, null, null, 30, false);
        String javaText = java.output + "\n" + java.error;
        Matcher version = Pattern.compile("version \\\"(\\d+)(?:[.][^\\\"]*)?\\\"").matcher(javaText);
        if (!version.find() || Integer.parseInt(version.group(1)) != 25) {
            throw new IllegalStateException("Java 25가 필요합니다. actual=" + oneLine(javaText));
        }
        if (containerRequired) {
            requireCommand("docker", "version", "--format", "{{.Server.Version}}");
            requireCommand("docker", "compose", "version");
        }
        if (Files.isDirectory(workspace.resolve("cpf-backoffice-web/frontend")) || Files.isDirectory(workspace.resolve("cpf-admin/frontend"))) {
            requireCommand("node", "--version");
        }
    }

    private List<Domain> discoverDomains() throws IOException {
        List<Domain> domains = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(workspace, "cpf-*")) {
            for (Path project : stream) {
                Path definition = project.resolve("cpf-domain.yaml");
                if (!Files.isRegularFile(definition)) continue;
                String text = Files.readString(definition, StandardCharsets.UTF_8);
                String name = requiredMatch(text, DOMAIN_NAME, "domain.name", definition);
                String code = requiredMatch(text, SYSTEM_CODE, "domain.systemCode", definition);
                int port = optionalInt(text, ONLINE_PORT, 18080 + domains.size() * 10);
                String persistence = optionalMatch(text, PERSISTENCE, "none");
                String cache = optionalMatch(text, CACHE, "none");
                String messaging = optionalMatch(text, MESSAGING, "none");
                domains.add(new Domain(name, code, port, persistence, cache, messaging, definition, project));
            }
        }
        domains.sort(Comparator.comparing(Domain::name));
        if (domains.isEmpty()) throw new IllegalStateException("cpf-<domain>/cpf-domain.yaml을 찾지 못했습니다: " + workspace);
        Set<String> names = new HashSet<>(), codes = new HashSet<>();
        for (Domain d : domains) {
            if (!names.add(d.name) || !codes.add(d.systemCode)) throw new IllegalStateException("Domain name/systemCode 중복: " + d);
            if (!Files.isRegularFile(d.project.resolve("settings.gradle"))) throw new IllegalStateException("Generated Domain settings.gradle 누락: " + d.project);
        }
        System.out.println("[CPF][BOOTSTRAP][DOMAINS] " + domains.stream().map(d -> d.name + ":" + d.systemCode).toList());
        return domains;
    }

    private String selectedDb() throws IOException {
        String value = firstNonBlank(env.get("CPF_DB_VENDOR"), env.get("CPF_LOCAL_DB"), workspaceProperty("cpf.default-db"), options.db, "postgresql").toLowerCase(Locale.ROOT);
        if (!DB3.contains(value)) throw new IllegalArgumentException("DB는 oracle/postgresql/mariadb만 지원합니다: " + value);
        return value;
    }

    private List<String> requiredMiddleware(List<Domain> domains) {
        LinkedHashSet<String> services = new LinkedHashSet<>();
        for (Domain d : domains) {
            switch (d.cache) {
                case "redis" -> services.add("cpf-redis");
                case "valkey" -> services.add("cpf-valkey");
            }
            switch (d.messaging) {
                case "kafka" -> services.add("cpf-kafka");
                case "rabbitmq" -> services.add("cpf-rabbitmq");
                case "jms" -> services.add("cpf-artemis");
                case "ibm-mq" -> services.add("cpf-ibm-mq");
            }
        }
        return List.copyOf(services);
    }

    private void composeUp(List<String> services) throws Exception {
        if (services.isEmpty()) return;
        if (!Files.isRegularFile(compose)) throw new IllegalStateException("Bootstrap compose asset 누락: " + compose);
        List<String> cmd = new ArrayList<>(List.of("docker", "compose", "-f", compose.toString()));
        if (services.contains("cpf-ibm-mq")) cmd.addAll(List.of("--profile", "ibm-mq"));
        cmd.addAll(List.of("up", "-d")); cmd.addAll(services);
        runChecked(cmd, workspace, null, null, options.timeoutSeconds);
    }

    private void waitHealthy(String service) throws Exception {
        Instant deadline = Instant.now().plusSeconds(options.timeoutSeconds);
        while (Instant.now().isBefore(deadline)) {
            ProcessResult id = run(List.of("docker", "compose", "-f", compose.toString(), "ps", "-q", service), workspace, null, null, 30, false);
            String containerId = id.output.trim();
            if (!containerId.isBlank()) {
                ProcessResult inspect = run(List.of("docker", "inspect", "--format", "{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}", containerId), workspace, null, null, 30, false);
                String status = inspect.output.trim();
                if ("healthy".equals(status) || "running".equals(status)) {
                    System.out.printf("[CPF][BOOTSTRAP][HEALTHY] service=%s container=%s%n", service, containerId.substring(0, Math.min(12, containerId.length())));
                    return;
                }
                if ("unhealthy".equals(status) || "exited".equals(status) || "dead".equals(status)) throw new IllegalStateException("Container health 실패 service=" + service + " status=" + status);
            }
            Thread.sleep(2000);
        }
        throw new IllegalStateException("Container health timeout service=" + service + " seconds=" + options.timeoutSeconds);
    }

    private void ensureLocalBindings(List<Domain> domains, String vendor) throws IOException {
        for (Domain d : domains) {
            if ("none".equals(d.persistence)) continue;
            Path profile = workspace.resolve("build/cpf-local").resolve(d.name).resolve("cpf-db-profile.local.json");
            if (Files.isRegularFile(profile)) {
                String body = Files.readString(profile, StandardCharsets.UTF_8);
                if (!body.contains("\"vendor\": \"" + vendor + "\"") && !body.contains("\"vendor\":\"" + vendor + "\"")) {
                    throw new IllegalStateException("Domain DB Binding vendor와 selected DB가 다릅니다: " + profile + " selected=" + vendor);
                }
                continue;
            }
            Files.createDirectories(profile.getParent());
            String code = d.systemCode.toLowerCase(Locale.ROOT);
            String dbName = "cpf_" + code;
            String schema = "postgresql".equals(vendor) ? "public" : ("oracle".equals(vendor) ? "CPF_" + d.systemCode + "_MIGRATION" : dbName);
            int port = switch (vendor) { case "postgresql" -> 5432; case "mariadb" -> 3306; default -> 1521; };
            String payload = """
                    {
                      "profileVersion": 2,
                      "profileName": "%s-local",
                      "environment": "local",
                      "domain": {"name": "%s", "systemCode": "%s"},
                      "database": {
                        "vendor": "%s", "host": "127.0.0.1", "port": %d,
                        %s
                        "schemaName": "%s",
                        "migration": {"username": "cpf_%s_migration", "password": {"env": "%s_DB_MIGRATION_PASSWORD"}},
                        "runtime": {"username": "cpf_%s_runtime", "password": {"env": "%s_DB_RUNTIME_PASSWORD"}}
                      }
                    }
                    """.formatted(d.name, d.name, d.systemCode, vendor, port,
                    "oracle".equals(vendor) ? "\"serviceName\": \"FREEPDB1\"," : "\"databaseName\": \"" + dbName + "\",",
                    schema, code, d.systemCode, code, d.systemCode);
            Files.writeString(profile, payload, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
            System.out.println("[CPF][BOOTSTRAP][BINDING_CREATED] " + workspace.relativize(profile));
        }
    }

    private void prepareDomainDatabases(List<Domain> domains, String vendor) throws Exception {
        requireEnv("CPF_LOCAL_DB_ADMIN_PASSWORD");
        for (Domain d : domains) {
            if ("none".equals(d.persistence)) continue;
            requireEnv(d.systemCode + "_DB_MIGRATION_PASSWORD");
            requireEnv(d.systemCode + "_DB_RUNTIME_PASSWORD");
            Path out = workspace.resolve("build/cpf-local").resolve(d.name).resolve("db3").resolve(vendor);
            Files.createDirectories(out);
            invokeGenerator(List.of("db", "render", "--file", workspace.relativize(d.definition).toString(), "--vendor", vendor, "--output", out.toString()));
            provisionDomainDatabase(d, vendor);
            executeSql(d, vendor, out.resolve("10_empty_install.sql"));
            executeSql(d, vendor, out.resolve("V1__" + d.name + "_domain.sql"));
            executeSql(d, vendor, out.resolve("20_product_seed.sql"));
            executeSql(d, vendor, out.resolve("90_verify.sql"));
        }
    }

    private void provisionDomainDatabase(Domain d, String vendor) throws Exception {
        String code = d.systemCode.toLowerCase(Locale.ROOT), db = "cpf_" + code;
        String mig = "cpf_" + code + "_migration", run = "cpf_" + code + "_runtime";
        String migPwd = requireEnv(d.systemCode + "_DB_MIGRATION_PASSWORD"), runPwd = requireEnv(d.systemCode + "_DB_RUNTIME_PASSWORD");
        Path sqlRoot = workspace.resolve("build/cpf-local/bootstrap");
        Files.createDirectories(sqlRoot);
        Path sql = Files.createTempFile(sqlRoot, "provision-", ".sql");
        try {
            String body;
            if ("postgresql".equals(vendor)) {
                body = "DO $$ BEGIN IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='"+mig+"') THEN CREATE ROLE "+mig+" LOGIN PASSWORD '"+sqlLiteral(migPwd)+"'; END IF; IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='"+run+"') THEN CREATE ROLE "+run+" LOGIN PASSWORD '"+sqlLiteral(runPwd)+"'; END IF; END $$;\n";
                runSqlContainer(vendor, null, sqlWrite(sql, body), true);
                ProcessResult exists = run(List.of("docker","exec","cpf-public-postgresql","psql","-U","postgres","-tAc","SELECT 1 FROM pg_database WHERE datname='"+db+"'"), workspace, null, null, 30, false);
                if (!exists.output.trim().equals("1")) runChecked(List.of("docker","exec","cpf-public-postgresql","createdb","-U","postgres","-O",mig,db), workspace, null, null, 60);
                runSqlContainer(vendor, db, sqlWrite(sql, "GRANT CONNECT ON DATABASE "+db+" TO "+run+";\n"), true);
            } else if ("mariadb".equals(vendor)) {
                body = "CREATE DATABASE IF NOT EXISTS `"+db+"`;\n"+
                        "CREATE USER IF NOT EXISTS '"+mig+"'@'%' IDENTIFIED BY '"+sqlLiteral(migPwd)+"';\n"+
                        "CREATE USER IF NOT EXISTS '"+run+"'@'%' IDENTIFIED BY '"+sqlLiteral(runPwd)+"';\n"+
                        "GRANT ALL PRIVILEGES ON `"+db+"`.* TO '"+mig+"'@'%';\n"+
                        "GRANT SELECT,INSERT,UPDATE,DELETE ON `"+db+"`.* TO '"+run+"'@'%'; FLUSH PRIVILEGES;\n";
                runSqlContainer(vendor, null, sqlWrite(sql, body), true);
            } else {
                String schema = "CPF_" + d.systemCode + "_MIGRATION";
                body = "WHENEVER SQLERROR EXIT SQL.SQLCODE\nDECLARE n NUMBER; BEGIN SELECT COUNT(*) INTO n FROM all_users WHERE username='"+schema+"'; IF n=0 THEN EXECUTE IMMEDIATE 'CREATE USER "+schema+" IDENTIFIED BY \""+sqlLiteral(migPwd)+"\"'; EXECUTE IMMEDIATE 'GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO "+schema+"'; EXECUTE IMMEDIATE 'ALTER USER "+schema+" QUOTA UNLIMITED ON USERS'; END IF; END; /\n";
                runSqlContainer(vendor, null, sqlWrite(sql, body), true);
            }
        } finally { Files.deleteIfExists(sql); }
    }

    private void executeSql(Domain d, String vendor, Path sql) throws Exception {
        if (!Files.isRegularFile(sql)) throw new IllegalStateException("Rendered SQL 누락: " + sql);
        String db = "cpf_" + d.systemCode.toLowerCase(Locale.ROOT);
        runSqlContainer(vendor, db, sql, false, d);
    }

    private void runSqlContainer(String vendor, String db, Path sql, boolean admin) throws Exception { runSqlContainer(vendor, db, sql, admin, null); }
    private void runSqlContainer(String vendor, String db, Path sql, boolean admin, Domain domain) throws Exception {
        List<String> cmd;
        Map<String,String> childEnv = new HashMap<>(env);
        if ("postgresql".equals(vendor)) {
            String user = admin || domain == null ? "postgres" : "cpf_" + domain.systemCode.toLowerCase(Locale.ROOT) + "_migration";
            if (!admin && domain != null) childEnv.put("PGPASSWORD", requireEnv(domain.systemCode + "_DB_MIGRATION_PASSWORD"));
            cmd = new ArrayList<>(List.of("docker","exec","-i"));
            if (!admin && domain != null) cmd.addAll(List.of("-e","PGPASSWORD"));
            cmd.addAll(List.of("cpf-public-postgresql","psql","-v","ON_ERROR_STOP=1","-U",user));
            if (db != null) cmd.addAll(List.of("-d",db));
        } else if ("mariadb".equals(vendor)) {
            String user = admin || domain == null ? "root" : "cpf_" + domain.systemCode.toLowerCase(Locale.ROOT) + "_migration";
            String shell = admin || domain == null ? "exec mariadb -uroot -p\"$MARIADB_ROOT_PASSWORD\"" : "exec mariadb -u\"$CPF_DOMAIN_DB_USER\" -p\"$CPF_DOMAIN_DB_PASSWORD\"";
            if (db != null) shell += " \"" + db + "\"";
            cmd = new ArrayList<>(List.of("docker","exec","-i"));
            if (!admin && domain != null) {
                childEnv.put("CPF_DOMAIN_DB_USER", user); childEnv.put("CPF_DOMAIN_DB_PASSWORD", requireEnv(domain.systemCode + "_DB_MIGRATION_PASSWORD"));
                cmd.addAll(List.of("-e","CPF_DOMAIN_DB_USER","-e","CPF_DOMAIN_DB_PASSWORD"));
            }
            cmd.addAll(List.of("cpf-public-mariadb","sh","-ec",shell));
        } else {
            String connect = admin || domain == null ? "system/$ORACLE_PWD@FREEPDB1" : "CPF_"+domain.systemCode+"_MIGRATION/$CPF_DOMAIN_DB_PASSWORD@FREEPDB1";
            cmd = new ArrayList<>(List.of("docker","exec","-i"));
            if (!admin && domain != null) { childEnv.put("CPF_DOMAIN_DB_PASSWORD", requireEnv(domain.systemCode + "_DB_MIGRATION_PASSWORD")); cmd.addAll(List.of("-e","CPF_DOMAIN_DB_PASSWORD")); }
            cmd.addAll(List.of("cpf-public-oracle","bash","-ec","sqlplus -s '"+connect+"'"));
        }
        runChecked(cmd, workspace, childEnv, sql, options.timeoutSeconds);
    }

    private void build(List<Domain> domains) throws Exception {
        if (isPublicWorkspace()) {
            runChecked(List.of(gradleCommand(), "cpfBuild", "--no-daemon"), workspace, gradleEnv(), null, options.timeoutSeconds);
        } else {
            runChecked(List.of(gradleCommand(), "-PcpfIncludeGeneratedDomains=true", "assemble", "--continue", "--no-daemon"), workspace, gradleEnv(), null, options.timeoutSeconds);
        }
    }

    private void test(List<Domain> domains) throws Exception {
        if (isPublicWorkspace()) {
            runChecked(List.of(gradleCommand(), "cpfTest", "--continue", "--no-daemon"), workspace, gradleEnv(), null, options.timeoutSeconds);
        } else {
            runChecked(List.of(gradleCommand(), "-PcpfIncludeGeneratedDomains=true", "test", "--continue", "--no-daemon"), workspace, gradleEnv(), null, options.timeoutSeconds);
        }
    }

    private Map<String,String> gradleEnv() {
        Map<String,String> result = new HashMap<>(env);
        result.put("CPF_DB_VENDOR", options.db == null ? "postgresql" : options.db);
        return result;
    }

    private void startRuntime(List<Domain> domains, String vendor) throws Exception {
        Path registry = workspace.resolve(REGISTRY); Files.createDirectories(registry.getParent());
        if (Files.exists(registry)) throw new IllegalStateException("Runtime registry가 이미 있습니다. stop 후 재실행하세요: " + registry);
        Properties rows = new Properties();
        for (Domain d : domains) {
            Path libs = d.project.resolve("online/build/libs");
            if (!Files.isDirectory(libs)) continue;
            Path jar;
            try (var files = Files.list(libs)) { jar = files.filter(p -> p.getFileName().toString().endsWith(".jar") && !p.getFileName().toString().contains("plain")).sorted().reduce((a,b)->b).orElse(null); }
            if (jar == null) continue;
            Path log = workspace.resolve("build/cpf-local/bootstrap/logs/" + d.name + ".log"); Files.createDirectories(log.getParent());
            List<String> cmd = new ArrayList<>(List.of(javaCommand(), "-jar", jar.toString(), "--spring.profiles.active=local", "--server.port=" + d.port));
            ProcessBuilder pb = new ProcessBuilder(cmd); pb.directory(workspace.toFile()); pb.redirectErrorStream(true); pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log.toFile()));
            Map<String,String> pe = pb.environment(); applyDatasourceEnv(pe,d,vendor);
            Process p = pb.start(); rows.setProperty(d.name + ".pid", Long.toString(p.pid())); rows.setProperty(d.name + ".port", Integer.toString(d.port)); rows.setProperty(d.name + ".log", workspace.relativize(log).toString().replace('\\','/'));
            System.out.printf("[CPF][BOOTSTRAP][RUNTIME] domain=%s pid=%d port=%d%n", d.name,p.pid(),d.port);
        }
        try (Writer w = Files.newBufferedWriter(registry,StandardCharsets.UTF_8)) { rows.store(w,"CPF local runtime registry"); }
    }

    private void applyDatasourceEnv(Map<String,String> target, Domain d, String vendor) {
        if ("none".equals(d.persistence)) return;
        String code=d.systemCode, db="cpf_"+code.toLowerCase(Locale.ROOT), user="cpf_"+code.toLowerCase(Locale.ROOT)+"_runtime";
        String url, driver;
        if ("postgresql".equals(vendor)) { url="jdbc:postgresql://127.0.0.1:5432/"+db; driver="org.postgresql.Driver"; }
        else if ("mariadb".equals(vendor)) { url="jdbc:mariadb://127.0.0.1:3306/"+db; driver="org.mariadb.jdbc.Driver"; }
        else { url="jdbc:oracle:thin:@//127.0.0.1:1521/FREEPDB1"; driver="oracle.jdbc.OracleDriver"; user="CPF_"+code+"_RUNTIME"; }
        target.put(code+"_DATASOURCE_URL",url); target.put(code+"_DATASOURCE_USERNAME",user); target.put(code+"_DATASOURCE_PASSWORD",requireEnv(code+"_DB_RUNTIME_PASSWORD")); target.put(code+"_DATASOURCE_DRIVER",driver);
        target.put(code+"_ONLINE_PORT",Integer.toString(d.port));
    }

    private void waitRuntimeHealth(List<Domain> domains) throws Exception {
        HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        for (Domain d:domains) {
            if (!Files.isDirectory(d.project.resolve("online"))) continue;
            URI uri=URI.create("http://127.0.0.1:"+d.port+"/actuator/health"); Instant deadline=Instant.now().plusSeconds(options.timeoutSeconds);
            while (Instant.now().isBefore(deadline)) {
                try { var r=client.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(3)).GET().build(),HttpResponse.BodyHandlers.ofString()); if (r.statusCode()==200 && r.body().contains("UP")) { System.out.println("[CPF][BOOTSTRAP][RUNTIME_HEALTHY] "+d.name+" "+uri); break; } } catch (Exception ignored) {}
                Thread.sleep(1500);
                if (Instant.now().isAfter(deadline)) throw new IllegalStateException("Runtime health timeout domain="+d.name+" uri="+uri);
            }
        }
    }

    private void stop(boolean keepContainers) throws Exception {
        Path registry=workspace.resolve(REGISTRY);
        if (Files.isRegularFile(registry)) {
            Properties p=new Properties(); try(Reader r=Files.newBufferedReader(registry,StandardCharsets.UTF_8)){p.load(r);} 
            for(String key:p.stringPropertyNames()) if(key.endsWith(".pid")) { long pid=Long.parseLong(p.getProperty(key)); ProcessHandle.of(pid).ifPresent(h->{h.destroy(); try { if(!h.onExit().get(10,TimeUnit.SECONDS).isAlive()) return; } catch(Exception ignored){} h.destroyForcibly();}); }
            Files.deleteIfExists(registry);
        }
        if (!keepContainers && Files.isRegularFile(compose)) runChecked(List.of("docker","compose","-f",compose.toString(),"stop"),workspace,null,null,options.timeoutSeconds);
        System.out.println("[CPF][BOOTSTRAP][STOP] runtime stopped; Docker volumes preserved");
    }

    private void reset() throws Exception {
        if (!options.confirmReset) throw new IllegalArgumentException("reset은 --confirm-local-reset 명시 승인이 필요합니다.");
        stop(true);
        if (Files.isRegularFile(compose)) runChecked(List.of("docker","compose","-f",compose.toString(),"down","--volumes","--remove-orphans"),workspace,null,null,options.timeoutSeconds);
        Path local=workspace.resolve("build/cpf-local"); if(Files.exists(local)) deleteTree(local);
        System.out.println("[CPF][BOOTSTRAP][RESET] local DB/middleware volumes and local binding outputs removed");
    }

    private void status() throws Exception {
        List<Domain> domains=discoverDomains();
        Path registry=workspace.resolve(REGISTRY); System.out.println("[CPF][BOOTSTRAP][STATUS] registry="+Files.exists(registry)+" domains="+domains.size());
        if(Files.isRegularFile(compose)) {
            if (findCommand(isWindows()?"docker.exe":"docker") == null) System.out.println("[CPF][BOOTSTRAP][STATUS] docker=UNAVAILABLE");
            else run(List.of("docker","compose","-f",compose.toString(),"ps"),workspace,null,null,30,true);
        }
    }

    private void writeState(List<Domain> domains,String db,List<String> middleware) throws IOException {
        Properties p=new Properties(); p.setProperty("status","PASS"); p.setProperty("db",db); p.setProperty("domains",String.join(",",domains.stream().map(Domain::name).toList())); p.setProperty("middleware",String.join(",",middleware)); p.setProperty("updatedAt",Instant.now().toString());
        Path state=workspace.resolve(STATE); Files.createDirectories(state.getParent()); try(Writer w=Files.newBufferedWriter(state,StandardCharsets.UTF_8)){p.store(w,"CPF bootstrap state");}
    }

    private void invokeGenerator(List<String> args) throws Exception {
        Path privateCli=workspace.resolve("cpf-tools/runtime/cli/cpf.py"); List<String> cmd=new ArrayList<>();
        if(Files.isRegularFile(privateCli)) { cmd.add(pythonCommand()); cmd.add(privateCli.toString()); cmd.add("--root"); cmd.add(workspace.toString()); }
        else { Path launcher=workspace.resolve("bin/CpfGeneratorLauncher.java"); if(!Files.isRegularFile(launcher)) throw new IllegalStateException("Public Generator launcher 누락: "+launcher); cmd.add(javaCommand()); cmd.add(launcher.toString()); cmd.add("--root"); cmd.add(workspace.toString()); }
        cmd.addAll(args); runChecked(cmd,workspace,null,null,options.timeoutSeconds);
    }

    private boolean isPublicWorkspace(){return Files.isDirectory(workspace.resolve(".cpf-public"));}
    private String workspaceProperty(String key) throws IOException { Path p=workspace.resolve("config/cpf-workspace.properties"); if(!Files.isRegularFile(p)) return null; Properties props=new Properties(); try(Reader r=Files.newBufferedReader(p,StandardCharsets.UTF_8)){props.load(r);} return props.getProperty(key); }
    private String dbService(String db){return switch(db){case "postgresql"->"cpf-postgresql";case "mariadb"->"cpf-mariadb";case "oracle"->"cpf-oracle";default->throw new IllegalArgumentException(db);};}
    private Path resolveCompose(Path root){ Path privatePath=root.resolve("cpf-tools/runtime/bootstrap/compose.yaml"); return Files.isRegularFile(privatePath)?privatePath:root.resolve("deploy/local/compose.yaml"); }
    private String gradleCommand(){return workspace.resolve(isWindows()?"gradlew.bat":"gradlew").toString();}
    private String javaCommand(){return Optional.ofNullable(findCommand(isWindows()?"java.exe":"java")).orElse("java");}
    private String pythonCommand(){String p=findCommand(isWindows()?"python.exe":"python3"); if(p==null)p=findCommand("python"); if(p==null)throw new IllegalStateException("Private generator 실행에 Python이 필요합니다."); return p;}
    private static boolean isWindows(){return System.getProperty("os.name","").toLowerCase(Locale.ROOT).contains("win");}

    private void requireWorkspace(){if(!Files.isDirectory(workspace))throw new IllegalArgumentException("Workspace가 없습니다: "+workspace); if(!Files.isRegularFile(workspace.resolve(isWindows()?"gradlew.bat":"gradlew")))throw new IllegalStateException("Gradle wrapper 누락: "+workspace);}
    private void requireCommand(String... cmd)throws Exception{ProcessResult r=run(Arrays.asList(cmd),workspace,null,null,30,false);if(r.code!=0)throw new IllegalStateException("Prerequisite command 실패: "+String.join(" ",cmd)+" "+oneLine(r.error));System.out.println("[CPF][BOOTSTRAP][PREREQ] "+cmd[0]+"=PASS");}
    private String requireEnv(String name){String v=env.get(name);if(v==null||v.isBlank())throw new IllegalStateException(name+" 환경변수가 필요합니다(Secret 원문은 로그에 출력하지 않습니다).");return v;}

    private static Options parse(String[] args){Path workspace=Path.of(".");String action="bootstrap",db=null;int timeout=300;boolean skipBuild=false,skipTest=false,startRuntime=false,confirmReset=false;for(int i=0;i<args.length;i++){switch(args[i]){case "bootstrap","build","test","stop","reset","status"->action=args[i];case "--workspace","--root"->workspace=Path.of(requireArg(args,++i,"workspace"));case "--db"->db=requireArg(args,++i,"db");case "--timeout-seconds"->timeout=Integer.parseInt(requireArg(args,++i,"timeout"));case "--skip-build"->skipBuild=true;case "--skip-test"->skipTest=true;case "--start-runtime"->startRuntime=true;case "--confirm-local-reset"->confirmReset=true;default->throw new IllegalArgumentException("알 수 없는 인자: "+args[i]);}}if(timeout<10||timeout>3600)throw new IllegalArgumentException("timeout은 10~3600초 범위여야 합니다.");return new Options(workspace,action,db,timeout,skipBuild,skipTest,startRuntime,confirmReset);}
    private static String requireArg(String[] args,int i,String name){if(i>=args.length)throw new IllegalArgumentException("--"+name+" 값이 필요합니다.");return args[i];}
    private static String requiredMatch(String text,Pattern p,String field,Path file){Matcher m=p.matcher(text);if(!m.find())throw new IllegalStateException(field+" 누락: "+file);return m.group(1);}
    private static String optionalMatch(String text,Pattern p,String def){Matcher m=p.matcher(text);return m.find()?m.group(1):def;}
    private static int optionalInt(String text,Pattern p,int def){Matcher m=p.matcher(text);return m.find()?Integer.parseInt(m.group(1)):def;}
    private static String firstNonBlank(String... values){for(String v:values)if(v!=null&&!v.isBlank())return v.trim();return "";}
    private static String sqlLiteral(String v){return v.replace("'","''");}
    private static Path sqlWrite(Path p,String body)throws IOException{Files.writeString(p,body,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);return p;}
    private static String oneLine(String v){return v==null?"":v.replace('\r',' ').replace('\n',' ').strip();}
    private static String findCommand(String name){String path=System.getenv("PATH");if(path==null)return null;for(String part:path.split(Pattern.quote(File.pathSeparator))){Path p=Path.of(part,name);if(Files.isRegularFile(p)&&Files.isExecutable(p))return p.toString();}return null;}
    private static void deleteTree(Path root)throws IOException{try(var paths=Files.walk(root)){for(Path p:paths.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}}

    @FunctionalInterface interface CheckedRunnable{void run()throws Exception;}
    @FunctionalInterface interface CheckedSupplier<T>{T get()throws Exception;}
    private static void stage(String name,CheckedRunnable work)throws Exception{Instant s=Instant.now();System.out.println("[CPF][BOOTSTRAP][START] stage="+name+" at="+s);try{work.run();System.out.println("[CPF][BOOTSTRAP][PASS] stage="+name+" elapsedMs="+Duration.between(s,Instant.now()).toMillis());}catch(Exception e){System.err.println("[CPF][BOOTSTRAP][FAIL] stage="+name+" error="+e.getMessage());throw e;}}
    private static <T>T stageValue(String name,CheckedSupplier<T> work)throws Exception{final Object[] v=new Object[1];stage(name,()->v[0]=work.get());@SuppressWarnings("unchecked")T result=(T)v[0];return result;}

    record ProcessResult(int code,String output,String error){}
    private static ProcessResult run(List<String> cmd,Path cwd,Map<String,String> environment,Path stdin,int timeoutSeconds,boolean inherit)throws Exception{
        ProcessBuilder pb=new ProcessBuilder(cmd);pb.directory(cwd.toFile());if(environment!=null){pb.environment().clear();pb.environment().putAll(environment);}if(stdin!=null)pb.redirectInput(stdin.toFile());if(inherit){pb.inheritIO();Process p=pb.start();if(!p.waitFor(timeoutSeconds,TimeUnit.SECONDS)){p.destroyForcibly();throw new IllegalStateException("command timeout: "+cmd);}return new ProcessResult(p.exitValue(),"","");}
        Process p=pb.start();
        ByteArrayOutputStream out=new ByteArrayOutputStream(),err=new ByteArrayOutputStream();
        Runnable stdout=()->p.inputReader(StandardCharsets.UTF_8).lines().forEachOrdered(line->{System.out.println(line);try{out.write((line+"\n").getBytes(StandardCharsets.UTF_8));}catch(IOException ignored){}});
        Runnable stderr=()->p.errorReader(StandardCharsets.UTF_8).lines().forEachOrdered(line->{System.err.println(line);try{err.write((line+"\n").getBytes(StandardCharsets.UTF_8));}catch(IOException ignored){}});
        Thread a=Thread.startVirtualThread(stdout),b=Thread.startVirtualThread(stderr);
        if(!p.waitFor(timeoutSeconds,TimeUnit.SECONDS)){p.destroyForcibly();throw new IllegalStateException("command timeout: "+cmd);}
        a.join();b.join();return new ProcessResult(p.exitValue(),out.toString(StandardCharsets.UTF_8),err.toString(StandardCharsets.UTF_8));}
    private static void runChecked(List<String> cmd,Path cwd,Map<String,String> environment,Path stdin,int timeoutSeconds)throws Exception{ProcessResult r=run(cmd,cwd,environment,stdin,timeoutSeconds,true);if(r.code!=0)throw new IllegalStateException("command failed exit="+r.code+": "+String.join(" ",cmd));}
}
