#!/usr/bin/env python3
"""Fail-closed static closure gate for the CPF fixed-length public capability."""
from __future__ import annotations
import argparse, json
from pathlib import Path


def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root', default='.'); args=ap.parse_args()
    root=Path(args.root).resolve(); findings=[]; checks=[]
    def check(name: str, ok: bool, detail: str=''):
        checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
        if not ok: findings.append(f'{name}: {detail}')
    def text(rel: str) -> str:
        p=root/rel
        if not p.is_file():
            return ''
        return p.read_text(encoding='utf-8-sig',errors='ignore')

    catalog=text('cpf-tools/generator/contracts/cpf-starter-catalog.json')
    check('CATALOG_OWNER', all(t in catalog for t in (
        '"artifactId": "cpf-starter-integration-fixed-length"',
        '"projectPath": ":starters:integration:fixed-length"',
        '"ownerPath": "cpf-starters/integration/fixed-length"')),
        'canonical fixed-length module/owner missing')

    api_files=[
        'CpfFixedLengthOperations.java','CpfFixedLengthParser.java','CpfFixedLengthWriter.java',
        'CpfFixedLengthLayoutRegistry.java','CpfFixedLengthLogDecoder.java','CpfFixedLengthDtoMapper.java'
    ]
    api_root=root/'cpf-starters/integration/fixed-length/src/main/java/com/cpf/integration/fixedlength/api'
    missing=[name for name in api_files if not (api_root/name).is_file()]
    check('PUBLIC_API_SURFACE', not missing, ','.join(missing))

    auto=text('cpf-starters/integration/fixed-length/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports')
    check('AUTOCONFIG', 'CpfFixedLengthAutoConfiguration' in auto and 'CpfFixedLengthRuntimeControlAutoConfiguration' in auto,
          'runtime/autoconfiguration imports incomplete')

    byte_test=text('cpf-starters/integration/fixed-length/src/test/java/com/cpf/integration/fixedlength/CpfFixedLengthCodecByteContractTest.java')
    check('BYTE_CONTRACT_TEST', all(t in byte_test for t in (
        'encodesMultibyteValueUsingByteOffsetsAndExactRecordLength',
        'rejectsByteOverflowUnknownFieldAndTruncatedMultibyteInput',
        'rejectsDuplicateFieldNamesAndMultibytePad', 'preservesDefensiveExactBytes')),
        'multibyte/overflow/malformed/padding coverage missing')

    operations_test=text('cpf-starters/integration/fixed-length/src/test/java/com/cpf/integration/fixedlength/CpfDefaultFixedLengthOperationsTest.java')
    check('MASKING_CONTRACT_TEST', 'maskedFields()' in operations_test and '"***"' in operations_test,
          'masked parse/write evidence missing')

    runtime_test=text('cpf-starters/integration/fixed-length/src/test/java/com/cpf/integration/fixedlength/runtimecontrol/CpfFixedLengthRuntimeControlTest.java')
    check('RUNTIME_LAYOUT_TEST', all(t in runtime_test for t in ('layoutId','version','UTF-8','registry.require')),
          'runtime layout/version registry coverage missing')

    decoder=text('cpf-starters/integration/fixed-length/src/main/java/com/cpf/integration/fixedlength/api/CpfFixedLengthLogDecoder.java')
    adm=text('cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmLogQueryService.java')
    check('MASKED_LOG_DECODER', 'maskedFields()' in decoder and 'maskedGroups()' in decoder and
          'CpfFixedLengthLogDecoder' in adm and 'layoutResolved' in adm and 'AdmLogSanitizer.sanitizeStructure' in adm,
          'ADM masked fixed-length log consumer incomplete')

    frontend=text('cpf-admin/frontend/src/features/logs/LogsPage.vue') + '\n' + text('cpf-admin/frontend/src/features/core/methods.ts')
    check('ADM_FRONTEND_CONSUMER', 'fixedLengthDetails' in frontend and '전문' in frontend,
          'ADM fixed-length detail consumer missing')

    edu_controller=text('cpf-education/src/main/java/com/cpf/education/integration/telegram/controller/EducationTelegramEducationController.java')
    edu_dto=text('cpf-education/src/main/java/com/cpf/education/integration/telegram/dto/EducationFixedLengthEducationTelegram.java')
    check('EDUCATION_CONSUMER', all(t in edu_controller for t in ('fixed-length/parse','fixed-length/write','CpfFixedLengthDtoMapper'))
          and '@CpfFixedLengthField' in edu_dto, 'EDU parse/write DTO consumer missing')

    build_admin=text('cpf-admin/build.gradle'); build_edu=text('cpf-education/build.gradle')
    check('ACTUAL_MODULE_CONSUMERS', "project(':starters:integration:fixed-length')" in build_admin and
          "project(':starters:integration:fixed-length')" in build_edu,
          'ADM/EDU dependency consumer missing')

    result={'status':'PASS' if not findings else 'FAIL','checks':checks,'failureCount':len(findings),'findings':findings}
    print(json.dumps(result,ensure_ascii=False,indent=2))
    return 0 if not findings else 1

if __name__=='__main__':
    raise SystemExit(main())
