package com.cpf.education.verification.catalog;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** EDU가 참조 업무 도메인 정체성과 기존 API 호환 별칭을 함께 유지하는지 검증합니다. */
class EducationIdentityContractTest {

    @Test
    void 모든EDUController가canonical경로와호환별칭을제공한다() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        StandardEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles("test");
        scanner.setEnvironment(environment);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<ControllerContract> controllers = scanner.findCandidateComponents("com.cpf.education").stream()
                .map(EducationIdentityContractTest::contract)
                .toList();

        assertThat(controllers).hasSizeGreaterThanOrEqualTo(20);
        for (ControllerContract controller : controllers) {
            assertThat(controller.paths())
                    .as("%s 클래스의 canonical 경로", controller.className())
                    .anyMatch(path -> path.startsWith("/api/education"));
            assertThat(controller.paths())
                    .as("%s 클래스의 legacy 호환 경로", controller.className())
                    .anyMatch(path -> path.startsWith("/education/edu"));
            assertThat(controller.tagName())
                    .as("%s 클래스의 OpenAPI tag", controller.className())
                    .isNotBlank()
                    .doesNotContain("EDU-EDU");
        }
    }

    @Test
    void EDUJavaPackage는도메인레벨EDU계층을사용하지않는다() {
        assertThat(EducationIdentityContractTest.class.getPackageName()).doesNotContain(".edu.");
    }

    private static ControllerContract contract(org.springframework.beans.factory.config.BeanDefinition candidate) {
        if (!(candidate instanceof AnnotatedBeanDefinition annotated)) {
            throw new IllegalStateException("EDU Controller metadata를 읽을 수 없습니다: " + candidate.getBeanClassName());
        }
        AnnotationMetadata metadata = annotated.getMetadata();
        var requestMapping = metadata.getAnnotations().get(RequestMapping.class);
        var tag = metadata.getAnnotations().get(Tag.class);
        if (!requestMapping.isPresent() || !tag.isPresent()) {
            throw new IllegalStateException("EDU Controller identity annotation이 없습니다: " + metadata.getClassName());
        }
        return new ControllerContract(
                metadata.getClassName(),
                Arrays.asList(requestMapping.getStringArray("value")),
                tag.getString("name"));
    }

    private record ControllerContract(String className, List<String> paths, String tagName) {}
}
