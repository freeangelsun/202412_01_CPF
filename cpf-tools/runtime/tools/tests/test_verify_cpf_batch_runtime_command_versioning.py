import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-batch-runtime-command-versioning.py'
def load():
    s=importlib.util.spec_from_file_location('v',SCRIPT); m=importlib.util.module_from_spec(s); s.loader.exec_module(m); return m
class T(unittest.TestCase):
    def fixture(self,secure=True):
        td=tempfile.TemporaryDirectory(); root=Path(td.name)
        paths={
          'cp':'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java',
          'rq':'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeCommandRequest.java',
          'ap':'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',
          'rc':'cpf-batch/api/src/main/java/com/cpf/batch/api/RuntimeCommand.java',
          'ba':'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/internal/JdbcRuntimeRegistry.java',
          'port':'cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/runtimecontrol/api/CpfManagedRuntimeRegistry.java',
          'provider':'cpf-starters/platform-operations/runtime-control/src/main/java/com/cpf/platform/operations/runtimecontrol/internal/CpfJdbcManagedRuntimeRegistry.java',
          'repo':'cpf-starters/platform-operations/runtime-control/src/main/java/com/cpf/platform/operations/runtimecontrol/internal/CpfRuntimeControlPlaneRepository.java'}
        p={k:root/v for k,v in paths.items()}
        for x in p.values(): x.parent.mkdir(parents=True,exist_ok=True)
        p['cp'].write_text('class C{void command(BatchRuntimeCommandRequest body){if(body.approvalRequestId==null)throw x;approvalService.execute(1L,body.reason,operatorId);}}')
        p['rq'].write_text('class BatchRuntimeCommandRequest{String approvalRequestId;String reason;}')
        p['ap'].write_text('class A{void executeRuntimeCommand(C c,R risk){if(risk.expectedVersion() == null)throw x;Map request=m();request.put("expectedVersion", risk.expectedVersion());}}')
        p['rc'].write_text('record RuntimeCommand(long expectedVersion){RuntimeCommand{if(expectedVersion<0)throw x;}}')
        p['port'].write_text('interface CpfManagedRuntimeRegistry{long updateDesiredState(String id,String state,long expectedVersion);}')
        p['provider'].write_text('class CpfJdbcManagedRuntimeRegistry{R repository;long x(String instanceId,String desiredState,long expectedVersion){return repository.updateManagedDesiredState(instanceId, desiredState, expectedVersion);}}')
        if secure:
            p['ba'].write_text('class JdbcRuntimeRegistry{private final CpfManagedRuntimeRegistry central;long updateDesiredState(String instanceId,DesiredState desired,long expectedVersion){return central.updateDesiredState(instanceId, desired.name(), expectedVersion);}}')
            p['repo'].write_text('class CpfRuntimeControlPlaneRepository{long updateManagedDesiredState(String instanceId,String desiredState,long expectedVersion){if(expectedVersion<0)throw x;int u=jdbc.update("UPDATE OPS_RUNTIME_INSTANCE_STATE SET control_row_version=control_row_version+1 WHERE instance_id=? AND control_row_version=?",desiredState,instanceId,expectedVersion);if(u!=1){S current=managedRuntimeSnapshot(instanceId);throw new CpfRuntimeVersionConflictException(expectedVersion,current.controlVersion());}return expectedVersion+1;}}')
        else:
            p['ba'].write_text('class JdbcRuntimeRegistry{private final CpfManagedRuntimeRegistry central;long updateDesiredState(String instanceId,DesiredState desired,long expectedVersion){return central.updateDesiredState(instanceId, desired.name(), 0);}}')
            p['repo'].write_text('class CpfRuntimeControlPlaneRepository{long updateManagedDesiredState(String instanceId,String desiredState,long expectedVersion){long current=read();return expectedVersion>0?expectedVersion:current;}}')
        return td,root
    def test_secure_chain_passes(self): td,r=self.fixture(); self.addCleanup(td.cleanup); self.assertEqual('PASS',load().verify(r)['status'])
    def test_version_fallback_or_drop_fails(self): td,r=self.fixture(False); self.addCleanup(td.cleanup); self.assertRaises(Exception,load().verify,r)
if __name__=='__main__': unittest.main()
