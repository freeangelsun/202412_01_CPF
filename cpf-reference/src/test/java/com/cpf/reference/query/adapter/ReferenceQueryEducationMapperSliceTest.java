package com.cpf.reference.query.adapter;

import com.cpf.reference.query.dto.ReferenceQueryEducationCriteria;
import com.cpf.reference.query.dto.ReferenceQueryEducationItem;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * REF 조회/CRUD EDU MyBatis XML을 실제 DB fixture로 검증하는 선택 실행 테스트입니다.
 *
 * <p>기본 Gradle test는 개발자 PC의 MariaDB를 임의로 건드리지 않도록 DB 구간을 skip합니다.
 * 공식 Provision/Install/Test Seed가 끝난 격리 DB와 환경변수를 지정하면 Mapper XML을 실제로 실행합니다.</p>
 */
class ReferenceQueryEducationMapperSliceTest {
    private static final String ENABLED_ENV = "CPF_REF_EDU_MAPPER_SLICE_TEST";
    private static final String DB_URL_ENV = "CPF_REF_EDU_MAPPER_DB_URL";
    private static final String DB_USERNAME_ENV = "CPF_REF_EDU_MAPPER_DB_USERNAME";
    private static final String LEGACY_DB_USER_ENV = "CPF_REF_EDU_MAPPER_DB_USER";
    private static final String DB_PASSWORD_ENV = "CPF_REF_EDU_MAPPER_DB_PASSWORD";
    private static final String DB_DRIVER_ENV = "CPF_REF_EDU_MAPPER_DB_DRIVER";

    @Test
    void mapperXmlDoesNotUseUnsafeStringSubstitutionForSort() throws Exception {
        Resource mapperXml = vendorMapperResource();
        String xml = mapperXml.getContentAsString(StandardCharsets.UTF_8);

        assertThat(xml)
                .contains("<choose>")
                .contains("ORDER BY sample_item_id ASC")
                .contains("criteria.sortCode == 'NAME_ASC'")
                .contains("criteria.sortCode == 'CREATED_DESC'")
                .contains("insertCrudItem")
                .contains("logicalDeleteCrudItem")
                .doesNotContain("${");
        assertThat(xml.toLowerCase())
                .doesNotContain(" join mbr_member")
                .doesNotContain(" join exs_")
                .doesNotContain("from mbr_member")
                .doesNotContain("from exs_");
    }

    @Test
    void officialVendorTestSeedOwnsEduMapperDataWithoutSchemaDdl() throws Exception {
        Path seedPath = vendorResourceRoot().resolve("seed").resolve("00_test_seed.sql");
        String sql = Files.readString(seedPath, StandardCharsets.UTF_8);

        assertThat(seedPath).isRegularFile();
        assertThat(sql)
                .contains("DELETE FROM ref_sample_item WHERE sample_item_id BETWEEN 90001 AND 90008")
                .contains("DELETE FROM ref_sample_item WHERE sample_item_id BETWEEN 91000 AND 91999")
                .contains("REF-MAPPER-90001")
                .contains("REF-MAPPER-90008")
                .doesNotContain("CREATE TABLE")
                .doesNotContain("DROP TABLE")
                .doesNotContain("TRUNCATE TABLE")
                .doesNotContain("DELETE FROM ref_sample_item;")
                .doesNotContain("DELETE FROM ref_sample_item WHERE 1=1");
    }

    @Test
    void mapperXmlRunsWithFixtureWhenSafeTestDatabaseIsProvided() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv(ENABLED_ENV)),
                "안전한 테스트 DB가 명시된 경우에만 실제 Mapper slice를 실행합니다.");

        DataSource dataSource = testDataSource();

        SqlSessionFactory sqlSessionFactory = sqlSessionFactory(dataSource);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            ReferenceQueryEducationMapper mapper = session.getMapper(ReferenceQueryEducationMapper.class);

            ReferenceQueryEducationItem single = mapper.findById(90001L);
            assertThat(single).isNotNull();
            assertThat(single.itemName()).isEqualTo("단건 조회 샘플");

            List<ReferenceQueryEducationItem> list = mapper.findItems(criteria(null, "ACTIVE", "ID_ASC", 20, 0, null));
            assertThat(list)
                    .extracting(ReferenceQueryEducationItem::itemId)
                    .contains(90001L, 90002L, 90003L, 90004L, 90005L, 90008L)
                    .doesNotContain(90007L);

            List<ReferenceQueryEducationItem> searched = mapper.findItems(criteria("검색", "ACTIVE", "ID_ASC", 10, 0, null));
            assertThat(searched)
                    .extracting(ReferenceQueryEducationItem::itemId)
                    .containsExactly(90003L);

            List<ReferenceQueryEducationItem> sorted = mapper.findItems(criteria(null, "ACTIVE", "CREATED_DESC", 3, 0, null));
            assertThat(sorted)
                    .extracting(ReferenceQueryEducationItem::itemId)
                    .containsExactly(90008L, 90005L, 90004L);

            List<ReferenceQueryEducationItem> unsafeSortFallsBack = mapper.findItems(criteria(null, "ACTIVE", "item_name desc; drop table", 3, 0, null));
            assertThat(unsafeSortFallsBack)
                    .extracting(ReferenceQueryEducationItem::itemId)
                    .containsExactly(90001L, 90002L, 90003L);

            List<ReferenceQueryEducationItem> offsetPage = mapper.findOffsetPageItems(criteria(null, "ACTIVE", "ID_ASC", 2, 2, null));
            assertThat(offsetPage)
                    .extracting(ReferenceQueryEducationItem::itemId)
                    .containsExactly(90003L, 90004L);
            assertThat(mapper.countOffsetPageItems(criteria(null, "ACTIVE", "ID_ASC", 2, 0, null)))
                    .isEqualTo(6);

            List<ReferenceQueryEducationItem> keysetPageWithLimitPlusOne = mapper.findKeysetPageItems(criteria(null, "ACTIVE", "ID_ASC", 3, 0, 90002L));
            assertThat(keysetPageWithLimitPlusOne)
                    .extracting(ReferenceQueryEducationItem::itemId)
                    .containsExactly(90003L, 90004L, 90005L);

            Long itemId = mapper.nextCrudItemId();
            mapper.insertCrudItem(itemId, "CRUD 등록 샘플", "CRUD", "ACTIVE", "MBR-CRUD-01", "MAPPER_TEST");
            assertThat(mapper.findById(itemId).itemName()).isEqualTo("CRUD 등록 샘플");
            assertThat(mapper.updateCrudItem(itemId, "CRUD 수정 샘플", "CRUD_UPD", "MBR-CRUD-02", "MAPPER_TEST")).isEqualTo(1);
            assertThat(mapper.findById(itemId).categoryCode()).isEqualTo("CRUD_UPD");
            assertThat(mapper.updateCrudItemStatus(itemId, "INACTIVE", "MAPPER_TEST")).isEqualTo(1);
            assertThat(mapper.findById(itemId).statusCode()).isEqualTo("INACTIVE");
            assertThat(mapper.logicalDeleteCrudItem(itemId, "MAPPER_TEST")).isEqualTo(1);
            assertThat(mapper.findById(itemId)).isNull();
        }
    }

    private static Resource vendorMapperResource() throws Exception {
        Path mapperRoot = vendorResourceRoot().resolve("runtime").resolve("ref").resolve("mybatis");
        if (!Files.isDirectory(mapperRoot)) {
            throw new IllegalStateException("REF 중앙 MyBatis 디렉터리가 없습니다. path=" + mapperRoot);
        }

        List<Path> matches;
        try (var paths = Files.walk(mapperRoot)) {
            matches = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("ReferenceQueryEducationMapper.xml"))
                    .toList();
        }

        if (matches.size() != 1) {
            throw new IllegalStateException(
                    "REF Query EDU Mapper는 중앙 Vendor Pack에 정확히 1개여야 합니다. count="
                            + matches.size() + ", root=" + mapperRoot);
        }
        return new FileSystemResource(matches.get(0));
    }
    private static DataSource testDataSource() {
        String url = requiredEnv(DB_URL_ENV);
        String user = requiredFirstEnv(DB_USERNAME_ENV, LEGACY_DB_USER_ENV);
        String password = requiredEnv(DB_PASSWORD_ENV);
        String driver = System.getenv(DB_DRIVER_ENV);
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driver == null || driver.isBlank() ? selectedDriverClassName() : driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    private static SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        Resource[] mapperLocations = new Resource[] { vendorMapperResource() };
        factoryBean.setMapperLocations(mapperLocations);
        SqlSessionFactory sqlSessionFactory = factoryBean.getObject();
        if (sqlSessionFactory == null) {
            throw new IllegalStateException("EDU 조회 Mapper SqlSessionFactory를 생성하지 못했습니다.");
        }
        if (!sqlSessionFactory.getConfiguration().hasMapper(ReferenceQueryEducationMapper.class)) {
            sqlSessionFactory.getConfiguration().addMapper(ReferenceQueryEducationMapper.class);
        }
        return sqlSessionFactory;
    }

    private static ReferenceQueryEducationCriteria criteria(
            String keyword,
            String statusCode,
            String sortCode,
            int limit,
            int offset,
            Long cursorId) {
        return new ReferenceQueryEducationCriteria(keyword, statusCode, sortCode, limit, offset, cursorId);
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Mapper slice 테스트 DB 환경변수가 필요합니다. name=" + name);
        }
        return value;
    }

    private static String requiredFirstEnv(String primaryName, String fallbackName) {
        String primaryValue = System.getenv(primaryName);
        if (primaryValue != null && !primaryValue.isBlank()) {
            return primaryValue;
        }
        String fallbackValue = System.getenv(fallbackName);
        if (fallbackValue != null && !fallbackValue.isBlank()) {
            return fallbackValue;
        }
        throw new IllegalStateException("Mapper slice 테스트 DB 사용자 환경변수가 필요합니다. name="
                + primaryName + " 또는 " + fallbackName);
    }

    private static Path vendorResourceRoot() {
        String resourceRoot = System.getProperty("cpf.db.resource-root");
        if (resourceRoot == null || resourceRoot.isBlank()) {
            throw new IllegalStateException("중앙 Vendor Pack 경로가 필요합니다. property=cpf.db.resource-root");
        }
        Path root = Path.of(resourceRoot).toAbsolutePath().normalize();
        if (!Files.isDirectory(root)) {
            throw new IllegalStateException("중앙 Vendor Pack 경로가 없습니다. path=" + root);
        }
        return root;
    }

    private static String selectedDriverClassName() {
        return switch (System.getProperty("cpf.db.vendor", "mariadb").toLowerCase(java.util.Locale.ROOT)) {
            case "postgresql" -> "org.postgresql.Driver";
            case "oracle" -> "oracle.jdbc.OracleDriver";
            case "mariadb" -> "org.mariadb.jdbc.Driver";
            default -> throw new IllegalStateException(
                    "지원하지 않는 테스트 DB Vendor입니다. vendor=" + System.getProperty("cpf.db.vendor"));
        };
    }
}
