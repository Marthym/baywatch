package fr.ght1pc9kc.baywatch.common.infra.filters;

import lombok.experimental.UtilityClass;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Locale;
import java.util.function.Function;

@UtilityClass
public class ReactiveLocaleContextHolder {
    private static final Class<?> LOCALE_CONTEXT_KEY = LocaleContext.class;

    public static Mono<LocaleContext> getContext() {
        return Mono.deferContextual(Mono::just).cast(Context.class)
                .filter(ReactiveLocaleContextHolder::hasLocaleContext)
                .flatMap(ReactiveLocaleContextHolder::getLocaleContext);
    }

    private static boolean hasLocaleContext(Context context) {
        return context.hasKey(LOCALE_CONTEXT_KEY);
    }

    private static Mono<LocaleContext> getLocaleContext(Context context) {
        return context.<Mono<LocaleContext>>get(LOCALE_CONTEXT_KEY);
    }

    public static Function<Context, Context> clearContext() {
        return context -> context.delete(LOCALE_CONTEXT_KEY);
    }

    public static Context withLocaleContext(Mono<? extends LocaleContext> localeContext) {
        return Context.of(LOCALE_CONTEXT_KEY, localeContext);
    }

    public static Context withLocale(Locale locale) {
        return withLocaleContext(Mono.just(new SimpleLocaleContext(locale)));
    }
}
