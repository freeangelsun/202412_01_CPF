package fps.mbr.bse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 회원 Data Transfer Object
 * - API 요청/응답에서 사용되는 데이터 객체
 * - 입력값 검증 규칙 포함
 * 
 * 요청 DTO:
 *   - createMember(): name, description만 필수
 *   - updateMember(): id, name, description 필수
 * 
 * 응답 DTO:
 *   - 모든 필드 포함 (감시 필드 포함)
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MbrDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // ==================== 요청/응답 공통 필드 ====================
    
    /** 회원 ID */
    private Integer memberId;
    
    /** 회원명 (필수, 1-100자) */
    @NotBlank(message = "회원명은 필수 입력값입니다.", groups = CreateGroup.class)
    @Size(min = 1, max = 100, message = "회원명은 1-100자 범위여야 합니다.", groups = CreateGroup.class)
    private String memberName;
    
    /** 회원 설명 */
    @Size(max = 255, message = "설명은 255자 이하여야 합니다.")
    private String description;
    
    // ==================== 응답 전용 필드 ====================
    
    /** 생성자 ID (감시용) */
    private String createdBy;
    
    /** 생성 일시 */
    private LocalDateTime createdAt;
    
    /** 수정자 ID (감시용) */
    private String updatedBy;
    
    /** 수정 일시 */
    private LocalDateTime updatedAt;
    
    // ==================== 검증 그룹 ====================
    
    /**
     * 생성 요청 검증 그룹
     * POST /mbr/create 에서만 사용
     */
    public interface CreateGroup {
    }
    
    /**
     * 수정 요청 검증 그룹
     * PUT /mbr/update 에서만 사용
     */
    public interface UpdateGroup {
    }
}
