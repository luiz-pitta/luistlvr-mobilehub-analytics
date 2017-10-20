package br.pucrio.inf.lac.mhub.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.infopae.model.BuyAnalyticsData;
import com.infopae.model.SendAnalyticsData;
import com.infopae.model.SendSensorData;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import br.pucrio.inf.lac.mhub.broadcastreceivers.BroadcastMessage;
import br.pucrio.inf.lac.mhub.components.AppConfig;
import br.pucrio.inf.lac.mhub.components.AppUtils;
import br.pucrio.inf.lac.mhub.components.Statistics;
import br.pucrio.inf.lac.mhub.models.base.LocalMessage;
import br.pucrio.inf.lac.mhub.models.base.QueryMessage;
import br.pucrio.inf.lac.mhub.models.locals.EventData;
import br.pucrio.inf.lac.mhub.models.locals.LocationData;
import br.pucrio.inf.lac.mhub.models.locals.MatchmakingData;
import br.pucrio.inf.lac.mhub.models.locals.MessageData;
import br.pucrio.inf.lac.mhub.models.locals.SensorData;
import br.pucrio.inf.lac.mhub.models.queries.MEPAQuery;
import br.pucrio.inf.lac.mhub.services.listeners.ConnectionListener;
import de.greenrobot.event.EventBus;
import lac.cnclib.net.NodeConnection;
import lac.cnclib.net.mrudp.MrUdpNodeConnection;
import lac.cnclib.sddl.message.ApplicationMessage;
import lac.cnclib.sddl.message.ClientLibProtocol.PayloadSerialization;
import lac.cnclib.sddl.message.Message;

public class ConnectionService extends Service {
	/** DEBUG */
	private static final String TAG = ConnectionService.class.getSimpleName();

    /** Tag used to route the message */
    public static final String ROUTE_TAG = "CONN";

	/** The context object */
	private Context ac;

	/** SDDL IP address */
	private String ipAddress;
	
	/** SDDL connection port */
	private Integer port;

    /** The UUID of the device */
    private UUID uuid;
	
	/** The node connection to the SDDL gateway */
	//private static NodeConnection connection;
	private NodeConnection connection;
	
	/** The connection listener for the node connection */
	private ConnectionListener listener;
	
	/** The MrUDP socket connection */
	private SocketAddress socket;
	
	/** The last location object */
	private LocationData lastLocation;
	
	/** The keep running flag to indicate if the service is running, used internally */
	private volatile Boolean keepRunning;
	
	/** The is connected flag to indicate if the connection is active, used internally */
	private volatile Boolean isConnected;

    /** The interval time between messages to be sent */
    private Integer sendAllMsgsInterval;

	/** Types of analytics services */
	private final static int GRAPH = 0;
	private final static int ALERT = 1;
	private final static int CUSTOM1 = 2;

	/** A list of messages to be sent to the gateway */
	//private final LinkedHashMap<String, Message> lstMsg = new LinkedHashMap<>();
	private final ConcurrentHashMap<String, Message> lstMsg = new ConcurrentHashMap<>();
	//private final ConcurrentLinkedQueue<Message> lstMsg = new ConcurrentLinkedQueue<>();

	/** Active Sensor Data */
	private ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Double[]>>> mActiveRequest = new ConcurrentHashMap<>();
								//macAddress				//uuidData		//uuidClient

	/** Active Analytics User */
	private ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<BuyAnalyticsData>>> mActiveRequestOption = new ConcurrentHashMap<>();
								//macAddress				//uuidData		//Option selected
	
	/** The Local Broadcast Manager */
	private LocalBroadcastManager lbm;
	
	/**
	 * The device connectivity, not related to the MrUDP connection, there are
	 * 3 types.
	 * - No Connection
	 * - 3G
	 * - WiFi
	 */
	private String deviceTypeConnectivity;

	final Object lock = new Object();

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
		ac = ConnectionService.this;
        // register to event bus
        EventBus.getDefault().register( this );
		// get local broadcast 
		lbm = LocalBroadcastManager.getInstance( ac );
		// register broadcasts
		registerBroadcasts();
		// if it is not connected, create a new thread resetting previous threads
		if( !isConnected ) {
			// call the bootstrap to initialize all the variables
			bootstrap();
			// start thread connection 
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

		SendSensorData sendSensorData = new SendSensorData();
		sendSensorData.setData(null);
		sendSensorData.setListData(null);
		sendSensorData.setSource(SendSensorData.ANALYTICS_HUB);
		sendSensorData.setUuidClients(getAllUuidList());

		if(sendSensorData.getUuidClients().size() > 0)
			createAndSendMsg(sendSensorData, uuid);

		// unregister broadcasts 
		unregisterBroadcasts();
        // unregister from event bus
        EventBus.getDefault().unregister( this );

		if( sendAllMsgsInterval <= 0 ) {
			synchronized( lock ) {
				lock.notify();
			}
		}
	}
	
	/**
	 * The bootstrap for this service, it will start and get all the default
	 * values from the SharedPreferences to start the service without any
	 * problem.
	 */
	private void bootstrap() {
		Boolean saved;
		// create the UUID for this device if there is not one
		if( AppUtils.getUuid( ac ) == null ) {
			saved = AppUtils.createSaveUuid( ac );
			if( !saved )
				AppUtils.logger( 'e', TAG, ">> UUID not saved to SharedPrefs" );
		}
		uuid = AppUtils.getUuid( ac );

		// set ip address 
		ipAddress = AppUtils.getIpAddress( ac );
		if( ipAddress == null )
			ipAddress = AppConfig.DEFAULT_SDDL_IP_ADDRESS;
		// save the ip address to SPREF 
		AppUtils.saveIpAddress( ac, ipAddress );

		// set port 
		port = AppUtils.getGatewayPort( ac );
		if( port == null )
			port = AppConfig.DEFAULT_SDDL_PORT;
		// save port to SPREF 
		AppUtils.saveGatewayPort( ac, port );

		// set the interval time between messages 
		sendAllMsgsInterval = AppUtils.getCurrentSendMessagesInterval( ac );
		if( sendAllMsgsInterval == null )
			sendAllMsgsInterval = AppConfig.DEFAULT_MESSAGES_INTERVAL_HIGH;
		AppUtils.saveCurrentSendMessagesInterval( ac, sendAllMsgsInterval );

		// start the listener here to be on another Thread 
		listener = ConnectionListener.getInstance( ac );
		//listener = new ConnectionListener( ac );
		
		// set all the default values for the options HIGH, MEDIUM and LOW on SPREF 
		if( AppUtils.getSendSignalsInterval( ac, AppConfig.SPREF_MESSAGES_INTERVAL_HIGH ) == null )
			AppUtils.saveSendSignalsInterval( ac,
					AppConfig.DEFAULT_MESSAGES_INTERVAL_HIGH,
					AppConfig.SPREF_MESSAGES_INTERVAL_HIGH );
		
		if( AppUtils.getSendSignalsInterval( ac, AppConfig.SPREF_MESSAGES_INTERVAL_MEDIUM ) == null )
			AppUtils.saveSendSignalsInterval( ac,
					AppConfig.DEFAULT_MESSAGES_INTERVAL_MEDIUM,
					AppConfig.SPREF_MESSAGES_INTERVAL_MEDIUM );
		
		if( AppUtils.getSendSignalsInterval( ac, AppConfig.SPREF_MESSAGES_INTERVAL_LOW ) == null )
			AppUtils.saveSendSignalsInterval( ac,
					AppConfig.DEFAULT_MESSAGES_INTERVAL_LOW,
					AppConfig.SPREF_MESSAGES_INTERVAL_LOW );
		
		// check for the network status 
		ConnectivityManager cm = (ConnectivityManager) ac.getSystemService( Context.CONNECTIVITY_SERVICE );
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		
		if( activeNetwork != null ) {
		    boolean isConnected = activeNetwork.isConnected();
		    boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
		    boolean is3G   = activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;
		    
		    if( isConnected && isWiFi )
		    	deviceTypeConnectivity = BroadcastMessage.INFO_CONNECTIVITY_WIFI;
		    else if( isConnected && is3G )
		    	deviceTypeConnectivity = BroadcastMessage.INFO_CONNECTIVITY_3G;
		    else if( !isConnected )
		    	deviceTypeConnectivity = BroadcastMessage.INFO_CONNECTIVITY_NO_CONNECTION;
		}
	}

	/**
	 * Calculates and sends the analysed data to IoTrade user
	 * @param macAddress MacAddress of sensor to be observed
	 * @param uuid UUID Data of sensor
	 * @param buyAnalyticsData Information of IoTrade user to be sent data
	 */
	private void calculateOption(BuyAnalyticsData buyAnalyticsData, String uuid, String macAddress, long interval){
		SendSensorData sendSensorData = new SendSensorData();
		ArrayList<String> listUuid = new ArrayList<>();
		ArrayList<Double[]> data = mActiveRequest.get(macAddress).get(uuid);
		String uuidClient = buyAnalyticsData.getUuidIotrade();
		int option = buyAnalyticsData.getOption();

		sendSensorData.setUuidClients(listUuid);
		sendSensorData.setInterval(interval);

		listUuid.add(uuidClient);

		SensorData sensorData = new SensorData();
		sensorData.setSensorName(uuidClient);
		sensorData.setSensorValue(data.get( (data.size()-1) ));
		sensorData.setRoute(MEPAService.ROUTE_TAG);

		if(uuid.equals("f000aa01-0451-4000-b000-000000000000")){
			//"Temperatura"
			if(option == GRAPH){
				sendSensorData.setListData(data);
			}else if(option == CUSTOM1){
				Statistics statisticsAmbient, statisticsTarget;
				double[] ambient = new double[data.size()];
				double[] target = new double[data.size()];
				Double[] ambientSend = new Double[2];
				Double[] targetSend = new Double[2];
				ArrayList<Double[]> sendData = new ArrayList<>();

				for(int i=0;i<data.size();i++){
					ambient[i] = data.get(i)[0];
					target[i] = data.get(i)[1];
				}

				statisticsAmbient = new Statistics(ambient);
				statisticsTarget = new Statistics(target);

				ambientSend[0] = statisticsAmbient.getMean();
				targetSend[0] = statisticsTarget.getMean();

				ambientSend[1] = statisticsAmbient.getStdDev();
				targetSend[1] = statisticsTarget.getStdDev();

				sendData.add(ambientSend);
				sendData.add(targetSend);

				sendSensorData.setListData(sendData);
			}
		} else if(uuid.equals("f000aa11-0451-4000-b000-000000000000")){
			//"Acelerômetro"
			if(option == GRAPH){
				sendSensorData.setListData(data);
			}
		} else if(uuid.equals("f000aa21-0451-4000-b000-000000000000")){
			//"Humidade"
			if(option == GRAPH){
				sendSensorData.setListData(data);
			}else if(option == CUSTOM1){
				Statistics statistics;
				double[] humidity = new double[data.size()];
				Double[] humiditySend = new Double[1];

				for(int i=0;i<data.size();i++){
					humidity[i] = data.get(i)[0];
				}

				statistics = new Statistics(humidity);

				humiditySend[0] = statistics.getMean();

				sendSensorData.setData(humiditySend);
			}
		} else if(uuid.equals("f000aa31-0451-4000-b000-000000000000")){
			//"Magnetômetro"
			if(option == GRAPH){
				sendSensorData.setListData(data);
			}
		} else if(uuid.equals("f000aa41-0451-4000-b000-000000000000")){
			//"Barômetro"
			if(option == GRAPH){
				sendSensorData.setListData(data);
			}else if(option == CUSTOM1){
				Statistics statistics;
				double[] barometer = new double[data.size()];
				Double[] barometerSend = new Double[1];

				for(int i=0;i<data.size();i++){
					barometer[i] = data.get(i)[0];
				}

				statistics = new Statistics(barometer);

				barometerSend[0] = statistics.getMean();

				sendSensorData.setData(barometerSend);
			}
		} else if(uuid.equals("f000aa51-0451-4000-b000-000000000000")){
			//"Giroscópio"
			if(option == GRAPH){
				sendSensorData.setListData(data);
			}
		} else if(uuid.equals("f000aa71-0451-4000-b000-000000000000")){
			//"Luxímetro"
			if(option == GRAPH){
				sendSensorData.setListData(data);
			}
		} else if(uuid.equals("f000aa81-0451-4000-b000-000000000000")){
			//"Movimento"
			if(option == GRAPH){
				sendSensorData.setListData(data);
			}
		}

		if(option != ALERT)
			createAndSendMsg(sendSensorData, this.uuid);
		else {
			sendSensorData.setListData(new ArrayList<>());
			sendSensorData.setData(null);
			createAndSendMsg(sendSensorData, this.uuid);
			EventBus.getDefault().post(sensorData);
		}
	}

	@SuppressWarnings("unused") //receives event from connection listner to removes a sensor data and IoTrade user
	public void onEvent( MatchmakingData matchmakingData ) {
		String uuidMatch = matchmakingData.getUuidMatch();
		String macAddress = matchmakingData.getMacAddress();
		String uuidData = matchmakingData.getUuidData();
		String uuidClient = matchmakingData.getUuidClient();
		String uuidClientAnalytics = matchmakingData.getUuidAnalyticsClient();
		boolean ack = matchmakingData.isAck();

		if(ack)
			createAndSendMsg( "a" + uuidClientAnalytics, uuid);

		if(matchmakingData.getStartStop() == MatchmakingData.STOP) {
			ConcurrentHashMap<String, ArrayList<BuyAnalyticsData>> map = mActiveRequestOption.get(macAddress);
			ArrayList<BuyAnalyticsData> arrayList = map.get(uuidData);
			int position = getUuidIoTrade(arrayList, uuidClientAnalytics);

			if (position >= 0) {
				BuyAnalyticsData buyAnalyticsData = arrayList.get(position);
				if(buyAnalyticsData.getOption() == 1){
					MEPAQuery mepa = new MEPAQuery();
					mepa.setLabel(uuidClientAnalytics);
					mepa.setObject(QueryMessage.ITEM.RULE);
					mepa.setType(QueryMessage.ACTION.REMOVE);
					EventBus.getDefault().post(mepa);
				}

				arrayList.remove(position);
				map.put(uuidData, arrayList);

				if(arrayList.size() == 0) {
					ConcurrentHashMap<String, ArrayList<Double[]>> mapInfo = mActiveRequest.get(macAddress);
					if (mapInfo.containsKey(uuidData))
						mapInfo.remove(uuidData);
				}
			}
		}
	}

    @SuppressWarnings("unused") //receives event from NetworkMonitoring service to removes a sensor data and IoTrade user
    public void onEvent( String string ) {
        if( string != null ) {
            switch (string){
                case "no_internet":
                    mActiveRequestOption.clear();
                    mActiveRequest.clear();
                    break;
            }
        }
    }

	@SuppressWarnings("unused") //receives event from connection listner and stores sensor data
	public void onEventMainThread( SendAnalyticsData sendAnalyticsData ) {
		Double[] data = sendAnalyticsData.getData();
		String macAddress = sendAnalyticsData.getMacAddress();
		String uuidData = sendAnalyticsData.getUuid();
		long interval = sendAnalyticsData.getInterval();

		if (!mActiveRequest.containsKey(macAddress)) {
			ConcurrentHashMap<String, ArrayList<Double[]>> map = new ConcurrentHashMap<>();
			ArrayList<Double[]> arrayList = new ArrayList<>();
			arrayList.add(data);
			map.put(uuidData, arrayList);
			mActiveRequest.put(macAddress, map);
		} else {
			ConcurrentHashMap<String, ArrayList<Double[]>> map = mActiveRequest.get(macAddress);
			ArrayList<Double[]> arrayList = new ArrayList<>();
			if (map.containsKey(uuidData)) {
				arrayList = map.get(uuidData);
				arrayList.add(data);
			} else {
				arrayList.add(data);
				map.put(uuidData, arrayList);
			}
			mActiveRequest.put(macAddress, map);
		}

		if(mActiveRequestOption.containsKey(macAddress)){
			ConcurrentHashMap<String, ArrayList<BuyAnalyticsData>> map = mActiveRequestOption.get(macAddress);
			if(map.containsKey(uuidData)){
				ArrayList<BuyAnalyticsData> list = map.get(uuidData);
				ArrayList<String> listUuid = new ArrayList<>();
				for(int i=0;i<list.size();i++){
					BuyAnalyticsData buyAnalyticsData = list.get(i);
					calculateOption(buyAnalyticsData, uuidData, macAddress, interval);
				}
			}
		}
	}

	@SuppressWarnings("unused") //receives event from connection listner and register an user purchase
	public void onEventMainThread( BuyAnalyticsData buyAnalyticsData ) {
		String macAddress = buyAnalyticsData.getMacAddress();
		String uuidData = buyAnalyticsData.getUuidData();

		if (!mActiveRequestOption.containsKey(macAddress)) {
			ConcurrentHashMap<String, ArrayList<BuyAnalyticsData>> map = new ConcurrentHashMap<>();
			ArrayList<BuyAnalyticsData> arrayList = new ArrayList<>();
			arrayList.add(buyAnalyticsData);
			map.put(uuidData, arrayList);
			mActiveRequestOption.put(macAddress, map);
		} else {
			ConcurrentHashMap<String, ArrayList<BuyAnalyticsData>> map = mActiveRequestOption.get(macAddress);
			ArrayList<BuyAnalyticsData> arrayList = new ArrayList<>();
			if (map.containsKey(uuidData)) {
				arrayList = map.get(uuidData);
				arrayList.add(buyAnalyticsData);
			} else {
				arrayList.add(buyAnalyticsData);
				map.put(uuidData, arrayList);
			}
			mActiveRequestOption.put(macAddress, map);
		}

		if(buyAnalyticsData.getOption() == 1){
			double value = buyAnalyticsData.getValue();
			MEPAQuery mepa = new MEPAQuery();
			mepa.setLabel(buyAnalyticsData.getUuidIotrade());
			mepa.setObject(QueryMessage.ITEM.RULE);
			mepa.setTarget(QueryMessage.ROUTE.GLOBAL);
			mepa.setType(QueryMessage.ACTION.ADD);
			mepa.setRule(generateCEPRule(macAddress, uuidData, buyAnalyticsData.getUuidIotrade(), value));
			EventBus.getDefault().post(mepa);
		}
	}

	/**
	 * Creates a cep rule to generate alerts based on a value passed as a parameter
	 * @param mac MacAddress of sensor to be observed
	 * @param uuidData UUID Data of sensor
	 * @param uuidlabel UUID of IoTrade user to serve as an identifier
	 * @param value The value to generate an alert
	 * @return cep rule
	 */
	private String generateCEPRule(String mac, String uuidData, String uuidlabel, double value){
		String begin = "SELECT ";
		String rule = " FROM SensorData (sensorName = '" + uuidlabel + "') WHERE ";
		int length;
		ArrayList<Double[]> list = mActiveRequest.get(mac).get(uuidData);
		if(list != null && list.size() > 0)
			length = list.get(0).length;
		else
			length = 1;

		for(int i=0;i<length;i++){
			begin += "sensorValue[" + i + "]";
			rule += "sensorValue[" + i + "] > " + value;
			if(length > 1 && i < (length - 1)) {
				begin += ", ";
				rule += " OR ";
			}
		}

		return begin + rule;
	}

    /**
     * Creates the MR-UDP connection
     * @return the connection
     * @throws IOException
     */
    /*public static NodeConnection getConnection() throws IOException {
        if( connection == null )
            connection = new MrUdpNodeConnection();
        return connection;
    }*/
	
	/**
	 * It starts the connection thread, it creates the connection and everything
	 * related to the connection to the gateway.
	 */
	private void startThread() {
		Thread t = new Thread( new Runnable() {
			public void run () {
				try {
					AppUtils.logger( 'i', TAG, "Thread created!! -- " + ipAddress + ":" + port );

                    //connection = getConnection();
					connection = new MrUdpNodeConnection();
					connection.addNodeConnectionListener( listener );
					socket = new InetSocketAddress( ipAddress, port );
					connection.connect( socket );
					isConnected = true;
					// set the service is running flag and is connected
                    Boolean saved = AppUtils.saveIsConnected( ac, true );
					if( !saved )
						AppUtils.logger( 'e', TAG, ">> isConnected flag not saved" );

					// loop forever while the service is running
					while( keepRunning ) {
						// kill connection
						if( !isConnected ) {
							keepRunning = false;
							connection.disconnect();
							stopThread();
						}

						// send messages if we have connection with the device
						if( isConnected && sendAllMsgsInterval > 0 ) {
                            AppUtils.logger( 'i', TAG, ">> Sending Messages(" + lstMsg.size() + ")" );
							Iterator<Map.Entry<String, Message>> it = lstMsg.entrySet().iterator();
							//Iterator<Message> it = lstMsg.iterator();

							synchronized( lstMsg ) {
								while( it.hasNext() ) {
									Map.Entry<String, Message> currentMessage = it.next();
									//Message currentMessage = it.next();
									connection.sendMessage( currentMessage.getValue() );
									//connection.sendMessage( currentMessage );
									it.remove();

								}
							}
							// This has to be changed. The disconnection will wait until the thread is wake up
							// Use handlers instead (Pendant)
							synchronized( this ) {
								Thread.sleep( sendAllMsgsInterval );
							}
						} else if( isConnected ) {
							synchronized( lock ) {
								lock.wait();
							}
						}
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
	}
	
	/**
	 * It stops the connection thread.
	 */
	private synchronized void stopThread() {
		Boolean saved = AppUtils.saveIsConnected( ac, false );
		if( !saved )
			AppUtils.logger( 'e', TAG, ">> isConnected flag not saved" );
	}

    /**
     * Creates an application message to send to the cloud in JSON
     * It includes the current location if exists to the message
     * Depending on the priority it will send the message immediately
     * or group it to be sent in an interval of time
     * @param s The Mobile Hub Message structure
     * @param sender The UUID of the Mobile Hub
     */
    private void createAndQueueMsg(LocalMessage s, UUID sender) {
        s.setUuid( sender.toString() );
        Double latitude = null, longitude = null;

        // If location service not activated, set location
        if( !AppUtils.getCurrentLocationService( ac ) ) {
            latitude = AppUtils.getLocationLatitude( ac );
            longitude = AppUtils.getLocationLongitude( ac );
        }
        // The last known location
        else if( lastLocation != null ) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }

        if( latitude != null && longitude != null ) {
            s.setLatitude( latitude );
            s.setLongitude( longitude );
        }

        try {
            ApplicationMessage am = new ApplicationMessage();
            am.setPayloadType( PayloadSerialization.JSON );
            am.setContentObject( s.toJSON() );
            am.setTagList( new ArrayList<String>() );
            am.setSenderID( sender );

            if( s.getPriority().equals( LocalMessage.HIGH ) ) {
                connection.sendMessage( am );
            } else {
                synchronized( lstMsg ) {
                    lstMsg.put( s.getID(), am );
					//lstMsg.add( am );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * Creates an application message to send to the cloud
	 * It will send the message immediately
	 * @param s The Mobile Hub Message structure
	 * @param sender The UUID of the Mobile Hub
	 */
	private void createAndSendMsg(Serializable s, UUID sender) {

		try {
			ApplicationMessage am = new ApplicationMessage();
			am.setContentObject( s );
			am.setTagList( new ArrayList<String>() );
			am.setSenderID( sender );

			connection.sendMessage( am );
		} catch (Exception e) {
			AppUtils.logger( 'i', TAG, "Error sending..." );
		}
	}

	/**
	 * Register/Unregister the broadcast receiver.
	 */
	private void registerBroadcasts() {
		IntentFilter filter = new IntentFilter();
		filter.addAction( BroadcastMessage.ACTION_CHANGE_MESSAGES_INTERVAL );
		filter.addAction( BroadcastMessage.ACTION_CONNECTIVITY_CHANGED );
		
		lbm.registerReceiver( mConnBroadcastReceiver, filter );
	}
	
	private void unregisterBroadcasts() {
        if( lbm != null )
		    lbm.unregisterReceiver( mConnBroadcastReceiver );
	}

    @SuppressWarnings("unused") // it's actually used to receive events from the Location Service
    public void onEvent( LocationData locData ) {
        if( locData != null && AppUtils.isInRoute( ROUTE_TAG, locData.getRoute() ) ) {
			AppUtils.logger( 'i', TAG, ">> NEW_LOCATION_MSG" );
            //create the message
            locData.setConnectionType( deviceTypeConnectivity );
            // set the battery status
            IntentFilter battFilter = new IntentFilter( Intent.ACTION_BATTERY_CHANGED );
            Intent iBatt = ac.registerReceiver( null, battFilter );

            // No battery present
            if( iBatt == null ) {
                AppUtils.logger( 'e', TAG, "No Battery Present" );
            } else {
                int level = iBatt.getIntExtra( BatteryManager.EXTRA_LEVEL, -1 );
                float scale = iBatt.getIntExtra( BatteryManager.EXTRA_SCALE, -1 );
                int battLevel = (int) ( ( level / scale ) * 100 );
                locData.setBatteryPercent( battLevel );
                // set the battery charging
                int charging = iBatt.getIntExtra( BatteryManager.EXTRA_STATUS, -1 );
                locData.setCharging( charging == BatteryManager.BATTERY_STATUS_CHARGING );
            }

            // save the last location (used when a new msg is send)
            lastLocation = locData;
            // add the message to the queue
            //createAndQueueMsg( locData, uuid );
        }
    }

    @SuppressWarnings("unused") // it's actually used to receive events from the S2PA Service
    public void onEvent( SensorData sensorData ) {
        // Look if the message is for this service
        //if( sensorData != null && AppUtils.isInRoute( ROUTE_TAG, sensorData.getRoute() ) )
		//	createAndQueueMsg( sensorData, uuid );
    }

    @SuppressWarnings("unused") // it's actually used to receive events from the MEPA Service
    public void onEvent( EventData eventData ) {
        // Look if the message is for this service
        if( eventData != null && AppUtils.isInRoute( ROUTE_TAG, eventData.getRoute() ) )
			createAndQueueMsg( eventData, uuid );
    }

    @SuppressWarnings("unused") // it's actually used to receive error events
    public void onEvent( MessageData messageData ) {
        //createAndQueueMsg( messageData, uuid );
    }

	@SuppressWarnings("unused")	// it's actually used to receive events from the Connection Listener
	public void onEvent( SendSensorData sendSensorData ) {
		if( sendSensorData != null ) {
			createAndSendMsg( sendSensorData, uuid );
		}
	}

	/**
	 * @return Returns all IoTrade users UUID that are registered to this analytics hub
	 */
	private ArrayList<String> getAllUuidList() {
		ArrayList<String> list = new ArrayList<>();
		for (String key : mActiveRequestOption.keySet()) {
			for (String key2 : mActiveRequestOption.get(key).keySet()) {
				ArrayList<BuyAnalyticsData> arrayList = mActiveRequestOption.get(key).get(key2);
				for(BuyAnalyticsData buyAnalyticsData : arrayList){
					list.add(buyAnalyticsData.getUuidIotrade());
				}
			}
		}
		return list;
	}

	/**
	 * @param arrayList List of IoTrade Users.
	 * @param uuidClientAnalytics IoTrade User UUID to be found on the list
	 * @return Returns position of IoTrade user in the list
	 */
	private int getUuidIoTrade(ArrayList<BuyAnalyticsData> arrayList, String uuidClientAnalytics){
		for(int i=0;i<arrayList.size();i++){
			BuyAnalyticsData buyAnalyticsData = arrayList.get(i);
			if(buyAnalyticsData.getUuidIotrade().equals(uuidClientAnalytics)) {
				return i;
			}
		}
		return -1;
	}

    /**
     * The broadcast receiver for all the services, it will receive all the
     * updates from the location/mepa/s2pa service and send it to the gateway.
     */
    private BroadcastReceiver mConnBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String action = i.getAction();

			/* Broadcast: ACTION_CHANGE_SEND_MESSAGES_INTERVAL */
			/* *********************************************** */
            if( action.equals( BroadcastMessage.ACTION_CHANGE_MESSAGES_INTERVAL ) ) {
                if( !AppUtils.getCurrentEnergyManager( ac ) )
                    return;

                sendAllMsgsInterval = i.getIntExtra( BroadcastMessage.EXTRA_CHANGE_MESSAGES_INTERVAL, -1 );
                // problem getting the value from the extra, set the default value
                if( sendAllMsgsInterval < 0 )
                    sendAllMsgsInterval = AppConfig.DEFAULT_MESSAGES_INTERVAL_HIGH;
                // save the preferences with the new value
                AppUtils.saveCurrentSendMessagesInterval( ac, sendAllMsgsInterval );
            }
			/* Broadcast: ACTION_CONNECTIVITY_CHANGED */
			/* ************************************** */
            else if( action.equals( BroadcastMessage.ACTION_CONNECTIVITY_CHANGED ) ) {
                deviceTypeConnectivity = i.getStringExtra( BroadcastMessage.EXTRA_CONNECTIVITY_CHANGED );
            }
        }
    };
}
