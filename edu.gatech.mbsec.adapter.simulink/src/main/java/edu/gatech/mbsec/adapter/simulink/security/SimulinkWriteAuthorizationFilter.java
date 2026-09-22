package edu.gatech.mbsec.adapter.simulink.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Protects state-changing OSLC requests with a deployment-supplied bearer
 * token. A missing token configuration fails closed: read-only requests keep
 * working, but POST and PUT requests are rejected until an operator configures
 * {@code SIMULINK_WRITE_TOKEN} or {@code simulink.writeToken}.
 */
public final class SimulinkWriteAuthorizationFilter implements Filter {

	private static final String TOKEN_PROPERTY = "simulink.writeToken";
	private static final String TOKEN_ENVIRONMENT_VARIABLE = "SIMULINK_WRITE_TOKEN";
	private static final String AUTHORIZATION_PREFIX = "Bearer ";

	private String expectedToken;

	@Override
	public void init(final FilterConfig filterConfig) {
		expectedToken = configuredToken();
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
			throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest httpRequest)
				|| !(response instanceof HttpServletResponse httpResponse)
				|| isReadOnly(httpRequest.getMethod())
				|| isAuthorized(httpRequest.getHeader("Authorization"), expectedToken)) {
			chain.doFilter(request, response);
			return;
		}

		httpResponse.setHeader("WWW-Authenticate", "Bearer");
		httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED,
				"Write authorization is required for this operation");
	}

	static boolean isReadOnly(final String method) {
		return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)
				|| "OPTIONS".equalsIgnoreCase(method);
	}

	static boolean isAuthorized(final String authorizationHeader, final String expectedToken) {
		if (authorizationHeader == null || expectedToken == null || expectedToken.isEmpty()
				|| !authorizationHeader.startsWith(AUTHORIZATION_PREFIX)) {
			return false;
		}
		final byte[] supplied = authorizationHeader.substring(AUTHORIZATION_PREFIX.length())
				.getBytes(StandardCharsets.UTF_8);
		final byte[] expected = expectedToken.getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(supplied, expected);
	}

	private static String configuredToken() {
		final String systemProperty = System.getProperty(TOKEN_PROPERTY);
		if (systemProperty != null && !systemProperty.isEmpty()) {
			return systemProperty;
		}
		final String environmentVariable = System.getenv(TOKEN_ENVIRONMENT_VARIABLE);
		return environmentVariable == null || environmentVariable.isEmpty() ? null : environmentVariable;
	}
}
