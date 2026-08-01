package com.cpf.reference.edu.counterparty.application;
import java.util.Map;
public record ReferenceCounterpartyResult(int httpStatus, boolean replayed, Map<String,Object> body) { }
