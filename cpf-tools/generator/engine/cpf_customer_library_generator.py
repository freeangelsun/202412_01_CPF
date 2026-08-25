#!/usr/bin/env python3
"""고객사 공통 JAR 작업공간을 생성하고 Generated Domain에 선택적으로 연결한다."""
from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path


class CustomerLibraryError(ValueError):
    pass


@dataclass(frozen=True)
class CustomerLibrary:
    name: str
    group: str
    package_name: str
    version: str
    root: Path

    @property
    def artifact(self) -> str:
        return self.name

    @property
    def coordinate(self) -> str:
        return f"{self.group}:{self.artifact}:{self.version}"


def _name(value: str) -> str:
    value = value.strip().lower()
    if not re.fullmatch(r"[a-z][a-z0-9-]{1,49}", value):
        raise CustomerLibraryError("Library 이름은 영문 소문자로 시작하는 2~50자리 kebab-case여야 합니다.")
    return value


def _java_name(value: str, label: str) -> str:
    value = value.strip()
    if not re.fullmatch(r"[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)*", value):
        raise CustomerLibraryError(f"{label} Java package 형식이 올바르지 않습니다: {value}")
    if value == "com.cpf" or value.startswith("com.cpf."):
        raise CustomerLibraryError(f"{label}는 CPF 소유 namespace(com.cpf.*)를 사용할 수 없습니다.")
    return value


def _root(workspace: Path, name: str) -> Path:
    return workspace / "customer-libraries" / name


def _properties(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for raw in path.read_text(encoding="utf-8-sig").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        values[key.strip()] = value.strip()
    return values


def load_library(workspace: Path, name: str) -> CustomerLibrary:
    name = _name(name)
    root = _root(workspace, name)
    contract = root / "gradle.properties"
    if not contract.is_file():
        raise CustomerLibraryError(f"고객사 공통 Library 계약이 없습니다: {contract}")
    values = _properties(contract)
    if values.get("cpf.customerLibrary.contractVersion") != "1":
        raise CustomerLibraryError(f"지원하지 않는 고객사 Library 계약입니다: {contract}")
    actual = values.get("cpf.customerLibrary.name", "")
    if actual != name:
        raise CustomerLibraryError(f"Library 디렉터리/계약 이름 불일치: {name} != {actual}")
    group = _java_name(values.get("cpf.customerLibrary.group", ""), "Library group")
    package_name = _java_name(values.get("cpf.customerLibrary.packageName", ""), "Library package")
    version = values.get("cpf.customerLibrary.version", "").strip()
    if not re.fullmatch(r"[0-9A-Za-z][0-9A-Za-z._+-]{0,63}", version):
        raise CustomerLibraryError(f"Library version 형식이 올바르지 않습니다: {version}")
    return CustomerLibrary(name, group, package_name, version, root)


def create_library(workspace: Path, name: str, group: str | None = None, package_name: str | None = None,
                   version: str = "1.0.0-SNAPSHOT") -> dict:
    workspace = workspace.resolve()
    name = _name(name)
    if group is None or not group.strip():
        raise CustomerLibraryError("고객사 공통 Library는 회사 소유 Java group을 --group으로 명시해야 합니다. 예: com.acme.shared")
    group = _java_name(group, "Library group")
    package_name = _java_name(package_name or f"{group}.{name.replace('-', '_')}", "Library package")
    root = _root(workspace, name)
    if root.exists():
        raise CustomerLibraryError(f"고객사 공통 Library가 이미 존재합니다: {root}")
    library = CustomerLibrary(name, group, package_name, version.strip(), root)
    root.mkdir(parents=True)
    package_path = Path(*package_name.split("."))
    class_name = "".join(part[:1].upper() + part[1:] for part in re.split(r"[-_]", name)) + "Library"
    files = {
        "settings.gradle": f'''// 고객사 공통 Library의 독립 Build 이름입니다.\nrootProject.name = "{name}"\n''',
        "gradle.properties": (
            "# 고객사 공통 Library의 Source-controlled 계약입니다. Secret을 저장하지 않습니다.\n"
            "cpf.customerLibrary.contractVersion=1\n"
            f"cpf.customerLibrary.name={name}\n"
            f"cpf.customerLibrary.group={group}\n"
            f"cpf.customerLibrary.packageName={package_name}\n"
            f"cpf.customerLibrary.version={library.version}\n"
            "org.gradle.jvmargs=-Xmx1g -Dfile.encoding=UTF-8\n"
        ),
        "build.gradle": f'''// 고객사가 여러 업무 Domain에서 재사용할 공통 함수/DTO/검증 로직을 JAR로 관리하는 작업공간입니다.\n// Domain 연결은 전역 자동 주입하지 않습니다. `cpf library attach --name {name} --domain <domain>`으로 필요한 Domain만 선택합니다.\nplugins {{ id 'java-library'; id 'maven-publish' }}\n\ngroup = providers.gradleProperty('cpf.customerLibrary.group').get()\nversion = providers.gradleProperty('cpf.customerLibrary.version').get()\n\njava {{ toolchain {{ languageVersion = JavaLanguageVersion.of(25) }} }}\n\nrepositories {{ mavenCentral() }}\n\ndependencies {{\n    // 고객사 공통 구현에 필요한 외부/CPF 공개 API가 있을 때만 명시적으로 추가합니다.\n    // 업무 Domain 전용 구현이나 CPF Internal Starter는 이 Library에 추가하지 않습니다.\n}}\n\ntasks.named('test') {{ useJUnitPlatform() }}\n\npublishing {{\n    publications {{\n        library(MavenPublication) {{ from components.java; artifactId = '{name}' }}\n    }}\n}}\n''',
        f"src/main/java/{package_path.as_posix()}/package-info.java": f'''/**\n * 고객사 여러 업무 Domain에서 함께 사용하는 공통 함수·DTO·검증 계약을 둡니다.\n * 특정 Domain 업무 규칙은 이 Library가 아니라 해당 Domain Source가 소유합니다.\n */\npackage {package_name};\n''',
        f"src/main/java/{package_path.as_posix()}/{class_name}.java": f'''package {package_name};\n\n/**\n * 고객사 공통 Library가 정상적으로 연결됐는지 확인할 수 있는 최소 표식입니다.\n * 실제 공통 기능은 이 package 아래 역할별 클래스로 추가하고 이 표식은 삭제해도 됩니다.\n */\npublic final class {class_name} {{\n    private {class_name}() {{ }}\n}}\n''',
        "README.md": f'''# {name}\n\n이 프로젝트는 고객사 공통 JAR 작업공간입니다.\n\n- 여러 Domain에서 공통으로 쓰는 함수, DTO, 검증, 사내 표준 Adapter를 둡니다.\n- 특정 Domain의 업무 규칙은 넣지 않습니다.\n- 모든 Domain에 자동으로 의존성을 넣지 않습니다. 필요한 Domain만 아래 명령으로 연결합니다.\n\n```powershell\ncpf library attach --name {name} --domain member\ncpf library sync\ncpf library verify --name {name}\n```\n\nDomain Root의 `customer-libraries.properties`가 선택 정본이며, `customer-library-settings.gradle`과 `customer-library-dependencies.gradle`은 `cpf library sync`가 재생성합니다.\n''',
    }
    for rel, content in files.items():
        target = root / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8", newline="\n")
    result = verify_library(workspace, name)
    result.update({"action": "LIBRARY_CREATE", "project": str(root), "coordinate": library.coordinate})
    return result


def _domain_root(workspace: Path, domain: str) -> Path:
    domain = domain.strip().lower()
    root = workspace / (domain if domain.startswith("cpf-") else f"cpf-{domain}")
    contract = root / "gradle.properties"
    if not contract.is_file() or "cpf.domain.contractVersion=" not in contract.read_text(encoding="utf-8-sig"):
        raise CustomerLibraryError(f"Generated Domain 계약이 없습니다: {contract}")
    return root


def _selected(domain_root: Path) -> list[str]:
    path = domain_root / "customer-libraries.properties"
    if not path.is_file():
        return []
    values = _properties(path)
    return sorted(dict.fromkeys(filter(None, (x.strip() for x in values.get("cpf.customerLibraries", "").split(",")))))


def _write_selection(domain_root: Path, names: list[str]) -> None:
    names = sorted(dict.fromkeys(_name(x) for x in names))
    (domain_root / "customer-libraries.properties").write_text(
        "# 이 Domain이 명시적으로 선택한 고객사 공통 Library 목록입니다. 전역 자동 의존성 주입은 하지 않습니다.\n"
        f"cpf.customerLibraries={','.join(names)}\n",
        encoding="utf-8", newline="\n")


def sync_domain(workspace: Path, domain_root: Path) -> dict:
    selected = _selected(domain_root)
    libraries = [load_library(workspace, name) for name in selected]
    settings_lines = [
        "// cpf library sync가 생성한 로컬 Composite Build 연결입니다. customer-libraries.properties를 수정하고 다시 sync 하세요.",
        "def cpfCustomerLibraryWorkspace = new File(settingsDir.parentFile, 'customer-libraries').canonicalFile",
    ]
    dependency_lines = [
        "// cpf library sync가 생성한 선택 Dependency입니다. 모든 Domain에 자동 주입하지 않습니다.",
        "dependencies {",
    ]
    for index, lib in enumerate(libraries, 1):
        var = f"cpfCustomerLibrary{index}"
        settings_lines += [
            f"def {var} = new File(cpfCustomerLibraryWorkspace, '{lib.name}').canonicalFile",
            f"if (!new File({var}, 'gradle.properties').isFile()) throw new GradleException('선택한 고객사 Library가 없습니다: {lib.name}')",
            f"includeBuild({var}) {{ dependencySubstitution {{ substitute module('{lib.group}:{lib.artifact}') using project(':') }} }}",
        ]
        dependency_lines.append(f'    implementation "{lib.coordinate}"')
    dependency_lines.append("}")
    (domain_root / "customer-library-settings.gradle").write_text("\n".join(settings_lines) + "\n", encoding="utf-8", newline="\n")
    (domain_root / "customer-library-dependencies.gradle").write_text("\n".join(dependency_lines) + "\n", encoding="utf-8", newline="\n")
    return {"domain": domain_root.name, "libraries": selected, "status": "PASS"}


def attach_library(workspace: Path, name: str, domains: list[str]) -> dict:
    workspace = workspace.resolve(); library = load_library(workspace, name)
    if not domains:
        raise CustomerLibraryError("--domain을 1개 이상 지정해야 합니다.")
    results = []
    for domain in domains:
        domain_root = _domain_root(workspace, domain)
        names = _selected(domain_root)
        if library.name not in names:
            names.append(library.name)
        _write_selection(domain_root, names)
        results.append(sync_domain(workspace, domain_root))
    return {"status": "PASS", "action": "LIBRARY_ATTACH", "library": library.name, "domains": results}


def sync_libraries(workspace: Path) -> dict:
    workspace = workspace.resolve(); results = []
    for contract in sorted(workspace.glob("cpf-*/customer-libraries.properties")):
        results.append(sync_domain(workspace, contract.parent))
    return {"status": "PASS", "action": "LIBRARY_SYNC", "domains": results, "count": len(results)}


def verify_library(workspace: Path, name: str) -> dict:
    library = load_library(workspace.resolve(), name)
    required = ["settings.gradle", "build.gradle", "gradle.properties", "README.md"]
    missing = [rel for rel in required if not (library.root / rel).is_file()]
    java = list((library.root / "src/main/java").rglob("*.java")) if (library.root / "src/main/java").is_dir() else []
    if missing or not java:
        raise CustomerLibraryError(f"고객사 공통 Library 구조가 불완전합니다: missing={missing} java={len(java)}")
    build = (library.root / "build.gradle").read_text(encoding="utf-8")
    if "com.cpf.internal" in build or ":internal:" in build:
        raise CustomerLibraryError("고객사 공통 Library는 CPF Internal dependency를 직접 참조할 수 없습니다.")
    return {"status": "PASS", "action": "LIBRARY_VERIFY", "library": name, "coordinate": library.coordinate, "javaFiles": len(java)}
