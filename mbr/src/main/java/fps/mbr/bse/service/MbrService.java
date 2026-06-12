package fps.mbr.bse.service;

import fps.mbr.common.exception.ApiException;
import fps.mbr.common.response.ResponseCode;
import fps.mbr.bse.dto.MbrDTO;
import fps.mbr.bse.entity.Member;
import fps.mbr.bse.mapper.MemberMapper;
import fps.pfw.common.exception.FpsNotFoundException;
import fps.pfw.common.exception.FpsValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원 서비스
 * - 회원 조회, 등록, 수정, 삭제 비즈니스 로직
 * - 데이터 검증 및 변환
 * - 트랜잭션 관리
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "mbrTransactionManager")
public class MbrService {
    
    private final MemberMapper memberMapper;
    
    /**
     * 전체 회원 목록 조회
     * @return 회원 DTO 목록
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public List<MbrDTO> getAllMembers() {
        log.info("전체 회원 목록 조회 시작");
        
        List<Member> members = memberMapper.selectAllMembers();
        
        if (members.isEmpty()) {
            log.info("조회된 회원 없음");
        } else {
            log.info("조회된 회원 수: {}", members.size());
        }
        
        return members.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 회원 ID로 상세 조회
     * @param memberId 회원 ID (필수, 쿼리 파라미터)
     * @return 회원 DTO
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public MbrDTO getMemberById(Integer memberId) {
        // 입력값 검증
        if (memberId == null || memberId <= 0) {
            log.warn("유효하지 않은 회원 ID: {}", memberId);
            // PFW 표준 예외 샘플입니다. 고객용 메시지는 메시지 테이블의 EXTERNAL 문구를 사용하고,
            // "memberId: ..." 상세 내용은 내부 메시지/DB 로그/파일 로그 추적용으로만 활용합니다.
            throw new FpsValidationException("memberId는 양수여야 합니다. memberId=" + memberId);
        }
        
        log.info("회원 상세 조회 - memberId: {}", memberId);
        
        Member member = memberMapper.selectMemberById(memberId)
                .orElseThrow(() -> {
                    log.warn("회원을 찾을 수 없음 - memberId: {}", memberId);
                    // 조회 대상 없음도 표준 예외로 던지면 PFW가 HTTP 404, 오류코드, 메시지 헤더를 자동 구성합니다.
                    return new FpsNotFoundException("회원 기본정보가 존재하지 않습니다. memberId=" + memberId);
                });
        
        return convertToDto(member);
    }
    
    /**
     * 회원명으로 검색 조회
     * @param name 회원명 검색 키워드 (필수, 쿼리 파라미터)
     * @return 회원 DTO 목록
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public List<MbrDTO> searchMembersByName(String name) {
        // 입력값 검증
        if (name == null || name.trim().isEmpty()) {
            log.warn("회원명 검색 키워드가 비어있음");
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "회원명은 필수 입력값입니다.");
        }
        
        if (name.length() > 100) {
            log.warn("회원명 검색 키워드 길이 초과: {}", name.length());
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "회원명은 100자 이하여야 합니다.");
        }
        
        log.info("회원명 검색 - keyword: {}", name);
        
        List<Member> members = memberMapper.selectMembersByName(name);
        
        log.info("회원명 검색 결과: {} 건", members.size());
        
        return members.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 회원 등록
     * @param memberName 회원명 (필수, Body 파라미터)
     * @param description 회원 설명 (선택, Body 파라미터)
     * @param requesterId 요청자 ID (감시용)
     * @return 등록된 회원 DTO
     */
    public MbrDTO createMember(String memberName, String description, String requesterId) {
        // 입력값 검증
        if (memberName == null || memberName.trim().isEmpty()) {
            log.warn("회원명이 비어있음");
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "회원명은 필수 입력값입니다.");
        }
        
        if (memberName.length() > 100) {
            log.warn("회원명 길이 초과: {}", memberName.length());
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "회원명은 100자 이하여야 합니다.");
        }
        
        if (description != null && description.length() > 255) {
            log.warn("설명 길이 초과: {}", description.length());
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "설명은 255자 이하여야 합니다.");
        }
        
        log.info("회원 등록 시작 - memberName: {}, requesterId: {}", memberName, requesterId);
        
        // 엔티티 생성 및 등록
        Member member = Member.builder()
                .name(memberName.trim())
                .description(description != null ? description.trim() : null)
                .createdBy(requesterId != null ? requesterId : "SYSTEM")
                .updatedBy(requesterId != null ? requesterId : "SYSTEM")
                .build();
        
        int result = memberMapper.insertMember(member);
        
        if (result <= 0) {
            log.error("회원 등록 실패 - memberName: {}", memberName);
            throw new ApiException(ResponseCode.DATABASE_ERROR, 
                    "회원 등록에 실패했습니다.");
        }
        
        log.info("회원 등록 완료 - id: {}, memberName: {}", member.getId(), memberName);
        
        return convertToDto(member);
    }
    
    /**
     * 회원 정보 수정
     * @param memberId 회원 ID (필수, Body 파라미터)
     * @param memberName 변경될 회원명 (필수, Body 파라미터)
     * @param description 변경될 설명 (선택, Body 파라미터)
     * @param requesterId 요청자 ID (감시용)
     * @return 수정된 회원 DTO
     */
    public MbrDTO updateMember(Integer memberId, String memberName, 
                               String description, String requesterId) {
        // 입력값 검증
        if (memberId == null || memberId <= 0) {
            log.warn("유효하지 않은 회원 ID: {}", memberId);
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "회원 ID는 양수여야 합니다.");
        }
        
        if (memberName == null || memberName.trim().isEmpty()) {
            log.warn("회원명이 비어있음");
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "회원명은 필수 입력값입니다.");
        }
        
        if (memberName.length() > 100) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "회원명은 100자 이하여야 합니다.");
        }
        
        if (description != null && description.length() > 255) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "설명은 255자 이하여야 합니다.");
        }
        
        log.info("회원 수정 시작 - memberId: {}, memberName: {}, requesterId: {}", 
                memberId, memberName, requesterId);
        
        // 기존 회원 확인
        Member existing = memberMapper.selectMemberById(memberId)
                .orElseThrow(() -> {
                    log.warn("수정할 회원을 찾을 수 없음 - memberId: {}", memberId);
                    return new ApiException(ResponseCode.NOT_FOUND, 
                            "회원을 찾을 수 없습니다.");
                });
        
        // 엔티티 업데이트
        Member member = Member.builder()
                .id(memberId)
                .name(memberName.trim())
                .description(description != null ? description.trim() : null)
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt())
                .updatedBy(requesterId != null ? requesterId : "SYSTEM")
                .build();
        
        int result = memberMapper.updateMember(member);
        
        if (result <= 0) {
            log.error("회원 수정 실패 - memberId: {}", memberId);
            throw new ApiException(ResponseCode.DATABASE_ERROR, 
                    "회원 수정에 실패했습니다.");
        }
        
        log.info("회원 수정 완료 - memberId: {}", memberId);
        
        // 수정된 회원 재조회
        return getMemberById(memberId);
    }
    
    /**
     * 회원 삭제
     * @param memberId 회원 ID (필수, 쿼리 파라미터)
     * @param requesterId 요청자 ID (감시용)
     */
    public void deleteMember(Integer memberId, String requesterId) {
        // 입력값 검증
        if (memberId == null || memberId <= 0) {
            log.warn("유효하지 않은 회원 ID: {}", memberId);
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "회원 ID는 양수여야 합니다.");
        }
        
        log.info("회원 삭제 시작 - memberId: {}, requesterId: {}", memberId, requesterId);
        
        // 기존 회원 확인
        memberMapper.selectMemberById(memberId)
                .orElseThrow(() -> {
                    log.warn("삭제할 회원을 찾을 수 없음 - memberId: {}", memberId);
                    return new ApiException(ResponseCode.NOT_FOUND, 
                            "회원을 찾을 수 없습니다.");
                });
        
        int result = memberMapper.deleteMemberById(memberId);
        
        if (result <= 0) {
            log.error("회원 삭제 실패 - memberId: {}", memberId);
            throw new ApiException(ResponseCode.DATABASE_ERROR, 
                    "회원 삭제에 실패했습니다.");
        }
        
        log.info("회원 삭제 완료 - memberId: {}", memberId);
    }
    
    /**
     * 엔티티를 DTO로 변환
     */
    private MbrDTO convertToDto(Member member) {
        return MbrDTO.builder()
                .memberId(member.getId())
                .memberName(member.getName())
                .description(member.getDescription())
                .createdBy(member.getCreatedBy())
                .createdAt(member.getCreatedAt())
                .updatedBy(member.getUpdatedBy())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
