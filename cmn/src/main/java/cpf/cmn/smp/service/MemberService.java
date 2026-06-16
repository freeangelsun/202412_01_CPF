package cpf.cmn.smp.service;

import cpf.cmn.smp.entity.Member;
import cpf.cmn.smp.mapper.MemberMapper;
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

