package edu.gatech.mbsec.adapter.simulink.application;

import java.io.File;
import java.nio.file.Path;

import simulink.WorkingDirectory;

import edu.gatech.mbsec.adapter.simulink.matlab.Simulink2XmiConverter;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkBlock;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkElementsToCreate;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkLine;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkParameter;
import edu.gatech.mbsec.adapter.simulink.services.OSLC4JSimulinkApplication;

/**
 * Production backend: runs MATLAB (via {@link Simulink2XmiConverter}) to convert
 * the Simulink working directory into {@code simulinkWorkDir.xmi} and loads it.
 * Selected via the {@code simulink.backend=matlab} configuration flag; requires
 * a MATLAB installation, so it is opt-in rather than the default.
 */
public class MatlabImpl implements SimulationModelBackend {

	@Override
	public WorkingDirectory loadWorkingDirectory() throws Exception {
		final Path modelsDirectory = Path.of(OSLC4JSimulinkApplication.simulinkModelsDirectory);
		final Path matlabScriptsDirectory = Path.of(OSLC4JSimulinkApplication.matlabScriptsDirectory);
		final Path xmi = new Simulink2XmiConverter().convert(modelsDirectory, matlabScriptsDirectory);
		return SimulinkManager.loadWorkingDirectoryFromXmi(xmi.toFile());
	}

	@Override
	public void createBlock(final SimulinkBlock block, final String modelName) {
		SimulinkManager.createSimulinkBlock(block, modelName);
	}

	@Override
	public void createParameter(final SimulinkParameter parameter, final String modelName) {
		SimulinkManager.createSimulinkParameter(parameter, modelName);
	}

	@Override
	public void createLine(final SimulinkLine line, final String modelName) {
		SimulinkManager.createSimulinkLine(line, modelName);
	}

	@Override
	public void createElements(final SimulinkElementsToCreate elements, final String modelName) {
		SimulinkManager.createSimulinkElements(elements, modelName);
	}
}
