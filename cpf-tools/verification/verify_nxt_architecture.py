#!/usr/bin/env python3
from __future__ import annotations
import csv, re, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CORE = ROOT / "cpf-core"
FOUNDATION = ROOT / "cpf-foundation"
FORBIDDEN_CORE = (
    "org.springframework.web", "jakarta.servlet", "io.opentelemetry",
    "org.springframework.batch", "software.amazon.awssdk", "org.springframework.data.redis",
)
FORBIDDEN_FOUNDATION = (
    "org.springframework", "jakarta.", "io.opentelemetry", "software.amazon.awssdk", "org.apache.",
)

def java_files(base: Path):
    return sorted(base.rglob("*.java")) if base.exists() else []

def imports(text: str):
    return re.findall(r"^import\s+([^;]+);", text, flags=re.M)

def classification(path: Path, text: str) -> str:
    rel = path.relative_to(ROOT).as_posix()
    if "/api/util/" in rel: return "MOVE_FOUNDATION_OR_CAPABILITY"
    if "/config/" in rel and "AutoConfiguration" in path.name: return "MOVE_STARTER"
    if any(x in text for x in ("OncePerRequestFilter", "WebMvcConfigurer", "ResponseBodyAdvice")): return "MOVE_STARTER"
    if any(x in text for x in ("io.opentelemetry", "OpenTelemetry")): return "MOVE_PROVIDER"
    if any(x in text for x in ("JdbcTemplate", "DataSource", "EntityManager")): return "MOVE_PROVIDER"
    if "/internal/" in rel and ("InMemory" in path.name or "Adapter" in path.name): return "MOVE_CAPABILITY"
    return "KEEP_CORE"

def main() -> int:
    failures=[]; rows=[]
    for path in java_files(CORE / "src/main/java"):
        text=path.read_text(encoding="utf-8")
        decision=classification(path,text)
        bad=[i for i in imports(text) if i.startswith(FORBIDDEN_CORE)]
        if bad: failures.append(f"CORE_FORBIDDEN_IMPORT:{path.relative_to(ROOT)}:{','.join(bad)}")
        if decision != "KEEP_CORE": failures.append(f"CORE_OWNERSHIP_REVIEW_REQUIRED:{path.relative_to(ROOT)}:{decision}")
        rows.append((path.relative_to(ROOT).as_posix(),decision,";".join(bad)))
    for path in java_files(FOUNDATION / "src/main/java"):
        text=path.read_text(encoding="utf-8")
        bad=[i for i in imports(text) if i.startswith(FORBIDDEN_FOUNDATION)]
        if bad: failures.append(f"FOUNDATION_FORBIDDEN_IMPORT:{path.relative_to(ROOT)}:{','.join(bad)}")
    for path in java_files(ROOT):
        if str(path).startswith(str(CORE)): continue
        text=path.read_text(encoding="utf-8",errors="ignore")
        if "import com.cpf.core.internal" in text:
            failures.append(f"EXTERNAL_INTERNAL_REFERENCE:{path.relative_to(ROOT)}")
    out=ROOT/"cpf-docs/work/CPF_CORE_SLIMMING_AUDIT.csv"
    out.parent.mkdir(parents=True,exist_ok=True)
    with out.open("w",encoding="utf-8",newline="") as f:
        w=csv.writer(f); w.writerow(["path","classification","forbidden_imports"]); w.writerows(rows)
    if failures:
        print("NXT_ARCHITECTURE_GATE=FAIL")
        print("\n".join(failures))
        return 1
    print(f"NXT_ARCHITECTURE_GATE=PASS core_classes={len(rows)}")
    return 0
if __name__ == "__main__": sys.exit(main())
