/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector;

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
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import info.zamojski.soft.towercollector.analytics.IntentSource;
import info.zamojski.soft.towercollector.broadcast.BatteryStatusBroadcastReceiver;
import info.zamojski.soft.towercollector.broadcast.ExternalBroadcastSender;
import info.zamojski.soft.towercollector.broadcast.LocationModeOrProvidersChangedReceiver;
import info.zamojski.soft.towercollector.collector.CollectorNotificationHelper;
import info.zamojski.soft.towercollector.collector.MeasurementUpdater;
import info.zamojski.soft.towercollector.collector.ParseResult;
import info.zamojski.soft.towercollector.collector.TelephonyTriple;
import info.zamojski.soft.towercollector.collector.parsers.MeasurementParser;
import info.zamojski.soft.towercollector.collector.parsers.MeasurementParserFactory;
import info.zamojski.soft.towercollector.collector.validators.LocationValidator;
import info.zamojski.soft.towercollector.collector.validators.SystemTimeValidator;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.enums.GpsStatus;
import info.zamojski.soft.towercollector.enums.KeepScreenOnMode;
import info.zamojski.soft.towercollector.enums.MeansOfTransport;
import info.zamojski.soft.towercollector.enums.NetworkGroup;
import info.zamojski.soft.towercollector.enums.Validity;
import info.zamojski.soft.towercollector.events.GpsStatusChangedEvent;
import info.zamojski.soft.towercollector.events.MeasurementProcessedEvent;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.SystemTimeChangedEvent;
import info.zamojski.soft.towercollector.model.AnalyticsStatistics;
import info.zamojski.soft.towercollector.model.Statistics;
import info.zamojski.soft.towercollector.model.Tuple;
import info.zamojski.soft.towercollector.utils.GpsUtils;
import info.zamojski.soft.towercollector.utils.MobileUtils;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;
import timber.log.Timber;

public class CollectorService extends Service {

    public static final String SERVICE_FULL_NAME = CollectorService.class.getCanonicalName();
    public static final String INTENT_KEY_START_INTENT_SOURCE = "start_intent_source";
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
    private List<TelephonyTriple> telephonyTriples = new ArrayList<>(2);
    private Tuple<Method, Boolean> getNeighboringCellInfoMethod;
    private LocationManager locationManager;

    private NotificationManager notificationManager;
    private CollectorNotificationHelper notificationHelper;

    private Handler gpsStatusHandler;
    private HandlerThread measurementParserThread;
    private Handler measurementParserHandler;

    private HandlerThread externalBroadcastSenderThread;
    private Handler externalBroadcastSenderHandler;
    private ExternalBroadcastSender externalBroadcastSender;

    private MeasurementUpdater measurementUpdater = new MeasurementUpdater();

    private float lastGpsAccuracy;
    private LocationValidator locationValidator;

    // prevent from being garbage collected
    private MeasurementParser measurementParser;
    private Timer periodicalPhoneStateListener;

    KeepScreenOnMode keepScreenOnMode;
    private Timer periodicalWakeLockAcquirer;

    private int conditionsNotAchievedCounter = CONDITIONS_NOT_ACHIEVED_COUNTER_INIT;
    private AtomicInteger currentIntervalValue = new AtomicInteger();//TODO: get rid when on same thread reconnected after processing received event
    private MeansOfTransport transportMode = MeansOfTransport.Fixed;

    private long startTime;
    private Statistics startStats;

    private Location lastLocation;
    private long lastLocationObtainedTime;
    private GpsStatus gpsStatus = GpsStatus.Initializing;
    private Validity lastIsSystemTimeValid = Validity.Valid;

    private SystemTimeValidator systemTimeValidator = new SystemTimeValidator();

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    private IntentSource startIntentSource;
    private int apiVersionUsed;

    // ========== SERVICE ========== //

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.d("onCreate(): Creating service");
        MyApplication.startBackgroundTask(this);
        // get managers
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        TelephonyManager defaultTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (MobileUtils.isApi24MultiSimCompatible()) { // multi-sim, multiple listeners
            try {
                SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                List<SubscriptionInfo> activeSubscriptions = subscriptionManager.getActiveSubscriptionInfoList();
                if (activeSubscriptions == null || activeSubscriptions.isEmpty()) {
                    // fallback in case subscriptions state is unknown or inactive
                    telephonyTriples.add(new TelephonyTriple(defaultTelephonyManager));
                } else {
                    for (SubscriptionInfo subscription : activeSubscriptions) {
                        TelephonyManager telephonyManager = defaultTelephonyManager.createForSubscriptionId(subscription.getSubscriptionId());
                        telephonyTriples.add(new TelephonyTriple(telephonyManager));
                    }
                }
            } catch (SecurityException ex) {
                Timber.e(ex, "onCreate(): phone permission is denied");
                stopSelf();
                return;
            }
        } else { // single-sim, single listener
            telephonyTriples.add(new TelephonyTriple(defaultTelephonyManager));
        }
        boolean hideNotification = MyApplication.getPreferencesProvider().getHideCollectorNotification();
        notificationHelper = new CollectorNotificationHelper(this, hideNotification);
        // create notification
        startStats = MeasurementsDatabase.getInstance(getApplication()).getMeasurementsStatistics();
        startTime = lastLocationObtainedTime = System.currentTimeMillis();
        EventBus.getDefault().register(this);
        // register receiver
        registerReceiver(stopRequestBroadcastReceiver, new IntentFilter(BROADCAST_INTENT_STOP_SERVICE));
        registerReceiver(batteryStatusBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        registerReceiver(locationModeOrProvidersChanged, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
        registerReceiver(locationModeOrProvidersChanged, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        Notification notification = notificationHelper.createNotification(notificationManager, getGpsStatusNotificationText(getGpsStatus()));
        // start as foreground service to prevent from killing
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Timber.d("onStartCommand(): Starting service");
        // get locomotion mode
        transportMode = (MeansOfTransport) intent.getSerializableExtra(INTENT_KEY_TRANSPORT_MODE);
        if (transportMode == null)
            transportMode = (MyApplication.getPreferencesProvider().getGpsOptimizationsEnabled() ? MeansOfTransport.Universal : MeansOfTransport.Fixed);
        Timber.d("onStartCommand(): Selected transport mode: %s", transportMode);
        String keepScreenOnModeString = intent.getStringExtra(INTENT_KEY_KEEP_SCREEN_ON_MODE);
        if (keepScreenOnModeString == null)
            keepScreenOnModeString = MyApplication.getPreferencesProvider().getCollectorKeepScreenOnMode();
        if (keepScreenOnModeString.equals(getString(R.string.preferences_keep_screen_on_mode_entries_value_full)))
            keepScreenOnMode = KeepScreenOnMode.FullBrightness;
        else if (keepScreenOnModeString.equals(getString(R.string.preferences_keep_screen_on_mode_entries_value_dim)))
            keepScreenOnMode = KeepScreenOnMode.Dimmed;
        else
            keepScreenOnMode = KeepScreenOnMode.Disabled;
        Timber.d("onStartCommand(): Keep screen on mode: %s", keepScreenOnModeString);
        startIntentSource = (IntentSource) intent.getSerializableExtra(INTENT_KEY_START_INTENT_SOURCE);
        if (startIntentSource == null)
            startIntentSource = IntentSource.System;
        // save interval (max by default, because it may be reconnected in a moment)
        currentIntervalValue.set(transportMode.getMaxTime());
        measurementUpdater.setMinDistanceAndInterval(transportMode.getDistance(), transportMode.getMaxTime());
        locationValidator = new LocationValidator(transportMode.getAccuracy());
        // listen for RSSI (ASU) and cell change
        periodicalPhoneStateListener = new Timer();
        try {
            registerPhoneStateListener();
        } catch (SecurityException ex) {
            Timber.e(ex, "onStartCommand(): coarse location permission is denied");
            stopSelf();
            return START_NOT_STICKY;
        }
        // make sure GPS is available on the device otherwise the following lines will throw an exception
        if (!GpsUtils.isGpsEnabled(this)) {
            Timber.w("onStartCommand(): GPS is unavailable, stopping");
            Toast.makeText(this, R.string.collector_gps_unavailable, Toast.LENGTH_LONG).show();
            stopSelf();
            return START_NOT_STICKY;
        }
        try {
            // listen for GPS location change
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, STATIC_LISTENER_INTERVAL, STATIC_LISTENER_DISTANCE, staticLocationListener);
            Timber.d("onStartCommand(): Static location listener started");
            synchronized (dynamicLocationListenerLock) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, currentIntervalValue.get(), 0, dynamicLocationListener);
                Timber.d("onStartCommand(): Service started with min distance: 0 and min time: %s", currentIntervalValue.get());
            }
        } catch (SecurityException ex) {
            Timber.e(ex, "onStartCommand(): fine location permission is denied");
            stopSelf();
            return START_NOT_STICKY;
        }
        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        registerWakeLockAcquirer();

        scheduleNextGpsStatusCheck();

        // send current gps status
        GpsStatus status = getGpsStatus();
        float accuracy = getLastGpsAccuracy();
        EventBus.getDefault().postSticky(new GpsStatusChangedEvent(status, accuracy));

        boolean notifyCollected = MyApplication.getPreferencesProvider().getNotifyMeasurementsCollected();
        if (notifyCollected) {
            externalBroadcastSender = new ExternalBroadcastSender();
            getExternalBroadcastSenderHandler().post(externalBroadcastSender);
        }
        MyApplication.getAnalytics().sendPrefsNotifyMeasurementsCollected(notifyCollected);

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("onBind(): Binding to service");
        return binder;
    }

    @Override
    public void onDestroy() {
        Timber.d("onDestroy(): Destroying service");
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
        if (measurementParser != null) {
            measurementParser.stop();
        }
        if (externalBroadcastSender != null) {
            externalBroadcastSender.stop();
        }
        EventBus.getDefault().postSticky(new GpsStatusChangedEvent());
        EventBus.getDefault().unregister(this);
        if (stopRequestBroadcastReceiver != null)
            unregisterReceiver(stopRequestBroadcastReceiver);
        if (batteryStatusBroadcastReceiver != null)
            unregisterReceiver(batteryStatusBroadcastReceiver);
        if (locationModeOrProvidersChanged != null)
            unregisterReceiver(locationModeOrProvidersChanged);
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
        for (TelephonyTriple telephonyTriple : telephonyTriples) {
            TelephonyManager telephonyManager = telephonyTriple.getTelephonyManager();
            PhoneStateListener phoneStateListener = telephonyTriple.getPhoneStateListener();
            if (telephonyManager != null && phoneStateListener != null)
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        if (measurementParserThread != null)
            measurementParserThread.quit();
        if (externalBroadcastSenderThread != null)
            externalBroadcastSenderThread.quit();
        long duration = (endTime - startTime);
        Statistics endStats = MeasurementsDatabase.getInstance(getApplication()).getMeasurementsStatistics();
        int numberOfCollectedLocations = endStats.getLocationsLocal() - startStats.getLocationsLocal();
        int numberOfCollectedCells = endStats.getCellsLocal() - startStats.getCellsLocal();
        AnalyticsStatistics stats = new AnalyticsStatistics();
        stats.setLocations(numberOfCollectedLocations);
        stats.setCells(numberOfCollectedCells);
        MyApplication.getAnalytics().sendCollectorFinished(startIntentSource, transportMode.name(), apiVersionUsed, duration, stats);
        super.onDestroy();
    }

    // ========== NOTIFICATION ========== //

    private synchronized void updateNotification(Statistics statistics) {
        Notification notification = notificationHelper.updateNotification(statistics);
        Timber.d("updateNotification(): Setting statistics: %s", statistics);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private synchronized void updateNotification(String notificationText) {
        Notification notification = notificationHelper.updateNotification(notificationText);
        Timber.d("updateNotification(): Setting text: %s", notificationText);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    // ========== LISTENERS ========== //

    private void registerPhoneStateListener() {
        String collectorApiVersion = MyApplication.getPreferencesProvider().getCollectorApiVersion();
        if (getString(R.string.preferences_collector_api_version_entries_value_auto).equals(collectorApiVersion)) {
            // auto detection
            MyApplication.getAnalytics().sendPrefsCollectorApiVersion(0);
            if (MobileUtils.isApi17FullyCompatible(getApplication())) {
                registerApi17PhoneStateListener();
            } else {
                registerApi1PhoneStateListener();
            }
        } else if (getString(R.string.preferences_collector_api_version_entries_value_api_17).equals(collectorApiVersion)) {
            // API 17 forced
            MyApplication.getAnalytics().sendPrefsCollectorApiVersion(17);
            registerApi17PhoneStateListener();
        } else {
            // API 1 forced
            MyApplication.getAnalytics().sendPrefsCollectorApiVersion(1);
            registerApi1PhoneStateListener();
        }
    }

    private void registerApi17PhoneStateListener() {
        Timber.d("Registering API 17 phone state listener");
        boolean collectNeighboringCells = MyApplication.getPreferencesProvider().getCollectNeighboringCells();
        measurementParser = new MeasurementParserFactory().CreateApi17Parser(transportMode.getAccuracy(), collectNeighboringCells);
        getMeasurementParserHandler().post(measurementParser);
        int telephonyManagerIndex = 0;
        for (TelephonyTriple telephonyTriple : telephonyTriples) {
            TelephonyManager telephonyManager = telephonyTriple.getTelephonyManager();
            // callback is registered for all sim cards but it's hard to union 2 data sets reported at different time to process them as one, so calling getAllCellInfo to get at least partially refreshed data in a single call
            final int finalTelephonyManagerIndex = telephonyManagerIndex;
            PhoneStateListener phoneStateListener = new PhoneStateListener() {
                private final String INNER_TAG = CollectorService.class.getSimpleName() + ".Api17Plus" + PhoneStateListener.class.getSimpleName() + "ForIndex" + finalTelephonyManagerIndex;

                @Override
                public void onCellInfoChanged(List<CellInfo> cellInfo) {
                    if (cellInfo == null) {
                        Timber.tag(INNER_TAG).d("onCellInfoChanged(): Null reported");
                        return;
                    }
                    try {
                        List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
                        Timber.tag(INNER_TAG).d("onCellInfoChanged(): Number of cells changed: %s, ignoring result from this subscription only, passing %s cells instead", cellInfo.size(), allCellInfo == null ? "null" : allCellInfo.size());
                        processCellInfo(allCellInfo);
                    } catch (SecurityException ex) {
                        Timber.tag(INNER_TAG).e(ex, "onCellInfoChanged(): coarse location or phone permission is denied");
                        stopSelf();
                    }
                }
            };
            try {
                telephonyTriple.setPhoneStateListener(phoneStateListener);
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_INFO);
            } catch (SecurityException ex) {
                Timber.e(ex, "registerApi17PhoneStateListener(): coarse location permission is denied");
                stopSelf();
            }

            if (MobileUtils.isApi29Limited()) {
                // callback is registered for all sim cards but it's hard to union 2 data sets reported at different time to process them as one, so calling getAllCellInfo to get at least partially refreshed data in a single call
                TelephonyManager.CellInfoCallback cellInfoUpdateRequestCallback = new TelephonyManager.CellInfoCallback() {
                    private final String INNER_TAG = CollectorService.class.getSimpleName() + "." + TelephonyManager.CellInfoCallback.class.getSimpleName();

                    @Override
                    public void onCellInfo(@NonNull List<CellInfo> cellInfo) {
                        try {
                            List<CellInfo> allCellInfo = telephonyManager.getAllCellInfo();
                            Timber.tag(INNER_TAG).d("onCellInfo(): Number of cells updated: %s, ignoring result from this subscription only, passing %s cells instead", cellInfo.size(), allCellInfo == null ? "null" : allCellInfo.size());
                            processCellInfo(allCellInfo);
                        } catch (SecurityException ex) {
                            Timber.tag(INNER_TAG).e(ex, "onCellInfo(): coarse location or phone permission is denied");
                            stopSelf();
                        }
                    }

                    @Override
                    public void onError(int errorCode, Throwable detail) {
                        Timber.tag(INNER_TAG).e(detail, "onError(): Error %s occurred when requesting cell info update", errorCode);
                    }
                };
                telephonyTriple.setCellInfoUpdateRequestCallback(cellInfoUpdateRequestCallback);
            }
            telephonyManagerIndex++;
        }

        // run scheduled cell listener
        periodicalPhoneStateListener.schedule(new TimerTask() {
            private final String INNER_TAG = CollectorService.class.getSimpleName() + ".Periodical" + PhoneStateListener.class.getSimpleName();

            @Override
            public void run() {
                try {
                    List<CellInfo> cellInfo;
                    if (MobileUtils.isApi29Limited()) {
                        for (TelephonyTriple telephonyTriple : telephonyTriples) {
                            TelephonyManager telephonyManager = telephonyTriple.getTelephonyManager();
                            TelephonyManager.CellInfoCallback cellInfoUpdateRequestCallback = (TelephonyManager.CellInfoCallback) telephonyTriple.getCellInfoUpdateRequestCallback();
                            // value is cached and update needs to be requested
                            telephonyManager.requestCellInfoUpdate(getMainExecutor(), cellInfoUpdateRequestCallback);
                        }
                    } else {
                        // value should be refreshed on the call
                        cellInfo = telephonyTriples.get(0).getTelephonyManager().getAllCellInfo();
                        if (cellInfo == null) {
                            Timber.tag(INNER_TAG).d("run(): Null reported");
                            return;
                        }
                        Timber.tag(INNER_TAG).d("run(): Number of cells: %s", cellInfo.size());
                        processCellInfo(cellInfo);
                    }
                } catch (SecurityException ex) {
                    Timber.tag(INNER_TAG).e(ex, "run(): coarse location or phone permission is denied");
                    stopSelf();
                }
            }
        }, 0, CELL_UPDATE_INTERVAL);
        apiVersionUsed = 17;
    }

    private void registerApi1PhoneStateListener() {
        Timber.d("Registering API 1 phone state listener");
        boolean collectNeighboringCells = MyApplication.getPreferencesProvider().getCollectNeighboringCells();
        measurementParser = new MeasurementParserFactory().CreateApi1Parser(transportMode.getAccuracy(), collectNeighboringCells);
        getMeasurementParserHandler().post(measurementParser);
        TelephonyTriple telephonyTriple = telephonyTriples.get(0);
        TelephonyManager telephonyManager = telephonyTriple.getTelephonyManager();
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            private final String INNER_TAG = CollectorService.class.getSimpleName() + ".Legacy" + PhoneStateListener.class.getSimpleName();

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                // in GSM networks, ASU is equal to the RSSI (received signal strength indicator, see TS 27.007)
                measurementUpdater.setLastSignalStrength(signalStrength);
                Timber.tag(INNER_TAG).d("onSignalStrengthsChanged(): Signal strength = %s", signalStrength);
            }

            @Override
            public void onCellLocationChanged(CellLocation cellLocation) {
                try {
                    Timber.tag(INNER_TAG).d("onCellLocationChanged(): %s", cellLocation);
                    List<NeighboringCellInfo> neighboringCells = null;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        neighboringCells = getNeighboringCellInfo(telephonyManager);
                    }
                    processCellLocation(telephonyManager, cellLocation, neighboringCells);
                } catch (SecurityException ex) {
                    Timber.tag(INNER_TAG).e(ex, "onCellLocationChanged(): coarse location or phone permission is denied");
                    stopSelf();
                }
            }
        };
        try {
            telephonyTriple.setPhoneStateListener(phoneStateListener);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION);
        } catch (SecurityException ex) {
            Timber.e(ex, "registerApi1PhoneStateListener(): coarse location permission is denied");
            stopSelf();
        }
        // run scheduled cell listener
        periodicalPhoneStateListener.schedule(new TimerTask() {
            private final String INNER_TAG = CollectorService.class.getSimpleName() + ".Periodical" + PhoneStateListener.class.getSimpleName();

            @Override
            public void run() {
                try {
                    CellLocation cellLocation = telephonyManager.getCellLocation();
                    Timber.tag(INNER_TAG).d("run(): %s", cellLocation);
                    List<NeighboringCellInfo> neighboringCells = null;
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        neighboringCells = getNeighboringCellInfo(telephonyManager);
                    }
                    processCellLocation(telephonyManager, cellLocation, neighboringCells);
                } catch (SecurityException ex) {
                    Timber.tag(INNER_TAG).e(ex, "run(): coarse location or phone permission is denied");
                    stopSelf();
                }
            }
        }, 0, CELL_UPDATE_INTERVAL);
        apiVersionUsed = 1;
    }

    private void processCellInfo(List<CellInfo> cellInfo) {
        measurementUpdater.setLastCellInfo(cellInfo);
    }

    private void processCellLocation(TelephonyManager telephonyManager, CellLocation cellLocation, List<NeighboringCellInfo> neighboringCells) {
        // get network type
        int networkTypeInt = telephonyManager.getNetworkType();
        NetworkGroup networkType = NetworkTypeUtils.getNetworkGroup(networkTypeInt);
        // get network operator (may be unreliable for CDMA)
        String networkOperatorCode = telephonyManager.getNetworkOperator();
        String networkOperatorName = telephonyManager.getNetworkOperatorName();
        Timber.d("processCellLocation(): Operator code = '%s', name = '%s'", networkOperatorCode, networkOperatorName);
        Timber.d("processCellLocation(): Reported %s neighboring cells", (neighboringCells != null ? neighboringCells.size() : null));
        measurementUpdater.setLastCellLocation(cellLocation, networkType, networkOperatorCode, networkOperatorName, neighboringCells);
    }

    private LocationListener staticLocationListener = new LocationListener() {
        private final String INNER_TAG = CollectorService.class.getSimpleName() + ".Static" + LocationListener.class.getSimpleName();

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // on Android 10 this callback will never be invoked
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
            Timber.tag(INNER_TAG).d("onStatusChanged(): %s", statusString);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Timber.tag(INNER_TAG).d("onProviderEnabled(): %s", provider);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Timber.tag(INNER_TAG).d("onProviderDisabled(): %s", provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Timber.tag(INNER_TAG).d("onLocationChanged(): %s", location);
            lastLocation = location;
            lastLocationObtainedTime = System.currentTimeMillis();
            setLastGpsAccuracy(lastLocation);
            updateGpsStatus(lastLocation, lastLocationObtainedTime, System.currentTimeMillis());
            updateSystemTimeChange(lastLocation);
            //TODO: check if does not interfere with reconnection getParseHandler().post(new MeasurementUpdater(location));
        }
    };

    private LocationListener dynamicLocationListener = new LocationListener() {
        private final String INNER_TAG = CollectorService.class.getSimpleName() + ".Dynamic" + LocationListener.class.getSimpleName();

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // on Android 10 this callback will never be invoked
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
            Timber.tag(INNER_TAG).d("onLocationChanged(): %s", location);
            lastLocation = location;
            long locationObtainedTime = System.currentTimeMillis();
            lastLocationObtainedTime = locationObtainedTime;
            setLastGpsAccuracy(location);
            measurementUpdater.setLastLocation(location, locationObtainedTime);
        }
    };

    // ========== EVENTS ========== //

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementProcessedEvent event) {
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
                Timber.d("updateDynamicInterval(): Skipping GPS reconnection because of too small interval difference: %s", intervalDiff);
                return;
            }
            Timber.d("updateDynamicInterval(): New interval calculated: %s difference to previous %s", interval, intervalDiff);
            // save calculated interval
            currentIntervalValue.set(interval);
            measurementUpdater.setMinDistanceAndInterval(transportMode.getDistance(), interval);
            // reconnect gps
            try {
                synchronized (dynamicLocationListenerLock) {
                    locationManager.removeUpdates(dynamicLocationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, currentIntervalValue.get(), 0, dynamicLocationListener);
                    Timber.d("updateDynamicInterval(): GPS reconnected with min distance: %s and min time: %s", transportMode.getDistance(), currentIntervalValue);
                }
            } catch (SecurityException ex) {
                Timber.e(ex, "updateDynamicInterval(): fine location permission is denied");
                stopSelf();
            }
            conditionsNotAchievedCounter = CONDITIONS_NOT_ACHIEVED_COUNTER_INIT;
            return;
        } else if (result == ParseResult.AccuracyNotAchieved || result == ParseResult.NoNetworkSignal
                || result == ParseResult.DistanceNotAchieved) {
            if (currentIntervalValue.get() != transportMode.getMaxTime()) {
                if (conditionsNotAchievedCounter <= 0) {
                    Timber.d("updateDynamicInterval(): GPS reconnected with max interval: %s because of fail: %s", transportMode.getMaxTime(), result);
                    // restore and save interval (increment to max because we don't get appropriate result at all)
                    int newInterval = Math.min(currentIntervalValue.get() + 500, transportMode.getMaxTime());
                    currentIntervalValue.set(newInterval);
                    measurementUpdater.setMinDistanceAndInterval(transportMode.getDistance(), newInterval);
                    try {
                        synchronized (dynamicLocationListenerLock) {
                            locationManager.removeUpdates(dynamicLocationListener);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, transportMode.getMaxTime(), 0, dynamicLocationListener);
                        }
                    } catch (SecurityException ex) {
                        Timber.e(ex, "updateDynamicInterval(): fine location permission is denied");
                        stopSelf();
                    }
                    conditionsNotAchievedCounter = CONDITIONS_NOT_ACHIEVED_COUNTER_INIT;
                    return;
                } else {
                    Timber.d("updateDynamicInterval(): Skipping GPS reconnection because of fail: %s", result);
                    conditionsNotAchievedCounter--;
                    return;
                }
            } else {
                Timber.d("updateDynamicInterval(): GPS failed because of: %s but not reconnected with same parameters", result);
                return;
            }
        } else if (result == ParseResult.LocationTooOld) {
            Timber.d("updateDynamicInterval(): Skipping GPS reconnection because location is too old");
            // TODO: consider reconnection
            setGpsStatus(GpsStatus.NoLocation);
            return;
        } else {// only when SaveFailed
            Timber.d("updateDynamicInterval(): Skipping GPS reconnection");
            return;
        }
    }

    // ========== BROADCAST RECEIVERS ========== //

    private BroadcastReceiver stopRequestBroadcastReceiver = new BroadcastReceiver() {
        private final String INNER_TAG = CollectorService.class.getSimpleName() + ".StopRequest" + BroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Timber.tag(INNER_TAG).d("stopRequestBroadcastReceiver.onReceive(): Received broadcast intent: %s", intent);
            if (intent.getAction().equals(BROADCAST_INTENT_STOP_SERVICE)) {
                stopSelf();
            }
        }
    };

    private BroadcastReceiver batteryStatusBroadcastReceiver = new BatteryStatusBroadcastReceiver();

    private BroadcastReceiver locationModeOrProvidersChanged = new LocationModeOrProvidersChangedReceiver(this);

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

    private Handler getExternalBroadcastSenderHandler() {
        if (externalBroadcastSenderHandler == null) {
            externalBroadcastSenderThread = new HandlerThread("ExternalBroadcastSenderHandler");
            externalBroadcastSenderThread.start();
            externalBroadcastSenderHandler = new Handler(externalBroadcastSenderThread.getLooper());
        }
        return externalBroadcastSenderHandler;
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
        GpsStatus status;
        if (location == null) {
            status = GpsStatus.Initializing;
        } else if (!locationValidator.isUpToDate(gpsTimestamp, systemTimestamp)) {
            status = GpsStatus.NoLocation;
        } else if (!locationValidator.hasRequiredAccuracy(location)) {
            status = GpsStatus.LowAccuracy;
        } else {
            status = GpsStatus.Ok;
        }
        Timber.d("updateGpsStatus(): Updating gps status = %s", status);
        setGpsStatus(status);
    }

    private void updateSystemTimeChange(Location location) {
        if (location == null) {
            Timber.w("updateSystemTimeChange(): Location is null");
            return;
        }
        boolean valid = systemTimeValidator.isValid(System.currentTimeMillis(), location.getTime());
        if (valid) {
            if (lastIsSystemTimeValid != Validity.Valid) {
                Timber.d("updateSystemTimeChange(): Setting system time to valid");
                EventBus.getDefault().postSticky(new SystemTimeChangedEvent(Validity.Valid));
                lastIsSystemTimeValid = Validity.Valid;
            }
        } else {
            if (lastIsSystemTimeValid != Validity.Invalid) {
                Timber.d("updateSystemTimeChange(): Setting system time to invalid");
                EventBus.getDefault().postSticky(new SystemTimeChangedEvent(Validity.Invalid));
                lastIsSystemTimeValid = Validity.Invalid;
            }
        }
    }

    private Runnable gpsStatusCheck = new Runnable() {
        private final String INNER_TAG = CollectorService.class.getSimpleName() + ".GpsStatusCheckRunnable";

        @Override
        public void run() {
            Timber.tag(INNER_TAG).d("run(): GPS status check performed");
            updateGpsStatus(lastLocation, lastLocationObtainedTime, System.currentTimeMillis());
            scheduleNextGpsStatusCheck();
        }
    };

    private void scheduleNextGpsStatusCheck() {
        Timber.d("scheduleNextGpsStatusCheck(): Setting next GPS status check");
        getGpsStatusHandler().postDelayed(gpsStatusCheck, GPS_STATUS_CHECK_INTERVAL);
    }

    private void cancelNextGpsStatusCheck() {
        Timber.d("cancelNextGpsStatusCheck(): Cancelling GPS status check");
        getGpsStatusHandler().removeCallbacks(gpsStatusCheck);
    }

    private void registerWakeLockAcquirer() {
        if (keepScreenOnMode == KeepScreenOnMode.Disabled) {
            Timber.d("registerWakeLockAcquirer(): WakeLock not configured");
            return;
        }
        periodicalWakeLockAcquirer = new Timer();
        // run scheduled cell listener
        periodicalWakeLockAcquirer.schedule(new TimerTask() {
            private final String INNER_TAG = CollectorService.class.getSimpleName() + ".Periodical" + WakeLock.class.getSimpleName() + "Acquirer";

            @Override
            public void run() {
                synchronized (reacquireWakeLockLock) {
                    WakeLock oldWakeLock = wakeLock;
                    Timber.tag(INNER_TAG).d("run(): New WakeLock acquire");
                    wakeLock = createWakeLock(keepScreenOnMode);
                    wakeLock.acquire(WAKE_LOCK_TIMEOUT);
                    Timber.tag(INNER_TAG).d("run(): Old WakeLock release");
                    if (oldWakeLock != null && oldWakeLock.isHeld())
                        oldWakeLock.release();
                }
            }
        }, 0, WAKE_LOCK_ACQUIRE_INTERVAL);
    }

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

    private List<NeighboringCellInfo> getNeighboringCellInfo(TelephonyManager telephonyManager) {
        try {
            if (getNeighboringCellInfoMethod == null) {
                getNeighboringCellInfoMethod = new Tuple<>(TelephonyManager.class.getMethod("getNeighboringCellInfo"), Boolean.TRUE);
            }
            if (getNeighboringCellInfoMethod.getItem2() == Boolean.TRUE) {
                return (List<NeighboringCellInfo>) getNeighboringCellInfoMethod.getItem1().invoke(telephonyManager);
            }
        } catch (NoSuchMethodException ex) {
            getNeighboringCellInfoMethod = new Tuple<>(null, Boolean.FALSE);
            Timber.w(ex, "getNeighboringCellInfo(): Method not found");
        } catch (Exception ex) {
            Timber.w(ex, "getNeighboringCellInfo(): Cannot get neighboring cell info");
        }
        return null;
    }

    private GpsStatus getGpsStatus() {
        return this.gpsStatus;
    }

    public void setGpsStatus(GpsStatus status) {
        Timber.d("setGpsStatus(): Setting gps status = %s", status);
        boolean statusChanged = (this.gpsStatus != status);
        this.gpsStatus = status;
        float accuracy = getLastGpsAccuracy();
        // Update always because accuracy might change
        EventBus.getDefault().postSticky(new GpsStatusChangedEvent(status, accuracy));
        if (gpsStatus != GpsStatus.Ok) {
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
        switch (status) {
            case LowAccuracy:
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
                break;
            case NoLocation:
                statusString = getString(R.string.status_no_gps_location);
                break;
            case Disabled:
                statusString = getString(R.string.status_disabled);
                break;
            case Initializing:
                statusString = getString(R.string.status_initializing);
                break;
            default:
                statusString = status.toString();
                break;
        }
        return getString(R.string.collector_notification_status, statusString);
    }

    public float getLastGpsAccuracy() {
        return this.lastGpsAccuracy;
    }

    public void setLastGpsAccuracy(Location location) {
        if (location == null) {
            Timber.w("setLastGpsAccuracy(): Location is null");
            return;
        }
        Timber.d("setLastGpsAccuracy(): Setting last gps accuracy = %s", location.getAccuracy());
        this.lastGpsAccuracy = (location.hasAccuracy() ? location.getAccuracy() : 1000);
    }

    public Validity getLastIsSystemTimeValid() {
        return this.lastIsSystemTimeValid;
    }
}
