from __future__ import annotations
import importlib.util,re,shutil,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-adm-e2e-contract.py"
spec=importlib.util.spec_from_file_location('adm_e2e_contract',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec.loader;spec.loader.exec_module(module)
class AdmE2eContractTest(unittest.TestCase):
    def test_real_overlay(self):
        root=Path(__file__).parents[4]
        result=module.validate(root)
        routes_file=root/"cpf-admin/frontend/src/app/routes.ts"
        source=routes_file.read_text(encoding="utf-8")
        routes_dir=routes_file.parent/"routes"
        if routes_dir.is_dir():
            source += "\n" + "\n".join(p.read_text(encoding="utf-8") for p in sorted(routes_dir.glob("*.ts")) if p.name != "types.ts")
        expected=len(module.ENTRY.findall(source))
        self.assertGreater(expected,0)
        self.assertEqual(expected,result['routes'])
    def test_missing_browser_rejected(self):
        root=Path(__file__).parents[4]
        with tempfile.TemporaryDirectory() as temp:
            target=Path(temp)
            # validate()가 실제로 읽는 계약 파일만 복사한다. 저장소 전체를 복사하면 node_modules
            # /build/Gradle project-cache 같은 생성물까지 끌고 오고, 이들은 실행 중 잠기거나
            # 바뀌기 때문에 계약과 무관한 copy 오류로 테스트가 실패한다.
            for relative in module.CONTRACT_SOURCES:
                source=root/relative
                if not source.exists(): continue
                destination=target/relative
                destination.parent.mkdir(parents=True,exist_ok=True)
                if source.is_dir(): shutil.copytree(source,destination)
                else: shutil.copy2(source,destination)
            p=target/'cpf-admin/frontend/playwright.config.ts';p.write_text(p.read_text(encoding="utf-8").replace('name: "webkit-desktop"','name: "webkit-disabled"'), encoding="utf-8")
            with self.assertRaises(module.ContractError):module.validate(target)
    def test_contract_sources_cover_every_validated_path(self):
        # CONTRACT_SOURCES 가 validate() 의 실제 읽기 경로보다 좁아지면, 복제 Tree 에서만
        # 파일이 없어져 계약 위반이 아닌 이유로 FAIL 한다. 두 목록의 드리프트를 차단한다.
        source=SCRIPT.read_text(encoding="utf-8")
        validated=set(re.findall(r'root / "([^"]+)"',source))
        self.assertTrue(validated)
        self.assertEqual(validated-set(module.CONTRACT_SOURCES),set())
        root=Path(__file__).parents[4]
        for relative in module.CONTRACT_SOURCES:
            self.assertTrue((root/relative).exists(),relative)
if __name__=='__main__':unittest.main()
