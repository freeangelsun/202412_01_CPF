package com.cpf.batch.operation;

/** BAT 운영 조회에서 DB/SQL 장애가 발생했음을 정상 0건과 구분하는 예외입니다. */
public class BatOperationQueryException extends RuntimeException {
    public BatOperationQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
