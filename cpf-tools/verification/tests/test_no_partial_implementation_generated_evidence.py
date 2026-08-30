from pathlib import Path
import subprocess, sys
ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/verification/verify_no_partial_implementation.py"

def test_generated_platform_evidence_python_environment_is_not_product_partial_implementation(tmp_path: Path):
    product = tmp_path / "cpf-core/src/main/java/com/cpf"; product.mkdir(parents=True)
    (product / "Good.java").write_text("package com.cpf; public class Good {}", encoding="utf-8")
    generated = tmp_path / "cpf-docs/governance/development-harness/evidence/platform/current/generated/python/open-git-release-venv/Lib/site-packages/vendor.py"; generated.parent.mkdir(parents=True)
    generated.write_text("# TODO vendor placeholder implementation\nraise NotImplementedError()", encoding="utf-8")
    cp=subprocess.run([sys.executable,str(SCRIPT),"--root",str(tmp_path)],text=True,encoding="utf-8",errors="replace",capture_output=True)
    assert cp.returncode==0, cp.stdout+cp.stderr

def test_product_partial_marker_still_fails_closed(tmp_path: Path):
    product=tmp_path/"cpf-core/src/main/java/com/cpf"; product.mkdir(parents=True)
    (product/"Bad.java").write_text("package com.cpf; // TODO placeholder implementation\npublic class Bad {}",encoding="utf-8")
    cp=subprocess.run([sys.executable,str(SCRIPT),"--root",str(tmp_path)],text=True,encoding="utf-8",errors="replace",capture_output=True)
    assert cp.returncode!=0 and "PARTIAL_MARKER:" in cp.stdout
