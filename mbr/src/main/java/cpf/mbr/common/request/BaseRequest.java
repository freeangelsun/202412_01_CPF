package cpf.mbr.common.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 湲덉쑖沅??쒖? ?붿껌 媛앹껜
 * ⑤뱺 API ?붿껌?먯꽌 怨듯넻?쇰줈 ?ы븿?섏뼱????硫뷀? ?뺣낫 ?뺤쓽
 * 
 * @author FPS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * ?붿껌??ID (媛먯떆/濡쒓퉭??
     * - ?섏쨷??security context?먯꽌 ?먮룞 ?ㅼ젙
     */
    @JsonIgnore
    private String requesterId;
    
    /**
     * ?붿껌 梨꾨꼸 (⑤컮?? WEB, API ??
     * - 湲덉쑖沅?媛먯떆 濡쒓렇???꾩닔
     */
    @JsonIgnore
    private String channel;
    
    /**
     * ?붿껌 IP 二쇱냼
     * - ?묎렐 ?쒖뼱 諛?媛먯떆???ъ슜
     */
    @JsonIgnore
    private String clientIp;
    
    /**
     * ?ъ슜???먯씠?꾪듃
     * - 媛먯떆 諛??듦퀎???ъ슜
     */
    @JsonIgnore
    private String userAgent;
}

