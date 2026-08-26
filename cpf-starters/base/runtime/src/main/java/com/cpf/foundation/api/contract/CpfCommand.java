package com.cpf.foundation.api.contract;

/** 기존 Command 표식의 호환 계약이며 신규 요청 경계에서는 Core CpfRequest 의미를 함께 가집니다. */
public interface CpfCommand extends com.cpf.core.api.base.CpfRequest {}
