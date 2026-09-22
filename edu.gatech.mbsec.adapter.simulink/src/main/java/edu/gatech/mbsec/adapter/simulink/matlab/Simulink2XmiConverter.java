package edu.gatech.mbsec.adapter.simulink.matlab;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.gatech.mbsec.adapter.simulink.application.MatlabCommand;

/** Runs the MATLAB-backed Simulink-to-XMI conversion synchronously. */
public final class Simulink2XmiConverter {

	private static final Logger LOG = LoggerFactory.getLogger(Simulink2XmiConverter.class);
	private static final long TIMEOUT_MINUTES = 5;

	/**
	 * Converts every Simulink model under {@code modelsDirectory} and returns
	 * the resulting XMI file. MATLAB diagnostics are inherited by the adapter.
	 */
	public Path convert(final Path modelsDirectory, final Path matlabScriptsDirectory)
			throws IOException, InterruptedException {
		final Path models = modelsDirectory.toAbsolutePath().normalize();
		final Path scripts = matlabScriptsDirectory.toAbsolutePath().normalize();
		if (!Files.isDirectory(models)) {
			throw new IOException("Simulink models directory does not exist: " + models);
		}
		if (!Files.isDirectory(scripts)) {
			throw new IOException("MATLAB scripts directory does not exist: " + scripts);
		}

		final String matlabScript = "try,addpath(" + MatlabCommand.stringLiteral(scripts.toString().replace('\\', '/'))
				+ ");simulink2xmi(" + MatlabCommand.stringLiteral(models.toString().replace('\\', '/'))
				+ ");catch exception,disp(getReport(exception,'extended','hyperlinks','off'));exit(1);end;exit(0);";
		final long startedAt = System.nanoTime();
		final Process process = MatlabCommand.start(matlabScript);
		final boolean completed;
		try {
			completed = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
		} catch (InterruptedException exception) {
			terminate(process);
			Thread.currentThread().interrupt();
			throw exception;
		}

		if (!completed) {
			terminate(process);
			throw new IOException("MATLAB Simulink conversion exceeded the " + TIMEOUT_MINUTES + " minute timeout");
		}
		final int exitCode = process.exitValue();
		if (exitCode != 0) {
			throw new IOException("MATLAB Simulink conversion failed with exit code " + exitCode);
		}

		final Path xmi = models.resolve("simulinkWorkDir.xmi");
		if (!Files.isRegularFile(xmi) || Files.size(xmi) == 0) {
			throw new IOException("MATLAB completed without producing a non-empty XMI file at " + xmi);
		}
		LOG.info("Simulink-to-XMI conversion completed in {} seconds",
				TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startedAt));
		return xmi;
	}

	private static void terminate(final Process process) {
		process.descendants().forEach(descendant -> {
			if (descendant.isAlive()) {
				descendant.destroyForcibly();
			}
		});
		if (process.isAlive()) {
			process.destroyForcibly();
		}
	}
}
