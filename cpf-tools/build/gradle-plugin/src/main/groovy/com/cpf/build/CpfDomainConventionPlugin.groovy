package com.cpf.build
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.jvm.toolchain.JavaLanguageVersion
class CpfDomainConventionPlugin implements Plugin<Project> {
 void apply(Project project) {
  project.pluginManager.apply(JavaPlugin)
  def javaVersion=project.rootProject.extensions.extraProperties.has('cpfJavaVersion')
          ? project.rootProject.extensions.extraProperties.get('cpfJavaVersion').toString().toInteger()
          : project.providers.gradleProperty('cpfJavaVersion')
              .orElse(project.providers.environmentVariable('CPF_JAVA_VERSION')).orElse('25').get().toInteger()
  project.extensions.configure(JavaPluginExtension) { JavaPluginExtension javaExtension ->
    javaExtension.toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    javaExtension.withSourcesJar()
    javaExtension.withJavadocJar()
  }
  def remoteRepo=project.providers.gradleProperty('cpfArtifactRepositoryUrl')
          .orElse(project.providers.environmentVariable('CPF_ARTIFACT_REPOSITORY_URL')).orNull
  def localRepo=project.providers.gradleProperty('cpfLocalArtifactRepository')
          .orElse(project.providers.environmentVariable('CPF_LOCAL_ARTIFACT_REPOSITORY'))
          .orElse(new File(System.getProperty('user.home'), '.cpf/repository').absolutePath)
  def offlineRepo=project.providers.gradleProperty('cpfOfflineArtifactRepository')
          .orElse(project.providers.environmentVariable('CPF_OFFLINE_ARTIFACT_REPOSITORY')).orNull
  def artifactMode=project.providers.gradleProperty('cpfArtifactMode')
          .orElse(project.providers.environmentVariable('CPF_ARTIFACT_MODE'))
          .orElse(remoteRepo == null ? 'LOCAL_DEV' : 'REMOTE').get().trim().toUpperCase(Locale.ROOT)
  if(!(artifactMode in ['LOCAL_DEV','REMOTE','OFFLINE'])) throw new GradleException("Unsupported CPF artifact mode: ${artifactMode}")
  if(artifactMode=='REMOTE' && !remoteRepo) throw new GradleException('CPF_ARTIFACT_MODE=REMOTE requires cpfArtifactRepositoryUrl or CPF_ARTIFACT_REPOSITORY_URL.')
  if(artifactMode=='OFFLINE' && !offlineRepo) throw new GradleException('CPF_ARTIFACT_MODE=OFFLINE requires cpfOfflineArtifactRepository or CPF_OFFLINE_ARTIFACT_REPOSITORY.')
  if(artifactMode=='REMOTE') project.repositories.maven {
    name='cpfRemote';url=project.uri(remoteRepo);content{includeGroupByRegex 'com[.]cpf([.].*)?'}
    def repoUser=System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
    if(repoUser) credentials{username=repoUser;password=System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')}
  } else if(artifactMode=='OFFLINE') project.repositories.maven {
    name='cpfOffline';url=project.uri(offlineRepo);content{includeGroupByRegex 'com[.]cpf([.].*)?'}
  } else project.repositories.maven {
    name='cpfLocal';url=project.uri(localRepo.get());content{includeGroupByRegex 'com[.]cpf([.].*)?'}
  }
  project.repositories.mavenCentral { content { excludeGroupByRegex 'com[.]cpf([.].*)?' } }
  project.dependencyLocking { lockAllConfigurations() }
  project.tasks.withType(JavaCompile).configureEach { options.release.set(javaVersion);options.encoding='UTF-8' }
  project.tasks.withType(AbstractArchiveTask).configureEach { preserveFileTimestamps=false;reproducibleFileOrder=true }
 }
}
