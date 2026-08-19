package com.cpf.education.online.basiccrud.dto;
import jakarta.validation.constraints.NotBlank;
/** 기본 CRUD 교육 예제의 DTO 역할과 CPF 표준 사용 경계를 보여줍니다. */
public record CrudCommand(CrudAction action,@NotBlank String memberId,String name){public CrudCommand{if(action==null)throw new IllegalArgumentException("action is required");}}
