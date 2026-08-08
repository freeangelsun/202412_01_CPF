from pathlib import Path
import argparse,sys
ap=argparse.ArgumentParser(); ap.add_argument("--root",type=Path,default=Path(".")); ap.add_argument("--self-test",action="store_true"); args=ap.parse_args(); root=args.root.resolve()
spool=(root/'cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogRecoverySpool.java').read_text(encoding='utf-8')
writer=(root/'cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogWriter.java').read_text(encoding='utf-8')
errors=[]
if 'Files.writeString(target' in spool: errors.append('spool bypasses hardened writer')
if '8L * 1024 * 1024' in spool or '<= 8' in spool: errors.append('size-capped dedup remains')
if 'java.io.tmpdir' in spool: errors.append('tmp recovery root remains')
for token in ['scheduleWithFixedDelay','RecoveryAppender','blockedTargets','continue']:
    if token not in spool: errors.append('missing '+token)
for token in ['appendRecoveredRecord','ensureSafeWritableLogPath','acquireProcessFileLock','containsRecoveryMarker']:
    if token not in writer: errors.append('missing writer recovery hardening '+token)

test=(root/'cpf-core/src/test/java/com/cpf/core/common/logging/file/CpfFileLogRecoveryContractTest.java').read_text(encoding='utf-8')
for token in ['failedHeadDoesNotStarveHealthyTailFromDifferentTarget','failedTargetPreservesPerTargetOrderAcrossRepeatedReplay','accessDeniedHeadStillAllowsHealthyTailAndRemainsRetryable','durablePendingEntrySurvivesRestartAndReplaysWithoutNewWrite','concurrentReplayersCannotDuplicateRecoveredRecord','maxEntriesPressureFailsClosedWithoutDeletingExistingPendingEntries']:
    if token not in test: errors.append('missing filelog fault test '+token)

if args.self_test:
    mutated=spool.replace('continue','break',1)
    if 'continue' not in spool or mutated==spool: errors.append('mutation fixture invalid')
if errors:
    print('FAIL ' + '; '.join(errors)); sys.exit(1)
print('PASS filelog recovery hardened replay/dedup/durable retry contract')
