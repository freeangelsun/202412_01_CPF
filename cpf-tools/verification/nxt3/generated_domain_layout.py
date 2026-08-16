#!/usr/bin/env python3
"""Generated Domain 물리 IA 판정에 사용하는 공통 경로 정책."""
from __future__ import annotations
from pathlib import Path

# Gradle/Node 실행이 만드는 캐시는 Generated Domain의 업무 IA가 아니다.
RUNTIME_GENERATED_DIRS = {'.gradle', '.pytest_cache', 'build', 'out', 'node_modules'}

def domain_surface_dirs(root: Path) -> set[str]:
    if not root.is_dir():
        return set()
    return {
        child.name
        for child in root.iterdir()
        if child.is_dir()
        and child.name not in RUNTIME_GENERATED_DIRS
        and any(item.is_file() for item in child.rglob('*'))
    }
