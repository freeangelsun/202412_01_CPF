package com.cpf.admin.approval.controller;

import com.cpf.admin.approval.service.AdmApprovalConflictException;
import com.cpf.core.api.error.CpfValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.ConcurrentModificationException;
import java.util.Map;

@RestControllerAdvice(assignableTypes = AdmApprovalController.class)
public class AdmApprovalExceptionHandler {
    @ExceptionHandler({AdmApprovalConflictException.class, ConcurrentModificationException.class, DataIntegrityViolationException.class})
    ResponseEntity<Map<String,Object>> conflict(RuntimeException error){return problem(HttpStatus.CONFLICT,"CONFLICT",error);}
    @ExceptionHandler({CpfValidationException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<Map<String,Object>> validation(Exception error){return problem(HttpStatus.UNPROCESSABLE_ENTITY,"VALIDATION_FAILED",error);}
    private static ResponseEntity<Map<String,Object>> problem(HttpStatus status,String code,Exception error){
        return ResponseEntity.status(status).body(Map.of("status",status.value(),"code",code,
                "message",error.getMessage()==null?status.getReasonPhrase():error.getMessage(),"timestamp",Instant.now().toString()));
    }
}
