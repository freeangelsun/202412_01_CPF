package fps.acc.bse.service;

import fps.cmn.smp.entity.Member;
import fps.cmn.smp.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CMN 모듈의 MemberService를 프록시하여 ACC 모듈에서 사용합니다.
 */
@Service
@RequiredArgsConstructor
public class CmnMemberProxyService {

    private final MemberService memberService;

    /**
     * CMN 모듈의 MemberService를 호출하여 모든 회원 정보를 조회합니다.
     * @return List<Member>
     */
    public List<Member> getAllMembersFromCMN() {
        return memberService.getAllMembers();
    }
}
