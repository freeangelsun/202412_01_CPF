package cpf.pfw.mapper.common.logging;

import cpf.pfw.common.logging.segment.TransactionSegmentRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 복합 거래 구간 로그를 저장하는 MyBatis mapper입니다.
 */
@Mapper
public interface TransactionSegmentMapper {
    void insertSegment(TransactionSegmentRecord record);

    void updateSegmentEnd(TransactionSegmentRecord record);
}
