package com.cpf.file.tabular.api;

/** Streaming Reader가 행을 처리 Owner에게 전달하는 callback입니다. */
@FunctionalInterface
public interface CpfTabularRowConsumer {
    void accept(CpfTabularRow row) throws Exception;
}
