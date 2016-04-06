package com.accesium.sendtopush;

import com.accesium.sendtopush.datatypes.Environment;
import com.accesium.sendtopush.datatypes.ServerResult;
import com.accesium.sendtopush.service.ServerRegistrationService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.List;

import rx.Observable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Fran Gilberte on 02/02/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class RegisterTest extends SendToPushManagerTest{

    @Test
    public void testRegisterEmitsIllegalArgumentExceptionWhenGcmReturnsNull(){

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
    public void testRegisterEmitsIOExceptionWhenGcmReturnIOException(){

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
    public void testRegisterNotCallServerWhenTokenAlreadyRegistered(){

        String token = "token";
        // GCM devuelve token
        when(gcmService.register(anyString())).thenReturn(Observable.just(token));
        //Ya estamos registrados para este token
        when(prefs.getGcmToken()).thenReturn(token);

        mPushManager.registerRx(RuntimeEnvironment.application, "user", null, false, gcmService, apiService, prefs)
                .subscribe(subscriber);

        verify(apiService, never()).registerInServer(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), any(Environment.class), any(List.class));

    }

    @Test
    public void testRegisterEmitsSuccessWhenTokenAlreadyRegistered(){

        String token = "token";
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
    public void testRegisterCallsServerWhenTokenAlreadyRegisteredForceRegister(){

        String token = "token";
        ServerResult success = new ServerResult(true);

        when(gcmService.register(anyString())).thenReturn(Observable.just(token));
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

        verify(apiService).registerInServer(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), any(Environment.class), any(List.class));
    }

    @Test
    public void testRegisterEmitsServerResultWhenGcmReturnToken(){

        ServerResult success = new ServerResult(true);
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
    public void testRegisterReturnsIOExceptionWhenServerReturnUnsuccess(){

        ServerResult success = new ServerResult(false);
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
    public void testRegisterReturnsIOExceptionWhenServerReturnNull(){

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
    public void testRegisterEmitsErrorWhenServerUrlIsNotValid(){

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
