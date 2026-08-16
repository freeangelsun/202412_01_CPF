package com.cpf.admin.opr.service;

import com.cpf.batch.api.CpfBatchRiskCommand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/** ADM Approval Engine에서 승인된 BAT 위험조치를 실제 BAT Owner Port로 전달합니다. */
@Component
public final class AdmBatchApprovalCommandDispatcher implements AdmApprovalOwnerCommandDispatcher {
    private final AdmBatchOperationService batch;
    private final ObjectMapper objectMapper;

    public AdmBatchApprovalCommandDispatcher(AdmBatchOperationService batch, ObjectMapper objectMapper) {
        this.batch = batch;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(String ownerModule, String ownerCommand) {
        return "BAT".equalsIgnoreCase(ownerModule) && java.util.Set.of(
                "releaseLock", "actGhostExecution", "requestRetry", "requestStop",
                "updateScheduleEnabled", "requestRun", "runSchedulerOnce").contains(ownerCommand);
    }

    @Override
    public Object execute(Map<String,Object> approvalRequest) {
        Map<String,Object> snapshot = read(required(approvalRequest, "commandPayloadSnapshot"));
        CpfBatchRiskCommand command = new CpfBatchRiskCommand(
                text(snapshot,"operation"), text(snapshot,"targetType"), text(snapshot,"targetId"),
                text(snapshot,"actionType"), text(snapshot,"requestUser"), text(snapshot,"reason"),
                text(snapshot,"approvalRequestId"), text(snapshot,"idempotencyKey"),
                longOrNull(snapshot.get("expectedVersion")), nullable(snapshot.get("payload")));
        String ownerCommand = text(approvalRequest,"ownerCommand");
        if (!ownerCommand.equals(command.operation())) {
            throw new IllegalStateException("approved ownerCommand does not match command snapshot");
        }
        return switch (command.operation()) {
            case "releaseLock" -> batch.releaseLock(command.targetId(), command);
            case "actGhostExecution" -> batch.actGhostExecution(
                    Long.parseLong(command.targetId()), action(command.actionType()), command);
            case "requestRetry" -> batch.requestRetry(Long.parseLong(command.targetId()), command);
            case "requestStop" -> batch.requestStop(Long.parseLong(command.targetId()), command);
            case "updateScheduleEnabled" -> batch.updateScheduleEnabled(
                    command.targetId(), command.actionType().endsWith("ENABLE"), command);
            case "requestRun" -> batch.requestRun(command.targetId(), command.payload(), command);
            case "runSchedulerOnce" -> batch.runSchedulerOnce(command);
            default -> throw new IllegalArgumentException("unsupported approved BAT command: " + command.operation());
        };
    }

    private Map<String,Object> read(String value) {
        try {
            return objectMapper.readValue(value, new TypeReference<>() {});
        } catch (Exception invalid) {
            throw new IllegalArgumentException("approved command payload snapshot is invalid", invalid);
        }
    }
    private static String action(String value) {
        String upper=value.toUpperCase(Locale.ROOT);
        return upper.startsWith("BATCH_GHOST_")?upper.substring("BATCH_GHOST_".length()):upper;
    }
    private static String text(Map<String,Object> map,String key){return required(map,key);}
    private static String required(Map<String,Object> map,String key){Object value=value(map,key);if(value==null||String.valueOf(value).isBlank())throw new IllegalArgumentException(key+" is required");return String.valueOf(value).trim();}
    private static Object value(Map<String,Object> map,String key){Object value=map.get(key);if(value!=null)return value;value=map.get(key.toUpperCase(Locale.ROOT));if(value!=null)return value;String snake=key.replaceAll("([a-z])([A-Z])","$1_$2").toLowerCase(Locale.ROOT);value=map.get(snake);return value!=null?value:map.get(snake.toUpperCase(Locale.ROOT));}
    private static String nullable(Object value){return value==null?"":String.valueOf(value);}
    private static Long longOrNull(Object value){return value==null||String.valueOf(value).isBlank()?null:Long.valueOf(String.valueOf(value));}
}
