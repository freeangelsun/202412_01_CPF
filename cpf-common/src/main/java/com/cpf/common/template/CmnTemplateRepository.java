package com.cpf.common.template;

import java.util.List;
import java.util.Optional;

/**
 * @deprecated The product persistence contract is {@link CmnTemplateStore}.
 * This source-only compatibility type remains temporarily so downstream code gets an explicit
 * migration target instead of silently activating a second persistence implementation.
 */
@Deprecated(forRemoval = true)
public interface CmnTemplateRepository extends CmnTemplateProvider {
    CmnTemplateRecord createDraft(CmnTemplateDefinition definition, String actor);
    CmnTemplateRecord approve(String templateCode, long version, String channel, String approver);
    CmnTemplateRecord retire(String templateCode, long version, String channel, String actor);
    Optional<CmnTemplateRecord> findVersion(String templateCode, long version, String channel);
    List<CmnTemplateRecord> history(String templateCode, String channel, int limit);
}
