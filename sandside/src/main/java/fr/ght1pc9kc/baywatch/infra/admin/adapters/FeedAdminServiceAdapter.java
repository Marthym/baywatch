package fr.ght1pc9kc.baywatch.infra.admin.adapters;

import fr.ght1pc9kc.baywatch.api.admin.FeedAdminService;
import fr.ght1pc9kc.baywatch.domain.admin.FeedAdminServiceImpl;
import fr.ght1pc9kc.baywatch.domain.admin.ports.FeedAdministrationPort;
import fr.ght1pc9kc.baywatch.domain.ports.AuthenticationFacade;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class FeedAdminServiceAdapter implements FeedAdminService {
    @Delegate
    private FeedAdminService delegate;

    public FeedAdminServiceAdapter(FeedAdministrationPort feedAdminPort, AuthenticationFacade authentFacade) {
        this.delegate = new FeedAdminServiceImpl(feedAdminPort, authentFacade);
    }
}
