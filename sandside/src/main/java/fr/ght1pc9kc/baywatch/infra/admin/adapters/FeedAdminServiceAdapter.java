package fr.ght1pc9kc.baywatch.infra.admin.adapters;

import fr.ght1pc9kc.baywatch.api.admin.FeedAdminService;
import fr.ght1pc9kc.baywatch.domain.admin.FeedAdminServiceImpl;
import fr.ght1pc9kc.baywatch.domain.security.ports.AuthenticationFacade;
import fr.ght1pc9kc.baywatch.infra.techwatch.persistence.FeedRepository;
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
