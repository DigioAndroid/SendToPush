package com.accesium.sendtopush.listeners;

import com.accesium.sendtopush.datatypes.PushError;

/**
 * Listener to receive the response from the server in a register or unregister
 * @author Isidoro Castell
 *
 */
public interface IPushResponseListener {

	/**
	 * The query with the server was succeed
	 */
	public void onSuccess();
	
	/**
	 * An error occurred during the request
	 * @param error
	 */
	public void onError(PushError error);
}
