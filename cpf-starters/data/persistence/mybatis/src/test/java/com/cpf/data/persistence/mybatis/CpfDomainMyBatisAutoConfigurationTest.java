package com.cpf.data.persistence.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.transaction.PlatformTransactionManager;

class CpfDomainMyBatisAutoConfigurationTest {
    @TempDir
    Path tempDir;

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfDomainMyBatisAutoConfiguration.class))
            .withBean("cpfDomainDataSource", DataSource.class, () -> mock(DataSource.class))
            .withBean("cpfDomainTransactionManager", PlatformTransactionManager.class,
                    () -> mock(PlatformTransactionManager.class));

    @Test
    void myBatisProviderBindsOnlyToTheExplicitDomainDataSource() throws IOException {
        Path mapperDirectory = Files.createDirectories(tempDir.resolve("runtime/test/mybatis"));
        Files.writeString(
                mapperDirectory.resolve("TestMapper.xml"),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" "
                        + "\"https://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
                        + "<mapper namespace=\"cpf.test\"></mapper>");

        runner.withPropertyValues(
                        "cpf.domain.persistence.provider=mybatis",
                        "mybatis.mapper-locations=" + mapperDirectory.resolve("TestMapper.xml").toUri())
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .hasBean("cpfDomainSqlSessionFactory")
                        .hasBean("cpfDomainSqlSessionTemplate"));
    }

    @Test
    void myBatisProviderFailsClosedWhenDeclaredMapperPatternResolvesNothing() {
        runner.withPropertyValues(
                        "cpf.domain.persistence.provider=mybatis",
                        "mybatis.mapper-locations=file:/definitely-missing-cpf-mapper/*.xml")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseMessage(
                                    "Generated Domain MyBatis mapper resources are required: mybatis.mapper-locations");
                });
    }

    @Test
    void nonMyBatisProviderDoesNotCreateDomainMyBatisBeans() {
        runner.withPropertyValues("cpf.domain.persistence.provider=jdbc")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .doesNotHaveBean("cpfDomainSqlSessionFactory")
                        .doesNotHaveBean("cpfDomainSqlSessionTemplate"));
    }
}
