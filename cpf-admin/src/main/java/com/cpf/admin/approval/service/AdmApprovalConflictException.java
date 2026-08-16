package com.cpf.admin.approval.service;
public class AdmApprovalConflictException extends RuntimeException {
    public AdmApprovalConflictException(String message){super(message);}
    public AdmApprovalConflictException(String message,Throwable cause){super(message,cause);}
}
