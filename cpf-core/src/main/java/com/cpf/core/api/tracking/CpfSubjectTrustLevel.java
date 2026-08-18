package com.cpf.core.api.tracking;

/** Subject Source의 신뢰 수준입니다. 높은 수준의 식별값을 낮은 수준의 claimed 값이 덮어쓸 수 없습니다. */
public enum CpfSubjectTrustLevel {
    UNVERIFIED(0),
    CLAIMED(10),
    TRUSTED(20),
    VERIFIED(30);

    private final int weight;
    CpfSubjectTrustLevel(int weight) { this.weight = weight; }
    /** 서로 다른 Subject Source를 병합할 때 높은 신뢰값을 우선하기 위한 내부 비교 가중치를 반환합니다. */
    public int weight() { return weight; }
}
