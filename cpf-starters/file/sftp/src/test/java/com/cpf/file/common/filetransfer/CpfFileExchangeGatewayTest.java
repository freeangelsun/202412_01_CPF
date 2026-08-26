package com.cpf.file.common.filetransfer;

import com.cpf.file.api.filetransfer.CpfRemoteCommandPlan;
import com.cpf.foundation.api.DefaultCpfTransactionIdGenerator;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import com.cpf.file.sftp.CpfSftpClient;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfFileExchangeGatewayTest {

    @TempDir
    Path tempDir;

    @Test
    void 로컬_helper는_허용경로_안에서만_읽기쓰기와_목록조회를_수행한다() {
        CpfFileExchangeGateway gateway = gateway();

        Path written = gateway.writeText("edu/sample.txt", "CPF", "tester");

        assertThat(written).startsWith(tempDir);
        assertThat(gateway.readText("edu/sample.txt", "tester")).isEqualTo("CPF");
        assertThat(gateway.list("edu", "tester")).containsExactly("edu/sample.txt");
        assertThat(gateway.findRecentHistory()).hasSize(3);
        assertThatThrownBy(() -> gateway.readText("../outside.txt", "tester"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 원격전송은_adapter가_없으면_fail_closed하고_명령계획은_실행되지_않는다() {
        CpfFileExchangeGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.transfer(
                "SFTP",
                "UPLOAD",
                "localhost",
                22,
                "CPF_SFTP_CREDENTIAL",
                "edu/sample.txt",
                "/data/sample.txt",
                "tester"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SFTP");
        CpfRemoteCommandPlan command = gateway.planRemoteCommand(
                "localhost",
                22,
                "appuser",
                "hostname",
                "tester");

        assertThat(command.accepted()).isTrue();
        assertThat(command.executed()).isFalse();
        assertThat(gateway.findRecentHistory()).hasSize(2);
    }

    private CpfFileExchangeGateway gateway() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.filetransfer.base-dir", tempDir.toString());
        return new CpfFileExchangeGateway(environment, new StaticListableBeanFactory().getBeanProvider(CpfSftpClient.class), new DefaultCpfTransactionIdGenerator("FLE", Clock.systemUTC()));
    }
}
