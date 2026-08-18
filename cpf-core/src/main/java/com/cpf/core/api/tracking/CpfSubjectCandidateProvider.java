package com.cpf.core.api.tracking;

import com.cpf.core.api.context.CpfContext;
import java.util.List;

/** 현재 Transaction Start/Identity Boundary에서 신뢰 가능한 Subject 후보를 제공하는 SPI입니다. */
@FunctionalInterface
public interface CpfSubjectCandidateProvider {
    List<CpfSubjectCandidate> candidates(CpfContext context);
}
