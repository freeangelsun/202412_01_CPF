package cpf.mbr.bse.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ?뚯썝 ?뷀떚??
 * - ?뚯썝 湲곕낯 ?뺣낫 愿由?
 * - 媛먯떆 ?꾨뱶 ?ы븿: created_at, updated_at, created_by, updated_by
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
    
    /** ?뚯썝 ID (PK) */
    private Integer id;
    
    /** ?뚯썝紐?*/
    private String name;
    
    /** ?뚯썝 ?ㅻ챸 */
    private String description;
    
    /** ?앹꽦??ID (媛먯떆?? */
    private String createdBy;
    
    /** ?앹꽦 ?쇱떆 */
    private LocalDateTime createdAt;
    
    /** ?섏젙??ID (媛먯떆?? */
    private String updatedBy;
    
    /** ?섏젙 ?쇱떆 */
    private LocalDateTime updatedAt;
}

