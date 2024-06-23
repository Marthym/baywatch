package fr.ght1pc9kc.baywatch.common.infra.filters;

import fr.ght1pc9kc.baywatch.common.api.model.ClientInfoContext;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.net.InetSocketAddress;
import java.util.function.Function;

@UtilityClass
public class ReactiveClientInfoContextHolder {
    private static final Class<?> CLIENT_INFO_CONTEXT_KEY = ClientInfoContext.class;

    public static Mono<ClientInfoContext> getContext() {
        return Mono.deferContextual(Mono::just).cast(Context.class)
                .filter(ReactiveClientInfoContextHolder::hasClientContext)
                .flatMap(ReactiveClientInfoContextHolder::getClientInfoContext);
    }

    private static boolean hasClientContext(Context context) {
        return context.hasKey(CLIENT_INFO_CONTEXT_KEY);
    }

    private static Mono<ClientInfoContext> getClientInfoContext(Context context) {
        return context.<Mono<ClientInfoContext>>get(CLIENT_INFO_CONTEXT_KEY);
    }

    public static Function<Context, Context> clearContext() {
        return context -> context.delete(CLIENT_INFO_CONTEXT_KEY);
    }

    public static Context withClientInfoContext(Mono<ClientInfoContext> clientInfoContext) {
        return Context.of(CLIENT_INFO_CONTEXT_KEY, clientInfoContext);
    }

    public static Context withClientInfo(InetSocketAddress ip, String userAgent) {
        return withClientInfoContext(Mono.just(new ClientInfoContext(ip, userAgent)));
    }
}
