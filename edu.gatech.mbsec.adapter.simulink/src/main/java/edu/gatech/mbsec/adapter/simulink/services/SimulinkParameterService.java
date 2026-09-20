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

import org.eclipse.lyo.oslc4j.core.OSLC4JUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import edu.gatech.mbsec.adapter.simulink.resources.Constants;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkBlock;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkInputPort;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkLine;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkModel;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkParameter;
import org.eclipse.lyo.oslc4j.core.annotation.OslcCreationFactory;
import org.eclipse.lyo.oslc4j.core.annotation.OslcQueryCapability;
import org.eclipse.lyo.oslc4j.core.annotation.OslcService;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;

import edu.gatech.mbsec.adapter.simulink.application.SimulinkManager;

/**
 * This servlet contains the implementation of OSLC RESTful web services for Simulink Parameter resources.
 * 
 * The servlet contains web services for: <ul margin-top: 0;>
 * <li> returning specific Simulink Parameter resources in HTML and other formats </li>
 * <li> returning all Simulink Parameter resources within a specific MagicDraw project
 *  in HTML and other formats </li>
 *  <li> adding new Simulink Parameter resources to a specific MagicDraw project</li>
 *  <li> updating a specific Simulink Parameter</li>
 *  <li> getting the ETag of a specific Simulink Parameter resource</li>
 *  </ul>
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
@OslcService(Constants.SIMULINK_PARAMETER_DOMAIN)
@Path("{modelName}/parameters")
public class SimulinkParameterService {

	private static final Logger LOG = LoggerFactory.getLogger(SimulinkParameterService.class);

	@Context
	private HttpServletRequest httpServletRequest;
	@Context
	private HttpServletResponse httpServletResponse;
	@Context
	private UriInfo uriInfo;

	static String baseHTTPURI = OSLC4JUtils.getPublicURI() != null ? OSLC4JUtils.getPublicURI() : ("http://localhost:" + OSLC4JSimulinkApplication.portNumber);

	@OslcQueryCapability(title = "Simulink Parameter Query Capability", label = "Simulink Parameter Catalog Query", resourceShape = OslcConstants.PATH_RESOURCE_SHAPES
			+ "/" + Constants.PATH_SIMULINK_PARAMETER, resourceTypes = { Constants.TYPE_SIMULINK_PARAMETER }, usages = { OslcConstants.OSLC_USAGE_DEFAULT })
	@GET
	@Produces({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public List<SimulinkParameter> getParameters(
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
		List<SimulinkParameter> simulinkElements = SimulinkManager
				.getParametersInModel(modelName);
		return simulinkElements;
	}

	@GET
	@Path("{uri}")
	@Produces({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_JSON })
	// getParameter(
	public synchronized Response getParameter(
			@PathParam("modelName") final String modelName,
			@PathParam("uri") final String qualifiedName,
			@Context Request request) throws URISyntaxException {
		SimulinkManager.loadSimulinkWorkingDirectory();
		SimulinkParameter simulinkParameter = SimulinkManager
				.getParameterByURI(modelName + "/parameters/" + qualifiedName);
		if(simulinkParameter == null){
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}
		EntityTag eTag = new EntityTag(md5Java(simulinkParameter));
		String requestETag = httpServletRequest.getHeader("If-None-Match");
		if(requestETag != null){
			// Conditional GET
			ResponseBuilder builder = request.evaluatePreconditions(eTag);
			
			// If rb is null then either it is first time request; or resource is
			// modified
			// Get the updated representation and return with Etag attached to it
			if (builder == null) {
				builder = Response.ok(simulinkParameter);
				builder.tag(eTag);
			} 
			return builder.build();
		}
		else{
			// Regular GET
			return Response.ok(simulinkParameter).tag(eTag).build();
		}

	}

	@GET
	@Path("{uri}")
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlParameter(
			@PathParam("modelName") final String modelName,
			@PathParam("uri") final String qualifiedName,
			@QueryParam("oslc.properties") final String propertiesString,
			@QueryParam("oslc.prefix") final String prefix)
			throws URISyntaxException, IOException {
		SimulinkManager.loadSimulinkWorkingDirectory();
		SimulinkParameter simulinkElement = SimulinkManager
				.getParameterByURI(modelName + "/parameters/" + qualifiedName);		
		String requestURL = httpServletRequest.getRequestURL().toString();
		if (simulinkElement != null) {			
			httpServletRequest.setAttribute("element", simulinkElement);
			httpServletRequest.setAttribute("requestURL", requestURL);
			RequestDispatcher rd = httpServletRequest
					.getRequestDispatcher("/simulink/parameter_html.jsp");
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
	public void getHtmlParameters(@PathParam("modelName") final String modelName) {
		SimulinkManager.loadSimulinkWorkingDirectory();
		List<SimulinkParameter> simulinkElements = SimulinkManager
				.getParametersInModel(modelName);
		String requestURL = httpServletRequest.getRequestURL().toString();
		if (simulinkElements != null) {			
			httpServletRequest.setAttribute("elements", simulinkElements);
			httpServletRequest.setAttribute("requestURL", requestURL);
			httpServletRequest.setAttribute("modelName", modelName);
			RequestDispatcher rd = httpServletRequest
					.getRequestDispatcher("/simulink/parameters_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				LOG.error("Unhandled exception", e);
				throw new WebApplicationException(e);
			}
		}
	}

	@OslcCreationFactory(title = "Simulink Parameter Creation Factory", label = "Simulink Parameter Creation", resourceShapes = { OslcConstants.PATH_RESOURCE_SHAPES
			+ "/" + Constants.PATH_SIMULINK_PARAMETER }, resourceTypes = { Constants.TYPE_SIMULINK_PARAMETER }, usages = { OslcConstants.OSLC_USAGE_DEFAULT })
	@POST
	@Consumes({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	@Produces({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public Response createParameter(
			@PathParam("modelName") final String modelName,
			final SimulinkParameter simulinkParameter) throws IOException,
			ServletException {
		// String ifMatchHeader = httpServletRequest.getHeader("If-Match");
		LOG.info(simulinkParameter.getName());
		SimulinkManager.getBackend().createParameter(simulinkParameter, modelName);
		URI about = simulinkParameter.getAbout();
		return Response.created(about).entity(simulinkParameter).build();
	}

	@PUT
	@Path("{uri}")
	@Consumes({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public synchronized Response updateParameter(
			@PathParam("modelName") final String modelName,
			final SimulinkParameter simulinkParameter, @Context Request request)
			throws IOException, ServletException {
		String incomingParameterURI = simulinkParameter.getAbout().toString();
		incomingParameterURI = incomingParameterURI.replace(baseHTTPURI
				+ "/services/", "");
		SimulinkParameter simulinkElementToUpdate = SimulinkManager
				.getParameterByURI(incomingParameterURI);
		EntityTag eTag = new EntityTag(md5Java(simulinkElementToUpdate));		
		// just for debug/checking purposes
		String requestETag = httpServletRequest.getHeader("If-Match");		
		ResponseBuilder builder = request.evaluatePreconditions(eTag);
		// client is not up to date (send back 412, Precondition failed)
		if (builder != null) {
			return builder.build();		
		}
		// update simulinkParameter
		simulinkElementToUpdate.setValue(simulinkParameter.getValue());
		SimulinkManager.getBackend().createParameter(simulinkElementToUpdate, modelName);
		builder = Response.ok();
		EntityTag updatedETag = new EntityTag(md5Java(simulinkElementToUpdate));
		return builder.tag(updatedETag).build();
	}

	@HEAD
	@Path("{uri}")
	public synchronized Response headParameter(
			@PathParam("modelName") final String modelName,
			@PathParam("uri") final String qualifiedName,
			@Context Request request) throws URISyntaxException {
		SimulinkManager.loadSimulinkWorkingDirectory();
		SimulinkParameter simulinkParameter = SimulinkManager
				.getParameterByURI(modelName + "/parameters/" + qualifiedName);
		if(simulinkParameter == null){
			return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
		}
		EntityTag eTag = new EntityTag(md5Java(simulinkParameter));		
		String requestETag = httpServletRequest.getHeader("If-None-Match");
		if(requestETag != null){			
			ResponseBuilder builder = request.evaluatePreconditions(eTag);
			// If rb is null then either it is first time request; or resource is
			// modified, return status 200 with Etag attached to it
			// else, just return status 304, not modified with Etag
			if (builder == null) {
				builder = Response.ok();
			} else {
				builder = Response.status(HttpServletResponse.SC_NOT_MODIFIED);
			}
			return builder.tag(eTag).build();
		}
		else{
			// Regular HEAD
			return Response.ok().tag(eTag).build();
		}
	}

//	// http://javarevisited.blogspot.com/2013/03/generate-md5-hash-in-java-string-byte-array-example-tutorial.html
//	// http://stackoverflow.com/questions/2836646/java-serializable-object-to-byte-array
//	public static String md5Java(Object object) {
//		String digest = null;
//		try {
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			ObjectOutputStream out = new ObjectOutputStream(bos);  
//			out.writeObject(object);
//			byte[] objectBytes = bos.toByteArray();
////			MessageDigest md = MessageDigest.getInstance("MD5");
//			MessageDigest md = MessageDigest.getInstance("SHA-1");
//			byte[] hash = md.digest(objectBytes);
//			StringBuilder sb = new StringBuilder(2*hash.length);
//			for (byte b : hash) {
//				sb.append(String.format("%02x", b & 0xff));
//			}
//			digest = sb.toString();
//		} catch (UnsupportedEncodingException ex) {
//		} catch (NoSuchAlgorithmException ex) {
//		}
//		catch (IOException e) {
//			LOG.trace("Could not serialize a Simulink parameter while calculating its ETag", e);
//		}
//		return digest;
//	}
	
	public static String md5Java(SimulinkParameter simulinkParameter) {
		String digest = null;
		String message = simulinkParameter.getAbout().toASCIIString() + simulinkParameter.getName() + simulinkParameter.getValue();
		try {			
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(message.getBytes("UTF-8"));
			 //converting byte array to Hexadecimal String
			StringBuilder sb = new StringBuilder(2*hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}
			digest = sb.toString();
		} catch (UnsupportedEncodingException ex) {
			LOG.error("UTF-8 encoding is unavailable while calculating a Simulink parameter ETag", ex);
		} catch (NoSuchAlgorithmException ex) {
			LOG.error("MD5 is unavailable while calculating a Simulink parameter ETag", ex);
		}
		return digest;
	}

}
