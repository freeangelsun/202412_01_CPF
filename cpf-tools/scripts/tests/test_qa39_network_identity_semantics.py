from __future__ import annotations

import subprocess
from pathlib import Path

QA39 = Path(__file__).parents[1] / "Qa39Tool.java"


def write(root: Path, relative: str, text: str) -> None:
    path = root / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def compile_tool(tmp_path: Path) -> Path:
    classes = tmp_path / "classes"
    classes.mkdir()
    result = subprocess.run(["javac", "-d", str(classes), str(QA39)], text=True, capture_output=True)
    assert result.returncode == 0, result.stdout + result.stderr
    return classes


def fixture(root: Path) -> None:
    files = {
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayPinnedAddressContext.java": "ThreadLocal<Map<String, InetAddress>> Unapproved gateway DNS resolution CURRENT.remove()",
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayPinnedHttpClientConfiguration.java": ".setDnsResolver(new DnsResolver() { CpfGatewayPinnedAddressContext.resolve(host); }) NoConnectionReuseStrategy.INSTANCE .disableRedirectHandling() .disableAutomaticRetries()",
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java": "CpfGatewayPinnedAddressContext.call( target.pinnedAddress() () -> http().handle(upstreamRequest)",
        "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java": "Gateway DNS mixed private/public response denied pinnedAddress validateResolvedAddresses",
        "cpf-gateway/src/test/java/com/cpf/gateway/scg/CpfScgTargetResolverTest.java": "dnsChangeCannotAlterActivePinnedConnectionIdentity rejectsMixedPrivatePublicAndMetadataResponses",
        "cpf-gateway/build.gradle": "org.apache.httpcomponents.client5:httpclient5",
        "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java": "new InetSocketAddress(target.address(), target.port()) setEndpointIdentificationAlgorithm(\"HTTPS\") new SNIHostName(target.host()) ARTIFACT_REDIRECT_DENIED _MIXED_DNS_RESPONSE_DENIED _PIN_REQUIRED",
        "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/ArtifactInstaller.java": "new PinnedArtifactHttpTransport(properties) artifactTransport.download",
        "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/AgentProperties.java": "artifactPinnedAddresses artifactAllowedCidrs artifactProxyPinnedAddresses artifactProxyAllowedCidrs artifactAllowedPorts",
        "cpf-batch/host-agent/src/test/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransportTest.java": "mismatchingPinAndMetadataAddressFailClosed mixedDnsCidrAndPortPoliciesFailClosed publicHostnameRequiresExplicitAddressPins",
    }
    for path, text in files.items():
        write(root, path, text)


def test_network_identity_accepts_dns_resolver_object_and_case_variant(tmp_path: Path) -> None:
    repo = tmp_path / "repo"
    fixture(repo)
    classes = compile_tool(tmp_path)
    result = subprocess.run(["java", "-cp", str(classes), "Qa39Tool", "network-identity", "--root", str(repo)], text=True, capture_output=True)
    assert result.returncode == 0, result.stdout + result.stderr
    assert "network identity: PASS" in result.stdout


def test_network_identity_rejects_unpinned_dns_resolver(tmp_path: Path) -> None:
    repo = tmp_path / "repo"
    fixture(repo)
    path = repo / "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayPinnedHttpClientConfiguration.java"
    path.write_text("NoConnectionReuseStrategy.INSTANCE .disableRedirectHandling() .disableAutomaticRetries()", encoding="utf-8")
    classes = compile_tool(tmp_path)
    result = subprocess.run(["java", "-cp", str(classes), "Qa39Tool", "network-identity", "--root", str(repo)], text=True, capture_output=True)
    assert result.returncode == 1
    assert "gateway-client:pinned DNS resolver" in result.stderr
