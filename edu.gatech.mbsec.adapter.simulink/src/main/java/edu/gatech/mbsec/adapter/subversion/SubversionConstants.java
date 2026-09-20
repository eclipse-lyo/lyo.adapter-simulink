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
 *     Russell Boykin       - initial API and implementation
 *     Alberto Giammaria    - initial API and implementation
 *     Chris Peters         - initial API and implementation
 *     Gianluca Bernardini  - initial API and implementation
 *     Michael Fiedler      - Bugzilla adpater implementations
 *     
 * Modifications performed by:    
 *     Axel Reichwein		- implementation for Simulink adapter
 *     (axel.reichwein@koneksys.com) 
 *******************************************************************************/
package edu.gatech.mbsec.adapter.subversion;


import org.eclipse.lyo.oslc4j.core.model.OslcConstants;

public interface SubversionConstants
{

    public static String SUBVERSION_PREFIX							 = "subversion";
    public static String SUBVERSION_NAMESPACE						 = "https://subversion.apache.org/";	
    
    public static String SUBVERSION_FILE_PREFIX                    	 = "subversion_file";

    
    public static String SUBVERSION_FILE_NAMESPACE                    = SUBVERSION_NAMESPACE + "File/";
    
    
    
    public static String TYPE_SUBVERSION_FILE                    	= SUBVERSION_NAMESPACE + "File";

    
    
    public static String SUBVERSION_FILE_DOMAIN                    	 = "https://subversion.apache.org/file/rdf#";

    

    public static String  PATH_SUBVERSION_FILE						= "file";
    
 
    public static final String HDR_OSLC_VERSION = "OSLC-Core-Version";
    
    
}
