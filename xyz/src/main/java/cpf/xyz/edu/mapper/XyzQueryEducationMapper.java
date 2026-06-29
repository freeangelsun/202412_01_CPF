package cpf.xyz.edu.mapper;

import cpf.xyz.edu.dto.XyzQueryEducationCriteria;
import cpf.xyz.edu.dto.XyzQueryEducationItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 조회 EDU 샘플의 MyBatis Mapper입니다.
 *
 * <p>업무 예제에서 동적 검색, 안전한 정렬, offset 페이징, keyset 페이징을 어떻게 나눠 작성하는지 보여줍니다.
 * 회원 정보는 MBR 테이블과 직접 조인하지 않고 샘플 테이블의 ownerMemberNo 값만 조회합니다.</p>
 */
@Mapper
public interface XyzQueryEducationMapper {
    XyzQueryEducationItem findById(@Param("itemId") Long itemId);

    List<XyzQueryEducationItem> findItems(@Param("criteria") XyzQueryEducationCriteria criteria);

    List<XyzQueryEducationItem> findOffsetPageItems(@Param("criteria") XyzQueryEducationCriteria criteria);

    long countOffsetPageItems(@Param("criteria") XyzQueryEducationCriteria criteria);

    List<XyzQueryEducationItem> findKeysetPageItems(@Param("criteria") XyzQueryEducationCriteria criteria);
}
