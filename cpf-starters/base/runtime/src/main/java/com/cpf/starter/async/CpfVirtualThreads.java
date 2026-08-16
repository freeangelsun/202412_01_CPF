package com.cpf.starter.async;
public final class CpfVirtualThreads {private CpfVirtualThreads(){}public static Thread start(Runnable r,CpfAsyncContextPropagation p){return Thread.ofVirtual().start(p.wrap(r,CpfAsyncForkType.VIRTUAL_THREAD));}}
