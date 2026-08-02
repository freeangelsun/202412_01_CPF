param([string]$ProjectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path)
$ErrorActionPreference='Stop'
$ProjectRoot=(Resolve-Path $ProjectRoot).Path
$work=Join-Path ([System.IO.Path]::GetTempPath()) ("cpf-qa38-msg-"+[guid]::NewGuid().ToString('N'))
$classes=Join-Path $work 'classes';$stub=Join-Path $work 'src/org/springframework/boot/context/properties'
New-Item -ItemType Directory -Path $classes,$stub -Force|Out-Null
try{
@'
package org.springframework.boot.context.properties;
import java.lang.annotation.*;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationProperties { String value() default ""; String prefix() default ""; }
'@ | Set-Content -LiteralPath (Join-Path $stub 'ConfigurationProperties.java') -Encoding UTF8
$relative=@(
'cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerPublishRequest.java',
'cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerPublishResult.java',
'cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerClient.java',
'cpf-starters/messaging-reliability-jdbc/src/main/java/com/cpf/starter/messaging/reliability/CpfBrokerClientRouter.java',
'cpf-starters/messaging-reliability-jdbc/src/main/java/com/cpf/starter/messaging/reliability/CpfNamedBrokerClient.java',
'cpf-starters/messaging-reliability-jdbc/src/main/java/com/cpf/starter/messaging/reliability/CpfMessageCompatibilityGuard.java',
'cpf-starters/messaging-reliability-jdbc/src/main/java/com/cpf/starter/messaging/reliability/CpfMessagingReliabilityProperties.java',
'cpf-starters/security-service-identity/src/main/java/com/cpf/starter/security/identity/CpfServiceIdentityProperties.java',
'cpf-starters/security-service-identity/src/main/java/com/cpf/starter/security/identity/CpfServiceIdentityTokenService.java',
'cpf-tools/verification/qa38/harness/QA38MessagingIdentityHarness.java'
)
$sources=@((Join-Path $stub 'ConfigurationProperties.java'))+$relative.ForEach({Join-Path $ProjectRoot $_})
& javac -encoding UTF-8 -d $classes @sources
if($LASTEXITCODE-ne 0){throw 'QA38 messaging/identity javac failed'}
& java -cp $classes com.cpf.tools.verification.qa38.QA38MessagingIdentityHarness
if($LASTEXITCODE-ne 0){throw 'QA38 messaging/identity harness failed'}
}finally{if(Test-Path $work){Remove-Item -LiteralPath $work -Recurse -Force}}
