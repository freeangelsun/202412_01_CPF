import importlib.util
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
MODULE_PATH = ROOT / "cpf-tools/verification/verify_gradle_project_dependency_closure.py"
SPEC = importlib.util.spec_from_file_location("gradle_dependency_closure", MODULE_PATH)
MODULE = importlib.util.module_from_spec(SPEC)
assert SPEC and SPEC.loader
SPEC.loader.exec_module(MODULE)


class GradleProjectDependencyClosureTest(unittest.TestCase):
    def test_multiline_include_and_implicit_parent_are_declared(self):
        with tempfile.TemporaryDirectory() as directory:
            settings = Path(directory) / "settings.gradle"
            settings.write_text("include(\n  ':one',\n  ':two:leaf'\n)\n", encoding="utf-8")
            self.assertEqual(
                {":one", ":two", ":two:leaf"}, MODULE.declared_projects(settings)
            )

    def test_scc_detects_cycle_and_self_loop(self):
        graph = {":a": {":b"}, ":b": {":a"}, ":self": {":self"}}
        components = MODULE.strongly_connected_components(graph)
        self.assertIn([":a", ":b"], components)
        self.assertIn([":self"], components)

    def test_undeclared_reference_is_distinguishable_from_declared_edge(self):
        declared = {":a", ":b"}
        targets = set(MODULE.PROJECT_REFERENCE.findall(
            "implementation project(':b')\nimplementation project(':missing')"
        ))
        self.assertEqual({":missing"}, targets - declared)

    def test_test_scoped_edges_are_excluded_from_production_cycle_graph(self):
        text = "implementation project(':main')\ntestImplementation project(':fixture')\nintegrationTestImplementation project(':it')\n"
        self.assertEqual([":main"], MODULE.production_project_targets(text))

    def test_same_group_and_leaf_dependency_is_rejected(self):
        self.assertIsNotNone(MODULE.same_component_identity_violation(
            ":internal:messaging:jdbc", "com.cpf.starter", ":starters:data:jdbc", "com.cpf.starter"
        ))
        self.assertIsNone(MODULE.same_component_identity_violation(
            ":starters:data:mybatis", "com.cpf.starter", ":starters:data:jdbc", "com.cpf.starter"
        ))
        self.assertIsNone(MODULE.same_component_identity_violation(
            ":internal:messaging:jdbc", "com.cpf.internal", ":starters:data:jdbc", "com.cpf.starter"
        ))

    def test_aggregate_and_profile_boundaries(self):
        self.assertIsNotNone(MODULE.aggregate_boundary_violation(
            ":provider", "starter-internal", ":starters:base", "starter-base", True
        ))
        self.assertIsNone(MODULE.aggregate_boundary_violation(
            ":profile", "starter-profile", ":starters:base", "starter-base", True
        ))
        self.assertIsNotNone(MODULE.aggregate_boundary_violation(
            ":runtime:batch:worker", "", ":starters:profiles:batch", "starter-profile", True
        ))
        self.assertIsNone(MODULE.aggregate_boundary_violation(
            ":apps:education", "", ":starters:profiles:batch", "starter-profile", False
        ))


if __name__ == "__main__":
    unittest.main()
