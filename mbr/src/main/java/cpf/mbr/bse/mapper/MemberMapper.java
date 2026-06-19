package cpf.mbr.bse.mapper;

import cpf.mbr.bse.entity.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MBR 회원 MyBatis Mapper입니다.
 */
public interface MemberMapper {

    /** 전체 회원 목록을 조회합니다. */
    List<Member> selectAllMembers();

    /** 회원 내부 순번으로 회원을 조회합니다. */
    Optional<Member> selectMemberById(@Param("id") Integer id);

    /** 로그인 ID로 인증 대상 회원을 조회합니다. */
    Optional<Member> selectMemberByLoginId(@Param("loginId") String loginId);

    /** 회원명 일부로 회원을 검색합니다. */
    List<Member> selectMembersByName(@Param("name") String name);

    /** 회원을 등록합니다. */
    int insertMember(Member member);

    /** 회원 기본 정보를 수정합니다. */
    int updateMember(Member member);

    /** 회원을 물리 삭제합니다. 운영 기능에서는 상태 변경을 우선 사용합니다. */
    int deleteMemberById(@Param("id") Integer id);

    /** 로그인 실패 횟수를 증가시킵니다. */
    int increaseLoginFailCount(@Param("id") Integer id);

    /** 로그인 성공 기준으로 실패 횟수와 최근 로그인 일시를 갱신합니다. */
    int markLoginSuccess(@Param("id") Integer id);

    /** 회원 로그인 이력을 저장합니다. */
    int insertMemberLoginHistory(Map<String, Object> row);

    /** 회원 refresh token hash를 저장합니다. */
    int insertRefreshToken(Map<String, Object> row);

    /** refresh token hash 기준으로 token 상태를 조회합니다. */
    Map<String, Object> selectRefreshTokenByHash(@Param("refreshTokenHash") String refreshTokenHash);

    /** refresh token hash 기준으로 token을 폐기합니다. */
    int revokeRefreshTokenByHash(@Param("refreshTokenHash") String refreshTokenHash);

    /** 최근 회원 로그인 이력을 조회합니다. */
    List<Map<String, Object>> selectLoginHistories(@Param("limit") int limit);
}
