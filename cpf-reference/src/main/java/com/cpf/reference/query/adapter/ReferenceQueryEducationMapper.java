package com.cpf.reference.query.adapter;

import com.cpf.reference.query.dto.ReferenceQueryEducationCriteria;
import com.cpf.reference.query.dto.ReferenceQueryEducationItem;
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
public interface ReferenceQueryEducationMapper {
    ReferenceQueryEducationItem findById(@Param("itemId") Long itemId);

    List<ReferenceQueryEducationItem> findItems(@Param("criteria") ReferenceQueryEducationCriteria criteria);

    List<ReferenceQueryEducationItem> findOffsetPageItems(@Param("criteria") ReferenceQueryEducationCriteria criteria);

    long countOffsetPageItems(@Param("criteria") ReferenceQueryEducationCriteria criteria);

    List<ReferenceQueryEducationItem> findKeysetPageItems(@Param("criteria") ReferenceQueryEducationCriteria criteria);

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
