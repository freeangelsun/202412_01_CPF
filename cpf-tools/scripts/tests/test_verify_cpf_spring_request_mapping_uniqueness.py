from __future__ import annotations

import importlib.util
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "verify-cpf-spring-request-mapping-uniqueness.py"


def load():
    spec = importlib.util.spec_from_file_location("mapping_gate", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    assert spec.loader
    spec.loader.exec_module(module)
    return module


def controller(root: Path, name: str, base: str, method: str, path: str, handler: str = "run") -> None:
    target = root / f"cpf-admin/src/main/java/com/cpf/admin/{name}.java"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(f'''package com.cpf.admin;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("{base}")
public class {name} {{
 @{method}Mapping("{path}")
 public String {handler}() {{ return "ok"; }}
}}
''', encoding="utf-8")


class MappingUniquenessTest(unittest.TestCase):
    def test_unique_mappings_pass(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            controller(root, "AController", "/api/a", "Get", "/{id}")
            controller(root, "BController", "/api/b", "Get", "/{id}")
            self.assertEqual("PASS", load().verify(root)["status"])

    def test_duplicate_mapping_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            controller(root, "AController", "/adm/api/approvals", "Get", "")
            controller(root, "BController", "/adm/api/approvals", "Get", "")
            with self.assertRaises(Exception): load().verify(root)

    def test_variable_names_are_normalized(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            controller(root, "AController", "/api", "Delete", "/items/{id}")
            controller(root, "BController", "/api", "Delete", "/items/{itemId}")
            with self.assertRaises(Exception): load().verify(root)

    def test_different_http_methods_are_allowed(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            controller(root, "AController", "/api", "Get", "/items")
            controller(root, "BController", "/api", "Post", "/items")
            self.assertEqual("PASS", load().verify(root)["status"])

    def test_comments_do_not_create_fake_mapping(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            controller(root, "AController", "/api", "Get", "/items")
            target = root / "cpf-admin/src/main/java/com/cpf/admin/BController.java"
            target.write_text('''package com.cpf.admin;
// @RestController @RequestMapping("/api") @GetMapping("/items")
public class BController {}
''', encoding="utf-8")
            self.assertEqual("PASS", load().verify(root)["status"])


if __name__ == "__main__": unittest.main()
