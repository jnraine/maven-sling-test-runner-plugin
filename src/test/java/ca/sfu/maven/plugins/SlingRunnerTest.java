package ca.sfu.maven.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class SlingRunnerTest {
	private SlingRunner slingRunner;

	@Before
	public void before() {
		this.slingRunner = new SlingRunner();
	}

	@Test
	/**
	 * This test only passes if you have a Sling server running on localhost:4502
	 */
	public void testRunTests() throws MojoExecutionException, MalformedURLException, SlingServerDownException {
		slingRunner.setSlingUrl(new URL("http://localhost:4502"));
		assertThat("should be a bunch of xml", slingRunner.runTests(), startsWith("<?xml"));
	}

	@Test (expected=SlingServerDownException.class)
	public void testRunTestsWithServerOutage() throws MalformedURLException, MojoExecutionException, SlingServerDownException {
		slingRunner.setSlingUrl(new URL("http://www.google.com"));
		slingRunner.runTests();
	}

	@Test
	public void testTestUrl() throws MalformedURLException, MojoExecutionException {
		slingRunner.setSlingUrl(new URL("http://localhost:4502"));
		assertThat("Path should be set to the XML JUnit servlet", slingRunner.testUrl().getPath(), is("/system/sling/junit/.xml"));
	}

	@Test
	public void testWriteTestResultsToFile() throws MojoExecutionException, IOException {
		slingRunner.setOutputDirectory("/tmp");
		slingRunner.writeTestResultsToFile("foo bar");
		String testResultsFileContents = new DataInputStream(new FileInputStream(new File(slingRunner.testFilePath()))).readLine();
		assertThat("test results file should contain foo bar", testResultsFileContents, is("foo bar"));
	}
}
