package com.cpf.education.online.cache.repository;
import com.cpf.education.online.cache.dto.MemberProfileResponse;
/** 실제 프로젝트에서는 DB Repository 구현이 이 책임을 소유합니다. */
public interface MemberProfileRepository { MemberProfileResponse findRequired(String memberId); }
