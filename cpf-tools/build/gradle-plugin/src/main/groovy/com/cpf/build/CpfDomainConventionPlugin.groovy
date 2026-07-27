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
  def remoteRepo=System.getenv('CPF_ARTIFACT_REPOSITORY_URL')
  if(remoteRepo) project.repositories.maven {
    name='cpfRemote'
    url=project.uri(remoteRepo)
    def repoUser=System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
    if(repoUser) credentials{username=repoUser;password=System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')}
  }
  def localRepo=project.providers.gradleProperty('cpfLocalArtifactRepository')
          .orElse(project.providers.environmentVariable('CPF_LOCAL_ARTIFACT_REPOSITORY'))
          .orElse(new File(System.getProperty('user.home'), '.cpf/repository').absolutePath)
  project.repositories.maven { name='cpfLocal'; url=project.uri(localRepo.get()) }
  project.repositories.mavenCentral()
  project.dependencyLocking { lockAllConfigurations() }
  project.tasks.withType(JavaCompile).configureEach { options.release.set(25);options.encoding='UTF-8' }
  project.tasks.withType(AbstractArchiveTask).configureEach { preserveFileTimestamps=false;reproducibleFileOrder=true }
 }
}
