#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path


def read(root: Path, relative: str) -> str:
    path = root / relative
    if not path.is_file():
        raise SystemExit(f"missing required file: {relative}")
    return path.read_text(encoding="utf-8-sig")


def require(body: str, label: str, *tokens: str) -> list[str]:
    return [f"{label}:{token}" for token in tokens if token not in body]


def main() -> int:
    parser = argparse.ArgumentParser(description="Verify QA34 Gateway and Host Agent network identity pinning.")
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    root = Path(args.root).resolve()

    gateway_context = read(root, "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayPinnedAddressContext.java")
    gateway_client = read(root, "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfGatewayPinnedHttpClientConfiguration.java")
    gateway_handler = read(root, "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgPrimaryHandler.java")
    gateway_resolver = read(root, "cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java")
    gateway_test = read(root, "cpf-gateway/src/test/java/com/cpf/gateway/scg/CpfScgTargetResolverTest.java")
    gateway_build = read(root, "cpf-gateway/build.gradle")

    agent_transport = read(root, "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransport.java")
    agent_installer = read(root, "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/ArtifactInstaller.java")
    agent_properties = read(root, "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/AgentProperties.java")
    agent_test = read(root, "cpf-batch/host-agent/src/test/java/com/cpf/batch/agent/internal/PinnedArtifactHttpTransportTest.java")

    failed: list[str] = []
    failed += require(gateway_context, "gateway-context", "ThreadLocal<Map<String, InetAddress>>", "Unapproved gateway DNS resolution", "CURRENT.remove()")
    failed += require(gateway_client, "gateway-client", ".setDnsResolver(CpfGatewayPinnedAddressContext::resolve)", "NoConnectionReuseStrategy.INSTANCE", ".disableRedirectHandling()", ".disableAutomaticRetries()")
    failed += require(gateway_handler, "gateway-handler", "CpfGatewayPinnedAddressContext.call(", "target.pinnedAddress()", "() -> http().handle(upstreamRequest)")
    failed += require(gateway_resolver, "gateway-resolver", "mixed private/public DNS response denied", "pinnedAddress", "validateResolvedAddresses")
    failed += require(gateway_test, "gateway-test", "dnsChangeCannotAlterActivePinnedConnectionIdentity", "rejectsMixedPrivatePublicAndMetadataResponses")
    failed += require(gateway_build, "gateway-build", "org.apache.httpcomponents.client5:httpclient5")

    failed += require(agent_transport, "agent-transport", "new InetSocketAddress(target.address(), target.port())", "setEndpointIdentificationAlgorithm(\"HTTPS\")", "new SNIHostName(target.host())", "ARTIFACT_REDIRECT_DENIED", "_MIXED_DNS_RESPONSE_DENIED", "_PIN_REQUIRED")
    failed += require(agent_installer, "agent-installer", "new PinnedArtifactHttpTransport(properties)", "artifactTransport.download")
    failed += require(agent_properties, "agent-properties", "artifactPinnedAddresses", "artifactAllowedCidrs", "artifactProxyPinnedAddresses", "artifactProxyAllowedCidrs", "artifactAllowedPorts")
    failed += require(agent_test, "agent-test", "mismatchingPinAndMetadataAddressFailClosed", "mixedDnsCidrAndPortPoliciesFailClosed", "publicHostnameRequiresExplicitAddressPins")

    forbidden = {
        "gateway-client": ["SimpleClientHttpRequestFactory"],
        "agent-installer": ["HttpClient.newHttpClient()", "followRedirects(HttpClient.Redirect.ALWAYS)"],
    }
    bodies = {"gateway-client": gateway_client, "agent-installer": agent_installer}
    for label, tokens in forbidden.items():
        for token in tokens:
            if token in bodies[label]:
                failed.append(f"{label}:forbidden:{token}")

    if failed:
        raise SystemExit("network identity contract failed: " + ", ".join(failed))
    print("CPF gateway/agent network identity: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
