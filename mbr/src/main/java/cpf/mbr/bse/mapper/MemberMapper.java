package cpf.mbr.bse.mapper;

import cpf.mbr.bse.entity.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * MBR 회원 MyBatis Mapper입니다.
 */
public interface MemberMapper {

    /** 전체 회원 목록을 조회합니다. */
    List<Member> selectAllMembers();

    /** 회원 내부 순번으로 회원을 조회합니다. */
    Optional<Member> selectMemberById(@Param("id") Integer id);

    /** 회원명 일부로 회원을 검색합니다. */
    List<Member> selectMembersByName(@Param("name") String name);

    /** 회원을 등록합니다. */
    int insertMember(Member member);

    /** 회원 기본 정보를 수정합니다. */
    int updateMember(Member member);

    /** 회원을 물리 삭제합니다. 운영 기능에서는 상태 변경을 우선 사용합니다. */
    int deleteMemberById(@Param("id") Integer id);
}
