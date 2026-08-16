from pathlib import Path
import json
import sys

root = Path(__file__).resolve().parents[2]
cat = json.loads((root / "cpf-tools/generator/contracts/cpf-starter-catalog.json").read_text(encoding="utf-8"))
optional = {
    ":starters:security:session:valkey",
    ":starters:file:object-storage:s3",
    ":starters:integration:graphql",
    ":cpf-starter-messaging-schema-governance",
}
profile_text = "\n".join(str(v) for v in cat.get("profileDefinitions", {}).values())
errors = sorted(p for p in optional if p in profile_text)
if errors:
    print("OPTIONAL_IN_DEFAULT_PROFILE:" + ",".join(errors))
    sys.exit(1)
print("PASS NXT zero-footprint gate")
