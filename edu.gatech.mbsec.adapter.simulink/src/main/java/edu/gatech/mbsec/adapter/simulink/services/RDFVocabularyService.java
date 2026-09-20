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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

import edu.gatech.mbsec.adapter.simulink.resources.SimulinkBlock;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkInputPort;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkLine;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkModel;
import edu.gatech.mbsec.adapter.simulink.resources.SimulinkOutputPort;
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

@Path("rdfvocabulary")
public class RDFVocabularyService {

	@Context
	private HttpServletRequest httpServletRequest;
	@Context
	private HttpServletResponse httpServletResponse;
	@Context
	private UriInfo uriInfo;

	@GET
	@Produces({ OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_JSON })
	public Response getRDFVocabulary()
					throws OslcCoreApplicationException, URISyntaxException {

			return vocabularyResponse("application/rdf+xml");

		// throw new WebApplicationException(Response.Status.NOT_FOUND);

	}

	@GET
	@Produces(MediaType.TEXT_HTML)
	public Response getHtmlRDFVocabulary() throws OslcCoreApplicationException, URISyntaxException {
		return vocabularyResponse(MediaType.TEXT_HTML);

		// throw new WebApplicationException(Response.Status.NOT_FOUND);

	}

	private Response vocabularyResponse(final String contentType) {
		try (InputStream input = httpServletRequest.getServletContext()
				.getResourceAsStream("/rdfvocabulary/simulinkRDFVocabulary.rdf")) {
			if (input == null) {
				throw new WebApplicationException("RDF vocabulary resource is missing");
			}
			final String vocabulary = new String(input.readAllBytes(), StandardCharsets.UTF_8);
			return Response.ok(vocabulary, contentType).build();
		} catch (IOException exception) {
			throw new WebApplicationException(exception);
		}
	}
}
