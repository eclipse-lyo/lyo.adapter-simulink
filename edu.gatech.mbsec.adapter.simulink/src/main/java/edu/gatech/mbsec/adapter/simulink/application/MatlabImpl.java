package edu.gatech.mbsec.adapter.simulink.application;

import java.io.File;

import simulink.WorkingDirectory;

import edu.gatech.mbsec.adapter.simulink.matlab.Simulink2XMIThread2;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkBlock;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkElementsToCreate;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkLine;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkParameter;
import edu.gatech.mbsec.adapter.simulink.services.OSLC4JSimulinkApplication;

/**
 * Production backend: runs MATLAB (via {@link Simulink2XMIThread2}) to convert
 * the Simulink working directory into {@code simulinkWorkDir.xmi} and loads it.
 * Selected via the {@code simulink.backend=matlab} configuration flag; requires
 * a MATLAB installation, so it is opt-in rather than the default.
 */
public class MatlabImpl implements SimulationModelBackend {

	@Override
	public WorkingDirectory loadWorkingDirectory() throws Exception {
		final Simulink2XMIThread2 thread = new Simulink2XMIThread2();
		thread.start();
		thread.join();
		final File xmi = new File(OSLC4JSimulinkApplication.simulinkModelsDirectory + "/simulinkWorkDir.xmi");
		return SimulinkManager.loadWorkingDirectoryFromXmi(xmi);
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
