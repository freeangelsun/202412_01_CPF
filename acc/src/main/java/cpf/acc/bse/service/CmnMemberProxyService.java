package cpf.acc.bse.service;

import cpf.cmn.smp.entity.Member;
import cpf.cmn.smp.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CMN 紐⑤뱢??MemberService瑜??꾨줉?쒗븯??ACC 紐⑤뱢?먯꽌 ?ъ슜?⑸땲??
 */
@Service
@RequiredArgsConstructor
public class CmnMemberProxyService {

    private final MemberService memberService;

    /**
     * CMN 紐⑤뱢??MemberService瑜??몄텧?섏뿬 紐⑤뱺 ?뚯썝 ?뺣낫瑜?議고쉶?⑸땲??
     * @return List<Member>
     */
    public List<Member> getAllMembersFromCMN() {
        return memberService.getAllMembers();
    }
}

