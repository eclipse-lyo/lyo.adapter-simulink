package edu.gatech.mbsec.adapter.simulink.matlab;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * Live MATLAB/Simulink checks. For the adapter endpoint test, set
 * {@code SIMULINK_BACKEND=matlab}, {@code SUBVERSION_CLIENT_IMPL=standalone},
 * {@code simulinkModelsDirectory=<module>/target/matlab-it-models}, and
 * {@code matlabScriptsDirectory=<module>/matlab} in the Maven process environment.
 */
class MatlabSimulinkIT {

	private static final int CONVERSION_ROUNDS = 3;

	@TempDir
	Path temporaryDirectory;

	@Test
	@Timeout(value = 15, unit = TimeUnit.MINUTES)
	void repeatedlyConvertsEveryPackagedModelUsingLiveMatlab() throws Exception {
		final Path sourceModels = Path.of(System.getProperty("simulink.models.directory"));
		final Path matlabScripts = Path.of(System.getProperty("matlab.scripts.directory"));
		final Path stagedModels = temporaryDirectory.resolve("simulink models");
		Files.createDirectories(stagedModels);

		final List<Path> sourceModelFiles;
		try (Stream<Path> files = Files.list(sourceModels)) {
			sourceModelFiles = files.filter(path -> path.getFileName().toString().toLowerCase().endsWith(".slx"))
					.toList();
		}
		assertTrue(sourceModelFiles.size() >= 8, "Expected the packaged Simulink model set");
		final Set<String> expectedModelNames = new HashSet<>();
		for (Path sourceModel : sourceModelFiles) {
			Files.copy(sourceModel, stagedModels.resolve(sourceModel.getFileName()));
			final String fileName = sourceModel.getFileName().toString();
			final String modelName = fileName.substring(0, fileName.lastIndexOf('.'));
			expectedModelNames.add(fileName + "---" + modelName);
		}

		final Simulink2XmiConverter converter = new Simulink2XmiConverter();
		for (int round = 1; round <= CONVERSION_ROUNDS; round++) {
			final Path xmi = converter.convert(stagedModels, matlabScripts);
			assertTrue(Files.size(xmi) > 10_000, "Round " + round + " produced unexpectedly small XMI");

			final Document document = parseXmi(xmi);
			final NodeList models = document.getElementsByTagName("model");
			final Set<String> modelNames = new HashSet<>();
			for (int i = 0; i < models.getLength(); i++) {
				modelNames.add(models.item(i).getAttributes().getNamedItem("name").getNodeValue());
			}
			assertEquals(expectedModelNames, modelNames, "Round " + round + " converted model names");
			assertTrue(document.getElementsByTagName("block").getLength() > 0,
					"Round " + round + " should contain converted blocks");
			assertTrue(document.getElementsByTagName("line").getLength() > 0,
					"Round " + round + " should contain converted connections");
		}
	}

	@Test
	void failsFastWhenTheModelsDirectoryIsMissing() {
		final Path missingModels = temporaryDirectory.resolve("missing-models");
		final Path matlabScripts = Path.of(System.getProperty("matlab.scripts.directory"));

		assertThrows(IOException.class,
				() -> new Simulink2XmiConverter().convert(missingModels, matlabScripts));
	}

	@Test
	void matlabBackedAdapterServesAConvertedModel() throws Exception {
		final HttpRequest request = HttpRequest.newBuilder(
					URI.create("http://localhost:8080/services/model4.slx---model4/model"))
				.header("Accept", "application/rdf+xml")
				.GET()
				.build();
		final HttpResponse<String> response = HttpClient.newHttpClient()
				.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("model4.slx---model4"),
				"The model resource should contain the model converted by the MATLAB backend");
	}

	private static Document parseXmi(final Path xmi) throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		return factory.newDocumentBuilder().parse(xmi.toFile());
	}
}
