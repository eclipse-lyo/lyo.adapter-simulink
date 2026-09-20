package edu.gatech.mbsec.adapter.simulink.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class MatlabCommandTest {

	@Test
	void escapesQuotesWithoutCreatingAnotherMatlabStatement() {
		final String payload = "1');system('calc.exe');%";

		assertEquals("'1'');system(''calc.exe'');%'", MatlabCommand.stringLiteral(payload));
	}

	@Test
	void preservesTheCompleteMatlabScriptAsOneProcessArgument() {
		final String script = "addSimulinkParameter('model','Block','Gain','1'');system(''calc'');%');";

		assertEquals(List.of("matlab", "start", "/wait", "-nodisplay", "-nosplash", "-nodesktop", "-r", script),
				MatlabCommand.arguments(script));
	}

	@Test
	void rejectsControlCharacters() {
		assertThrows(IllegalArgumentException.class, () -> MatlabCommand.stringLiteral("safe\nclose_system"));
	}
}
