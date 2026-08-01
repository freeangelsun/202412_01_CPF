from __future__ import annotations

import importlib.util
import tempfile
import unittest
import sys
from pathlib import Path

MODULE = Path(__file__).parents[1] / "verify-cpf-adm-route-source-consumers.py"
spec = importlib.util.spec_from_file_location("route_source_gate", MODULE)
gate = importlib.util.module_from_spec(spec)
assert spec.loader
sys.modules[spec.name] = gate
spec.loader.exec_module(gate)


class RouteSourceConsumerUnitTest(unittest.TestCase):
    def test_dynamic_endpoint_resolves_to_generated_operation(self):
        descriptors = [gate.OperationDescriptor("GET", "/adm/api/jobs/{jobId}", "admFindJob")]
        self.assertEqual("admFindJob", gate.operation_for("GET", "/adm/api/jobs/${jobId}?x=1", descriptors))

    def test_mutation_method_is_read_from_second_argument(self):
        source = 'await admMutation(`/adm/api/jobs/${jobId}`, "POST", body)'
        match = gate.CALL.search(source)
        self.assertIsNotNone(match)
        assert match
        self.assertEqual("POST", gate.method_for_call(match.group("prefix"), source, match.end()))

    def test_component_graph_follows_feature_api_helper(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp) / "src"
            page = root / "features/jobs/JobsPage.vue"
            api = root / "features/jobs/api.ts"
            page.parent.mkdir(parents=True)
            page.write_text('<script setup lang="ts">import { findJobs } from "./api"; findJobs();</script>', encoding="utf-8")
            api.write_text('import { admQuery } from "../../shared/cpfApi"; export const findJobs=()=>admQuery("/adm/api/jobs");', encoding="utf-8")
            (root / "shared").mkdir()
            (root / "shared/cpfApi.ts").write_text("export const admQuery=()=>null", encoding="utf-8")
            texts, missing = gate.crawl_component(page, root)
            self.assertEqual(set(), missing)
            self.assertIn(api.resolve(), texts)
            consumed, direct = gate.extract_consumed_operations(
                texts,
                [gate.OperationDescriptor("GET", "/adm/api/jobs", "admFindJobs")],
                {"admFindJobs"},
            )
            self.assertEqual({"admFindJobs"}, consumed)
            self.assertEqual(set(), direct)

    def test_direct_fetch_in_feature_graph_is_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "Page.vue"
            path.write_text('<script setup>fetch("/adm/api/jobs")</script>', encoding="utf-8")
            consumed, direct = gate.extract_consumed_operations(
                {path: path.read_text(encoding="utf-8")},
                [gate.OperationDescriptor("GET", "/adm/api/jobs", "admFindJobs")],
                {"admFindJobs"},
            )
            self.assertEqual({"admFindJobs"}, consumed)
            self.assertEqual({path}, direct)

    def test_expired_waiver_is_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            path = Path(temp) / "waivers.csv"
            path.write_text(
                "route_id,operation_id,owner,reason,approved_by,expires_on\n"
                "jobs,admFindJobs,team,temporary,tester,2000-01-01\n",
                encoding="utf-8",
            )
            with self.assertRaises(gate.ContractError):
                gate.read_waivers(path)

    def test_multiline_import_is_crawled(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            source_root = root / "src"
            feature = source_root / "feature"
            feature.mkdir(parents=True)
            page = feature / "Page.vue"
            api = feature / "api.ts"
            page.write_text(
                '<script setup lang="ts">\n'
                'import {\n  loadItems,\n  saveItem,\n} from "./api"\n'
                '</script>\n',
                encoding="utf-8",
            )
            api.write_text('export const loadItems = () => 1\n', encoding="utf-8")
            texts, missing = gate.crawl_component(page, source_root)
            self.assertFalse(missing)
            self.assertIn(api.resolve(), texts)

    def test_adjacent_post_does_not_reclassify_prior_get(self):
        source = (
            'function load(){ return request(`/adm/api/batch-runtime/views/${view}`, { credentials: "same-origin" }) }\n'
            'function mutate(){ return request("/adm/api/batch-runtime/commands", { method: "POST" }) }\n'
        )
        matches = list(gate.CALL.finditer(source))
        self.assertEqual(2, len(matches))
        self.assertEqual("GET", gate.method_for_call(matches[0].group("prefix"), source, matches[0].end()))
        self.assertEqual("POST", gate.method_for_call(matches[1].group("prefix"), source, matches[1].end()))

    def test_nested_generic_request_is_parsed(self):
        source = 'return request<Record<string, unknown>>("/adm/api/center-cut/jobs/ABC", { credentials: "same-origin" })'
        matches = list(gate.CALL.finditer(source))
        self.assertEqual(1, len(matches))
        self.assertEqual("/adm/api/center-cut/jobs/ABC", matches[0].group("url"))

    def test_route_specific_store_action_is_resolved(self):
        with tempfile.TemporaryDirectory() as temp:
            frontend = Path(temp) / "frontend"
            methods = frontend / "src/app/methods"
            methods.mkdir(parents=True)
            (methods / "jobs.ts").write_text(
                'export const jobMethods = {\n'
                '  async loadJobs() { return this.getJson("/adm/api/jobs"); },\n'
                '  async deleteJob() { return this.sendJson("/adm/api/jobs/${id}", "DELETE"); }\n'
                '}\n', encoding="utf-8")
            actions = gate.read_action_bodies(frontend)
            operations, clients, visited = gate.resolve_action_operations(
                {"loadJobs"}, actions,
                [
                    gate.OperationDescriptor("GET", "/adm/api/jobs", "admFindJobs"),
                    gate.OperationDescriptor("DELETE", "/adm/api/jobs/{id}", "admDeleteJob"),
                ],
                {"admFindJobs", "admDeleteJob"},
            )
            self.assertEqual({"admFindJobs"}, operations)
            self.assertEqual(set(), clients)
            self.assertEqual({"loadJobs"}, visited)

    def _write_global_bridge(self, frontend: Path) -> None:
        (frontend / "src/components").mkdir(parents=True)
        (frontend / "src/shared").mkdir(parents=True)
        (frontend / "src/App.vue").write_text(
            '<template><RouteOperationWorkbench :operation-ids="currentOperationIds" /></template>\n'
            '<script lang="ts">\n'
            'import RouteOperationWorkbench from "./components/RouteOperationWorkbench.vue";\n'
            'export default { computed: { currentOperationIds(): readonly any[] { '
            'return findCapabilityByRouteName(this.route.name)?.expectedOperationIds ?? []; } } };\n'
            '</script>\n', encoding="utf-8")
        (frontend / "src/components/RouteOperationWorkbench.vue").write_text(
            '<script setup lang="ts">\n'
            'const props=defineProps<{ title:string; operationIds:readonly CpfOperationId[] }>();\n'
            'const descriptors=props.operationIds.map(id=>cpfOperationDescriptors.find(item=>item.operationId===id));\n'
            'async function execute(){const descriptor=descriptors[0]; '
            'if(!window.confirm("run"))return; return admInvokeOperation(descriptor.operationId,{path,query,body});}\n'
            '</script><template><p role="alert">{{error}}</p><pre>{{JSON.stringify(result)}}</pre></template>',
            encoding="utf-8")
        (frontend / "src/shared/cpfApi.ts").write_text(
            'export async function admInvokeOperation<T = unknown>(operationId: CpfOperationId){\n'
            ' const descriptor=cpfOperationDescriptors.find(value=>value.operationId===operationId);\n'
            ' const relative=renderOperationPath(descriptor.template,{});\n'
            ' if(CLIENT_ACTOR_FIELDS.has(key))throw new Error("actor");\n'
            ' if(descriptor.method==="GET")return admQuery<T>(relative);\n'
            ' return admMutation<T>(relative,descriptor.method,undefined);\n'
            '}\n', encoding="utf-8")

    def test_valid_global_route_workbench_bridge(self):
        with tempfile.TemporaryDirectory() as temp:
            frontend = Path(temp) / "frontend"
            self._write_global_bridge(frontend)
            self.assertEqual([], gate.validate_global_route_workbench(frontend))

    def test_global_route_workbench_missing_route_projection_is_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            frontend = Path(temp) / "frontend"
            self._write_global_bridge(frontend)
            app = frontend / "src/App.vue"
            app.write_text(app.read_text(encoding="utf-8").replace("?.expectedOperationIds", "?.label"), encoding="utf-8")
            self.assertTrue(any("route operation projection" in item for item in gate.validate_global_route_workbench(frontend)))

    def test_global_route_workbench_missing_actor_boundary_is_rejected(self):
        with tempfile.TemporaryDirectory() as temp:
            frontend = Path(temp) / "frontend"
            self._write_global_bridge(frontend)
            api = frontend / "src/shared/cpfApi.ts"
            api.write_text(api.read_text(encoding="utf-8").replace("CLIENT_ACTOR_FIELDS.has(key)", "false"), encoding="utf-8")
            self.assertTrue(any("actor trust boundary" in item for item in gate.validate_global_route_workbench(frontend)))

    def test_store_action_delegation_is_followed(self):
        with tempfile.TemporaryDirectory() as temp:
            frontend = Path(temp) / "frontend"
            methods = frontend / "src/app/methods"
            methods.mkdir(parents=True)
            (methods / "jobs.ts").write_text(
                'export const jobMethods = {\n'
                '  async loadJobs() { return this.getJson("/adm/api/jobs"); },\n'
                '  async refreshJobs() { return this.loadJobs(); }\n'
                '}\n', encoding="utf-8")
            actions = gate.read_action_bodies(frontend)
            operations, _, visited = gate.resolve_action_operations(
                {"refreshJobs"}, actions,
                [gate.OperationDescriptor("GET", "/adm/api/jobs", "admFindJobs")],
                {"admFindJobs"},
            )
            self.assertEqual({"admFindJobs"}, operations)
            self.assertEqual({"loadJobs", "refreshJobs"}, visited)


if __name__ == "__main__":
    unittest.main()
