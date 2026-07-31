package com.cpf.gradle;
import static org.junit.jupiter.api.Assertions.*;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
class CpfPlatformConventionPluginTest {
 @Test void appliesJava25Toolchain() { Project p=ProjectBuilder.builder().build(); p.getPluginManager().apply("com.cpf.platform-conventions"); assertEquals(25, p.getExtensions().getByType(JavaPluginExtension.class).getToolchain().getLanguageVersion().get().asInt()); }
}
