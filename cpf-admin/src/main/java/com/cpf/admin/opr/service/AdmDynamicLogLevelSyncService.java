package cpf.adm.opr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AdmDynamicLogLevelSyncService extends cpf.adm.common.base.AdmBaseService {
    private static final Logger log = LoggerFactory.getLogger(AdmDynamicLogLevelSyncService.class);

    private final AdmDynamicLogLevelBroadcastService broadcastService;

    public AdmDynamicLogLevelSyncService(AdmDynamicLogLevelBroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @Scheduled(
            fixedDelayString = "${cpf.adm.dynamic-log.sync-millis:5000}",
            initialDelayString = "${cpf.adm.dynamic-log.initial-sync-delay-millis:5000}")
    public void syncFromDatabase() {
        broadcastService.syncFromDatabase("scheduled");
        log.trace("Dynamic log-level scheduled synchronization completed.");
    }
}
