package com.cpf.core.api.persistence;

/** Persistence lock 선택입니다. NONE이 기본이며 업무 invariant에 필요한 경우만 잠금을 사용합니다. */
public enum CpfLockMode { NONE, OPTIMISTIC, PESSIMISTIC_READ, PESSIMISTIC_WRITE }
