package com.accesium.sendtopush;

import com.accesium.sendtopush.datatypes.ServerResult;
import com.accesium.sendtopush.service.ServerRegistrationService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;

import rx.Observable;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Fran Gilberte on 02/02/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RegisterTest extends SendToPushManagerTest{

    @Test
    public void registerGcmReturnNull(){
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        //Recibimos un token nulo o un stream vacío
        when(gcmService.register(anyString())).thenReturn(Observable.just(null));

        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, false, gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
        subscriber.assertError(IllegalArgumentException.class);
        assertEquals(subscriber.getOnErrorEvents().size(), 1);
        assertEquals(subscriber.getOnErrorEvents().get(0).getMessage(), "Invalid response from GCM");
        assertFalse(subscriber.getOnErrorEvents().contains(IOException.class));


    }

    @Test
    public void registerGcmReturnIOException(){
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        // GCM falla al obtener el token
        when(gcmService.register(anyString())).thenReturn(Observable.error(new IOException()));

        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, false, gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertError(IOException.class);
        assertEquals(subscriber.getOnErrorEvents().size(), 1);
        subscriber.assertNoValues();
        subscriber.assertNotCompleted();
    }

    @Test
    public void registerInServerWithTokenAlreadyRegistered(){
        String token = "token";
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        // GCM devuelve token
        when(gcmService.register(anyString())).thenReturn(Observable.just(token));
        //Ya estamos registrados para este token
        when(prefs.getGcmToken()).thenReturn(token);

        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, false, gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        assertEquals(subscriber.getOnNextEvents().size(), 1);
        assertTrue(subscriber.getOnNextEvents().get(0).isSuccess());
        subscriber.assertCompleted();
    }

    @Test
    public void registerInServerWithTokenAlreadyRegisteredForceRegister(){
        String token = "token";
        ServerResult success = new ServerResult(true);
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        // GCM devuelve token
        when(gcmService.register(anyString())).thenReturn(Observable.just(token));
        //Ya estamos registrados para este token
        when(prefs.getGcmToken()).thenReturn(token);
        //Servidor devuelve success
        when(apiService.registerInServer(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyObject(), anyList())).thenReturn(Observable.just(success));
        //registro con flag forceRegister activado
        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, true, gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertTerminalEvent();
        subscriber.assertCompleted();
        subscriber.assertValues(success);
    }

    @Test
    public void registerGcmReturnTokenServerReturnSuccess(){
        ServerResult success = new ServerResult(true);
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        // GCM devuelve token
        when(gcmService.register(anyString())).thenReturn(Observable.just("Token"));
        //Servidor devuelve success
        when(apiService.registerInServer(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyObject(), anyList())).thenReturn(Observable.just(success));

        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, false, gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertNoErrors();
        subscriber.assertValues(success);
        subscriber.assertTerminalEvent();
        subscriber.assertCompleted();

    }

    @Test
    public void registerGcmReturnTokenServerReturnUnsuccess(){
        ServerResult success = new ServerResult(false);
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        // GCM devuelve token
        when(gcmService.register(anyString())).thenReturn(Observable.just("Token"));
        //Servidor devuelve Unsuccess
        when(apiService.registerInServer(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyObject(), anyList())).thenReturn(Observable.just(success));

        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, false, gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertError(IOException.class);
        assertEquals(subscriber.getOnErrorEvents().size(), 1);
        assertEquals(subscriber.getOnErrorEvents().get(0).getMessage(), "Server registration failed");
        subscriber.assertNoValues();
        subscriber.assertTerminalEvent();
        subscriber.assertNotCompleted();
    }

    @Test
    public void registerGcmReturnTokenServerReturnNull(){
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        // GCM devuelve token
        when(gcmService.register(anyString())).thenReturn(Observable.just("Token"));
        //Recibimos una respuesta del servidor nula o un stream vacío
        when(apiService.registerInServer(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyObject(), anyList())).thenReturn(Observable.just(null));

        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, false, gcmService, apiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertError(IOException.class);
        assertEquals(subscriber.getOnErrorEvents().size(), 1);
        assertEquals(subscriber.getOnErrorEvents().get(0).getMessage(), "Invalid response from server");
        subscriber.assertNoValues();
        subscriber.assertTerminalEvent();
        subscriber.assertNotCompleted();
    }

    @Test
    public void registerGcmReturnTokenServerReturnThrowable(){
        TestSubscriber<ServerResult> subscriber = new TestSubscriber<>();
        // GCM devuelve token
        when(gcmService.register(anyString())).thenReturn(Observable.just("Token"));
        //Se llama al servidor con una base url no válida
        ServerRegistrationService realApiService = new ServerRegistrationService("http://www.sendtopush.com/rest_api/fake/url");

        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, false, gcmService, realApiService, prefs)
                .subscribe(subscriber);

        subscriber.awaitTerminalEvent();
        subscriber.assertError(Throwable.class);
        assertEquals(subscriber.getOnErrorEvents().size(), 1);
        subscriber.assertNoValues();
        subscriber.assertTerminalEvent();
        subscriber.assertNotCompleted();
    }

}
