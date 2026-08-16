package com.cpf.batch.worker;

import com.cpf.file.api.filetransfer.CpfFileEndpoint;
import com.cpf.file.api.filetransfer.CpfFileRequest;
import com.cpf.file.api.filetransfer.CpfFileResult;
import com.cpf.file.api.filetransfer.CpfFileTransferClient;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovedFileExecutorRemoteTransferTest {

    @Test
    void approvedRemoteUploadUsesCatalogEndpointAndCredentialReference() throws Exception {
        Path root = Files.createTempDirectory("cpf-file-upload-");
        Files.writeString(root.resolve("sample.txt"), "cpf-remote-upload");
        WorkerOperationalProperties properties = properties(root);
        CapturingClient client = new CapturingClient(false);
        ApprovedFileExecutor executor = new ApprovedFileExecutor(properties);
        executor.setFileTransferClient(client);

        executor.transfer("LOCAL_OUT", "sample.txt", "SFTP_OUT", "daily/sample.txt",
                false, "tx-1", "seg-1", "worker-01");

        assertEquals("UPLOAD", client.request.operation());
        assertEquals("CPF-SFTP-01", client.endpoint.endpointCode());
        assertEquals("daily/sample.txt", client.request.remotePath().substring(
                client.request.remotePath().lastIndexOf("/daily/") + 1));
        assertEquals("credential-01", client.endpoint.credential().credentialId());
        assertTrue(client.request.fileSize() > 0);
    }

    @Test
    void approvedRemoteDownloadVerifiesChecksumAndCreatesLocalFile() throws Exception {
        Path root = Files.createTempDirectory("cpf-file-download-");
        WorkerOperationalProperties properties = properties(root);
        CapturingClient client = new CapturingClient(true);
        ApprovedFileExecutor executor = new ApprovedFileExecutor(properties);
        executor.setFileTransferClient(client);

        Path downloaded = executor.transfer("SFTP_OUT", "daily/result.txt", "LOCAL_OUT", "result.txt",
                true, "tx-2", "seg-2", "worker-02");

        assertEquals("DOWNLOAD", client.request.operation());
        assertTrue(Files.isRegularFile(downloaded));
        assertEquals("cpf-remote-download", Files.readString(downloaded));
    }

    private static WorkerOperationalProperties properties(Path root) {
        WorkerOperationalProperties properties = new WorkerOperationalProperties();
        WorkerOperationalProperties.PathAlias local = new WorkerOperationalProperties.PathAlias();
        local.setProvider("LOCAL");
        local.setRoot(root.toString());
        local.setAllowedExtensions(java.util.List.of("txt"));

        WorkerOperationalProperties.PathAlias remote = new WorkerOperationalProperties.PathAlias();
        remote.setProvider("SFTP");
        remote.setProtocol("SFTP");
        remote.setEndpointCode("CPF-SFTP-01");
        remote.setHost("sftp.internal.example");
        remote.setPort(22);
        remote.setRemoteBasePath("/cpf/batch");
        remote.setCredentialScope("BAT");
        remote.setCredentialId("credential-01");
        remote.setCredentialVersion("v1");
        Map<String, WorkerOperationalProperties.PathAlias> aliases = new LinkedHashMap<>();
        aliases.put("LOCAL_OUT", local);
        aliases.put("SFTP_OUT", remote);
        properties.setPathAliases(aliases);
        return properties;
    }

    private static final class CapturingClient implements CpfFileTransferClient {
        private final boolean download;
        private CpfFileEndpoint endpoint;
        private CpfFileRequest request;

        private CapturingClient(boolean download) {
            this.download = download;
        }

        @Override
        public CpfFileResult execute(CpfFileEndpoint endpoint, CpfFileRequest request) {
            this.endpoint = endpoint;
            this.request = request;
            try {
                String checksum = request.checksum();
                long size = request.fileSize();
                if (download) {
                    Path local = Path.of(request.localPath());
                    Files.createDirectories(local.getParent());
                    Files.writeString(local, "cpf-remote-download");
                    ApprovedFileExecutor.FileFingerprint fingerprint =
                            new ApprovedFileExecutor(new WorkerOperationalProperties()).fingerprint(local);
                    checksum = fingerprint.sha256();
                    size = fingerprint.size();
                }
                return new CpfFileResult("COMPLETED", endpoint.endpointCode(), request.localPath(),
                        request.remotePath(), checksum, size, Instant.now(), "OK");
            } catch (Exception failure) {
                throw new IllegalStateException(failure);
            }
        }
    }
}
