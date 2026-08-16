#!/usr/bin/env python3
from pathlib import Path
import argparse

ap=argparse.ArgumentParser()
ap.add_argument('positional_root',nargs='?',default=None)
ap.add_argument('--root',dest='root_opt',default=None)
ns=ap.parse_args()
root=Path(ns.root_opt or ns.positional_root or '.').resolve()
fail=[]

# 과거 cpf-common.validation은 Owner가 아니며 잔존 Java Source가 없어야 한다.
legacy_validation=root/'cpf-common/src/main/java/com/cpf/common/validation'
if legacy_validation.exists():
    java=list(legacy_validation.rglob('*.java'))
    if java:
        fail.extend('COMMON_VALIDATION_FORBIDDEN:'+str(p.relative_to(root)) for p in java)

# 표준 HTTP 거래 Header DTO는 Web capability가 소유하고 Jakarta Bean Validation을 사용한다.
dto=root/'cpf-starters/web/src/main/java/com/cpf/web/dto/HeaderDTO.java'
if not dto.is_file():
    fail.append('HEADER_DTO_MISSING')
else:
    text=dto.read_text(encoding='utf-8',errors='ignore')
    for token in ['jakarta.validation.constraints.NotEmpty','jakarta.validation.constraints.NotNull',
                  'transactionId','initialChannelCode','channelCode','timestamp']:
        if token not in text:
            fail.append('HEADER_DTO_VALIDATION_MISSING:'+token)

# HeaderValidator 같은 별도 Legacy validator를 다시 만들지 않는다.
legacy_validator_hits=[p for p in root.rglob('HeaderValidator.java')
                       if 'build' not in p.parts and '.gradle' not in p.parts]
if legacy_validator_hits:
    fail.extend('LEGACY_HEADER_VALIDATOR_FORBIDDEN:'+str(p.relative_to(root)) for p in legacy_validator_hits)

if fail:
    print('CPF_COMMON_VALIDATION_OWNER=FAIL')
    print('\n'.join(fail))
    sys.exit(1)
print('CPF_COMMON_VALIDATION_OWNER=PASS legacyValidationPackage=0 headerOwner=cpf-starters/web beanValidation=present')
