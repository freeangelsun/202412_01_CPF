#!/usr/bin/env python3
"""Compile current canonical generated-domain Java templates for every persistence profile."""
from __future__ import annotations

from pathlib import Path
import re
import subprocess
import sys
import tempfile

root = Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
generator_path = root / "cpf-tools/generator/create-domain.ps1"
if not generator_path.is_file():
    raise SystemExit(f"canonical generator missing: {generator_path}")
src = generator_path.read_text(encoding="utf-8")

VALUES = {
    "${FeatureClassPrefix}": "Sample",
    "${ModuleClassName}": "Sample",
    "$FeaturePackage": "com.example.sample.sampleitem",
    "$BasePackage": "com.example.sample",
    "$ModuleClassName": "Sample",
    "$module": "sample",
    "$ModuleUpper": "SMP",
}


def block(name: str) -> str:
    match = re.search(rf"^\${name}\s*=\s*@\"\n(.*?)\n\"@", src, re.S | re.M)
    if not match:
        raise RuntimeError(f"canonical generator template missing: {name}")
    text = match.group(1)
    for before, after in VALUES.items():
        text = text.replace(before, after)
    text = re.sub(
        r"\$\(\[string\]::Concat\('import ', com\.example\.sample, '\.common\.base\.', Sample, 'BaseService;'\)\)",
        "import com.example.sample.common.base.SampleBaseService;",
        text,
    )
    text = re.sub(
        r"\$\(\[string\]::Concat\('import ', com\.example\.sample, '\.common\.contract\.', Sample, '(Request|Response);'\)\)",
        lambda value: "import com.example.sample.common.contract.Sample" + value.group(1) + ";",
        text,
    )
    return text


COMMON_BLOCKS = [
    "commandPortSource",
    "inMemoryAdapter",
    "service",
    "sampleCommand",
    "sampleItem",
    "searchResult",
    "deleteCommand",
    "deleteResult",
    "idempotencyEntry",
]

COMMON_EXTRA = {
    "com/example/sample/sampleitem/dto/SampleSearchRequest.java": """package com.example.sample.sampleitem.dto; public record SampleSearchRequest(String keyword,Integer page,Integer size,String sortBy,String sortDirection){ public SampleSearchRequest normalized(){return this;} public int offset(){int p=page==null?0:Math.max(0,page); int s=size==null?20:Math.max(1,size); return p*s;} }""",
    "com/example/sample/sampleitem/port/SampleQueryPort.java": """package com.example.sample.sampleitem.port; import com.example.sample.sampleitem.dto.*; import com.cpf.core.api.page.CpfSlice; import java.util.Optional; public interface SampleQueryPort { SampleSearchResult search(SampleSearchRequest r); Optional<SampleSampleItem> findBySampleKey(String k); CpfSlice<SampleSampleItem> cursor(Long a,int s); }""",
    "com/example/sample/common/base/SampleBaseService.java": "package com.example.sample.common.base; public abstract class SampleBaseService {}",
    "com/example/sample/common/contract/SampleRequest.java": "package com.example.sample.common.contract; public interface SampleRequest {}",
    "com/example/sample/common/contract/SampleResponse.java": "package com.example.sample.common.contract; public interface SampleResponse {}",
    "com/cpf/core/api/page/CpfSlice.java": "package com.cpf.core.api.page; import java.util.List; public record CpfSlice<T>(List<T> items,int page,int size,boolean hasNext) {}",
    "com/cpf/core/api/error/CpfValidationException.java": "package com.cpf.core.api.error; public class CpfValidationException extends RuntimeException { public CpfValidationException(String m){super(m);} }",
    "com/cpf/core/api/logging/CpfTransactionContext.java": """package com.cpf.core.api.logging; public final class CpfTransactionContext { public static String idempotencyKey(){return "I";} public static String operatorId(){return "O";} public static String userId(){return "U";} public static String transactionId(){return "T";} public static long nextSequence(){return 1;} }""",
    "com/cpf/core/api/security/CpfMasking.java": "package com.cpf.core.api.security; public final class CpfMasking { public static String mask(String s){return s;} }",
    "org/springframework/stereotype/Service.java": "package org.springframework.stereotype; public @interface Service {}",
    "org/springframework/stereotype/Component.java": "package org.springframework.stereotype; public @interface Component {}",
    "org/springframework/stereotype/Repository.java": "package org.springframework.stereotype; public @interface Repository {}",
    "org/springframework/context/annotation/Profile.java": "package org.springframework.context.annotation; public @interface Profile { String value(); }",
    "org/springframework/transaction/annotation/Transactional.java": "package org.springframework.transaction.annotation; public @interface Transactional { boolean readOnly() default false; }",
    "org/springframework/dao/OptimisticLockingFailureException.java": "package org.springframework.dao; public class OptimisticLockingFailureException extends RuntimeException { public OptimisticLockingFailureException(String m){super(m);} }",
    "jakarta/validation/constraints/NotBlank.java": "package jakarta.validation.constraints; public @interface NotBlank {}",
    "jakarta/validation/constraints/Pattern.java": "package jakarta.validation.constraints; public @interface Pattern { String regexp(); }",
    "jakarta/validation/constraints/PositiveOrZero.java": "package jakarta.validation.constraints; public @interface PositiveOrZero {}",
    "jakarta/validation/constraints/Size.java": "package jakarta.validation.constraints; public @interface Size { int max(); }",
}

PROFILE_EXTRA = {
    "mybatis": {
        "com/cpf/core/api/database/CpfDataOperations.java": """package com.cpf.core.api.database; import java.util.List; public interface CpfDataOperations { <T> T selectOne(String statement,Object parameter); <E> List<E> selectList(String statement,Object parameter); int insert(String statement,Object parameter); int update(String statement,Object parameter); void inRollbackOnlyTransaction(java.util.function.Consumer<Object> work); }""",
    },
    "jdbc": {
        "com/cpf/core/api/database/CpfJdbcOperations.java": """package com.cpf.core.api.database; import java.util.List; public interface CpfJdbcOperations { <T> T queryOne(String sql,Object parameter,Class<T> type); <T> List<T> queryList(String sql,Object parameter,Class<T> type); int update(String sql,Object parameter); void inRollbackOnlyTransaction(java.util.function.Consumer<Object> work); }""",
        "com/cpf/core/api/database/CpfVendorSqlCatalog.java": """package com.cpf.core.api.database; public interface CpfVendorSqlCatalog { String required(String key); }""",
        "com/cpf/core/api/database/CpfVendorSqlCatalogProvider.java": """package com.cpf.core.api.database; public interface CpfVendorSqlCatalogProvider { CpfVendorSqlCatalog forModule(String module); }""",
    },
}


def write_source(source_root: Path, text: str) -> None:
    package = re.search(r"package\s+([\w.]+);", text)
    declaration = re.search(r"public\s+(?:class|interface|record)\s+(\w+)", text)
    if not (package and declaration):
        raise RuntimeError(text[:240])
    path = source_root / Path(*package.group(1).split(".")) / (declaration.group(1) + ".java")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def compile_profile(profile: str, repository_block: str) -> tuple[int, int, str]:
    with tempfile.TemporaryDirectory(prefix=f"cpf-generator-{profile}-compile-") as directory:
        output = Path(directory)
        source_root = output / "src"
        source_root.mkdir(parents=True)
        for name in [*COMMON_BLOCKS, repository_block]:
            write_source(source_root, block(name))
        for relative, text in {**COMMON_EXTRA, **PROFILE_EXTRA[profile]}.items():
            path = source_root / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding="utf-8")
        files = [str(path) for path in source_root.rglob("*.java")]
        process = subprocess.run(
            ["javac", "--release", "21", "-d", str(output / "classes"), *files],
            capture_output=True,
            text=True,
        )
        detail = "\n".join(part.strip() for part in (process.stdout, process.stderr) if part.strip())
        return process.returncode, len(files), detail


failures: list[str] = []
total_sources = 0
for profile, repository_block in (("mybatis", "myBatisRepository"), ("jdbc", "jdbcRepository")):
    code, count, detail = compile_profile(profile, repository_block)
    total_sources += count
    if code:
        failures.append(f"profile={profile} sources={count}\n{detail}")
    else:
        print(f"GENERATOR_JAVA_TEMPLATE_PROFILE=PASS profile={profile} sources={count} release=21")

if failures:
    for failure in failures:
        print(failure)
    print(f"GENERATOR_JAVA_TEMPLATE_COMPILE=FAIL profiles=2 sources={total_sources} release=21")
    raise SystemExit(1)
print(f"GENERATOR_JAVA_TEMPLATE_COMPILE=PASS profiles=2 sources={total_sources} release=21")
