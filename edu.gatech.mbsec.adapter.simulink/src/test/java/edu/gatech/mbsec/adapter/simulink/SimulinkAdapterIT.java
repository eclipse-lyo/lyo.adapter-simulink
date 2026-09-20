package edu.gatech.mbsec.adapter.simulink;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Acceptance tests that exercise the full OSLC discovery chain end-to-end
 * against the adapter deployed in Jetty (see the {@code acceptance} Maven profile):
 *
 * <pre>
 *   Service Provider Catalog --&gt; Service Provider --&gt; Query Capability --&gt; individual resource
 * </pre>
 *
 * <p>The adapter is populated from a Simulink XMI fixture ({@code simulinkWorkDir.xmi})
 * loaded by {@code SimulationModelBackendStandaloneImpl} — the default backend
 * (also selectable explicitly via {@code -Dsimulink.backend=xmi}), so no MATLAB is
 * required. The assertions verify that the RDF returned by the resources actually
 * corresponds to the XMI fixture data (model names, contained blocks, etc.).
 */
public class SimulinkAdapterIT {

	private static final String BASE = "http://localhost:8080";

	// RDF predicates as declared by the OSLC resource classes
	// (edu.gatech.mbsec.adapter.simulink.resources.*).
	private static final String RDF_VOCAB = "http://example.com/ns/oslc_simulink#";
	private static final Property MODEL_NAME = prop(RDF_VOCAB + "Model_name");
	private static final Property MODEL_BLOCK = prop(RDF_VOCAB + "Model_block");
	private static final Property BLOCK_NAME = prop(RDF_VOCAB + "Block_name");
	private static final Property BLOCK_TYPE = prop(RDF_VOCAB + "Block_type");
	// The adapter declares the MBSE Model/Block RDF types under its own OSLC namespace
	// (see the @OslcNamespace / @OslcResourceShape on the resource classes), not a
	// separate http://eclipse.org/MBSE vocabulary.
	private static final Resource MBSE_MODEL = res(RDF_VOCAB + "Model");

	private static final String OSLC_CORE = "http://open-services.net/ns/core#";
	private static final Property QUERY_CAPABILITY = prop(OSLC_CORE + "queryCapability");
	private static final Property QUERY_BASE = prop(OSLC_CORE + "queryBase");
	private static final Resource SERVICE_PROVIDER = res(OSLC_CORE + "ServiceProvider");
	private static final Property IDENTIFIER = prop("http://purl.org/dc/terms/identifier");

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = BASE;
	}

	// ------------------------------------------------------------------------
	// Step 1: the service provider catalog exposes the XMI models
	// ------------------------------------------------------------------------
	@Test
	void catalogPublishesXmiModels() {
		final Model catalog = getRdf("/services/catalog/singleton");
		assertTrue(catalog.containsResource(catalog.createResource(BASE + "/services/catalog/singleton")),
				"catalog about URI must identify the catalog endpoint");

		final Set<String> ids = new HashSet<>();
		final StmtIterator sps = catalog.listStatements(null, RDF.type, SERVICE_PROVIDER);
		while (sps.hasNext()) {
			final Statement st = sps.next();
			final Statement id = st.getSubject().getProperty(IDENTIFIER);
			if (id != null) {
				ids.add(id.getString());
			}
		}

		// The XMI fixture (simulinkWorkDir.xmi) defines exactly these two models.
		assertTrue(ids.contains("cruiseControl"),
				"catalog should expose a service provider for the cruiseControl model, got: " + ids);
		assertTrue(ids.contains("engine"),
				"catalog should expose a service provider for the engine model, got: " + ids);
	}

	// ------------------------------------------------------------------------
	// Steps 2 + 3: service provider -> query capability -> individual model resource
	// ------------------------------------------------------------------------
	@Test
	void modelQueryCapabilityResolvesToXmiModel() {
		final Model sp = getRdf("/services/serviceProviders/cruiseControl");

		// Collect the query bases advertised by this service provider and confirm
		// one of them points at the individual model resource.
		final Set<String> queryBases = new HashSet<>();
		final StmtIterator qcs = sp.listStatements(null, QUERY_CAPABILITY, (RDFNode) null);
		while (qcs.hasNext()) {
			final Resource qc = qcs.next().getObject().asResource();
			if (qc.hasProperty(QUERY_BASE)) {
				queryBases.add(qc.getProperty(QUERY_BASE).getResource().getURI());
			}
		}
		assertFalse(queryBases.isEmpty(), "service provider should advertise at least one query capability");
		assertTrue(queryBases.stream().anyMatch(u -> u.endsWith("/cruiseControl/model")),
				"expected a model query capability whose query base ends with /cruiseControl/model, got: "
						+ queryBases);

		// Follow the chain to the individual resource and check its RDF.
		final Model model = getRdf("/services/cruiseControl/model");
		final Resource modelRes = findModel(model, "cruiseControl");
		assertEquals("cruiseControl", modelRes.getProperty(MODEL_NAME).getString());
		assertEquals(MBSE_MODEL, modelRes.getProperty(RDF.type).getResource());

		// The cruiseControl model in the fixture has three blocks: Constant, Gain, Sum.
		assertEquals(3, countProperties(modelRes, MODEL_BLOCK),
				"cruiseControl RDF should reference exactly 3 blocks");
	}

	@Test
	void subversionServiceProviderExposesFileQueryCapability() {
		final Model sp = getRdf("/services/serviceProviders/subversionfiles");
		assertTrue(sp.listStatements(null, QUERY_CAPABILITY, (RDFNode) null).hasNext(),
				"Subversion service provider should expose the file query capability");
	}

	// ------------------------------------------------------------------------
	// Step 4: drill into a contained block and verify it matches the XMI
	// ------------------------------------------------------------------------
	@Test
	void blockResourceMatchesXmi() {
		final Model block = getRdf("/services/cruiseControl/blocks/Constant");
		final Resource blockRes = block.listSubjectsWithProperty(BLOCK_NAME).next();
		assertEquals("Constant", blockRes.getProperty(BLOCK_NAME).getString());
		assertEquals("Constant", blockRes.getProperty(BLOCK_TYPE).getString());
	}

	// ------------------------------------------------------------------------
	// Both XMI models are served (engine has a single Pulse block)
	// ------------------------------------------------------------------------
	@Test
	void engineModelAlsoServed() {
		final Model model = getRdf("/services/engine/model");
		final Resource modelRes = findModel(model, "engine");
		assertEquals("engine", modelRes.getProperty(MODEL_NAME).getString());
		assertEquals(1, countProperties(modelRes, MODEL_BLOCK),
				"engine RDF should reference exactly 1 block (Pulse)");
	}

	@Test
	void vocabularyUsesConfiguredAbsoluteUri() {
		final String vocabulary = given()
				.accept("application/rdf+xml")
				.when()
				.get("/services/rdfvocabulary")
				.then()
				.statusCode(200)
				.extract()
				.body()
				.asString();
		assertTrue(vocabulary.contains(RDF_VOCAB));
		assertFalse(vocabulary.contains("localhost:8181"));
	}

	// ------------------------------------------------------------------------
	// helpers
	// ------------------------------------------------------------------------
	private static Resource findModel(final Model m, final String name) {
		final ResIterator it = m.listSubjectsWithProperty(RDF.type, MBSE_MODEL);
		while (it.hasNext()) {
			final Resource r = it.nextResource();
			if (r.hasProperty(MODEL_NAME) && name.equals(r.getProperty(MODEL_NAME).getString())) {
				return r;
			}
		}
		throw new AssertionError("model " + name + " not found in RDF graph");
	}

	private static int countProperties(final Resource r, final Property p) {
		return r.listProperties(p).toList().size();
	}

	private static Model getRdf(final String path) {
		final String body = given()
				.header("OSLC-Core-Version", "2.0")
				.accept("application/rdf+xml")
				.when()
				.get(path)
				.then()
				.statusCode(200)
				.extract()
				.body()
				.asString();
		final Model m = ModelFactory.createDefaultModel();
		m.read(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)), "", "RDF/XML");
		return m;
	}

	private static Property prop(final String uri) {
		return ModelFactory.createDefaultModel().createProperty(uri);
	}

	private static Resource res(final String uri) {
		return ModelFactory.createDefaultModel().createResource(uri);
	}
}
