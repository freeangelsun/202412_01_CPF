package com.cpf.file.sftp;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CpfSftpPropertiesTest {

    @Test
    void rawPasswordIsRejected() {
        CpfSftpProperties properties = base();
        properties.setPassword("raw");

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Raw");
    }

    @Test
    void approvedSecretReferenceIsAccepted() {
        CpfSftpProperties properties = base();
        properties.setPasswordSecret("vault:cpf/sftp/password");

        assertThatCode(properties::validate).doesNotThrowAnyException();
    }

    @Test
    void durableLedgerCannotBeDisabled() {
        CpfSftpProperties properties = base();
        properties.setPasswordSecret("vault:cpf/sftp/password");
        properties.setLedgerRequired(false);

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ledger");
    }

    private CpfSftpProperties base() {
        CpfSftpProperties properties = new CpfSftpProperties();
        properties.setEnabled(true);
        properties.setHost("sftp.local");
        properties.setUsername("cpf");
        properties.setLocalRoot(".");
        properties.setRemoteRoot("/exchange");
        return properties;
    }
}
