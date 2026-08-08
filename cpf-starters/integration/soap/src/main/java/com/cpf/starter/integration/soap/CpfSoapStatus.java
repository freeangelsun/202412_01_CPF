package com.cpf.starter.integration.soap;
/** SOAP 호출 최종 판정. Timeout/connection-loss는 UNKNOWN으로 분리하여 reconcile 대상으로 남깁니다. */
public enum CpfSoapStatus { SUCCESS, FAILED, UNKNOWN }
