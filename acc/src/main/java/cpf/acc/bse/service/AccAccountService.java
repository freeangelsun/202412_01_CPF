package cpf.acc.bse.service;

import cpf.acc.bse.entity.AccAccount;
import cpf.acc.bse.mapper.AccAccountMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccAccountService {

    private final AccAccountMapper accAccountMapper;

    public AccAccountService(AccAccountMapper accAccountMapper) {
        this.accAccountMapper = accAccountMapper;
    }

    public List<AccAccount> getAllAccounts() {
        return accAccountMapper.selectAllAccounts();
    }
}
