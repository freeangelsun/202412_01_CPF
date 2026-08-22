package external.sample.repository;

import external.sample.model.SampleIdempotencyRecord;
import external.sample.model.SampleItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/** 중앙 Vendor Runtime Query Pack이 구현하는 Generated Domain Mapper 계약입니다. */
@Mapper
public interface SampleTransactionMapper {
    List<SampleItem> search(
            @Param("keyword") String keyword,
            @Param("statusCode") String statusCode,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection);
    long count(@Param("keyword") String keyword, @Param("statusCode") String statusCode);
    SampleItem findBySampleKey(@Param("value") String sampleKey);
    SampleItem findById(@Param("value") long sampleItemId);
    SampleIdempotencyRecord findIdempotency(@Param("value") String idempotencyKey);
    SampleItem findForUpdate(@Param("sampleItemId") long sampleItemId);
    List<SampleItem> cursorSlice(
            @Param("keyword") String keyword,
            @Param("statusCode") String statusCode,
            @Param("cursor") long cursor,
            @Param("size") int size);
    int insert(SampleItem item);
    int insertIdempotency(SampleIdempotencyRecord record);
    int updateWithVersion(SampleItem item);
    int logicalDeleteWithVersion(SampleItem item);
}
