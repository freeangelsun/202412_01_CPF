from pathlib import Path

SCRIPT = Path(__file__).parents[1] / "run-cpf-full-qa-validation.ps1"


def test_full_exact_head_runner_covers_entire_product_gate():
    text = SCRIPT.read_text(encoding="utf-8")
    for token in (
        "git fetch origin", "branch --show-current", "rev-parse origin/master", "status --porcelain",
        "Java 25 이상 필요", "PYTHON_GATE_TESTS", "SPLIT_MASTER_DATASET", "OWNER_BOUNDARIES",
        "SPRING_REQUEST_MAPPING_UNIQUENESS", "APPROVAL_STATE_MACHINE", "OPERATOR_TRUST", "INTERNAL_SERVICE_IDENTITY_BINDING", "BATCH_APPROVAL_TRUST_BOUNDARY", "BATCH_RUNTIME_COMMAND_VERSIONING", "RUNTIME_SNAPSHOT_VERSIONING", "DB_VENDOR_SEMANTIC", "BAT_OPERATION_LEDGER_LIFECYCLE",
        "GENERATOR_CANONICAL_CLOSURE", "CUSTOMER_PROVIDER_CONFORMANCE",
        "JAVA25_ROOT_CLEAN_TEST_ASSEMBLE", "cpf-admin/frontend", "cpf-biz-admin/frontend",
        "FreshInstall", "Upgrade", "RollbackReapply", "AUDIT_SPRING_MULTI_INSTANCE_KILL_RESTART",
        "FULL_QA_PRODUCT_PASS_71321", "requirementCount=30558", "scenarioCount=40763",
        "logicalItemCount=71321",
    ):
        assert token in text


def test_product_pass_gate_is_last_validation_step():
    text = SCRIPT.read_text(encoding="utf-8")
    product = text.index("FULL_QA_PRODUCT_PASS_71321")
    assert product > text.index("JAVA25_ROOT_CLEAN_TEST_ASSEMBLE")
    assert product > text.index("AUDIT_SPRING_MULTI_INSTANCE_KILL_RESTART")
    assert "--mode product-pass" in text[product:]


def test_runner_does_not_mutate_or_clean_repository():
    text = SCRIPT.read_text(encoding="utf-8")
    for forbidden in ("reset --hard", "restore .", "git clean", "git stash", "git checkout -", "git commit", "git push"):
        assert forbidden not in text.lower()
