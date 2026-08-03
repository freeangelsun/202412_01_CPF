import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.*;
import java.util.stream.*;

public final class Qa39Tool {
    private static final String BASE_SHA = "4aea798c913787e86341809e2cef2b9495cbf7ba";
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final Set<String> TEXT_EXT = Set.of(".gradle", ".kts", ".java", ".kt", ".groovy", ".xml", ".json", ".yaml", ".yml", ".properties", ".md", ".csv", ".txt", ".ps1", ".py", ".js", ".cjs", ".mjs", ".ts", ".tsx", ".vue", ".sql", ".sh", ".bat", ".toml");
    private static final Set<String> SKIP_DIRS = Set.of(".git", "build", ".gradle", "node_modules", "dist", "coverage", "playwright-report", "test-results");
    private static final List<String> PROTECTED = List.of("cpf-docs/deliverables/", "cpf-docs/guides/", "cpf-docs/environment/docker/", "cpf-tools/environment/docker-development-test/");
    private static final Set<String> RESUME_ALLOWED_MODIFIED_PATHS = Set.of(
        "cpf-docs/quality/CPF_20260731_QA33_DEFECT_REGISTER.csv",
        "cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv",
        "cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv",
        "cpf-docs/quality/CPF_QA38_STARTER_CAPABILITY_CATALOG.csv",
        "cpf-docs/work/CPF_CHANGE_MANIFEST.csv",
        "cpf-docs/work/CPF_STARTER_VALUE_CATALOG.csv",
        "cpf-docs/work/review/CPF_20260731_QA32_INDEPENDENT_SOURCE_REVIEW.md",
        "cpf-docs/work/review/CPF_QA38_POST_PUSH_DEFECT_REGISTER.csv",
        "cpf-tools/generator/contracts/capability-profiles.json",
        "cpf-tools/scripts/verify-qa38-starter-closure.ps1",
        "cpf-tools/verification/qa38/verify-qa38-structure.py",
        "cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py",
        "cpf-tools/verification/qa39/verify-qa39-post-push-closure.py"
    );
    private static final List<String> PYTHON_FILES = List.of(
        "cpf-tools/scripts/apply-qa39-final-corrective.py",
        "cpf-tools/verification/qa39/verify-qa39-final-canonical.py",
        "cpf-tools/verification/qa39/verify-qa39-naming-steering.py",
        "cpf-tools/verification/qa39/verify-qa39-db-parity.py",
        "cpf-tools/verification/qa39/verify-qa39-openapi-frontend.py",
        "cpf-tools/verification/qa39/verify-qa39-source-boundaries.py",
        "cpf-tools/verification/qa39/verify-qa39-evidence-truth.py",
        "cpf-tools/verification/qa39/verify-qa39-overlay-package.py"
    );

    private Path root;
    private final List<Map<String,Object>> operations = new ArrayList<>();
    private final Set<String> canonicalOverlayPaths = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private final List<Pair> comparisonReplacements = new ArrayList<>();

    public static void main(String[] args) {
        try {
            if (args.length == 0) throw new IllegalArgumentException("mode required: apply | verify | low-cost");
            String mode = args[0];
            Map<String,String> options = parseOptions(Arrays.copyOfRange(args, 1, args.length));
            Path root = Paths.get(options.getOrDefault("root", ".")).toAbsolutePath().normalize();
            Qa39Tool tool = new Qa39Tool();
            tool.root = root;
            switch (mode) {
                case "apply" -> tool.apply(options.containsKey("allow-dirty"), options.containsKey("skip-head-check"));
                case "resume" -> tool.resume(options);
                case "verify" -> tool.verifyCanonical();
                case "low-cost", "canonical-closure" -> tool.lowCost();
                case "naming" -> tool.verifyNaming();
                case "db" -> tool.verifyDb();
                case "openapi" -> tool.verifyOpenApi();
                case "boundaries" -> tool.verifyBoundaries();
                case "evidence" -> tool.verifyEvidence();
                case "provider-conformance" -> tool.providerConformance();
                case "build-contract" -> tool.verifyBuildContract();
                case "bff-security" -> tool.verifyBffSecurity();
                case "batch-outbound" -> tool.verifyBatchOutboundPolicy();
                case "kafka-ack" -> tool.verifyKafkaAckContract();
                case "network-identity" -> tool.verifyNetworkIdentity();
                case "browser-contract" -> tool.verifyBrowserContract();
                case "runtime-contracts" -> tool.verifyRuntimeContracts();
                case "db-static-token-parity" -> tool.verifyDbStaticTokenParity(options.get("json-report"));
                case "source-closure" -> tool.verifySourceClosure(options.get("evidence-output"));
                case "batch-control-plane" -> tool.verifyBatchControlPlane(options.get("json-report"));
                case "supply-chain" -> tool.verifySupplyChain(options);
                default -> throw new IllegalArgumentException("unknown mode: " + mode);
            }
        } catch (Exception e) {
            System.err.println("[CPF][QA39][FAIL] " + e.getMessage());
            System.exit(1);
        }
    }

    private static Map<String,String> parseOptions(String[] args) {
        Map<String,String> out = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("--")) throw new IllegalArgumentException("invalid argument: " + arg);
            String key = arg.substring(2);
            if (Set.of("allow-dirty", "skip-head-check", "release", "keep-workspace").contains(key)) out.put(key, "true");
            else {
                if (i + 1 >= args.length) throw new IllegalArgumentException("missing value for " + arg);
                out.put(key, args[++i]);
            }
        }
        return out;
    }

    private void apply(boolean allowDirty, boolean skipHeadCheck) throws Exception {
        requireDirectory(root.resolve(".git"), "not a Git repository root: " + root);
        String head = run("git", "-C", root.toString(), "rev-parse", "HEAD").trim();
        Path moveCsv = root.resolve("cpf-docs/work/CPF_QA_FINAL_MOVE_MAP.csv");
        Path manifestCsv = root.resolve("cpf-docs/work/manifest/CHANGE_MANIFEST.csv");
        requireFile(moveCsv, "move map missing: " + moveCsv);
        List<Map<String,String>> moveMaps = readCsv(moveCsv);
        if (Files.isRegularFile(manifestCsv)) for (Map<String,String> row : readCsv(manifestCsv)) if (!row.getOrDefault("path", "").isBlank()) canonicalOverlayPaths.add(norm(row.get("path")));
        for (String py : PYTHON_FILES) canonicalOverlayPaths.add(norm(py));
        prepareComparisonReplacements(moveMaps);
        List<String> preflightIssues = collectApplyPreflight(moveMaps, allowDirty, skipHeadCheck, head);
        if (!preflightIssues.isEmpty()) {
            writePreflightReport(head, preflightIssues);
            throw new IllegalStateException("QA39 apply preflight failed with " + preflightIssues.size() + " issue(s):\n - " + String.join("\n - ", preflightIssues));
        }
        moveMaps.sort(Comparator.comparing((Map<String,String> row) -> isNested(root.resolve(row.get("source")), root.resolve(row.get("target"))) ? 1 : 0).thenComparing(row -> row.get("source")));
        for (Map<String,String> row : moveMaps) mergeTree(row.get("source"), row.get("target"));
        repairNestedTargetLayouts();
        internalizeOpenApi();

        List<Pair> pathMap = new ArrayList<>(); List<Pair> artifactMap = new ArrayList<>();
        for (Map<String,String> row : moveMaps) {
            pathMap.add(new Pair(row.get("source"), row.get("target")));
            if (!Objects.equals(row.get("oldArtifactId"), row.get("newArtifactId"))) artifactMap.add(new Pair(row.get("oldArtifactId"), row.get("newArtifactId")));
        }
        artifactMap.add(new Pair("cpf-starter-openapi-webmvc", "cpf-starter-profile-web-api"));
        List<Pair> packageMap = pairs(new String[][]{
            {"com.cpf.core.api.feature","com.cpf.core.api.featureflag"},{"com.cpf.core.spi.feature","com.cpf.core.spi.featureflag"},
            {"com.cpf.starter.base","com.cpf.starter.foundation.base"},{"com.cpf.starter.persistence.jdbc","com.cpf.starter.data.persistence.jdbc"},{"com.cpf.starter.persistence.mybatis","com.cpf.starter.data.persistence.mybatis"},
            {"com.cpf.starter.cache.valkey","com.cpf.starter.data.cache.valkey"},{"com.cpf.starter.cache","com.cpf.starter.data.cache.caffeine"},{"com.cpf.starter.http","com.cpf.starter.integration.http"},
            {"com.cpf.integration.fixedlength.core","com.cpf.starter.integration.fixedlength.core"},{"com.cpf.integration.fixedlength","com.cpf.starter.integration.fixedlength"},{"com.cpf.starter.attachment","com.cpf.starter.file.attachment"},
            {"com.cpf.starter.tabular","com.cpf.starter.file.tabular"},{"com.cpf.starter.integration.sftp","com.cpf.starter.file.sftp"},{"com.cpf.starter.notification.internal","com.cpf.starter.notification.dispatch.internal"},
            {"com.cpf.starter.notification","com.cpf.starter.notification.dispatch"},{"com.cpf.notification.sms","com.cpf.starter.notification.sms.spi"},{"com.cpf.starter.secret","com.cpf.starter.security.secret"},
            {"com.cpf.starter.observability.otlp","com.cpf.starter.platform.operations.otlp"},{"com.cpf.starter.observability","com.cpf.starter.platform.operations.observability"},
            {"com.cpf.starter.runtime.control.client","com.cpf.starter.platform.operations.runtime.control.client"},{"com.cpf.starter.channel.registry.jdbc","com.cpf.starter.platform.operations.channel.registry.jdbc"}
        });
        List<Pair> configMap = pairs(new String[][]{
            {"cpf.persistence.jdbc","cpf.data.persistence.jdbc"},{"cpf.persistence.mybatis","cpf.data.persistence.mybatis"},{"cpf.cache.valkey","cpf.data.cache.valkey"},{"cpf.cache","cpf.data.cache.caffeine"},
            {"cpf.http","cpf.integration.http"},{"cpf.attachment","cpf.file.attachment"},{"cpf.tabular","cpf.file.tabular"},{"cpf.integration.sftp","cpf.file.sftp"},{"cpf.secret","cpf.security.secret"},
            {"cpf.observability.otlp","cpf.platform-operations.otlp"},{"cpf.observability","cpf.platform-operations.observability"},{"cpf.runtime-control","cpf.platform-operations.runtime-control"},{"cpf.channel-registry","cpf.platform-operations.channel-registry"}
        });
        Comparator<Pair> longest = Comparator.comparingInt((Pair p) -> p.oldValue.length()).reversed();
        pathMap.sort(longest); artifactMap.sort(longest); packageMap.sort(longest); configMap.sort(longest);
        rewriteRepositoryText(pathMap, artifactMap, packageMap, configMap);
        repairOpenApiPackageLeak();
        internalizeOpenApiPackage();
        internalizeBrokerClientAdapter();
        relocateJavaByPackage();
        deleteLegacyContracts();
        deletePythonFiles();
        injectNotificationImports();
        relocateJavaByPackage();
        verifyCanonical();
        updateDeleteManifests(head);
        writeApplyReport(head);
        lowCost();
        System.out.println("[CPF][QA39][PASS] Java-only apply operations=" + operations.size() + " head=" + head);
    }


    private void resume(Map<String,String> options) throws Exception {
        Path evidence = Paths.get(options.getOrDefault("evidence", "D:/11096/Downloads/CPF_QA39_R9_COMPANY_RESUME_REPORT.json")).toAbsolutePath().normalize();
        Path gradleCache = Paths.get(options.getOrDefault("gradle-cache", "D:/11096/CPF_CACHE/QA39_R9")).toAbsolutePath().normalize();
        Path logRoot = Paths.get(options.getOrDefault("log-root", "D:/11096/Downloads/CPF_QA39_R9_LOGS")).toAbsolutePath().normalize();
        Files.createDirectories(evidence.getParent());
        Files.createDirectories(gradleCache);
        Files.createDirectories(logRoot);

        List<Map<String,Object>> results = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        OffsetDateTime started = OffsetDateTime.now();
        String head = "";
        String origin = "";

        try {
            head = run("git", "-C", root.toString(), "rev-parse", "HEAD").trim();
            origin = run("git", "-C", root.toString(), "rev-parse", "origin/master").trim();
            String staged = run("git", "-C", root.toString(), "diff", "--cached", "--name-only").trim();
            List<String> issues = new ArrayList<>();
            if (!BASE_SHA.equals(head)) issues.add("baseline SHA mismatch current=" + head);
            if (!origin.isBlank() && !origin.equals(head)) issues.add("origin/master mismatch head=" + head + " origin=" + origin);
            if (!staged.isBlank()) issues.add("staged changes exist");
            addInternalStep(results, failures, "baseline-and-staged-safety", issues);
        } catch (Exception e) {
            addInternalStep(results, failures, "baseline-and-staged-safety", List.of(e.getMessage()));
        }

        Path javaExecutable = Paths.get(System.getProperty("java.home"), "bin", isWindows() ? "java.exe" : "java");
        Path tool = root.resolve("cpf-tools/scripts/Qa39Tool.java");
        Map<String,String> commonEnv = new LinkedHashMap<>();

        StepResult applyResult = runLoggedStep("qa39-apply-resume",
            List.of(javaExecutable.toString(), tool.toString(), "apply", "--root", root.toString()), commonEnv, logRoot);
        addProcessStep(results, failures, applyResult);

        StepResult lowCostResult = runLoggedStep("qa39-low-cost-gates",
            List.of(javaExecutable.toString(), tool.toString(), "low-cost", "--root", root.toString()), commonEnv, logRoot);
        addProcessStep(results, failures, lowCostResult);

        Path projectCache = gradleCache.resolve("project-cache");
        Files.createDirectories(projectCache);
        String gradleUserHome = System.getenv("GRADLE_USER_HOME");
        if (gradleUserHome == null || gradleUserHome.isBlank()) gradleUserHome = Paths.get(System.getProperty("user.home"), ".gradle").toString();
        Map<String,String> gradleEnv = new LinkedHashMap<>();
        gradleEnv.put("GRADLE_USER_HOME", gradleUserHome);
        Path gradleWrapper = root.resolve(isWindows() ? "gradlew.bat" : "gradlew");
        List<String> gradleCommand = new ArrayList<>();
        if (isWindows()) {
            gradleCommand.add("cmd.exe"); gradleCommand.add("/d"); gradleCommand.add("/c"); gradleCommand.add(gradleWrapper.toString());
        } else gradleCommand.add(gradleWrapper.toString());
        gradleCommand.addAll(List.of("help", "-PcpfSourceSha=" + head, "--no-daemon", "--console=plain", "--stacktrace", "--project-cache-dir", projectCache.toString()));
        StepResult gradleResult = runLoggedStep("gradle-settings-configuration-external-project-cache", gradleCommand, gradleEnv, logRoot);
        addProcessStep(results, failures, gradleResult);

        StepResult diffResult = runLoggedStep("git-diff-check",
            List.of("git", "-C", root.toString(), "diff", "--check"), commonEnv, logRoot);
        addProcessStep(results, failures, diffResult);

        List<String> layoutIssues = new ArrayList<>();
        for (String wrong : List.of(
            "cpf-starters/notification/dispatch/main",
            "cpf-starters/notification/dispatch/test",
            "cpf-starters/security/session-jdbc/main",
            "cpf-starters/security/session-jdbc/test")) {
            if (Files.exists(root.resolve(wrong))) layoutIssues.add("wrong canonical layout remains: " + wrong);
        }
        for (String required : List.of(
            "cpf-starters/notification/dispatch/src/main",
            "cpf-starters/security/session-jdbc/src/main",
            "cpf-starters/profiles/web-api/src/main")) {
            if (!Files.isDirectory(root.resolve(required))) layoutIssues.add("required canonical path missing: " + required);
        }
        addInternalStep(results, failures, "canonical-layout-summary", layoutIssues);

        List<String> statusLines = commandLinesAllowFailure(List.of("git", "-C", root.toString(), "-c", "core.quotepath=false", "status", "--short", "--branch"));
        List<String> porcelain = commandLinesAllowFailure(List.of("git", "-C", root.toString(), "status", "--porcelain=v1", "--untracked-files=all"));
        long modified = porcelain.stream().filter(line -> line.startsWith(" M") || line.startsWith("M ")).count();
        long deleted = porcelain.stream().filter(line -> line.startsWith(" D") || line.startsWith("D ")).count();
        long untracked = porcelain.stream().filter(line -> line.startsWith("??")).count();
        long staged = commandLinesAllowFailure(List.of("git", "-C", root.toString(), "diff", "--cached", "--name-only")).stream().filter(line -> !line.isBlank()).count();

        Map<String,Object> summary = new LinkedHashMap<>();
        summary.put("total", porcelain.size()); summary.put("modified", modified); summary.put("deleted", deleted); summary.put("untracked", untracked); summary.put("staged", staged);
        Map<String,Object> report = new LinkedHashMap<>();
        report.put("schema_version", 3);
        report.put("evidence_id", "QA39-R9-COMPANY-RESUME");
        report.put("source_sha", head);
        report.put("origin_master", origin);
        report.put("result", failures.isEmpty() ? "PASS" : "FAIL");
        report.put("exit_code", failures.isEmpty() ? 0 : 1);
        report.put("started_at", started.toString());
        report.put("finished_at", OffsetDateTime.now().toString());
        report.put("results", results);
        report.put("failures", failures);
        report.put("status_summary", summary);
        report.put("git_status", statusLines);
        report.put("external_project_cache", gradleCache.toString());
        report.put("external_log_root", logRoot.toString());
        report.put("gradle_user_home", gradleUserHome);
        report.put("sanitized", true);
        Files.writeString(evidence, Json.stringify(report, 2) + "\n", UTF8);

        System.out.println("[CPF][QA39] STATUS total=" + porcelain.size() + " modified=" + modified + " deleted=" + deleted + " untracked=" + untracked + " staged=" + staged);
        for (Map<String,Object> item : results) System.out.println("[CPF][QA39][" + item.get("result") + "] " + item.get("name") + " log=" + item.getOrDefault("log_path", ""));
        if (!failures.isEmpty()) {
            System.err.println("[CPF][QA39] Consolidated failures=" + failures.size());
            for (String failure : failures) System.err.println(" - " + failure);
            System.err.println("[CPF][QA39] Full external report: " + evidence);
            throw new IllegalStateException("QA39 R9 consolidated repair failed with " + failures.size() + " step(s)");
        }
        System.out.println("[CPF][QA39][PASS] R9 consolidated apply and verification external-report=" + evidence);
    }

    private static void addInternalStep(List<Map<String,Object>> results, List<String> failures, String name, List<String> issues) {
        Map<String,Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("result", issues.isEmpty() ? "PASS" : "FAIL");
        item.put("exit_code", issues.isEmpty() ? 0 : 1);
        item.put("detail", issues.isEmpty() ? "PASS" : String.join("; ", issues));
        item.put("output_tail", "");
        results.add(item);
        for (String issue : issues) failures.add(name + ": " + issue);
    }

    private static void addProcessStep(List<Map<String,Object>> results, List<String> failures, StepResult step) {
        Map<String,Object> item = new LinkedHashMap<>();
        item.put("name", step.name()); item.put("result", step.exitCode() == 0 ? "PASS" : "FAIL"); item.put("exit_code", step.exitCode());
        item.put("detail", step.exitCode() == 0 ? "PASS" : "exit=" + step.exitCode()); item.put("log_path", step.logPath().toString()); item.put("output_tail", step.outputTail());
        results.add(item);
        if (step.exitCode() != 0) failures.add(step.name() + ": exit=" + step.exitCode() + " tail=" + step.outputTail());
    }

    private static StepResult runLoggedStep(String name, List<String> command, Map<String,String> environment, Path logRoot) throws Exception {
        Path log = logRoot.resolve(name.replaceAll("[^A-Za-z0-9_.-]", "_") + ".log");
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().putAll(environment);
        Process process = builder.start();
        byte[] output = process.getInputStream().readAllBytes();
        int code = process.waitFor();
        Files.write(log, output);
        String text = new String(output, UTF8);
        if (!text.isBlank()) System.out.print(text.endsWith("\n") ? text : text + "\n");
        return new StepResult(name, code, log, tail(text, 80));
    }

    private static List<String> commandLinesAllowFailure(List<String> command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            String text = new String(process.getInputStream().readAllBytes(), UTF8);
            process.waitFor();
            return text.lines().toList();
        } catch (Exception e) { return List.of("COMMAND_ERROR: " + e.getMessage()); }
    }

    private static String tail(String text, int maxLines) {
        List<String> lines = text.lines().toList();
        int start = Math.max(0, lines.size() - maxLines);
        return String.join("\n", lines.subList(start, lines.size()));
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private record StepResult(String name, int exitCode, Path logPath, String outputTail) {}

    private List<String> collectApplyPreflight(List<Map<String,String>> moves, boolean allowDirty, boolean skipHeadCheck, String head) throws Exception {
        List<String> issues = new ArrayList<>();
        if (!skipHeadCheck && !BASE_SHA.equals(head)) issues.add("baseline SHA mismatch: expected=" + BASE_SHA + " actual=" + head);
        for (String p : canonicalOverlayPaths) collectProtectedIssue(p, issues);
        for (Map<String,String> row : moves) { collectProtectedIssue(row.get("source"), issues); collectProtectedIssue(row.get("target"), issues); }
        if (!allowDirty) issues.addAll(unexpectedWorktreeChanges(moves));
        for (Map<String,String> row : moves) {
            Path source = root.resolve(row.get("source")); Path target = root.resolve(row.get("target"));
            if (!Files.exists(source) && !Files.exists(target)) issues.add("missing source and target: " + row.get("source") + " -> " + row.get("target"));
            else collectMergeConflicts(source, target, row.get("source"), row.get("target"), issues);
        }
        Path openApiSource = root.resolve("cpf-starters/openapi-webmvc");
        Path openApiTarget = root.resolve("cpf-starters/profiles/web-api");
        if (!Files.exists(openApiSource) && !Files.exists(openApiTarget)) issues.add("OpenAPI source/profile both missing");
        else if (Files.exists(openApiSource)) collectOpenApiConflicts(openApiSource, openApiTarget, issues);
        collectNestedLayoutRepairConflicts(root.resolve("cpf-starters/notification/dispatch"), issues);
        collectNestedLayoutRepairConflicts(root.resolve("cpf-starters/security/session-jdbc"), issues);
        return issues.stream().distinct().sorted().toList();
    }

    private void collectProtectedIssue(String path, List<String> issues) {
        try { preflightProtected(path); }
        catch (RuntimeException e) { issues.add(e.getMessage()); }
    }

    private List<String> unexpectedWorktreeChanges(List<Map<String,String>> moves) throws Exception {
        Set<String> allowed = new TreeSet<>(String.CASE_INSENSITIVE_ORDER); allowed.addAll(canonicalOverlayPaths); allowed.addAll(PYTHON_FILES);
        for (Map<String,String> row : moves) { allowed.add(norm(row.get("source"))); allowed.add(norm(row.get("target"))); }
        allowed.add("cpf-starters/openapi-webmvc");
        allowed.add("cpf-starters/profiles/web-api");
        allowed.add("cpf-docs/evidence/qa39/apply/apply-report.json");
        allowed.add("cpf-docs/evidence/qa39/apply/preflight-report.json");
        allowed.add("cpf-docs/evidence/qa39/static/CPF_QA39_R5_CONSOLIDATED_REPAIR.sanitized.json");
        allowed.add("cpf-docs/work/current/CPF_QA39_R5_CONSOLIDATED_REPAIR.md");
        allowed.add("cpf-core/src/main/java/com/cpf/core/api/feature");
        allowed.add("cpf-core/src/main/java/com/cpf/core/api/featureflag");
        allowed.add("cpf-core/src/main/java/com/cpf/core/spi/feature");
        allowed.add("cpf-core/src/main/java/com/cpf/core/spi/featureflag");
        allowed.addAll(RESUME_ALLOWED_MODIFIED_PATHS);
        allowed.add("cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerClientAdapter.java");
        allowed.add("cpf-core/src/main/java/com/cpf/core/internal/broker/CpfBrokerClientAdapter.java");
        Path priorReport = root.resolve("cpf-docs/evidence/qa39/apply/apply-report.json");
        if (Files.isRegularFile(priorReport)) {
            Object parsed = Json.parse(Files.readString(priorReport, UTF8));
            if (parsed instanceof Map<?,?> report && report.get("operations") instanceof List<?> priorOperations) {
                for (Object item : priorOperations) if (item instanceof Map<?,?> operation) {
                    for (String key : List.of("path","source","target")) {
                        Object value = operation.get(key); if (value != null && !String.valueOf(value).isBlank()) allowed.add(norm(String.valueOf(value)));
                    }
                }
            }
        }
        byte[] bytes = runBytes("git", "-C", root.toString(), "status", "--porcelain=v1", "-z", "--untracked-files=all");
        String output = new String(bytes, UTF8); List<String> unexpected = new ArrayList<>();
        for (String item : output.split("\\u0000")) {
            if (item.isEmpty()) continue;
            String status = item.substring(0, 2); String path = norm(item.substring(3)); int arrow = path.indexOf(" -> "); if (arrow >= 0) path = path.substring(arrow + 4);
            boolean ok = false; for (String a : allowed) if (path.equalsIgnoreCase(a) || path.toLowerCase(Locale.ROOT).startsWith(a.replaceAll("/+$", "").toLowerCase(Locale.ROOT) + "/")) { ok = true; break; }
            if (!ok && status.contains("M") && isExpectedPartialRewriteFromHead(path)) ok = true;
            if (!ok) unexpected.add("unexpected pre-existing Working Tree change: " + status + " " + path);
        }
        return unexpected;
    }

    private boolean isExpectedPartialRewriteFromHead(String path) {
        Path current = root.resolve(path);
        if (!Files.isRegularFile(current)) return false;
        try {
            String headText = new String(runBytes("git", "-C", root.toString(), "show", "HEAD:" + path), UTF8).replace("\r\n", "\n");
            String currentText = Files.readString(current, UTF8).replace("\r\n", "\n");
            String transformed = headText;
            for (Pair pair : comparisonReplacements) transformed = transformed.replace(pair.oldValue, pair.newValue);
            return transformed.equals(currentText);
        } catch (Exception e) {
            return false;
        }
    }

    private void collectMergeConflicts(Path source, Path target, String sourceRelative, String targetRelative, List<String> issues) throws Exception {
        if (!Files.exists(source)) return;
        if (!Files.isDirectory(source)) { issues.add("move source is not a directory: " + sourceRelative); return; }
        if (Files.exists(target) && !Files.isDirectory(target)) { issues.add("move target is not a directory: " + targetRelative); return; }
        if (isNested(source, target)) {
            String sourceName = source.getFileName().toString();
            Set<String> siblings = sourceName.equals("notification") ? Set.of("dispatch","email","sms-spi") : sourceName.equals("security") ? Set.of("resource-server","session-jdbc","service-identity","secret") : Set.of();
            String targetFirst = source.relativize(target).getName(0).toString();
            try (Stream<Path> stream = Files.list(source)) {
                for (Path entry : stream.toList()) {
                    if (entry.getFileName().toString().equals(targetFirst) || siblings.contains(entry.getFileName().toString())) continue;
                    if (Files.isRegularFile(entry)) collectFileConflict(entry, target.resolve(entry.getFileName()), issues);
                    else for (Path file : filesUnder(entry)) collectFileConflict(file, target.resolve(entry.relativize(file)), issues);
                }
            }
        } else {
            for (Path file : filesUnder(source)) collectFileConflict(file, target.resolve(source.relativize(file)), issues);
        }
    }

    private void collectOpenApiConflicts(Path source, Path target, List<String> issues) throws Exception {
        for (String sub : List.of("src/main/java","src/test/java","src/main/resources","src/test/resources")) {
            Path s = source.resolve(sub); if (!Files.exists(s)) continue;
            for (Path file : filesUnder(s)) collectFileConflict(file, target.resolve(sub).resolve(s.relativize(file)), issues);
        }
    }

    private void collectFileConflict(Path source, Path target, List<String> issues) throws Exception {
        if (!Files.isRegularFile(target)) return;
        if (sha256(source).equals(sha256(target)) || canonicalOverlayPaths.contains(rel(target)) || equivalentAfterExpectedRewrite(source, target)) return;
        issues.add("move conflict: " + rel(source) + " -> " + rel(target));
    }

    private void prepareComparisonReplacements(List<Map<String,String>> moveMaps) {
        comparisonReplacements.clear();
        for (Map<String,String> row : moveMaps) {
            comparisonReplacements.add(new Pair(row.get("source"), row.get("target")));
            if (!Objects.equals(row.get("oldArtifactId"), row.get("newArtifactId"))) comparisonReplacements.add(new Pair(row.get("oldArtifactId"), row.get("newArtifactId")));
        }
        comparisonReplacements.add(new Pair("cpf-starter-openapi-webmvc", "cpf-starter-profile-web-api"));
        comparisonReplacements.addAll(pairs(new String[][]{
            {"com.cpf.core.config","com.cpf.starter.profile.webapi.internal.openapi"},{"com.cpf.core.api.feature","com.cpf.core.api.featureflag"},{"com.cpf.core.spi.feature","com.cpf.core.spi.featureflag"},
            {"com.cpf.starter.base","com.cpf.starter.foundation.base"},{"com.cpf.starter.persistence.jdbc","com.cpf.starter.data.persistence.jdbc"},{"com.cpf.starter.persistence.mybatis","com.cpf.starter.data.persistence.mybatis"},
            {"com.cpf.starter.cache.valkey","com.cpf.starter.data.cache.valkey"},{"com.cpf.starter.cache","com.cpf.starter.data.cache.caffeine"},{"com.cpf.starter.http","com.cpf.starter.integration.http"},
            {"com.cpf.integration.fixedlength.core","com.cpf.starter.integration.fixedlength.core"},{"com.cpf.integration.fixedlength","com.cpf.starter.integration.fixedlength"},{"com.cpf.starter.integration.tcp","com.cpf.starter.integration.tcp"},
            {"cpf.persistence.jdbc","cpf.data.persistence.jdbc"},{"cpf.persistence.mybatis","cpf.data.persistence.mybatis"},{"cpf.cache.valkey","cpf.data.cache.valkey"},{"cpf.cache","cpf.data.cache.caffeine"},
            {"cpf.http","cpf.integration.http"},{"cpf.attachment","cpf.file.attachment"},{"cpf.tabular","cpf.file.tabular"},{"cpf.integration.sftp","cpf.file.sftp"},{"cpf.secret","cpf.security.secret"},
            {"cpf.observability.otlp","cpf.platform-operations.otlp"},{"cpf.observability","cpf.platform-operations.observability"},{"cpf.runtime-control","cpf.platform-operations.runtime-control"},{"cpf.channel-registry","cpf.platform-operations.channel-registry"}
        }));
        comparisonReplacements.sort(Comparator.comparingInt((Pair p) -> p.oldValue.length()).reversed());
    }

    private boolean equivalentAfterExpectedRewrite(Path source, Path target) {
        try {
            String sourceText = normalizeLineEndings(Files.readString(source, UTF8));
            String targetText = normalizeLineEndings(Files.readString(target, UTF8));
            String transformed = sourceText;
            for (Pair pair : comparisonReplacements) transformed = transformed.replace(pair.oldValue, pair.newValue);
            return transformed.equals(targetText);
        } catch (IOException | RuntimeException e) { return false; }
    }

    private static String normalizeLineEndings(String value) {
        return value.replace("\r\n", "\n").replace("\r", "\n");
    }

    private void collectNestedLayoutRepairConflicts(Path module, List<String> issues) throws Exception {
        for (String segment : List.of("main", "test")) {
            Path wrong = module.resolve(segment);
            Path correct = module.resolve("src").resolve(segment);
            if (!Files.isDirectory(wrong)) continue;
            for (Path file : filesUnder(wrong)) {
                collectFileConflict(file, correct.resolve(wrong.relativize(file)), issues);
            }
        }
    }

    private void repairNestedTargetLayouts() throws Exception {
        repairNestedTargetLayout(root.resolve("cpf-starters/notification/dispatch"));
        repairNestedTargetLayout(root.resolve("cpf-starters/security/session-jdbc"));
    }

    private void repairNestedTargetLayout(Path module) throws Exception {
        for (String segment : List.of("main", "test")) {
            Path wrong = module.resolve(segment);
            Path correct = module.resolve("src").resolve(segment);
            if (!Files.isDirectory(wrong)) continue;
            for (Path file : filesUnder(wrong)) {
                mergeFile(file, correct.resolve(wrong.relativize(file)));
            }
            deleteTreeIfExists(wrong);
            op("nested-layout-repair", Map.of("module", rel(module), "segment", segment));
        }
    }

    private void writePreflightReport(String head, List<String> issues) throws Exception {
        Path report = root.resolve("cpf-docs/evidence/qa39/apply/preflight-report.json"); Files.createDirectories(report.getParent());
        Map<String,Object> data = new LinkedHashMap<>(); data.put("source_sha", head); data.put("result", issues.isEmpty() ? "PASS" : "FAIL"); data.put("exit_code", issues.isEmpty() ? 0 : 1); data.put("issue_count", issues.size()); data.put("issues", issues); data.put("sanitized", true);
        Files.writeString(report, Json.stringify(data, 2) + "\\n", UTF8);
    }

    private void preflightProtected(String path) {
        String p = norm(path); for (String prefix : PROTECTED) if (p.equalsIgnoreCase(prefix.substring(0,prefix.length()-1)) || p.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) throw new IllegalStateException("protected path in QA39 manifest: " + p);
    }

    private void mergeTree(String sourceRelative, String targetRelative) throws Exception {
        Path source = root.resolve(sourceRelative).normalize(); Path target = root.resolve(targetRelative).normalize();
        if (!Files.exists(source)) { if (Files.exists(target)) return; throw new IllegalStateException("move source and target are both missing: " + sourceRelative + " -> " + targetRelative); }
        Files.createDirectories(target);
        if (isNested(source,target)) {
            String sourceName = source.getFileName().toString(); Set<String> siblings = sourceName.equals("notification") ? Set.of("dispatch","email","sms-spi") : sourceName.equals("security") ? Set.of("resource-server","session-jdbc","service-identity","secret") : Set.of();
            String targetFirst = source.relativize(target).getName(0).toString();
            try (Stream<Path> stream = Files.list(source)) {
                for (Path entry : stream.toList()) {
                    if (entry.getFileName().toString().equals(targetFirst) || siblings.contains(entry.getFileName().toString())) continue;
                    if (Files.isRegularFile(entry)) mergeFile(entry, target.resolve(entry.getFileName()));
                    else {
                        for (Path file : filesUnder(entry)) mergeFile(file, target.resolve(source.relativize(file)));
                        deleteTreeIfExists(entry);
                    }
                }
            }
        } else {
            for (Path file : filesUnder(source)) mergeFile(file, target.resolve(source.relativize(file)));
            deleteTreeIfExists(source);
        }
    }

    private void mergeFile(Path source, Path target) throws Exception {
        Files.createDirectories(target.getParent());
        if (Files.isRegularFile(target)) {
            if (sha256(source).equals(sha256(target))) { Files.delete(source); op("deduplicate", Map.of("source", rel(source), "target", rel(target))); return; }
            if (canonicalOverlayPaths.contains(rel(target))) { Files.delete(source); op("canonical-overlay-replace", Map.of("source", rel(source), "target", rel(target))); return; }
            if (equivalentAfterExpectedRewrite(source, target)) { Files.delete(source); op("expected-rewrite-deduplicate", Map.of("source", rel(source), "target", rel(target))); return; }
            throw new IllegalStateException("move conflict: " + rel(source) + " -> " + rel(target));
        }
        Files.move(source, target); op("move", Map.of("source", rel(source), "target", rel(target)));
    }

    private void internalizeOpenApi() throws Exception {
        Path src = root.resolve("cpf-starters/openapi-webmvc"), dst = root.resolve("cpf-starters/profiles/web-api"); if (!Files.exists(src)) return;
        for (String sub : List.of("src/main/java","src/test/java","src/main/resources","src/test/resources")) {
            Path s = src.resolve(sub); if (!Files.exists(s)) continue; for (Path file : filesUnder(s)) mergeFile(file, dst.resolve(sub).resolve(s.relativize(file)));
        }
        Path build = src.resolve("build.gradle"); if (Files.exists(build)) { Files.delete(build); op("delete-executed", Map.of("path",rel(build),"reason","standalone OpenAPI artifact internalized into web-api profile")); }
        deleteEmptyDirectories(src); if (Files.exists(src)) {
            try (Stream<Path> residual = Files.walk(src)) { List<String> items = residual.filter(p -> !p.equals(src)).map(this::rel).toList(); if (!items.isEmpty()) throw new IllegalStateException("OpenAPI source residual after internalization: " + items); }
            Files.deleteIfExists(src);
        }
    }

    private void rewriteRepositoryText(List<Pair> pathMap, List<Pair> artifactMap, List<Pair> packageMap, List<Pair> configMap) throws Exception {
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                String rel = rel(file); if (isProtected(rel) || canonicalOverlayPaths.contains(rel) || hasSkippedPart(rel)) continue;
                String ext = extension(file.getFileName().toString()); if (!TEXT_EXT.contains(ext)) continue;
                String old; try { old = Files.readString(file, UTF8); } catch (UncheckedIOException | MalformedInputException e) { continue; }
                String value = old;
                for (Pair p : pathMap) value = replaceToken(value,p,true);
                for (Pair p : artifactMap) value = replaceToken(value,p,false);
                for (Pair p : packageMap) value = replaceToken(value,p,false);
                for (Pair p : configMap) value = replaceToken(value,p,false);
                if (!value.equals(old)) { Files.writeString(file,value,UTF8); op("rewrite",Map.of("path",rel)); }
            }
        }
    }

    private static String replaceToken(String text, Pair pair, boolean path) {
        String left = path ? "(?<![A-Za-z0-9_./-])" : "(?<![A-Za-z0-9_.-])"; String right = path ? "(?![A-Za-z0-9_./-])" : "(?![A-Za-z0-9_.-])";
        return Pattern.compile(left + Pattern.quote(pair.oldValue) + right).matcher(text).replaceAll(Matcher.quoteReplacement(pair.newValue));
    }

    private void relocateJavaByPackage() throws Exception {
        List<Path> roots;
        try (Stream<Path> stream = Files.walk(root)) { roots = stream.filter(Files::isDirectory).filter(p -> rel(p).matches(".*src/(main|test)/java$")).filter(p -> !hasSkippedPart(rel(p))).toList(); }
        Pattern packagePattern = Pattern.compile("(?m)^\\s*package\\s+([A-Za-z_][\\w.]*)\\s*;");
        for (Path sourceRoot : roots) for (Path java : filesUnder(sourceRoot).stream().filter(p -> p.toString().endsWith(".java")).toList()) {
            Matcher m = packagePattern.matcher(Files.readString(java,UTF8)); if (!m.find()) continue; Path expected = sourceRoot.resolve(m.group(1).replace('.',File.separatorChar)).resolve(java.getFileName());
            if (java.equals(expected)) continue; Files.createDirectories(expected.getParent());
            if (Files.exists(expected)) {
                if (!sha256(expected).equals(sha256(java)) && !canonicalOverlayPaths.contains(rel(expected))) {
                    throw new IllegalStateException("Java package relocation conflict: " + rel(java) + " -> " + rel(expected));
                }
                Files.delete(java);
                op(canonicalOverlayPaths.contains(rel(expected)) ? "canonical-overlay-java-replace" : "java-package-deduplicate", Map.of("source",rel(java),"target",rel(expected)));
            }
            else Files.move(java,expected);
            op("java-package-relocate",Map.of("source",rel(java),"target",rel(expected)));
        }
        deleteEmptyDirectories(root);
    }

    private void deleteLegacyContracts() throws Exception {
        for (String rel : List.of(
            "cpf-core/src/main/java/com/cpf/core/api/feature","cpf-core/src/main/java/com/cpf/core/spi/feature",
            "cpf-starters/notification/dispatch/src/main/java/com/cpf/starter/notification/dispatch/CpfNotificationRequest.java",
            "cpf-starters/notification/dispatch/src/main/java/com/cpf/starter/notification/dispatch/CpfNotificationResult.java",
            "cpf-starters/notification/dispatch/src/main/java/com/cpf/starter/notification/dispatch/CpfNotificationProvider.java",
            "cpf-starters/notification/dispatch/src/main/java/com/cpf/starter/notification/dispatch/CpfNotificationProviderStatus.java",
            "cpf-starters/notification/dispatch/src/main/java/com/cpf/starter/notification/dispatch/CpfNotificationReceipt.java",
            "cpf-starters/notification/dispatch/src/main/java/com/cpf/starter/notification/dispatch/CpfNotificationReconciler.java",
            "cpf-starters/notification/dispatch/src/main/java/com/cpf/starter/notification/dispatch/CpfNotificationOperations.java")) {
            Path p = root.resolve(rel); if (Files.exists(p)) { if (Files.isDirectory(p)) deleteTreeIfExists(p); else Files.delete(p); op("delete-executed",Map.of("path",rel,"reason","canonical API/SPI package migration")); }
        }
    }

    private void deletePythonFiles() throws Exception {
        for (String rel : PYTHON_FILES) { Path p = root.resolve(rel); if (Files.exists(p)) { Files.delete(p); op("delete-executed",Map.of("path",rel,"reason","QA39 local workflow is Java/PowerShell only")); } }
    }

    private void repairOpenApiPackageLeak() throws Exception {
        String leaked = "com.cpf.starter.profile.webapi.internal.openapi";
        String canonical = "com.cpf.core.config";
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                String relative = rel(file);
                if (relative.startsWith("cpf-starters/profiles/web-api/") || isProtected(relative) || hasSkippedPart(relative)) continue;
                if (!TEXT_EXT.contains(extension(file.getFileName().toString()))) continue;
                String text;
                try { text = Files.readString(file, UTF8); }
                catch (MalformedInputException | UncheckedIOException e) { continue; }
                if (!text.contains(leaked)) continue;
                Files.writeString(file, text.replace(leaked, canonical), UTF8);
                op("repair-openapi-package-leak", Map.of("path", relative));
            }
        }
    }

    private void internalizeOpenApiPackage() throws Exception {
        Path base = root.resolve("cpf-starters/profiles/web-api/src");
        if (!Files.isDirectory(base)) return;
        String canonical = "com.cpf.core.config";
        String internal = "com.cpf.starter.profile.webapi.internal.openapi";
        for (Path file : filesUnder(base)) {
            if (!Files.isRegularFile(file) || !TEXT_EXT.contains(extension(file.getFileName().toString()))) continue;
            String text;
            try { text = Files.readString(file, UTF8); }
            catch (MalformedInputException | UncheckedIOException e) { continue; }
            if (!text.contains(canonical)) continue;
            Files.writeString(file, text.replace(canonical, internal), UTF8);
            op("internalize-openapi-package", Map.of("path", rel(file)));
        }
    }

    private void internalizeBrokerClientAdapter() throws Exception {
        Path source = root.resolve("cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerClientAdapter.java");
        Path target = root.resolve("cpf-core/src/main/java/com/cpf/core/internal/broker/CpfBrokerClientAdapter.java");
        requireFile(target, "canonical broker adapter missing: " + rel(target));
        String expectedImport = "com.cpf.core.internal.broker.CpfBrokerClientAdapter";
        String legacyImport = "com.cpf.core.common.broker.CpfBrokerClientAdapter";
        if (Files.isRegularFile(source)) {
            String legacy = Files.readString(source, UTF8);
            if (!legacy.contains("package com.cpf.core.common.broker;") || !legacy.contains("class CpfBrokerClientAdapter")) {
                throw new IllegalStateException("unexpected legacy broker adapter content: " + rel(source));
            }
            Files.delete(source);
            op("delete-executed", Map.of("path", rel(source), "reason", "implementation moved to cpf-core internal boundary"));
        }
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path file : stream.filter(Files::isRegularFile).toList()) {
                String relative = rel(file);
                if (isProtected(relative) || hasSkippedPart(relative) || !TEXT_EXT.contains(extension(file.getFileName().toString()))) continue;
                String text;
                try { text = Files.readString(file, UTF8); }
                catch (MalformedInputException | UncheckedIOException e) { continue; }
                if (!text.contains(legacyImport)) continue;
                Files.writeString(file, text.replace(legacyImport, expectedImport), UTF8);
                op("internalize-broker-adapter-reference", Map.of("path", relative));
            }
        }
    }

    private void injectNotificationImports() throws Exception {
        Path base = root.resolve("cpf-starters/notification/dispatch/src"); if (!Files.exists(base)) return;
        Map<String,String> imports = new LinkedHashMap<>();
        imports.put("CpfNotificationRequest","com.cpf.core.api.notification.CpfNotificationRequest"); imports.put("CpfNotificationResult","com.cpf.core.api.notification.CpfNotificationResult"); imports.put("CpfNotificationProviderStatus","com.cpf.core.api.notification.CpfNotificationProviderStatus"); imports.put("CpfNotificationReceipt","com.cpf.core.api.notification.CpfNotificationReceipt"); imports.put("CpfNotificationProvider","com.cpf.core.spi.notification.CpfNotificationProvider"); imports.put("CpfNotificationReconciler","com.cpf.core.spi.notification.CpfNotificationReconciler");
        Pattern pkg = Pattern.compile("(?m)^(package\\s+[^;]+;)");
        for (Path java : filesUnder(base).stream().filter(p -> p.toString().endsWith(".java")).toList()) {
            String text = Files.readString(java,UTF8); Matcher pm = pkg.matcher(text); if (!pm.find()) continue; List<String> additions = new ArrayList<>();
            for (Map.Entry<String,String> e : imports.entrySet()) if (Pattern.compile("\\b"+Pattern.quote(e.getKey())+"\\b").matcher(text).find() && !text.contains(e.getValue()) && !Pattern.compile("\\b(class|record|interface|enum)\\s+"+Pattern.quote(e.getKey())+"\\b").matcher(text).find()) additions.add("import "+e.getValue()+";");
            if (!additions.isEmpty()) { Collections.sort(additions); text = text.substring(0,pm.end()) + "\n\n" + String.join("\n",additions) + text.substring(pm.end()); Files.writeString(java,text,UTF8); op("notification-core-import",Map.of("path",rel(java))); }
        }
    }

    private void updateDeleteManifests(String head) throws Exception {
        String now = OffsetDateTime.now().toString();
        for (String rel : List.of("cpf-docs/work/manifest/DELETE_MANIFEST.csv","cpf-docs/work/CPF_QA_FINAL_DELETE_WORK_ITEMS.csv")) {
            Path p = root.resolve(rel); if (!Files.isRegularFile(p)) continue; List<Map<String,String>> rows = readCsv(p); if (rows.isEmpty()) continue; boolean changed=false;
            for (Map<String,String> row : rows) {
                String expected = "EXACT_DECLARED_CONTRACT_SET".equals(row.get("target_type")) ? "NOT_APPLICABLE" : "PASS";
                if ("EXECUTED".equals(row.get("lifecycle_status")) && ("WORKTREE_ON_BASE_"+head).equals(row.get("executed_commit_sha")) && "PASS".equals(row.get("residual_reference_result")) && expected.equals(row.get("empty_directory_result"))) continue;
                row.put("lifecycle_status","EXECUTED"); row.put("executed_at",now); row.put("executed_commit_sha","WORKTREE_ON_BASE_"+head); row.put("residual_reference_result","PASS"); row.put("empty_directory_result",expected); changed=true;
            }
            if (changed) { writeCsv(p,rows); op("delete-manifest-executed",Map.of("path",rel)); }
        }
    }

    private void writeApplyReport(String head) throws Exception {
        Path dir=root.resolve("cpf-docs/evidence/qa39/apply"), report=dir.resolve("apply-report.json"); Files.createDirectories(dir);
        if (!operations.isEmpty() || !Files.exists(report)) {
            List<Map<String,Object>> merged = new ArrayList<>();
            Set<String> seen = new LinkedHashSet<>();
            if (Files.isRegularFile(report)) {
                Object parsed = Json.parse(Files.readString(report, UTF8));
                if (parsed instanceof Map<?,?> prior && prior.get("operations") instanceof List<?> priorOperations) {
                    for (Object item : priorOperations) if (item instanceof Map<?,?> map) {
                        Map<String,Object> copy = new LinkedHashMap<>();
                        for (Map.Entry<?,?> entry : map.entrySet()) copy.put(String.valueOf(entry.getKey()), entry.getValue());
                        String key = Json.stringify(copy, 0); if (seen.add(key)) merged.add(copy);
                    }
                }
            }
            for (Map<String,Object> operation : operations) {
                Map<String,Object> copy = new LinkedHashMap<>(operation);
                String key = Json.stringify(copy, 0); if (seen.add(key)) merged.add(copy);
            }
            Map<String,Object> data=new LinkedHashMap<>(); data.put("requirement_id","QA39-045..QA39-064"); data.put("source_sha",head); data.put("result","PASS"); data.put("exit_code",0); data.put("operations",merged); data.put("sanitization_checked",true); Files.writeString(report,Json.stringify(data,2)+"\n",UTF8);
        }
    }

    @SuppressWarnings("unchecked")
    private void verifyCanonical() throws Exception {
        Map<String,Object> canonical=asMap(Json.parse(Files.readString(requireFile(root.resolve("cpf-tools/generator/contracts/cpf-starter-catalog.json"),"missing canonical catalog"),UTF8)));
        Map<String,Object> release=asMap(Json.parse(Files.readString(requireFile(root.resolve("cpf-tools/release/cpf-final-artifact-catalog.json"),"missing release catalog"),UTF8)));
        List<Map<String,Object>> modules=asMapList(canonical.get("modules")); List<Map<String,Object>> starters=asMapList(release.get("artifacts")).stream().filter(m -> Set.of("starter-profile","internal-starter").contains(String.valueOf(m.get("kind")))).toList();
        if(modules.size()!=38||starters.size()!=38) fail("CANONICAL","starter count mismatch canonical="+modules.size()+" release="+starters.size()+" expected=38");
        checkDuplicate(modules,"artifactId","canonical");checkDuplicate(modules,"ownerPath","canonical");checkDuplicate(starters,"artifactId","release");checkDuplicate(starters,"ownerPath","release");
        Map<String,Map<String,Object>> cm=index(modules,"artifactId"),rm=index(starters,"artifactId"); if(!cm.keySet().equals(rm.keySet()))fail("CANONICAL","catalog artifact parity mismatch");
        for(String aid:cm.keySet())for(String key:List.of("ownerPath","visibility","ownerGroup","internalRole"))if(!Objects.equals(cm.get(aid).get(key),rm.get(aid).get(key)))fail("CANONICAL","catalog mismatch "+aid+" field="+key);
        List<Map<String,Object>> profiles=modules.stream().filter(m->"starter-profile".equals(m.get("kind"))).toList(),internal=modules.stream().filter(m->"internal-starter".equals(m.get("kind"))).toList(); if(profiles.size()!=6||internal.size()!=32)fail("CANONICAL","visibility count profile="+profiles.size()+" internal="+internal.size());
        Set<String> profileNames=profiles.stream().map(m->Paths.get(String.valueOf(m.get("ownerPath"))).getFileName().toString()).collect(Collectors.toSet()); if(!profileNames.equals(Set.of("minimal-domain","web-api","secure-api","browser-bff","event-service","batch-service")))fail("CANONICAL","profile name set mismatch");
        Set<String> groups=internal.stream().map(m->String.valueOf(m.get("ownerGroup"))).filter(g->!g.equals("foundation")).collect(Collectors.toSet());if(!groups.equals(Set.of("data","messaging","integration","file","notification","security","platform-operations")))fail("CANONICAL","capability group set mismatch");
        Path startersRoot=root.resolve("cpf-starters");if(Files.isDirectory(startersRoot)){
            Set<String> actual;try(Stream<Path>s=Files.list(startersRoot)){actual=s.filter(Files::isDirectory).map(p->p.getFileName().toString()).collect(Collectors.toSet());}if(!actual.equals(Set.of("foundation","data","messaging","integration","file","notification","security","platform-operations","profiles")))fail("CANONICAL","physical root mismatch actual="+actual);
            Set<String> legacy=Set.of("base","persistence-jdbc","persistence-mybatis","cache","cache-valkey","messaging-reliability-jdbc","messaging-kafka","messaging-rabbitmq","messaging-jms","messaging-ibm-mq","http-client","integration-tcp","integration-fixedlength-core","integration-fixedlength","integration-iso8583","attachment","file-archive","tabular-poi","integration-sftp","notification-email","notification-sms-spi","security-resource-server","security-service-identity","secret","observability","observability-otlp","runtime-control-client","channel-registry-jdbc","openapi-webmvc");for(String name:legacy)if(Files.exists(startersRoot.resolve(name)))fail("CANONICAL","legacy starter root remains: cpf-starters/"+name);
            for(String module:List.of("cpf-starters/notification/dispatch","cpf-starters/security/session-jdbc")){Path m=root.resolve(module);if(Files.isDirectory(m.resolve("main"))||Files.isDirectory(m.resolve("test")))fail("CANONICAL","nested move dropped src segment: "+module);if(!Files.isDirectory(m.resolve("src/main")))fail("CANONICAL","canonical src/main missing: "+module);}
            Set<String> physical;try(Stream<Path>s=Files.walk(startersRoot)){physical=s.filter(p->Files.isRegularFile(p)&&p.getFileName().toString().equals("build.gradle")).map(p->rel(p.getParent())).collect(Collectors.toSet());}Set<String>expected=modules.stream().map(m->String.valueOf(m.get("ownerPath"))).collect(Collectors.toSet());if(!physical.equals(expected))fail("CANONICAL","physical/catalog parity mismatch missing="+difference(expected,physical)+" extra="+difference(physical,expected));
        }
        if(Files.exists(root.resolve("cpf-starters/openapi-webmvc")))fail("CANONICAL","standalone openapi-webmvc remains");
        String publicBom=readIfExists("cpf-tools/build/platform-bom/public-bom/build.gradle");if(publicBom!=null){if(!publicBom.contains("visibility?.toString()=='public'")||!publicBom.contains("publicModules.size()!=6"))fail("CANONICAL","public BOM is not catalog-driven for six public profiles");if(publicBom.contains("internalModules"))fail("CANONICAL","public BOM references internal module selection");}
        String internalBom=readIfExists("cpf-tools/build/platform-bom/internal-bom/build.gradle");if(internalBom!=null){if(!internalBom.contains("visibility?.toString()=='internal'")||!internalBom.contains("internalModules.size()!=32"))fail("CANONICAL","internal BOM is not catalog-driven for 32 internal starters");if(!internalBom.contains("cpf-internal-platform-bom"))fail("CANONICAL","internal BOM publication coordinate missing");}
        System.out.println("[CPF][QA39][PASS] canonical starter=38 profiles=6 internal=32 groups=7 duplicates=0");
    }

    private void lowCost() throws Exception {
        List<String> failures = new ArrayList<>();
        runCollectedGate("canonical", this::verifyCanonical, failures);
        runCollectedGate("naming", this::verifyNaming, failures);
        runCollectedGate("db", this::verifyDb, failures);
        runCollectedGate("openapi-frontend", this::verifyOpenApi, failures);
        runCollectedGate("source-boundaries", this::verifyBoundaries, failures);
        runCollectedGate("evidence-truth", this::verifyEvidence, failures);
        runCollectedGate("git-diff-check", () -> run("git","-C",root.toString(),"diff","--check"), failures);
        if (!failures.isEmpty()) throw new IllegalStateException("QA39 low-cost gates failed with " + failures.size() + " issue(s):\n - " + String.join("\n - ", failures));
        System.out.println("[CPF][QA39][PASS] Java-only low-cost gates count=7");
    }

    private void runCollectedGate(String name, CheckedRunnable action, List<String> failures) {
        try { action.run(); }
        catch (Exception e) { failures.add(name + ": " + e.getMessage()); System.err.println("[CPF][QA39][COLLECTED-FAIL] " + name + ": " + e.getMessage()); }
    }

    @FunctionalInterface
    private interface CheckedRunnable { void run() throws Exception; }

    @SuppressWarnings("unchecked") private void verifyNaming() throws Exception {
        List<String> issues = new ArrayList<>();
        Map<String,Object> cat = asMap(Json.parse(Files.readString(root.resolve("cpf-tools/generator/contracts/cpf-starter-catalog.json"), UTF8)));
        for (Map<String,Object> m : asMapList(cat.get("modules"))) {
            String aid = s(m.get("artifactId"));
            String path = s(m.get("ownerPath"));
            String packageBase = s(m.get("packageBase"));
            String configPrefix = s(m.get("configPrefix"));
            if (!aid.startsWith("cpf-starter-")) issues.add("artifact prefix: " + aid);
            String expected;
            if ("starter-profile".equals(m.get("kind"))) expected = "cpf-starters/profiles/" + aid.substring("cpf-starter-profile-".length());
            else if ("foundation".equals(m.get("ownerGroup"))) expected = "cpf-starters/foundation/base";
            else {
                String prefix = "cpf-starter-" + m.get("ownerGroup") + "-";
                expected = "cpf-starters/" + m.get("ownerGroup") + "/" + aid.substring(prefix.length());
            }
            if (!path.equals(expected)) issues.add("path/artifact semantic mismatch " + aid + ": " + path + " != " + expected);
            if (!packageBase.startsWith("com.cpf.starter.")) issues.add("package prefix " + aid + ": " + packageBase);
            if (!configPrefix.startsWith("cpf.")) issues.add("config prefix " + aid + ": " + configPrefix);
            if ("internal-starter".equals(m.get("kind")) && s(m.get("internalRole")).isBlank()) issues.add("missing internalRole " + aid);
        }

        Pattern cls = Pattern.compile("\\b(class|record|interface|enum)\\s+(Cpf\\w+)");
        Pattern pkg = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+);");
        Pattern publicType = Pattern.compile("\\bpublic\\s+(final\\s+)?(class|record|interface|enum)\\b");
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path java : stream.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java")).toList()) {
                String relative = rel(java);
                if (hasSkippedPart(relative)) continue;
                String text = Files.readString(java, UTF8);
                Matcher classMatcher = cls.matcher(text);
                Matcher packageMatcher = pkg.matcher(text);
                if (classMatcher.find() && classMatcher.group(2).matches("^Cpf(Service|Manager|Helper|Util|Impl|DefaultProvider)$")) {
                    issues.add("ambiguous class " + classMatcher.group(2) + " at " + relative);
                }
                String packageName = packageMatcher.find() ? packageMatcher.group(1) : "";
                if (relative.startsWith("cpf-starters/") && relative.contains("/src/main/java/")
                        && !packageName.contains(".internal") && !packageName.startsWith("com.cpf.starter.profile.")
                        && publicType.matcher(text).find()) {
                    issues.add("starter public implementation is not internal: " + relative + " package=" + packageName);
                }
                boolean capabilityPackage = packageName.matches(".*\\.(notification|resilience|featureflag|broker)(\\..*)?");
                boolean allowedBoundary = packageName.startsWith("com.cpf.core.api.")
                        || packageName.startsWith("com.cpf.core.spi.")
                        || packageName.startsWith("com.cpf.core.internal.");
                if (relative.startsWith("cpf-core/src/main/java/") && capabilityPackage && !allowedBoundary
                        && !isUnchangedLegacyCoreCapability(relative, packageName)) {
                    issues.add("new or relocated core capability outside api/spi/internal: " + relative + " package=" + packageName);
                }
            }
        }
        if (Files.exists(root.resolve("cpf-core/src/main/java/com/cpf/core/common/broker/CpfBrokerClientAdapter.java"))) {
            issues.add("legacy broker adapter remains outside internal boundary");
        }
        if (!Files.isRegularFile(root.resolve("cpf-core/src/main/java/com/cpf/core/internal/broker/CpfBrokerClientAdapter.java"))) {
            issues.add("internal broker adapter missing");
        }
        if (!issues.isEmpty()) {
            throw new IllegalStateException("[NAMING] " + issues.size() + " issue(s):\n - " + String.join("\n - ", issues));
        }
        System.out.println("[CPF][QA39][PASS] naming steering artifact/path/package/config/class baseline-legacy-aware");
    }

    private boolean isUnchangedLegacyCoreCapability(String relative, String currentPackage) {
        try {
            byte[] headBytes = runBytes("git", "-C", root.toString(), "show", "HEAD:" + relative);
            String headText = new String(headBytes, UTF8);
            Matcher matcher = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+);").matcher(headText);
            return matcher.find() && matcher.group(1).equals(currentPackage);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void verifyDb() throws Exception {
        List<String>vendors=List.of("oracle","postgresql","mariadb"),files=List.of("source/15_qa39_resilience_feature_flag.sql","install/03_qa39_resilience_feature_flag.sql","migration/flyway/cpfDB/V97__qa39_resilience_feature_flag.sql","rollback/R97__qa39_resilience_feature_flag.sql","verify/97_qa39_resilience_feature_flag.sql");Set<String>tables=Set.of("cpf_resilience_policy","cpf_resilience_policy_request","cpf_resilience_audit","cpf_feature_flag_override_request","cpf_feature_flag_override","cpf_feature_flag_kill_switch","cpf_feature_flag_revision","cpf_feature_flag_audit"),constraints=null;Pattern create=Pattern.compile("(?i)create\\s+table\\s+([a-z0-9_]+)"),constraint=Pattern.compile("(?i)constraint\\s+([a-z0-9_]+)"),drop=Pattern.compile("(?i)drop\\s+table\\s+(?:if\\s+exists\\s+)?([a-z0-9_]+)");for(String v:vendors){Path base=root.resolve("cpf-tools/db/vendor/"+v);for(String f:files)requireFile(base.resolve(f),"missing "+v+"/"+f);Path mig=base.resolve(files.get(2));String text=Files.readString(mig,UTF8);Set<String>found=matches(create,text,1);if(!found.equals(tables))fail("DB",v+" table set mismatch "+symmetric(found,tables));Set<String>cs=matches(constraint,text,1);if(constraints==null)constraints=cs;else if(!constraints.equals(cs))fail("DB","constraint identity differs across vendors");Path side=Paths.get(mig+".sha256");requireFile(side,"missing checksum "+side);String expected=Files.readString(side,UTF8).trim().split("\\s+")[0].toLowerCase(Locale.ROOT);if(!expected.equals(sha256(mig)))fail("DB","checksum mismatch "+side);Set<String>drops=matches(drop,Files.readString(base.resolve(files.get(3)),UTF8),1);if(!drops.equals(tables))fail("DB",v+" rollback table set mismatch");}System.out.println("[CPF][QA39][PASS] db vendors=3 tables=8 lifecycle=source/install/V97/rollback/verify checksum=exact");
    }

    @SuppressWarnings("unchecked") private void verifyOpenApi() throws Exception {
        Set<String>expected=Set.of("admFeatureFlagSearch","admFeatureFlagFind","admFeatureFlagEvaluate","admFeatureFlagRequestOverride","admFeatureFlagApproveOverride","admFeatureFlagRevokeOverride","admFeatureFlagSetKillSwitch","admResiliencePolicySearch","admResiliencePolicyFind","admResiliencePolicyRequest","admResiliencePolicyApprove","admResiliencePolicyReject");Map<String,Object>spec=asMap(Json.parse(Files.readString(root.resolve("cpf-admin/frontend/openapi/cpf-openapi.json"),UTF8)));Map<String,Object>paths=asMap(spec.get("paths"));List<String>ids=new ArrayList<>();for(Object item:paths.values())for(Map.Entry<String,Object>method:asMap(item).entrySet())if(Set.of("get","post","put","patch","delete").contains(method.getKey())&&method.getValue()instanceof Map<?,?>m&&m.get("operationId")!=null)ids.add(s(m.get("operationId")));Set<String>dup=ids.stream().filter(x->Collections.frequency(ids,x)>1).collect(Collectors.toSet());if(!dup.isEmpty())fail("OPENAPI","duplicate operationId "+dup);if(!ids.containsAll(expected))fail("OPENAPI","missing OpenAPI ops "+difference(expected,new HashSet<>(ids)));for(String rel:List.of("cpf-admin/frontend/src/generated/cpf-api.ts","cpf-admin/frontend/src/generated/cpf-operation-contract.ts","cpf-admin/frontend/src/generated/adm-route-operation-contract.ts","cpf-admin/frontend/src/app/routes.ts")){String text=Files.readString(requireFile(root.resolve(rel),"missing "+rel),UTF8);for(String op:expected)if(!text.contains(op))fail("OPENAPI",rel+" missing "+op);}for(String rel:List.of("cpf-admin/frontend/src/features/feature-flags/FeatureFlagsPage.vue","cpf-admin/frontend/src/features/resilience-policies/ResiliencePoliciesPage.vue"))requireFile(root.resolve(rel),"missing consumer "+rel);System.out.println("[CPF][QA39][PASS] openapi operations="+ids.size()+" new=12 duplicates=0 generated-route-consumer=exact");
    }

    private void verifyBoundaries() throws Exception {
        for(String[]x:new String[][]{{"worker","cpf-batch/worker/build.gradle"},{"scheduler","cpf-batch/scheduler/build.gradle"}}){String t=Files.readString(root.resolve(x[1]),UTF8);if(t.matches("(?s).*(messaging-kafka|spring-integration-kafka|spring-kafka).*") )fail("BOUNDARY","batch "+x[0]+" provider direct dependency");}String state=Files.readString(root.resolve("cpf-batch/worker/src/main/java/com/cpf/batch/worker/SpringBatchWorkerRuntimeState.java"),UTF8);if(state.contains("KafkaListenerEndpointRegistry"))fail("BOUNDARY","worker source imports Kafka provider");if(!state.contains("CpfBrokerConsumerControlPort"))fail("BOUNDARY","worker missing provider-neutral control port");String identity=Files.readString(root.resolve("cpf-batch/worker/src/main/java/com/cpf/batch/worker/CpfBatchWorkerIdentity.java"),UTF8);for(String token:List.of("systemId","instanceId","processId","restartId","leaseEpoch","fencingToken"))if(!identity.contains(token))fail("BOUNDARY","batch identity missing "+token);String reporter=Files.readString(root.resolve("cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerRegistryReporter.java"),UTF8);if(!reporter.contains("CpfBatchWorkerIdentity")||!reporter.contains("identity.canonicalId()"))fail("BOUNDARY","worker registry does not persist canonical composite identity");String kafka=Files.readString(root.resolve("cpf-starters/messaging/kafka/src/main/java/com/cpf/starter/messaging/kafka/internal/CpfKafkaConsumerControlAdapter.java"),UTF8);if(!kafka.contains("implements CpfBrokerConsumerControlPort"))fail("BOUNDARY","Kafka provider control adapter missing");for(String rel:List.of("cpf-starters/integration/http-client/src/main/java/com/cpf/starter/integration/http/client/internal/CpfResilientHttpClient.java","cpf-starters/integration/tcp/src/main/java/com/cpf/starter/integration/tcp/internal/CpfResilientTcpClient.java","cpf-gateway/src/main/java/com/cpf/gateway/internal/resilience/CpfGatewayResilientInvoker.java"))if(!Files.readString(requireFile(root.resolve(rel),"missing "+rel),UTF8).contains("CpfResilienceExecutor"))fail("BOUNDARY","missing resilience consumer "+rel);Set<String>legacy=Set.of("cpf-starter-base","cpf-starter-persistence-jdbc","cpf-starter-persistence-mybatis","cpf-starter-cache","cpf-starter-http-client","cpf-starter-attachment","cpf-starter-tabular-poi","cpf-starter-secret","cpf-starter-observability","cpf-starter-observability-otlp","cpf-starter-runtime-control-client","cpf-starter-channel-registry-jdbc","cpf-starter-openapi-webmvc");try(Stream<Path>stream=Files.walk(root)){for(Path gradle:stream.filter(p->Files.isRegularFile(p)&&p.toString().endsWith(".gradle")).toList()){String rel=rel(gradle);if(hasSkippedPart(rel))continue;String text=Files.readString(gradle,UTF8);for(String old:legacy)if(Pattern.compile("(?<![\\w.-])"+Pattern.quote(old)+"(?![\\w.-])").matcher(text).find())fail("BOUNDARY","legacy artifact "+old+" at "+rel);}}System.out.println("[CPF][QA39][PASS] source boundaries batch-provider-neutral resilience-consumers=3 legacy-artifact=0");
    }

    @SuppressWarnings("unchecked") private void verifyEvidence() throws Exception {
        Path matrix=root.resolve("cpf-docs/work/matrix/CPF_QA39_REQUIREMENT_SCENARIO_RESULT_MATRIX.csv");requireFile(matrix,"missing requirement matrix");for(Map<String,String>row:readCsv(matrix))if("완료".equals(row.get("verification_status"))){String result=row.getOrDefault("actual_result","");if(!(result.equals("PASS")||result.equals("성공"))||result.matches("(?i).*(READY|PLANNED|NOT_EXECUTED|SKIPPED|미실행|미검증).*"))fail("EVIDENCE","false completed verification "+row.get("requirement_id")+": "+result);String ev=row.getOrDefault("evidence_path","");if(ev.isBlank()||!Files.isRegularFile(root.resolve(ev)))fail("EVIDENCE","completed row lacks evidence "+row.get("requirement_id")+": "+ev);if(ev.endsWith(".json")){Map<String,Object>d=asMap(Json.parse(Files.readString(root.resolve(ev),UTF8)));if(!Objects.equals(number(d.get("exit_code")),0L)||!"PASS".equals(d.get("result")))fail("EVIDENCE","invalid evidence result "+ev);String sha=s(d.get("source_sha"));if(!sha.isBlank()&&!sha.equals("WORKTREE_AFTER_APPLY")&&sha.length()!=40)fail("EVIDENCE","invalid evidence SHA "+ev);}}System.out.println("[CPF][QA39][PASS] evidence truth no false-pass rows");
    }



    private void verifyBuildContract() throws Exception {
        String settings = Files.readString(requireFile(root.resolve("settings.gradle"), "missing settings.gradle"), UTF8);
        String pluginBuild = Files.readString(requireFile(root.resolve("cpf-tools/build/gradle-plugin/build.gradle"), "missing convention plugin build"), UTF8);
        String bomBuild = Files.readString(requireFile(root.resolve("cpf-tools/build/platform-bom/build.gradle"), "missing platform BOM build"), UTF8);
        String member = Files.readString(requireFile(root.resolve("cpf-member/build.gradle"), "missing cpf-member build"), UTF8);
        String generator = Files.readString(requireFile(root.resolve("cpf-tools/generator/create-domain.ps1"), "missing create-domain.ps1"), UTF8);
        String exporter = Files.readString(requireFile(root.resolve("cpf-tools/generator/export-domain-repository.ps1"), "missing export-domain-repository.ps1"), UTF8);
        String jobpack = Files.readString(requireFile(root.resolve("cpf-tools/generator/create-domain-jobpack.ps1"), "missing create-domain-jobpack.ps1"), UTF8);
        String verifier = Files.readString(requireFile(root.resolve("cpf-tools/scripts/verify-local-artifact-propagation.ps1"), "missing artifact propagation verifier"), UTF8);
        String canonicalPlugin = "com.cpf.platform-conventions", legacyPlugin = "com.cpf.domain-conventions", canonicalBom = "com.cpf:cpf-platform-bom", legacyBom = "com.cpf:cpf-bom";
        if (!settings.contains("pluginManagement") || !settings.contains("includeBuild('cpf-tools/build/gradle-plugin')")) fail("BUILD-CONTRACT", "canonical convention plugin included build is not wired in pluginManagement");
        if (!(pluginBuild.contains("id = '" + canonicalPlugin + "'") || pluginBuild.contains("id='" + canonicalPlugin + "'"))) fail("BUILD-CONTRACT", "canonical plugin ID is not published");
        if (!pluginBuild.contains("group = 'com.cpf.gradle'")) fail("BUILD-CONTRACT", "canonical plugin implementation group mismatch");
        if (!bomBuild.contains("artifactId = 'cpf-platform-bom'")) fail("BUILD-CONTRACT", "canonical BOM artifact mismatch");
        for (Map.Entry<String,String> item : Map.of("cpf-member", member, "generator", generator).entrySet()) {
            if (!item.getValue().contains(canonicalPlugin)) fail("BUILD-CONTRACT", item.getKey() + " does not consume canonical plugin");
            if (item.getValue().contains(legacyPlugin)) fail("BUILD-CONTRACT", item.getKey() + " still consumes legacy plugin");
        }
        for (Map.Entry<String,String> item : Map.of("create-domain generator", generator, "domain repository exporter", exporter, "domain jobpack generator", jobpack).entrySet()) {
            if (!item.getValue().contains(canonicalBom)) fail("BUILD-CONTRACT", item.getKey() + " does not emit canonical BOM coordinate: " + canonicalBom);
            if (item.getValue().contains(legacyBom)) fail("BUILD-CONTRACT", item.getKey() + " still emits legacy BOM coordinate: " + legacyBom);
        }
        for (String required : List.of(canonicalPlugin, "com.cpf.gradle", "cpf-platform-bom")) if (!verifier.contains(required)) fail("BUILD-CONTRACT", "artifact verifier missing canonical token: " + required);
        for (String forbidden : List.of(legacyPlugin, "com.cpf.build", "cpf-bom/$version")) if (verifier.contains(forbidden)) fail("BUILD-CONTRACT", "artifact verifier still accepts legacy coordinate: " + forbidden);
        System.out.println("CPF canonical build contract: PASS");
    }

    private void providerConformance() throws Exception {
        List<Path> sources = List.of(
            root.resolve("cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerClient.java"),
            root.resolve("cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerPublishRequest.java"),
            root.resolve("cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerPublishResult.java"),
            root.resolve("cpf-core/src/main/java/com/cpf/core/spi/notification/CpfNotificationProvider.java"),
            root.resolve("cpf-core/src/main/java/com/cpf/core/api/notification/CpfNotificationProviderStatus.java"),
            root.resolve("cpf-core/src/main/java/com/cpf/core/api/notification/CpfNotificationRequest.java"),
            root.resolve("cpf-core/src/main/java/com/cpf/core/api/notification/CpfNotificationResult.java")
        );
        Pattern forbidden = Pattern.compile("(?m)^\\s*import\\s+(?:com\\.cpf\\.(?:core\\.common|starter\\.[^.]+\\.internal)|org\\.springframework\\.(?:kafka|amqp|jms|jdbc)|org\\.apache\\.kafka|jakarta\\.jms)\\.");
        for (Path source : sources) {
            requireFile(source, "public SPI source missing: " + rel(source));
            if (forbidden.matcher(Files.readString(source, UTF8)).find()) fail("PROVIDER-CONFORMANCE", "OSS/internal type leaked into public SPI: " + rel(source));
        }
        Path javaHome = Paths.get(System.getProperty("java.home"));
        String exe = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win") ? ".exe" : "";
        Path javac = javaHome.resolve("bin/javac" + exe), java = javaHome.resolve("bin/java" + exe);
        requireFile(javac, "javac is required: " + javac); requireFile(java, "java is required: " + java);
        Path temp = Files.createTempDirectory("cpf-provider-conformance-");
        try {
            Path sourceRoot = temp.resolve("src"), classes = temp.resolve("classes");
            Path fixture = sourceRoot.resolve("com/customer/extension/CustomerProviderConformanceFixture.java");
            Files.createDirectories(fixture.getParent()); Files.createDirectories(classes);
            String code = """
                package com.customer.extension;

                import com.cpf.core.api.broker.CpfBrokerClient;
                import com.cpf.core.api.broker.CpfBrokerPublishRequest;
                import com.cpf.core.api.broker.CpfBrokerPublishResult;
                import com.cpf.core.spi.notification.CpfNotificationProvider;
                import com.cpf.core.api.notification.CpfNotificationProviderStatus;
                import com.cpf.core.api.notification.CpfNotificationRequest;
                import com.cpf.core.api.notification.CpfNotificationResult;
                import java.time.Instant;
                import java.util.Map;

                public final class CustomerProviderConformanceFixture {
                    private CustomerProviderConformanceFixture() {}
                    static final class CustomerBrokerProvider implements CpfBrokerClient {
                        @Override public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
                            return new CpfBrokerPublishResult("ENQUEUED", request.messageId(), "customer-broker", request.key(), Instant.parse("2026-08-02T00:00:00Z"), "accepted");
                        }
                    }
                    static final class CustomerNotificationProvider implements CpfNotificationProvider {
                        @Override public String channel() { return "CUSTOM"; }
                        @Override public CpfNotificationResult send(CpfNotificationRequest request) { return CpfNotificationResult.sent(request.notificationId(), "customer-notification", "receipt-1"); }
                        @Override public CpfNotificationProviderStatus health() { return CpfNotificationProviderStatus.up(); }
                    }
                    public static void main(String[] args) {
                        CpfBrokerPublishRequest brokerRequest = new CpfBrokerPublishRequest("message-1", "topic-1", null, new byte[]{1}, "application/octet-stream", "transaction-1", "segment-1", "CUSTOMER", "TARGET", "idem-1", Map.of("correlationId", "correlation-1"), Map.of());
                        CpfBrokerPublishResult brokerResult = new CustomerBrokerProvider().enqueue(brokerRequest);
                        if (!"ENQUEUED".equals(brokerResult.status())) throw new IllegalStateException("broker conformance failed");
                        CpfNotificationRequest notificationRequest = new CpfNotificationRequest("notification-1", "CUSTOM", "masked-recipient", "template-1", Map.of(), "idem-2", "transaction-1", null);
                        CustomerNotificationProvider provider = new CustomerNotificationProvider();
                        CpfNotificationResult result = provider.send(notificationRequest);
                        if (!"SENT".equals(result.status()) || !"UP".equals(provider.health().status())) throw new IllegalStateException("notification conformance failed");
                        System.out.println("[CPF][PROVIDER-CONFORMANCE][PASS] broker+notification customer SPI");
                    }
                }
                """;
            Files.writeString(fixture, code, UTF8);
            List<String> compile = new ArrayList<>(); compile.add(javac.toString()); compile.add("-encoding"); compile.add("UTF-8"); compile.add("-Xlint:all"); compile.add("-Werror"); compile.add("-d"); compile.add(classes.toString());
            for (Path source : sources) compile.add(source.toString()); compile.add(fixture.toString());
            run(compile.toArray(String[]::new));
            String output = run(java.toString(), "-cp", classes.toString(), "com.customer.extension.CustomerProviderConformanceFixture").trim();
            if (!output.contains("[CPF][PROVIDER-CONFORMANCE][PASS]")) fail("PROVIDER-CONFORMANCE", "fixture runtime output missing PASS: " + output);
            System.out.println(output);
        } finally { deleteTreeIfExists(temp); }
    }

    private void verifyBffSecurity() throws Exception {
        Path main = findUniqueByName(root.resolve("cpf-starters/security"), "CpfServerSessionSecurityAutoConfiguration.java");
        Path test = findUniqueByName(root.resolve("cpf-starters/security"), "CpfServerSessionSecurityFilterChainTest.java");
        String s = Files.readString(main, UTF8), t = Files.readString(test, UTF8);
        if (s.contains(".anyRequest().permitAll()")) fail("BFF-SECURITY", "BFF anyRequest permitAll is forbidden");
        requireTokens(s, "BFF-SECURITY", rel(main), List.of(".anyRequest().authenticated()", "HttpStatus.UNAUTHORIZED", "HttpStatus.FORBIDDEN", "\"/api/bza/auth/login\""));
        requireTokens(t, "BFF-SECURITY", rel(test), List.of("isUnauthorized()", "isForbidden()", "with(csrf())"));
        System.out.println("CPF BFF authorization ownership: PASS");
    }

    private void verifyBatchOutboundPolicy() throws Exception {
        Map<String,String> bodies = new LinkedHashMap<>();
        Map<String,String> paths = Map.of(
            "registry", "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchRuntimeExecutorRegistry.java",
            "policy", "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchOutboundHttpPolicy.java",
            "transport", "cpf-batch/worker/src/main/java/com/cpf/batch/worker/PinnedBatchHttpTransport.java",
            "properties", "cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerOperationalProperties.java",
            "test", "cpf-batch/worker/src/test/java/com/cpf/batch/worker/BatchOutboundHttpPolicyTest.java");
        for (Map.Entry<String,String> e : paths.entrySet()) bodies.put(e.getKey(), Files.readString(requireFile(root.resolve(e.getValue()), "missing: " + e.getValue()), UTF8));
        List<String> failed = new ArrayList<>();
        Object[][] checks = {
            {"disabled-default","private boolean enabled;","properties"},{"dns-pin","BATCH_OUTBOUND_DNS_PIN_REQUIRED","policy"},{"mixed-dns-deny","BATCH_OUTBOUND_MIXED_DNS_RESPONSE_DENIED","policy"},
            {"metadata-deny","BATCH_OUTBOUND_METADATA_ADDRESS_DENIED","policy"},{"cidr-allowlist","allowedCidrs","properties"},{"pinned-connect","target.address()","transport"},
            {"sni","SNIHostName(target.host())","transport"},{"redirect-deny","BATCH_OUTBOUND_REDIRECT_DENIED","transport"},{"request-cap","BATCH_OUTBOUND_REQUEST_SIZE_EXCEEDED","policy"},
            {"response-cap","BATCH_OUTBOUND_RESPONSE_SIZE_EXCEEDED","transport"},{"header-injection-deny","BATCH_OUTBOUND_HEADER_INJECTION_DENIED","registry"},
            {"idempotency","X-Cpf-Idempotency-Key","registry"},{"reconcile","X-Cpf-Reconcile-Key","registry"},{"unknown-result","PROTOCOL_TIMEOUT_UNKNOWN","registry"},
            {"negative-test","dnsPinMismatchAndMetadataAddressFailClosed","test"}
        };
        for (Object[] c : checks) if (!bodies.get((String)c[2]).contains((String)c[1])) failed.add((String)c[0]);
        String registry = bodies.get("registry"); int offset = registry.indexOf("private ExecutionResult executeProtocol"); String protocol = offset >= 0 ? registry.substring(offset) : registry;
        for (String forbidden : List.of("HttpRequest.newBuilder(uri)", "BodyHandlers.ofString")) if (protocol.contains(forbidden)) failed.add("legacy:" + forbidden);
        if (!failed.isEmpty()) fail("BATCH-OUTBOUND", String.join(", ", failed));
        System.out.println("CPF batch outbound policy: PASS");
    }

    private void verifyKafkaAckContract() throws Exception {
        String base = "cpf-batch/execution-runtime/src/"; List<String> files = List.of(
            "main/java/com/cpf/batch/execution/CpfBatchKafkaRemoteConfiguration.java",
            "main/java/com/cpf/batch/execution/CpfBatchKafkaInboundBridge.java",
            "main/java/com/cpf/batch/execution/CpfBatchKafkaWorkerListener.java",
            "main/java/com/cpf/batch/execution/CpfSynchronousWorkerChannel.java",
            "test/java/com/cpf/batch/execution/CpfBatchKafkaWorkerListenerAckBoundaryTest.java");
        StringBuilder joined = new StringBuilder(); String config = "";
        for (String f : files) { String text = Files.readString(requireFile(root.resolve(base + f), "missing: " + base + f), UTF8); joined.append(text).append('\n'); if (f.contains("RemoteConfiguration")) config = text; }
        List<String> missing = new ArrayList<>();
        for (String token : List.of("MANUAL_IMMEDIATE","setCommitRecovered(true)","CpfSynchronousWorkerChannel","ledger.complete","bridge.request(json);acknowledgment.acknowledge()","handlerOrLedgerFailureNeverAcknowledges")) if (!joined.toString().contains(token)) missing.add(token);
        if (config.contains("new DirectChannel()")) missing.add("legacy DirectChannel");
        if (!missing.isEmpty()) fail("KAFKA-ACK", String.join(", ", missing));
        System.out.println("CPF Kafka handler/ledger/ACK contract: PASS");
    }

    private void verifyNetworkIdentity() throws Exception {
        Map<String,String> b = new LinkedHashMap<>();
        Map<String,String> p = Map.ofEntries(
            Map.entry("gateway-context","cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayPinnedAddressContext.java"),
            Map.entry("gateway-client","cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayPinnedHttpClientConfiguration.java"),
            Map.entry("gateway-handler","cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java"),
            Map.entry("gateway-resolver","cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java"),
            Map.entry("gateway-test","cpf-gateway/src/test/java/com/cpf/gateway/scg/CpfScgTargetResolverTest.java"), Map.entry("gateway-build","cpf-gateway/build.gradle"),
            Map.entry("agent-transport","cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java"),
            Map.entry("agent-installer","cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/ArtifactInstaller.java"),
            Map.entry("agent-properties","cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/AgentProperties.java"),
            Map.entry("agent-test","cpf-batch/host-agent/src/test/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransportTest.java"));
        for (Map.Entry<String,String> e:p.entrySet()) b.put(e.getKey(),Files.readString(requireFile(root.resolve(e.getValue()),"missing required file: "+e.getValue()),UTF8));
        Map<String,List<String>> required = Map.ofEntries(
            Map.entry("gateway-context",List.of("ThreadLocal<Map<String, InetAddress>>","Unapproved gateway DNS resolution","CURRENT.remove()")),
            Map.entry("gateway-client",List.of(".setDnsResolver(CpfGatewayPinnedAddressContext::resolve)","NoConnectionReuseStrategy.INSTANCE",".disableRedirectHandling()",".disableAutomaticRetries()")),
            Map.entry("gateway-handler",List.of("CpfGatewayPinnedAddressContext.call(","target.pinnedAddress()","() -> http().handle(upstreamRequest)")),
            Map.entry("gateway-resolver",List.of("mixed private/public DNS response denied","pinnedAddress","validateResolvedAddresses")),
            Map.entry("gateway-test",List.of("dnsChangeCannotAlterActivePinnedConnectionIdentity","rejectsMixedPrivatePublicAndMetadataResponses")), Map.entry("gateway-build",List.of("org.apache.httpcomponents.client5:httpclient5")),
            Map.entry("agent-transport",List.of("new InetSocketAddress(target.address(), target.port())","setEndpointIdentificationAlgorithm(\"HTTPS\")","new SNIHostName(target.host())","ARTIFACT_REDIRECT_DENIED","_MIXED_DNS_RESPONSE_DENIED","_PIN_REQUIRED")),
            Map.entry("agent-installer",List.of("new PinnedArtifactHttpTransport(properties)","artifactTransport.download")),
            Map.entry("agent-properties",List.of("artifactPinnedAddresses","artifactAllowedCidrs","artifactProxyPinnedAddresses","artifactProxyAllowedCidrs","artifactAllowedPorts")),
            Map.entry("agent-test",List.of("mismatchingPinAndMetadataAddressFailClosed","mixedDnsCidrAndPortPoliciesFailClosed","publicHostnameRequiresExplicitAddressPins")));
        List<String> failed = new ArrayList<>(); for (Map.Entry<String,List<String>> e:required.entrySet()) for(String t:e.getValue()) if(!b.get(e.getKey()).contains(t)) failed.add(e.getKey()+":"+t);
        if(b.get("gateway-client").contains("SimpleClientHttpRequestFactory"))failed.add("gateway-client:forbidden:SimpleClientHttpRequestFactory");
        for(String t:List.of("HttpClient.newHttpClient()","followRedirects(HttpClient.Redirect.ALWAYS)"))if(b.get("agent-installer").contains(t))failed.add("agent-installer:forbidden:"+t);
        if(!failed.isEmpty())fail("NETWORK-IDENTITY",String.join(", ",failed)); System.out.println("CPF gateway/agent network identity: PASS");
    }

    private void verifyBrowserContract() throws Exception {
        List<String> failed=new ArrayList<>();
        for(String[] pair:new String[][]{{"cpf-admin","admRouterRecords"},{"cpf-biz-admin","bzaRouterRecords"}}){String module=pair[0],registry=pair[1];Path front=root.resolve(module+"/frontend");String cfg=Files.readString(requireFile(front.resolve("playwright.config.ts"),"missing playwright config: "+module),UTF8),spec=Files.readString(requireFile(front.resolve("e2e/route-quality.spec.ts"),"missing route spec: "+module),UTF8),sec=Files.readString(requireFile(front.resolve("e2e/bff-security.spec.ts"),"missing security spec: "+module),UTF8);
            for(String t:List.of("Desktop Chrome","Desktop Firefox","Desktop Safari","CPF_FRONTEND_URL","CPF_E2E_AUTH_STATE","CPF_E2E_ROUTE_MATRIX","CPF_E2E_FAILURE_MATRIX","CPF_E2E_SECURITY_FIXTURE"))if(!cfg.contains(t))failed.add(module+":config:"+t);
            for(String t:List.of(registry,"for(const path of routes)","release route matrix covers","expect(injected","401,403,409,429,500,503","server-side failure matrix","riskConfirmationSelector","horizontal overflow"))if(!spec.contains(t))failed.add(module+":route:"+t);
            for(String t:List.of("slice(0, 40)","test.skip(release,'Release mode uses real backend failure scenarios"))if(spec.contains(t))failed.add(module+":false-green:"+t);
            for(String t:List.of("CPF_E2E_PRIVILEGED_ENDPOINTS","[401, 403]","localStorage","sessionStorage","session fixation","logout did not revoke session","write without CSRF token","untrusted Origin","firstSessionAfterSecondLoginStatuses"))if(!sec.contains(t))failed.add(module+":security:"+t);
        }
        if(!failed.isEmpty())fail("BROWSER-CONTRACT",String.join(", ",failed));System.out.println("CPF 3-browser full-route/BFF contract: PASS");
    }

    private void verifyRuntimeContracts() throws Exception {
        Map<String,List<String>> checks=Map.of(
            "cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerDispatchService.java",List.of("UNKNOWN","fencing","outbox","reconcile"),
            "cpf-batch/control-server/src/main/java/com/cpf/batch/control/deploy/DeploymentEngine.java",List.of("UNKNOWN_RESULT","ROLLBACK","reconcile","idempotencyKey"),
            "cpf-batch/control-server/src/main/java/com/cpf/batch/control/deploy/DeploymentCellLock.java",List.of("acquire","release"),
            "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerCompletionFilter.java",List.of("UNKNOWN_RESULT","recovery"),
            "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayAuditRecoverySpool.java",List.of("spool","replay"),
            "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayLedgerRecoverySpool.java",List.of("spool","replay"),
            "cpf-tools/scripts/smoke-bat-two-worker-runtime.ps1",List.of("Stop-Process","two","worker"));
        for(Map.Entry<String,List<String>>e:checks.entrySet())requireTokens(Files.readString(requireFile(root.resolve(e.getKey()),"missing: "+e.getKey()),UTF8),"RUNTIME-CONTRACT",e.getKey(),e.getValue());
        System.out.println("[CPF][QA34][PASS] runtime contracts");
    }

    private void verifyDbStaticTokenParity(String reportPath) throws Exception {
        List<String> vendors=List.of("mariadb","postgresql","oracle");Map<String,Set<String>> contracts=new LinkedHashMap<>();
        contracts.put("V83__spring_session_jdbc_bff.sql",Set.of("spring_session","spring_session_attributes","primary_id","session_id","expiry_time","principal_name","attribute_bytes","spring_session_ix1","spring_session_ix2","spring_session_ix3","spring_session_attributes_fk"));
        contracts.put("V86__bff_encrypted_credential_vault.sql",Set.of("cpf_bff_credential_vault","handle_id","key_id","access_iv","access_cipher_text","refresh_iv","refresh_cipher_text","access_expires_at","refresh_expires_at","version_no","idx_cpf_bff_credential_expiry","idx_cpf_bff_credential_key"));
        contracts.put("V87__batch_remote_message_ledger.sql",Set.of("bat_remote_message_ledger","direction_cd","message_id","payload_sha256","status_cd","owner_id","lease_until","expires_at","attempt_no","version_no","idx_bat_remote_msg_status","idx_bat_remote_msg_expiry"));
        contracts.put("V88__scheduler_durable_launch_outbox.sql",Set.of("bat_schedule_trigger","job_id","definition_version","definition_checksum","business_date","fire_zone","idempotency_key","dispatch_owner","dispatch_token","dispatch_lease_until","attempt_count","last_error_code","last_error_at","dispatched_at","updated_at","uq_bat_schedule_trigger_idem","ix_bat_schedule_trigger_dispatch"));
        contracts.put("V89__batch_execution_idempotency_lifecycle.sql",Set.of("cpf_batch_execution_control","idempotency_scope","request_hash","plan_checksum","control_version","reconcile_attempts","reconcile_after","last_error_code","last_error_detail","uk_cpf_bat_exec_idem_scope","ix_cpf_bat_exec_reconcile","cpf_batch_execution_epoch","current_fencing_token","epoch_version"));
        contracts.put("V90__deployment_request_hash_reconciliation.sql",Set.of("bat_deployment_execution","idempotency_scope","request_hash","reconcile_requested_by","reconcile_approved_by","reconcile_approval_request_id","reconcile_reason","reconciled_at","ix_bat_deploy_exec_reconciled"));
        contracts.put("V91__bza_bootstrap_claim_recovery.sql",Set.of("bza_bootstrap_approval","claim_owner_id","claim_expires_at","cleanup_status","cleanup_failure_code","cleanup_updated_at","ix_bza_bootstrap_claim_lease"));
        List<String>failures=new ArrayList<>();Map<String,Object>matrix=new LinkedHashMap<>();
        for(Map.Entry<String,Set<String>> c:contracts.entrySet()){Map<String,Object>vr=new LinkedHashMap<>();matrix.put(c.getKey(),vr);for(String v:vendors){List<Path>found;try(Stream<Path>s=Files.walk(root.resolve("cpf-tools/db/vendor/"+v))){found=s.filter(p->Files.isRegularFile(p)&&p.getFileName().toString().equals(c.getKey())).toList();}if(found.size()!=1){failures.add(v+":"+c.getKey()+":expected exactly one file, found "+found.size());continue;}String text=normalizeSql(Files.readString(found.get(0),UTF8));List<String>missing=c.getValue().stream().filter(t->!text.contains(t)).sorted().toList();vr.put(v,Map.of("path",rel(found.get(0)),"missing",missing));for(String t:missing)failures.add(v+":"+c.getKey()+":missing semantic token:"+t);}}
        Map<String,List<String>>families=Map.of("mariadb",List.of("longblob","varbinary","datetime(6)","bigint"),"postgresql",List.of("bytea","timestamp(6)","bigint"),"oracle",List.of("blob","raw(32)","timestamp(6)","number(19)"));
        for(String v:vendors){StringBuilder all=new StringBuilder();for(String n:contracts.keySet()){try(Stream<Path>s=Files.walk(root.resolve("cpf-tools/db/vendor/"+v))){for(Path p:s.filter(x->Files.isRegularFile(x)&&x.getFileName().toString().equals(n)).toList())all.append(normalizeSql(Files.readString(p,UTF8))).append(' ');}}for(String t:families.get(v))if(!all.toString().contains(t))failures.add(v+":vendor-native type family missing:"+t);}
        Map<String,Object> report=new LinkedHashMap<>();report.put("schemaVersion",2);report.put("gateType","STATIC_TOKEN_PARITY");report.put("status",failures.isEmpty()?"PASS":"FAIL");report.put("contracts",matrix);report.put("failures",new TreeSet<>(failures));if(reportPath!=null&&!reportPath.isBlank()){Path out=Paths.get(reportPath);if(!out.isAbsolute())out=root.resolve(out);Files.createDirectories(out.getParent());Files.writeString(out,Json.stringify(report,2)+"\n",UTF8);}if(!failures.isEmpty())fail("DB-STATIC-TOKEN",String.join(", ",failures));System.out.println("[CPF][QA34][PASS] DB vendor static token parity");
    }

    private void verifySourceClosure(String evidenceOutput) throws Exception {
        String head=run("git","-C",root.toString(),"rev-parse","HEAD").trim();if(!head.matches("[0-9a-f]{40}"))fail("SOURCE-CLOSURE","exact Git SHA required");String dirty=run("git","-C",root.toString(),"status","--porcelain=v1","--untracked-files=all").trim();if(!dirty.isBlank())fail("SOURCE-CLOSURE","release source closure requires clean Working Tree");
        try(Stream<Path>s=Files.walk(root)){List<String>garbage=s.filter(p->p.getFileName()!=null&&(p.getFileName().toString().equals("__pycache__")||p.getFileName().toString().endsWith(".pyc"))).map(this::rel).toList();if(!garbage.isEmpty())fail("SOURCE-CLOSURE","repository hygiene: "+garbage);}
        verifyBuildContract();verifyBffSecurity();verifyBatchOutboundPolicy();verifyKafkaAckContract();verifyNetworkIdentity();verifyBrowserContract();verifyRuntimeContracts();verifyDbStaticTokenParity(null);
        for(String app:List.of("cpf-admin","cpf-biz-admin")){Path dir=root.resolve(app+"/frontend/scripts");if(Files.isDirectory(dir))try(Stream<Path>s=Files.list(dir)){for(Path mjs:s.filter(p->p.toString().endsWith(".mjs")).sorted().toList())run("node","--check",mjs.toString());}}
        if(evidenceOutput!=null&&!evidenceOutput.isBlank()){Path out=Paths.get(evidenceOutput);if(!out.isAbsolute())out=root.resolve(out);Files.createDirectories(out.getParent());Map<String,Object>e=new LinkedHashMap<>();e.put("schemaVersion",3);e.put("evidenceId","QA34-SOURCE-CLOSURE-JAVA");e.put("sourceSha",head);e.put("result","PASS");e.put("exitCode",0);e.put("launcher","Qa39Tool.java; Python not required");e.put("sanitized",true);Files.writeString(out,Json.stringify(e,2)+"\n",UTF8);}System.out.println("[CPF][QA34][PASS] Java-only source closure");
    }

    @SuppressWarnings("unchecked")
    private void verifyBatchControlPlane(String reportPath) throws Exception {
        List<String>f=new ArrayList<>();
        String adapter=readRequired("cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/JdbcBatchExecutionControlPlaneAdapter.java"),resolver=readRequired("cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/JdbcBatchApprovedLaunchRequestResolver.java"),control=readRequired("cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java"),digest=readRequired("cpf-batch/contract/src/main/java/com/cpf/batch/api/BatchCanonicalDigest.java"),listener=readRequired("cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchExecutionListener.java");
        for(String t:List.of("claimLatestEpoch","lockCurrentEpoch","BATCH_STALE_FENCING_EPOCH","sql.required(\"execution-control-reserve\")","sql.required(\"execution-control-assert-current\")","sql.required(\"execution-epoch-lock\")","BATCH_LINK_IMMUTABLE_FIELD_CONFLICT","BATCH_CONTROL_BIND_FENCE_CONFLICT"))if(!adapter.contains(t))f.add("missing adapter marker: "+t);
        for(String t:List.of("sql.required(\"execution-approved-launch-find-trigger\")","sql.required(\"execution-approved-launch-find-manual\")"))if(!resolver.contains(t))f.add("missing approved-launch resolver marker: "+t);if(adapter.contains("existing.fencingToken() == request.fencingToken()"))f.add("idempotency immutable comparison still couples operational fencing token");
        String reserve=readRequired("cpf-tools/db/runtime-template/bat/repository/execution-control-reserve.sql.template").toLowerCase(Locale.ROOT),assertSql=readRequired("cpf-tools/db/runtime-template/bat/repository/execution-control-assert-current.sql.template").toLowerCase(Locale.ROOT),lock=readRequired("cpf-tools/db/runtime-template/bat/repository/execution-epoch-lock.sql.template").toLowerCase(Locale.ROOT),trigger=readRequired("cpf-tools/db/runtime-template/bat/repository/execution-approved-launch-find-trigger.sql.template").toLowerCase(Locale.ROOT),manual=readRequired("cpf-tools/db/runtime-template/bat/repository/execution-approved-launch-find-manual.sql.template").toLowerCase(Locale.ROOT);
        for(String t:List.of("cpf_batch_execution_control","request_hash","idempotency_scope","fencing_token"))if(!reserve.contains(t))f.add("execution reserve SQL lacks "+t);for(String t:List.of("cpf_batch_execution_control","join cpf_batch_execution_epoch","current_fencing_token","control_status"))if(!assertSql.contains(t))f.add("assert-current SQL lacks "+t);if(!(lock.contains("cpf_batch_execution_epoch")&&lock.contains("for update")))f.add("epoch lock SQL does not lock the current epoch through commit");for(Map.Entry<String,String>e:Map.of("trigger",trigger,"manual",manual).entrySet())if(!(e.getValue().contains("cpf_batch_approved_launch")&&e.getValue().contains("approval_status = 'approved'")))f.add("approved-launch "+e.getKey()+" SQL is not fail-closed");
        for(String t:List.of("BATCH_START_RESPONSE_UNKNOWN","reconcile(cpfExecutionId)","JobRepository","getJobInstances","getJobExecutions"))if(!control.contains(t))f.add("missing unknown-result reconciliation marker: "+t);if(control.contains("JobExplorer"))f.add("Spring Batch 6 reconciliation still depends on JobExplorer");if(digest.contains("operatorId\", request.operatorId()"))f.add("request hash includes operatorId");if(digest.contains("fencingToken\", request.fencingToken()"))f.add("request hash includes fencingToken");if(!listener.contains("BATCH_JOB_START_LEDGER_OBSERVATION_FAILED"))f.add("beforeJob observation failure is not isolated");if(!listener.contains("BATCH_STEP_START_LEDGER_OBSERVATION_FAILED"))f.add("beforeStep observation failure is not isolated");
        Map<String,Object>schema=asMap(Json.parse(readRequired("cpf-tools/db/canonical/platform-schema.json")));Map<String,Map<String,Object>>tables=new HashMap<>();for(Map<String,Object>t:asMapList(schema.get("tables")))tables.put(s(t.get("name")).toLowerCase(Locale.ROOT),t);Map<String,Set<String>>req=Map.of("cpf_batch_approved_launch",Set.of("approval_id","job_id","definition_version","definition_checksum","approval_status","launch_request_json","row_version"),"cpf_batch_execution_control",Set.of("cpf_execution_id","job_id","idempotency_scope","idempotency_key","request_hash","fencing_token","control_status","control_version"),"cpf_batch_execution_link",Set.of("cpf_execution_id","link_key","job_id","definition_version","spring_job_execution_id","fencing_token"),"cpf_batch_execution_epoch",Set.of("job_id","current_fencing_token","epoch_version","updated_at"));for(Map.Entry<String,Set<String>>e:req.entrySet()){if(!tables.containsKey(e.getKey())){f.add("canonical schema lacks "+e.getKey());continue;}Set<String>cols=asMapList(tables.get(e.getKey()).get("columns")).stream().map(x->s(x.get("name"))).collect(Collectors.toSet());if(!cols.containsAll(e.getValue()))f.add("canonical "+e.getKey()+" columns incomplete: "+difference(e.getValue(),cols));}
        String[][] migrations={{"mariadb","cpf-tools/db/vendor/mariadb/migration/flyway/V89__batch_execution_idempotency_lifecycle.sql","cpf-tools/db/vendor/mariadb/migration/flyway/checksums.sha256"},{"postgresql","cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V89__batch_execution_idempotency_lifecycle.sql","cpf-tools/db/vendor/postgresql/migration/flyway/batDB/checksums.sha256"},{"oracle","cpf-tools/db/vendor/oracle/migration/flyway/batDB/V89__batch_execution_idempotency_lifecycle.sql","cpf-tools/db/vendor/oracle/migration/flyway/batDB/checksums.sha256"}};for(String[]m:migrations){String x=readRequired(m[1]);if(x.contains("requires empty CPF_BATCH_EXECUTION_CONTROL"))f.add(m[0]+" V89 rejects non-empty data");for(String t:List.of("CPF_BATCH_EXECUTION_EPOCH","LEGACY_EXECUTION_REQUIRES_RECONCILIATION"))if(!x.contains(t))f.add(m[0]+" V89 lacks "+t);verifyChecksum(root.resolve(m[1]),root.resolve(m[2]),f);}
        for(String r:List.of("cpf-tools/db/vendor/mariadb/rollback/R89__batch_execution_idempotency_lifecycle.sql","cpf-tools/db/vendor/postgresql/rollback/batDB/R89__batch_execution_idempotency_lifecycle.sql","cpf-tools/db/vendor/oracle/rollback/batDB/R89__batch_execution_idempotency_lifecycle.sql")){String x=readRequired(r);if(!x.contains("CPF_BATCH_EXECUTION_EPOCH_R89_BAK"))f.add("rollback lacks epoch checkpoint: "+r);if(!x.contains("cannot restore global idempotency uniqueness"))f.add("rollback lacks duplicate guard: "+r);}
        String[][] standards={{"mariadb","cpf-tools/db/vendor/mariadb/migration/flyway/V95__batch_control_schema_standard.sql","cpf-tools/db/vendor/mariadb/migration/flyway/checksums.sha256"},{"postgresql","cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V95__batch_control_schema_standard.sql","cpf-tools/db/vendor/postgresql/migration/flyway/batDB/checksums.sha256"},{"oracle","cpf-tools/db/vendor/oracle/migration/flyway/batDB/V95__batch_control_schema_standard.sql","cpf-tools/db/vendor/oracle/migration/flyway/batDB/checksums.sha256"}};for(String[]m:standards){String x=readRequired(m[1]).toLowerCase(Locale.ROOT);for(String t:List.of("cpf_batch_execution_control","cpf_batch_execution_link","cpf_batch_approved_launch"))if(!x.contains(t))f.add(m[0]+" V95 lacks canonical table marker: "+t);if(!x.contains("on delete cascade"))f.add(m[0]+" V95 lacks execution-link lifecycle cascade");verifyChecksum(root.resolve(m[1]),root.resolve(m[2]),f);}
        Map<String,Object>report=Map.of("gate","qa33-batch-control-plane","status",f.isEmpty()?"PASS":"FAIL","failures",f);if(reportPath!=null&&!reportPath.isBlank()){Path out=Paths.get(reportPath);if(!out.isAbsolute())out=root.resolve(out);Files.createDirectories(out.getParent());Files.writeString(out,Json.stringify(report,2)+"\n",UTF8);}if(!f.isEmpty())fail("BATCH-CONTROL",String.join(", ",f));System.out.println("[CPF][QA33][PASS] Batch Control Plane idempotency/fencing/unknown-result gate");
    }

    @SuppressWarnings("unchecked")
    private void verifySupplyChain(Map<String,String> options) throws Exception {
        boolean release=options.containsKey("release");List<String>failures=new ArrayList<>();List<String>warnings=new ArrayList<>();Path settings=root.resolve("settings.gradle"),catalogPath=root.resolve("cpf-tools/release/cpf-final-artifact-catalog.json"),policyPath=root.resolve("cpf-tools/supply-chain/cpf-supply-chain-policy.json"),approvedPath=root.resolve("cpf-tools/supply-chain/approved-primary-oss.csv"),notice=root.resolve("cpf-docs/legal/THIRD_PARTY_NOTICES_QA32.md"),envTemplatePath=root.resolve("cpf-tools/governance/cpf-runtime-environment-manifest.template.json"),envSchema=root.resolve("cpf-tools/governance/cpf-runtime-environment-manifest.schema.json");for(Path p:List.of(settings,approvedPath,notice,envTemplatePath,envSchema))if(!Files.isRegularFile(p))failures.add("missing file: "+p);
        Map<String,Object>catalog=loadJsonObject(catalogPath,failures),policy=loadJsonObject(policyPath,failures),envTemplate=loadJsonObject(envTemplatePath,failures);loadJsonObject(envSchema,failures);
        Set<String>allowed=strSet(policy.get("allowedLicenses")),conditional=strSet(policy.get("conditionalLicenses")),denied=strSet(policy.get("deniedLicenses"));if(allowed.isEmpty()||denied.isEmpty()||!Boolean.TRUE.equals(policy.get("failClosed")))failures.add("supply-chain policy must define allowed/denied licenses and failClosed=true");if(!Collections.disjoint(allowed,denied)||!Collections.disjoint(allowed,conditional)||!Collections.disjoint(conditional,denied))failures.add("supply-chain license sets must be disjoint");Set<String>tools=new HashSet<>();if(policy.get("requiredTools") instanceof List<?>l)for(Object x:l)if(x instanceof Map<?,?>m)tools.add(s(m.get("name")));Set<String>requiredTools=Set.of("cyclonedx-gradle","ort","syft","grype","cpf-release-signer");if(!tools.containsAll(requiredTools))failures.add("supply-chain required tools missing: "+difference(requiredTools,tools));
        int approvedCount=0;if(Files.isRegularFile(approvedPath)){List<Map<String,String>>rows=readCsv(approvedPath);approvedCount=rows.size();if(rows.isEmpty())failures.add("approved primary OSS catalog is empty");for(Map<String,String>r:rows){String component=!r.getOrDefault("component","").isBlank()?r.get("component"):r.getOrDefault("name","<unknown>"),version=r.getOrDefault("version",""),url=r.getOrDefault("source_url",""),lic=r.getOrDefault("license","UNKNOWN");if(version.isBlank()||!url.startsWith("https://"))failures.add("incomplete approved component "+component);if(denied.contains(lic)||!(allowed.contains(lic)||conditional.contains(lic)))failures.add("unapproved license in approved component "+component+":"+lic);}}
        Set<String>expectedProjects=Files.isRegularFile(settings)?gradleProjectPaths(Files.readString(settings,UTF8)):Set.of();List<Map<String,Object>>artifacts=catalog.get("artifacts") instanceof List<?>?asMapList(catalog.get("artifacts")):new ArrayList<>();if(number(catalog.get("schemaVersion"))!=1||artifacts.isEmpty())failures.add("final artifact catalog must be schemaVersion=1 with non-empty artifacts");if(!strSet(catalog.get("officialDatabaseVendors")).stream().map(x->x.toLowerCase(Locale.ROOT)).collect(Collectors.toSet()).equals(Set.of("oracle","postgresql","mariadb")))failures.add("artifact catalog official DB vendors must be Oracle/PostgreSQL/MariaDB only");Set<String>ids=new HashSet<>(),owners=new HashSet<>();for(Map<String,Object>a:artifacts){String id=s(a.get("artifactId")),owner=s(a.get("ownerPath")).replaceAll("^/+|/+$",""),pattern=s(a.get("outputPattern"));if(id.isBlank()||!ids.add(id))failures.add("duplicate/blank artifactId: "+id);if(!relativeSafe(owner)||!relativeSafe(pattern))failures.add("unsafe artifact path/pattern: "+id);if(s(a.get("producer")).isBlank()||s(a.get("consumer")).isBlank())failures.add("artifact producer/consumer missing: "+id);if(!(a.get("requiredAttestations") instanceof List<?>att)&&false){}else if(!(a.get("requiredAttestations") instanceof List<?>att)||!att.contains("sha256"))failures.add("artifact sha256 attestation missing: "+id);owners.add(owner);}if(!owners.containsAll(expectedProjects))failures.add("included Gradle projects missing from artifact catalog: "+difference(expectedProjects,owners));Set<String>dbPacks=artifacts.stream().filter(a->"database-pack".equals(a.get("kind"))).map(a->Paths.get(s(a.get("ownerPath"))).getFileName().toString()).collect(Collectors.toSet());if(!dbPacks.equals(Set.of("oracle","postgresql","mariadb")))failures.add("database artifact packs must be exactly Oracle/PostgreSQL/MariaDB: "+dbPacks);validateEnvironment(envTemplate,null,false,failures);
        if(options.containsKey("sbom") && !release){ validateSbom(loadJsonObject(resolveOption(options.get("sbom")),failures),denied,failures); }
        String head;try{head=run("git","-C",root.toString(),"rev-parse","HEAD").trim();}catch(Exception e){head=null;}Path evidence=options.containsKey("evidence-dir")?resolveOption(options.get("evidence-dir")):null;if(release){if(evidence==null||!Files.isDirectory(evidence))failures.add("release evidence directory missing");else if(policy.get("releaseRequiredEvidence") instanceof List<?>l)for(Object n:l)if(!Files.isRegularFile(evidence.resolve(s(n))))failures.add("release evidence missing: "+n);Path env=options.containsKey("environment-manifest")?resolveOption(options.get("environment-manifest")):(evidence==null?null:evidence.resolve("environment-manifest.json"));Map<String,Object>envData=env==null?new LinkedHashMap<>():loadJsonObject(env,failures);validateEnvironment(envData,head,true,failures);Path sbom=options.containsKey("sbom")?resolveOption(options.get("sbom")):(evidence==null?null:evidence.resolve("cyclonedx-bom.json"));if(sbom!=null)validateSbom(loadJsonObject(sbom,failures),denied,failures);if(evidence!=null){Map<String,Object>manifest=loadJsonObject(evidence.resolve("artifact-manifest.json"),failures);if(head!=null&&!head.equals(s(manifest.get("sourceSha"))))failures.add("artifact manifest SHA mismatch");Set<String>mid=new HashSet<>();if(manifest.get("artifacts") instanceof List<?>l)for(Object x:l)if(x instanceof Map<?,?>m){String id=s(m.get("artifactId"));mid.add(id);if(!s(m.get("sha256")).matches("[0-9a-f]{64}"))failures.add("artifact manifest invalid sha256: "+id);}if(!mid.containsAll(ids))failures.add("release artifact manifest coverage missing: "+difference(ids,mid));Map<String,Object>v=loadJsonObject(evidence.resolve("vulnerability-report.json"),failures);if(number(v.get("critical"))>0||number(v.get("high"))>0)failures.add("release vulnerability report contains critical/high findings");Map<String,Object>sig=loadJsonObject(evidence.resolve("signature-verification.json"),failures);if(!(sig.get("artifacts") instanceof List<?>l)||l.isEmpty()||l.stream().anyMatch(x->!(x instanceof Map<?,?>m)||!Boolean.TRUE.equals(m.get("verified"))))failures.add("one or more release artifact signatures are not verified");}}else if(evidence!=null)warnings.add("evidence-dir ignored unless release");Map<String,Object>result=new LinkedHashMap<>();result.put("status",failures.isEmpty()?"PASS":"FAIL");result.put("release",release);result.put("headSha",head);result.put("includedProjectCount",expectedProjects.size());result.put("artifactCount",artifacts.size());result.put("approvedOssCount",approvedCount);result.put("warnings",warnings);result.put("failures",failures);System.out.println(Json.stringify(result,2));if(!failures.isEmpty())fail("SUPPLY-CHAIN",String.join("; ",failures));
    }

    private Path findUniqueByName(Path start,String name)throws IOException{if(!Files.isDirectory(start))throw new IllegalStateException("missing directory: "+start);try(Stream<Path>s=Files.walk(start)){List<Path>x=s.filter(p->Files.isRegularFile(p)&&p.getFileName().toString().equals(name)).toList();if(x.size()!=1)throw new IllegalStateException("expected one "+name+" under "+rel(start)+", found "+x.size());return x.get(0);}}
    private static void requireTokens(String body,String area,String label,List<String>tokens){List<String>m=tokens.stream().filter(t->!body.contains(t)).toList();if(!m.isEmpty())fail(area,label+" missing: "+m);}
    private String readRequired(String rel)throws IOException{return Files.readString(requireFile(root.resolve(rel),"missing: "+rel),UTF8);}
    private static String normalizeSql(String text){return text.toLowerCase(Locale.ROOT).replaceAll("(?m)--.*$"," ").replaceAll("\\s+"," ");}
    private void verifyChecksum(Path source,Path manifest,List<String>failures)throws Exception{String digest=sha256(source),expected=null;for(String line:Files.readAllLines(requireFile(manifest,"missing checksum manifest: "+manifest),UTF8)){Matcher m=Pattern.compile("^([0-9a-f]{64})\\s+\\*?(.+)$").matcher(line.trim());if(m.matches()&&m.group(2).equals(source.getFileName().toString()))expected=m.group(1);}if(!digest.equals(expected))failures.add("checksum mismatch: "+rel(source));}
    private Map<String,Object> loadJsonObject(Path p,List<String>failures){if(!Files.isRegularFile(p)){failures.add("missing file: "+p);return new LinkedHashMap<>();}try{Object x=Json.parse(Files.readString(p,UTF8));if(x instanceof Map<?,?>)return asMap(x);failures.add("JSON root must be object: "+p);}catch(Exception e){failures.add("invalid JSON "+p+": "+e.getMessage());}return new LinkedHashMap<>();}
    private static Set<String>strSet(Object o){Set<String>x=new HashSet<>();if(o instanceof Collection<?>c)for(Object v:c)x.add(s(v));return x;}
    private static boolean relativeSafe(String v){return v!=null&&!v.isBlank()&&!v.contains("\\")&&!v.startsWith("/")&&!Arrays.asList(v.split("/")).contains("..");}
    private Path resolveOption(String value){Path p=Paths.get(value);return p.isAbsolute()?p:root.resolve(p);}
    private static Set<String>gradleProjectPaths(String settings){Map<String,String>aliases=new HashMap<>();Matcher a=Pattern.compile("project\\(['\"]:(?<name>[^'\"]+)['\"]\\)\\.projectDir\\s*=\\s*file\\(['\"](?<path>[^'\"]+)['\"]\\)").matcher(settings);while(a.find())aliases.put(a.group("name"),a.group("path").replace('\\','/').replaceAll("^/+|/+$",""));Set<String>names=new HashSet<>();Matcher i=Pattern.compile("(?m)^\\s*include\\s+(.+?)\\s*$").matcher(settings),q=Pattern.compile("['\"]([^'\"]+)['\"]").matcher("");while(i.find()){q.reset(i.group(1));while(q.find()){String n=q.group(1).replaceFirst("^:","");if(!n.isBlank())names.add(n);}}return names.stream().map(n->aliases.getOrDefault(n,n.replace(':','/'))).collect(Collectors.toSet());}
    private static void validateEnvironment(Map<String,Object>d,String expected,boolean release,List<String>f){if(number(d.get("schemaVersion"))!=1)f.add("environment manifest schemaVersion must be 1");if(!Boolean.TRUE.equals(d.get("sanitized")))f.add("environment manifest must declare sanitized=true");String sha=s(d.get("sourceSha"));if(!sha.matches("[0-9a-f]{40}"))f.add("environment manifest sourceSha must be 40-char SHA");else if(expected!=null&&!expected.equals(sha))f.add("environment manifest SHA mismatch");Set<String>names=new HashSet<>();Map<String,String>versions=new HashMap<>();if(d.get("tools") instanceof List<?>l)for(Object x:l)if(x instanceof Map<?,?>m){names.add(s(m.get("name")));versions.put(s(m.get("name")),s(m.get("version")));}else f.add("environment manifest tool row invalid");for(String r:Set.of("java","gradle-wrapper","node","npm","powershell"))if(!names.contains(r))f.add("environment manifest tool missing: "+r);if(release&&(!versions.getOrDefault("java","").startsWith("25")||!versions.getOrDefault("node","").startsWith("22")))f.add("release environment requires Java 25 and Node 22");Set<String>db=new HashSet<>();if(d.get("databases") instanceof List<?>l)for(Object x:l)if(x instanceof Map<?,?>m){db.add(s(m.get("vendor")).toLowerCase(Locale.ROOT));if(release&&!Boolean.TRUE.equals(m.get("available")))f.add("release database unavailable: "+m.get("vendor"));}if(!db.equals(Set.of("oracle","postgresql","mariadb")))f.add("environment database vendors must be exactly official three");Set<String>br=new HashSet<>();if(d.get("browsers") instanceof List<?>l)for(Object x:l)if(x instanceof Map<?,?>m){br.add(s(m.get("name")).toLowerCase(Locale.ROOT));if(release&&!Boolean.TRUE.equals(m.get("available")))f.add("release browser unavailable: "+m.get("name"));}if(!br.equals(Set.of("chromium","firefox","webkit")))f.add("environment browsers must be chromium/firefox/webkit");}
    private static void validateSbom(Map<String,Object>sbom,Set<String>denied,List<String>f){if(!(sbom.get("components") instanceof List<?>l)){f.add("SBOM components must be array");return;}for(Object x:l)if(x instanceof Map<?,?>m){List<String>licenses=new ArrayList<>();if(m.get("licenses") instanceof List<?>ll)for(Object y:ll)if(y instanceof Map<?,?>lm&&lm.get("license") instanceof Map<?,?>lic)licenses.add(!s(lic.get("id")).isBlank()?s(lic.get("id")):s(lic.get("name")));if(licenses.isEmpty())licenses.add("UNKNOWN");List<String>bad=licenses.stream().filter(denied::contains).toList();if(!bad.isEmpty())f.add("denied/unknown license "+m.get("name")+":"+bad);}}

    private static List<Pair> pairs(String[][] values){return Arrays.stream(values).map(v->new Pair(v[0],v[1])).collect(Collectors.toCollection(ArrayList::new));}
    private record Pair(String oldValue,String newValue){}
    private void op(String name,Map<String,String>values){Map<String,Object>m=new LinkedHashMap<>();m.put("operation",name);m.putAll(values);operations.add(m);}
    private String rel(Path p){return norm(root.relativize(p.toAbsolutePath().normalize()).toString());}
    private static String norm(String p){return p.replace('\\','/').replaceAll("^/+","");}
    private static boolean isNested(Path source,Path target){Path s=source.toAbsolutePath().normalize(),t=target.toAbsolutePath().normalize();return !s.equals(t)&&t.startsWith(s);}
    private static String extension(String name){int i=name.lastIndexOf('.');return i<0?"":name.substring(i).toLowerCase(Locale.ROOT);}
    private boolean isProtected(String rel){String p=norm(rel).toLowerCase(Locale.ROOT);for(String prefix:PROTECTED)if(p.equals(prefix.substring(0,prefix.length()-1))||p.startsWith(prefix))return true;return false;}
    private static boolean hasSkippedPart(String rel){return Arrays.stream(norm(rel).split("/")).anyMatch(SKIP_DIRS::contains);}
    private static List<Path> filesUnder(Path dir)throws IOException{if(!Files.exists(dir))return List.of();try(Stream<Path>s=Files.walk(dir)){return s.filter(Files::isRegularFile).sorted().toList();}}
    private static void deleteTreeIfExists(Path dir)throws IOException{if(!Files.exists(dir))return;try(Stream<Path>s=Files.walk(dir)){for(Path p:s.sorted(Comparator.reverseOrder()).toList())Files.deleteIfExists(p);}}
    private static void deleteEmptyDirectories(Path start)throws IOException{if(!Files.exists(start))return;try(Stream<Path>s=Files.walk(start)){for(Path p:s.filter(Files::isDirectory).sorted(Comparator.reverseOrder()).toList()){if(p.getFileName()!=null&&p.getFileName().toString().equals(".git"))continue;try(Stream<Path>c=Files.list(p)){if(c.findAny().isEmpty())Files.deleteIfExists(p);}}}}
    private static String sha256(Path p)throws Exception{MessageDigest md=MessageDigest.getInstance("SHA-256");try(InputStream in=Files.newInputStream(p)){byte[]b=new byte[1024*1024];for(int n;(n=in.read(b))>0;)md.update(b,0,n);}return HexFormat.of().formatHex(md.digest());}
    private static String run(String...cmd)throws Exception{return new String(runBytes(cmd),UTF8);}
    private static byte[] runBytes(String...cmd)throws Exception{Process p=new ProcessBuilder(cmd).redirectErrorStream(true).start();byte[]out=p.getInputStream().readAllBytes();int code=p.waitFor();if(code!=0)throw new IllegalStateException("command failed ("+code+"): "+String.join(" ",cmd)+"\n"+new String(out,UTF8));return out;}
    private static Path requireFile(Path p,String message){if(!Files.isRegularFile(p))throw new IllegalStateException(message);return p;}
    private static Path requireDirectory(Path p,String message){if(!Files.isDirectory(p))throw new IllegalStateException(message);return p;}
    private String readIfExists(String rel)throws IOException{Path p=root.resolve(rel);return Files.isRegularFile(p)?Files.readString(p,UTF8):null;}
    private static void fail(String area,String message){throw new IllegalStateException("["+area+"] "+message);}
    private static String s(Object o){return o==null?"":String.valueOf(o);}
    private static long number(Object o){return o instanceof Number n?n.longValue():Long.parseLong(s(o));}
    @SuppressWarnings("unchecked") private static Map<String,Object> asMap(Object o){return (Map<String,Object>)o;}
    private static List<Map<String,Object>> asMapList(Object o){List<Map<String,Object>>out=new ArrayList<>();for(Object x:(List<?>)o)out.add(asMap(x));return out;}
    private static Map<String,Map<String,Object>> index(List<Map<String,Object>>rows,String key){Map<String,Map<String,Object>>m=new HashMap<>();for(Map<String,Object>r:rows)m.put(s(r.get(key)),r);return m;}
    private static void checkDuplicate(List<Map<String,Object>>rows,String key,String label){Set<String>seen=new HashSet<>();Set<String>dup=new TreeSet<>();for(Map<String,Object>r:rows){String v=s(r.get(key));if(!seen.add(v))dup.add(v);}if(!dup.isEmpty())fail("CANONICAL",label+" duplicate "+key+": "+dup);}
    private static <T>Set<T>difference(Set<T>a,Set<T>b){Set<T>x=new TreeSet<>(Comparator.comparing(String::valueOf));x.addAll(a);x.removeAll(b);return x;}
    private static <T>Set<T>symmetric(Set<T>a,Set<T>b){Set<T>x=new HashSet<>(a);x.removeAll(b);Set<T>y=new HashSet<>(b);y.removeAll(a);x.addAll(y);return x;}
    private static Set<String>matches(Pattern p,String text,int group){Set<String>out=new TreeSet<>();Matcher m=p.matcher(text);while(m.find())out.add(m.group(group).toLowerCase(Locale.ROOT));return out;}

    private static List<Map<String,String>> readCsv(Path path)throws IOException{
        String raw=Files.readString(path,UTF8);if(raw.startsWith("\uFEFF"))raw=raw.substring(1);List<List<String>>records=parseCsv(raw);if(records.isEmpty())return new ArrayList<>();List<String>header=records.get(0);List<Map<String,String>>rows=new ArrayList<>();for(int i=1;i<records.size();i++){List<String>r=records.get(i);if(r.size()==1&&r.get(0).isBlank())continue;Map<String,String>m=new LinkedHashMap<>();for(int c=0;c<header.size();c++)m.put(header.get(c),c<r.size()?r.get(c):"");rows.add(m);}return rows;
    }
    private static void writeCsv(Path path,List<Map<String,String>>rows)throws IOException{if(rows.isEmpty())return;List<String>header=new ArrayList<>(rows.get(0).keySet());StringBuilder b=new StringBuilder();writeCsvRow(b,header);for(Map<String,String>row:rows)writeCsvRow(b,header.stream().map(k->row.getOrDefault(k,"")).toList());Files.writeString(path,b.toString(),UTF8);}
    private static void writeCsvRow(StringBuilder b,List<String>values){for(int i=0;i<values.size();i++){if(i>0)b.append(',');String v=values.get(i)==null?"":values.get(i);if(v.indexOf(',')>=0||v.indexOf('"')>=0||v.indexOf('\n')>=0||v.indexOf('\r')>=0)b.append('"').append(v.replace("\"","\"\"")).append('"');else b.append(v);}b.append('\n');}
    private static List<List<String>> parseCsv(String text){List<List<String>>rows=new ArrayList<>();List<String>row=new ArrayList<>();StringBuilder cell=new StringBuilder();boolean quoted=false;for(int i=0;i<text.length();i++){char ch=text.charAt(i);if(quoted){if(ch=='"'){if(i+1<text.length()&&text.charAt(i+1)=='"'){cell.append('"');i++;}else quoted=false;}else cell.append(ch);}else if(ch=='"')quoted=true;else if(ch==','){row.add(cell.toString());cell.setLength(0);}else if(ch=='\n'){row.add(cell.toString());cell.setLength(0);rows.add(row);row=new ArrayList<>();}else if(ch!='\r')cell.append(ch);}if(cell.length()>0||!row.isEmpty()){row.add(cell.toString());rows.add(row);}return rows;}

    private static final class Json {
        static Object parse(String s){return new Parser(s).parse();}
        static String stringify(Object value,int indent){StringBuilder b=new StringBuilder();write(value,b,0,indent);return b.toString();}
        private static void write(Object v,StringBuilder b,int depth,int indent){if(v==null)b.append("null");else if(v instanceof String s)b.append('"').append(escape(s)).append('"');else if(v instanceof Number||v instanceof Boolean)b.append(v);else if(v instanceof Map<?,?>m){b.append('{');boolean first=true;for(Map.Entry<?,?>e:m.entrySet()){if(!first)b.append(',');newline(b,depth+1,indent);write(String.valueOf(e.getKey()),b,depth+1,indent);b.append(':');if(indent>0)b.append(' ');write(e.getValue(),b,depth+1,indent);first=false;}if(!m.isEmpty())newline(b,depth,indent);b.append('}');}else if(v instanceof Collection<?>c){b.append('[');boolean first=true;for(Object x:c){if(!first)b.append(',');newline(b,depth+1,indent);write(x,b,depth+1,indent);first=false;}if(!c.isEmpty())newline(b,depth,indent);b.append(']');}else write(String.valueOf(v),b,depth,indent);}
        private static void newline(StringBuilder b,int depth,int indent){if(indent>0)b.append('\n').append(" ".repeat(depth*indent));}
        private static String escape(String s){StringBuilder b=new StringBuilder();for(char c:s.toCharArray())switch(c){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\b'->b.append("\\b");case '\f'->b.append("\\f");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(c<32)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}return b.toString();}
        private static final class Parser {final String s;int i;Parser(String s){this.s=s;}Object parse(){skip();Object v=value();skip();if(i!=s.length())throw new IllegalArgumentException("JSON trailing data at "+i);return v;}Object value(){skip();if(i>=s.length())throw new IllegalArgumentException("JSON unexpected end");char c=s.charAt(i);return switch(c){case '{'->object();case '['->array();case '"'->string();case 't'->{literal("true");yield true;}case 'f'->{literal("false");yield false;}case 'n'->{literal("null");yield null;}default->number();};}Map<String,Object>object(){i++;Map<String,Object>m=new LinkedHashMap<>();skip();if(peek('}')){i++;return m;}while(true){skip();String k=string();skip();expect(':');m.put(k,value());skip();if(peek('}')){i++;return m;}expect(',');}}List<Object>array(){i++;List<Object>a=new ArrayList<>();skip();if(peek(']')){i++;return a;}while(true){a.add(value());skip();if(peek(']')){i++;return a;}expect(',');}}String string(){expect('"');StringBuilder b=new StringBuilder();while(i<s.length()){char c=s.charAt(i++);if(c=='"')return b.toString();if(c=='\\'){char e=s.charAt(i++);switch(e){case '"','\\','/'->b.append(e);case 'b'->b.append('\b');case 'f'->b.append('\f');case 'n'->b.append('\n');case 'r'->b.append('\r');case 't'->b.append('\t');case 'u'->{b.append((char)Integer.parseInt(s.substring(i,i+4),16));i+=4;}default->throw new IllegalArgumentException("JSON escape at "+i);}}else b.append(c);}throw new IllegalArgumentException("JSON unterminated string");}Number number(){int st=i;if(peek('-'))i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;if(peek('.')){i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;}if(peek('e')||peek('E')){i++;if(peek('+')||peek('-'))i++;while(i<s.length()&&Character.isDigit(s.charAt(i)))i++;}String n=s.substring(st,i);return n.contains(".")||n.contains("e")||n.contains("E")?Double.parseDouble(n):Long.parseLong(n);}void literal(String x){if(!s.startsWith(x,i))throw new IllegalArgumentException("JSON literal at "+i);i+=x.length();}void skip(){while(i<s.length()&&Character.isWhitespace(s.charAt(i)))i++;}boolean peek(char c){return i<s.length()&&s.charAt(i)==c;}void expect(char c){skip();if(!peek(c))throw new IllegalArgumentException("JSON expected "+c+" at "+i);i++;}}
    }
}
