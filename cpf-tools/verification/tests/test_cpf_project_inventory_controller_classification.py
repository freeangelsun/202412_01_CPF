from importlib.util import module_from_spec, spec_from_file_location
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[3]
MODULE_PATH = ROOT / 'cpf-tools/governance/tools/generate-cpf-project-inventory.py'
spec = spec_from_file_location('cpf_inventory_generator', MODULE_PATH)
mod = module_from_spec(spec)
sys.modules[spec.name] = mod
assert spec.loader is not None
spec.loader.exec_module(mod)

ANN = ['@RestController', '@Controller', '@CpfController']


def classify(source: str):
    return mod.java_type_controller_metadata(source, ANN)


def test_advice_and_comment_mentions_are_not_controllers():
    assert classify('''@RestControllerAdvice class X {}''')[0] is False
    assert classify('''@ControllerAdvice public class X {}''')[0] is False
    assert classify('''/** @CpfController contract */ public final class X {}''')[0] is False


def test_meta_annotation_definition_is_not_a_controller():
    assert classify('''@RestController public @interface CpfController {}''')[0] is False


def test_http_and_graphql_controllers_are_distinguished():
    assert classify('''@RestController public class X { @GetMapping("/x") void x(){} }''')[:2] == (True, 'http')
    result = classify('''@Controller public class X { @QueryMapping String x(){return "x";} }''')
    assert result[0] is True
    assert result[1] == 'graphql'
    assert result[2] == ['QueryMapping']
