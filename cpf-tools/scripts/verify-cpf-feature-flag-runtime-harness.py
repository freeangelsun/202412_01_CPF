#!/usr/bin/env python3
"""Compile and execute the vendor-neutral CPF feature-flag runtime without Spring/OpenFeature SDK.

This verifier is intentionally dependency-free. It validates the public API/SPI boundary and
runs deterministic assertions for cache/revision, fallback, controlled-state precedence,
override lifecycle, kill switch, audit sanitization, and input fail-closed behavior.
"""
from __future__ import annotations

import argparse
import json
import shutil
import subprocess
import tempfile
from pathlib import Path

SOURCE_PATHS = [
    "cpf-core/src/main/java/com/cpf/core/api/featureflag/CpfFeatureFlagContext.java",
    "cpf-core/src/main/java/com/cpf/core/api/featureflag/CpfFeatureFlagOperations.java",
    "cpf-core/src/main/java/com/cpf/core/api/featureflag/CpfFeatureFlagResult.java",
    "cpf-core/src/main/java/com/cpf/core/api/featureflag/CpfFeatureFlagValue.java",
    "cpf-core/src/main/java/com/cpf/core/spi/featureflag/CpfFeatureFlagProvider.java",
    "cpf-core/src/main/java/com/cpf/core/spi/featureflag/CpfFeatureFlagStateStore.java",
    "cpf-core/src/main/java/com/cpf/core/spi/featureflag/CpfFeatureFlagAuditSink.java",
    "cpf-starters/platform-operations/feature-flag-openfeature/src/main/java/com/cpf/starter/platform/operations/feature/flag/openfeature/internal/CpfFeatureFlagTransactionRunner.java",
    "cpf-starters/platform-operations/feature-flag-openfeature/src/main/java/com/cpf/starter/platform/operations/feature/flag/openfeature/internal/CpfFeatureFlagRuntime.java",
]

HARNESS_PATH = (
    "com/cpf/starter/platform/operations/feature/flag/openfeature/internal/"
    "FeatureFlagRuntimeHarness.java"
)

HARNESS_SOURCE = r'''package com.cpf.starter.platform.operations.feature.flag.openfeature.internal;

import com.cpf.core.api.featureflag.*;
import com.cpf.core.spi.featureflag.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class FeatureFlagRuntimeHarness {
    private static final Instant NOW = Instant.parse("2026-08-05T15:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    public static void main(String[] args) {
        List<String> passed = new ArrayList<>();
        contextSanitizesSensitiveAttributes(passed);
        providerResultIsCachedAndRevisionInvalidates(passed);
        providerFailureFallsBack(passed);
        controlledStatePrecedesProvider(passed);
        overrideLifecycleIsTransactionalAndAudited(passed);
        killSwitchAndValidationFailClosed(passed);
        System.out.println("PASS assertions=" + passed.size() + " " + String.join(",", passed));
    }

    private static void contextSanitizesSensitiveAttributes(List<String> passed) {
        var c = new CpfFeatureFlagContext("  member-1 ", Map.of(
                "token", "secret-token", "Password", "pw", "segment", " gold "));
        check("member-1".equals(c.targetingKey()), "target key normalized");
        check(c.attributes().equals(Map.of("segment", "gold")), "sensitive context keys filtered");
        passed.add("context-sanitization");
    }

    private static void providerResultIsCachedAndRevisionInvalidates(List<String> passed) {
        var provider = new Provider(false); var state = new State(); var audit = new Audit();
        var runtime = new CpfFeatureFlagRuntime(provider, state, audit, CLOCK, Duration.ofSeconds(30));
        var fallback = new CpfFeatureFlagValue.BooleanValue(false);
        var context = new CpfFeatureFlagContext("member-1", Map.of("segment", "gold"));
        var first = runtime.evaluate("payments.new-flow", fallback, context);
        var second = runtime.evaluate("payments.new-flow", fallback, context);
        check(first.source() == CpfFeatureFlagResult.Source.PROVIDER, "first provider source");
        check(second.source() == CpfFeatureFlagResult.Source.CACHE, "second cache source");
        check(provider.calls.get() == 1, "provider called once");
        state.revision++;
        var third = runtime.evaluate("payments.new-flow", fallback, context);
        check(third.source() == CpfFeatureFlagResult.Source.PROVIDER, "revision invalidates cache");
        check(provider.calls.get() == 2, "provider called after revision");
        check(audit.events.stream().allMatch(e -> !e.attributes.toString().contains("member-1")),
                "raw targeting key not audited");
        passed.add("cache-revision-audit");
    }

    private static void providerFailureFallsBack(List<String> passed) {
        var runtime = new CpfFeatureFlagRuntime(new Provider(true), new State(), new Audit(), CLOCK,
                Duration.ofSeconds(30));
        var fallback = new CpfFeatureFlagValue.StringValue("safe-default");
        var result = runtime.evaluate("unstable.flag", fallback,
                new CpfFeatureFlagContext("m", Map.of()));
        check(result.source() == CpfFeatureFlagResult.Source.FALLBACK, "provider exception fallback source");
        check(result.value().equals(fallback), "provider exception fallback value");
        check("PROVIDER_ERROR".equals(result.reasonCode()), "provider exception reason");
        passed.add("provider-fallback");
    }

    private static void controlledStatePrecedesProvider(List<String> passed) {
        var provider = new Provider(false); var state = new State();
        state.effective = new CpfFeatureFlagResult<>("kill.flag",
                new CpfFeatureFlagValue.BooleanValue(false), "off", "KILL_SWITCH",
                CpfFeatureFlagResult.Source.KILL_SWITCH, 7, NOW);
        var runtime = new CpfFeatureFlagRuntime(provider, state, new Audit(), CLOCK,
                Duration.ofSeconds(30));
        var result = runtime.evaluate("kill.flag", new CpfFeatureFlagValue.BooleanValue(true),
                new CpfFeatureFlagContext("m", Map.of()));
        check(result.source() == CpfFeatureFlagResult.Source.KILL_SWITCH, "controlled state precedence");
        check(provider.calls.get() == 0, "provider bypassed by controlled state");
        passed.add("controlled-state-precedence");
    }

    private static void overrideLifecycleIsTransactionalAndAudited(List<String> passed) {
        var state = new State(); var audit = new Audit();
        var runtime = new CpfFeatureFlagRuntime(new Provider(false), state, audit, CLOCK,
                Duration.ofSeconds(30));
        String requestId = runtime.requestOverride("risk.flag",
                new CpfFeatureFlagValue.BooleanValue(true), NOW.plusSeconds(600),
                "requester-1", "incident mitigation");
        check("REQ-1".equals(requestId), "request id");
        var approved = runtime.approveOverride(requestId, "approver-2",
                "approved incident mitigation");
        check(approved.source() == CpfFeatureFlagResult.Source.SECURE_OVERRIDE,
                "approved override source");
        check(state.revision >= 1, "approval increments revision");
        runtime.revokeOverride(requestId, "operator-3", "incident closed");
        check(state.revoked, "override revoked");
        check(audit.types().containsAll(List.of("FEATURE_FLAG_OVERRIDE_REQUESTED",
                "FEATURE_FLAG_OVERRIDE_APPROVED", "FEATURE_FLAG_OVERRIDE_REVOKED")),
                "lifecycle audit events");
        passed.add("override-lifecycle");
    }

    private static void killSwitchAndValidationFailClosed(List<String> passed) {
        var state = new State(); var audit = new Audit();
        var runtime = new CpfFeatureFlagRuntime(new Provider(false), state, audit, CLOCK,
                Duration.ofSeconds(30));
        runtime.setKillSwitch("kill.flag", true, "operator", "emergency stop");
        check(state.killSwitch, "kill switch persisted");
        check(audit.types().contains("FEATURE_FLAG_KILL_SWITCH_CHANGED"), "kill switch audited");
        expectIllegal(() -> runtime.requestOverride("x", new CpfFeatureFlagValue.BooleanValue(true),
                NOW, "r", "why"), "expired override rejected");
        expectIllegal(() -> runtime.requestOverride("x", new CpfFeatureFlagValue.BooleanValue(true),
                NOW.plusSeconds(1), "", "why"), "blank requester rejected");
        expectIllegal(() -> runtime.search("", -1, 50), "negative page rejected");
        expectIllegal(() -> runtime.search("", 0, 501), "oversize page rejected");
        passed.add("kill-switch-validation");
    }

    private static void expectIllegal(Runnable work, String message) {
        try { work.run(); throw new AssertionError(message); }
        catch (IllegalArgumentException expected) { }
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class Provider implements CpfFeatureFlagProvider {
        final AtomicInteger calls = new AtomicInteger(); final boolean fail;
        Provider(boolean fail) { this.fail = fail; }
        public CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(String key,
                CpfFeatureFlagValue fallback, CpfFeatureFlagContext context) {
            calls.incrementAndGet(); if (fail) throw new IllegalStateException("provider down");
            return new CpfFeatureFlagResult<>(key, new CpfFeatureFlagValue.BooleanValue(true),
                    "on", "TARGETING_MATCH", CpfFeatureFlagResult.Source.PROVIDER, revision(), NOW);
        }
        public long revision() { return 3; }
    }

    private static final class State implements CpfFeatureFlagStateStore {
        long revision; CpfFeatureFlagResult<CpfFeatureFlagValue> effective;
        boolean revoked; boolean killSwitch;
        public Optional<CpfFeatureFlagResult<CpfFeatureFlagValue>> findEffective(String key, Instant now) {
            return Optional.ofNullable(effective);
        }
        public List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(String filter, int offset,
                int limit, Instant now) { return effective == null ? List.of() : List.of(effective); }
        public String requestOverride(String key, CpfFeatureFlagValue value, Instant expiresAt,
                String requester, String reason) { return "REQ-1"; }
        public CpfFeatureFlagResult<CpfFeatureFlagValue> approveOverride(String id,
                String approver, String reason, Instant now) {
            revision++;
            effective = new CpfFeatureFlagResult<>("risk.flag",
                    new CpfFeatureFlagValue.BooleanValue(true), "override", "APPROVED",
                    CpfFeatureFlagResult.Source.SECURE_OVERRIDE, revision, now);
            return effective;
        }
        public void revokeOverride(String id, String operator, String reason, Instant now) {
            revoked = true; effective = null; revision++;
        }
        public void setKillSwitch(String key, boolean enabled, String operator, String reason,
                Instant now) { killSwitch = enabled; revision++; }
        public long revision() { return revision; }
    }

    private static final class Audit implements CpfFeatureFlagAuditSink {
        final List<Event> events = new ArrayList<>();
        public void record(String eventType, String flagKey, String actorId, String reason,
                Map<String,String> attributes, Instant occurredAt) {
            events.add(new Event(eventType, flagKey, actorId, reason, Map.copyOf(attributes)));
        }
        List<String> types() { return events.stream().map(Event::type).toList(); }
    }
    private record Event(String type, String flagKey, String actorId, String reason,
                         Map<String,String> attributes) {}
}
'''


def run(repo_root: Path, report_json: Path | None = None) -> dict:
    missing = [path for path in SOURCE_PATHS if not (repo_root / path).is_file()]
    result: dict = {
        "status": "FAIL" if missing else "PENDING",
        "source_paths": SOURCE_PATHS,
        "missing_source_paths": missing,
        "assertion_count": 0,
        "assertions": [],
        "javac_exit_code": None,
        "java_exit_code": None,
    }
    if missing:
        if report_json:
            report_json.parent.mkdir(parents=True, exist_ok=True)
            report_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        return result
    if not shutil.which("javac") or not shutil.which("java"):
        result.update(status="FAIL", error="java and javac are required")
        if report_json:
            report_json.parent.mkdir(parents=True, exist_ok=True)
            report_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        return result

    with tempfile.TemporaryDirectory(prefix="cpf-feature-flag-runtime-") as tmp:
        tmp_path = Path(tmp)
        src = tmp_path / "src"
        classes = tmp_path / "classes"
        classes.mkdir(parents=True)
        copied: list[Path] = []
        for relative in SOURCE_PATHS:
            source = repo_root / relative
            package_path = Path(relative.split("/src/main/java/", 1)[1])
            target = src / package_path
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, target)
            copied.append(target)
        harness = src / HARNESS_PATH
        harness.parent.mkdir(parents=True, exist_ok=True)
        harness.write_text(HARNESS_SOURCE, encoding="utf-8")
        copied.append(harness)

        compile_run = subprocess.run(
            ["javac", "-d", str(classes), *map(str, copied)],
            text=True, capture_output=True, check=False,
        )
        result["javac_exit_code"] = compile_run.returncode
        result["javac_stdout"] = compile_run.stdout
        result["javac_stderr"] = compile_run.stderr
        if compile_run.returncode != 0:
            result["status"] = "FAIL"
        else:
            runtime_run = subprocess.run(
                ["java", "-cp", str(classes),
                 "com.cpf.starter.platform.operations.feature.flag.openfeature.internal.FeatureFlagRuntimeHarness"],
                text=True, capture_output=True, check=False,
            )
            result["java_exit_code"] = runtime_run.returncode
            result["java_stdout"] = runtime_run.stdout
            result["java_stderr"] = runtime_run.stderr
            if runtime_run.returncode == 0 and runtime_run.stdout.startswith("PASS assertions=6 "):
                assertion_text = runtime_run.stdout.strip().split(" ", 2)[2]
                result.update(
                    status="PASS",
                    assertion_count=6,
                    assertions=assertion_text.split(","),
                )
            else:
                result["status"] = "FAIL"

    if report_json:
        report_json.parent.mkdir(parents=True, exist_ok=True)
        report_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repo-root", type=Path, default=Path("."))
    parser.add_argument("--report-json", type=Path)
    args = parser.parse_args()
    result = run(args.repo_root.resolve(), args.report_json)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if result["status"] == "PASS" else 1


if __name__ == "__main__":
    raise SystemExit(main())
