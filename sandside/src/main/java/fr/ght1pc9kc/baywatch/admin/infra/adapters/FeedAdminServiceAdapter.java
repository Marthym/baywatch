package fr.ght1pc9kc.baywatch.admin.infra.adapters;

import fr.ght1pc9kc.baywatch.admin.api.FeedAdminService;
import fr.ght1pc9kc.baywatch.admin.domain.FeedAdminServiceImpl;
import fr.ght1pc9kc.baywatch.security.domain.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.techwatch.infra.persistence.FeedRepository;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class FeedAdminServiceAdapter implements FeedAdminService {
    @Delegate
    private final FeedAdminService delegate;

    public FeedAdminServiceAdapter(FeedRepository feedRepository, AuthenticationFacade authentFacade) {
        this.delegate = new FeedAdminServiceImpl(feedRepository, authentFacade);
    }
}
