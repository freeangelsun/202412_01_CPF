package com.cpf.web.context;
import com.cpf.core.api.context.CpfContextSnapshot;
/** HTTP ingress에서 생성한 Core Snapshot과 Web 전용 Interaction metadata입니다. */
public record CpfHttpInboundResult(CpfContextSnapshot snapshot,CpfWebContext interaction) { }
