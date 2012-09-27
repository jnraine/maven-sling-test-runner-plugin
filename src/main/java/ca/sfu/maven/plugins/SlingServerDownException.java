package ca.sfu.maven.plugins;

import java.io.FileNotFoundException;

/**
 *
 */
public class SlingServerDownException extends Exception {
	public SlingServerDownException(String message, Throwable e) {
		super(message, e);
	}
}
