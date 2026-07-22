package cpf.acc.account.facade;

import cpf.acc.account.dto.AccAccountResponse;
import cpf.acc.account.port.AccAccountRepository;
import cpf.cmn.api.account.AccountSummary;
import cpf.cmn.api.account.AccountSummaryFacade;
import cpf.pfw.common.exception.CpfNotFoundException;
import org.springframework.stereotype.Component;

/** ACC 소유 Repository를 통해 공유 계약을 구현하는 Local Facade Adapter입니다. */
@Component
public class AccAccountSummaryFacade implements AccountSummaryFacade {
    private final AccAccountRepository repository;

    public AccAccountSummaryFacade(AccAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountSummary findSummary(long accountId) {
        AccAccountResponse account = repository.find(accountId)
                .orElseThrow(() -> new CpfNotFoundException("ACC 계정을 찾을 수 없습니다. accountId=" + accountId));
        return new AccountSummary(
                account.accountId(), account.accountNo(), account.accountName(), account.statusCode(), account.version());
    }
}
