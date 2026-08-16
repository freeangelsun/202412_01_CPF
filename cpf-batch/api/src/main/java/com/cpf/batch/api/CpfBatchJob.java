package com.cpf.batch.api;
import java.lang.annotation.*;
/** CPF Batch Job 식별 메타데이터 표준 Annotation입니다. */
@Target({ElementType.METHOD,ElementType.TYPE}) @Retention(RetentionPolicy.RUNTIME) public @interface CpfBatchJob { String id(); String name(); String ownerDomain(); }
