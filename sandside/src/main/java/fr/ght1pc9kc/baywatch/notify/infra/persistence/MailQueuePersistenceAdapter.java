package fr.ght1pc9kc.baywatch.notify.infra.persistence;

import fr.ght1pc9kc.baywatch.notify.domain.model.Mail;
import fr.ght1pc9kc.baywatch.notify.domain.ports.MailQueuePersistencePort;
import fr.ght1pc9kc.entity.api.Entity;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Repository
public class MailQueuePersistenceAdapter implements MailQueuePersistencePort {
    private final Queue<Entity<Mail>> queue = new ConcurrentLinkedQueue<>();

    @Override
    public Mono<Void> push(Entity<Mail> mail) {
        return Mono.create(sink -> {
            try {
                if (queue.offer(mail)) {
                    sink.success();
                } else {
                    sink.error(new IllegalStateException("Queue is full"));
                }
            } catch (Exception e) {
                sink.error(e);
            }
        }).then();
    }

    @Override
    public Flux<Entity<Mail>> consume() {
        return Flux.create(sink -> sink.onRequest(n -> {
            long count = n;
            while (count > 0 && !queue.isEmpty()) {
                sink.next(queue.poll());
                --count;
            }
            sink.complete();
        }));
    }
}
