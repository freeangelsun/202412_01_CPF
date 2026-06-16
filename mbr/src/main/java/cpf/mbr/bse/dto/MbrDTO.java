package cpf.mbr.bse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ?뚯썝 Data Transfer Object
 * - API ?붿껌/?묐떟?먯꽌 ?ъ슜?섎뒗 ?곗씠??媛앹껜
 * - ?낅젰媛?寃利?洹쒖튃 ?ы븿
 * 
 * ?붿껌 DTO:
 *   - createMember(): name, description留??꾩닔
 *   - updateMember(): id, name, description ?꾩닔
 * 
 * ?묐떟 DTO:
 *   - 紐⑤뱺 ?꾨뱶 ?ы븿 (媛먯떆 ?꾨뱶 ?ы븿)
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
    
    // ==================== ?붿껌/?묐떟 怨듯넻 ?꾨뱶 ====================
    
    /** ?뚯썝 ID */
    private Integer memberId;
    
    /** ?뚯썝紐?(?꾩닔, 1-100?? */
    @NotBlank(message = "?뚯썝紐낆? ?꾩닔 ?낅젰媛믪엯?덈떎.", groups = CreateGroup.class)
    @Size(min = 1, max = 100, message = "?뚯썝紐낆? 1-100??踰붿쐞?ъ빞 ?⑸땲??", groups = CreateGroup.class)
    private String memberName;
    
    /** ?뚯썝 ?ㅻ챸 */
    @Size(max = 255, message = "?ㅻ챸? 255???댄븯?ъ빞 ?⑸땲??")
    private String description;
    
    // ==================== ?묐떟 ?꾩슜 ?꾨뱶 ====================
    
    /** ?앹꽦??ID (媛먯떆?? */
    private String createdBy;
    
    /** ?앹꽦 ?쇱떆 */
    private LocalDateTime createdAt;
    
    /** ?섏젙??ID (媛먯떆?? */
    private String updatedBy;
    
    /** ?섏젙 ?쇱떆 */
    private LocalDateTime updatedAt;
    
    // ==================== 寃利?洹몃９ ====================
    
    /**
     * ?앹꽦 ?붿껌 寃利?洹몃９
     * POST /mbr/create ?먯꽌留??ъ슜
     */
    public interface CreateGroup {
    }
    
    /**
     * ?섏젙 ?붿껌 寃利?洹몃９
     * PUT /mbr/update ?먯꽌留??ъ슜
     */
    public interface UpdateGroup {
    }
}

