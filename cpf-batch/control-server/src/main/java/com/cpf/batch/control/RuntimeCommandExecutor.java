package com.cpf.batch.control;

import com.cpf.batch.api.*;
import com.cpf.batch.control.deploy.RuntimeLifecycleService;
import com.cpf.batch.control.internal.*;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RuntimeCommandExecutor {
    private static final Set<String> RISKY=Set.of("START","STOP","RESTART","DRAIN","RESUME","ROLLBACK");
    private final JdbcRuntimeCommandRepository commands; private final JdbcRuntimeRegistry registry; private final RuntimeLifecycleService lifecycle;
    public RuntimeCommandExecutor(JdbcRuntimeCommandRepository commands,JdbcRuntimeRegistry registry,RuntimeLifecycleService lifecycle){this.commands=commands;this.registry=registry;this.lifecycle=lifecycle;}

    public Map<String,Object> execute(RuntimeCommand command){
        Map<String,Object> persisted=commands.create(command);
        if(!command.commandId().equals(String.valueOf(persisted.get("command_id")))) return persisted;
        String type=command.commandType().toUpperCase(Locale.ROOT);
        if(!RISKY.contains(type)) return fail(command,"VALIDATION","Unsupported command");
        if(command.approvalRequestId()==null||command.approvalRequestId().isBlank()||
           command.approvalPolicyVersion()==null||command.approvalPolicyVersion().isBlank())
            return fail(command,"APPROVAL","Approved command reference/policy is required");
        if(command.approvedBy()==null||command.approvedBy().isBlank()||command.approvedBy().equals(command.requestedBy()))
            return fail(command,"APPROVAL","Requester/approver separation is required");
        if(command.expiresAt()!=null&&command.expiresAt().isBefore(java.time.Instant.now()))
            return fail(command,"EXPIRY","Approved command expired");
        if(!commands.beginExecution(command.commandId()))
            return commands.find(command.idempotencyKey()).orElseThrow();
        boolean unknown=false,failed=false;StringBuilder summary=new StringBuilder();int attempt=0;
        for(String target:command.targetIds()){
            attempt++;
            try {
                Map<String,Object> before=registry.snapshot(target);
                DesiredState desired=desired(type);
                if(desired!=null) registry.updateDesiredState(target,desired,command.expectedVersion());
                AgentCommandResult result=lifecycle.operate(
                        target,
                        type,
                        command.requestedBy(),
                        command.approvedBy(),
                        command.approvalRequestId(),
                        command.reason());
                commands.recordAttempt(command.commandId(),attempt,target,"AGENT_"+type,result.state(),result.message());
                if(result.state()==CommandState.SUCCEEDED && "ROLLBACK".equals(type)) registry.updateDesiredState(target,DesiredState.RUNNING,0L);
                summary.append(target).append('=').append(result.state()).append(';');
                unknown|=result.state()==CommandState.UNKNOWN_RESULT; failed|=result.state()==CommandState.FAILED;
            } catch(RuntimeException e){
                commands.recordAttempt(command.commandId(),attempt,target,"CONTROL_DISPATCH",CommandState.UNKNOWN_RESULT,e.getClass().getSimpleName());
                summary.append(target).append("=UNKNOWN_RESULT;");unknown=true;
            }
        }
        CommandState state=unknown?CommandState.UNKNOWN_RESULT:(failed?CommandState.FAILED:CommandState.SUCCEEDED);
        commands.transition(command.commandId(),state,unknown?"OWNER_API_DISPATCH":(failed?"AGENT_EXECUTION":null),summary.toString());
        return commands.find(command.idempotencyKey()).orElseThrow();
    }

    private Map<String,Object> fail(RuntimeCommand c,String stage,String message){commands.transition(c.commandId(),CommandState.FAILED,stage,message);return commands.find(c.idempotencyKey()).orElseThrow();}
    private DesiredState desired(String type){return switch(type){case "START","RESTART","RESUME"->DesiredState.RUNNING;case "STOP"->DesiredState.STOPPED;case "DRAIN"->DesiredState.DRAINING;case "ROLLBACK"->DesiredState.ROLLING_BACK;default->null;};}
}
