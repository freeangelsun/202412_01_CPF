package com.cpf.common.sample;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CmnSampleSqlDialectTest {
    @Test
    void resolvesOnlyOfficialDatabaseVendors() {
        assertThat(CmnSampleSqlDialect.fromDatabaseProductName("MariaDB"))
                .isEqualTo(CmnSampleSqlDialect.MARIADB);
        assertThat(CmnSampleSqlDialect.fromDatabaseProductName("PostgreSQL"))
                .isEqualTo(CmnSampleSqlDialect.POSTGRESQL);
        assertThat(CmnSampleSqlDialect.fromDatabaseProductName("Oracle Database 21c"))
                .isEqualTo(CmnSampleSqlDialect.ORACLE);
        assertThatThrownBy(() -> CmnSampleSqlDialect.fromDatabaseProductName("MySQL"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported");
    }

    @Test
    void oracleUsesNativeConcatenationAndRowLimitingClause() {
        String offsetSql = CmnSampleSqlDialect.ORACLE.offsetPageSql();
        assertThat(offsetSql)
                .contains("NVL(searchable_text, '')")
                .contains("OFFSET ? ROWS FETCH NEXT ? ROWS ONLY")
                .doesNotContain("CONCAT(item_name")
                .doesNotContain("LIMIT ?");
        assertThat(CmnSampleSqlDialect.ORACLE.cursorPageSql())
                .contains("FETCH FIRST ? ROWS ONLY")
                .doesNotContain("LIMIT ?");
    }

    @Test
    void mariadbAndPostgresqlUseLimitAndExpectedParameterOrder() {
        for (CmnSampleSqlDialect dialect : new CmnSampleSqlDialect[] {
                CmnSampleSqlDialect.MARIADB,
                CmnSampleSqlDialect.POSTGRESQL}) {
            assertThat(dialect.offsetPageSql())
                    .contains("CONCAT(item_name")
                    .contains("LIMIT ? OFFSET ?");
            assertThat(dialect.offsetPageParameters("ACTIVE", "cpf", 20, 10))
                    .containsExactly("ACTIVE", "ACTIVE", "cpf", "cpf", 10, 20);
        }
        assertThat(CmnSampleSqlDialect.ORACLE.offsetPageParameters("ACTIVE", "cpf", 20, 10))
                .containsExactly("ACTIVE", "ACTIVE", "cpf", "cpf", 20, 10);
    }
}
