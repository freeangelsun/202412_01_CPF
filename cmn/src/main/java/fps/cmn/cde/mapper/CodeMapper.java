package fps.cmn.cde.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;
import fps.cmn.cde.dto.CommonCodeRequest;

/**
 * CodeMapper.java
 *
 * - 코드 데이터를 조회하는 MyBatis 매퍼 인터페이스
 * - 코드 테이블에서 모든 코드 데이터를 조회하거나 특정 코드 키로 조회하는 메서드를 제공합니다.
 */
@Mapper
public interface CodeMapper {
    /**
     * 모든 코드 데이터를 조회합니다.
     *
     * @return 코드 데이터 목록 (Map 형태로 반환)
     */
    List<Map<String, Object>> findAllCodes();

    /**
     * 특정 코드 키에 해당하는 코드 데이터를 조회합니다.
     *
     * @param codeKey 코드 키
     * @return 코드 데이터 (Map 형태로 반환)
     */
    Map<String, Object> findCodeByKey(@Param("codeKey") String codeKey);

    /**
     * 특정 코드 키에 해당하는 코드 목록을 조회합니다.
     *
     * @param codeKey 코드 키
     * @return 같은 코드 키를 가진 코드 목록
     */
    List<Map<String, Object>> findCodesByKey(@Param("codeKey") String codeKey);

    /**
     * 코드 ID로 코드 한 건을 조회합니다.
     *
     * @param codeId 코드 ID
     * @return 코드 데이터
     */
    Map<String, Object> findCodeById(@Param("codeId") Long codeId);

    /**
     * 공통 코드를 등록합니다.
     *
     * @param request 등록 요청
     * @return 등록 건수
     */
    int insertCode(CommonCodeRequest request);

    /**
     * 공통 코드를 수정합니다.
     *
     * @param codeId 수정할 코드 ID
     * @param request 수정 요청
     * @return 수정 건수
     */
    int updateCode(@Param("codeId") Long codeId, @Param("request") CommonCodeRequest request);

    /**
     * 공통 코드를 삭제합니다.
     *
     * @param codeId 삭제할 코드 ID
     * @return 삭제 건수
     */
    int deleteCode(@Param("codeId") Long codeId);
}
