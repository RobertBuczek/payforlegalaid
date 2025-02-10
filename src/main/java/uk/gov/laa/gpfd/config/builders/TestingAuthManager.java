package uk.gov.laa.gpfd.config.builders;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

final class TestingAuthorizationManagerWithAuthenticationProvider<T> implements AuthorizationManager<T> {

    private static final AuthenticationProvider provider = new TestingAuthenticationProvider();

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, T object) {
        Authentication auth = authentication.get();
        return new AuthorizationDecision(provider.authenticate(auth).isAuthenticated());
    }
}


final class TestingAuthorizationManagerWithTestingAuthenticationToken<T> implements AuthorizationManager<T> {

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, T object) {
        Authentication auth = authentication.get();
        if (auth != null && auth.isAuthenticated() && auth instanceof TestingAuthenticationToken) {
            return new AuthorizationDecision(true);
        }
        return new AuthorizationDecision(false);
    }
}