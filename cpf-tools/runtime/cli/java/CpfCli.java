import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.CodeSource;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * CPF Unified CLI canonical implementation.
 *
 * <p>Exactly-one official Tooling Interface. Windows/Linux wrappers only launch this JAR.
 * Public and internal differences are capability/profile projection, never separate CLIs.</p>
 */
public final class CpfCli {
    private static final Properties BUILD = loadBuildProperties();
    private static final String VERSION = BUILD.getProperty("version", "UNKNOWN");
    private static final String SOURCE_IDENTITY = BUILD.getProperty("sourceIdentitySha256", "UNKNOWN");
    private static final String CAPABILITY_PROFILE = BUILD.getProperty("capabilityProfile", "INTERNAL").trim().toUpperCase(Locale.ROOT);
    private static final int OK = 0;
    private static final int FAILURE = 1;
    private static final int USAGE = 2;
    private static final int PREREQUISITE = 69;
    private static final int TIMEOUT = 124;
    private static final Duration COMMAND_TIMEOUT = Duration.ofMinutes(45);
    private static final Set<String> PUBLIC = Set.of(
            "bootstrap", "domain-new", "domain-sync", "build", "test", "run", "stop", "reset", "status", "version", "help");
    private static final Set<String> INTERNAL_NAMESPACES = Set.of("dev", "verify", "publish", "release");

    private CpfCli() {}

    public static void main(String[] args) { System.exit(run(args)); }

    static int run(String[] args) {
        try {
            List<String> argv = new ArrayList<>(Arrays.asList(args));
            String command = normalize(argv.isEmpty() ? "help" : argv.remove(0));
            if (command.equals("-h") || command.equals("--help")) command = "help";
            if (command.equals("--version") || command.equals("-v")) command = "version";
            if (!PUBLIC.contains(command) && !INTERNAL_NAMESPACES.contains(command)) return usage("unsupported command=" + command);
            if (INTERNAL_NAMESPACES.contains(command) && !internalEnabled()) {
                fail("CPF-CLI-CAPABILITY", "Internal command is not projected in profile=" + CAPABILITY_PROFILE);
                return PREREQUISITE;
            }
            if (command.equals("help")) return help();
            if (command.equals("version")) return version();
            Path root = workspaceRoot();
            Instant started = Instant.now();
            log("START", "command=" + command + " profile=" + CAPABILITY_PROFILE + " root=" + root + " time=" + started);
            int result = switch (command) {
                case "help", "version" -> OK;
                case "bootstrap" -> requireJava25Then(() -> internalEnabled() ? internalBootstrap(root, argv) : bootstrap(root, argv, false));
                case "run" -> requireJava25Then(() -> internalEnabled() ? internalRuntime(root, "start", argv) : bootstrap(root, argv, true));
                case "stop" -> requireJava25Then(() -> internalEnabled() ? internalRuntime(root, "stop", argv) : bootstrapCommand(root, "stop", argv));
                case "reset" -> requireJava25Then(() -> internalEnabled() ? internalReset(root, argv) : reset(root, argv));
                case "status" -> internalEnabled() ? internalRuntime(root, "status", argv) : status(root);
                case "domain-new" -> requireJava25Then(() -> domainNew(root, argv));
                case "domain-sync" -> requireJava25Then(() -> generator(root, concat(List.of("domain", "sync"), argv)));
                case "build" -> requireJava25Then(() -> gradle(root, "cpfBuild", argv));
                case "test" -> requireJava25Then(() -> gradle(root, "cpfTest", argv));
                case "dev" -> requireJava25Then(() -> internalDev(root, argv));
                case "verify" -> requireJava25Then(() -> internalVerify(root, argv));
                case "publish" -> requireJava25Then(() -> internalPublish(root, argv));
                case "release" -> requireJava25Then(() -> internalRelease(root, argv));
                default -> USAGE;
            };
            log("END", "command=" + command + " status=" + (result == 0 ? "PASS" : "FAIL") + " exitCode=" + result + " start=" + started + " end=" + Instant.now());
            return result;
        } catch (IllegalArgumentException e) {
            fail("CPF-CLI-USAGE", e.getMessage());
            return USAGE;
        } catch (Throwable e) {
            fail("CPF-CLI-UNEXPECTED", e.getMessage());
            return FAILURE;
        }
    }

    private static int help() {
        System.out.println("CPF Unified CLI");
        System.out.println("Public commands:");
        System.out.println("  cpf bootstrap [--db oracle|postgresql|mariadb]");
        System.out.println("  cpf domain-new <domain> [SYSTEM_CODE]");
        System.out.println("  cpf domain-sync [domain]");
        System.out.println("  cpf build | test | run | stop | reset | status");
        if (internalEnabled()) {
            System.out.println("Internal namespaces:");
            System.out.println("  cpf dev build|test|targeted-test|full-validation|run-batch|modules|resource|db3");
            System.out.println("  cpf verify all|generator|generated|db3|catalog|ownership|source");
            System.out.println("  cpf publish framework");
            System.out.println("  cpf release open-git [build|check|status] [--profile binary|source]");
        }
        System.out.println("  cpf version");
        return OK;
    }

    private static int version() {
        System.out.println("CPF_CLI_VERSION=" + VERSION);
        System.out.println("SOURCE_IDENTITY=" + sourceIdentity());
        System.out.println("CAPABILITY_PROFILE=" + CAPABILITY_PROFILE);
        System.out.println("JAVA_VERSION=" + Runtime.version());
        return OK;
    }

    private static int status(Path root) throws IOException {
        Path state = root.resolve("build/cpf-bootstrap/current-runtime.properties");
        if (!Files.isRegularFile(state)) {
            System.out.println("CPF_STATUS=STOPPED");
            return OK;
        }
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(state, StandardCharsets.UTF_8)) { p.load(reader); }
        System.out.println("CPF_STATUS=RUNNING");
        for (String name : new TreeSet<>(p.stringPropertyNames())) {
            if (name.toLowerCase(Locale.ROOT).matches(".*(password|secret|token|credential).*")) continue;
            System.out.println(name + "=" + sanitize(p.getProperty(name)));
        }
        return OK;
    }

    private static int bootstrap(Path root, List<String> args, boolean startRuntime) throws Exception {
        List<String> forwarded = new ArrayList<>();
        forwarded.add("bootstrap");
        if (startRuntime) forwarded.add("--run");
        forwarded.addAll(normalizeBootstrapArgs(args));
        return runClass(root, "CpfBootstrap", forwarded);
    }

    private static int bootstrapCommand(Path root, String command, List<String> args) throws Exception {
        List<String> forwarded = new ArrayList<>(); forwarded.add(command); forwarded.addAll(args);
        return runClass(root, "CpfBootstrap", forwarded);
    }

    private static int reset(Path root, List<String> args) throws Exception {
        List<String> normalized = new ArrayList<>();
        for (String arg : args) normalized.add("--confirm".equals(arg) ? "--confirm-local-reset" : arg);
        return bootstrapCommand(root, "reset", normalized);
    }

    private static List<String> normalizeBootstrapArgs(List<String> args) {
        List<String> out = new ArrayList<>();
        for (String arg : args) {
            if ("--start-runtime".equals(arg)) out.add("--run");
            else if ("--timeout-seconds".equals(arg)) out.add("--timeout");
            else out.add(arg);
        }
        return out;
    }

    private static int domainNew(Path root, List<String> args) throws Exception {
        if (args.isEmpty()) return usage("cpf domain-new <domain> [SYSTEM_CODE]");
        String name = args.get(0);
        String system = args.size() > 1 && !args.get(1).startsWith("-") ? args.get(1) : deriveSystemCode(name);
        int tail = args.size() > 1 && !args.get(1).startsWith("-") ? 2 : 1;
        List<String> forwarded = new ArrayList<>(List.of("domain", "create", "--name", name, "--system-code", system));
        forwarded.addAll(args.subList(tail, args.size()));
        return generator(root, forwarded);
    }

    private static int generator(Path root, List<String> args) throws Exception {
        if (internalEnabled()) {
            List<String> forwarded = new ArrayList<>(List.of("--root", root.toString()));
            forwarded.addAll(args);
            return runPythonEngine(root, forwarded);
        }
        List<String> forwarded = new ArrayList<>(List.of("--root", root.toString()));
        forwarded.addAll(args);
        return runClass(root, "CpfGeneratorLauncher", forwarded);
    }

    private static int gradle(Path root, String task, List<String> args) throws Exception {
        Path wrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
        if (!Files.isRegularFile(wrapper)) return prerequisite("CPF-CLI-GRADLE-MISSING", "Gradle wrapper missing: " + wrapper);
        if (!isWindows()) wrapper.toFile().setExecutable(true, true);
        List<String> command = new ArrayList<>(List.of(wrapper.toString(), task, "--no-daemon", "--no-parallel"));
        command.addAll(args);
        return runProcess(root, command, COMMAND_TIMEOUT);
    }

    private static int internalDev(Path root, List<String> args) throws Exception {
        if (args.isEmpty()) return usage("cpf dev build|test|targeted-test|full-validation|run-batch|modules|resource|db3");
        String sub = normalize(args.remove(0));
        return switch (sub) {
            case "build" -> gradle(root, "clean", concat(List.of("build", "--continue"), args));
            case "test" -> gradle(root, "test", concat(List.of("--continue"), args));
            case "targeted-test" -> gradle(root, "cpfVerifyTargeted", args);
            case "full-validation" -> runPython(root, root.resolve("cpf-tools/verification/tools/run-cpf-unified-validation.py"), concat(List.of("--root", root.toString()), args));
            case "run-batch" -> gradle(root, "cpfRunBatch", args);
            case "modules" -> gradle(root, "cpfModules", args);
            case "resource" -> gradle(root, "cpfResourcePolicy", args);
            case "db3" -> runPython(root, root.resolve("cpf-tools/db/tests/run_db3_lifecycle.py"), concat(List.of("--root", root.toString()), args));
            default -> usage("unsupported cpf dev command=" + sub);
        };
    }

    private static int internalVerify(Path root, List<String> args) throws Exception {
        String sub = args.isEmpty() ? "all" : normalize(args.remove(0));
        return switch (sub) {
            case "all" -> runPython(root, root.resolve("cpf-tools/verification/tools/run-cpf-canonical-verifiers.py"), concat(List.of("--root", root.toString()), args));
            case "generator" -> runPythonEngine(root, concat(List.of("--root", root.toString(), "verify", "generator"), args));
            case "generated" -> runPythonEngine(root, concat(List.of("--root", root.toString(), "verify", "all"), args));
            case "db3" -> runPython(root, root.resolve("cpf-tools/db/verification/verify-cpf-db-vendor-semantic-parity.py"), concat(List.of("--root", root.toString()), args));
            case "catalog" -> runPython(root, root.resolve("cpf-tools/verification/tools/verify-cpf-starter-catalog-truth.py"), args);
            case "ownership" -> runPython(root, root.resolve("cpf-tools/verification/tools/verify-cpf-owner-boundaries.py"), args);
            case "source" -> runPython(root, root.resolve("cpf-tools/verification/tools/cpf-source-state.py"), concat(List.of("--root", root.toString(), "--scope", "source"), args));
            default -> usage("unsupported cpf verify command=" + sub);
        };
    }

    private static int internalPublish(Path root, List<String> args) throws Exception {
        String sub = args.isEmpty() ? "framework" : normalize(args.remove(0));
        if (!"framework".equals(sub)) return usage("cpf publish framework");
        return gradle(root, "publish", args);
    }

    private static int internalRelease(Path root, List<String> args) throws Exception {
        if (args.isEmpty() || !"open-git".equals(normalize(args.remove(0)))) return usage("cpf release open-git [build|check|status]");
        String action = args.isEmpty() || args.get(0).startsWith("-") ? "build" : normalize(args.remove(0));
        if (!Set.of("build", "check", "status").contains(action)) return usage("unsupported open-git action=" + action);
        Path tool = root.resolve("cpf-tools/release/open-git/cpf_open_git.py");
        return runPython(root, tool, concat(List.of(action, "--root", root.toString()), args));
    }

    private static int internalBootstrap(Path root, List<String> args) throws Exception {
        if (!args.isEmpty()) return usage("cpf bootstrap does not accept Public DB options in INTERNAL profile");
        int source = runPython(root, root.resolve("cpf-tools/verification/tools/cpf-source-state.py"), List.of("--root", root.toString(), "--scope", "source"));
        if (source != 0) return source;
        int catalog = runPython(root, root.resolve("cpf-tools/runtime/cli/tools/generate-cpf-tooling-entrypoint-inventory.py"), List.of("--root", root.toString()));
        if (catalog != 0) return catalog;
        int verify = gradle(root, "cpfVerifyFast", List.of());
        if (verify == 0) System.out.println("CPF FRAMEWORK DEVELOPMENT READY");
        return verify;
    }

    private static int internalRuntime(Path root, String command, List<String> args) throws Exception {
        List<String> forwarded = new ArrayList<>(List.of(command, "--root", root.toString()));
        forwarded.addAll(args);
        return runPython(root, root.resolve("cpf-tools/runtime/tools/cpf_local_runtime.py"), forwarded);
    }

    private static int internalReset(Path root, List<String> args) throws Exception {
        boolean confirmed = args.stream().anyMatch(x -> "--confirm".equals(x) || "--confirm-local-reset".equals(x));
        if (!confirmed) return prerequisite("CPF-CLI-RESET-CONFIRM", "reset requires --confirm; local generated runtime state may be removed");
        return internalRuntime(root, "reset", List.of("--confirm"));
    }

    private static int runPythonEngine(Path root, List<String> args) throws Exception {
        Path engine = root.resolve("cpf-tools/runtime/cli/cpf.py");
        return runPython(root, engine, args);
    }

    private static int runPython(Path root, Path script, List<String> args) throws Exception {
        if (!Files.isRegularFile(script)) return prerequisite("CPF-CLI-ENGINE-MISSING", "Engine missing: " + script);
        String python = firstExecutable(isWindows() ? List.of("py", "python") : List.of("python3", "python"));
        if (python == null) return prerequisite("CPF-CLI-PYTHON-MISSING", "Python 3 is required by internal tooling engine");
        List<String> command = new ArrayList<>();
        command.add(python);
        if (isWindows() && "py".equalsIgnoreCase(Path.of(python).getFileName().toString())) command.add("-3");
        command.add(script.toString()); command.addAll(args);
        return runProcess(root, command, COMMAND_TIMEOUT);
    }

    private static int runTool(Path root, String relative, List<String> args) throws Exception {
        Path script = root.resolve(relative);
        if (!Files.isRegularFile(script)) return prerequisite("CPF-CLI-ENGINE-MISSING", "Engine missing: " + script);
        if (relative.endsWith(".ps1")) {
            String pwsh = firstExecutable(List.of("pwsh", "powershell"));
            if (pwsh == null) return prerequisite("CPF-CLI-POWERSHELL-MISSING", "PowerShell is required by this internal engine");
            List<String> command = new ArrayList<>(List.of(pwsh, "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", script.toString()));
            command.addAll(args); return runProcess(root, command, COMMAND_TIMEOUT);
        }
        return runPython(root, script, args);
    }

    private static int runClass(Path root, String className, List<String> args) throws Exception {
        Path jar = cliJar();
        List<String> command = new ArrayList<>(List.of(javaExecutable(), "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8", "-Dstderr.encoding=UTF-8", "-cp", jar.toString(), className));
        command.addAll(args);
        return runProcess(root, command, COMMAND_TIMEOUT);
    }

    private static int runProcess(Path root, List<String> command, Duration timeout) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command).directory(root.toFile()).redirectErrorStream(true);
        builder.environment().put("JAVA_TOOL_OPTIONS", appendUtf8(builder.environment().get("JAVA_TOOL_OPTIONS")));
        Process process = builder.start();
        Thread pump = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                for (String line; (line = reader.readLine()) != null;) System.out.println(line);
            } catch (IOException e) { fail("CPF-CLI-OUTPUT", e.getMessage()); }
        }, "cpf-cli-output");
        pump.setDaemon(true); pump.start();
        if (!process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS)) {
            process.destroy(); if (!process.waitFor(5, TimeUnit.SECONDS)) process.destroyForcibly(); pump.join(5000);
            fail("CPF-CLI-TIMEOUT", "timeoutSeconds=" + timeout.toSeconds()); return TIMEOUT;
        }
        pump.join(5000); return process.exitValue();
    }

    private static <T> int requireJava25Then(ThrowingIntSupplier call) throws Exception {
        if (Runtime.version().feature() != 25) return prerequisite("CPF-CLI-JAVA-VERSION", "Java 25 required, actual=" + Runtime.version());
        return call.getAsInt();
    }

    private static boolean internalEnabled() { return "INTERNAL".equals(CAPABILITY_PROFILE); }
    private static String sourceIdentity() {
        String override = System.getProperty("cpf.sourceIdentity", System.getenv("CPF_SOURCE_IDENTITY"));
        return override == null || override.isBlank() ? SOURCE_IDENTITY : override.trim();
    }
    private static Path workspaceRoot() throws Exception {
        String explicit = firstNonBlank(System.getenv("CPF_WORKSPACE"), System.getenv("CPF_WORKSPACE_ROOT"));
        if (explicit != null) return validateWorkspace(Path.of(explicit));
        Path jar = cliJar(); Path lib = jar.getParent();
        if (lib != null && lib.getParent() != null && lib.getParent().getParent() != null) {
            Path candidate = lib.getParent().getParent().toAbsolutePath().normalize();
            if (isWorkspace(candidate)) return candidate;
        }
        for (Path p = Path.of(System.getProperty("user.dir")).toAbsolutePath().normalize(); p != null; p = p.getParent()) if (isWorkspace(p)) return p;
        throw new IllegalStateException("CPF workspace root not found");
    }
    private static Path validateWorkspace(Path root) { Path n = root.toAbsolutePath().normalize(); if (!isWorkspace(n)) throw new IllegalArgumentException("invalid CPF workspace=" + n); return n; }
    private static boolean isWorkspace(Path p) { return Files.isRegularFile(p.resolve("settings.gradle")) && (Files.isDirectory(p.resolve("config")) || Files.isDirectory(p.resolve("cpf-tools"))); }
    private static Path cliJar() throws Exception {
        CodeSource cs = CpfCli.class.getProtectionDomain().getCodeSource(); if (cs == null) throw new IllegalStateException("CPF CLI code source unavailable");
        URI uri = cs.getLocation().toURI(); Path p = Path.of(uri).toAbsolutePath().normalize();
        if (Files.isDirectory(p)) { String v = System.getenv("CPF_CLI_JAR"); if (v == null || v.isBlank()) throw new IllegalStateException("CPF_CLI_JAR is required for class-directory execution"); return Path.of(v).toAbsolutePath().normalize(); }
        return p;
    }
    private static String javaExecutable() { return Path.of(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java").toString(); }
    private static String firstExecutable(List<String> names) { for (String n : names) { String path = findOnPath(n); if (path != null) return path; } return null; }
    private static String findOnPath(String name) {
        String path = System.getenv("PATH"); if (path == null) return null; String ext = isWindows() && !name.contains(".") ? ".exe" : "";
        for (String entry : path.split(java.util.regex.Pattern.quote(File.pathSeparator))) { Path p = Path.of(entry, name + ext); if (Files.isRegularFile(p)) return p.toString(); Path raw = Path.of(entry, name); if (Files.isRegularFile(raw)) return raw.toString(); }
        return null;
    }
    private static String appendUtf8(String current) { String flags = "-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8"; return current == null || current.isBlank() ? flags : current + " " + flags; }
    private static Properties loadBuildProperties() { Properties p = new Properties(); try (InputStream in = CpfCli.class.getResourceAsStream("/cpf-cli.properties")) { if (in != null) p.load(new InputStreamReader(in, StandardCharsets.UTF_8)); } catch (Exception ignored) {} return p; }
    private static String deriveSystemCode(String name) { String n = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT); return (n + "XXX").substring(0, 3); }
    private static String normalize(String v) { return v.trim().toLowerCase(Locale.ROOT).replace('_','-'); }
    private static List<String> concat(List<String> a, List<String> b) { List<String> out = new ArrayList<>(a); out.addAll(b); return out; }
    private static String firstNonBlank(String... values) { for (String v : values) if (v != null && !v.isBlank()) return v.trim(); return null; }
    private static boolean isWindows() { return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win"); }
    private static String sanitize(String v) { if (v == null) return "unknown"; return v.replaceAll("[\\r\\n\\t]+", " ").replaceAll("(?i)(password|secret|token|credential)=\\S+", "$1=***"); }
    private static int usage(String m) { fail("CPF-CLI-USAGE", m); System.err.println("Run 'cpf help' for usage."); return USAGE; }
    private static int prerequisite(String code, String m) { fail(code, m); return PREREQUISITE; }
    private static void fail(String code, String m) { System.err.println("CPF_CLI=FAIL code=" + code + " message=" + sanitize(m)); }
    private static void log(String phase, String detail) { System.out.println("[CPF][CLI] " + phase + " " + detail); }
    @FunctionalInterface private interface ThrowingIntSupplier { int getAsInt() throws Exception; }
}
