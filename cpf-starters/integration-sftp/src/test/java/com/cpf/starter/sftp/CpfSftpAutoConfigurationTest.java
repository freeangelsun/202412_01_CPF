package com.cpf.starter.sftp;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CpfSftpAutoConfigurationTest {

    @Test
    void endpointIsMaskedForHealthOutput() {
        assertThat(CpfSftpAutoConfiguration.maskEndpoint("sftp.internal.example", 22))
                .isEqualTo("s***:22");
        assertThat(CpfSftpAutoConfiguration.maskEndpoint(null, 2022))
                .isEqualTo("***:2022");
    }
}
