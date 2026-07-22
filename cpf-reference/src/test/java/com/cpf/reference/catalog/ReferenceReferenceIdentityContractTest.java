package cpf.xyz.catalog;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** XYZ가 참조 업무 도메인 정체성과 기존 API 호환 별칭을 함께 유지하는지 검증합니다. */
class XyzReferenceIdentityContractTest {

    @Test
    void 모든XYZController가canonical경로와호환별칭을제공한다() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        List<Class<?>> controllers = scanner.findCandidateComponents("cpf.xyz").stream()
                .<Class<?>>map(candidate -> loadClass(candidate.getBeanClassName()))
                .toList();

        assertThat(controllers).hasSizeGreaterThanOrEqualTo(20);
        for (Class<?> controller : controllers) {
            RequestMapping mapping = controller.getAnnotation(RequestMapping.class);
            assertThat(mapping)
                    .as("%s 클래스의 주제영역 경로", controller.getName())
                    .isNotNull();
            List<String> paths = Arrays.asList(mapping.value());
            assertThat(paths)
                    .as("%s 클래스의 canonical 경로", controller.getName())
                    .anyMatch(path -> path.startsWith("/api/xyz/reference"));
            assertThat(paths)
                    .as("%s 클래스의 legacy 호환 경로", controller.getName())
                    .anyMatch(path -> path.startsWith("/xyz/edu"));

            Tag tag = controller.getAnnotation(Tag.class);
            assertThat(tag).as("%s 클래스의 OpenAPI tag", controller.getName()).isNotNull();
            assertThat(tag.name()).doesNotContain("XYZ-EDU");
        }
    }

    @Test
    void XYZJavaPackage는도메인레벨EDU계층을사용하지않는다() {
        assertThat(XyzReferenceIdentityContractTest.class.getPackageName()).doesNotContain(".edu.");
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("XYZ Controller를 불러오지 못했습니다: " + className, exception);
        }
    }
}
