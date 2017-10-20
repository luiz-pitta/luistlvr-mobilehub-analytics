package br.pucrio.inf.lac.mhub.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.infopae.model.BuyAnalyticsData;
import com.infopae.model.SendActuatorData;
import com.infopae.model.SendAnalyticsData;
import com.infopae.model.SendSensorData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import br.pucrio.inf.lac.mhub.R;
import br.pucrio.inf.lac.mhub.adapters.NavigationDrawerAdapter;
import br.pucrio.inf.lac.mhub.components.AppConfig;
import br.pucrio.inf.lac.mhub.components.AppUtils;
import br.pucrio.inf.lac.mhub.components.Statistics;
import br.pucrio.inf.lac.mhub.model_server.Response;
import br.pucrio.inf.lac.mhub.model_server.Sensor;
import br.pucrio.inf.lac.mhub.model_server.User;
import br.pucrio.inf.lac.mhub.models.ConnectionData;
import br.pucrio.inf.lac.mhub.models.NavigationItem;
import br.pucrio.inf.lac.mhub.models.base.QueryMessage;
import br.pucrio.inf.lac.mhub.models.locals.SensorData;
import br.pucrio.inf.lac.mhub.models.queries.MEPAQuery;
import br.pucrio.inf.lac.mhub.network.NetworkUtil;
import br.pucrio.inf.lac.mhub.services.ConnectionService;
import br.pucrio.inf.lac.mhub.services.LocationService;
import br.pucrio.inf.lac.mhub.services.MEPAService;
import br.pucrio.inf.lac.mhub.services.S2PAService;
import de.greenrobot.event.EventBus;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Main GUI window of the M-Hub, used as a menu and for general
 * configurations. It also starts/stops the S2PA Service.
 */
public class MHubSettings extends AppCompatActivity implements ListView.OnItemClickListener {
	/** The context object */
	private Context ac;

    /** GUI Controllers */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    /** Current connection state */
    private ConnectionData connState;

    /** Progress dialog for disconnection */
    private ProgressDialog disconnectDialog;

    /** Component to make server request */
    private CompositeDisposable mSubscriptions;

    /** BroadcastReceiver */
    private BroadcastReceiver mMessageReceiverDisable = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setMobileHubDisabled();
        }
    };

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_main);

        mSubscriptions = new CompositeDisposable();

        setupGUI();
        updateData( savedInstanceState );

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiverDisable, new IntentFilter("disable_mobile_hub"));
    }

    @Override
    public void onStart() {
        super.onStart();
        // register to event bus
        EventBus.getDefault().registerSticky( this );
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister from event bus
        EventBus.getDefault().unregister( this );
    }

    /**
     * The method used to logout analytics user.
     */
    private void setMobileHubDisabled() {
        UUID uuid = AppUtils.getUuid( ac );
        SharedPreferences config = ac.getSharedPreferences( AppConfig.SHARED_PREF_FILE, MODE_PRIVATE );
        String strName = config.getString( AppConfig.NAME, "");

        User user = new User();
        user.setUuid(uuid);
        user.setName(strName);
        user.setDevice(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
        user.setActive(false);

        if(uuid != null)
            registerAnalytics(user);
    }

    /**
     * The method used to register state in server of analytics user.
     * @param usr The new location object.
     */
    private void registerAnalytics(User usr) {

        mSubscriptions.add(NetworkUtil.getRetrofit().setAnalyticsMobileHub(usr)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError));
    }

    /**
     * Callback called when updates user's state returns successfully.
     *
     * @param response register user state.
     */
    private void handleResponse(Response response) {
    }

    /**
     * Callback called when updates user's state returns with error.
     *
     * @param error returns the error.
     */
    private void handleError(Throwable error) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Dismiss the dialogs
        if( disconnectDialog != null ) {
            disconnectDialog.dismiss();
            disconnectDialog = null;
        }

        if(mSubscriptions != null)
            mSubscriptions.dispose();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate( savedInstanceState );
        mDrawerToggle.syncState();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate( R.menu.mhub_settings, menu );

		if( !AppUtils.isMyServiceRunning( ac, S2PAService.class.getName() ) ) {
			MenuItem mi = menu.findItem( R.id.service_state );
			mi.setIcon( android.R.drawable.ic_media_play );
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected( final MenuItem item ) {
        if( mDrawerToggle.onOptionsItemSelected( item ) ) {
            return true;
        }

        final Intent iS2P = new Intent( MHubSettings.this, S2PAService.class );

		switch( item.getItemId() ) {
			case R.id.service_state:
				if( AppUtils.isMyServiceRunning( ac, S2PAService.class.getName() ) ) {
                    setMobileHubDisabled();

					item.setIcon( android.R.drawable.ic_media_play );
                    // Show loading while disconnecting
                    if( connState != null && connState.getState().equals( ConnectionData.CONNECTED ) ) {
                        disconnectDialog = ProgressDialog.show(
                                ac,
                                "",
                                getString( R.string.message_disconnecting )
                        );
                    }
                    // Stop services
					stopService( iS2P );
				} else {
					item.setIcon( R.drawable.ic_media_stop );
				    startService( iS2P );
				}
    			break;

            case R.id.clear_data:
                AlertDialog.Builder builder = new AlertDialog.Builder( ac );
                builder.setMessage( R.string.message_confirm );
                builder.setPositiveButton( R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        AppUtils.trimPreferences( ac );
                        AppUtils.trimCache( ac );

                        if( AppUtils.isMyServiceRunning( ac, S2PAService.class.getName() ) ) {
                            item.setIcon( android.R.drawable.ic_media_play );
                            stopService( iS2P );
                        }

                        recreate();
                    }
                } );
                builder.setNegativeButton( R.string.no, null );
                builder.show();
                break;
		}

		return super.onOptionsItemSelected( item );
	}

    @Override
    public void onBackPressed() {
        if( mDrawerLayout.isDrawerOpen( GravityCompat.START ) )
            mDrawerLayout.closeDrawer( GravityCompat.START );
        else
            super.onBackPressed();
    }

    private void setupGUI() {
        // get the context
        ac = MHubSettings.this;
        // global components
        mDrawerLayout = (DrawerLayout) findViewById( R.id.drawerLayout );
        mDrawerList   = (ListView) findViewById(R.id.left_drawer);
        // Only used at creation
        Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );

        setSupportActionBar( toolbar );
        if( getSupportActionBar() != null )
            getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        mDrawerToggle = new ActionBarDrawerToggle( this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close ) {
            @Override
            public void onDrawerClosed( View drawerView) {
                super.onDrawerClosed( drawerView );
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened( drawerView );
            }
        };

        mDrawerLayout.setDrawerShadow( R.drawable.drawer_shadow, GravityCompat.START );
        mDrawerLayout.setDrawerListener( mDrawerToggle );

        mDrawerList.setOnItemClickListener( this );
    }

    private void updateData( Bundle savedInstanceState ) {
        String[] mTitles = getResources().getStringArray( R.array.nav_options );

        ArrayList<NavigationItem> items = new ArrayList<>();
        items.add( new NavigationItem( mTitles[ 0 ], R.drawable.settings ) );
        items.add( new NavigationItem( mTitles[ 1 ], R.drawable.view ) );
        items.add( new NavigationItem( mTitles[ 2 ], R.drawable.events ) );

        mDrawerList.setAdapter( new NavigationDrawerAdapter( this, items ) );

        if( savedInstanceState == null ) {
            selectItem( 0 );
        }
    }

    private void selectItem(int position) {
        Fragment fragment;

        switch( position ) {
            default:
            case 0:
                fragment = new PrefsFragmentInner();
                break;

            case 1:
                fragment = new MHubViewer();
                break;

            case 2:
                fragment = new MHubEvents();
                break;
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace( R.id.content, fragment ).commit();

        mDrawerList.setItemChecked( position, true );
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem( position );
    }

    @SuppressWarnings("unused") // it receives events from the Connection Listener
    public void onEventMainThread( ConnectionData connection ) {
        // Verifies if it was previously connected
        if( disconnectDialog != null && connection.getState().equals( ConnectionData.DISCONNECTED ) ) {
            disconnectDialog.dismiss();
            disconnectDialog = null;
        }
        // Save the latest state
        connState = connection;
    }

    public static class PrefsFragmentInner extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    	private Context c;

    	private EditTextPreference
    		ETP_SPREF_GATEWAY_IP_ADDRESS,
    		ETP_SPREF_GATEWAY_PORT,

            ETP_SPREF_CURRENT_LATITUDE,
            ETP_SPREF_CURRENT_LONGITUDE,

    		ETP_SPREF_CURRENT_SEND_SIGNALS_INTERVAL,
    		ETP_SPREF_MESSAGES_INTERVAL_HIGH,
    		ETP_SPREF_MESSAGES_INTERVAL_MEDIUM,
    		ETP_SPREF_MESSAGES_INTERVAL_LOW,

    		ETP_SPREF_CURRENT_LOCATION_INTERVAL,
    		ETP_SPREF_LOCATION_INTERVAL_HIGH,
    		ETP_SPREF_LOCATION_INTERVAL_MEDIUM,
    		ETP_SPREF_LOCATION_INTERVAL_LOW,

    		ETP_SPREF_CURRENT_SCAN_INTERVAL,
    		ETP_SPREF_SCAN_INTERVAL_HIGH,
    		ETP_SPREF_SCAN_INTERVAL_MEDIUM,
    		ETP_SPREF_SCAN_INTERVAL_LOW;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

    		PreferenceManager prefMgr = getPreferenceManager();
            prefMgr.setSharedPreferencesName( AppConfig.SHARED_PREF_FILE );
            prefMgr.setSharedPreferencesMode( MODE_PRIVATE );

            addPreferencesFromResource( R.xml.fragment_settings);

            setupGUI();
            updateData( false );
        }

        private void setupGUI() {
            // get the context
            c = getActivity();
            // set the title
            getActivity().setTitle( R.string.title_activity_mhub_settings );

        	ETP_SPREF_GATEWAY_IP_ADDRESS = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_GATEWAY_IP_ADDRESS );
    		ETP_SPREF_GATEWAY_PORT = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_GATEWAY_PORT );

            ETP_SPREF_CURRENT_LATITUDE = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_LOCATION_LATITUDE );
            ETP_SPREF_CURRENT_LONGITUDE = (EditTextPreference) getPreferenceScreen()
                    .findPreference( AppConfig.SPREF_LOCATION_LONGITUDE );

    		ETP_SPREF_CURRENT_SEND_SIGNALS_INTERVAL = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_CURRENT_MESSAGES_INTERVAL );
    		ETP_SPREF_MESSAGES_INTERVAL_HIGH = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_MESSAGES_INTERVAL_HIGH );
    		ETP_SPREF_MESSAGES_INTERVAL_MEDIUM = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_MESSAGES_INTERVAL_MEDIUM );
    		ETP_SPREF_MESSAGES_INTERVAL_LOW = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_MESSAGES_INTERVAL_LOW );

    		ETP_SPREF_CURRENT_LOCATION_INTERVAL = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_CURRENT_LOCATION_INTERVAL );
    		ETP_SPREF_LOCATION_INTERVAL_HIGH = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_LOCATION_INTERVAL_HIGH );
    		ETP_SPREF_LOCATION_INTERVAL_MEDIUM = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_LOCATION_INTERVAL_MEDIUM );
    		ETP_SPREF_LOCATION_INTERVAL_LOW = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_LOCATION_INTERVAL_LOW );

    		ETP_SPREF_CURRENT_SCAN_INTERVAL = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_CURRENT_SCAN_INTERVAL );
    		ETP_SPREF_SCAN_INTERVAL_HIGH = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_SCAN_INTERVAL_HIGH );
    		ETP_SPREF_SCAN_INTERVAL_MEDIUM = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_SCAN_INTERVAL_MEDIUM );
    		ETP_SPREF_SCAN_INTERVAL_LOW = (EditTextPreference) getPreferenceScreen()
    				.findPreference( AppConfig.SPREF_SCAN_INTERVAL_LOW );
        }

        private void updateData( boolean refresh ) {
            Intent iLoc  = new Intent( c, LocationService.class );

            if( AppUtils.getCurrentLocationService( c ) ) {
                ETP_SPREF_CURRENT_LATITUDE.setEnabled( false );
                ETP_SPREF_CURRENT_LONGITUDE.setEnabled( false );

                if( refresh && AppUtils.isMyServiceRunning( c, S2PAService.class.getName() ) )
                    c.startService( iLoc );
            } else {
                ETP_SPREF_CURRENT_LATITUDE.setEnabled( true );
                ETP_SPREF_CURRENT_LONGITUDE.setEnabled( true );

                if( refresh && AppUtils.isMyServiceRunning( c, LocationService.class.getName() ) )
                    c.stopService( iLoc );
            }
        }

        @Override
		public void onResume() {
    		super.onResume();
    		// register listener to update when value change
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener( this );
            setAllSummaries();
    	}

        @Override
        public void onPause() {
    		super.onPause();
    		// unregister listener
    		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener( this );
    	}

        private String isFlagSetOrNull(String value) {
    		if( value == null )
    			return "null";
    		return value;
    	}

        private String isFlagSetOrNull(Double value) {
            if( value == null )
                return "null";
            return value.toString();
        }

    	private String isFlagSetOrNull(Integer value) {
    		if( value == null )
    			return "null";
    		return value.toString();
    	}

        private void setAllSummaries() {
    		ETP_SPREF_GATEWAY_IP_ADDRESS.setSummary( isFlagSetOrNull( AppUtils.getIpAddress( c ) ) );
    		ETP_SPREF_GATEWAY_PORT.setSummary( isFlagSetOrNull( AppUtils.getGatewayPort( c ) ) );

            ETP_SPREF_CURRENT_LATITUDE.setSummary( isFlagSetOrNull( AppUtils.getLocationLatitude(c) ) );
            ETP_SPREF_CURRENT_LONGITUDE.setSummary( isFlagSetOrNull( AppUtils.getLocationLongitude(c) ) );

    		ETP_SPREF_CURRENT_SEND_SIGNALS_INTERVAL.setSummary( isFlagSetOrNull( AppUtils.getCurrentSendMessagesInterval( c ) ) );
    		ETP_SPREF_MESSAGES_INTERVAL_HIGH.setSummary( isFlagSetOrNull( AppUtils.getSendSignalsInterval( c, AppConfig.SPREF_MESSAGES_INTERVAL_HIGH ) ) );
    		ETP_SPREF_MESSAGES_INTERVAL_MEDIUM.setSummary( isFlagSetOrNull( AppUtils.getSendSignalsInterval( c, AppConfig.SPREF_MESSAGES_INTERVAL_MEDIUM ) ) );
    		ETP_SPREF_MESSAGES_INTERVAL_LOW.setSummary( isFlagSetOrNull( AppUtils.getSendSignalsInterval( c, AppConfig.SPREF_MESSAGES_INTERVAL_LOW ) ) );

    		ETP_SPREF_CURRENT_LOCATION_INTERVAL.setSummary( isFlagSetOrNull( AppUtils.getCurrentLocationInterval( c ) ) );
    		ETP_SPREF_LOCATION_INTERVAL_HIGH.setSummary( isFlagSetOrNull( AppUtils.getLocationInterval( c, AppConfig.SPREF_LOCATION_INTERVAL_HIGH)));
    		ETP_SPREF_LOCATION_INTERVAL_MEDIUM.setSummary( isFlagSetOrNull( AppUtils.getLocationInterval( c, AppConfig.SPREF_LOCATION_INTERVAL_MEDIUM)));
    		ETP_SPREF_LOCATION_INTERVAL_LOW.setSummary( isFlagSetOrNull( AppUtils.getLocationInterval( c, AppConfig.SPREF_LOCATION_INTERVAL_LOW)));

    		ETP_SPREF_CURRENT_SCAN_INTERVAL.setSummary( isFlagSetOrNull( AppUtils.getCurrentScanInterval( c ) ) );
    		ETP_SPREF_SCAN_INTERVAL_HIGH.setSummary( isFlagSetOrNull(AppUtils.getScanInterval(c, AppConfig.SPREF_SCAN_INTERVAL_HIGH ) ) );
    		ETP_SPREF_SCAN_INTERVAL_MEDIUM.setSummary( isFlagSetOrNull(AppUtils.getScanInterval(c, AppConfig.SPREF_SCAN_INTERVAL_MEDIUM ) ) );
    		ETP_SPREF_SCAN_INTERVAL_LOW.setSummary (isFlagSetOrNull( AppUtils.getScanInterval( c, AppConfig.SPREF_SCAN_INTERVAL_LOW ) ) );
    	}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			setAllSummaries();
            updateData( true );
		}
    }
}
