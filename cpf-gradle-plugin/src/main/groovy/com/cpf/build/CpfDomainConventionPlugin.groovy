package com.cpf.build
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.toolchain.JavaLanguageVersion
class CpfDomainConventionPlugin implements Plugin<Project> {
 void apply(Project project) {
  project.pluginManager.apply(JavaPlugin)
  project.extensions.configure(JavaPluginExtension) { toolchain.languageVersion.set(JavaLanguageVersion.of(25));withSourcesJar();withJavadocJar() }
  project.repositories.mavenCentral()
  def repo=System.getenv('CPF_ARTIFACT_REPOSITORY_URL')
  if(repo) project.repositories.maven { url=project.uri(repo);credentials{username=System.getenv('CPF_ARTIFACT_REPOSITORY_USER');password=System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')} }
  project.dependencyLocking { lockAllConfigurations() }
  project.tasks.withType(JavaCompile).configureEach { options.release.set(25);options.encoding='UTF-8' }
  project.tasks.withType(AbstractArchiveTask).configureEach { preserveFileTimestamps=false;reproducibleFileOrder=true }
 }
}
