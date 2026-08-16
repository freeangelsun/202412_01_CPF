#!/usr/bin/env python3
from pathlib import Path
import re,sys
ROOT=Path(__file__).resolve().parents[2];fail=[]
ann=ROOT/'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfLogging.java'
mode=ROOT/'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfLogMode.java'
aspect=ROOT/'cpf-starters/base/runtime/src/main/java/com/cpf/starter/runtime/CpfLoggingAspect.java'
for p in [ann,mode,aspect]:
    if not p.is_file():fail.append('MISSING:'+p.relative_to(ROOT).as_posix())
if ann.is_file():
    s=ann.read_text(encoding='utf-8')
    for token in ['CpfLogMode.SUMMARY','boolean includeArguments() default false','boolean includeResult() default false','String[] allowlist() default {}','String[] resultAllowlist() default {}']:
        if token not in s:fail.append('LOGGING_SAFE_DEFAULT_MISSING:'+token)
if mode.is_file():
    s=mode.read_text(encoding='utf-8')
    for token in ['NONE','SUMMARY','ENTRY_EXIT']:
        if token not in s:fail.append('LOGGING_MODE_MISSING:'+token)
if aspect.is_file():
    s=aspect.read_text(encoding='utf-8')
    for token in ['@annotation(com.cpf.foundation.annotation.CpfLogging)','CpfLogMasking.mask','CpfContexts.currentTransactionId()','CpfContexts.currentExecutionId()','CPF ERROR operation=']:
        if token not in s:fail.append('LOGGING_ASPECT_GUARD_MISSING:'+token)
    if re.search(r'@Around\("execution\(',s):fail.append('LOGGING_GLOBAL_METHOD_POINTCUT_FORBIDDEN')
# No competing public logging annotation definition.
for p in ROOT.rglob('CpfLogging.java'):
    if p!=ann:fail.append('DUPLICATE_CPF_LOGGING_ANNOTATION:'+p.relative_to(ROOT).as_posix())
fail=sorted(set(fail));print('CPF_LOGGING_DX='+('PASS' if not fail else 'FAIL'));print('failures='+str(len(fail)));[print(x) for x in fail];sys.exit(0 if not fail else 1)
