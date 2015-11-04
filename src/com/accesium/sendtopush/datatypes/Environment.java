package com.accesium.sendtopush.datatypes;

/**
 * The environment which the application will be executed. This is used in registration process.
 * 
 * @author Isidoro Castell
 * 
 */
public enum Environment {
	/**
	 * For development applications
	 */
	SANDBOX("sandbox"),
	/**
	 * For production applications
	 */
	PRODUCTION("production");

	private String environment;

	Environment(String environment) {
		this.environment = environment;
	}

	/**
	 * @return the environment
	 */
	public String getEnvironment() {
		return environment;
	}

}
