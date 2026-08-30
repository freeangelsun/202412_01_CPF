from pathlib import Path
import importlib.util


SCRIPT = Path(__file__).parents[2] / "tools/verify-cpf-public-javadoc-quality.py"
SPEC = importlib.util.spec_from_file_location("public_javadoc_quality", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def test_type_javadoc_survives_multiline_annotations():
    source = """/** Configures the supported CPF domain datasource contract. */
@AutoConfiguration(
    beforeName = "example.DataSourceAutoConfiguration")
@ConditionalOnProperty(
    prefix = "cpf.domain.persistence",
    name = "enabled")
public class CpfDomainDataSourceAutoConfiguration {}
"""
    declaration = source.index("public class")
    assert "supported CPF domain datasource" in MODULE._doc_for(source, declaration)


def test_type_javadoc_does_not_cross_an_executable_statement():
    source = """/** Documentation for a different type. */
int value = 1;
public class Undocumented {}
"""
    assert MODULE._doc_for(source, source.index("public class")) is None
