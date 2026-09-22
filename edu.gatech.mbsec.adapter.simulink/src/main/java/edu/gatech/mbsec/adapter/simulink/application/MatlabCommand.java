package edu.gatech.mbsec.adapter.simulink.application;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Builds and starts MATLAB processes without allowing request data to alter
 * the process arguments or MATLAB statement structure.
 */
public final class MatlabCommand {

	private MatlabCommand() {
	}

	/**
	 * Returns a MATLAB character-vector literal containing {@code value}.
	 * MATLAB escapes a single quote inside a character vector by doubling it.
	 * Control characters are rejected because they can change command parsing
	 * across MATLAB and operating-system boundaries.
	 */
	public static String stringLiteral(final String value) {
		if (value == null) {
			throw new IllegalArgumentException("MATLAB command values must not be null");
		}
		for (int i = 0; i < value.length(); i++) {
			final char character = value.charAt(i);
			if (Character.isISOControl(character) || character == '\u2028' || character == '\u2029') {
				throw new IllegalArgumentException("MATLAB command values must not contain control characters");
			}
		}
		return "'" + value.replace("'", "''") + "'";
	}

	static List<String> arguments(final String matlabScript) {
		return Arrays.asList("matlab", "start", "/wait", "-nodisplay", "-nosplash", "-nodesktop", "-r",
				matlabScript);
	}

	public static Process start(final String matlabScript) throws IOException {
		// MATLAB writes startup and conversion diagnostics.  Inheriting the
		// adapter's streams prevents the child process from blocking on an
		// unconsumed stdout/stderr pipe during model conversion.
		return new ProcessBuilder(arguments(matlabScript)).inheritIO().start();
	}
}
