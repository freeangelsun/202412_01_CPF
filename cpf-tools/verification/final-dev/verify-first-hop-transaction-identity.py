#!/usr/bin/env python3
from pathlib import Path
import argparse,re,sys
ap=argparse.ArgumentParser(); ap.add_argument("--root",type=Path,default=Path(".")); ap.add_argument("--self-test",action="store_true"); args=ap.parse_args(); root=args.root.resolve()
idf=(root/'cpf-core/src/main/java/com/cpf/core/common/filter/CpfAuthenticatedSystemIdentityFilter.java').read_text(encoding='utf-8')
txf=(root/'cpf-core/src/main/java/com/cpf/core/common/filter/TransactionContextFilter.java').read_text(encoding='utf-8')
errs=[]
if 'Ordered.HIGHEST_PRECEDENCE + 10' not in idf:errs.append('identity filter must run at +10')
if 'Ordered.HIGHEST_PRECEDENCE + 20' not in txf:errs.append('transaction policy filter must run after identity at +20')
if 'AUTHENTICATED_SYSTEM_CODE_ATTRIBUTE' not in idf or 'TRUSTED_CONTEXT_ATTRIBUTE' not in idf:errs.append('server-authenticated attributes missing')
# The producer must not derive identity from caller-controlled CPF headers.
for token in ('CALLER_SERVICE','CHANNEL_CODE','ORIGINAL_CHANNEL_CODE','TRANSACTION_ID'):
 if token in idf:errs.append('identity producer trusts raw CPF header token '+token)
if 'getUserPrincipal()' not in idf or 'X509Certificate' not in idf:errs.append('principal/mTLS producer path missing')

test=root/'cpf-core/src/test/java/com/cpf/core/common/filter/CpfAuthenticatedSystemIdentityFilterIntegrationTest.java'
if not test.is_file():errs.append('filter-chain integration test missing')
else:
 tt=test.read_text(encoding='utf-8')
 for token in ('authenticatedChannelStartPreservesOfficialTransactionIdThroughConsumer','sameOfficialRequestRetryKeepsTransactionIdAndDoesNotCreateSecondClaim','authenticatedInternalPropagationPreservesOriginTransactionIdEvenWhenCallerDiffers','rawClientIdentityHeadersCannotSpoofOfficialStarter','alteredOriginSystemIsRejectedAfterServerAuthentication'):
  if token not in tt:errs.append('filter-chain integration scenario missing: '+token)

if args.self_test:
    mutated=idf.replace('Ordered.HIGHEST_PRECEDENCE + 10','Ordered.HIGHEST_PRECEDENCE + 30')
    if 'Ordered.HIGHEST_PRECEDENCE + 10' in mutated: errs.append('mutation did not alter precedence')
if errs:print('FAIL first-hop identity wiring\n'+'\n'.join(errs));raise SystemExit(1)
print('PASS first-hop identity producer precedes transaction policy and ignores raw CPF identity headers')
