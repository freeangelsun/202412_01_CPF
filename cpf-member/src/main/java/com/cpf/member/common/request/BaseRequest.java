package cpf.mbr.common.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * 
 * @author CPF Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @JsonIgnore
    private String requesterId;
    
    /**
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @JsonIgnore
    private String channel;
    
    /**
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @JsonIgnore
    private String clientIp;
    
    /**
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @JsonIgnore
    private String userAgent;
}

