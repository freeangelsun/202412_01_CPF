package com.cpf.education.online.cache.controller;
import com.cpf.education.online.cache.dto.MemberProfileResponse;
import com.cpf.education.online.cache.service.MemberProfileService;
import com.cpf.foundation.execution.api.CpfOnlineTransaction; import com.cpf.web.api.CpfRestController; import io.swagger.v3.oas.annotations.Operation; import org.springframework.web.bind.annotation.*;
@CpfRestController @RequestMapping("/edu/online/member-profile")
/** MemberProfileController는 CpfCache의 getOrLoad와 명시적 무효화를 사용하는 Cache Golden Path입니다. */
public class MemberProfileController { private final MemberProfileService service; public MemberProfileController(MemberProfileService service){this.service=service;}
 @GetMapping("/{memberId}") @Operation(operationId="EDU_MEMBER_PROFILE_CACHE",summary="회원 프로필 Cache") @CpfOnlineTransaction(operationId="EDU_MEMBER_PROFILE_CACHE",name="회원 프로필 Cache",description="CpfCache getOrLoad single-flight Golden Path")
 public MemberProfileResponse get(@PathVariable String memberId){return service.find(memberId);} }
