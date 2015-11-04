package com.accesium.sendtopush.datatypes;

/**
 * The possible status for a preference.
 * 
 * @author Isidoro Castell
 * 
 */
public enum PushStateType {
	/**
	 * Enable state
	 */
	ENABLE,
	/**
	 * Disable state
	 */
	DISABLE,
	/**
	 * Use the system configuration
	 */
	SYSTEM;
	
	/**
	 * Build a type from a boolean
	 * @param state True if enable, otherwise disable
	 * @return The state object
	 */
	public static PushStateType fromBoolean(boolean state){
		if(state){
			return ENABLE;
		}
		
		return DISABLE;
	}
}
