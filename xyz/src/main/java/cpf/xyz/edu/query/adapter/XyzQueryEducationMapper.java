package cpf.xyz.edu.query.adapter;

import cpf.xyz.edu.query.dto.XyzQueryEducationCriteria;
import cpf.xyz.edu.query.dto.XyzQueryEducationItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 조회/CRUD EDU 샘플 MyBatis Mapper입니다.
 *
 * <p>동적 검색, 안전한 정렬, offset/keyset paging, 등록/수정/상태변경/논리삭제를 하나의 교육 테이블로 보여줍니다.
 * 업무 예제는 MBR/BZA 같은 다른 주제영역 테이블을 직접 조인하지 않고, 필요한 식별자 값만 저장해 경계를 유지합니다.</p>
 */
@Mapper
public interface XyzQueryEducationMapper {
    XyzQueryEducationItem findById(@Param("itemId") Long itemId);

    List<XyzQueryEducationItem> findItems(@Param("criteria") XyzQueryEducationCriteria criteria);

    List<XyzQueryEducationItem> findOffsetPageItems(@Param("criteria") XyzQueryEducationCriteria criteria);

    long countOffsetPageItems(@Param("criteria") XyzQueryEducationCriteria criteria);

    List<XyzQueryEducationItem> findKeysetPageItems(@Param("criteria") XyzQueryEducationCriteria criteria);

    Long nextCrudItemId();

    int insertCrudItem(
            @Param("itemId") Long itemId,
            @Param("itemName") String itemName,
            @Param("categoryCode") String categoryCode,
            @Param("statusCode") String statusCode,
            @Param("ownerMemberNo") String ownerMemberNo,
            @Param("requestUser") String requestUser);

    int updateCrudItem(
            @Param("itemId") Long itemId,
            @Param("itemName") String itemName,
            @Param("categoryCode") String categoryCode,
            @Param("ownerMemberNo") String ownerMemberNo,
            @Param("requestUser") String requestUser);

    int updateCrudItemStatus(
            @Param("itemId") Long itemId,
            @Param("statusCode") String statusCode,
            @Param("requestUser") String requestUser);

    int logicalDeleteCrudItem(
            @Param("itemId") Long itemId,
            @Param("requestUser") String requestUser);
}
