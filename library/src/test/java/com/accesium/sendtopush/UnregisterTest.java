package com.accesium.sendtopush;

import com.accesium.sendtopush.datatypes.ServerResult;
import com.accesium.sendtopush.service.ServerRegistrationService;

import org.junit.Test;

import java.io.IOException;

import rx.Observable;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Fran Gilberte on 05/02/2016.
 */

public class UnregisterTest extends SendToPushManagerTest {

    @Test
    public void unregisterGcmReturnEmpty() {
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        //Recibimos un stream vacío.
        when(gcmService.unregister()).thenReturn(Observable.empty());

        mPushManager.unregisterRx(gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
        subscriber.assertError(IllegalArgumentException.class);
        assertEquals(subscriber.getOnErrorEvents().size(), 1);
        assertEquals(subscriber.getOnErrorEvents().get(0).getMessage(), "Invalid response from GCM");
    }

    @Test
    public void unregisterGcmReturnUnsuccess() {
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        //Llamada al proceso de desregitro devuelve false.
        when(gcmService.unregister()).thenReturn(Observable.just(false));

        mPushManager.unregisterRx(gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
        subscriber.assertError(IllegalArgumentException.class);
        assertEquals(subscriber.getOnErrorEvents().size(), 1);
        assertEquals(subscriber.getOnErrorEvents().get(0).getMessage(), "Invalid response from GCM");
    }

    @Test
    public void unregisterGcmReturnIOException() {
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        // GCM falla al desregistrar
        when(gcmService.unregister()).thenReturn(Observable.error(new IOException()));

        mPushManager.unregisterRx(gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertError(IOException.class);
        assertEquals(subscriber.getOnErrorEvents().size(), 1);
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
    }

    @Test
    public void unregisterGcmReturnSuccessButNotPreviouslyRegistered() {
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();

        //Llamada al proceso de desregitro devuelve true.
        when(gcmService.unregister()).thenReturn(Observable.just(true));

        //No esbamos registrados
        when(prefs.getUserPid()).thenReturn(null);

        mPushManager.unregisterRx(gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        assertTrue(subscriber.getOnNextEvents().get(0).isSuccess());
        subscriber.assertTerminalEvent();
        subscriber.assertCompleted();
    }


    @Test
    public void unregisterGcmReturnSuccessServerReturnSuccess() {
        ServerResult success = new ServerResult(true);
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();

        //Llamada al proceso de desregitro devuelve true.
        when(gcmService.unregister()).thenReturn(Observable.just(true));
        //Servidor devuelve success.
        when(apiService.unregisterInServer(anyString(), anyString(), anyString(), anyString())).thenReturn(Observable.just(success));
        //Estamos registrados previamente.
        when(prefs.getUserPid()).thenReturn("userPid");

        mPushManager.unregisterRx(gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(success);
        subscriber.assertTerminalEvent();
        subscriber.assertCompleted();
    }

    @Test
    public void unregisterGcmReturnSuccessServerReturnUnsuccess() {
        ServerResult success = new ServerResult(false);
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();

        //Llamada al proceso de desregitro devuelve true.
        when(gcmService.unregister()).thenReturn(Observable.just(true));
        //Servidor devuelve success.
        when(apiService.unregisterInServer(anyString(), anyString(), anyString(), anyString())).thenReturn(Observable.just(success));
        //Estamos registrados previamente.
        when(prefs.getUserPid()).thenReturn("userPid");

        mPushManager.unregisterRx(gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        assertFalse(subscriber.getOnNextEvents().get(0).isSuccess());
        subscriber.assertTerminalEvent();
        subscriber.assertCompleted();
    }

    @Test
    public void unregisterGcmReturnSuccessServerReturnNull() {
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();

        //Llamada al proceso de desregitro devuelve true.
        when(gcmService.unregister()).thenReturn(Observable.just(true));
        //Servidor devuelve null.
        when(apiService.unregisterInServer(anyString(), anyString(), anyString(), anyString())).thenReturn(Observable.just(null));
        //Estamos registrados previamente.
        when(prefs.getUserPid()).thenReturn("userPid");

        mPushManager.unregisterRx(gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        assertTrue(subscriber.getOnNextEvents().get(0).isSuccess());
        subscriber.assertTerminalEvent();
        subscriber.assertCompleted();
    }

    @Test
    public void unregisterGcmReturnSuccessServerReturnThrowable() {

        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();

        //Llamada al proceso de desregitro devuelve true.
        when(gcmService.unregister()).thenReturn(Observable.just(true));

        //Se llama al servidor con una base url no válida
        ServerRegistrationService realApiService = new ServerRegistrationService("http://www.sendtopush.com/rest_api/fake/url");

        //Estamos registrados previamente.
        when(prefs.getUserPid()).thenReturn("userPid");

        mPushManager.unregisterRx(gcmService, realApiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
        assertTrue(subscriber.getOnNextEvents().get(0).isSuccess());
        subscriber.assertTerminalEvent();
        subscriber.assertCompleted();
    }

}
