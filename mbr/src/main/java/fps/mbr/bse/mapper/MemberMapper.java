package fps.mbr.bse.mapper;

import fps.mbr.bse.entity.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 회원 MyBatis Mapper 인터페이스
 * - 데이터베이스 조회/등록/수정/삭제 쿼리 정의
 * - XML 설정과 함께 사용: mbr-mybatis-config.xml
 * 
 * @author FPS Team
 * @version 1.0.0
 */
public interface MemberMapper {
    
    /**
     * 전체 회원 조회 (페이징 미포함)
     * @return 회원 목록
     */
    List<Member> selectAllMembers();
    
    /**
     * 회원 ID로 조회
     * @param id 회원 ID
     * @return 회원 정보 Optional
     */
    Optional<Member> selectMemberById(@Param("id") Integer id);
    
    /**
     * 회원명으로 조회
     * @param name 회원명
     * @return 회원 목록
     */
    List<Member> selectMembersByName(@Param("name") String name);
    
    /**
     * 회원 등록
     * @param member 회원 정보
     * @return 등록된 행 수
     */
    int insertMember(Member member);
    
    /**
     * 회원 수정
     * @param member 수정 회원 정보
     * @return 수정된 행 수
     */
    int updateMember(Member member);
    
    /**
     * 회원 삭제
     * @param id 회원 ID
     * @return 삭제된 행 수
     */
    int deleteMemberById(@Param("id") Integer id);
}
