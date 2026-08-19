package com.cpf.education.online.basiccrud.service;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.data.persistence.api.CpfCrudRepository;
import com.cpf.education.online.basiccrud.dto.CrudCommand;
import com.cpf.education.online.basiccrud.model.Member;
import com.cpf.foundation.annotation.CpfService;
import org.springframework.beans.factory.ObjectProvider;
@CpfService
/** 기본 CRUD 교육 예제의 Service 역할과 CPF 표준 사용 경계를 보여줍니다. */
public class MemberCrudService {
 private final ObjectProvider<CpfCrudRepository<Member,String>> repositories;
 /** 기본 CRUD 예제의 Service 의존성을 주입해 표준 실행 경계를 구성합니다. */
 public MemberCrudService(ObjectProvider<CpfCrudRepository<Member,String>> repositories){this.repositories=repositories;}
 /** 기본 CRUD 예제에서 create 요청을 표준 호출 흐름으로 처리합니다. */
 public Member create(CrudCommand c){return repository().save(new Member(c.memberId(),c.name(),0));}
 /** 기본 CRUD 예제에서 find 요청을 표준 호출 흐름으로 처리합니다. */
 public Member find(String id){return repository().findById(id).orElseThrow(()->new CpfNotFoundException("회원이 없습니다: "+id));}
 /** 기본 CRUD 예제에서 update 요청을 표준 호출 흐름으로 처리합니다. */
 public Member update(CrudCommand c){Member current=find(c.memberId());String name=c.name()==null||c.name().isBlank()?current.name():c.name();return repository().save(new Member(current.memberId(),name,current.version()+1));}
 /** 기본 CRUD 예제에서 delete 요청을 표준 호출 흐름으로 처리합니다. */
 public void delete(String id){if(!repository().existsById(id))throw new CpfNotFoundException("회원이 없습니다: "+id);repository().deleteById(id);}
 private CpfCrudRepository<Member,String> repository(){var r=repositories.getIfAvailable();if(r==null)throw new IllegalStateException("CPF CRUD repository provider is not configured");return r;}
}
