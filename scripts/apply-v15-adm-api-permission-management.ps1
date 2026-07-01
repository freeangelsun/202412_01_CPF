param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $JdbcUrl = $env:ADM_DB_URL,
    [string] $Username = $env:ADM_DB_MIGRATION_USERNAME,
    [string] $Password = $env:ADM_DB_MIGRATION_PASSWORD,
    [string] $SqlPath = "",
    [string] $ResultPath = "",
    [string] $JdbcJar = ""
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($JdbcUrl)) {
    $JdbcUrl = "jdbc:mariadb://localhost:3306/admDB"
}
if ([string]::IsNullOrWhiteSpace($Username)) {
    $Username = "cpf_adm_migration"
}
if ([string]::IsNullOrWhiteSpace($Password)) {
    $Password = "cpf_local_pw"
}
if ([string]::IsNullOrWhiteSpace($SqlPath)) {
    $SqlPath = Join-Path $Root "specs/sql/migration/flyway/V15__adm_api_permission_management.sql"
}
if ([string]::IsNullOrWhiteSpace($ResultPath)) {
    $resultDir = Join-Path $Root "build/runtime-smoke"
    New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
    $ResultPath = Join-Path $resultDir "v15-adm-api-permission-result.json"
}

if (-not (Test-Path -LiteralPath $SqlPath)) {
    throw "V15 SQL file was not found: $SqlPath"
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

$tempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("cpf-v15-adm-api-permission-" + [guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null
$javaFile = Join-Path $tempDir "ApplyCpfAdmV15Sql.java"
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

public class ApplyCpfAdmV15Sql {
    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            throw new IllegalArgumentException("Usage: ApplyCpfAdmV15Sql <jdbcUrl> <username> <sqlPath> <resultPath>");
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
            counts.apiPermissionTableCount = queryLong(statement, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'adm_api_permission'");
            counts.roleApiPermissionTableCount = queryLong(statement, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'adm_role_api_permission'");
            if (counts.apiPermissionTableCount > 0) {
                counts.apiPermissionCount = queryLong(statement, "SELECT COUNT(*) FROM adm_api_permission");
                counts.writePutPermissionCount = queryLong(statement, "SELECT COUNT(*) FROM adm_api_permission WHERE API_PERMISSION_ID = 'API_PERMISSION_WRITE_PUT'");
            }
            if (counts.roleApiPermissionTableCount > 0) {
                counts.roleApiPermissionCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_api_permission");
                counts.viewerWritePutDenyCount = queryLong(statement, "SELECT COUNT(*) FROM adm_role_api_permission WHERE ROLE_ID = 'ADM_VIEWER' AND API_PERMISSION_ID = 'API_PERMISSION_WRITE_PUT' AND ALLOW_YN = 'N'");
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
        long apiPermissionTableCount;
        long roleApiPermissionTableCount;
        long apiPermissionCount;
        long roleApiPermissionCount;
        long writePutPermissionCount;
        long viewerWritePutDenyCount;

        String toJson() {
            return "{"
                    + "\"apiPermissionTableCount\":" + apiPermissionTableCount + ","
                    + "\"roleApiPermissionTableCount\":" + roleApiPermissionTableCount + ","
                    + "\"apiPermissionCount\":" + apiPermissionCount + ","
                    + "\"roleApiPermissionCount\":" + roleApiPermissionCount + ","
                    + "\"writePutPermissionCount\":" + writePutPermissionCount + ","
                    + "\"viewerWritePutDenyCount\":" + viewerWritePutDenyCount
                    + "}";
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Counts that)) {
                return false;
            }
            return apiPermissionTableCount == that.apiPermissionTableCount
                    && roleApiPermissionTableCount == that.roleApiPermissionTableCount
                    && apiPermissionCount == that.apiPermissionCount
                    && roleApiPermissionCount == that.roleApiPermissionCount
                    && writePutPermissionCount == that.writePutPermissionCount
                    && viewerWritePutDenyCount == that.viewerWritePutDenyCount;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(apiPermissionTableCount + roleApiPermissionTableCount
                    + apiPermissionCount + roleApiPermissionCount
                    + writePutPermissionCount + viewerWritePutDenyCount);
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
    & java -cp "$tempJdbcJar;$tempDir" ApplyCpfAdmV15Sql $JdbcUrl $Username $SqlPath $ResultPath
    if ($LASTEXITCODE -ne 0) {
        throw "java ApplyCpfAdmV15Sql failed with exit code $LASTEXITCODE"
    }
} finally {
    Remove-Item Env:\CPF_SQL_PASSWORD -ErrorAction SilentlyContinue
    if (Test-Path -LiteralPath $tempDir) {
        Remove-Item -LiteralPath $tempDir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "V15 ADM API permission migration applied. Result: $ResultPath"
