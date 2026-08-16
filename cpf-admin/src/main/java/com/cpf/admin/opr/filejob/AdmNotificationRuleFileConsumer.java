package com.cpf.admin.opr.filejob;

import com.cpf.admin.opr.dto.AdmNotificationRuleRequest;
import com.cpf.admin.opr.service.AdmNotificationService;
import org.springframework.stereotype.Component;
import java.util.Map;

/** ADM_NOTIFICATION_RULE_IMPORT를 실제 Notification Owner Command에 연결합니다. */
@Component
public class AdmNotificationRuleFileConsumer implements AdmFileJobConsumer {
    private final AdmNotificationService service;
    public AdmNotificationRuleFileConsumer(AdmNotificationService service){this.service=service;}
    @Override
    public String templateCode(){return "ADM_NOTIFICATION_RULE_IMPORT";}

    @Override
    public ApplyResult apply(ApplyCommand command) {
        Map<String,String> values = command.values();
        try {
            var result=service.saveRule(null,new AdmNotificationRuleRequest(
                    required(values,"eventType"),blank(values,"eventSubType"),required(values,"channelCode"),
                    required(values,"templateCode"),required(values,"severity"),required(values,"receiverGroup"),
                    required(values,"useYn"),command.reason(),command.operatorId()),command.operatorId(),command.clientIp());
            return new ApplyResult(Long.toString(result.ruleId()),Long.toString(result.ruleId()),"알림 규칙 반영 완료");
        } catch (IllegalArgumentException error) {
            throw AdmFileJobDispatchException.notApplied(error.getMessage(), error);
        } catch (AdmFileJobDispatchException error) {
            throw error;
        } catch (RuntimeException error) {
            throw AdmFileJobDispatchException.unknown("알림 규칙 저장 결과를 확정할 수 없습니다.", error);
        }
    }

    @Override
    public void rollback(RollbackCommand command) {
        try {
            service.disableRule(Long.parseLong(command.rollbackToken()),command.reason(),command.operatorId(),command.clientIp());
        } catch (IllegalArgumentException error) {
            throw AdmFileJobDispatchException.notApplied(error.getMessage(), error);
        } catch (AdmFileJobDispatchException error) {
            throw error;
        } catch (RuntimeException error) {
            throw AdmFileJobDispatchException.unknown("알림 규칙 Rollback 결과를 확정할 수 없습니다.", error);
        }
    }
    private String required(Map<String,String> values,String key){
        String value=values.get(key);if(value==null||value.isBlank())throw new IllegalArgumentException(key+"는 필수입니다.");return value.trim();
    }
    private String blank(Map<String,String> values,String key){String v=values.get(key);return v==null||v.isBlank()?null:v.trim();}
}
