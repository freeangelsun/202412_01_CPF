#!/usr/bin/env python3
from __future__ import annotations

import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(sys.argv[1] if len(sys.argv) > 1 else '.').resolve()
JAVA_FILES = sorted(ROOT.rglob('*.java'))

HELPER = r'''
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import com.sun.source.util.JavacTask;

public final class CpfJavaSyntaxGate {
    public static void main(String[] args) throws Exception {
        Path root = Paths.get(args[0]).toAbsolutePath().normalize();
        List<Path> paths = new ArrayList<>();
        try (var stream = Files.walk(root)) {
            stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .sorted()
                    .forEach(paths::add);
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
    completed = subprocess.run(
        [command('java'), '-cp', str(temp_path), 'CpfJavaSyntaxGate', str(ROOT)],
        check=False,
        text=True,
        capture_output=True,
    )
    if completed.stdout:
        print(completed.stdout, end='')
    if completed.stderr:
        print(completed.stderr, end='', file=sys.stderr)
    raise SystemExit(completed.returncode)
