package com.cpf.batch.agent.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.batch.agent.AgentProperties;
import com.cpf.batch.api.AgentArtifactRequest;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Artifact 다운로드, 서명, 상태 원장, Rollback 재검증을 검증합니다. */
class ArtifactInstallerSecurityTest {
    @TempDir Path temp;

    @Test
    void installsSignedArtifactsRollsBackAndRejectsTamperedState() throws Exception {
        byte[] version1 = "cpf-artifact-v1".getBytes(StandardCharsets.UTF_8);
        byte[] version2 = "cpf-artifact-v2".getBytes(StandardCharsets.UTF_8);
        KeyPair pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        Path publicKey = writePublicKey(pair);
        HttpServer server = artifactServer(Map.of("1.0.0", version1, "2.0.0", version2));
        server.start();
        try {
            AgentProperties properties = properties(server, publicKey);
            ArtifactVerifier verifier = new ArtifactVerifier(properties);
            ArtifactStateStore stateStore = new ArtifactStateStore(properties);
            ArtifactInstaller installer = new ArtifactInstaller(properties, verifier, stateStore);

            assertThat(installer.install(request("1.0.0", 1, version1, pair)).version()).isEqualTo("1.0.0");
            assertThat(installer.install(request("2.0.0", 2, version2, pair)).version()).isEqualTo("2.0.0");
            assertThat(installer.rollback("demo")).isEqualTo("1.0.0");

            Path state = temp.resolve("install/artifact-state.properties");
            Files.writeString(state, Files.readString(state).replace("version=1.0.0", "version=9.9.9"));
            assertThatThrownBy(() -> stateStore.read(state, true)).isInstanceOf(SecurityException.class);
        } finally {
            server.stop(0);
        }
    }

    private Path writePublicKey(KeyPair pair) throws Exception {
        Path target = temp.resolve("trusted.pem");
        String pem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(pair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n";
        Files.writeString(target, pem, StandardCharsets.US_ASCII);
        return target;
    }

    private HttpServer artifactServer(Map<String, byte[]> content) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/repo/com/cpf/demo/demo/", exchange -> {
            try {
                String[] segments = exchange.getRequestURI().getPath().split("/");
                byte[] body = content.get(segments[segments.length - 2]);
                if (body == null) {
                    exchange.sendResponseHeaders(404, -1);
                    return;
                }
                String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
                exchange.getResponseHeaders().add("Content-Type", "application/java-archive");
                exchange.getResponseHeaders().add("X-Checksum-Sha256", digest);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
            } catch (Exception failure) {
                exchange.sendResponseHeaders(500, -1);
            } finally {
                exchange.close();
            }
        });
        return server;
    }

    private AgentProperties properties(HttpServer server, Path publicKey) {
        AgentProperties properties = new AgentProperties();
        properties.setArtifactRepositoryBaseUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/repo");
        properties.setArtifactAllowedHosts(List.of("127.0.0.1"));
        properties.setAllowPrivateRepositoryAddresses(true);
        properties.setArtifactStateMacKeyBase64(Base64.getEncoder().encodeToString(new byte[32]));
        AgentProperties.TrustedKey trusted = new AgentProperties.TrustedKey();
        trusted.setPublicKeyPath(publicKey.toString());
        trusted.setNotBefore(Instant.now().minusSeconds(60));
        trusted.setNotAfter(Instant.now().plusSeconds(3600));
        properties.setArtifactTrustStore(Map.of("key-1", trusted));
        AgentProperties.ServiceDefinition service = new AgentProperties.ServiceDefinition();
        service.setServiceId("demo");
        service.setArtifactId("demo");
        service.setInstallRoot(temp.resolve("install").toString());
        service.setRuntimeMode("embedded-bootjar");
        service.setEnvironmentCode("qa");
        service.setReleaseChannel("stable");
        properties.setServices(Map.of("demo", service));
        return properties;
    }

    private static AgentArtifactRequest request(String version, long sequence, byte[] body, KeyPair pair)
            throws Exception {
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body));
        AgentArtifactRequest unsigned = new AgentArtifactRequest(
                "demo", "com.cpf.demo:demo", version, digest, "pending", "embedded-bootjar",
                "vault://demo/config", "operator", "approved install", sequence, "qa", "stable", "key-1");
        Signature signature = Signature.getInstance("Ed25519");
        signature.initSign(pair.getPrivate());
        signature.update(ArtifactVerifier.canonical(unsigned, body.length, digest).getBytes(StandardCharsets.UTF_8));
        return new AgentArtifactRequest(
                "demo", "com.cpf.demo:demo", version, digest,
                Base64.getEncoder().encodeToString(signature.sign()), "embedded-bootjar",
                "vault://demo/config", "operator", "approved install", sequence, "qa", "stable", "key-1");
    }
}
