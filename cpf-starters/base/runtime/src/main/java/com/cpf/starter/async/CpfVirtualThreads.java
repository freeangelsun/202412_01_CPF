package com.cpf.starter.async;
/** CPF 거래 Context를 보존한 Virtual Thread를 시작하는 공개 유틸리티입니다. */
public final class CpfVirtualThreads {private CpfVirtualThreads(){}public static Thread start(Runnable r,CpfAsyncContextPropagation p){return Thread.ofVirtual().start(p.wrap(r,CpfAsyncForkType.VIRTUAL_THREAD));}}
