package cpf.mbr.bse.service;

import cpf.cmn.utils.TextUtils;
import cpf.mbr.bse.dto.MbrDTO;
import cpf.mbr.bse.entity.Member;
import cpf.mbr.bse.mapper.MemberMapper;
import cpf.mbr.common.exception.ApiException;
import cpf.mbr.common.response.ResponseCode;
import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.pfw.common.exception.CpfValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MBR 회원 샘플 서비스입니다.
 *
 * <p>개발자 교육용 CRUD 흐름과 ADM 회원 운영 화면에서 공통으로 사용할 수 있도록
 * 회원 기본 정보, 상태, 잠금, 탈퇴 여부를 함께 관리합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(transactionManager = "mbrTransactionManager")
public class MbrService {
    private static final DateTimeFormatter MEMBER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MemberMapper memberMapper;

    /** 전체 회원 목록을 조회합니다. */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public List<MbrDTO> getAllMembers() {
        List<Member> members = memberMapper.selectAllMembers();
        log.info("MBR 회원 목록 조회 완료. count={}", members.size());
        return members.stream().map(this::convertToDto).toList();
    }

    /** 회원 내부 순번으로 상세 정보를 조회합니다. */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public MbrDTO getMemberById(Integer memberId) {
        if (memberId == null || memberId <= 0) {
            throw new CpfValidationException("memberId는 양수여야 합니다. memberId=" + memberId);
        }

        Member member = memberMapper.selectMemberById(memberId)
                .orElseThrow(() -> new CpfNotFoundException("회원을 찾을 수 없습니다. memberId=" + memberId));
        return convertToDto(member);
    }

    /** 회원명 일부로 회원을 검색합니다. */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public List<MbrDTO> searchMembersByName(String name) {
        if (!TextUtils.hasText(name)) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, "회원명 검색어는 필수입니다.");
        }
        if (name.length() > 100) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, "회원명 검색어는 100자 이하여야 합니다.");
        }

        List<Member> members = memberMapper.selectMembersByName(name.trim());
        log.info("MBR 회원 검색 완료. keyword={}, count={}", name, members.size());
        return members.stream().map(this::convertToDto).toList();
    }

    /** 기존 샘플 API 호환용 회원 생성 메서드입니다. */
    public MbrDTO createMember(String memberName, String description, String requesterId) {
        return createMember(MbrDTO.builder()
                .memberName(memberName)
                .description(description)
                .build(), requesterId);
    }

    /** 회원을 생성합니다. */
    public MbrDTO createMember(MbrDTO request, String requesterId) {
        validateMemberName(request.getMemberName());
        validateDescription(request.getDescription());

        String user = TextUtils.defaultIfBlank(requesterId, "SYSTEM");
        String memberNo = TextUtils.defaultIfBlank(request.getMemberNo(), generateMemberNo());
        String loginId = TextUtils.defaultIfBlank(request.getLoginId(), memberNo.toLowerCase());

        Member member = Member.builder()
                .memberNo(memberNo)
                .customerNo(TextUtils.defaultIfBlank(request.getCustomerNo(), "C" + memberNo.substring(1)))
                .loginId(loginId)
                .loginFailCount(0)
                .passwordChangeRequiredYn("N")
                .name(request.getMemberName().trim())
                .email(blankToNull(request.getEmail()))
                .mobileNo(blankToNull(request.getMobileNo()))
                .memberStatus(TextUtils.defaultIfBlank(request.getMemberStatus(), "ACTIVE"))
                .lockYn(yn(request.getLockYn(), "N"))
                .withdrawYn(yn(request.getWithdrawYn(), "N"))
                .channelCode(TextUtils.defaultIfBlank(request.getChannelCode(), "WEB"))
                .joinedAt(request.getJoinedAt())
                .lastLoginAt(request.getLastLoginAt())
                .description(blankToNull(request.getDescription()))
                .createdBy(user)
                .updatedBy(user)
                .build();

        int result = memberMapper.insertMember(member);
        if (result <= 0) {
            throw new ApiException(ResponseCode.DATABASE_ERROR, "회원 등록에 실패했습니다.");
        }
        return convertToDto(member);
    }

    /** 기존 샘플 API 호환용 회원 수정 메서드입니다. */
    public MbrDTO updateMember(Integer memberId, String memberName, String description, String requesterId) {
        MbrDTO existing = getMemberById(memberId);
        existing.setMemberName(memberName);
        existing.setDescription(description);
        return updateMember(existing, requesterId);
    }

    /** 회원 기본 정보를 수정합니다. */
    public MbrDTO updateMember(MbrDTO request, String requesterId) {
        if (request.getMemberId() == null || request.getMemberId() <= 0) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, "회원 ID는 필수입니다.");
        }
        validateMemberName(request.getMemberName());
        validateDescription(request.getDescription());

        Member existing = memberMapper.selectMemberById(request.getMemberId())
                .orElseThrow(() -> new ApiException(ResponseCode.NOT_FOUND, "회원을 찾을 수 없습니다."));
        String user = TextUtils.defaultIfBlank(requesterId, "SYSTEM");

        Member member = Member.builder()
                .id(request.getMemberId())
                .memberNo(TextUtils.defaultIfBlank(request.getMemberNo(), existing.getMemberNo()))
                .customerNo(firstText(request.getCustomerNo(), existing.getCustomerNo()))
                .loginId(TextUtils.defaultIfBlank(request.getLoginId(), existing.getLoginId()))
                .passwordHash(existing.getPasswordHash())
                .loginFailCount(existing.getLoginFailCount())
                .passwordChangeRequiredYn(existing.getPasswordChangeRequiredYn())
                .passwordExpireAt(existing.getPasswordExpireAt())
                .name(request.getMemberName().trim())
                .email(firstText(request.getEmail(), existing.getEmail()))
                .mobileNo(firstText(request.getMobileNo(), existing.getMobileNo()))
                .memberStatus(TextUtils.defaultIfBlank(request.getMemberStatus(), existing.getMemberStatus()))
                .lockYn(yn(firstText(request.getLockYn(), existing.getLockYn()), "N"))
                .withdrawYn(yn(firstText(request.getWithdrawYn(), existing.getWithdrawYn()), "N"))
                .channelCode(TextUtils.defaultIfBlank(request.getChannelCode(), existing.getChannelCode()))
                .joinedAt(existing.getJoinedAt())
                .lastLoginAt(request.getLastLoginAt() != null ? request.getLastLoginAt() : existing.getLastLoginAt())
                .description(blankToNull(request.getDescription()))
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt())
                .updatedBy(user)
                .build();

        int result = memberMapper.updateMember(member);
        if (result <= 0) {
            throw new ApiException(ResponseCode.DATABASE_ERROR, "회원 수정에 실패했습니다.");
        }
        return getMemberById(request.getMemberId());
    }

    /** 샘플 API의 물리 삭제 기능입니다. 운영 화면에서는 탈퇴 상태 변경을 우선 사용합니다. */
    public void deleteMember(Integer memberId, String requesterId) {
        if (memberId == null || memberId <= 0) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, "회원 ID는 필수입니다.");
        }
        memberMapper.selectMemberById(memberId)
                .orElseThrow(() -> new ApiException(ResponseCode.NOT_FOUND, "회원을 찾을 수 없습니다."));

        int result = memberMapper.deleteMemberById(memberId);
        if (result <= 0) {
            throw new ApiException(ResponseCode.DATABASE_ERROR, "회원 삭제에 실패했습니다.");
        }
        log.info("MBR 회원 삭제 완료. memberId={}, requesterId={}", memberId, requesterId);
    }

    private void validateMemberName(String memberName) {
        if (!TextUtils.hasText(memberName)) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, "회원명은 필수입니다.");
        }
        if (memberName.length() > 100) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, "회원명은 100자 이하여야 합니다.");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.length() > 255) {
            throw new ApiException(ResponseCode.INVALID_PARAMETER, "설명은 255자 이하여야 합니다.");
        }
    }

    private String generateMemberNo() {
        return "M" + LocalDateTime.now().format(MEMBER_NO_TIME) + (System.nanoTime() % 1000);
    }

    private String blankToNull(String value) {
        return TextUtils.hasText(value) ? value.trim() : null;
    }

    private String firstText(String value, String fallback) {
        return TextUtils.hasText(value) ? value.trim() : fallback;
    }

    private String yn(String value, String fallback) {
        String normalized = TextUtils.defaultIfBlank(value, fallback).trim().toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }

    private MbrDTO convertToDto(Member member) {
        return MbrDTO.builder()
                .memberId(member.getId())
                .memberNo(member.getMemberNo())
                .customerNo(member.getCustomerNo())
                .loginId(member.getLoginId())
                .memberName(member.getName())
                .email(member.getEmail())
                .mobileNo(member.getMobileNo())
                .memberStatus(member.getMemberStatus())
                .lockYn(member.getLockYn())
                .withdrawYn(member.getWithdrawYn())
                .channelCode(member.getChannelCode())
                .joinedAt(member.getJoinedAt())
                .lastLoginAt(member.getLastLoginAt())
                .description(member.getDescription())
                .createdBy(member.getCreatedBy())
                .createdAt(member.getCreatedAt())
                .updatedBy(member.getUpdatedBy())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
