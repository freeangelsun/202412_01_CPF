package com.cpf.starter.sftp.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.common.filetransfer.CpfDuplicatePreventionPort;
import com.cpf.core.common.filetransfer.CpfFileInspectionPort;
import com.cpf.core.common.filetransfer.CpfFileTransferEngine;
import com.cpf.core.common.filetransfer.CpfFileTransferHistoryPort;
import com.cpf.core.common.filetransfer.CpfFileTransferPort;
import com.cpf.core.common.filetransfer.CpfFileTransferRuntimeState;
import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CpfSftpRuntimeControlAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfFileTransferRuntimeState fileTransferRuntimeState() {
        return new CpfFileTransferRuntimeState();
    }

    @Bean
    @ConditionalOnBean({
        CpfFileTransferPort.class,
        CpfFileTransferHistoryPort.class,
        CpfDuplicatePreventionPort.class
    })
    @ConditionalOnMissingBean
    CpfFileTransferEngine fileTransferEngine(
            CpfFileTransferPort transfer,
            CpfFileTransferHistoryPort history,
            CpfDuplicatePreventionPort duplicatePrevention,
            ObjectProvider<CpfReconciliationPort> reconciliation,
            CpfFileTransferRuntimeState runtimeState,
            ObjectProvider<CpfFileInspectionPort> inspection) {
        return new CpfFileTransferEngine(
                transfer,
                history,
                duplicatePrevention,
                reconciliation.getIfAvailable(),
                runtimeState,
                inspection.getIfAvailable());
    }

    @Bean(name = "cpfFilePolicyRuntimeApplier")
    @ConditionalOnBean(CpfFileTransferEngine.class)
    @ConditionalOnMissingBean(name = "cpfFilePolicyRuntimeApplier")
    CpfRuntimeChangeApplier filePolicyRuntimeApplier(
            CpfFileTransferRuntimeState runtimeState) {
        return new CpfFilePolicyRuntimeApplier(runtimeState);
    }

    @Bean(name = "cpfSftpTransferRuntimeApplier")
    @ConditionalOnBean(CpfFileTransferEngine.class)
    @ConditionalOnMissingBean(name = "cpfSftpTransferRuntimeApplier")
    CpfRuntimeChangeApplier sftpTransferRuntimeApplier(
            CpfFileTransferRuntimeState runtimeState) {
        return new CpfSftpTransferRuntimeApplier(runtimeState);
    }
}
