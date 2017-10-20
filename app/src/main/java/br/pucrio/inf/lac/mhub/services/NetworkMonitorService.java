package br.pucrio.inf.lac.mhub.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;

import br.pucrio.inf.lac.mhub.components.AppUtils;
import de.greenrobot.event.EventBus;

public class NetworkMonitorService extends Service {
	/** DEBUG */
	private static final String TAG = NetworkMonitorService.class.getSimpleName();

    /** Tag used to route the message */
    public static final String ROUTE_TAG = "MON_NET";

	/** The context object */
	private Context ac;
	
	/** The keep running flag to indicate if the service is running, used internally */
	private volatile Boolean keepRunning;
	
	/** The is connected flag to indicate if the connection is active, used internally */
	private volatile Boolean isConnected;

	@Override
	public void onCreate() {
		super.onCreate();
		// initialize the flags
		keepRunning = true;
		isConnected = false;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        AppUtils.logger( 'i', TAG, ">> Started" );
		// get the context 
		ac = NetworkMonitorService.this;
		// if it is not connected, create a new thread resetting previous threads
		if( !isConnected ) {
			// start thread monitoring
			startThread();
		}
		// If we get killed, after returning from here, restart 
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind( Intent i ) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
        AppUtils.logger( 'i', TAG, ">> Destroyed" );
		// not connected
		isConnected = false;
	}
	
	/**
	 * It starts the monitor thread and keeps tracking if the device is connected
	 * and if it detects that has lost connectivity, trigers a contingency
	 */
	private void startThread() {
		Thread t = new Thread( new Runnable() {
			public void run () {
				try {
					Boolean isDisconnected = false;
					isConnected = true;
					// loop forever while the service is running
					while( keepRunning ) {
						// check for the network status
						ConnectivityManager cm = (ConnectivityManager) ac.getSystemService( Context.CONNECTIVITY_SERVICE );
						NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

						if( activeNetwork != null ) {
							boolean isConnected = activeNetwork.isConnected();

							if( !isConnected ) {
								isDisconnected = true;
								EventBus.getDefault().post("no_internet");
							}else
								isDisconnected = false;

						}else if(!isDisconnected) {
							isDisconnected = true;
							EventBus.getDefault().post("no_internet");
						}

						Thread.sleep( 5000 );
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
}
