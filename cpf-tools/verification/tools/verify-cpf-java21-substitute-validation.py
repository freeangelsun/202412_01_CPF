#!/usr/bin/env python3
"""Run reproducible Java 21 substitute validation without Gradle or network access.

The validator compiles the real CPF controller and unit-test source against minimal
API stubs in an isolated temporary directory, executes the real unit-test methods,
and runs an additional runtime harness. No build output is written into the repository.
"""
from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
from typing import Iterable

JAVA_SOURCE = Path("cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java")
TEST_SOURCE = Path("cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerActorTest.java")
DTO_SOURCES = (
    Path("cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeCommandRequest.java"),
    Path("cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeDeploymentPlanRequest.java"),
)
EXPECTED_MAJOR = 65  # Java 21 class-file major version

STUBS: dict[str, str] = {
    "com/cpf/admin/approval/service/AdmApprovalService.java": """
package com.cpf.admin.approval.service;
import java.util.Map;
public class AdmApprovalService {
  public Map<String,Object> execute(long id, String reason, String operatorId) { return Map.of(); }
}
""",
    "io/swagger/v3/oas/annotations/media/Schema.java": """
package io.swagger.v3.oas.annotations.media;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE,ElementType.FIELD,ElementType.RECORD_COMPONENT,ElementType.PARAMETER})
public @interface Schema { String name() default ""; String description() default ""; RequiredMode requiredMode() default RequiredMode.AUTO; enum RequiredMode { AUTO, REQUIRED, NOT_REQUIRED } }
""",
    "com/cpf/admin/common/base/AdmBaseController.java": """
package com.cpf.admin.common.base;
public class AdmBaseController {}
""",
    "com/cpf/admin/opr/batch/runtime/BatchControlClientException.java": """
package com.cpf.admin.opr.batch.runtime;
public class BatchControlClientException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  public enum Category { VALIDATION, PERMISSION, NOT_FOUND, CONFLICT, UNKNOWN_RESULT, UNAVAILABLE, OWNER_ERROR }
  public Category category() { return Category.OWNER_ERROR; }
  public String errorCode() { return "E"; }
  public String traceId() { return null; }
}
""",
    "com/cpf/admin/opr/batch/runtime/BatchRuntimeControlClient.java": """
package com.cpf.admin.opr.batch.runtime;
import java.util.Map;
public class BatchRuntimeControlClient {
  public Object instances(long value) { return null; }
  public Map<String,Object> view(String value) { return Map.of(); }
  public Object jobDefinitions(String jobId, String state, int limit) { return null; }
  public Map<String,Object> jobDefinitionDetail(String jobId, long version) { return Map.of(); }
  public Map<String,Object> validateJobDefinition(Map<String,Object> request) { return Map.of(); }
  public Map<String,Object> saveJobDefinition(Map<String,Object> request) { return Map.of(); }
  public Map<String,Object> transitionJobDefinition(String jobId, long version, Map<String,Object> request) { return Map.of(); }
  public Map<String,Object> command(Map<String,Object> request) { return Map.of(); }
  public Map<String,Object> commandState(String key) { return Map.of(); }
  public Map<String,Object> createPlan(Map<String,Object> request) { return Map.of(); }
}
""",
    "io/swagger/v3/oas/annotations/Operation.java": """
package io.swagger.v3.oas.annotations;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD})
public @interface Operation { String operationId() default ""; String summary() default ""; String description() default ""; }
""",
    "io/swagger/v3/oas/annotations/tags/Tag.java": """
package io.swagger.v3.oas.annotations.tags;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE})
public @interface Tag { String name() default ""; String description() default ""; }
""",
    "org/springframework/http/ResponseEntity.java": """
package org.springframework.http;
public class ResponseEntity<T> {
  public static <T> ResponseEntity<T> ok(T body) { return new ResponseEntity<>(); }
  public static BodyBuilder status(int status) { return new BodyBuilder(); }
  public static BodyBuilder accepted() { return new BodyBuilder(); }
  public static BodyBuilder badRequest() { return new BodyBuilder(); }
  public static final class BodyBuilder {
    public <T> ResponseEntity<T> body(T body) { return new ResponseEntity<>(); }
  }
}
""",
    "org/springframework/web/bind/annotation/RestController.java": """
package org.springframework.web.bind.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface RestController {}
""",
    "org/springframework/web/bind/annotation/RequestMapping.java": """
package org.springframework.web.bind.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE, ElementType.METHOD}) public @interface RequestMapping { String[] value() default {}; }
""",
    "org/springframework/web/bind/annotation/GetMapping.java": """
package org.springframework.web.bind.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface GetMapping { String[] value() default {}; }
""",
    "org/springframework/web/bind/annotation/PostMapping.java": """
package org.springframework.web.bind.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface PostMapping { String[] value() default {}; }
""",
    "org/springframework/web/bind/annotation/RequestParam.java": """
package org.springframework.web.bind.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) public @interface RequestParam { String value() default ""; boolean required() default true; String defaultValue() default ""; }
""",
    "org/springframework/web/bind/annotation/PathVariable.java": """
package org.springframework.web.bind.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) public @interface PathVariable { String value() default ""; }
""",
    "org/springframework/web/bind/annotation/RequestAttribute.java": """
package org.springframework.web.bind.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) public @interface RequestAttribute { String value() default ""; }
""",
    "org/springframework/web/bind/annotation/RequestBody.java": """
package org.springframework.web.bind.annotation;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER) public @interface RequestBody {}
""",
    "org/junit/jupiter/api/Test.java": """
package org.junit.jupiter.api;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) public @interface Test {}
""",
    "org/junit/jupiter/api/Assertions.java": """
package org.junit.jupiter.api;
import java.util.Objects;
import java.util.function.Supplier;
public final class Assertions {
  private Assertions() {}
  public static void assertEquals(Object expected, Object actual) {
    if (!Objects.equals(expected, actual)) throw new AssertionError("expected=" + expected + ", actual=" + actual);
  }
  public static void assertFalse(boolean value, Supplier<String> message) {
    if (value) throw new AssertionError(message.get());
  }
  public static <T> T assertInstanceOf(Class<T> type, Object value) {
    if (!type.isInstance(value)) throw new AssertionError("expected instance of " + type.getName() + ", actual=" + value);
    return type.cast(value);
  }
  public static <T extends Throwable> T assertThrows(Class<T> type, Executable executable) {
    try { executable.execute(); }
    catch (Throwable failure) {
      if (!type.isInstance(failure)) throw new AssertionError("expected " + type.getName() + ", actual=" + failure, failure);
      return type.cast(failure);
    }
    throw new AssertionError("expected exception " + type.getName());
  }
  public static void assertTrue(boolean value) { if (!value) throw new AssertionError("expected true"); }
  @FunctionalInterface public interface Executable { void execute() throws Throwable; }
}
""",
    "com/cpf/admin/opr/batch/runtime/BatchActorUnitRunner.java": """
package com.cpf.admin.opr.batch.runtime;
public final class BatchActorUnitRunner {
  public static void main(String[] args) throws Exception {
    BatchRuntimeControlControllerActorTest test = new BatchRuntimeControlControllerActorTest();
    test.stripsClientActorAliasesRecursivelyAndInjectsAuthenticatedOperator();
    test.rejectsMissingAuthenticatedOperator();
    System.out.println("JAVA21_UNIT_TEST_PASS count=2");
  }
}
""",
    "com/cpf/admin/opr/batch/runtime/BatchActorRuntimeHarness.java": """
package com.cpf.admin.opr.batch.runtime;
import java.lang.reflect.*;
import java.util.*;
public final class BatchActorRuntimeHarness {
  private static final Set<String> ACTORS = Set.of("requestedBy", "requestUser", "actorId", "operatorId", "operatorIdOverride");
  public static void main(String[] args) throws Exception {
    Method method = BatchRuntimeControlController.class.getDeclaredMethod("withServerActor", Map.class, String.class);
    method.setAccessible(true);
    Map<String,Object> request = new LinkedHashMap<>();
    request.put("requestedBy", "browser");
    request.put("approvedBy", "approver");
    request.put("nested", Map.of("operatorIdOverride", "override", "items", List.of(Map.of("requestUser", "deep", "value", 7))));
    @SuppressWarnings("unchecked") Map<String,Object> command = (Map<String,Object>) method.invoke(null, request, "server-admin");
    if (!"server-admin".equals(command.get("requestedBy"))) throw new AssertionError(command);
    if (!"approver".equals(command.get("approvedBy"))) throw new AssertionError(command);
    assertClean(command, true);
    try { command.put("actorId", "tampered"); throw new AssertionError("top map is mutable"); }
    catch (UnsupportedOperationException expected) { }
    try { method.invoke(null, Map.of("reason", "x"), " "); throw new AssertionError("blank actor accepted"); }
    catch (InvocationTargetException expected) {
      if (!(expected.getCause() instanceof IllegalArgumentException)) throw expected;
    }
    System.out.println("JAVA21_RUNTIME_HARNESS_PASS");
  }
  private static void assertClean(Object value, boolean top) {
    if (value instanceof Map<?,?> map) {
      for (Map.Entry<?,?> entry : map.entrySet()) {
        String key = String.valueOf(entry.getKey());
        if (ACTORS.contains(key) && !(top && key.equals("requestedBy"))) throw new AssertionError("actor leak: " + key);
        assertClean(entry.getValue(), false);
      }
    } else if (value instanceof List<?> list) {
      for (Object item : list) assertClean(item, false);
    }
  }
}
""",
}


def run(command: list[str], cwd: Path | None = None) -> dict[str, object]:
    completed = subprocess.run(command, cwd=cwd, text=True, capture_output=True, check=False)
    return {
        "command": command,
        "exitCode": completed.returncode,
        "stdout": completed.stdout,
        "stderr": completed.stderr,
    }


def version_major(text: str) -> int | None:
    match = re.search(r'(?:version\s+"?|javac\s+)(\d+)', text)
    return int(match.group(1)) if match else None


def write_sources(root: Path, repository_root: Path) -> list[Path]:
    source_root = root / "src"
    copied: list[Path] = []
    for relative in (JAVA_SOURCE, TEST_SOURCE, *DTO_SOURCES):
        source = repository_root / relative
        if not source.is_file():
            raise FileNotFoundError(f"required source missing: {relative}")
        target = source_root / relative.name
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
        copied.append(target)
    for relative, content in STUBS.items():
        target = source_root / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content.strip() + "\n", encoding="utf-8")
        copied.append(target)
    return copied


def class_major(path: Path) -> int:
    data = path.read_bytes()
    if len(data) < 8 or data[:4] != b"\xca\xfe\xba\xbe":
        raise ValueError(f"not a class file: {path}")
    return int.from_bytes(data[6:8], "big")


def validate(repository_root: Path) -> dict[str, object]:
    java_path = shutil.which("java")
    javac_path = shutil.which("javac")
    if not java_path or not javac_path:
        raise RuntimeError("java and javac are required")

    java_version = run([java_path, "-version"])
    javac_version = run([javac_path, "-version"])
    observed = version_major(str(java_version["stderr"]) + str(java_version["stdout"]))
    compiler = version_major(str(javac_version["stderr"]) + str(javac_version["stdout"]))
    if observed != 21 or compiler != 21:
        raise RuntimeError(f"Java 21 required, observed java={observed}, javac={compiler}")

    with tempfile.TemporaryDirectory(prefix="cpf-java21-validation-") as temp:
        temp_root = Path(temp)
        classes = temp_root / "classes"
        classes.mkdir()
        sources = write_sources(temp_root, repository_root)
        compile_result = run([javac_path, "--release", "21", "-Xlint:all", "-d", str(classes), *map(str, sources)])
        if compile_result["exitCode"] != 0:
            raise RuntimeError(json.dumps(compile_result, ensure_ascii=False))

        unit_result = run([java_path, "-cp", str(classes), "com.cpf.admin.opr.batch.runtime.BatchActorUnitRunner"])
        if unit_result["exitCode"] != 0 or "JAVA21_UNIT_TEST_PASS count=2" not in str(unit_result["stdout"]):
            raise RuntimeError(json.dumps(unit_result, ensure_ascii=False))

        harness_result = run([java_path, "-cp", str(classes), "com.cpf.admin.opr.batch.runtime.BatchActorRuntimeHarness"])
        if harness_result["exitCode"] != 0 or "JAVA21_RUNTIME_HARNESS_PASS" not in str(harness_result["stdout"]):
            raise RuntimeError(json.dumps(harness_result, ensure_ascii=False))

        product_class = classes / "com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.class"
        major = class_major(product_class)
        if major != EXPECTED_MAJOR:
            raise RuntimeError(f"unexpected class major: {major}")

        return {
            "status": "PASS",
            "repositoryRoot": str(repository_root),
            "javaVersion": java_version,
            "javacVersion": javac_version,
            "compile": compile_result,
            "unitTest": unit_result,
            "runtimeHarness": harness_result,
            "compiledSourceFiles": [str(JAVA_SOURCE), str(TEST_SOURCE)],
            "classFileMajor": major,
            "expectedClassFileMajor": EXPECTED_MAJOR,
            "repositoryBuildOutputsCreated": False,
        }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repository-root", type=Path, default=Path.cwd())
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    result = validate(args.repository_root.resolve())
    payload = json.dumps(result, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(payload, encoding="utf-8")
    print(payload, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
