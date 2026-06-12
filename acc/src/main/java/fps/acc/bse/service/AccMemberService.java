package fps.acc.bse.service;

import fps.acc.bse.entity.AccMember;
import fps.acc.bse.mapper.AccMemberMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccMemberService {

    private final AccMemberMapper accMemberMapper;

    public AccMemberService(AccMemberMapper accMemberMapper) {
        this.accMemberMapper = accMemberMapper;
    }

    public List<AccMember> getAllAccMembers() {
        return accMemberMapper.selectAllMembers();
    }
}
