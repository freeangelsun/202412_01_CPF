package fps.mbr.bse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 회원 엔티티
 * - 회원 기본 정보 관리
 * - 감시 필드 포함: created_at, updated_at, created_by, updated_by
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** 회원 ID (PK) */
    private Integer id;
    
    /** 회원명 */
    private String name;
    
    /** 회원 설명 */
    private String description;
    
    /** 생성자 ID (감시용) */
    private String createdBy;
    
    /** 생성 일시 */
    private LocalDateTime createdAt;
    
    /** 수정자 ID (감시용) */
    private String updatedBy;
    
    /** 수정 일시 */
    private LocalDateTime updatedAt;
}
