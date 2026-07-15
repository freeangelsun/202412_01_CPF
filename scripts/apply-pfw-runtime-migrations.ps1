param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $JdbcUrl = $env:PFW_DB_URL,
    [string] $Username = $env:PFW_DB_MIGRATION_USERNAME,
    [string] $Password = $env:PFW_DB_MIGRATION_PASSWORD,
    [string] $ResultPath = "",
    [string] $JdbcJar = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($JdbcUrl)) {
    $JdbcUrl = "jdbc:mariadb://localhost:3306/pfwDB"
}
if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = "cpf_pfw_migration"
}
if ([string]::IsNullOrWhiteSpace($Password)) {
    throw "PFW_DB_MIGRATION_PASSWORD 환경변수 또는 -Password 인수가 필요합니다."
}
if ([string]::IsNullOrWhiteSpace($ResultPath)) {
    $resultDir = Join-Path $Root "build/runtime-smoke"
    New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
    $ResultPath = Join-Path $resultDir "pfw-runtime-migrations-result.json"
}

$sqlFiles = @(
    (Join-Path $Root "specs/sql/migration/flyway/V11__transaction_meta_log_policy.sql"),
    (Join-Path $Root "specs/sql/migration/flyway/V12__log_policy_runtime_standard.sql")
)
foreach ($sqlFile in $sqlFiles) {
    if (-not (Test-Path -LiteralPath $sqlFile)) {
        throw "PFW runtime migration SQL was not found: $sqlFile"
    }
}

if ([string]::IsNullOrWhiteSpace($JdbcJar)) {
    $jdbcJarItem = Get-ChildItem -LiteralPath "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1\org.mariadb.jdbc\mariadb-java-client" `
        -Recurse `
        -File `
        -Filter "mariadb-java-client-*.jar" `
        -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike "*sources*" -and $_.Name -notlike "*javadoc*" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $jdbcJarItem) {
        throw "MariaDB JDBC driver was not found in the Gradle cache."
    }
    $JdbcJar = $jdbcJarItem.FullName
}

$tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("cpf-pfw-migrations-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
$javaFile = Join-Path $tempDir "ApplyCpfPfwSql.java"
$tempJdbcJar = Join-Path $tempDir "mariadb-java-client.jar"

$javaSource = @'
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ApplyCpfPfwSql {
    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            throw new IllegalArgumentException("Usage: ApplyCpfPfwSql <jdbcUrl> <username> <resultPath> <sqlPath...>");
        }
        String jdbcUrl = args[0];
        String username = args[1];
        String resultPath = args[2];
        String password = System.getenv("CPF_SQL_PASSWORD");
        if (password == null) {
            password = "";
        }

        Class.forName("org.mariadb.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            connection.setAutoCommit(false);
            Counts before = readCounts(connection);
            int firstRunStatements = executeAll(connection, args, 3);
            Counts afterFirstRun = readCounts(connection);
            int secondRunStatements = executeAll(connection, args, 3);
            Counts afterSecondRun = readCounts(connection);
            connection.commit();

            StringBuilder files = new StringBuilder("[");
            for (int i = 3; i < args.length; i++) {
                if (i > 3) {
                    files.append(',');
                }
                files.append('"').append(json(args[i].replace('\\', '/'))).append('"');
            }
            files.append(']');

            String json = "{\n"
                    + "  \"startedAt\": \"" + json(OffsetDateTime.now().toString()) + "\",\n"
                    + "  \"jdbcUrl\": \"" + json(maskJdbcUrl(jdbcUrl)) + "\",\n"
                    + "  \"username\": \"" + json(username) + "\",\n"
                    + "  \"sqlFiles\": " + files + ",\n"
                    + "  \"firstRunStatements\": " + firstRunStatements + ",\n"
                    + "  \"secondRunStatements\": " + secondRunStatements + ",\n"
                    + "  \"before\": " + before.toJson() + ",\n"
                    + "  \"afterFirstRun\": " + afterFirstRun.toJson() + ",\n"
                    + "  \"afterSecondRun\": " + afterSecondRun.toJson() + ",\n"
                    + "  \"idempotent\": " + afterFirstRun.equals(afterSecondRun) + ",\n"
                    + "  \"completedAt\": \"" + json(OffsetDateTime.now().toString()) + "\"\n"
                    + "}\n";
            Files.writeString(Path.of(resultPath), json, StandardCharsets.UTF_8);
        }
    }

    private static int executeAll(Connection connection, String[] args, int startIndex) throws Exception {
        int executed = 0;
        for (int i = startIndex; i < args.length; i++) {
            executed += executeSql(connection, args[i]);
        }
        return executed;
    }

    private static Counts readCounts(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            Counts counts = new Counts();
            counts.metaTableCount = queryLong(statement, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'pfw_transaction_meta'");
            counts.policyTableCount = queryLong(statement, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'pfw_log_policy'");
            counts.overrideTableCount = queryLong(statement, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'pfw_log_policy_override'");
            counts.auditTableCount = queryLong(statement, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'pfw_log_policy_audit'");
            if (counts.policyTableCount > 0) {
                counts.policyCount = queryLong(statement, "SELECT COUNT(*) FROM pfw_log_policy");
                counts.onlineTransactionPolicyCount = queryLong(statement, "SELECT COUNT(*) FROM pfw_log_policy WHERE policy_key='ONLINE_DEFAULT' AND target_type='ONLINE_TRANSACTION'");
                counts.legacyTransactionPolicyCount = queryLong(statement, "SELECT COUNT(*) FROM pfw_log_policy WHERE target_type='TRANSACTION'");
            }
            if (counts.metaTableCount > 0) {
                counts.metaRowCount = queryLong(statement, "SELECT COUNT(*) FROM pfw_transaction_meta");
            }
            return counts;
        }
    }

    private static int executeSql(Connection connection, String sqlPath) throws Exception {
        String sql = Files.readString(Path.of(sqlPath), StandardCharsets.UTF_8);
        List<String> statements = splitStatements(sql);
        int executed = 0;
        try (Statement statement = connection.createStatement()) {
            for (String raw : statements) {
                String normalized = raw.trim();
                if (normalized.isEmpty() || normalized.startsWith("--")) {
                    continue;
                }
                statement.execute(normalized);
                executed++;
            }
        }
        return executed;
    }

    private static List<String> splitStatements(String sql) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) {
                inSingleQuote = !inSingleQuote;
            }
            if (ch == ';' && !inSingleQuote) {
                statements.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (current.length() > 0) {
            statements.add(current.toString());
        }
        return statements;
    }

    private static long queryLong(Statement statement, String sql) throws Exception {
        try (ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    private static String maskJdbcUrl(String jdbcUrl) {
        return jdbcUrl.replaceAll("(?i)(password=)[^;&]+", "$1****");
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class Counts {
        long metaTableCount;
        long policyTableCount;
        long overrideTableCount;
        long auditTableCount;
        long policyCount;
        long onlineTransactionPolicyCount;
        long legacyTransactionPolicyCount;
        long metaRowCount;

        String toJson() {
            return "{"
                    + "\"metaTableCount\":" + metaTableCount + ","
                    + "\"policyTableCount\":" + policyTableCount + ","
                    + "\"overrideTableCount\":" + overrideTableCount + ","
                    + "\"auditTableCount\":" + auditTableCount + ","
                    + "\"policyCount\":" + policyCount + ","
                    + "\"onlineTransactionPolicyCount\":" + onlineTransactionPolicyCount + ","
                    + "\"legacyTransactionPolicyCount\":" + legacyTransactionPolicyCount + ","
                    + "\"metaRowCount\":" + metaRowCount
                    + "}";
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Counts that)) {
                return false;
            }
            return metaTableCount == that.metaTableCount
                    && policyTableCount == that.policyTableCount
                    && overrideTableCount == that.overrideTableCount
                    && auditTableCount == that.auditTableCount
                    && policyCount == that.policyCount
                    && onlineTransactionPolicyCount == that.onlineTransactionPolicyCount
                    && legacyTransactionPolicyCount == that.legacyTransactionPolicyCount
                    && metaRowCount == that.metaRowCount;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(metaTableCount + policyTableCount + overrideTableCount + auditTableCount
                    + policyCount + onlineTransactionPolicyCount + legacyTransactionPolicyCount + metaRowCount);
        }
    }
}
'@

try {
    Copy-Item -LiteralPath $JdbcJar -Destination $tempJdbcJar -Force
    [System.IO.File]::WriteAllText($javaFile, $javaSource, [System.Text.UTF8Encoding]::new($false))
    & javac -encoding UTF-8 $javaFile
    if ($LASTEXITCODE -ne 0) {
        throw "javac failed with exit code $LASTEXITCODE"
    }
    $env:CPF_SQL_PASSWORD = $Password
    & java -cp "$tempJdbcJar;$tempDir" ApplyCpfPfwSql $JdbcUrl $Username $ResultPath @sqlFiles
    if ($LASTEXITCODE -ne 0) {
        throw "java ApplyCpfPfwSql failed with exit code $LASTEXITCODE"
    }
} finally {
    Remove-Item Env:\CPF_SQL_PASSWORD -ErrorAction SilentlyContinue
    if (Test-Path -LiteralPath $tempDir) {
        Remove-Item -LiteralPath $tempDir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "PFW runtime migrations applied. Result: $ResultPath"
