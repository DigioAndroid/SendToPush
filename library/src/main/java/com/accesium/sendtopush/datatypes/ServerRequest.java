package com.accesium.sendtopush.datatypes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A class which encapsulates the server response
 * 
 * @author Isidoro Castell
 * 
 */
public class ServerRequest {

	public final static int SUCCESS = 200;
	public final static int INCORRECT_PARAMS = 400;
	public final static int SERVER_ERROR = 500;

	private int code;
	private ServerResult serverResult;

	public ServerRequest(int code, ServerResult serverResult) {
		super();
		this.code = code;
		this.serverResult = serverResult;
	}

	public ServerRequest(int code) {
		super();
		this.code = code;
	}

	public ServerRequest(int code, String result) {
		super();
		this.code = code;
		Gson gson = new GsonBuilder().create();
		serverResult = gson.fromJson(result, ServerResult.class);
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public ServerResult getServerResult() {
		return serverResult;
	}

	public void setServerResult(ServerResult serverResult) {
		this.serverResult = serverResult;
	}

}
