package cpf.cmn.utils;

import cpf.cmn.dto.HeaderDTO;

/**
 * 怨듯넻 ?좏슚??寃???좏떥由ы떚.
 * HeaderDTO 媛앹껜???좏슚?깆쓣 ?뺤씤?⑸땲??
 */
public class ValidationUtils {

    /**
     * HeaderDTO???꾩닔 ?꾨뱶瑜?寃?ы빀?덈떎.
     *
     * @param header 寃?ы븷 HeaderDTO 媛앹껜
     * @throws IllegalArgumentException ?꾨뱶 媛믪씠 ?좏슚?섏? ?딆쓣 寃쎌슦 ?덉쇅 諛쒖깮
     */
    public static void validateHeader(HeaderDTO header) {
        if (header.getTransactionId() == null || header.getTransactionId().isEmpty()) {
            throw new IllegalArgumentException("Transaction ID???꾩닔 媛믪엯?덈떎.");
        }
        if (header.getInitialChannelCode() == null || header.getInitialChannelCode().isEmpty()) {
            throw new IllegalArgumentException("Initial Channel Code???꾩닔 媛믪엯?덈떎.");
        }
        if (header.getChannelCode() == null || header.getChannelCode().isEmpty()) {
            throw new IllegalArgumentException("Channel Code???꾩닔 媛믪엯?덈떎.");
        }
        if (header.getTimestamp() == null) {
            throw new IllegalArgumentException("Timestamp???꾩닔 媛믪엯?덈떎.");
        }
    }
}

