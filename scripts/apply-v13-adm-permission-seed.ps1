param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $JdbcUrl = $env:ADM_DB_URL,
    [string] $Username = $env:ADM_DB_USERNAME,
    [string] $Password = $env:ADM_DB_PASSWORD,
    [string] $SqlPath = "",
    [string] $ResultPath = "",
    [string] $JdbcJar = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($JdbcUrl)) {
    $JdbcUrl = "jdbc:mariadb://localhost:3306/admDB"
}
if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = "cpf_adm_app"
}
if ([string]::IsNullOrWhiteSpace($Password)) {
    throw "ADM_DB_PASSWORD 환경변수 또는 -Password 인수가 필요합니다."
}
if ([string]::IsNullOrWhiteSpace($SqlPath)) {
    $SqlPath = Join-Path $Root "specs/sql/migration/flyway/V13__adm_runtime_policy_permission_seed.sql"
}
if ([string]::IsNullOrWhiteSpace($ResultPath)) {
    $resultDir = Join-Path $Root "build/runtime-smoke"
    New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
    $ResultPath = Join-Path $resultDir "v13-adm-permission-seed-result.json"
}

if (-not (Test-Path -LiteralPath $SqlPath)) {
    throw "V13 SQL file was not found: $SqlPath"
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

$tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("cpf-v13-seed-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
$javaFile = Join-Path $tempDir "ApplyCpfSql.java"
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

public class ApplyCpfSql {
    private static final String[] BUTTON_IDS = {
            "TRANSACTION_META_READ",
            "TRANSACTION_META_SCAN",
            "TRANSACTION_META_WRITE",
            "LOG_POLICY_READ",
            "LOG_POLICY_WRITE",
            "LOG_POLICY_OVERRIDE",
            "LOG_POLICY_CACHE_REFRESH",
            "LOG_POLICY_CACHE_CLEAR"
    };

    private static final String[] ROLE_IDS = {
            "ADM_ADMIN",
            "ADM_DEV_OPERATOR",
            "ADM_OPERATOR",
            "ADM_BIZ_OPERATOR",
            "ADM_VIEWER"
    };

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: ApplyCpfSql <jdbcUrl> <username> <sqlPath> <resultPath>");
        }
        String jdbcUrl = args[0];
        String username = args[1];
        String sqlPath = args[2];
        String resultPath = args[3];
        String password = System.getenv("CPF_SQL_PASSWORD");
        if (password == null) {
            password = "";
        }

        Class.forName("org.mariadb.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            connection.setAutoCommit(false);
            Counts before = readCounts(connection);
            int firstRunStatements = executeSql(connection, sqlPath);
            Counts afterFirstRun = readCounts(connection);
            int secondRunStatements = executeSql(connection, sqlPath);
            Counts afterSecondRun = readCounts(connection);
            connection.commit();

            String json = "{\n"
                    + "  \"startedAt\": \"" + json(OffsetDateTime.now().toString()) + "\",\n"
                    + "  \"jdbcUrl\": \"" + json(maskJdbcUrl(jdbcUrl)) + "\",\n"
                    + "  \"username\": \"" + json(username) + "\",\n"
                    + "  \"sqlPath\": \"" + json(sqlPath.replace('\\', '/')) + "\",\n"
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

    private static Counts readCounts(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            Counts counts = new Counts();
            counts.menuCount = queryLong(statement, "SELECT COUNT(*) FROM adm_menu WHERE MENU_ID IN ('TRANSACTION_META','LOG_POLICY')");
            counts.adminMenuWriteCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_menu WHERE ROLE_ID='ADM_ADMIN' AND MENU_ID IN ('TRANSACTION_META','LOG_POLICY') AND READ_YN='Y' AND WRITE_YN='Y'");
            counts.operatorMenuWriteCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_menu WHERE ROLE_ID IN ('ADM_DEV_OPERATOR','ADM_OPERATOR') AND MENU_ID IN ('TRANSACTION_META','LOG_POLICY') AND READ_YN='Y' AND WRITE_YN='Y'");
            counts.readOnlyMenuCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_menu WHERE ROLE_ID IN ('ADM_BIZ_OPERATOR','ADM_VIEWER') AND MENU_ID IN ('TRANSACTION_META','LOG_POLICY') AND READ_YN='Y' AND WRITE_YN='N'");
            counts.buttonCount = queryLong(statement, "SELECT COUNT(*) FROM adm_button WHERE BUTTON_ID IN (" + quotedList(BUTTON_IDS) + ")");
            counts.adminAllowedCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_button WHERE ROLE_ID='ADM_ADMIN' AND BUTTON_ID IN (" + quotedList(BUTTON_IDS) + ") AND ALLOW_YN='Y'");
            counts.operatorAllowedCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_button WHERE ROLE_ID IN ('ADM_DEV_OPERATOR','ADM_OPERATOR') AND BUTTON_ID IN (" + quotedList(BUTTON_IDS) + ") AND ALLOW_YN='Y'");
            counts.readOnlyAllowedCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_button WHERE ROLE_ID IN ('ADM_BIZ_OPERATOR','ADM_VIEWER') AND BUTTON_ID IN ('TRANSACTION_META_READ','LOG_POLICY_READ') AND ALLOW_YN='Y'");
            counts.scanGrantCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_button WHERE ROLE_ID IN (" + quotedList(ROLE_IDS) + ") AND BUTTON_ID='TRANSACTION_META_SCAN' AND ALLOW_YN='Y'");
            counts.cacheRefreshGrantCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_button WHERE ROLE_ID IN (" + quotedList(ROLE_IDS) + ") AND BUTTON_ID='LOG_POLICY_CACHE_REFRESH' AND ALLOW_YN='Y'");
            counts.cacheClearGrantCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_button WHERE ROLE_ID IN (" + quotedList(ROLE_IDS) + ") AND BUTTON_ID='LOG_POLICY_CACHE_CLEAR' AND ALLOW_YN='Y'");
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

    private static String quotedList(String[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('\'').append(values[i]).append('\'');
        }
        return builder.toString();
    }

    private static String maskJdbcUrl(String jdbcUrl) {
        return jdbcUrl.replaceAll("(?i)(password=)[^;&]+", "$1****");
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final class Counts {
        long menuCount;
        long adminMenuWriteCount;
        long operatorMenuWriteCount;
        long readOnlyMenuCount;
        long buttonCount;
        long adminAllowedCount;
        long operatorAllowedCount;
        long readOnlyAllowedCount;
        long scanGrantCount;
        long cacheRefreshGrantCount;
        long cacheClearGrantCount;

        String toJson() {
            return "{"
                    + "\"menuCount\":" + menuCount + ","
                    + "\"adminMenuWriteCount\":" + adminMenuWriteCount + ","
                    + "\"operatorMenuWriteCount\":" + operatorMenuWriteCount + ","
                    + "\"readOnlyMenuCount\":" + readOnlyMenuCount + ","
                    + "\"buttonCount\":" + buttonCount + ","
                    + "\"adminAllowedCount\":" + adminAllowedCount + ","
                    + "\"operatorAllowedCount\":" + operatorAllowedCount + ","
                    + "\"readOnlyAllowedCount\":" + readOnlyAllowedCount + ","
                    + "\"scanGrantCount\":" + scanGrantCount + ","
                    + "\"cacheRefreshGrantCount\":" + cacheRefreshGrantCount + ","
                    + "\"cacheClearGrantCount\":" + cacheClearGrantCount
                    + "}";
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Counts that)) {
                return false;
            }
            return menuCount == that.menuCount
                    && adminMenuWriteCount == that.adminMenuWriteCount
                    && operatorMenuWriteCount == that.operatorMenuWriteCount
                    && readOnlyMenuCount == that.readOnlyMenuCount
                    && buttonCount == that.buttonCount
                    && adminAllowedCount == that.adminAllowedCount
                    && operatorAllowedCount == that.operatorAllowedCount
                    && readOnlyAllowedCount == that.readOnlyAllowedCount
                    && scanGrantCount == that.scanGrantCount
                    && cacheRefreshGrantCount == that.cacheRefreshGrantCount
                    && cacheClearGrantCount == that.cacheClearGrantCount;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(menuCount + adminMenuWriteCount + operatorMenuWriteCount + readOnlyMenuCount
                    + buttonCount + adminAllowedCount + operatorAllowedCount
                    + readOnlyAllowedCount + scanGrantCount + cacheRefreshGrantCount + cacheClearGrantCount);
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
    & java -cp "$tempJdbcJar;$tempDir" ApplyCpfSql $JdbcUrl $Username $SqlPath $ResultPath
    if ($LASTEXITCODE -ne 0) {
        throw "java ApplyCpfSql failed with exit code $LASTEXITCODE"
    }
} finally {
    Remove-Item Env:\CPF_SQL_PASSWORD -ErrorAction SilentlyContinue
    if (Test-Path -LiteralPath $tempDir) {
        Remove-Item -LiteralPath $tempDir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "V13 ADM permission seed applied. Result: $ResultPath"
