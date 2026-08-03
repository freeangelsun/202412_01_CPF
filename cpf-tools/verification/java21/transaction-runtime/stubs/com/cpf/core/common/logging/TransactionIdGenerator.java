package com.cpf.core.common.logging; public final class TransactionIdGenerator {public static boolean isValid(String v,int n){return v!=null&&v.matches("[A-Z0-9]{8}-[A-Z0-9]{"+n+"}");}}
