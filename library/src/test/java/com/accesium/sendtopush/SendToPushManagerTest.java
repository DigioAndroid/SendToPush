package com.accesium.sendtopush;

import com.accesium.sendtopush.datatypes.Environment;
import com.accesium.sendtopush.datatypes.Preferences;
import com.accesium.sendtopush.service.GcmRegistrationService;
import com.accesium.sendtopush.service.ServerRegistrationService;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by Fran Gilberte on 04/02/2016.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class SendToPushManagerTest {
    private final static String APIKEY = "a075e8677bfb9e4f58e1261da4cddfaf841627f9";
    private final static String COMPANY = "digio";
    private final static String APPNAME = "testpush";
    private final static String GCM_SENDER_ID = "583758746761";

    SendToPushManager mPushManager;
    @Mock GcmRegistrationService gcmService;
    @Mock ServerRegistrationService apiService;
    @Mock Preferences prefs;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mPushManager = SendToPushManager.init(APIKEY, COMPANY, APPNAME, GCM_SENDER_ID, Environment.SANDBOX);
    }

}
