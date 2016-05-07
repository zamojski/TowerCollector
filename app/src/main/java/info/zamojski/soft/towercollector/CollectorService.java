/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

import info.zamojski.soft.towercollector.broadcast.ExternalIntentBroadcaster;
import info.zamojski.soft.towercollector.enums.GpsStatus;
import info.zamojski.soft.towercollector.enums.KeepScreenOnMode;
import info.zamojski.soft.towercollector.enums.MeansOfTransport;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.enums.Validity;
import info.zamojski.soft.towercollector.events.GpsStatusChangedEvent;
import info.zamojski.soft.towercollector.events.MeasurementProcessedEvent;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.SystemTimeChangedEvent;
import info.zamojski.soft.towercollector.collector.CollectorNotificationHelper;
import info.zamojski.soft.towercollector.collector.MeasurementUpdater;
import info.zamojski.soft.towercollector.collector.ParseResult;
import info.zamojski.soft.towercollector.collector.parsers.MeasurementParser;
import info.zamojski.soft.towercollector.collector.parsers.MeasurementParserFactory;
import info.zamojski.soft.towercollector.collector.validators.LocationValidator;
import info.zamojski.soft.towercollector.collector.validators.SystemTimeValidator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.model.Statistics;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;
import info.zamojski.soft.towercollector.utils.MobileUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import de.greenrobot.event.EventBus;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CollectorService extends Service {

    private static final String TAG = CollectorService.class.getSimpleName();

    public static final String SERVICE_FULL_NAME = CollectorService.class.getCanonicalName();
    public static final String BROADCAST_INTENT_STOP_SERVICE = SERVICE_FULL_NAME + ".CollectorStop";
    static final String INTENT_KEY_TRANSPORT_MODE = MeansOfTransport.class.getCanonicalName();
    static final String INTENT_KEY_KEEP_SCREEN_ON_MODE = "CollectorKeepScreenOnMode";
    public static final int NOTIFICATION_ID = 'C';
    private static final int CONDITIONS_NOT_ACHIEVED_COUNTER_INIT = 3;
    private static final int STATIC_LISTENER_INTERVAL = 15000;// milliseconds
    private static final int STATIC_LISTENER_DISTANCE = 0;// meters (always)
    private static final int GPS_STATUS_CHECK_INTERVAL = 15000;// milliseconds
    private static final int CELL_UPDATE_INTERVAL = 10000;// milliseconds
    private static final int WAKE_LOCK_TIMEOUT = 60000;// milliseconds
    private static final int WAKE_LOCK_ACQUIRE_INTERVAL = 5000;// milliseconds

    private static final Object dynamicLocationListenerLock = new Object();
    private static final Object reacquireWakeLockLock = new Object();

    private IBinder binder = new LocalBinder();
    private TelephonyManager telephonyManager;
    private LocationManager locationManager;

    private NotificationManager notificationManager;
    private CollectorNotificationHelper notificationHelper;

    private Handler gpsStatusHandler;
    private HandlerThread measurementParserThread;
    private Handler measurementParserHandler;

    private HandlerThread externalBroadcasterThread;
    private Handler externalBroadcasterHandler;
    private ExternalIntentBroadcaster externalIntentBroadcaster;

    private MeasurementUpdater measurementUpdater = new MeasurementUpdater();

    private float lastGpsAccuracy;
    private LocationValidator locationValidator;

    // prevent from being garbage collected
    private MeasurementParser measurementParser;
    private PhoneStateListener phoneStateListener;
    private Timer periodicalPhoneStateListener;

    KeepScreenOnMode keepScreenOnMode;
    private Timer periodicalWakeLockAcquirer;

    private int conditionsNotAchievedCounter = CONDITIONS_NOT_ACHIEVED_COUNTER_INIT;
    private AtomicInteger currentIntervalValue = new AtomicInteger();//TODO: get rid when on same thread reconnected after procesisng received event
    private MeansOfTransport transportMode;

    private long startTime;
    private Statistics startStats;

    private Location lastLocation;
    private long lastLocationObtainedTime;
    private GpsStatus gpsStatus = GpsStatus.Unknown;
    private Validity lastIsSystemTimeValid = Validity.Valid;

    private SystemTimeValidator systemTimeValidator = new SystemTimeValidator();

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    // ========== SERVICE ========== //

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate(): Creating service");
        MyApplication.startBackgroundTask(this);
        // get managers
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationHelper = new CollectorNotificationHelper(this);
        // create notification
        startStats = MeasurementsDatabase.getInstance(getApplication()).getMeasurementsStatistics();
        startTime = lastLocationObtainedTime = System.currentTimeMillis();
        EventBus.getDefault().register(this);
        // register receiver
        registerReceiver(stopRequestBroadcastReceiver, new IntentFilter(BROADCAST_INTENT_STOP_SERVICE));
        Notification notification = notificationHelper.createNotification(getGpsStatusNotificationText(getGpsStatus()));
        // start as foreground service to prevent from killing
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand(): Starting service");
        // get locomotion mode
        transportMode = (MeansOfTransport) intent.getSerializableExtra(CollectorService.INTENT_KEY_TRANSPORT_MODE);
        if (transportMode == null)
            transportMode = (MyApplication.getPreferencesProvider().getGpsOptimizationsEnabled() ? MeansOfTransport.Universal : MeansOfTransport.Fixed);
        Log.d(TAG, "onStartCommand(): Selected transport mode: " + transportMode);
        String keepScreenOnModeString = intent.getStringExtra(CollectorService.INTENT_KEY_KEEP_SCREEN_ON_MODE);
        if (keepScreenOnModeString == null)
            keepScreenOnModeString = MyApplication.getPreferencesProvider().getCollectorKeepScreenOnMode();
        if (keepScreenOnModeString.equals(getString(R.string.preferences_keep_screen_on_mode_entries_value_full)))
            keepScreenOnMode = KeepScreenOnMode.FullBrightness;
        else if (keepScreenOnModeString.equals(getString(R.string.preferences_keep_screen_on_mode_entries_value_dim)))
            keepScreenOnMode = KeepScreenOnMode.Dimmed;
        else
            keepScreenOnMode = KeepScreenOnMode.Disabled;
        Log.d(TAG, "onStartCommand(): Keep screen on mode: " + keepScreenOnModeString);
        // save interval (max by default, because it may be reconnected in a moment)
        currentIntervalValue.set(transportMode.getMaxTime());
        measurementUpdater.setMinDistanceAndInterval(transportMode.getDistance(), transportMode.getMaxTime());
        locationValidator = new LocationValidator(transportMode.getAccuracy());
        // listen for RSSI (ASU) and cell change
        periodicalPhoneStateListener = new Timer();
        registerPhoneStateListener();
        // listen for GPS location change
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, STATIC_LISTENER_INTERVAL, STATIC_LISTENER_DISTANCE, staticLocationListener);
        Log.d(TAG, "onStartCommand(): Static location listener started");
        synchronized (dynamicLocationListenerLock) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, currentIntervalValue.get(), 0, dynamicLocationListener);
            Log.d(TAG, "onStartCommand(): Service started with min distance: " + 0 + " and min time: " + currentIntervalValue.get());
        }

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        registerWakeLockAcquirer();

        scheduleNextGpsStatusCheck();

        // send current gps status
        GpsStatus status = getGpsStatus();
        float accuracy = getLastGpsAccuracy();
        EventBus.getDefault().postSticky(new GpsStatusChangedEvent(status, accuracy));

        if (MyApplication.getPreferencesProvider().getNotifyMeasurementsCollected()) {
            externalIntentBroadcaster = new ExternalIntentBroadcaster();
            getExternalBroadcasterHandler().post(externalIntentBroadcaster);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind(): Binding to service");
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy(): Destroying service");
        if (periodicalWakeLockAcquirer != null) {
            periodicalWakeLockAcquirer.cancel();
        }
        synchronized (reacquireWakeLockLock) {
            if (wakeLock != null && wakeLock.isHeld())
                wakeLock.release();
        }
        MyApplication.stopBackgroundTask();
        stopForeground(true);
        cancelNextGpsStatusCheck();
        if (periodicalPhoneStateListener != null) {
            periodicalPhoneStateListener.cancel();
        }
        measurementParser.stop();
        if (externalIntentBroadcaster != null) {
            externalIntentBroadcaster.stop();
        }
        EventBus.getDefault().postSticky(new GpsStatusChangedEvent());
        EventBus.getDefault().unregister(this);
        if (stopRequestBroadcastReceiver != null)
            unregisterReceiver(stopRequestBroadcastReceiver);
        long endTime = System.currentTimeMillis();
        notificationManager.cancel(NOTIFICATION_ID);
        if (locationManager != null) {
            synchronized (dynamicLocationListenerLock) {
                if (dynamicLocationListener != null)
                    locationManager.removeUpdates(dynamicLocationListener);
            }
            if (staticLocationListener != null)
                locationManager.removeUpdates(staticLocationListener);
        }
        if (telephonyManager != null)
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        if (measurementParserThread != null)
            measurementParserThread.quit();
        if (externalBroadcasterThread != null)
            externalBroadcasterThread.quit();
        long duration = (endTime - startTime);
        Statistics endStats = MeasurementsDatabase.getInstance(getApplication()).getMeasurementsStatistics();
        int numberOfCollectedLocations = endStats.getLocationsLocal() - startStats.getLocationsLocal();
        int numberOfCollectedCells = endStats.getCellsLocal() - startStats.getCellsLocal();
        AnalyticsStatistics stats = new AnalyticsStatistics();
        stats.setLocations(numberOfCollectedLocations);
        stats.setCells(numberOfCollectedCells);
        MyApplication.getAnalytics().sendCollectorFinished(duration, transportMode.name(), stats);
        super.onDestroy();
    }

    // ========== NOTIFICATION ========== //

    private synchronized void updateNotification(Statistics statistics) {
        Notification notification = notificationHelper.updateNotification(statistics);
        Log.d(TAG, "updateNotification(): Setting statistics: " + statistics);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private synchronized void updateNotification(String notificationText) {
        Notification notification = notificationHelper.updateNotification(notificationText);
        Log.d(TAG, "updateNotification(): Setting text: " + notificationText);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    // ========== LISTENERS ========== //

    private void registerPhoneStateListener() {
        String collectorApiVersion = MyApplication.getPreferencesProvider().getCollectorApiVersion();
        // double check to avoid fail in production caused by invalid settings
        if (MobileUtils.isApi17VersionCompatible()) {
            if (getString(R.string.preferences_collector_api_version_entries_value_auto).equals(collectorApiVersion)) {
                // auto detection
                if (MobileUtils.isApi17FullyCompatible(getApplication())) {
                    registerApi17PhoneStateListener();
                } else {
                    registerApi1PhoneStateListener();
                }
            } else {
                // API 17 forced
                registerApi17PhoneStateListener();
            }
        } else {
            // API 1 forced
            registerApi1PhoneStateListener();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void registerApi17PhoneStateListener() {
        boolean collectNeighboringCells = MyApplication.getPreferencesProvider().getCollectNeighboringCells();
        measurementParser = new MeasurementParserFactory().CreateApi17Parser(transportMode.getAccuracy(), collectNeighboringCells);
        getMeasurementParserHandler().post(measurementParser);
        phoneStateListener = new PhoneStateListener() {
            private final String INNER_TAG = TAG + ".Api17Plus" + PhoneStateListener.class.getSimpleName();

            @Override
            public void onCellInfoChanged(List<CellInfo> cellInfo) {
                if (cellInfo == null) {
                    Log.d(INNER_TAG, "onCellInfoChanged(): Null reported");
                    return;
                }
                Log.d(INNER_TAG, "onCellInfoChanged(): Number of cells: " + cellInfo.size());
                processCellInfo(cellInfo);
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_INFO);
        // run scheduled cell listener
        periodicalPhoneStateListener.schedule(new TimerTask() {
            private final String INNER_TAG = TAG + ".Periodical" + PhoneStateListener.class.getSimpleName();

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void run() {
                List<CellInfo> cellInfo = telephonyManager.getAllCellInfo();
                if (cellInfo == null) {
                    Log.d(INNER_TAG, "run(): Null reported");
                    return;
                }
                Log.d(INNER_TAG, "run(): Number of cells: " + cellInfo.size());
                processCellInfo(cellInfo);
            }
        }, 0, CELL_UPDATE_INTERVAL);
    }

    private void registerApi1PhoneStateListener() {
        boolean collectNeighboringCells = MyApplication.getPreferencesProvider().getCollectNeighboringCells();
        measurementParser = new MeasurementParserFactory().CreateApi1Parser(transportMode.getAccuracy(), collectNeighboringCells);
        getMeasurementParserHandler().post(measurementParser);
        phoneStateListener = new PhoneStateListener() {
            private final String INNER_TAG = TAG + ".Legacy" + PhoneStateListener.class.getSimpleName();

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                // in GSM networks, ASU is equal to the RSSI (received signal strength indicator, see TS 27.007)
                measurementUpdater.setLastSignalStrength(signalStrength);
                Log.d(INNER_TAG, "onSignalStrengthsChanged(): Signal strength = " + signalStrength);
            }

            @Override
            public void onCellLocationChanged(CellLocation cellLocation) {
                Log.d(INNER_TAG, "onCellLocationChanged(): " + cellLocation);
                processCellLocation(cellLocation);
            }
        };
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION);
        // run scheduled cell listener
        periodicalPhoneStateListener.schedule(new TimerTask() {
            private final String INNER_TAG = TAG + ".Periodical" + PhoneStateListener.class.getSimpleName();

            @Override
            public void run() {
                CellLocation cellLocation = telephonyManager.getCellLocation();
                Log.d(INNER_TAG, "run(): " + cellLocation);
                processCellLocation(cellLocation);
            }
        }, 0, CELL_UPDATE_INTERVAL);
    }

    private void processCellInfo(List<CellInfo> cellInfo) {
        measurementUpdater.setLastCellInfo(cellInfo);
    }

    private void processCellLocation(CellLocation cellLocation) {
        // get network type
        int networkTypeInt = telephonyManager.getNetworkType();
        NetworkGroup networkType = NetworkTypeUtils.getNetworkGroup(networkTypeInt);
        // get network operator (may be unreliable for CDMA)
        String networkOperatorCode = telephonyManager.getNetworkOperator();
        String networkOperatorName = telephonyManager.getNetworkOperatorName();
        Log.d(TAG, "processCellLocation(): Operator code = '" + networkOperatorCode + "', name = '" + networkOperatorName + "'");
        List<NeighboringCellInfo> neighboringCells = telephonyManager.getNeighboringCellInfo();
        Log.d(TAG, "processCellLocation(): Reported " + neighboringCells.size() + " neighboring cells");
        measurementUpdater.setLastCellLocation(cellLocation, networkType, networkOperatorCode, networkOperatorName, neighboringCells);
    }

    private LocationListener staticLocationListener = new LocationListener() {
        private final String INNER_TAG = TAG + ".Static" + LocationListener.class.getSimpleName();

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            String statusString;
            switch (status) {
                case LocationProvider.AVAILABLE:
                    statusString = "AVAILABLE";
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    statusString = "OUT_OF_SERVICE";
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    statusString = "TEMPORARILY_UNAVAILABLE";
                    break;
                default:
                    statusString = "UNKNOWN";
                    break;
            }
            Log.d(INNER_TAG, "onStatusChanged(): " + statusString);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(INNER_TAG, "onProviderEnabled(): " + provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(INNER_TAG, "onProviderDisabled(): " + provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(INNER_TAG, "onLocationChanged(): " + location);
            lastLocation = location;
            long locationObtainedTime = System.currentTimeMillis();
            lastLocationObtainedTime = locationObtainedTime;
            setLastGpsAccuracy(lastLocation);
            updateGpsStatus(lastLocation, lastLocationObtainedTime, System.currentTimeMillis());
            updateSystemTimeChange(lastLocation);
            //TODO: check if does not interfere with reconnection getParseHandler().post(new MeasurementUpdater(location));
        }
    };

    private LocationListener dynamicLocationListener = new LocationListener() {
        private final String INNER_TAG = TAG + ".Dynamic" + LocationListener.class.getSimpleName();

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // don't duplicate logic from static listener
        }

        @Override
        public void onProviderEnabled(String provider) {
            // don't duplicate logic from static listener
        }

        @Override
        public void onProviderDisabled(String provider) {
            // don't duplicate logic from static listener
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(INNER_TAG, "onLocationChanged(): " + location);
            lastLocation = location;
            long locationObtainedTime = System.currentTimeMillis();
            lastLocationObtainedTime = locationObtainedTime;
            setLastGpsAccuracy(location);
            measurementUpdater.setLastLocation(location, locationObtainedTime);
        }
    };

    // ========== EVENTS ========== //

    public void onEventMainThread(MeasurementProcessedEvent event) {
        float speed = 0;
        if (event instanceof MeasurementSavedEvent) {
            MeasurementSavedEvent savedEvent = (MeasurementSavedEvent) event;
            speed = savedEvent.getMeasurement().getGpsSpeed();
            updateNotification(savedEvent.getStatistics());
        }
        if (transportMode != MeansOfTransport.Fixed)
            updateDynamicInterval(event.getResult(), speed);
    }

    public void updateDynamicInterval(ParseResult result, float speed) {
        if (result == ParseResult.Saved) {
            // get interval
            int interval = recalculateInterval(speed);
            // change only if it makes difference (probably utilizes less CPU time)
            int intervalDiff = Math.abs(currentIntervalValue.get() - interval);
            if (intervalDiff < 300) {
                Log.d(TAG, "updateDynamicInterval(): Skipping GPS reconnection because of too small interval difference: " + intervalDiff);
                return;
            }
            Log.d(TAG, "updateDynamicInterval(): New interval calculated: " + interval + " difference to previous " + intervalDiff);
            // save calculated interval
            currentIntervalValue.set(interval);
            measurementUpdater.setMinDistanceAndInterval(transportMode.getDistance(), interval);
            // reconnect gps
            synchronized (dynamicLocationListenerLock) {
                locationManager.removeUpdates(dynamicLocationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, currentIntervalValue.get(), 0, dynamicLocationListener);
                Log.d(TAG, "updateDynamicInterval(): GPS reconnected with min distance: " + transportMode.getDistance() + " and min time: " + currentIntervalValue);
            }
            conditionsNotAchievedCounter = CONDITIONS_NOT_ACHIEVED_COUNTER_INIT;
            return;
        } else if (result == ParseResult.AccuracyNotAchieved || result == ParseResult.NoNetworkSignal
                || result == ParseResult.DistanceNotAchieved) {
            if (currentIntervalValue.get() != transportMode.getMaxTime()) {
                if (conditionsNotAchievedCounter <= 0) {
                    Log.d(TAG, "updateDynamicInterval(): GPS reconnected with max interval: " + transportMode.getMaxTime() + " because of fail: " + result);
                    // restore and save interval (increment to max because we don't get appropriate result at all)
                    int newInterval = Math.min(currentIntervalValue.get() + 500, transportMode.getMaxTime());
                    currentIntervalValue.set(newInterval);
                    measurementUpdater.setMinDistanceAndInterval(transportMode.getDistance(), newInterval);
                    synchronized (dynamicLocationListenerLock) {
                        locationManager.removeUpdates(dynamicLocationListener);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, transportMode.getMaxTime(), 0, dynamicLocationListener);
                    }
                    conditionsNotAchievedCounter = CONDITIONS_NOT_ACHIEVED_COUNTER_INIT;
                    return;
                } else {
                    Log.d(TAG, "updateDynamicInterval(): Skipping GPS reconnection because of fail: " + result);
                    conditionsNotAchievedCounter--;
                    return;
                }
            } else {
                Log.d(TAG, "updateDynamicInterval(): GPS failed because of: " + result + " but not reconnected with same parameters");
                return;
            }
        } else if (result == ParseResult.LocationTooOld) {
            Log.d(TAG, "updateDynamicInterval(): Skipping GPS reconnection because location is too old");
            // TODO: consider reconnection
            setGpsStatus(GpsStatus.NoLocation);
            return;
        } else {// only when SaveFailed
            Log.d(TAG, "updateDynamicInterval(): Skipping GPS reconnection");
            return;
        }
    }

    // ========== BROADCAST RECEIVERS ========== //

    private BroadcastReceiver stopRequestBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "stopRequestBroadcastReceiver.onReceive(): Received broadcast intent: " + intent);
            if (intent.getAction().equals(BROADCAST_INTENT_STOP_SERVICE)) {
                stopSelf();
            }
        }
    };

    // ========== MISCELLANEOUS ========== //

    private Handler getGpsStatusHandler() {
        if (gpsStatusHandler == null) {
            gpsStatusHandler = new Handler();
        }
        return gpsStatusHandler;
    }

    private Handler getMeasurementParserHandler() {
        if (measurementParserHandler == null) {
            measurementParserThread = new HandlerThread("MeasurementParserHandler");
            measurementParserThread.start();
            measurementParserHandler = new Handler(measurementParserThread.getLooper());
        }
        return measurementParserHandler;
    }

    private Handler getExternalBroadcasterHandler() {
        if (externalBroadcasterHandler == null) {
            externalBroadcasterThread = new HandlerThread("ExternalBroadcasterHandler");
            externalBroadcasterThread.start();
            externalBroadcasterHandler = new Handler(externalBroadcasterThread.getLooper());
        }
        return externalBroadcasterHandler;
    }

    private class LocalBinder extends Binder implements ICollectorService {
        @Override
        public GpsStatus getGpsStatus() {
            return CollectorService.this.getGpsStatus();
        }

        @Override
        public float getGpsLastAccuracy() {
            return CollectorService.this.getLastGpsAccuracy();
        }

        @Override
        public Validity isSystemTimeValid() {
            return CollectorService.this.getLastIsSystemTimeValid();
        }
    }

    private void updateGpsStatus(Location location, long gpsTimestamp, long systemTimestamp) {
        GpsStatus status = getGpsStatus();
        if (location == null) {
            status = GpsStatus.Unknown;
        } else if (!locationValidator.isUpToDate(gpsTimestamp, systemTimestamp)) {
            status = GpsStatus.NoLocation;
        } else if (!locationValidator.hasRequiredAccuracy(location)) {
            status = GpsStatus.LowAccuracy;
        } else {
            status = GpsStatus.Ok;
        }
        Log.d(TAG, "updateGpsStatus(): Updating gps status = " + status);
        setGpsStatus(status);
    }

    private void updateSystemTimeChange(Location location) {
        boolean valid = systemTimeValidator.isValid(System.currentTimeMillis(), location.getTime());
        if (valid) {
            if (lastIsSystemTimeValid != Validity.Valid) {
                Log.d(TAG, "updateSystemTimeChange(): Setting system time to valid");
                EventBus.getDefault().postSticky(new SystemTimeChangedEvent(Validity.Valid));
                lastIsSystemTimeValid = Validity.Valid;
            }
        } else {
            if (lastIsSystemTimeValid != Validity.Invalid) {
                Log.d(TAG, "updateSystemTimeChange(): Setting system time to invalid");
                EventBus.getDefault().postSticky(new SystemTimeChangedEvent(Validity.Invalid));
                lastIsSystemTimeValid = Validity.Invalid;
            }
        }
    }

    private Runnable gpsStatusCheck = new Runnable() {
        private final String INNER_TAG = TAG + ".GpsStatusCheckRunnable";

        @Override
        public void run() {
            Log.d(INNER_TAG, "run(): GPS status check performed");
            updateGpsStatus(lastLocation, lastLocationObtainedTime, System.currentTimeMillis());
            scheduleNextGpsStatusCheck();
        }
    };

    private void scheduleNextGpsStatusCheck() {
        Log.d(TAG, "scheduleNextGpsStatusCheck(): Setting next GPS status check");
        getGpsStatusHandler().postDelayed(gpsStatusCheck, GPS_STATUS_CHECK_INTERVAL);
    }

    private void cancelNextGpsStatusCheck() {
        Log.d(TAG, "cancelNextGpsStatusCheck(): Cancelling GPS status check");
        getGpsStatusHandler().removeCallbacks(gpsStatusCheck);
    }

    private void registerWakeLockAcquirer() {
        if (keepScreenOnMode == KeepScreenOnMode.Disabled) {
            Log.d(TAG, "registerWakeLockAcquirer(): WakeLock not configured");
            return;
        }
        periodicalWakeLockAcquirer = new Timer();
        // run scheduled cell listener
        periodicalWakeLockAcquirer.schedule(new TimerTask() {
            private final String INNER_TAG = TAG + ".Periodical" + WakeLock.class.getSimpleName() + "Acquirer";

            @Override
            public void run() {
                synchronized (reacquireWakeLockLock) {
                    WakeLock oldWakeLock = wakeLock;
                    Log.d(INNER_TAG, "run(): New WakeLock acquire");
                    wakeLock = createWakeLock(keepScreenOnMode);
                    wakeLock.acquire(WAKE_LOCK_TIMEOUT);
                    Log.d(INNER_TAG, "run(): Old WakeLock release");
                    if (oldWakeLock != null && oldWakeLock.isHeld())
                        oldWakeLock.release();
                }
            }
        }, 0, WAKE_LOCK_ACQUIRE_INTERVAL);
    }

    @SuppressWarnings("deprecation")
    private WakeLock createWakeLock(KeepScreenOnMode keepScreenOnMode) {
        WakeLock newWakeLock = null;
        if (keepScreenOnMode == KeepScreenOnMode.FullBrightness) {
            newWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, SERVICE_FULL_NAME + ".BrightWakeLock_" + System.currentTimeMillis());
            newWakeLock.setReferenceCounted(false);
        } else if (keepScreenOnMode == KeepScreenOnMode.Dimmed) {
            newWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, SERVICE_FULL_NAME + ".DimWakeLock_" + System.currentTimeMillis());
            newWakeLock.setReferenceCounted(false);
        }
        return newWakeLock;
    }

    private int recalculateInterval(float speed) {
        // Random r = new Random();
        // speed = r.nextFloat() * 60 + 1.5f;// 5.4 - 221.4 km/h
        int result = transportMode.getMaxTime();
        // calculate by speed
        if (speed != 0.0f) {
            int interval = (int) ((transportMode.getDistance() / speed) * 1000);
            if (interval < transportMode.getMinTime())
                interval = transportMode.getMinTime();
            else if (interval > transportMode.getMaxTime())
                interval = transportMode.getMaxTime();
            result = interval;
        }
        return result;
    }

    private GpsStatus getGpsStatus() {
        return this.gpsStatus;
    }

    private void setGpsStatus(GpsStatus status) {
        Log.d(TAG, "setGpsStatus(): Setting gps status = " + status);
        boolean statusChanged = (this.gpsStatus != status);
        this.gpsStatus = status;
        float accuracy = getLastGpsAccuracy();
        // Update always because accuracy might change
        EventBus.getDefault().postSticky(new GpsStatusChangedEvent(status, accuracy));
        if (gpsStatus == GpsStatus.LowAccuracy || gpsStatus == GpsStatus.NoLocation) {
            String notificationText = getGpsStatusNotificationText(gpsStatus);
            updateNotification(notificationText);
        }
        // Optimization: it doesn't make sense to refresh if nothing changes (after save updated in a different way)
        else if (statusChanged) {
            updateNotification(MeasurementsDatabase.getInstance(getApplication()).getMeasurementsStatistics());
        }
    }

    private String getGpsStatusNotificationText(GpsStatus status) {
        String statusString;
        if (status == GpsStatus.LowAccuracy) {
            // length unit
            boolean useImperialUnits = MyApplication.getPreferencesProvider().getUseImperialUnits();
            String lengthUnit;
            if (useImperialUnits) {
                lengthUnit = getString(R.string.unit_length_imperial);
            } else {
                lengthUnit = getString(R.string.unit_length_metric);
            }
            // format string
            float accuracy = getLastGpsAccuracy();
            statusString = getString(R.string.status_low_gps_accuracy, accuracy, lengthUnit);
        } else if (status == GpsStatus.NoLocation)
            statusString = getString(R.string.status_no_gps_location);
        else
            statusString = getString(R.string.status_unknown);// this should never happen
        return getString(R.string.collector_notification_status, statusString);
    }

    public float getLastGpsAccuracy() {
        return this.lastGpsAccuracy;
    }

    public void setLastGpsAccuracy(Location location) {
        Log.d(TAG, "setLastGpsAccuracy(): Setting last gps accuracy = " + location.getAccuracy());
        this.lastGpsAccuracy = (location.hasAccuracy() ? location.getAccuracy() : 1000);
    }

    public Validity getLastIsSystemTimeValid() {
        return this.lastIsSystemTimeValid;
    }
}
