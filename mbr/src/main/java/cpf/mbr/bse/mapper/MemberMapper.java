package cpf.mbr.bse.mapper;

import cpf.mbr.bse.entity.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * ?뚯썝 MyBatis Mapper ?명꽣?섏씠??
 * - ?곗씠?곕쿋?댁뒪 議고쉶/?깅줉/?섏젙/??젣 荑쇰━ ?뺤쓽
 * - XML ?ㅼ젙怨??④퍡 ?ъ슜: mbr-mybatis-config.xml
 * 
 * @author FPS Team
 * @version 1.0.0
 */
public interface MemberMapper {
    
    /**
     * ?꾩껜 ?뚯썝 議고쉶 (?섏씠吏?誘명룷??
     * @return ?뚯썝 紐⑸줉
     */
    List<Member> selectAllMembers();
    
    /**
     * ?뚯썝 ID濡?議고쉶
     * @param id ?뚯썝 ID
     * @return ?뚯썝 ?뺣낫 Optional
     */
    Optional<Member> selectMemberById(@Param("id") Integer id);
    
    /**
     * ?뚯썝紐낆쑝濡?議고쉶
     * @param name ?뚯썝紐?
     * @return ?뚯썝 紐⑸줉
     */
    List<Member> selectMembersByName(@Param("name") String name);
    
    /**
     * ?뚯썝 ?깅줉
     * @param member ?뚯썝 ?뺣낫
     * @return ?깅줉??????
     */
    int insertMember(Member member);
    
    /**
     * ?뚯썝 ?섏젙
     * @param member ?섏젙 ?뚯썝 ?뺣낫
     * @return ?섏젙??????
     */
    int updateMember(Member member);
    
    /**
     * ?뚯썝 ??젣
     * @param id ?뚯썝 ID
     * @return ??젣??????
     */
    int deleteMemberById(@Param("id") Integer id);
}

