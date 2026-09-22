/*********************************************************************************************
 * Copyright (c) 2014 Model-Based Systems Engineering Center, Georgia Institute of Technology.
 *                         http://www.mbse.gatech.edu/
 *                  http://www.mbsec.gatech.edu/research/oslc
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Eclipse Distribution License is available at
 *  http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *  Contributors:
 *
 *	   Axel Reichwein, Koneksys (axel.reichwein@koneksys.com)		
 *******************************************************************************************/
package edu.gatech.mbsec.adapter.simulink.matlab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.gatech.mbsec.adapter.simulink.services.OSLC4JSimulinkApplication;
import edu.gatech.mbsec.adapter.simulink.application.MatlabCommand;


/**
 * Simulink2XMIThread2 is responsible for converting the information  contained in
 * several Simulink models into an XMI file which can then be easily parsed and read 
 * by the OSLC Simulink adapter.
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
public class Simulink2XMIThread2 extends Thread {

	private static final Logger LOG = LoggerFactory.getLogger(Simulink2XMIThread2.class);

	private final String simulinkModelsFolder;
	private final String matlabScriptsFolder;

	public Simulink2XMIThread2(final String simulinkModelsFolder, final String matlabScriptsFolder) {
		this.simulinkModelsFolder = simulinkModelsFolder;
		this.matlabScriptsFolder = matlabScriptsFolder;
	}
	
	public void run() {
		long startTime = System.currentTimeMillis();
		// Execute Matlab from the command prompt
		try {						
			String matlabFolder = new File(matlabScriptsFolder)
					.getAbsolutePath().replace('\\', '/');
			String modelsFolder = simulinkModelsFolder == null ? "" : simulinkModelsFolder.replace('\\', '/');
			Process process = MatlabCommand.start("addpath("
					+ MatlabCommand.stringLiteral(matlabFolder) + ");simulink2xmi("
					+ MatlabCommand.stringLiteral(modelsFolder) + ");exit;");
			process.waitFor();									
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			LOG.info("OSLC Adapter <-> Simulink Interaction in "
					+ (duration / 1000) + " seconds");
		} catch (IOException e) {
			LOG.error("Unhandled exception", e);
		} catch (InterruptedException e) {
			LOG.error("Unhandled exception", e);
		}

	}

}
