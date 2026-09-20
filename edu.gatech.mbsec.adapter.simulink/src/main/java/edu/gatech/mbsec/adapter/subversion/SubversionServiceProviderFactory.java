package edu.gatech.mbsec.adapter.subversion;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.PrefixDefinition;
import org.eclipse.lyo.oslc4j.core.model.Publisher;
import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.oslc4j.core.model.ServiceProviderFactory;

/**
 * Service provider factory for the Subversion OSLC Service Provider.
 * Generates a ServiceProvider resource exposing the SubversionFileService capabilities.
 */
public class SubversionServiceProviderFactory {

	private static final Class<?>[] RESOURCE_CLASSES = {
			SubversionFileService.class
	};
	private SubversionServiceProviderFactory() {
		super();
	}

	public static ServiceProvider createServiceProvider(final String baseURI, final String product)
			throws OslcCoreApplicationException, URISyntaxException {
		final ServiceProvider serviceProvider = ServiceProviderFactory.createServiceProvider(
				baseURI,
				"http://open-services.net/ns/subversion#",
				product,
				"Service Provider for Subversion Files",
				new Publisher("Georgia Institute of Technology OSLC Project", "urn:oslc:ServiceProvider"),
				RESOURCE_CLASSES,
				new HashMap<String, Object>()
		);

		final PrefixDefinition[] prefixDefinitions = {
				new PrefixDefinition(OslcConstants.DCTERMS_NAMESPACE_PREFIX,
						new URI(OslcConstants.DCTERMS_NAMESPACE)),
				new PrefixDefinition(OslcConstants.OSLC_CORE_NAMESPACE_PREFIX,
						new URI(OslcConstants.OSLC_CORE_NAMESPACE)),
				new PrefixDefinition(OslcConstants.OSLC_DATA_NAMESPACE_PREFIX,
						new URI(OslcConstants.OSLC_DATA_NAMESPACE)),
				new PrefixDefinition(OslcConstants.RDF_NAMESPACE_PREFIX,
						new URI(OslcConstants.RDF_NAMESPACE)),
				new PrefixDefinition(OslcConstants.RDFS_NAMESPACE_PREFIX,
						new URI(OslcConstants.RDFS_NAMESPACE))
		};

		serviceProvider.setPrefixDefinitions(prefixDefinitions);
		return serviceProvider;
	}
}
