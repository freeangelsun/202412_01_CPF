#!/usr/bin/env python3
from __future__ import annotations

import argparse
import os
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

_parser = argparse.ArgumentParser()
_parser.add_argument('--root', default='.')
_args = _parser.parse_args()
ROOT = Path(_args.root).resolve()

def _skip_dir(root: Path, candidate: Path) -> bool:
    try:
        rel = candidate.relative_to(root).as_posix()
    except ValueError:
        return True
    parts = candidate.relative_to(root).parts
    if not parts:
        return False
    if parts[0] == "build":
        return True
    if any(part in {".git", ".gradle", "node_modules", "__pycache__", ".pytest_cache"} for part in parts):
        return True
    # Module Gradle output directories are generated, except cpf-tools/build which is product source.
    if "build" in parts and not rel.startswith("cpf-tools/build/"):
        return True
    return False

def java_files(root: Path) -> list[Path]:
    result: list[Path] = []
    for current, dirs, files in os.walk(root, topdown=True, onerror=lambda _error: None):
        current_path = Path(current)
        dirs[:] = [name for name in dirs if not _skip_dir(root, current_path / name)]
        if _skip_dir(root, current_path):
            continue
        for name in files:
            if name.endswith(".java"):
                result.append(current_path / name)
    return sorted(result)

JAVA_FILES = java_files(ROOT)

HELPER = r'''
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import com.sun.source.util.JavacTask;

public final class CpfJavaSyntaxGate {
    public static void main(String[] args) throws Exception {
        Path listFile = Paths.get(args[0]).toAbsolutePath().normalize();
        List<Path> paths = new ArrayList<>();
        for (String line : Files.readAllLines(listFile, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) paths.add(Paths.get(line));
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK compiler is unavailable. Use a JDK, not a JRE.");
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnostics, Locale.ROOT, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromPaths(paths);
            JavacTask task = (JavacTask) compiler.getTask(
                    null, fileManager, diagnostics, List.of("-proc:none", "-Xlint:none"), null, units);
            task.parse();
        }
        int errors = 0;
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            if (diagnostic.getKind() != Diagnostic.Kind.ERROR) {
                continue;
            }
            errors++;
            String source = diagnostic.getSource() == null
                    ? "<unknown>"
                    : Paths.get(diagnostic.getSource().toUri()).toString();
            System.err.printf(
                    "%s:%d:%d: %s%n",
                    source,
                    diagnostic.getLineNumber(),
                    diagnostic.getColumnNumber(),
                    diagnostic.getMessage(Locale.ROOT));
        }
        if (errors > 0) {
            System.err.printf("JAVA_SYNTAX=FAIL files=%d errors=%d%n", paths.size(), errors);
            System.exit(1);
        }
        System.out.printf("JAVA_SYNTAX=PASS files=%d errors=0%n", paths.size());
    }
}
'''


def command(name: str) -> str:
    resolved = shutil.which(name)
    if not resolved:
        raise SystemExit(f'{name} is required but was not found on PATH.')
    return resolved


if not JAVA_FILES:
    raise SystemExit('No Java source files were found.')

with tempfile.TemporaryDirectory(prefix='cpf-java-syntax-') as temp:
    temp_path = Path(temp)
    source = temp_path / 'CpfJavaSyntaxGate.java'
    source.write_text(HELPER, encoding='utf-8')
    subprocess.run([command('javac'), str(source)], check=True)
    source_list = temp_path / 'java-sources.txt'
    source_list.write_text('\n'.join(str(path) for path in JAVA_FILES) + '\n', encoding='utf-8')
    completed = subprocess.run(
        [command('java'), '-cp', str(temp_path), 'CpfJavaSyntaxGate', str(source_list)],
        check=False,
        text=True,
        capture_output=True,
    )
    if completed.stdout:
        print(completed.stdout, end='')
    if completed.stderr:
        print(completed.stderr, end='', file=sys.stderr)
    raise SystemExit(completed.returncode)
