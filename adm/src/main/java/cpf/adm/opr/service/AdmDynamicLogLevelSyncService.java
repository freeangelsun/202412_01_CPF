package cpf.adm.opr.service;

import cpf.pfw.common.logging.DynamicLogLevelRule;
import cpf.pfw.common.logging.DynamicTransactionLogLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdmDynamicLogLevelSyncService {
    private static final Logger log = LoggerFactory.getLogger(AdmDynamicLogLevelSyncService.class);

    private final AdmDynamicLogLevelRuleStore ruleStore;
    private final DynamicTransactionLogLevelService runtimeService;

    public AdmDynamicLogLevelSyncService(
            AdmDynamicLogLevelRuleStore ruleStore,
            DynamicTransactionLogLevelService runtimeService) {
        this.ruleStore = ruleStore;
        this.runtimeService = runtimeService;
    }

    @Scheduled(
            fixedDelayString = "${cpf.adm.dynamic-log.sync-millis:5000}",
            initialDelayString = "${cpf.adm.dynamic-log.initial-sync-delay-millis:5000}")
    public void syncFromDatabase() {
        List<DynamicLogLevelRule> activeRules = ruleStore.findActiveRules();
        runtimeService.replaceAll(activeRules);
        if (!activeRules.isEmpty()) {
            log.debug("Dynamic log-level rules synchronized from DB. count={}", activeRules.size());
        }
    }
}
