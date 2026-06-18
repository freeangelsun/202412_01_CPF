package cpf.cmn.mqe.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.exception.CpfSystemException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 硫붿떆吏 Payload? 怨좎젙湲몄씠 ?꾨Ц 蹂??寃곌낵瑜?JSON?쇰줈 蹂?섑븯??怨듯넻 Codec?낅땲??
 */
@Component
public class CmnMessageCodec {
    private final ObjectMapper objectMapper;

    public CmnMessageCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 媛앹껜瑜?JSON 臾몄옄?대줈 蹂?섑빀?덈떎.
     *
     * @param value 蹂?섑븷 媛앹껜
     * @return JSON 臾몄옄??     */
    public String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new CpfSystemException("硫붿떆吏 JSON 吏곷젹?붿뿉 ?ㅽ뙣?덉뒿?덈떎.", ex);
        }
    }

    /**
     * JSON 臾몄옄?댁쓣 DTO濡?蹂?섑빀?덈떎.
     *
     * @param json JSON 臾몄옄??     * @param type 蹂????????     * @param <T>  蹂??????쒕꽕由????     * @return 蹂?섎맂 DTO
     */
    public <T> T fromJson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new CpfSystemException("硫붿떆吏 JSON ??쭅?ы솕???ㅽ뙣?덉뒿?덈떎.", ex);
        }
    }

    /**
     * 媛앹껜瑜?Map?쇰줈 蹂?섑빀?덈떎.
     *
     * @param value 蹂?섑븷 媛앹껜
     * @return Map ?쒗쁽
     */
    public Map<String, Object> toMap(Object value) {
        return objectMapper.convertValue(value, new TypeReference<>() {
        });
    }
}

