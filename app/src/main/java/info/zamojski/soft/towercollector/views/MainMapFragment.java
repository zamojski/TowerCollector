/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.net.ConnectivityManagerCompat;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.controls.DialogManager;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.map.FollowMyLocationOverlay;
import info.zamojski.soft.towercollector.model.Boundaries;
import info.zamojski.soft.towercollector.model.MapCell;
import info.zamojski.soft.towercollector.model.MapMeasurement;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.model.Statistics;
import info.zamojski.soft.towercollector.model.Tuple;
import info.zamojski.soft.towercollector.utils.GpsUtils;
import info.zamojski.soft.towercollector.utils.MapUtils;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;
import info.zamojski.soft.towercollector.utils.ResourceUtils;
import timber.log.Timber;

public class MainMapFragment extends MainFragmentBase implements FollowMyLocationOverlay.FollowMyLocationChangeListener {

    private static final int MAP_DATA_LOAD_DELAY_IN_MILLIS = 200;
    private static final int MAX_MARKERS_ADDED_INDIVIDUALLY = 500;
    private static final float BOUNDARIES_INCREASE_FACTOR = 1.2f; // 10% more each side

    public static final OnlineTileSourceBase TILE_SOURCE = TileSourceFactory.MAPNIK;

    private MapView mainMapView;
    private FollowMyLocationOverlay myLocationOverlay;
    private static boolean shouldEnableMyLocation = true;
    private ImageButton followMeButton;
    private ImageButton myLocationButton;
    private ImageButton toggleLocationButton;
    private ImageButton helpButton;
    private RadiusMarkerClusterer markersOverlay;
    private Bitmap clusterIcon;
    private BackgroundMarkerLoaderTask backgroundMarkerLoaderTask;
    private boolean missedMapZoomScrollUpdates = false;
    private int markersAddedIndividually = 0;
    private BoundingBox lastLoadedBoundingBox = null;
    private boolean isLightThemeForced;
    private Resources.Theme theme;

    private ConnectivityManager connectivityManager;
    private boolean isNetworkCallbackRegistered = false;

    private SimpleDateFormat dateTimeFormatStandard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MapUtils.configureMap(MyApplication.getApplication());
        View rootView = inflater.inflate(R.layout.main_map_fragment, container, false);
        configureControls(rootView);
        connectivityManager = (ConnectivityManager) MyApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        return rootView;
    }

    @Override
    protected void configureOnResume() {
        super.configureOnResume();
        boolean themeChanged = reloadTheme();
        if (themeChanged) {
            reloadMapTheme();
        }
        reloadMarkers(true);
        registerNetworkCallback();
        if (mainMapView != null) {
            mainMapView.onResume();
        }
        setFollowMe(MyApplication.getPreferencesProvider().isMainMapFollowMeEnabled());
        setToggleMyLocation(shouldEnableMyLocation);
    }

    @Override
    protected void configureOnPause() {
        super.configureOnPause();
        if (mainMapView != null)
            mainMapView.onPause();
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.disableMyLocation();
        unregisterNetworkCallback();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (backgroundMarkerLoaderTask != null)
            backgroundMarkerLoaderTask.cancel(false);
        if (mainMapView != null)
            mainMapView.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        markersOverlay = createMarkersOverlay();
        mainMapView.getOverlays().add(markersOverlay);
        mainMapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                Timber.d("onFirstLayout(): Move to last measurement");
                moveToLastMeasurement();
            }
        });
    }

    private RadiusMarkerClusterer createMarkersOverlay() {
        RadiusMarkerClusterer overlay = new RadiusMarkerClusterer(MyApplication.getApplication());
        overlay.setIcon(getClusterIcon());
        overlay.setRadius(100);
        overlay.setMaxClusteringZoomLevel(MyApplication.getPreferencesProvider().getMapMaxZoomClusteringLevel());
        return overlay;
    }

    @Override
    protected void configureControls(View view) {
        super.configureControls(view);
        reloadTheme();
        mainMapView = view.findViewById(R.id.main_map);
        followMeButton = view.findViewById(R.id.main_map_follow_me_button);
        followMeButton.setOnLongClickListener(IMAGE_BUTTON_LONG_CLICK_LISTENER);
        myLocationButton = view.findViewById(R.id.main_map_my_location_button);
        myLocationButton.setOnLongClickListener(IMAGE_BUTTON_LONG_CLICK_LISTENER);
        toggleLocationButton = view.findViewById(R.id.main_map_location_toggle_button);
        toggleLocationButton.setOnLongClickListener(IMAGE_BUTTON_LONG_CLICK_LISTENER);
        helpButton = view.findViewById(R.id.main_map_help_button);
        helpButton.setOnLongClickListener(IMAGE_BUTTON_LONG_CLICK_LISTENER);

        TextView copyrightTextView = view.findViewById(R.id.main_map_copyright);
        copyrightTextView.setMovementMethod(LinkMovementMethod.getInstance());

        mainMapView.setTileSource(TILE_SOURCE);
        mainMapView.setMultiTouchControls(true);
        mainMapView.setMinZoomLevel((double)Math.max(3, TILE_SOURCE.getMinimumZoomLevel()));
        mainMapView.setMaxZoomLevel((double)TILE_SOURCE.getMaximumZoomLevel());
        mainMapView.getZoomController().getDisplay().setAdditionalPixelMargins(0, 0, 0, 32);
        IMapController mapController = mainMapView.getController();
        mapController.setZoom(MyApplication.getPreferencesProvider().getMainMapZoomLevel());

        myLocationOverlay = new FollowMyLocationOverlay(mainMapView);
        myLocationOverlay.setFollowMyLocationChangeListener(this);
        myLocationOverlay.setDrawAccuracyEnabled(true);
        myLocationOverlay.setEnableAutoStop(true);
        setFollowMe(MyApplication.getPreferencesProvider().isMainMapFollowMeEnabled());
        mainMapView.getOverlays().add(myLocationOverlay);

        ScaleBarOverlay scaleOverlay = new ScaleBarOverlay(mainMapView);
        scaleOverlay.setAlignBottom(true);
        scaleOverlay.setUnitsOfMeasure(MyApplication.getPreferencesProvider().getUseImperialUnits() ? ScaleBarOverlay.UnitsOfMeasure.imperial : ScaleBarOverlay.UnitsOfMeasure.metric);
        mainMapView.getOverlays().add(scaleOverlay);

        reloadMapTheme();

        // configure zoom using mouse wheel
        mainMapView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
                    if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                        if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
                            mainMapView.getController().zoomOut();
                        else {
                            mainMapView.getController().zoomIn();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location lastLocation = myLocationOverlay.getLastFix();
                Timber.i("onMyLocationClick(): Moving to %s", lastLocation);
                if (lastLocation != null) {
                    GeoPoint myPosition = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
                    mainMapView.getController().stopAnimation(false);
                    mainMapView.getController().animateTo(myPosition);
                }
            }
        });

        followMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFollowMe(!myLocationOverlay.isFollowLocationEnabled());
            }
        });

        toggleLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToggleMyLocation(!myLocationOverlay.isMyLocationEnabled());
            }
        });

        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("helpButton.click(): Showing map help");
                DialogManager.createHtmlInfoDialog(getActivity(), R.string.info_map_help_title, R.raw.info_map_help, false, false).show();
            }
        });

        mainMapView.addMapListener(new DelayedMapListener(new MapListener() {
            private final String INNER_TAG = MainMapFragment.class.getSimpleName() + "." + DelayedMapListener.class.getSimpleName();

            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                Timber.tag(INNER_TAG).d("onScroll(): Scrolling to x=%1$s, y=%2$s", scrollEvent.getX(), scrollEvent.getY());
                reloadMarkers(false);
                double zoomLevel = mainMapView.getZoomLevelDouble();
                Timber.tag(INNER_TAG).d("onScroll(): Changing zoom level to %s", zoomLevel);
                MyApplication.getPreferencesProvider().setMainMapZoomLevel((float) zoomLevel);
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                Timber.tag(INNER_TAG).d("onZoom(): Changing zoom level to %s", zoomEvent.getZoomLevel());
                MyApplication.getPreferencesProvider().setMainMapZoomLevel((float) zoomEvent.getZoomLevel());
                reloadMarkers(true);
                return true;
            }
        }, MAP_DATA_LOAD_DELAY_IN_MILLIS));

        dateTimeFormatStandard = new SimpleDateFormat(getString(R.string.date_time_format_standard), new Locale(getString(R.string.locale)));
    }

    private void reloadMarkers(boolean force) {
        if (backgroundMarkerLoaderTask == null) {
            if (force || lastLoadedBoundingBox == null) {
                Timber.d("reloadMarkers(): Loading markers due to force=%1$s, lastLoadedBoundingBox=%2$s", force, lastLoadedBoundingBox);
                Tuple<Boundaries, BoundingBox> boundaries = getVisibleBoundaries();
                this.backgroundMarkerLoaderTask = new BackgroundMarkerLoaderTask(boundaries.getItem2());
                this.backgroundMarkerLoaderTask.execute(boundaries.getItem1());
            } else {
                BoundingBox boundingBox = mainMapView.getProjection().getBoundingBox();
                double north = boundingBox.getActualNorth();
                double south = boundingBox.getActualSouth();
                double east = boundingBox.getLonEast();
                double west = boundingBox.getLonWest();
                if (!lastLoadedBoundingBox.contains(north, east) || !lastLoadedBoundingBox.contains(north, west)
                        || !lastLoadedBoundingBox.contains(south, east) || !lastLoadedBoundingBox.contains(south, west)) {
                    Timber.d("reloadMarkers(): No overlap between new and previously loaded bounding boxes");
                    reloadMarkers(true);
                } else {
                    Timber.d("reloadMarkers(): New and previously loaded bounding boxes overlap, skipping load");
                }
            }
        } else {
            // background load is active, we miss the scroll/zoom
            missedMapZoomScrollUpdates = true;
        }
    }

    private void displayMarkers(RadiusMarkerClusterer newMarkersOverlay) {
        // cleanup InfoWindows and remove overlay
        for (Marker marker : markersOverlay.getItems()) {
            marker.closeInfoWindow();
        }
        mainMapView.getOverlays().remove(markersOverlay);
        markersOverlay.onDetach(mainMapView);
        // add new overlay
        markersOverlay = newMarkersOverlay;
        mainMapView.getOverlays().add(markersOverlay);
        if (mainMapView.isAnimating()) {
            mainMapView.postInvalidate();
        } else {
            mainMapView.invalidate();
        }
        markersAddedIndividually = 0;
    }

    private Tuple<Boundaries, BoundingBox> getVisibleBoundaries() {
        BoundingBox boundingBox = mainMapView.getProjection().getBoundingBox();
        BoundingBox boundingBoxWithReserve = boundingBox.increaseByScale(BOUNDARIES_INCREASE_FACTOR);
        double minLat = boundingBoxWithReserve.getActualSouth();
        double maxLat = boundingBoxWithReserve.getActualNorth();
        double minLon = boundingBoxWithReserve.getLonWest();
        double maxLon = boundingBoxWithReserve.getLonEast();
        // when passing date line
        if (maxLon < minLon) {
            double swap = minLon;
            minLon = maxLon;
            maxLon = swap;
        }
        return new Tuple<Boundaries, BoundingBox>(new Boundaries(minLat, minLon, maxLat, maxLon), boundingBoxWithReserve);
    }

    private Marker createMarker(MapMeasurement m, long localSinceTimestamp) {
        List<MapCell> mainCells = m.getMainCells();
        boolean isBright = m.containsDiscoveredCells(localSinceTimestamp);
        @DrawableRes int iconId;
        if (mainCells.size() == 1) {
            iconId = NetworkTypeUtils.getNetworkGroupIcon(mainCells.get(0).getNetworkType());
        } else {
            iconId = NetworkTypeUtils.getNetworkGroupIcon(mainCells.get(0).getNetworkType(), mainCells.get(1).getNetworkType());
        }
        Marker item = new Marker(mainMapView);

        Drawable icon = ResourcesCompat.getDrawable(getResources(), iconId, theme);
        if (!isBright) {
            int color = Color.argb(160, 64, 64, 64);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                icon.setColorFilter(new BlendModeColorFilter(color, BlendMode.SRC_ATOP));
            } else {
                icon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
        item.setIcon(icon);
        item.setTitle(dateTimeFormatStandard.format(new Date(m.getMeasuredAt())));
        item.setSnippet(m.getDescription(MyApplication.getApplication()));
        item.setPosition(new GeoPoint(m.getLatitude(), m.getLongitude()));
        item.setAnchor(0.5f, 0.5f);
        item.setOnMarkerClickListener(MARKER_CLICK_LISTENER);
        return item;
    }

    private void moveToLastMeasurement() {
        Measurement lastMeasurement = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLastMeasurement();
        if (lastMeasurement != null) {
            moveToLocation(lastMeasurement.getLatitude(), lastMeasurement.getLongitude());
        } else {
            Timber.d("moveToLastMeasurement(): No measurements, moving to last known location");
            if (GpsUtils.hasGpsPermissions(MyApplication.getApplication())) {
                LocationManager locationManager = (LocationManager) MyApplication.getApplication().getSystemService(Context.LOCATION_SERVICE);
                @SuppressLint("MissingPermission") Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                if (lastKnownLocation != null) {
                    moveToLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                }
            }
        }
    }

    private void moveToLocation(double lat, double lon) {
        Timber.d("moveToLocation(): Moving screen to lat=%1$s, lon=%2$s", lat, lon);
        GeoPoint startPoint = new GeoPoint(lat, lon);
        mainMapView.getController().setCenter(startPoint); // don't animate because it's used on first load
    }

    private void setFollowMe(boolean enable) {
        if (enable) {
            Timber.i("onFollowMeClick(): Enabling follow me");
            myLocationOverlay.enableFollowLocation();
            followMeButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.map_follow_me_enabled, theme));
        } else {
            Timber.i("onFollowMeClick(): Disabling follow me");
            myLocationOverlay.disableFollowLocation();
            followMeButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.map_follow_me_disabled, theme));
        }
        MyApplication.getPreferencesProvider().setMainMapFollowMeEnabled(myLocationOverlay.isFollowLocationEnabled());
    }

    private void setToggleMyLocation(boolean enableMyLocation) {
        Timber.d("toggleLocationButton.click(): %s location", enableMyLocation ? "Enabling" : "Disabling");
        if (enableMyLocation) {
            myLocationOverlay.enableMyLocation();
        } else {
            myLocationOverlay.disableMyLocation();
        }
        shouldEnableMyLocation = enableMyLocation;
        toggleLocationButton.setContentDescription(getString(enableMyLocation ? R.string.main_map_location_disable_button : R.string.main_map_location_enable_button));
        toggleLocationButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), enableMyLocation ? R.drawable.map_location_enabled : R.drawable.map_location_disabled, theme));
    }

    private Bitmap getClusterIcon() {
        if (clusterIcon == null) {
            clusterIcon = ResourceUtils.getDrawableBitmap(MyApplication.getApplication(), R.drawable.dot_cluster);
        }
        return clusterIcon;
    }

    private boolean reloadTheme() {
        boolean previousLightTheme = isLightThemeForced;
        isLightThemeForced = MyApplication.getPreferencesProvider().isMainMapForceLightThemeEnabled();
        theme = isLightThemeForced ? new ContextThemeWrapper(getActivity(), R.style.LightAppTheme).getTheme() : getActivity().getTheme();
        return previousLightTheme != isLightThemeForced;
    }

    private void reloadMapTheme() {
        myLocationButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.map_my_location, theme));
        helpButton.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.map_help, theme));
        boolean useDarkTheme = MyApplication.getCurrentAppTheme() == R.style.DarkAppTheme && !isLightThemeForced;
        mainMapView.getOverlayManager().getTilesOverlay().setColorFilter(useDarkTheme ? TilesOverlay.INVERT_COLORS : null);
        myLocationOverlay.setDirectionArrow(ResourceUtils.getDrawableBitmap(MyApplication.getApplication(), R.drawable.map_person, theme),
                ResourceUtils.getDrawableBitmap(MyApplication.getApplication(), R.drawable.map_direction_arrow, theme));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementSavedEvent event) {
        if (++markersAddedIndividually <= MAX_MARKERS_ADDED_INDIVIDUALLY) {
            Timber.d("onEvent(): Adding saved measurement to the map, added %s of %s", markersAddedIndividually, MAX_MARKERS_ADDED_INDIVIDUALLY);
            MapMeasurement m = MapMeasurement.fromMeasurement(event.getMeasurement());
            markersOverlay.add(createMarker(m, event.getStatistics().getSinceLocal()));
            markersOverlay.invalidate();
        } else {
            Timber.d("onEvent(): Reloading measurements on the map after saving measurement due to threshold %s reached", MAX_MARKERS_ADDED_INDIVIDUALLY);
            reloadMarkers(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PrintMainWindowEvent event) {
        reloadMarkers(true);
    }

    @Override
    public void onFollowMyLocationChanged(boolean enabled) {
        setFollowMe(enabled);
    }

    private class BackgroundMarkerLoaderTask extends AsyncTask<Boundaries, Void, RadiusMarkerClusterer> {
        private final String INNER_TAG = MainMapFragment.class.getSimpleName() + "." + BackgroundMarkerLoaderTask.class.getSimpleName();

        private final BoundingBox boundingBox;

        public BackgroundMarkerLoaderTask(BoundingBox boundingBox) {
            this.boundingBox = boundingBox;
        }

        @Override
        protected RadiusMarkerClusterer doInBackground(Boundaries... boundariesParams) {
            RadiusMarkerClusterer result = createMarkersOverlay();
            try {
                Boundaries boundaries = boundariesParams[0];
                Statistics stats = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsStatistics();
                List<MapMeasurement> measurements = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsInArea(boundaries);
                for (MapMeasurement m : measurements) {
                    if (isCancelled())
                        return null;
                    result.add(createMarker(m, stats.getSinceLocal()));
                }
            } catch (Exception ex) {
                Timber.tag(INNER_TAG).e(ex, "doInBackground(): Failed to load markers");
                cancel(false);
            }

            if (!isCancelled()) {
                Timber.tag(INNER_TAG).d("doInBackground(): Loaded %s markers", result.getItems().size());
                return result;
            }
            Timber.tag(INNER_TAG).d("doInBackground(): Markers loading cancelled");
            return null;
        }

        @Override
        protected void onPostExecute(RadiusMarkerClusterer result) {
            if (!isCancelled() && result != null) {
                displayMarkers(result);
            }
            lastLoadedBoundingBox = boundingBox;
            backgroundMarkerLoaderTask = null;
            // reload if scroll/zoom occurred while loading
            if (missedMapZoomScrollUpdates) {
                Timber.tag(INNER_TAG).d("onPostExecute(): Missed scroll/zoom updates - reloading");
                missedMapZoomScrollUpdates = false;
                reloadMarkers(true);
            }
        }
    }

    private static final Marker.OnMarkerClickListener MARKER_CLICK_LISTENER = new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            if (marker.isInfoWindowShown()) {
                marker.closeInfoWindow();
            } else {
                marker.showInfoWindow();
            }
            return true;
        }
    };

    private final View.OnLongClickListener IMAGE_BUTTON_LONG_CLICK_LISTENER = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            Toast.makeText(getActivity(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
            return true;
        }
    };

    private void registerNetworkCallback() {
        if (!MyApplication.getPreferencesProvider().isMapUpdatedOnlyOnUnmetered()) {
            mainMapView.setUseDataConnection(true);
            return;
        }
        // register for network connectivity changes
        NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            networkRequestBuilder.addCapability(NetworkCapabilities.NET_CAPABILITY_TEMPORARILY_NOT_METERED);
        }
        connectivityManager.registerNetworkCallback(networkRequestBuilder.build(), networkCallback);
        isNetworkCallbackRegistered = true;
        updateNetworkStatus();
    }

    private void unregisterNetworkCallback() {
        if (isNetworkCallbackRegistered) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        isNetworkCallbackRegistered = false;
    }

    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            updateNetworkStatus();
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            updateNetworkStatus();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            updateNetworkStatus();
        }
    };

    private void updateNetworkStatus() {
        boolean isNetworkMetered = ConnectivityManagerCompat.isActiveNetworkMetered(connectivityManager);
        mainMapView.setUseDataConnection(!isNetworkMetered);
    }
}
