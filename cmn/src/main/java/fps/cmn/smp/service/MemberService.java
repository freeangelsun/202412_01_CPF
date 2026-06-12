package fps.cmn.smp.service;

import fps.cmn.smp.entity.Member;
import fps.cmn.smp.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;

    public List<Member> getAllMembers() {
        return memberMapper.selectAllMembers();
    }
}
