package com.cpf.security.session.jdbc;

import org.springframework.context.ApplicationListener;
import org.springframework.session.Session;
import org.springframework.session.events.SessionDestroyedEvent;

/** 분산 Session 만료·강제 삭제 시 연결된 Vault Credential을 함께 폐기합니다. */
final class CpfBffSessionDestroyedListener implements ApplicationListener<SessionDestroyedEvent> {
    private final CpfBffCredentialVault vault;
    CpfBffSessionDestroyedListener(CpfBffCredentialVault vault) { this.vault = vault; }

    @Override public void onApplicationEvent(SessionDestroyedEvent event) {
        Session session = event.getSession();
        Object handle = session.getAttribute(CpfBffSessionBridgeFilter.CREDENTIAL_HANDLE);
        if (handle instanceof String value && !value.isBlank()) vault.revoke(value);
    }
}
