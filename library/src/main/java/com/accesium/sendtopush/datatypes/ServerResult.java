package com.accesium.sendtopush.datatypes;

import java.io.IOException;
import java.util.Map;

import io.reactivex.Observable;

/**
 * Represents the JSON returned from our push server in a task
 * (register/unregister)
 *
 * @author Isidoro Castell
 */
public class ServerResult {

    private boolean status;
    private String message;
    private String error;
    private Map<String, String> data;

    public ServerResult(boolean status) {
        this(status, null, null, null);
    }

    public ServerResult(boolean status, String message, String error, Map<String, String> data) {
        super();
        this.status = status;
        this.message = message;
        this.error = error;
        this.data = data;
    }

    public boolean isSuccess() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Observable<ServerResult> filterErrors() {
        if (status) {
            return Observable.just(this);
        } else {
            return Observable.error(new IOException("Server registration failed"));
        }
    }

}
