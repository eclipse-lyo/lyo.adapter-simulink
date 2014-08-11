/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *  
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *
 *     Michael Fiedler     - initial API and implementation for Bugzilla adapter
 *     
 *     Axel Reichwein	   - implementation for Simulink adapter (axel.reichwein@koneksys.com)
 *     
 *******************************************************************************/
package org.eclipse.lyo.adapter.simulink.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.lyo.adapter.simulink.resources.Constants;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkBlock;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkElementsToCreate;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkInputPort;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkLine;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkModel;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkOutputPort;
import org.eclipse.lyo.adapter.simulink.resources.SimulinkParameter;
import org.eclipse.lyo.adapter.simulink.services.ServiceProviderCatalogService;
import org.eclipse.lyo.adapter.simulink.services.ServiceProviderService;
import org.eclipse.lyo.adapter.simulink.services.SimulinkBlockService;
import org.eclipse.lyo.adapter.simulink.services.SimulinkInputPortService;
import org.eclipse.lyo.adapter.simulink.services.SimulinkLineService;
import org.eclipse.lyo.adapter.simulink.services.SimulinkModelService;
import org.eclipse.lyo.adapter.simulink.services.SimulinkOutputPortService;
import org.eclipse.lyo.adapter.simulink.services.SimulinkParameterService;
import org.eclipse.lyo.oslc4j.application.OslcWinkApplication;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.ServiceProviderCatalog;
import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;

/**
 * OSLC4JSimulinkApplication registers all entity providers for converting POJOs into 
 * RDF/XML, JSON and other formats. OSLC4JSimulinkApplication registers also registers each 
 * servlet class containing the implementation of OSLC RESTful web services. 
 * 
 * OSLC4JSimulinkApplication also reads the user-defined configuration file 
 * with loadPropertiesFile(). This is done at the initialization of the web application, 
 * for example when the first resource or service of the OSLC Simulink adapter is requested. 
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
public class OSLC4JSimulinkApplication extends OslcWinkApplication{

	private static final Set<Class<?>>         RESOURCE_CLASSES                          = new HashSet<Class<?>>();
    private static final Map<String, Class<?>> RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP = new HashMap<String, Class<?>>();
	
    public static String simulinkEcoreLocation = null;
    public static String simulinkModelsDirectory = null;
    public static String portNumber = null;
    
	 static
	    {
		 RESOURCE_CLASSES.addAll(JenaProvidersRegistry.getProviders());	
		 RESOURCE_CLASSES.add(ServiceProviderCatalogService.class);
		 RESOURCE_CLASSES.add(ServiceProviderService.class);
		 RESOURCE_CLASSES.add(SimulinkModelService.class);
		 RESOURCE_CLASSES.add(SimulinkBlockService.class);
		 RESOURCE_CLASSES.add(SimulinkInputPortService.class);
		 RESOURCE_CLASSES.add(SimulinkOutputPortService.class);
		 RESOURCE_CLASSES.add(SimulinkLineService.class);
		 RESOURCE_CLASSES.add(SimulinkParameterService.class);
		 RESOURCE_CLASSES.add(SimulinkBlock.class);	
		 RESOURCE_CLASSES.add(SimulinkLine.class);
		 RESOURCE_CLASSES.add(SimulinkParameter.class);
		 RESOURCE_CLASSES.add(SimulinkElementsToCreate.class);
		 
		 RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.put(Constants.PATH_SIMULINK_MODEL, SimulinkModel.class);
		 RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.put(Constants.PATH_SIMULINK_BLOCK, SimulinkBlock.class);
		 RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.put(Constants.PATH_SIMULINK_INPUTPORT, SimulinkInputPort.class);
		 RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.put(Constants.PATH_SIMULINK_OUTPUTPORT, SimulinkOutputPort.class);
		 RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.put(Constants.PATH_SIMULINK_LINE, SimulinkLine.class);
		 RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.put(Constants.PATH_SIMULINK_PARAMETER, SimulinkParameter.class);

		 loadPropertiesFile();
	    }
	
	 public OSLC4JSimulinkApplication()
	           throws OslcCoreApplicationException,
	                  URISyntaxException
	    {
	        super(RESOURCE_CLASSES,
	              OslcConstants.PATH_RESOURCE_SHAPES,
	              RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP);
	    }

	 private static void loadPropertiesFile() {
			Properties prop = new Properties();
			InputStream input = null;
		 
			try {
				// loading properties file
				input = new FileInputStream("./configuration/config.properties");
							
				// load property file content and convert backslashes into forward slashes
				String str = readFile("./configuration/config.properties", Charset.defaultCharset());
				prop.load(new StringReader(str.replace("\\","/")));
				
				// get the property value 
				String simulinkEcoreLocationFromUser = prop.getProperty("simulinkEcoreLocation");			
				String simulinkModelsDirectoryFromUser = prop.getProperty("simulinkModelsDirectory");
				
				// add trailing slash if missing
				if(!simulinkModelsDirectoryFromUser.endsWith("/")){
					simulinkModelsDirectoryFromUser = simulinkModelsDirectoryFromUser + "/";
				}
				simulinkModelsDirectory = simulinkModelsDirectoryFromUser;
				simulinkEcoreLocation = simulinkEcoreLocationFromUser;
				portNumber = prop.getProperty("portNumber");
		 
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
		
		static String readFile(String path, Charset encoding) 
				  throws IOException 
				{
				  byte[] encoded = Files.readAllBytes(Paths.get(path));
				  return encoding.decode(ByteBuffer.wrap(encoded)).toString();
				}
}
