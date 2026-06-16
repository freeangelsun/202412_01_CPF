package cpf.mbr.bse.service;

import cpf.mbr.common.exception.ApiException;
import cpf.mbr.common.response.ResponseCode;
import cpf.mbr.bse.dto.MbrDTO;
import cpf.mbr.bse.entity.Member;
import cpf.mbr.bse.mapper.MemberMapper;
import cpf.pfw.common.exception.FpsNotFoundException;
import cpf.pfw.common.exception.FpsValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ?뚯썝 ?쒕퉬??
 * - ?뚯썝 議고쉶, ?깅줉, ?섏젙, ??젣 鍮꾩쫰?덉뒪 濡쒖쭅
 * - ?곗씠??寃利?諛?蹂??
 * - ?몃옖??뀡 愿由?
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
     * ?꾩껜 ?뚯썝 紐⑸줉 議고쉶
     * @return ?뚯썝 DTO 紐⑸줉
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public List<MbrDTO> getAllMembers() {
        log.info("?꾩껜 ?뚯썝 紐⑸줉 議고쉶 ?쒖옉");
        
        List<Member> members = memberMapper.selectAllMembers();
        
        if (members.isEmpty()) {
            log.info("議고쉶???뚯썝 ?놁쓬");
        } else {
            log.info("議고쉶???뚯썝 ?? {}", members.size());
        }
        
        return members.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * ?뚯썝 ID濡??곸꽭 議고쉶
     * @param memberId ?뚯썝 ID (?꾩닔, 荑쇰━ ?뚮씪誘명꽣)
     * @return ?뚯썝 DTO
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public MbrDTO getMemberById(Integer memberId) {
        // ?낅젰媛?寃利?
        if (memberId == null || memberId <= 0) {
            log.warn("?좏슚?섏? ?딆? ?뚯썝 ID: {}", memberId);
            // PFW ?쒖? ?덉쇅 ?섑뵆?낅땲?? 怨좉컼??硫붿떆吏??硫붿떆吏 ?뚯씠釉붿쓽 EXTERNAL 臾멸뎄瑜??ъ슜?섍퀬,
            // "memberId: ..." ?곸꽭 ?댁슜? ?대? 硫붿떆吏/DB 濡쒓렇/?뚯씪 濡쒓렇 異붿쟻?⑹쑝濡쒕쭔 ?쒖슜?⑸땲??
            throw new FpsValidationException("memberId???묒닔?ъ빞 ?⑸땲?? memberId=" + memberId);
        }
        
        log.info("?뚯썝 ?곸꽭 議고쉶 - memberId: {}", memberId);
        
        Member member = memberMapper.selectMemberById(memberId)
                .orElseThrow(() -> {
                    log.warn("?뚯썝??李얠쓣 ???놁쓬 - memberId: {}", memberId);
                    // 議고쉶 ????놁쓬???쒖? ?덉쇅濡??섏?硫?PFW媛 HTTP 404, ?ㅻ쪟肄붾뱶, 硫붿떆吏 ?ㅻ뜑瑜??먮룞 援ъ꽦?⑸땲??
                    return new FpsNotFoundException("?뚯썝 湲곕낯?뺣낫媛 議댁옱?섏? ?딆뒿?덈떎. memberId=" + memberId);
                });
        
        return convertToDto(member);
    }
    
    /**
     * ?뚯썝紐낆쑝濡?寃??議고쉶
     * @param name ?뚯썝紐?寃???ㅼ썙??(?꾩닔, 荑쇰━ ?뚮씪誘명꽣)
     * @return ?뚯썝 DTO 紐⑸줉
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public List<MbrDTO> searchMembersByName(String name) {
        // ?낅젰媛?寃利?
        if (name == null || name.trim().isEmpty()) {
            log.warn("?뚯썝紐?寃???ㅼ썙?쒓? 鍮꾩뼱?덉쓬");
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?뚯썝紐낆? ?꾩닔 ?낅젰媛믪엯?덈떎.");
        }
        
        if (name.length() > 100) {
            log.warn("?뚯썝紐?寃???ㅼ썙??湲몄씠 珥덇낵: {}", name.length());
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?뚯썝紐낆? 100???댄븯?ъ빞 ?⑸땲??");
        }
        
        log.info("?뚯썝紐?寃??- keyword: {}", name);
        
        List<Member> members = memberMapper.selectMembersByName(name);
        
        log.info("Member search result count: {}", members.size());
        
        return members.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * ?뚯썝 ?깅줉
     * @param memberName ?뚯썝紐?(?꾩닔, Body ?뚮씪誘명꽣)
     * @param description ?뚯썝 ?ㅻ챸 (?좏깮, Body ?뚮씪誘명꽣)
     * @param requesterId ?붿껌??ID (媛먯떆??
     * @return ?깅줉???뚯썝 DTO
     */
    public MbrDTO createMember(String memberName, String description, String requesterId) {
        // ?낅젰媛?寃利?
        if (memberName == null || memberName.trim().isEmpty()) {
            log.warn("?뚯썝紐낆씠 鍮꾩뼱?덉쓬");
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?뚯썝紐낆? ?꾩닔 ?낅젰媛믪엯?덈떎.");
        }
        
        if (memberName.length() > 100) {
            log.warn("?뚯썝紐?湲몄씠 珥덇낵: {}", memberName.length());
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?뚯썝紐낆? 100???댄븯?ъ빞 ?⑸땲??");
        }
        
        if (description != null && description.length() > 255) {
            log.warn("?ㅻ챸 湲몄씠 珥덇낵: {}", description.length());
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?ㅻ챸? 255???댄븯?ъ빞 ?⑸땲??");
        }
        
        log.info("?뚯썝 ?깅줉 ?쒖옉 - memberName: {}, requesterId: {}", memberName, requesterId);
        
        // ?뷀떚???앹꽦 諛??깅줉
        Member member = Member.builder()
                .name(memberName.trim())
                .description(description != null ? description.trim() : null)
                .createdBy(requesterId != null ? requesterId : "SYSTEM")
                .updatedBy(requesterId != null ? requesterId : "SYSTEM")
                .build();
        
        int result = memberMapper.insertMember(member);
        
        if (result <= 0) {
            log.error("?뚯썝 ?깅줉 ?ㅽ뙣 - memberName: {}", memberName);
            throw new ApiException(ResponseCode.DATABASE_ERROR, 
                    "?뚯썝 ?깅줉???ㅽ뙣?덉뒿?덈떎.");
        }
        
        log.info("?뚯썝 ?깅줉 ?꾨즺 - id: {}, memberName: {}", member.getId(), memberName);
        
        return convertToDto(member);
    }
    
    /**
     * ?뚯썝 ?뺣낫 ?섏젙
     * @param memberId ?뚯썝 ID (?꾩닔, Body ?뚮씪誘명꽣)
     * @param memberName 蹂寃쎈맆 ?뚯썝紐?(?꾩닔, Body ?뚮씪誘명꽣)
     * @param description 蹂寃쎈맆 ?ㅻ챸 (?좏깮, Body ?뚮씪誘명꽣)
     * @param requesterId ?붿껌??ID (媛먯떆??
     * @return ?섏젙???뚯썝 DTO
     */
    public MbrDTO updateMember(Integer memberId, String memberName, 
                               String description, String requesterId) {
        // ?낅젰媛?寃利?
        if (memberId == null || memberId <= 0) {
            log.warn("?좏슚?섏? ?딆? ?뚯썝 ID: {}", memberId);
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?뚯썝 ID???묒닔?ъ빞 ?⑸땲??");
        }
        
        if (memberName == null || memberName.trim().isEmpty()) {
            log.warn("?뚯썝紐낆씠 鍮꾩뼱?덉쓬");
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?뚯썝紐낆? ?꾩닔 ?낅젰媛믪엯?덈떎.");
        }
        
        if (memberName.length() > 100) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?뚯썝紐낆? 100???댄븯?ъ빞 ?⑸땲??");
        }
        
        if (description != null && description.length() > 255) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?ㅻ챸? 255???댄븯?ъ빞 ?⑸땲??");
        }
        
        log.info("?뚯썝 ?섏젙 ?쒖옉 - memberId: {}, memberName: {}, requesterId: {}", 
                memberId, memberName, requesterId);
        
        // 湲곗〈 ?뚯썝 ?뺤씤
        Member existing = memberMapper.selectMemberById(memberId)
                .orElseThrow(() -> {
                    log.warn("?섏젙???뚯썝??李얠쓣 ???놁쓬 - memberId: {}", memberId);
                    return new ApiException(ResponseCode.NOT_FOUND, 
                            "?뚯썝??李얠쓣 ???놁뒿?덈떎.");
                });
        
        // ?뷀떚???낅뜲?댄듃
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
            log.error("?뚯썝 ?섏젙 ?ㅽ뙣 - memberId: {}", memberId);
            throw new ApiException(ResponseCode.DATABASE_ERROR, 
                    "?뚯썝 ?섏젙???ㅽ뙣?덉뒿?덈떎.");
        }
        
        log.info("?뚯썝 ?섏젙 ?꾨즺 - memberId: {}", memberId);
        
        // ?섏젙???뚯썝 ?ъ“??
        return getMemberById(memberId);
    }
    
    /**
     * ?뚯썝 ??젣
     * @param memberId ?뚯썝 ID (?꾩닔, 荑쇰━ ?뚮씪誘명꽣)
     * @param requesterId ?붿껌??ID (媛먯떆??
     */
    public void deleteMember(Integer memberId, String requesterId) {
        // ?낅젰媛?寃利?
        if (memberId == null || memberId <= 0) {
            log.warn("?좏슚?섏? ?딆? ?뚯썝 ID: {}", memberId);
            throw new ApiException(ResponseCode.INVALID_PARAMETER, 
                    "?뚯썝 ID???묒닔?ъ빞 ?⑸땲??");
        }
        
        log.info("?뚯썝 ??젣 ?쒖옉 - memberId: {}, requesterId: {}", memberId, requesterId);
        
        // 湲곗〈 ?뚯썝 ?뺤씤
        memberMapper.selectMemberById(memberId)
                .orElseThrow(() -> {
                    log.warn("??젣???뚯썝??李얠쓣 ???놁쓬 - memberId: {}", memberId);
                    return new ApiException(ResponseCode.NOT_FOUND, 
                            "?뚯썝??李얠쓣 ???놁뒿?덈떎.");
                });
        
        int result = memberMapper.deleteMemberById(memberId);
        
        if (result <= 0) {
            log.error("?뚯썝 ??젣 ?ㅽ뙣 - memberId: {}", memberId);
            throw new ApiException(ResponseCode.DATABASE_ERROR, 
                    "?뚯썝 ??젣???ㅽ뙣?덉뒿?덈떎.");
        }
        
        log.info("?뚯썝 ??젣 ?꾨즺 - memberId: {}", memberId);
    }
    
    /**
     * ?뷀떚?곕? DTO濡?蹂??
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

