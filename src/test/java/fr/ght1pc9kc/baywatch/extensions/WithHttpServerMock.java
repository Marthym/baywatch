package fr.ght1pc9kc.baywatch.extensions;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import javax.annotation.Nullable;
import java.util.Optional;

public class WithHttpServerMock implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private final WireMockServer webServer;

    private WithHttpServerMock(@Nullable Integer port) {
        final WireMockConfiguration config = Optional.ofNullable(port)
                .map(p -> WireMockConfiguration.wireMockConfig().port(p))
                .orElseGet(() -> WireMockConfiguration.wireMockConfig().dynamicPort());
        this.webServer = new WireMockServer(config);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        webServer.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        webServer.shutdownServer();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        final Class<?> clazz = parameterContext.getParameter().getType();
        return WireMockServer.class.equals(clazz);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext context) throws ParameterResolutionException {
        Class<?> clazz = parameterContext.getParameter().getType();
        if (WireMockServer.class.equals(clazz)) {
            return webServer;
        }
        throw new ParameterResolutionException("Unable to resolve parameter of type: " + clazz.getName());
    }

    /**
     * Create a builder for the extension.
     *
     * @return {@link WithHttpServerMockBuilder}.
     */
    public static WithHttpServerMockBuilder builder() {
        return new WithHttpServerMockBuilder();
    }

    /**
     * Builder class for {@link WithHttpServerMock}.
     */
    public static final class WithHttpServerMockBuilder {

        @Nullable
        private Integer port;

        private WithHttpServerMockBuilder() {
        }

        /**
         * Define the port on which the server is listening.
         * If not set, a dynamic port will be used.
         *
         * @param port Port to listen on.
         * @return Builder instance.
         */
        public WithHttpServerMockBuilder onPort(int port) {
            this.port = port;
            return this;
        }

        /**
         * Build the extension.
         *
         * @return {@link WithHttpServerMock}.
         */
        public WithHttpServerMock build() {
            return new WithHttpServerMock(port);
        }
    }

}
