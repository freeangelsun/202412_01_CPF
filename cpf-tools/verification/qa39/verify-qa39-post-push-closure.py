from __future__ import annotations
from pathlib import Path
import json, re, sys, collections

R = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path.cwd()
errors: list[str] = []
warnings: list[str] = []

def text(path: Path) -> str:
    return path.read_text(encoding="utf-8-sig", errors="replace")

settings_path = R / "settings.gradle"
if not settings_path.exists():
    raise SystemExit("settings.gradle not found: " + str(R))
settings = text(settings_path)

includes: set[str] = set()
for m in re.finditer(r"(?m)^\s*include\s+([^\n]+)", settings):
    includes.update((":" + v.lstrip(":")) for v in re.findall(r"'([^']+)'", m.group(1)))
mappings: dict[str, str] = {}
for m in re.finditer(r"project\('(:[^']+)'\)\.projectDir\s*=\s*file\('([^']+)'\)", settings):
    mappings[m.group(1)] = m.group(2)

mapped_dirs = set(mappings.values())
for inc in includes:
    if inc not in mappings:
        rel = inc.lstrip(":").replace(":", "/")
        if (R / rel / "build.gradle").exists():
            mapped_dirs.add(rel)

build_dirs = sorted(
    p.parent.relative_to(R).as_posix()
    for p in R.rglob("build.gradle")
    if p.parent != R and p.parent.relative_to(R).as_posix().startswith("cpf-starters/")
)
unmapped = [d for d in build_dirs if d not in mapped_dirs]
for d in unmapped:
    errors.append("UNREGISTERED_MODULE " + d)

catalog_path = R / "cpf-tools/release/cpf-final-artifact-catalog.json"
catalog = json.loads(text(catalog_path))
catalog_owners = {str(x.get("ownerPath")) for x in catalog.get("qa38StarterArtifacts", [])}
for d in build_dirs:
    if d not in catalog_owners and d not in {"cpf-starters/security"}:
        errors.append("ARTIFACT_CATALOG_MISSING " + d)

platform_text = text(R / "gradle/cpf-platform.properties")
bom_text = text(R / "cpf-tools/build/platform-bom/build.gradle")
literal_bom = re.findall(r"api\s+'[^']*\$\{project\.version\}[^']*'", bom_text)
if literal_bom:
    errors.append(f"BOM_LITERAL_PROJECT_VERSION count={len(literal_bom)}")

# project dependency graph with api transitivity
project_for_dir = {v: k for k, v in mappings.items()}
for inc in includes:
    if inc not in mappings:
        rel = inc.lstrip(":").replace(":", "/")
        if (R / rel / "build.gradle").exists():
            project_for_dir.setdefault(rel, inc)
for d in build_dirs:
    project_for_dir.setdefault(d, ":UNMAPPED:" + d)

deps: dict[str, list[str]] = {}
scopes: dict[str, dict[str, str]] = {}
for rel, project in project_for_dir.items():
    build = R / rel / "build.gradle"
    if not build.exists():
        continue
    body = text(build)
    ds: list[str] = []
    sm: dict[str, str] = {}
    for m in re.finditer(r"(?m)^\s*(api|implementation|compileOnly|runtimeOnly)\s+project\('([^']+)'\)", body):
        target = m.group(2)
        if not target.startswith(":"):
            target = ":" + target
        ds.append(target)
        sm[target] = m.group(1)
    deps[project] = ds
    scopes[project] = sm

def compile_projects(project: str) -> set[str]:
    result = {project}
    stack = list(deps.get(project, []))
    while stack:
        dep = stack.pop()
        if dep in result:
            continue
        result.add(dep)
        for child in deps.get(dep, []):
            if scopes.get(dep, {}).get(child) == "api":
                stack.append(child)
    return result

class_owner: dict[str, str] = {}
for rel, project in project_for_dir.items():
    src = R / rel / "src/main/java"
    if not src.exists():
        continue
    for java in src.rglob("*.java"):
        body = text(java)
        pm = re.search(r"\bpackage\s+([\w.]+)\s*;", body)
        if pm:
            class_owner[pm.group(1) + "." + java.stem] = project

classpath_errors = 0
for rel, project in project_for_dir.items():
    src = R / rel / "src/main/java"
    if not src.exists():
        continue
    cp = compile_projects(project)
    for java in src.rglob("*.java"):
        body = text(java)
        for imported in re.findall(r"\bimport\s+([\w.]+)\s*;", body):
            owner = class_owner.get(imported)
            if owner and owner not in cp:
                errors.append(
                    "INTERNAL_CLASSPATH_MISSING "
                    + java.relative_to(R).as_posix()
                    + " imports " + imported + " owner=" + owner
                )
                classpath_errors += 1

# Generator provider bindings must affect dependency generation, not metadata only.
generator = text(R / "cpf-tools/generator/create-domain.ps1")
binding_uses = [m.start() for m in re.finditer(r"\$resolvedProviderBindings", generator)]
if len(binding_uses) <= 5 and "profileDependency" not in generator[min(binding_uses or [0]):max(binding_uses or [0])+1]:
    errors.append("GENERATOR_PROVIDER_BINDING_METADATA_ONLY")
else:
    # exact current implementation has five uses and none in dependency resolution section
    dependency_slice = generator[generator.find("$profileProjectName"):generator.find("$profileApplicationFiles")]
    if "$resolvedProviderBindings" not in dependency_slice:
        errors.append("GENERATOR_PROVIDER_BINDING_DOES_NOT_CHANGE_DEPENDENCY")

# Archive moved implementation must be wired to the legacy/core API.
archive_auto = text(R / "cpf-starters/file-archive/src/main/java/com/cpf/starter/archive/CpfArchiveAutoConfiguration.java")
if "LocalCpfArchiveService" not in archive_auto:
    errors.append("ARCHIVE_LEGACY_API_IMPLEMENTATION_NOT_WIRED")

batch_build = text(R / "cpf-batch/build.gradle")
if re.search(r"subprojects\s*\{[\s\S]*cpf-starter-profile-event-kafka", batch_build):
    errors.append("BATCH_RUNTIME_PROFILE_APPLIED_TO_ALL_SUBPROJECTS")

# Product Java modules with zero tests.
zero_test_modules = []
for d in build_dirs:
    main = R / d / "src/main/java"
    if not main.exists() or not any(main.rglob("*.java")):
        continue
    test = R / d / "src/test/java"
    if not test.exists() or not any(test.rglob("*.java")):
        zero_test_modules.append(d)
for d in zero_test_modules:
    warnings.append("PRODUCT_MODULE_WITH_ZERO_TESTS " + d)

matrix_path = R / "cpf-docs/quality/CPF_QA38_FINAL_REQUIREMENT_MATRIX.csv"
if matrix_path.exists():
    import csv
    with matrix_path.open(encoding="utf-8-sig", newline="") as fh:
        rows = list(csv.DictReader(fh))
    if rows and all(r.get("development_status") == "완료" for r in rows):
        errors.append(f"FALSE_COMPLETE_MATRIX all_complete={len(rows)}")

latest_expected = "54bcc10887a83b933685bff462c0b0d7df824923"
stale_sha_files = []
for rel in [
    "cpf-tools/release/cpf-final-artifact-catalog.json",
    "cpf-docs/work/manifest/CPF_QA38_PACKAGE_MANIFEST.json",
    "cpf-docs/evidence/20260802/qa38/QA38_LOCAL_VERIFICATION_EVIDENCE.md",
]:
    p = R / rel
    if p.exists() and latest_expected not in text(p):
        stale_sha_files.append(rel)
for rel in stale_sha_files:
    errors.append("STALE_EXACT_SHA " + rel)

print(f"MODULES={len(build_dirs)} UNREGISTERED={len(unmapped)} CLASSPATH_ERRORS={classpath_errors} ZERO_TEST_MODULES={len(zero_test_modules)}")
for item in errors:
    print("ERROR", item)
for item in warnings:
    print("WARN", item)
if errors:
    print("QA39_POST_PUSH_CLOSURE_FAIL")
    raise SystemExit(1)
print("QA39_POST_PUSH_CLOSURE_PASS")
