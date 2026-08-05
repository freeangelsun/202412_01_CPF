package com.cpf.common.template;

import java.util.List;
import java.util.Optional;

/** Product persistence contract for versioned template administration and runtime lookup. */
public interface CmnTemplateStore extends CmnTemplateProvider {
    Optional<CmnTemplateVersion> findVersion(String templateCode, String channel, long version);

    List<CmnTemplateAuditEntry> auditHistory(String templateCode, String channel, long version, int limit);

    CmnTemplateVersion createDraft(CmnTemplateDefinition definition, String actor, String reason);

    CmnTemplateVersion approve(
            String templateCode,
            String channel,
            long version,
            long expectedRevision,
            String actor,
            String reason);

    CmnTemplateVersion retire(
            String templateCode,
            String channel,
            long version,
            long expectedRevision,
            String actor,
            String reason);
}
