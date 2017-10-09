package br.pucrio.inf.lac.mhub.s2pa.technologies.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.provider.Settings;
import android.widget.Toast;

import com.infopae.model.SendActuatorData;
import com.infopae.model.SendSensorData;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import br.pucrio.inf.lac.mhub.components.AppUtils;
import br.pucrio.inf.lac.mhub.components.MOUUID;
import br.pucrio.inf.lac.mhub.models.locals.MatchmakingData;
import br.pucrio.inf.lac.mhub.model_server.Response;
import br.pucrio.inf.lac.mhub.model_server.Sensor;
import br.pucrio.inf.lac.mhub.model_server.SensorWrapper;
import br.pucrio.inf.lac.mhub.models.DeviceModel;
import br.pucrio.inf.lac.mhub.network.NetworkUtil;
import br.pucrio.inf.lac.mhub.s2pa.base.Technology;
import br.pucrio.inf.lac.mhub.s2pa.base.TechnologyListener;
import br.pucrio.inf.lac.mhub.s2pa.base.TechnologySensor;
import br.pucrio.inf.lac.mhub.s2pa.technologies.ble.operations.BLEDisconnect;
import br.pucrio.inf.lac.mhub.s2pa.technologies.ble.operations.BLENone;
import br.pucrio.inf.lac.mhub.s2pa.technologies.ble.operations.BLEOperation;
import br.pucrio.inf.lac.mhub.s2pa.technologies.ble.operations.BLERead;
import br.pucrio.inf.lac.mhub.s2pa.technologies.ble.operations.BLEReadRssi;
import br.pucrio.inf.lac.mhub.s2pa.technologies.ble.operations.BLEWrite;
import br.pucrio.inf.lac.mhub.services.AdaptationService;
import de.greenrobot.event.EventBus;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class BLETechnology extends ResultReceiver implements Technology {
	/** DEBUG */
	private static final String TAG = BLETechnology.class.getSimpleName();
		
	/** Service context */
	private Context ac;
	
	/** Technology ID */
	public final static int ID = 1;

	/** Bluetooth Generals */
	private BluetoothManager mBluetoothManager = null;
	private BluetoothAdapter mBluetoothAdapter = null;
	
	/** Scanner for BLE devices */
	private BLEDevicesScanner scanner;
	
	/** RSSI read handler */
    private Handler mTimerHandler;
	
    /** Black List Devices */
	private List<String> mBlackListDevices;

    /** White List Devices */
    private List<String> mWhiteListDevices;

	/** Connected Devices */
	private ConcurrentHashMap<String, MobileObject> mConnectedDevices;
	
	/** Active Device Types */
	private ConcurrentHashMap<String, ArrayList<Sensor>> mDeviceModules;

	/** Active Sensor Times */
	private ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> mDeviceSensorTimes;

	/** Active Sensor Times */
	private ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<String>>> mActiveRequest;
							//macAddress				//uuidData		//uuidClient
	
	/** Queue to handle connections */
	private Queue<Object> sOperationsQueue;
	private Object sCurrentOperation = null;
    
    /** RSSI allowed for connections and to keep connected */
    private Integer allowedRssi;
    
	/** Listener implemented by the S2PA service */
	private TechnologyListener listener;
	
	/** Flags */
	private boolean autoConnect = false;

	private CompositeDisposable mSubscriptions;
	
	/** defines (in milliseconds) how often RSSI should be updated */ 
    private final static int RSSI_UPDATE_TIME_INTERVAL = 1500; // 1.5 seconds

    private byte[] mCalibrationData = null;

	/**
	 * BLETechnology constructor
	 * @param context Service 
	 */
	public BLETechnology( Context context ) {
        super( new Handler() );
        this.ac = context;
	}
	
	@Override
	public boolean initialize() {
		sOperationsQueue   = new ConcurrentLinkedQueue<>();
		mConnectedDevices  = new ConcurrentHashMap<>();
        mDeviceModules     = new ConcurrentHashMap<>();
		mDeviceSensorTimes = new ConcurrentHashMap<>();
		mActiveRequest = new ConcurrentHashMap<>();
		mBlackListDevices = new ArrayList<>();

		// register to event bus
		EventBus.getDefault().register( this );
		
		mTimerHandler = new Handler();

		mSubscriptions = new CompositeDisposable();

        // Bluetooth available
		mBluetoothManager = (BluetoothManager) ac.getSystemService( Context.BLUETOOTH_SERVICE );
		if( mBluetoothManager == null )
			return false;
		
		// Adapter available
		mBluetoothAdapter = mBluetoothManager.getAdapter();
		if( mBluetoothAdapter == null )
			return false;
		
		// BLE available
		if( !ac.getPackageManager().hasSystemFeature( PackageManager.FEATURE_BLUETOOTH_LE ) )
			return false;
		
		sCurrentOperation = null;

        // creates the scanner for BLE devices
		scanner = new BLEDevicesScanner( mBluetoothAdapter, new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
    			final String macAddress = device.getAddress();
    			final String deviceName = device.getName();
    			
    			AppUtils.logger('i', TAG, "onLeScan: " + macAddress + "(" + deviceName + ") RSSI: " + rssi);

                if( listener != null )
    			    listener.onMObjectFound( new MOUUID( ID, macAddress ), (double) rssi );
                else
                    AppUtils.logger( 'i', TAG, "Listener not set" );

                allowedRssi = AppUtils.getCurrentSignalAllowedMO( ac );
    			if( autoConnect && rssi >= allowedRssi )
    				connect( macAddress );
            }
        });
		
		IntentFilter filter = new IntentFilter( BluetoothAdapter.ACTION_STATE_CHANGED );
	    ac.registerReceiver( mReceiver, filter );
	    
	    startMonitoringRssiValue();

	    return true;
	}
	
	@Override
	public void enable() throws NullPointerException {
        if( mBluetoothAdapter == null )
            throw new NullPointerException( "Technology not initialized" );
		else if( !mBluetoothAdapter.isEnabled() )
			mBluetoothAdapter.enable();
	}
	
	@Override
	public void destroy() {
		stopMonitoringRssiValue();
		
		ac.unregisterReceiver( mReceiver );

		// unregister from event bus
		EventBus.getDefault().unregister( this );

        if( scanner != null ) {
            scanner.stop();
            scanner = null;
        }

		if(mSubscriptions != null)
			mSubscriptions.dispose();
		
		sOperationsQueue.clear();
        sCurrentOperation = null;

        for( String macAddress : mConnectedDevices.keySet() )
            disconnect( macAddress );

        mConnectedDevices.clear();
	}
	
	@Override
	public void setListener( TechnologyListener listener ) {
		this.listener = listener;
	}
	
	@Override
	public void startScan( boolean autoConnect ) throws NullPointerException {
		this.autoConnect = autoConnect;
        if( mBluetoothAdapter == null )
            throw new NullPointerException( "Technology not initialized" );
		else if( mBluetoothAdapter.isEnabled() )
			scanner.start();
	}

    @Override
    public void stopScan() throws NullPointerException {
        if( mBluetoothAdapter == null )
            throw new NullPointerException( "Technology not initialized" );
        else if( mBluetoothAdapter.isEnabled() )
            scanner.stop();
    }

    @Override
	public boolean connect( final String macAddress ) {
		if( mBluetoothAdapter == null || macAddress == null || mBlackListDevices.contains( macAddress ) ) {
			AppUtils.logger('w', TAG, "Connect: BluetoothAdapter not initialized, unspecified address or black list");
			return false;
		}

        // Validate the Bluetooth Address
        if( !BluetoothAdapter.checkBluetoothAddress( macAddress ) ) {
            AppUtils.logger( 'w', TAG, "Connect: Wrong Address" );
            return false;
        }

        // Check if the device is in the blacklist
        if( isInBlackList( macAddress ) ) {
            AppUtils.logger( 'w', TAG, "Connect: Device is in the black list." );
            return false;
        }

		// Get the devices
		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( macAddress );

		// Get if the device is already waiting to connect
		if( sOperationsQueue.contains( device ) ) {
			AppUtils.logger( 'w', TAG, "Connect: Device already waiting to connect." );
			return false;
		}
		
		// Attempt to connect to the GATT if correct state
		int connectionState = mBluetoothManager.getConnectionState( device, BluetoothProfile.GATT );
		if( connectionState == BluetoothProfile.STATE_DISCONNECTED ) {			
			queueOperation( device );
			return true;
		} 
		
		AppUtils.logger( 'w', TAG, "Attempt to connect in state: " + connectionState );
		return false;
	}
	
	@Override
	public boolean disconnect( final String macAddress ) {
		if( mBluetoothAdapter == null || macAddress == null ) {
			AppUtils.logger( 'w', TAG, "Disconnect: BluetoothAdapter not initialized or unspecified address" );
			return false;
		}

        // Validate the Bluetooth Address
        if( !BluetoothAdapter.checkBluetoothAddress( macAddress ) ) {
            AppUtils.logger( 'w', TAG, "Disconnect: Wrong Address" );
            return false;
        }
		
		// Get the GATT of the connected device
		final BluetoothGatt gatt = mConnectedDevices.get( macAddress ).mGatt;
		if( gatt == null ) {
			AppUtils.logger( 'w', TAG, "Disconnect: Device not connected" );
			return false;
		}
		
		// Disconnect from the GATT if correct state
		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice( macAddress );
	    int connectionState = mBluetoothManager.getConnectionState( device, BluetoothProfile.GATT );
	    if( connectionState != BluetoothProfile.STATE_DISCONNECTED ) {
			queueOperation( new BLEDisconnect( gatt ) );
			return true;
		} 
		
	    AppUtils.logger( 'w', TAG, "Attempt to disconnect in state: " + connectionState );
		return false;
	}

	@SuppressWarnings("unused")
	public void onEvent( MatchmakingData matchmakingData ) {
	}

	private void updateSensorParameters(Sensor sensor) {

		mSubscriptions.add(NetworkUtil.getRetrofit().setSensorParameters(sensor)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeOn(Schedulers.io())
				.subscribe(this::handleResponse,this::handleError));
	}

	private void removeSensorMobileHub(Sensor sensor) {

		mSubscriptions.add(NetworkUtil.getRetrofit().removeSensorMobileHub(sensor)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeOn(Schedulers.io())
				.subscribe(this::handleResponse,this::handleError));
	}

    private void handleResponse(Response response) {
    }

    private void handleError(Throwable error) {

    }
	
	@Override
	public void readSensorValue( String macAddress, String serviceName ) {
		if( mBluetoothAdapter == null )
            AppUtils.logger( 'w', TAG, "BluetoothAdapter not initialized" );
	}

	@Override
	public void writeSensorValue( String macAddress, String serviceID, Object value ) {
		if( mBluetoothAdapter == null ) {
			AppUtils.logger( 'w', TAG, "BluetoothAdapter not initialized" );
		}
	}
	
	@Override
	public void addToBlackList( String macAddress ) {
		mBlackListDevices.add( macAddress );

        if( mConnectedDevices.containsKey( macAddress ) )
            disconnect( macAddress );
	}

	@Override
	public boolean removeFromBlackList( String macAddress ) {
		return mBlackListDevices.remove( macAddress );
	}

	@Override
	public void clearBlackList() {
		mBlackListDevices.clear();
	}

	@Override
	public boolean isInBlackList( String macAddress ) {
        return mBlackListDevices.contains( macAddress );
    }

    @Override
    public void addToWhiteList( String macAddress ) {
        mWhiteListDevices.add( macAddress );
    }

    @Override
    public boolean removeFromWhiteList( String macAddress ) {
        return false;
    }

    @Override
    public void clearWhiteList() {
        mWhiteListDevices.clear();
    }

    @Override
    public boolean isInWhiteList( String macAddress ) {
        return mWhiteListDevices.contains( macAddress );
    }

    /**
     * Operation's Queue
     */
	private synchronized void setCurrentOperation( Object currentOperation ) {
    	sCurrentOperation = currentOperation;
    }
	
    private synchronized void queueOperation( Object gattOperation ) {
    	sOperationsQueue.add( gattOperation );
        doOperation();
    }
    
    private synchronized void doOperation() {
    	if( sCurrentOperation != null ) 
            return;
    	
    	if( sOperationsQueue.size() == 0 ) 
            return;
        
    	final Object operation = sOperationsQueue.poll();
    	setCurrentOperation( operation );
    	
    	if( operation instanceof BluetoothDevice ) {
            getDeviceStructureAndConnect( (BluetoothDevice) operation );
        }
    	else if( operation == sCurrentOperation ) {
    		( (BLEOperation) operation).execute();
    	}
    }

    /**
     * Looks for the device structure and tries to connect
     * @param device The device desired to connect with
     */
    private void getDeviceStructureAndConnect( final BluetoothDevice device ) {
        final String deviceName = device.getName();
        if( deviceName == null ) {
            setCurrentOperation( null );
            doOperation();
        }
    	else if( mDeviceModules.containsKey( deviceName ) ) {
            device.connectGatt( ac, false, new MobileObject( device ) );
        }
        else {
            DeviceModel model = new DeviceModel( deviceName, device.getAddress(), this );
            EventBus.getDefault().post( model );
        }
    } 
    
    /** starts monitoring RSSI value */
    public void  startMonitoringRssiValue() {
    	mTimerHandler.postDelayed( mReadRSSI, RSSI_UPDATE_TIME_INTERVAL );
    }
    
    /** stops monitoring of RSSI value */
    public void stopMonitoringRssiValue() {
    	mTimerHandler.removeCallbacks( mReadRSSI );
    }
    
    // Runnable for te read of RSSI
    private Runnable mReadRSSI = new Runnable() {
	   @Override
	   public void run() {
		   if( mBluetoothAdapter != null || !mConnectedDevices.isEmpty() ) {
			   // request RSSI value
			   for( MobileObject value : mConnectedDevices.values() ) 
				   queueOperation( new BLEReadRssi( value.mGatt ) );
		   }
		   mTimerHandler.postDelayed( mReadRSSI, RSSI_UPDATE_TIME_INTERVAL );
	   }
	};
    
    // Gatt callback (Mobile Object)
 	public class MobileObject extends BluetoothGattCallback {
 		/** Bluetooth Device */
 		private final BluetoothDevice mDevice;

		/** Mobile Object Unique Identifier */
		private final MOUUID mMOUUID;

		/** Services provided */
		private final List<String> mServices;

		/** Gatt of the Mobile Object (Only when correctly connected) */
		private BluetoothGatt mGatt;

 		/** Current RSSI */
 		private Double mRSSI;

 		/** Queue and flag to handle operations (Connection) */
 		private final Queue<BLEOperation> mActionQueue;
 		private boolean sIsExecuting = false;

 		/** M-Object Configuration Descriptor and calibration data */
 	    private final UUID CONFIG_DESCRIPTOR = UUID.fromString( "00002902-0000-1000-8000-00805f9b34fb" );
		private final byte[] CALIBRATION_DATA = new byte[] { 2 };

		/** Current calibration UUID and sensor */
		private UUID mCalibration;
		private TechnologySensor mSensor;

        private long mLastClickTime = 0;

        private static final long TIME_UPDATE_REFRESH = 10000;
        private static final long TIME_UPDATE_REFRESH2 = 5000;
 		
 	    public MobileObject( BluetoothDevice device ) {
            final String macAddress = device.getAddress();

 			mDevice      = device;
            mMOUUID      = new MOUUID( ID, macAddress );
			mServices    = new ArrayList<>();
			mActionQueue = new ConcurrentLinkedQueue<>();

			EventBus.getDefault().register( this );
 		}
 	    
 		@Override
        public void onConnectionStateChange( BluetoothGatt gatt, int status, int newState ) {
 			final String macAddress = mDevice.getAddress();

         	AppUtils.logger( 'i', TAG,
                    macAddress + ": " + "Connection State Change: " +
                    AppUtils.gattState( status ) + " -> " +
                    AppUtils.connectionState( newState )
            );

            /* Successfully connected */
            if( status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED ) {
                AppUtils.logger( 'i', TAG, "=>Connected: " + macAddress );
                // Get the gatt object
            	mGatt = gatt;
                // Add to connected devices
            	mConnectedDevices.put( macAddress, this );
                // Inform to the S2PA Service
             	listener.onMObjectConnected( mMOUUID );
             	// Discovering services
             	mGatt.discoverServices();
            }
            /* Disconnected */
            else if( status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED ) {
                AppUtils.logger( 'i', TAG, "=>Disconnected: " + macAddress );
				// Disconnect (no error)
				sendDisconnection( gatt, macAddress, false );
             	// Start with next operation
				queue( new BLENone(), true );
            }
            /* If there is a failure at any stage */
            else if( status != BluetoothGatt.GATT_SUCCESS ) {
                AppUtils.logger( 'e', TAG, "=>Gatt Failed: " + macAddress );
				// Disconnect
				sendDisconnection( gatt, macAddress, true );
				// Continue with next operation
				queue( new BLENone(), true );
            }
            // Remove the device from the waiting list (to connect)
            sOperationsQueue.remove( mDevice );
 		}

 		private int isServiceIn(UUID service, ArrayList<Sensor> list){
			for(int i = 0;i<list.size();i++){
				Sensor sensor = list.get(i);
				if(service.toString().equals(sensor.getUuidSer()))
					return i;
			}
			return -1;
		}

		private boolean isActuator(UUID service, ArrayList<Sensor> list){
			for(int i = 0;i<list.size();i++){
				Sensor sensor = list.get(i);
				if(service.toString().equals(sensor.getUuidSer()))
					return sensor.isActuator();
			}
			return false;
		}

        private void convertSensorData(Sensor sensor) {

            mSubscriptions.add(NetworkUtil.getRetrofit().convertSensorData(sensor)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(this::handleResponseData,this::handleError));
        }

        private void handleError(Throwable error) {

        }

        private void handleResponseData(Response response) {
            Double[] data = response.getData();
			ArrayList<String> listClients = mActiveRequest.get(response.getMacAddress()).get(response.getUuid());

			SendSensorData sendSensorData = new SendSensorData();
			sendSensorData.setData(data);
			sendSensorData.setUuidClients(listClients);

			EventBus.getDefault().post( sendSensorData );

            //if( data != null )
            //    listener.onMObjectValueRead( mMOUUID, mRSSI, getSensorCategory(list, response.getUuid()), data );
        }

        private String getSensorCategory(ArrayList<Sensor> list, String uuidData){
            for(int i = 0;i < list.size(); i++){
                String uuid = list.get(i).getUuidData();
                if(uuid.equals(uuidData))
                    return list.get(i).getName();
            }
            return null;
        }

        @Override
        public void onServicesDiscovered( BluetoothGatt gatt, int status ) {
        	final String macAddress = gatt.getDevice().getAddress();
         	final String deviceName = gatt.getDevice().getName();

            /* Services discovered successfully */
         	if( status == BluetoothGatt.GATT_SUCCESS ) {
         		AppUtils.logger( 'i', TAG, "=>Services Discovered: " + macAddress );
                // Get the module for the device
             	ArrayList<Sensor> listSensors = mDeviceModules.get( deviceName );
            	// Loop through the services
                for( BluetoothGattService serv : gatt.getServices() ) {
                    // Get the sensor(s) of a service by its UUID
                    int isSensorFound = isServiceIn(serv.getUuid(), listSensors);
					// Check if actuator
					boolean isActuator = isActuator(serv.getUuid(), listSensors);
                    // Enable the sensors and add to the sensor's list
                    if( isSensorFound >= 0 && !isActuator)
						subscribe(gatt, listSensors.get(isSensorFound));
					else if(isSensorFound >= 0 && isActuator)
						activate(gatt, listSensors.get(isSensorFound));
                }
                // Inform to the S2PA Service
             	listener.onMObjectServicesDiscovered( mMOUUID, mServices );
                // Continue with the next operation
				queue( new BLENone() );
 			}
            /* If there is a failure at any stage */
            else {
 				AppUtils.logger( 'e', TAG, "=>Services Discovery Failed: " + macAddress );
                // Disconnect
				sendDisconnection( gatt, macAddress, true );
                // Continue with next operation
				queue( new BLENone(), true );
 			}
        }

        @Override
		public void onCharacteristicRead( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status ) {
			/* Calibrate sensor and continue with next Mobile Object operation */
			if( mCalibration != null && characteristic.getUuid().equals( mCalibration ) ) {
                mCalibrationData = characteristic.getValue();
				//mSensor.setCalibrationData( characteristic.getValue() );
				mCalibration = null;
				mSensor = null;

				sIsExecuting = false;
				execute();
			}
			/* Send sensor value and continue with next BLE operation */
			else {
				sendSensorValue( gatt, characteristic );
				queue( new BLENone() );
			}
        }
        
        @Override
        public void onDescriptorRead( BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status ) {
			// Continue with the next operation
			sIsExecuting = false;
			execute();
        }

        @Override
        public void onCharacteristicWrite( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status ) {
			/* Continue with the next Mobile Object operation */
			if( sIsExecuting ) {
				sIsExecuting = false;
				execute();
			}
			/* Continue with the next BLE operation */
			else
				queue( new BLENone() );
        }
         
        @Override
        public void onDescriptorWrite( BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status ) {
			// Continue with the next operation
			sIsExecuting = false;
			execute();
        }
        
        @Override
        public void onReadRemoteRssi( BluetoothGatt gatt, int rssi, int status ) {
            // RSSI obtained successfully
        	if( status == BluetoothGatt.GATT_SUCCESS ) {
                mRSSI = (double) rssi;
                // If RSSI is lower than the allowed, disconnect
                allowedRssi = AppUtils.getCurrentSignalAllowedMO( ac );
        		if( mRSSI < allowedRssi )
        			queueOperation( new BLEDisconnect( mGatt ) );
				else {
                    if (SystemClock.elapsedRealtime() - mLastClickTime >= TIME_UPDATE_REFRESH2){
                        mLastClickTime = SystemClock.elapsedRealtime();
                        Sensor sensor = new Sensor();
                        sensor.setRssi(rssi);
                        sensor.setName(Settings.Secure.getString(ac.getContentResolver(), Settings.Secure.ANDROID_ID));
                        sensor.setMacAddress(gatt.getDevice().getAddress());
                        updateSensorParameters(sensor);
                    }
				}
        	}
			// Continue with the next operation
			queue( new BLENone() );
        }

        @Override
        public void onCharacteristicChanged( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic ) {
			//AppUtils.logger( 'i', TAG, String.valueOf(SystemClock.elapsedRealtime() - elapsed) );
			//elapsed = SystemClock.elapsedRealtime();

			sendSensorValue( gatt, characteristic );
        }

        private byte[] getEnableByteArray(List<Byte> byteList){
            Byte[] ENABLE_SENSOR;

            if(byteList.size() > 0) {
                ENABLE_SENSOR = new Byte[byteList.size()];
                ENABLE_SENSOR = byteList.toArray(ENABLE_SENSOR);
            }else
                ENABLE_SENSOR = new Byte[]{0x00};

            return ArrayUtils.toPrimitive(ENABLE_SENSOR);
        }

        /**
         * Enables sensor and notifications of a service
         * @param gatt The Gatt connection in BLE
         * @param sensor The representation of the sensors
         */
        private void subscribe( BluetoothGatt gatt, Sensor sensor ) {
			AppUtils.logger( 'i', TAG, "=>Enabling " + sensor.getName() );
			mServices.add( sensor.getUuidSer() );
			// Get enabler code
            byte[] ENABLE_SENSOR = getEnableByteArray(sensor.getEnable());
			//ENABLE_SENSOR[0] = sensor.getEnable();

			// Get UUIDs of the service, data and configurations
			final UUID UUID_SERV = UUID.fromString(sensor.getUuidSer());
			final UUID UUID_DATA = UUID.fromString(sensor.getUuidData());
			final UUID UUID_CONF = UUID.fromString(sensor.getUuidConf());
			final UUID UUID_CALI = sensor.getUuidCali() != null ? UUID.fromString(sensor.getUuidCali()) : null;
			// Get the service and characteristics by UUID
			BluetoothGattService serv = gatt.getService( UUID_SERV );
			BluetoothGattCharacteristic characteristic = serv.getCharacteristic( UUID_DATA );
			// Get configuration descriptor
			BluetoothGattDescriptor config = characteristic.getDescriptor( CONFIG_DESCRIPTOR );
			gatt.setCharacteristicNotification( characteristic, true );
			// Check if the device requires calibration
			if( UUID_CALI != null ) {
				mCalibration = UUID_CALI;
				//mSensor = sensor;
				BluetoothGattCharacteristic configuration = serv.getCharacteristic( UUID_CONF );
				queue( new BLEWrite( gatt, configuration, CALIBRATION_DATA ) );

				BluetoothGattCharacteristic calibration = serv.getCharacteristic( UUID_CALI );
				queue( new BLERead( gatt, calibration ) );
			}
			// Check if the device requires to be enabled
			if( UUID_CONF != null ) {
				BluetoothGattCharacteristic configuration = serv.getCharacteristic( UUID_CONF );
				queue( new BLEWrite( gatt, configuration, ENABLE_SENSOR ) );
			}
			// Enable the notifications for the service
			queue( new BLEWrite( gatt, config, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE ) );

        }

		/**
		 * Enables sensor and notifications of a service
		 * @param gatt The Gatt connection in BLE
		 * @param sensor The representation of the actuator
		 */
		private void activate( BluetoothGatt gatt, Sensor sensor ) {
			mServices.add( sensor.getUuidSer() );

			// Get enabler code
			byte[] REMOTE_MODE = getEnableByteArray(sensor.getEnable());
			byte[] INITIATE = new byte[]{0x00};

			// Get UUIDs of the service, data and configurations
			UUID UUID_SERV = UUID.fromString(sensor.getUuidSer());
			UUID UUID_DATA = UUID.fromString(sensor.getUuidData());
			UUID UUID_CONF = UUID.fromString(sensor.getUuidConf());

			// Get the service and characteristics by UUID
			BluetoothGattService service = gatt.getService( UUID_SERV );
			BluetoothGattCharacteristic configuration = service.getCharacteristic( UUID_CONF );

			BluetoothGattCharacteristic data = service.getCharacteristic( UUID_DATA );
			queue( new BLEWrite( gatt, data, INITIATE ) );

			queue( new BLEWrite( gatt, configuration, REMOTE_MODE ) );
		}

		@SuppressWarnings("unused")
		public void onEvent( SendActuatorData sendActuatorData ) {
			ArrayList<Sensor> arrayList = mDeviceModules.get(mGatt.getDevice().getName());
			Sensor sensor = null;
			for(int i=0;i<arrayList.size();i++){
				Sensor s = arrayList.get(i);
				if(s.getUuidData().equals(sendActuatorData.getUuidData()))
					sensor = s;

			}

			if(sensor != null)
				executeCommandActuator(sensor, sendActuatorData.getCommand());
		}

		/**
		 * Enables sensor and notifications of a service
		 * @param sensor The representation of the actuator
		 */
		private void executeCommandActuator( Sensor sensor, byte[] COMMAND ) {
			// Get UUIDs of the service, data
			UUID UUID_SERV = UUID.fromString(sensor.getUuidSer());
			UUID UUID_DATA = UUID.fromString(sensor.getUuidData());

			// Get the service and characteristics by UUID
			BluetoothGattService service = mGatt.getService( UUID_SERV );
			BluetoothGattCharacteristic data = service.getCharacteristic( UUID_DATA );
			queue( new BLEWrite( mGatt, data, COMMAND ) );
		}

        /**
         * Refresh the cache information of the GATT
         * @param gatt the GATT desired to refresh
         * @return If it was successful
         */
        private boolean refreshDeviceCache( BluetoothGatt gatt ) {
            try {
                // Get method by reflection
                Method localMethod = gatt.getClass().getMethod( "refresh", new Class[0] );
                if( localMethod != null ) {
                    return (Boolean) localMethod.invoke( gatt, new Object[0] );
                }
            } catch( Exception ex ) {
                AppUtils.logger( 'e', TAG, "An exception occurred while refreshing device: " + ex.getMessage() );
            }
            return false;
        }

		/**
		 * Allow only one operation to execute at a time
		 * @param o The object to execute, can be a BLEOperation or Bluetooth Device
		 * @param clear Clears the action queue, normally in case of a disconnection
		 */
		private synchronized void queue( final BLEOperation o, boolean clear ) {
			if( clear ) mActionQueue.clear();
			mActionQueue.add( o );
			execute();
		}

		/**
		 * Allow only one operation to execute at a time
		 * @param o The object to execute, can be a BLEOperation or Bluetooth Device
		 */
		private synchronized void queue( final BLEOperation o ) {
			queue( o, false );
		}

		/**
		 * Executes the operation in top of the Action Queue
		 */
		private synchronized void execute() {
			if( sIsExecuting )
				return;

			if( mActionQueue.size() == 0 )
				return;

			final BLEOperation operation = mActionQueue.poll();
			sIsExecuting = operation.execute();

			if( operation instanceof BLENone ) {
				setCurrentOperation( null );
				doOperation();
			}
		}

		/**
		 * Gets the sensor value from the characteristic by applying the respective transformation
		 * @param gatt The gatt of the device
		 * @param characteristic The characteristic with raw values
		 */
		private void sendSensorValue( BluetoothGatt gatt, BluetoothGattCharacteristic characteristic ) {
			ConcurrentHashMap<String, Long> map = mDeviceSensorTimes.get(gatt.getDevice().getAddress());

			if (SystemClock.elapsedRealtime() - map.get(characteristic.getUuid().toString()) < TIME_UPDATE_REFRESH)
				return;

			map.put(characteristic.getUuid().toString(), SystemClock.elapsedRealtime());

			if(mActiveRequest.containsKey(gatt.getDevice().getAddress())){
				ConcurrentHashMap<String, ArrayList<String>> concurrentHashMap = mActiveRequest.get(gatt.getDevice().getAddress());
				if(concurrentHashMap.containsKey(characteristic.getUuid().toString())){

					ArrayList<String> arrayList = concurrentHashMap.get(characteristic.getUuid().toString());

					if(arrayList.size() > 0) {
						Sensor sensor = new Sensor();
						sensor.setValue(characteristic.getValue());
						sensor.setUuidData(characteristic.getUuid().toString());
						sensor.setCalibrationData(mCalibrationData);
						sensor.setName(gatt.getDevice().getName());
						sensor.setMacAddress(gatt.getDevice().getAddress());
						convertSensorData(sensor);

						listener.onMObjectValueRead(mMOUUID, mRSSI, characteristic.getService().getUuid().toString(), new Double[]{1.0});
					}
				}
			}else
				listener.onMObjectValueRead( mMOUUID, mRSSI, characteristic.getService().getUuid().toString(), new Double[]{0.0} );



		}

		/**
		 * Disconnects from a device, either for an error or disconnection call
		 * @param gatt The BluetoothGahtt of the connection
		 * @param macAddress The address of the device
		 */
		private void sendDisconnection( BluetoothGatt gatt, String macAddress, boolean error ) {
			// Refresh the Gatt if error
			if( error )
				refreshDeviceCache( gatt );
			// Close the gatt
			gatt.close();
			// Remove from connected devi ces
			mConnectedDevices.remove( macAddress );
			// Inform to the S2PA Service
			listener.onMObjectDisconnected( mMOUUID, mServices );

            EventBus.getDefault().unregister( this );

			Sensor sensor = new Sensor();
			sensor.setName(Settings.Secure.getString(ac.getContentResolver(), Settings.Secure.ANDROID_ID));
			sensor.setMacAddress(gatt.getDevice().getAddress());
			removeSensorMobileHub(sensor);
		}
 	}
 	
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
 	    @Override
 	    public void onReceive(Context context, Intent intent) {
 	        final String action = intent.getAction();

 	        if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
 	            final int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR );
 	            
 	            switch( state ) {
	 	            case BluetoothAdapter.STATE_TURNING_ON:
	 	            		initialize();
	 	                break;
	 	                
	 	            case BluetoothAdapter.STATE_TURNING_OFF:
	 	            	break;
	 	            	
	 	            case BluetoothAdapter.STATE_OFF:
	 	            	break;
 	            }
 	        }
 	    }
 	};

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        switch( resultCode ) {
            case AdaptationService.SUCCESS:
                // Got the module from the AdaptationService
                SensorWrapper sensorWrapper = (SensorWrapper) resultData.getSerializable( AdaptationService.EXTRA_RESULT );
                // Get the device
                BluetoothDevice device = (BluetoothDevice) sCurrentOperation;
                // Save the module
                if( sensorWrapper != null ) {
                    mDeviceModules.putIfAbsent( device.getName(), sensorWrapper.getListSensor() );

					ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();
					ArrayList<Sensor> arrayList = mDeviceModules.get(device.getName());

					for (int i=0;i<arrayList.size();i++){
						Sensor sensor = arrayList.get(i);
						map.putIfAbsent(sensor.getUuidData(), 0L);
					}

					mDeviceSensorTimes.putIfAbsent(device.getAddress(), map);

                    // Tries to connect with the Mobile Object
                    device.connectGatt( ac, false, new MobileObject( device ) );
                } else {
                    // Remove from connected devices
                    mConnectedDevices.remove( device.getAddress() );
                    // Start with next operation
                    setCurrentOperation( null );
                    doOperation();
                }

                break;

            case AdaptationService.FAILED:
                setCurrentOperation( null );
                doOperation();
                break;
        }
    }
}
