#!/usr/bin/env python3
"""External provider 없이 컴파일 가능한 CPF 핵심 계약과 Security runtime을 실제 javac/java로 검증합니다."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
from pathlib import Path
import shutil
import subprocess
import tempfile
import textwrap

ROOT = Path(__file__).resolve().parents[2]


def java_files(*roots: str) -> list[str]:
    files: list[str] = []
    for rel in roots:
        p = ROOT / rel
        if p.is_file() and p.suffix == ".java":
            files.append(str(p))
        elif p.is_dir():
            files.extend(str(x) for x in sorted(p.rglob("*.java")))
    return files


def compile_group(name: str, files: list[str], tmp: Path) -> Path:
    out = tmp / name
    out.mkdir(parents=True)
    cp = subprocess.run(["javac", "-encoding", "UTF-8", "-d", str(out), *files], text=True, capture_output=True)
    if cp.returncode != 0:
        print(f"CPF_JAVAC_{name.upper()}=FAIL")
        if cp.stdout: print(cp.stdout)
        if cp.stderr: print(cp.stderr)
        raise SystemExit(cp.returncode)
    classes = len(list(out.rglob("*.class")))
    print(f"CPF_JAVAC_{name.upper()}=PASS sources={len(files)} classes={classes}")
    return out


def main() -> int:
    tmp = Path(tempfile.mkdtemp(prefix="cpf-java-contract-"))
    try:
        core_error = java_files("cpf-core/src/main/java/com/cpf/core/api/error")
        context = java_files(
            "cpf-core/src/main/java/com/cpf/core/api/context",
            "cpf-core/src/main/java/com/cpf/core/spi/context/CpfContextRuntimeProvider.java",
        )
        compile_group("context", context, tmp)

        error = core_error + java_files(
            "cpf-starters/data/src/main/java/com/cpf/data/error",
            "cpf-starters/integration/src/main/java/com/cpf/integration/error",
            "cpf-starters/messaging/src/main/java/com/cpf/messaging/error",
            "cpf-batch/api/src/main/java/com/cpf/batch/api/error",
        )
        compile_group("error_owner", error, tmp)

        spring_service = tmp / "org/springframework/stereotype/Service.java"
        spring_service.parent.mkdir(parents=True, exist_ok=True)
        spring_service.write_text(
            "package org.springframework.stereotype; "
            "@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME) "
            "@java.lang.annotation.Target(java.lang.annotation.ElementType.TYPE) "
            "public @interface Service { String value() default \"\"; }",
            encoding="utf-8",
        )

        foundation = core_error + java_files(
            "cpf-core/src/main/java/com/cpf/core/api/base",
            "cpf-starters/base/runtime/src/main/java/com/cpf/foundation/api",
            "cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation",
        )
        compile_group("foundation_api", foundation + [str(spring_service)], tmp)

        openapi = java_files("cpf-starters/web/src/main/java/com/cpf/web/api/openapi")
        compile_group("openapi_public_api", openapi, tmp)

        security_sources = core_error + java_files(
            "cpf-starters/security/src/main/java/com/cpf/security/api/CpfSensitiveData.java",
            "cpf-starters/security/src/main/java/com/cpf/security/api/CpfMasking.java",
            "cpf-starters/security/src/main/java/com/cpf/security/api/CpfMaskingRuntime.java",
            "cpf-starters/security/src/main/java/com/cpf/security/api/CpfMaskingPolicySnapshot.java",
        )
        harness = tmp / "CpfSecurityMaskingHarness.java"
        harness.write_text(textwrap.dedent('''
            import com.cpf.security.api.CpfMasking;
            import com.cpf.security.api.CpfSensitiveData;
            import java.util.LinkedHashMap;
            import java.util.Map;
            public final class CpfSecurityMaskingHarness {
              private static void ok(boolean value, String message) {
                if (!value) throw new IllegalStateException(message);
              }
              public static void main(String[] args) {
                String raw = "abcdef";
                String generic = CpfMasking.mask(raw);
                ok(!raw.equals(generic) && !generic.contains("bcde"), "generic raw leak");
                String emailRaw = "alice@example.com";
                String email = CpfMasking.email(emailRaw);
                ok(!emailRaw.equals(email) && email.endsWith("@example.com"), "email masking");
                String mobileRaw = "01012345678";
                String mobile = CpfMasking.mobile(mobileRaw);
                ok(!mobileRaw.equals(mobile) && mobile.endsWith("5678"), "mobile masking");
                String reasonRaw = "Bearer abc.def token=superSecret alice@example.com 010-1234-5678";
                String reason = CpfSensitiveData.sanitizeAuditReason(reasonRaw);
                ok(!reason.contains("abc.def") && !reason.contains("superSecret") && !reason.contains("alice@example.com") && !reason.contains("010-1234-5678"), "audit reason raw leak");
                Map<String,Object> data = new LinkedHashMap<>();
                data.put("secret", "TOP-SECRET");
                data.put("email", "bob@example.com");
                data.put("count", 3);
                Object sanitized = CpfMasking.structured(data);
                String text = String.valueOf(sanitized);
                ok(text.contains("[REDACTED]") && !text.contains("TOP-SECRET") && !text.contains("bob@example.com") && text.contains("count=3"), "structured masking");
              }
            }
        '''), encoding="utf-8")
        security_out = compile_group("security", security_sources + [str(harness)], tmp)
        cp = subprocess.run(["java", "-cp", str(security_out), "CpfSecurityMaskingHarness"], text=True, capture_output=True)
        if cp.returncode != 0:
            print("CPF_SECURITY_MASKING_RUNTIME=FAIL")
            if cp.stdout: print(cp.stdout)
            if cp.stderr: print(cp.stderr)
            return cp.returncode
        print("CPF_SECURITY_MASKING_RUNTIME=PASS")
        print("CPF_JAVA_CONTRACT_TESTS=PASS")
        return 0
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


if __name__ == "__main__":
    raise SystemExit(main())
