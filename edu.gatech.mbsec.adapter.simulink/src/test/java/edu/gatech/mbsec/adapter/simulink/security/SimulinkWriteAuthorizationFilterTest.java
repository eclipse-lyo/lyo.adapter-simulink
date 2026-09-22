package edu.gatech.mbsec.adapter.simulink.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimulinkWriteAuthorizationFilterTest {

	@Test
	void acceptsOnlyTheConfiguredBearerToken() {
		assertTrue(SimulinkWriteAuthorizationFilter.isAuthorized("Bearer test-token", "test-token"));
		assertFalse(SimulinkWriteAuthorizationFilter.isAuthorized("Bearer wrong-token", "test-token"));
		assertFalse(SimulinkWriteAuthorizationFilter.isAuthorized("Basic test-token", "test-token"));
	}

	@Test
	void readOnlyMethodsDoNotRequireAWriteToken() {
		assertTrue(SimulinkWriteAuthorizationFilter.isReadOnly("GET"));
		assertTrue(SimulinkWriteAuthorizationFilter.isReadOnly("HEAD"));
		assertTrue(SimulinkWriteAuthorizationFilter.isReadOnly("OPTIONS"));
		assertFalse(SimulinkWriteAuthorizationFilter.isReadOnly("POST"));
		assertFalse(SimulinkWriteAuthorizationFilter.isReadOnly("PUT"));
		assertFalse(SimulinkWriteAuthorizationFilter.isReadOnly("DELETE"));
	}
}
