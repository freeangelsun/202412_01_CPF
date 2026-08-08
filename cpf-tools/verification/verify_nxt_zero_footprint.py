from pathlib import Path
import json,sys
root=Path(__file__).resolve().parents[2]; cat=json.loads((root/'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text())
optional={':cpf-starter-security-session-valkey',':cpf-starter-file-object-storage-s3',':cpf-starter-integration-graphql',':cpf-starter-messaging-schema-governance'}
profile_text='
'.join(str(v) for v in cat.get('profileDefinitions',{}).values())
errors=[p for p in optional if p in profile_text]
print('PASS NXT zero-footprint gate' if not errors else 'OPTIONAL_IN_DEFAULT_PROFILE:'+','.join(errors));sys.exit(1 if errors else 0)
