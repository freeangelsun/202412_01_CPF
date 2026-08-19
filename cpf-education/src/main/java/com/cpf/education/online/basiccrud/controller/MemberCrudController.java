package com.cpf.education.online.basiccrud.controller;
import com.cpf.education.online.basiccrud.dto.CrudCommand;
import com.cpf.education.online.basiccrud.service.MemberCrudService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.web.api.CpfRestController;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/members")
/** 기본 CRUD 교육 예제의 Controller 역할과 CPF 표준 사용 경계를 보여줍니다. */
public class MemberCrudController {
 private final MemberCrudService service; public MemberCrudController(MemberCrudService service){this.service=service;}
 @PostMapping @Operation(operationId="EDU_MEMBER_CRUD",summary="기본 CRUD 거래")
 @CpfOnlineTransaction(operationId="EDU_MEMBER_CRUD",name="기본 CRUD 거래",description="CPF Repository를 사용해 생성·조회·수정·삭제를 수행한다.")
 public Map<String,Object> execute(@Valid @RequestBody CrudCommand c){return switch(c.action()){case CREATE->Map.of("member",service.create(c));case READ->Map.of("member",service.find(c.memberId()));case UPDATE->Map.of("member",service.update(c));case DELETE->{service.delete(c.memberId());yield Map.of("deleted",c.memberId());}};}
}
