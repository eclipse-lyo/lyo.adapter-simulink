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
import java.util.ArrayList;
import java.util.Collection;
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
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import edu.gatech.mbsec.adapter.simulink.resources.Constants;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkBlock;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkInputPort;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkLine;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkModel;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkOutputPort;
import org.eclipse.lyo.oslc4j.core.OSLC4JUtils;
import org.eclipse.lyo.oslc4j.core.annotation.OslcCreationFactory;
import org.eclipse.lyo.oslc4j.core.annotation.OslcQueryCapability;
import org.eclipse.lyo.oslc4j.core.annotation.OslcService;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.eclipse.lyo.oslc4j.core.model.ResourceShapeFactory;
import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;

import edu.gatech.mbsec.adapter.simulink.application.SimulinkManager;
import edu.gatech.mbsec.adapter.simulink.serviceproviders.ServiceProviderCatalogSingleton;

@Path(OslcConstants.PATH_RESOURCE_SHAPES)
public class ResourceShapeService {

	private static final Logger LOG = LoggerFactory.getLogger(ResourceShapeService.class);

	@Context
	private HttpServletRequest httpServletRequest;
	@Context
	private HttpServletResponse httpServletResponse;
	@Context
	private UriInfo uriInfo;

	@GET
	@Path("{resourceShapePath}")
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlResourceShape(@Context final HttpServletRequest httpServletRequest,
			@PathParam("resourceShapePath") final String resourceShapePath)
					throws OslcCoreApplicationException, URISyntaxException {

		final String baseURI = OSLC4JUtils.resolveURI(httpServletRequest, false);

		final Class<?> resourceClass = OSLC4JSimulinkApplication.RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP
				.get(resourceShapePath);

		if (resourceClass != null) {
			ResourceShape resourceShape = ResourceShapeFactory.createResourceShape(baseURI,
					OslcConstants.PATH_RESOURCE_SHAPES, resourceShapePath, resourceClass);
			String requestURL = httpServletRequest.getRequestURL().toString();
			httpServletRequest.setAttribute("resource", resourceShape);
			httpServletRequest.setAttribute("requestURL", requestURL);
			RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/resourceshape/resourceshape_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				LOG.error("Unhandled exception", e);
				throw new WebApplicationException(e);
			}
		}

		// throw new WebApplicationException(Response.Status.NOT_FOUND);

	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlResourceShapes(@Context final HttpServletRequest httpServletRequest)
			throws OslcCoreApplicationException, URISyntaxException {
		final String baseURI = httpServletRequest.getRequestURL().toString().replace("/resourceShapes", "");
		final Collection<Class<?>> elements = OSLC4JSimulinkApplication.RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP
				.values();
		Collection<ResourceShape> resourceShapes = new ArrayList<ResourceShape>();
		for (String resourceShapePath : OSLC4JSimulinkApplication.RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.keySet()) {
			final Class<?> resourceClass = OSLC4JSimulinkApplication.RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP
					.get(resourceShapePath);
			ResourceShape resourceShape = ResourceShapeFactory.createResourceShape(baseURI,
					OslcConstants.PATH_RESOURCE_SHAPES, resourceShapePath, resourceClass);
			if (resourceShape != null) {
				resourceShapes.add(resourceShape);
			}

		}

		if (!resourceShapes.isEmpty()) {
			String requestURL = httpServletRequest.getRequestURL().toString();
			httpServletRequest.setAttribute("elements", resourceShapes);
			httpServletRequest.setAttribute("requestURL", requestURL);
			RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/resourceshape/resourceshapes_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				LOG.error("Unhandled exception", e);
				throw new WebApplicationException(e);
			}
		}

	}

	@OslcQueryCapability(title = "Resource Shape Query Capability", label = "Resource Shape Query", resourceShape = OslcConstants.PATH_RESOURCE_SHAPES
			+ "/" + OslcConstants.PATH_RESOURCE_SHAPE, resourceTypes = { OslcConstants.PATH_RESOURCE_SHAPE }, usages = {
					OslcConstants.OSLC_USAGE_DEFAULT })
	@GET
	@Produces({ OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public List<ResourceShape> getResourceShapes() {
		final String baseURI = httpServletRequest.getRequestURL().toString().replace("/resourceShapes", "");
		List<ResourceShape> resourceShapes = new ArrayList<ResourceShape>();
		for (String resourceShapePath : OSLC4JSimulinkApplication.RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.keySet()) {
			final Class<?> resourceClass = OSLC4JSimulinkApplication.RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP
					.get(resourceShapePath);
			ResourceShape resourceShape = null;
			try {
				resourceShape = ResourceShapeFactory.createResourceShape(baseURI, OslcConstants.PATH_RESOURCE_SHAPES,
						resourceShapePath, resourceClass);
			} catch (OslcCoreApplicationException e) {
				LOG.error("Unhandled exception", e);
			} catch (URISyntaxException e) {
				LOG.error("Unhandled exception", e);
			}
			if (resourceShape != null) {
				resourceShapes.add(resourceShape);
			}
		}
		return resourceShapes;

	}

	@GET
	@Path("{resourceShapePath}")
	@Produces({ OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public Response getResourceShape(@Context final HttpServletRequest httpServletRequest,
			@PathParam("resourceShapePath") final String resourceShapePath, @Context Request request) {
		
		final String baseURI = OSLC4JUtils.resolveURI(httpServletRequest, false);

		final Class<?> resourceClass = OSLC4JSimulinkApplication.RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP
				.get(resourceShapePath);

		ResourceShape resourceShape = null;
		if (resourceClass != null) {
			try {
				resourceShape = ResourceShapeFactory.createResourceShape(baseURI,
						OslcConstants.PATH_RESOURCE_SHAPES, resourceShapePath, resourceClass);
			} catch (OslcCoreApplicationException e) {
				LOG.error("Unhandled exception", e);
			} catch (URISyntaxException e) {
				LOG.error("Unhandled exception", e);
			}
		}

		ResponseBuilder builder = null;
		if(resourceShape != null){
			builder = request.evaluatePreconditions();			
			//If rb is null then either it is first time request; or resource is modified
	        //Get the updated representation and return with Etag attached to it
			if (builder == null) {
			    builder = Response.ok(resourceShape);
			}
		}
		else{
			builder = Response.status(500);
		}
		return builder.build();
	}

}
