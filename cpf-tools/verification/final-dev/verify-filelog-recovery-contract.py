from pathlib import Path
import sys
root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
spool=(root/'cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogRecoverySpool.java').read_text(encoding='utf-8')
writer=(root/'cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogWriter.java').read_text(encoding='utf-8')
errors=[]
if 'Files.writeString(target' in spool: errors.append('spool bypasses hardened writer')
if '8L * 1024 * 1024' in spool or '<= 8' in spool: errors.append('size-capped dedup remains')
if 'java.io.tmpdir' in spool: errors.append('tmp recovery root remains')
for token in ['scheduleWithFixedDelay','RecoveryAppender']:
    if token not in spool: errors.append('missing '+token)
for token in ['appendRecoveredRecord','ensureSafeWritableLogPath','acquireProcessFileLock','containsRecoveryMarker']:
    if token not in writer: errors.append('missing writer recovery hardening '+token)
if errors:
    print('FAIL ' + '; '.join(errors)); sys.exit(1)
print('PASS filelog recovery hardened replay/dedup/durable retry contract')
