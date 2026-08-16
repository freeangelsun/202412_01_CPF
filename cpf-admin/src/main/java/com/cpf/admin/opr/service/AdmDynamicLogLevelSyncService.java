/* ADM/BZA 실제 Consumer가 CPF Framework Annotation을 사용하도록 currentize한다. */
package com.cpf.admin.opr.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import com.cpf.foundation.annotation.CpfService;

@CpfService
public class AdmDynamicLogLevelSyncService extends com.cpf.admin.common.base.AdmBaseService {
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
