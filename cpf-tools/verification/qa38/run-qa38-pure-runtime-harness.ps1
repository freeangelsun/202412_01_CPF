param([string]$ProjectRoot=(Resolve-Path "$PSScriptRoot/../../..").Path)
$ErrorActionPreference='Stop'
$ProjectRoot=(Resolve-Path $ProjectRoot).Path
$work=Join-Path ([System.IO.Path]::GetTempPath()) ("cpf-qa38-pure-"+[guid]::NewGuid().ToString('N'))
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
'cpf-starters/integration-fixedlength-core/src/main/java/com/cpf/integration/fixedlength/CpfBinaryFieldCodec.java',
'cpf-starters/integration-fixedlength-core/src/main/java/com/cpf/integration/fixedlength/CpfFixedLengthLayout.java',
'cpf-starters/integration-fixedlength-core/src/main/java/com/cpf/integration/fixedlength/CpfFixedLengthCodec.java',
'cpf-starters/integration-fixedlength-core/src/main/java/com/cpf/integration/fixedlength/CpfFixedLengthField.java',
'cpf-starters/integration-iso8583/src/main/java/com/cpf/starter/iso8583/CpfIso8583Message.java',
'cpf-starters/integration-iso8583/src/main/java/com/cpf/starter/iso8583/CpfIso8583HmacMacProvider.java',
'cpf-starters/integration-iso8583/src/main/java/com/cpf/starter/iso8583/CpfIso8583FieldSpec.java',
'cpf-starters/integration-iso8583/src/main/java/com/cpf/starter/iso8583/CpfIso8583Codec.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpUnknownResultStore.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpTlsContextProvider.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/KeyStoreCpfTcpTlsContextProvider.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpCorrelationRegistry.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpProperties.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpClient.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpReconnectPolicy.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpUnknownResult.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpFrameCodec.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpServer.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpDeterministicSimulator.java',
'cpf-starters/integration-tcp/src/main/java/com/cpf/starter/tcp/CpfTcpOperations.java',
'cpf-starters/notification/src/main/java/com/cpf/starter/notification/CpfNotificationProvider.java',
'cpf-starters/notification/src/main/java/com/cpf/starter/notification/CpfNotificationRequest.java',
'cpf-starters/notification/src/main/java/com/cpf/starter/notification/CpfNotificationPreferencePolicy.java',
'cpf-starters/notification/src/main/java/com/cpf/starter/notification/CpfNotificationResult.java',
'cpf-starters/notification-sms-spi/src/main/java/com/cpf/notification/sms/CpfSmsProvider.java',
'cpf-starters/notification-sms-spi/src/main/java/com/cpf/notification/sms/CpfSmsDispatcher.java',
'cpf-starters/file-archive/src/main/java/com/cpf/starter/archive/CpfArchiveProperties.java',
'cpf-starters/file-archive/src/main/java/com/cpf/starter/archive/CpfArchiveService.java',
'cpf-starters/security-service-identity/src/main/java/com/cpf/starter/security/identity/CpfServiceIdentityProperties.java',
'cpf-starters/security-service-identity/src/main/java/com/cpf/starter/security/identity/CpfServiceIdentityTokenService.java',
'cpf-starters/base/src/main/java/com/cpf/starter/base/CpfCapabilityBinding.java',
'cpf-starters/base/src/main/java/com/cpf/starter/base/CpfCapabilityBindingRegistry.java',
'cpf-tools/verification/qa38/harness/Qa38PureRuntimeHarness.java'
)
$sources=@((Join-Path $stub 'ConfigurationProperties.java'))+$relative.ForEach({Join-Path $ProjectRoot $_})
& javac -encoding UTF-8 -d $classes @sources
if($LASTEXITCODE-ne 0){throw 'QA38 pure runtime javac failed'}
& java -cp $classes com.cpf.tools.verification.qa38.Qa38PureRuntimeHarness
if($LASTEXITCODE-ne 0){throw 'QA38 pure runtime harness failed'}
}finally{if(Test-Path $work){Remove-Item -LiteralPath $work -Recurse -Force}}
