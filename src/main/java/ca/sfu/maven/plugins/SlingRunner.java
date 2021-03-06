package ca.sfu.maven.plugins;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Goal which runs tests on Sling server
 *
 * @goal test
 *
 * @phase test
 */
public class SlingRunner extends AbstractMojo {
	/**
	 * Where JUnit results file is placed
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String outputDirectory;

	/**
	 * Sling URL where tests are run. Looks something like http://localhost:4502.
	 * @parameter
	 * @required
	 */
	private URL slingUrl;

	public void execute() throws MojoExecutionException {
		String testResults = runTests();
		writeTestResultsToFile(testResults);
	}

	protected String testFilePath() {
		return outputDirectory + "/sling-integration-test-results.xml";
	}

	protected void writeTestResultsToFile(String testResults) throws MojoExecutionException {
		try {
			getLog().info("Writing test results to " + testFilePath());
			BufferedWriter out = new BufferedWriter(new FileWriter(testFilePath()));
			out.write(testResults);
			out.close();
		}
		catch (IOException e) {
			throw new MojoExecutionException("A problem occurred while writing test results to a file", e);
		}
	}

	protected String runTests() throws MojoExecutionException {
		try {
			getLog().info("Running Sling tests on " + testUrl());
			HttpURLConnection connection = (HttpURLConnection) testUrl().openConnection();
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", "0");
			DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
			outStream.flush();
			outStream.close();

			DataInputStream inStream;
			if(connection.getResponseCode() == 404) {
				getLog().info("Zero tests found on server");
				inStream = new DataInputStream(connection.getErrorStream());
			} else {
				inStream = new DataInputStream(connection.getInputStream());
			}

			String buffer;
			String response = "";
			while((buffer = inStream.readLine()) != null) {
				response += buffer;
			}

			inStream.close();

			getLog().debug("Test results: " + response);
			return response;
		} catch(Exception e) {
			throw new MojoExecutionException("A problem occurred while running tests", e);
		}
	}

	protected URL testUrl() throws MojoExecutionException {
		String testRunnerPath = "/system/sling/junit/.xml";
		try {
			return new URL(slingUrl.toString() + testRunnerPath);
		} catch (MalformedURLException e) {
			throw new MojoExecutionException("A problem occurred while appending the test runner path ("+testRunnerPath+") to the sling URL", e);
		}
	}

	public void setSlingUrl(URL slingUrl) {
		this.slingUrl = slingUrl;
	}

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
}
