#!/usr/bin/env python3
"""Security Owner Context의 credential 차단과 실제 request-boundary consumer wiring을 검증합니다."""

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
from pathlib import Path
import re
import shutil, subprocess, tempfile, textwrap, sys
ROOT=Path(__file__).resolve().parents[2]

def main():
    fail=[]
    auth=ROOT/'cpf-starters/security/resource-server/src/main/java/com/cpf/security/resource/CpfAuthenticatedContextFilter.java'
    session=ROOT/'cpf-starters/security/session/jdbc/src/main/java/com/cpf/security/session/jdbc/CpfBffSessionBridgeFilter.java'
    for p in (auth,session):
        if not p.is_file(): fail.append('MISSING:'+p.relative_to(ROOT).as_posix())
    if auth.is_file():
        s=auth.read_text(encoding='utf-8',errors='ignore')
        for token in ('new CpfSecurityRuntimeContext(','request.setAttribute(CpfSecurityRuntimeContext.REQUEST_ATTRIBUTE','request.removeAttribute(CpfSecurityRuntimeContext.REQUEST_ATTRIBUTE','finally'):
            if token not in s: fail.append('SECURITY_REQUEST_SCOPE_MISSING:'+token)
        if 'request.getHeader("Authorization")' in s and 'CpfSecurityRuntimeContext(' in s and 'Authorization' in s[s.find('CpfSecurityRuntimeContext('):s.find('CpfSecurityRuntimeContext(')+600]:
            fail.append('SECURITY_CONTEXT_AUTHORIZATION_SOURCE')
    if session.is_file():
        s=session.read_text(encoding='utf-8',errors='ignore')
        for token in ('new CpfSessionContext(','request.setAttribute(CpfSessionContext.REQUEST_ATTRIBUTE','request.removeAttribute(CpfSessionContext.REQUEST_ATTRIBUTE','finally'):
            if token not in s: fail.append('SESSION_REQUEST_SCOPE_MISSING:'+token)
        # Variable names are implementation detail. Require SHA-256 of the active HttpSession id semantically.
        if not re.search(r'CpfHashes\.sha256\(\s*[A-Za-z_$][A-Za-z0-9_$]*\.getId\(\)\s*\)', s):
            fail.append('SESSION_REQUEST_SCOPE_MISSING:hashed-session-id')
        if re.search(r'new\s+CpfSessionContext\(\s*[A-Za-z_$][A-Za-z0-9_$]*\.getId\(\)', s):
            fail.append('RAW_SESSION_ID_IN_CONTEXT')
        for old in ('CpfContextComponentRegistry','CpfSecurityContextComponents'):
            if old in s: fail.append('SESSION_LEGACY_CONTEXT_MECHANISM:'+old)
    if fail:
        print('CPF_SECURITY_CONTEXT_RUNTIME=FAIL')
        print('failures='+str(len(fail)))
        for x in fail: print(x)
        return 1
    tmp=Path(tempfile.mkdtemp(prefix='cpf-security-context-'))
    try:
        harness=tmp/'CpfSecurityContextHarness.java'
        harness.write_text(textwrap.dedent('''
            import com.cpf.security.context.*;
            import java.time.*;
            public final class CpfSecurityContextHarness {
              static void ok(boolean v,String m){if(!v)throw new IllegalStateException(m);}
              public static void main(String[] args){
                Instant now=Instant.parse("2026-08-09T00:00:00Z");
                var sec=new CpfSecurityRuntimeContext("jwt-123","JWT","aal2",now,"authz-1","v1",null,"risk-1","RESOURCE_SERVER");
                ok("JWT".equals(sec.authenticationMethod()),"security metadata");
                ok(CpfSecurityRuntimeContext.REQUEST_ATTRIBUTE.contains("CpfSecurityRuntimeContext"),"security attribute");
                try { new CpfSecurityRuntimeContext("Bearer raw-token","JWT",null,now,null,null,null,null,null); throw new IllegalStateException("secret accepted"); }
                catch(IllegalArgumentException expected) { }
                var session=new CpfSessionContext("sha256-reference",2L,"user-1",now,now,now.plusSeconds(60),now.plusSeconds(600),"jwt-123","device-1",CpfSessionContext.State.ACTIVE);
                ok("sha256-reference".equals(session.sessionReference()),"session reference");
                ok(session.state()==CpfSessionContext.State.ACTIVE,"session state");
                ok(CpfSessionContext.REQUEST_ATTRIBUTE.contains("CpfSessionContext"),"session attribute");
              }
            }
        '''),encoding='utf-8')
        out=tmp/'classes';out.mkdir()
        src=[
            ROOT/'cpf-starters/security/src/main/java/com/cpf/security/context/CpfSecurityRuntimeContext.java',
            ROOT/'cpf-starters/security/src/main/java/com/cpf/security/context/CpfSessionContext.java',
            harness,
        ]
        cp=subprocess.run(['javac','-encoding','UTF-8','-d',str(out),*[str(x) for x in src]],text=True,capture_output=True)
        if cp.returncode:
            print('CPF_SECURITY_CONTEXT_COMPILE=FAIL');print(cp.stdout);print(cp.stderr,file=sys.stderr);return cp.returncode
        print(f'CPF_SECURITY_CONTEXT_COMPILE=PASS sources={len(src)} classes={len(list(out.rglob("*.class")))}')
        cp=subprocess.run(['java','-cp',str(out),'CpfSecurityContextHarness'],text=True,capture_output=True)
        if cp.returncode:
            print('CPF_SECURITY_CONTEXT_RUNTIME=FAIL');print(cp.stdout);print(cp.stderr,file=sys.stderr);return cp.returncode
        print('CPF_SECURITY_CONTEXT_RUNTIME=PASS cases=resource-server,session,credential-reject,session-hash,request-scope-wiring')
        return 0
    finally:
        shutil.rmtree(tmp,ignore_errors=True)
if __name__=='__main__': raise SystemExit(main())
