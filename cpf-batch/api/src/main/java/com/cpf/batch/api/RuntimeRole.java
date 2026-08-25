package com.cpf.batch.api;

/** BAT 설치물의 실행 역할. 역할은 Process/Artifact 경계와 일치해야 한다. */
public enum RuntimeRole {
    CONTROL_PLANE,
    SCHEDULER,
    WORKER,
    CENTER_CUT_RUNNER,
    AGENT
}
