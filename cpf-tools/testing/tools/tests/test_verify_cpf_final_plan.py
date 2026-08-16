from __future__ import annotations
import importlib.util, unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-final-plan.py"
spec=importlib.util.spec_from_file_location("final_plan",SCRIPT);module=importlib.util.module_from_spec(spec);assert spec and spec.loader;spec.loader.exec_module(module)

class FinalPlanNegativeTest(unittest.TestCase):
    def base(self):
        return {"orderedStages":["source","java"],"commands":[
            {"id":"a","stage":"source","runner":"python","path":"a.py","args":[],"required":True},
            {"id":"b","stage":"java","runner":"gradle","path":"gradlew.bat","args":[],"required":True},
        ]}
    def test_duplicate_command_rejected(self):
        data=self.base();data["commands"][1]["id"]="a"
        with self.assertRaises(module.PlanError): module.validate(Path("."),data,False)
    def test_stage_regression_rejected(self):
        data=self.base();data["commands"].reverse()
        with self.assertRaises(module.PlanError): module.validate(Path("."),data,False)
    def test_missing_stage_rejected(self):
        data=self.base();data["commands"]=data["commands"][:1]
        with self.assertRaises(module.PlanError): module.validate(Path("."),data,False)
    def test_optional_command_cannot_false_green(self):
        data=self.base();data["commands"][0]["required"]=False
        with self.assertRaises(module.PlanError): module.validate(Path("."),data,False)
    def test_unknown_placeholder_rejected(self):
        data=self.base();data["commands"][0]["args"]=["{unknown}"]
        with self.assertRaises(module.PlanError): module.validate(Path("."),data,False)

    def test_known_command_missing_required_flag_rejected(self):
        data=self.base()
        data["commands"][0].update({"path":"cpf-tools/verification/openapi/verify-cpf-openapi-controller-coverage.py","args":["--root","{root}"]})
        with self.assertRaises(module.PlanError):module.validate(Path("."),data,False)
if __name__=="__main__": unittest.main()
