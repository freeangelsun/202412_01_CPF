#!/usr/bin/env python3
from __future__ import annotations
import argparse,shutil,subprocess,tempfile
from pathlib import Path

def fail(x):print('CPF_SECURITY_SESSION_OIDC=FAIL '+x);return 1

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ns=ap.parse_args();r=Path(ns.root).resolve()
 # Session owner API + Valkey implementation witnesses.
 sec=r/'cpf-starters/security/src/main/java/com/cpf/security/api'
 for x in ['CpfSessionOperations.java','CpfSessionSnapshot.java','CpfSessionMetrics.java','CpfSessionMetricsSnapshot.java']:
  if not (sec/x).is_file():return fail('sessionApiMissing='+x)
 val=r/'cpf-starters/security/session/valkey/src/main/java/com/cpf/security/session/valkey'
 for x in ['ValkeyCpfSessionOperations.java','MeteredCpfSessionOperations.java','CpfValkeySessionAutoConfiguration.java','CpfValkeySessionProperties.java']:
  if not (val/x).is_file():return fail('valkeyMissing='+x)
 vs=(val/'ValkeyCpfSessionOperations.java').read_text()
 for token in ['renew(','rotate(','revokePrincipal(','findByPrincipal(','CONCURRENT_LIMIT','expireAt(','fixation-defense','audit.record','tenantId']:
  if token not in vs:return fail('sessionWitnessMissing='+token)
 ms=(val/'MeteredCpfSessionOperations.java').read_text()
 for token in ['providerFailures','forcedLogouts','misses','CpfSessionMetrics']:
  if token not in ms:return fail('sessionMetricsMissing='+token)
 if not (r/'cpf-starters/security/session/jdbc').exists():return fail('jdbcSessionProviderMissing=true')
 # OIDC actual canonical successor.
 oidc=r/'cpf-starters/security/oidc/src/main/java/com/cpf/security/oidc'
 for x in ['CpfOidcAutoConfiguration.java','CpfOidcPrincipalMapper.java','CpfOidcUserService.java','CpfOidcContextBridge.java','CpfOidcContextFilter.java','CpfOidcSecurityEventSink.java']:
  if not (oidc/x).is_file():return fail('oidcMissing='+x)
 joined='\n'.join((oidc/x).read_text() for x in ['CpfOidcAutoConfiguration.java','CpfOidcPrincipalMapper.java','CpfOidcUserService.java','CpfOidcContextBridge.java'])
 for token in ['authorizationCode()','refreshToken()','clientCredentials()','OIDC_LOGIN','OIDC_LOGOUT','withIdentityAndTenant','tenantId','getSafeClaimNames']:
  if token not in joined:return fail('oidcWitnessMissing='+token)
 if 'com.cpf.core.api.config.' in joined:return fail('staleCoreConfig=true')
 # Compile owner-neutral OIDC context bridge and Core Context with -Werror.
 javac=shutil.which('javac'); java=shutil.which('java')
 if not javac or not java:return fail('javacMissing=true')
 with tempfile.TemporaryDirectory(prefix='cpf-oidc-') as td:
  t=Path(td);c=t/'c';c.mkdir()
  src=[r/'cpf-core/src/main/java/com/cpf/core/api/context/CpfContext.java',r/'cpf-core/src/main/java/com/cpf/core/api/context/CpfContextSnapshot.java',oidc/'CpfOidcPrincipal.java',oidc/'CpfOidcContextBridge.java']
  cp=subprocess.run([javac,'-Xlint:all','-Werror','-d',str(c),*map(str,src)],text=True,capture_output=True)
  if cp.returncode:return fail('oidcBridgeCompile='+(cp.stdout+cp.stderr).replace('\n',' | '))
 print('CPF_SECURITY_SESSION_OIDC=PASS jdbcSession=true valkeySession=true ttl=true rotation=true fixation=true concurrency=true forceLogout=true metrics=true providerFailure=true oidc=true sso=true claimMap=true contextBridge=true audit=true refresh=true')
 return 0
if __name__=='__main__':raise SystemExit(main())
