package com.accesium.sendtopush.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.provider.Settings.Secure;
import android.support.v4.content.LocalBroadcastManager;

import com.accesium.sendtopush.R;
import com.accesium.sendtopush.datatypes.PushError;
import com.accesium.sendtopush.datatypes.ServerRequest;
import com.accesium.sendtopush.datatypes.ServerResult;
import com.accesium.sendtopush.datatypes.ServerTask;
import com.accesium.sendtopush.tools.Log;
import com.accesium.sendtopush.util.Constants;
import com.accesium.sendtopush.util.EasySSLSocketFactory;
import com.accesium.sendtopush.util.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * The service used to register and unregister in our push server
 *
 * @author Isidoro Castell
 *
 */
public class PushRegisterService extends IntentService {

	public PushRegisterService() {
		super("PushRegisterService");
	}

	public PushRegisterService(String name) {
		super(name);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Service created");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d("Service running");

		final ServerTask task = intent.getParcelableExtra(Constants.SERVICE_TASK_KEY);

		final String apiKey = intent.getStringExtra(Constants.TASK_APIKEY);
		final String company = intent.getStringExtra(Constants.TASK_COMPANY);
		final String appname = intent.getStringExtra(Constants.TASK_APPNAME);
		final ArrayList<String> tags = intent.getStringArrayListExtra(Constants.TASK_TAGS);

		ServerRequest requestResult = null;
		final Intent resultIntent = new Intent(Constants.SERVICE_RESULT_PUSH_REQUEST);

		try {

			switch (task) {
				case REGISTER: {
					final String token = intent.getStringExtra(Constants.SERVICE_TOKEN_KEY);
					final String appUsername = intent.getStringExtra(Constants.TASK_APP_USERNAME);
					final String appversion = getApplicationVersion();
					final String devicename = android.os.Build.MODEL.toString();
					final String devicemodel = String.format("%s, %s", android.os.Build.PRODUCT, android.os.Build.MANUFACTURER);
					final String deviceversion = String.valueOf(android.os.Build.VERSION.SDK_INT);
					final String enabled = "enabled";
					final String environment = intent.getStringExtra(Constants.TASK_ENVIRONMENT);

					ServerResult result = getService().reqister(apiKey,company,	appname, Constants.TASK_REGISTER, token,
							appUsername, appversion, getUniqueID(this),devicename, devicemodel, deviceversion,
							enabled, enabled, enabled, environment, tags);

					requestResult = new ServerRequest(200, result);

				}
				break;
				case UNREGISTER: {
					final String userPid = intent.getStringExtra(Constants.TASK_PID);
					ServerResult result = getService().unreqister(apiKey,company, appname, userPid, Constants.TASK_UNREGISTER_ID);
					requestResult = new ServerRequest(200, result);
				}
				break;

				default:
					break;
			}

			Log.d("Send the request to the push server");


			if (requestResult != null && requestResult.getServerResult() != null) {
				// Parse the result
				int resultCode = requestResult.getCode();
				if (resultCode == ServerRequest.SUCCESS) {
					// Success
					resultIntent.putExtra(Constants.PUSH_REQUEST_SUCCESS, true);
					// If was register we get the pid and store it
					if (task == ServerTask.REGISTER) {
						final Map<String, String> data = requestResult.getServerResult().getData();
						if (data != null) {
							Log.d("Data: " + data.toString());
							// Parse the json in data and get the pid value
							final String pid = data.get(Constants.TASK_PID);
							// Store the pid in preferences
							SharedPreferences.Editor editor = getSharedPreferences(Constants.PREF_PUSH_FILE, Context.MODE_PRIVATE).edit();
							editor.putString(Constants.TASK_PID, pid).commit();
						}
					}
				} else {
					// Error
					resultIntent.putExtra(Constants.PUSH_REQUEST_SUCCESS, false);
					PushError error = new PushError(requestResult.getServerResult().getError(), PushError.Type.fromInt(resultCode));
					resultIntent.putExtra(Constants.PUSH_REQUEST_ERROR, error);
				}
			} else {
				// Connection error
				resultIntent.putExtra(Constants.PUSH_REQUEST_SUCCESS, false);
				PushError error = new PushError("Connection error", PushError.Type.CONNECTION_ERROR);
				resultIntent.putExtra(Constants.PUSH_REQUEST_ERROR, error);
			}
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);

		} catch(Exception ex){
			ex.printStackTrace();

			// Connection error
			resultIntent.putExtra(Constants.PUSH_REQUEST_SUCCESS, false);
			PushError error = new PushError("Connection error", PushError.Type.CONNECTION_ERROR);
			resultIntent.putExtra(Constants.PUSH_REQUEST_ERROR, error);
			LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(resultIntent);
		}


	}

	private Service getService(){

		final String serverUrl = getString(R.string.server_url_base);

		Gson gson = new GsonBuilder()
				.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
				.serializeNulls()
				.create();

		return new RestAdapter.Builder().setConverter(new GsonConverter(gson)).setEndpoint(serverUrl)
				.setClient(new OkClient(Utils.getCustomOkHttpClient()))
				.setLogLevel(RestAdapter.LogLevel.BASIC).build().create(Service.class);
	}

	interface Service {
		@GET("/gcm")
		ServerResult reqister(@Query(Constants.TASK_APIKEY) String apiKey, @Query(Constants.TASK_COMPANY) String company,
							  @Query(Constants.TASK_APPNAME) String appName,
							  @Query(Constants.TASK) String task, @Query(Constants.TASK_DEVICE_TOKEN) String token,
							  @Query(Constants.TASK_APP_USERNAME) String user, @Query(Constants.TASK_APP_VERSION) String version,
							  @Query(Constants.TASK_DEVICE_UID) String uid, @Query(Constants.TASK_DEVICE_NAME) String name,
							  @Query(Constants.TASK_DEVICE_MODEL) String model, @Query(Constants.TASK_DEVICE_VERSION) String devideVersion,
							  @Query(Constants.TASK_PUSH_BADGE) String badge, @Query(Constants.TASK_PUSH_ALERT) String alert,
							  @Query(Constants.TASK_PUSH_SOUND) String sound, @Query(Constants.TASK_ENVIRONMENT) String environment,
							  @Query(Constants.TASK_TAGS) List<String> tags);

		@GET("/gcm")
		ServerResult unreqister(@Query(Constants.TASK_APIKEY) String apiKey, @Query(Constants.TASK_COMPANY) String company,
								@Query(Constants.TASK_APPNAME) String appName, @Query(Constants.TASK_PID) String pid,
								@Query(Constants.TASK) String task);
	}


	/**
	 * Send a request via GET to the push server
	 *
	 * @param url
	 *            The url with the parameters
	 * @return The result from the server
	 */
	private ServerRequest doRequest(String url) {

		Log.d("Request: " + url);

		BufferedReader in = null;
		try {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

			HttpParams params = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is
			// established.
			// The default value is zero, that means the timeout is not used.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(params, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT)
			// in milliseconds which is the timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(params, timeoutSocket);

			params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
			params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
			params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

			ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);

			HttpClient client = new DefaultHttpClient(cm, params);
			HttpGet request = new HttpGet();

			request.setURI(new URI(url));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();

			Log.d("Response: " + sb.toString());

			return new ServerRequest(response.getStatusLine().getStatusCode(), sb.toString());
		} catch (Exception e) {
			Log.e("Exception ", e);
		}
		return null;
	}

	/**
	 * Return the application version name using in the Manifest file
	 *
	 * @return The application version name
	 */
	private String getApplicationVersion() {
		String version = null;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e("NameNotFoundException ", e);
		}
		return (version != null) ? version : "0";
	}

	/**
	 * Build a unique ID looking for IMEI, MAC code or Android UID
	 *
	 * @return A unique ID
	 */
	public static String getUniqueID(Context context) {
		// String szImei = TelephonyMgr.getDeviceId(); // Requires
		// READ_PHONE_STATE
		/*TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
		String szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE

		if (szImei != null) {
			return szImei;
		}*/

		/*¡WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();

		if (m_szWLANMAC != null) {
			return m_szWLANMAC;
		}*/

		String m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

		if (m_szAndroidID != null) {
			return m_szAndroidID;
		}

		String m_szDevIDShort = "35"
				+ // we make this look like a valid IMEI
				Build.BOARD.length() % 10 + Build.BRAND.length() % 10 + Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10
				+ Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10
				+ Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 + Build.TAGS.length() % 10 + Build.TYPE.length() % 10 + Build.USER.length()
				% 10; // 13 digits

		return m_szDevIDShort;
	}
}
