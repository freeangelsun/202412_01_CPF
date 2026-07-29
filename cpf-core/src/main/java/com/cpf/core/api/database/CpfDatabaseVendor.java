package com.cpf.core.api.database;

import java.util.Arrays;
import java.util.Locale;

/**
 * CPF 공식 지원 DB Vendor 계약입니다.
 *
 * <p>공식 지원 Vendor는 MariaDB, PostgreSQL, Oracle 3종으로 고정합니다. 그 외 Vendor adapter가
 * 필요하면 CPF 제품 선택 Surface와 분리된 고객 확장 영역에서 명시적으로 구현해야 합니다.</p>
 */
public enum CpfDatabaseVendor {
    MARIADB("mariadb", "org.mariadb.jdbc.Driver", 3306),
    POSTGRESQL("postgresql", "org.postgresql.Driver", 5432),
    ORACLE("oracle", "oracle.jdbc.OracleDriver", 1521);

    private final String id;
    private final String driverClassName;
    private final int defaultPort;

    CpfDatabaseVendor(String id,String driverClassName,int defaultPort){this.id=id;this.driverClassName=driverClassName;this.defaultPort=defaultPort;}
    public String id(){return id;}
    public String driverClassName(){return driverClassName;}
    public int defaultPort(){return defaultPort;}
    public String myBatisDatabaseId(){return id;}
    public String springBatchDatabaseType(){return switch(this){case MARIADB->"MARIADB";case POSTGRESQL->"POSTGRES";case ORACLE->"ORACLE";};}

    public String jdbcUrl(String host,Integer configuredPort,String databaseName){
        String safeHost=requireText(host,"host"),safeDatabaseName=requireText(databaseName,"databaseName");
        int port=configuredPort==null?defaultPort:configuredPort;if(port<1||port>65535)throw new IllegalArgumentException("port는 1~65535여야 합니다.");
        return switch(this){
            case MARIADB->"jdbc:mariadb://%s:%d/%s".formatted(safeHost,port,safeDatabaseName);
            case POSTGRESQL->"jdbc:postgresql://%s:%d/%s".formatted(safeHost,port,safeDatabaseName);
            case ORACLE->"jdbc:oracle:thin:@//%s:%d/%s".formatted(safeHost,port,safeDatabaseName);
        };
    }

    public boolean accepts(String jdbcUrl){if(jdbcUrl==null)return false;String normalized=jdbcUrl.trim().toLowerCase(Locale.ROOT);return switch(this){
        case MARIADB->normalized.startsWith("jdbc:mariadb:");case POSTGRESQL->normalized.startsWith("jdbc:postgresql:");case ORACLE->normalized.startsWith("jdbc:oracle:");};}

    public static CpfDatabaseVendor from(String value){
        String normalized=requireText(value,"cpf.db.vendor").toLowerCase(Locale.ROOT).replace("-","").replace("_","");
        return Arrays.stream(values()).filter(v->v.id.replace("-","").equals(normalized)).findFirst()
                .orElseThrow(()->new IllegalArgumentException("지원하지 않는 cpf.db.vendor입니다: "+value+" (mariadb|postgresql|oracle)"));
    }
    private static String requireText(String value,String fieldName){if(value==null||value.isBlank())throw new IllegalArgumentException(fieldName+"는 필수입니다.");return value.trim();}
}
