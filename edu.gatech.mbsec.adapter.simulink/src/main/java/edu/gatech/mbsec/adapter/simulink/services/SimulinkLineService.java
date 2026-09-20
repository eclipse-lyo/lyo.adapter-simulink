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
package edu.gatech.mbsec.adapter.simulink.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import edu.gatech.mbsec.adapter.simulink.resources.Constants;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkBlock;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkInputPort;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkLine;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkModel;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkOutputPort;
import org.eclipse.lyo.oslc4j.core.annotation.OslcCreationFactory;
import org.eclipse.lyo.oslc4j.core.annotation.OslcQueryCapability;
import org.eclipse.lyo.oslc4j.core.annotation.OslcService;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;

import edu.gatech.mbsec.adapter.simulink.application.SimulinkManager;


/**
 * This servlet contains the implementation of OSLC RESTful web services for Simulink Line resources.
 * 
 * The servlet contains web services for: <ul margin-top: 0;>
 * <li> returning specific Simulink Line resources in HTML and other formats </li>
 * <li> returning all Simulink Line resources within a specific Simulink model
 *  in HTML and other formats </li>
 *  <li> adding new Simulink Line resources to a specific Simulink model</li>
 *  </ul>
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
@OslcService(Constants.SIMULINK_LINE_DOMAIN)
@Path("{modelName}/lines")
public class SimulinkLineService {

	private static final Logger LOG = LoggerFactory.getLogger(SimulinkLineService.class);
	
	@Context
	private HttpServletRequest httpServletRequest;
	@Context
	private HttpServletResponse httpServletResponse;
	@Context
	private UriInfo uriInfo;
	
	@OslcQueryCapability(title = "Simulink Line Query Capability", label = "Simulink Line Catalog Query", resourceShape = OslcConstants.PATH_RESOURCE_SHAPES
			+ "/" + Constants.PATH_SIMULINK_LINE, resourceTypes = { Constants.TYPE_SIMULINK_LINE }, usages = { OslcConstants.OSLC_USAGE_DEFAULT })
	@GET
	@Produces({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public List<SimulinkLine> getLines(
			@PathParam("modelName") final String modelName,
			@QueryParam("oslc.where") final String where,
			@QueryParam("oslc.select") final String select,
			@QueryParam("oslc.prefix") final String prefix,
			@QueryParam("page") final String pageString,
			@QueryParam("oslc.orderBy") final String orderBy,
			@QueryParam("oslc.searchTerms") final String searchTerms,
			@QueryParam("oslc.paging") final String paging,
			@QueryParam("oslc.pageSize") final String pageSize)
			throws IOException, ServletException {
		SimulinkManager.loadSimulinkWorkingDirectory();
		List<SimulinkLine> simulinkElements = SimulinkManager.getLinesInModel(modelName);
		return simulinkElements;
	}
	
	@GET	
	@Path("{uri}")
	@Produces({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_JSON })
	public edu.gatech.mbsec.adapter.simulink.resources.SimulinkLine getLine(@PathParam("modelName") final String modelName,
			@PathParam("uri") final String qualifiedName)
			throws URISyntaxException {
		SimulinkManager.loadSimulinkWorkingDirectory();
		SimulinkLine simulinkLine = SimulinkManager
				.getLineByURI(modelName + "/lines/" + qualifiedName);
		return simulinkLine;
	}
	
	@GET
	@Path("{uri}")
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlLine(@PathParam("modelName") final String modelName,
			@PathParam("uri") final String qualifiedName,
			@QueryParam("oslc.properties") final String propertiesString,
			@QueryParam("oslc.prefix") final String prefix)
			throws URISyntaxException, IOException {
		SimulinkManager.loadSimulinkWorkingDirectory();
		SimulinkLine simulinkElement = SimulinkManager
				.getLineByURI(modelName + "/lines/" + qualifiedName);
		String requestURL = httpServletRequest.getRequestURL().toString();
		if (simulinkElement != null) {			
			httpServletRequest.setAttribute("element", simulinkElement);
			httpServletRequest.setAttribute("requestURL", requestURL);
			RequestDispatcher rd = httpServletRequest
					.getRequestDispatcher("/simulink/line_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				LOG.error("Unhandled exception", e);
				throw new WebApplicationException(e);
			}
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlLines(@PathParam("modelName") final String modelName) {
		SimulinkManager.loadSimulinkWorkingDirectory();
		List<SimulinkLine> simulinkElements = SimulinkManager.getLinesInModel(modelName);
		String requestURL = httpServletRequest.getRequestURL().toString();
		if (simulinkElements != null) {			
			httpServletRequest.setAttribute("elements", simulinkElements);
			httpServletRequest.setAttribute("requestURL", requestURL);
			httpServletRequest.setAttribute("modelName", modelName);
			RequestDispatcher rd = httpServletRequest
					.getRequestDispatcher("/simulink/lines_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				LOG.error("Unhandled exception", e);
				throw new WebApplicationException(e);
			}
		}
	}

	@OslcCreationFactory(title = "Simulink Line Creation Factory", label = "Simulink Line Creation", resourceShapes = { OslcConstants.PATH_RESOURCE_SHAPES
			+ "/" + Constants.PATH_SIMULINK_LINE }, resourceTypes = { Constants.TYPE_SIMULINK_LINE }, usages = { OslcConstants.OSLC_USAGE_DEFAULT })
	@POST
	@Consumes({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	@Produces({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public Response addLine(@PathParam("modelName") final String modelName,
			final SimulinkLine simulinkLine) throws IOException, ServletException {				
		SimulinkManager.getBackend().createLine(simulinkLine, modelName);
		URI about = simulinkLine.getAbout();
		return Response.created(about).entity(simulinkLine).build();
	}
}
