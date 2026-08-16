package com.cpf.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/**
 * CPF 전체 Java 모듈이 공통으로 사용하는 최소 Gradle Convention을 적용한다.
 *
 * <p>이 플러그인은 Java Toolchain, 인코딩, 파라미터 메타데이터와 의존성 충돌 정책만 소유한다.
 * Generated Customer Domain의 Starter 조합과 정책 검증은 Generator/Verification 계층의 책임이며,
 * 이 플러그인이 프로젝트 내부 manifest를 읽거나 특정 CPF 모듈 의존성을 암묵적으로 주입하지 않는다.</p>
 */
public final class CpfPlatformConventionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaPlugin.class);
        project.getExtensions().configure(JavaPluginExtension.class,
                java -> java.getToolchain().getLanguageVersion().set(JavaLanguageVersion.of(25)));
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            // CPF 공식 Source/Generated Source는 운영체제와 무관하게 UTF-8로 컴파일한다.
            task.getOptions().setEncoding("UTF-8");
            task.getOptions().getRelease().set(25);
            // Runtime reflection/argument resolver가 안정적으로 parameter name을 사용할 수 있게 보존한다.
            task.getOptions().getCompilerArgs().add("-parameters");
        });
        // 하나의 Build에서 서로 다른 전이 버전을 조용히 선택하지 않고 구성 단계에서 실패시킨다.
        project.getConfigurations().configureEach(configuration ->
                configuration.getResolutionStrategy().failOnVersionConflict());
    }
}
