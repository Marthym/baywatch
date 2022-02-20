package fr.ght1pc9kc.baywatch.notify.infra;

import fr.ght1pc9kc.baywatch.notify.api.NotifyService;
import fr.ght1pc9kc.baywatch.notify.domain.NotifyServiceImpl;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Service;

@Service
public class NotifyServiceAdapter implements NotifyService {
    @Delegate
    private final NotifyService delegate;

    public NotifyServiceAdapter() {
        this.delegate = new NotifyServiceImpl();
    }
}
