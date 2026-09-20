/*******************************************************************************
 * Copyright (c) 2026 Eclipse Lyo migration.
 *
 * This file is adapted from the Eclipse Lyo reference implementation
 * (co.oslc.refimpl.*.gen.servlet.ServletListener) to configure the
 * public base URI of the OSLC4J application at servlet-context startup.
 *
 * At Lyo 4.x the public URI is no longer inferred from a built-in
 * default (the old ServiceProviderRegistryURIs was removed); it must be
 * set explicitly via OSLC4JUtils.setPublicURI(...). The catalog's
 * static initializer dereferences OSLC4JUtils.getPublicURI(), so this
 * listener (which runs before the JAX-RS application is initialized)
 * must configure it first.
 *******************************************************************************/
package edu.gatech.mbsec.adapter.simulink.servlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import jakarta.ws.rs.core.UriBuilder;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.lyo.oslc4j.core.OSLC4JUtils;

import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures the public base URI for the OSLC resources produced by this
 * server through the OSLC4J method {@link OSLC4JUtils#setPublicURI(String)}.
 *
 * <p>See {@link #getConfigurationProperty(String, String, ServletContext)}
 * for the alternatives used to resolve the base URL.
 */
public class ServletListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(ServletListener.class);

    private static final String BASE_URL_KEY = "baseurl";
    private static final String FALLBACK_BASE = "http://localhost:8080";
    private static final String SERVLET_URL_PATTERN = "services/";

    public ServletListener() {
        super();
    }

    @Override
    public void contextInitialized(final ServletContextEvent servletContextEvent) {
        final ServletContext servletContext = servletContextEvent.getServletContext();

        // Select the Simulink backend (xmi | matlab) before any OSLC resource
        // references SimulinkManager, whose static initializer reads this
        // system property. Resolved via the standard precedence
        // (environment -> system property -> context param -> JNDI -> default). The standalone
        // XMI backend is the default so the server runs without MATLAB.
        final String backend =
                getConfigurationProperty("simulink.backend", "xmi", servletContext);
        System.setProperty("simulink.backend", backend);
        LOG.info("Using SimulationModelBackend implementation: {}",
                ("xmi".equalsIgnoreCase(backend)
                        ? "SimulationModelBackendStandaloneImpl (XMI fixture from classpath, no MATLAB required)"
                        : "MatlabImpl (requires MATLAB)"));

        final String svnImplementation =
                getConfigurationProperty("subversion.client.impl", "standalone", servletContext);
        System.setProperty("subversion.client.impl", svnImplementation);
        LOG.info("Using Subversion implementation: {}",
                ("standalone".equalsIgnoreCase(svnImplementation)
                        ? "SubversionServiceStandaloneImpl"
                        : "SubversionServiceSvnkitImpl (requires -Pfull)"));

        final String basePathProperty =
                getConfigurationProperty(BASE_URL_KEY, FALLBACK_BASE, servletContext);
        final UriBuilder builder = UriBuilder.fromUri(basePathProperty);
        final String baseUrl = builder.path(servletContext.getContextPath()).build().toString();

        try {
            LOG.info("Setting public URI: {}", baseUrl);
            OSLC4JUtils.setPublicURI(baseUrl);
            LOG.info("Setting servlet path: {}", SERVLET_URL_PATTERN);
            OSLC4JUtils.setServletPath(SERVLET_URL_PATTERN);
        } catch (final MalformedURLException e) {
            LOG.error("ServletListener encountered MalformedURLException.", e);
        } catch (final IllegalArgumentException e) {
            LOG.error("ServletListener encountered IllegalArgumentException.", e);
        }

        LOG.info("ServletListener contextInitialized.");
    }

    @Override
    public void contextDestroyed(final ServletContextEvent servletContextEvent) {
        // nothing to tear down
    }

    /**
     * Resolves a configuration property using the following precedence:
     * <ol>
     *   <li>Environment variable (exact key, legacy uppercase form, then Docker-style uppercase snake case)</li>
     *   <li>System property</li>
     *   <li>Servlet context init parameter</li>
     *   <li>JNDI ({@code java:comp/env/&lt;key&gt;}, then {@code &lt;key&gt;})</li>
     *   <li>Provided default value</li>
     * </ol>
     */
    private static String getConfigurationProperty(final String propertyKey,
            final String defaultValue, final ServletContext servletContext) {
        String value = System.getenv(propertyKey);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        value = System.getenv(propertyKey.toUpperCase().replace('.', '_'));
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        value = System.getenv(toDockerEnvironmentKey(propertyKey));
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        value = System.getProperty(propertyKey);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        value = servletContext.getInitParameter(propertyKey);
        if (value != null && !value.trim().isEmpty()) {
            return value;
        }

        try {
            final InitialContext context = new InitialContext();
            for (final String name : new String[] { "java:comp/env/" + propertyKey, propertyKey }) {
                try {
                    final Object jndiValue = context.lookup(name);
                    if (jndiValue != null && !jndiValue.toString().trim().isEmpty()) {
                        return jndiValue.toString();
                    }
                } catch (NamingException ignored) {
                    // Try the next standard JNDI name before using the default.
                    LOG.trace("JNDI lookup failed for {}", name, ignored);
                }
            }
        } catch (NamingException ignored) {
            // JNDI is optional in standalone Jetty and Docker deployments.
            LOG.trace("JNDI is unavailable while resolving {}", propertyKey, ignored);
        }

        return defaultValue;
    }

    private static String toDockerEnvironmentKey(final String propertyKey) {
        if ("svnurl".equals(propertyKey)) {
            return "SVN_URL";
        }
        return propertyKey.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replace('.', '_').toUpperCase();
    }
}
