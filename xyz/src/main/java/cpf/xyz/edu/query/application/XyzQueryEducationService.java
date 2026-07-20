package cpf.xyz.edu.query.application;

import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.xyz.edu.query.dto.XyzQueryEducationItem;
import cpf.xyz.edu.query.dto.XyzQueryKeysetResponse;
import cpf.xyz.edu.query.dto.XyzQueryPageResponse;
import cpf.xyz.edu.query.adapter.XyzQueryEducationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 실제 SQL 조회 패턴을 학습하기 위한 EDU 서비스입니다.
 *
 * <p>단건, 목록, offset 페이징, keyset 페이징을 각각 분리해서 보여줍니다. Controller는 요청 파라미터만 받고,
 * Repository는 검색/정렬/limit 정규화를 담당하며, Service는 트랜잭션 경계와 응답 조립을 담당합니다.</p>
 */
@Service
public class XyzQueryEducationService {
    private final XyzQueryEducationRepository repository;

    public XyzQueryEducationService(XyzQueryEducationRepository repository) {
        this.repository = repository;
    }

    /**
     * 단건 조회 샘플입니다.
     *
     * <p>조회 결과가 없으면 CPF 표준 NotFound 예외로 변환합니다. 실제 업무에서도 Service에서 업무 의미가 있는
     * 예외로 바꾸면 Controller의 오류 응답 포맷을 일관되게 유지할 수 있습니다.</p>
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzQueryEducationItem getItem(Long itemId) {
        return repository.findById(itemId)
                .orElseThrow(() -> new CpfNotFoundException("조회 EDU 항목을 찾을 수 없습니다. itemId=" + itemId));
    }

    /**
     * 단순 목록 조회 샘플입니다.
     *
     * <p>정렬 값은 Repository에서 whitelist 코드로 변환한 뒤 Mapper XML의 choose 분기로만 처리합니다.</p>
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public List<XyzQueryEducationItem> findItems(String keyword, String statusCode, String sort, int limit) {
        return repository.findItems(keyword, statusCode, sort, limit);
    }

    /**
     * offset 기반 페이징 샘플입니다.
     *
     * <p>관리자 목록처럼 전체 건수가 필요한 화면에 적합합니다. 대용량 실시간 목록은 keyset 방식을 우선 검토합니다.</p>
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzQueryPageResponse<XyzQueryEducationItem> findOffsetPage(
            String keyword,
            String statusCode,
            String sort,
            int page,
            int size) {
        int normalizedPage = repository.normalizePage(page);
        int normalizedSize = repository.normalizeSize(size);
        long total = repository.countOffsetPageItems(keyword, statusCode);
        List<XyzQueryEducationItem> items = repository.findOffsetPageItems(
                keyword,
                statusCode,
                sort,
                normalizedPage,
                normalizedSize);
        boolean hasNext = normalizedPage * (long) normalizedSize < total;
        return new XyzQueryPageResponse<>(items, normalizedPage, normalizedSize, total, hasNext);
    }

    /**
     * keyset 기반 페이징 샘플입니다.
     *
     * <p>마지막으로 본 itemId 이후를 조회하고, 요청 크기보다 한 건 더 가져와 다음 페이지 존재 여부를 판단합니다.</p>
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzQueryKeysetResponse<XyzQueryEducationItem> findKeysetPage(Long cursorId, int size) {
        int normalizedSize = repository.normalizeSize(size);
        List<XyzQueryEducationItem> page = repository.findKeysetPageItems(cursorId, normalizedSize);
        boolean hasNext = page.size() > normalizedSize;
        List<XyzQueryEducationItem> items = hasNext ? page.subList(0, normalizedSize) : page;
        Long nextCursorId = items.isEmpty() ? cursorId : items.get(items.size() - 1).itemId();
        return new XyzQueryKeysetResponse<>(items, nextCursorId, hasNext);
    }
}
