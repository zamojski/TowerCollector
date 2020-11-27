/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.map.FollowMyLocationOverlay;
import info.zamojski.soft.towercollector.model.Boundaries;
import info.zamojski.soft.towercollector.model.MapCell;
import info.zamojski.soft.towercollector.model.MapMeasurement;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.MapUtils;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;
import info.zamojski.soft.towercollector.utils.ResourceUtils;
import timber.log.Timber;

public class MainMapFragment extends MainFragmentBase implements FollowMyLocationOverlay.FollowMyLocationChangeListener {

    private static final int MAP_DATA_LOAD_DELAY_IN_MILLIS = 200;
    private static final int MAX_MARKERS_ADDED_INDIVIDUALLY = 500;
    private static final float BOUNDARIES_INCREASE_FACTOR = 1.2f; // 10% more each side

    private MapView mainMapView;
    private FollowMyLocationOverlay myLocationOverlay;
    private ImageButton followMeButton;
    private RadiusMarkerClusterer markersOverlay;
    private Bitmap clusterIcon;
    private BackgroundMarkerLoaderTask backgroundMarkerLoaderTask;
    private boolean missedMapZoomScrollUpdates = false;
    private int markersAddedIndividually = 0;

    private SimpleDateFormat dateTimeFormatStandard;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MapUtils.configureMap(MyApplication.getApplication());
        View rootView = inflater.inflate(R.layout.main_map_fragment, container, false);
        configureControls(rootView);
        return rootView;
    }

    @Override
    protected void configureOnResume() {
        super.configureOnResume();
        if (mainMapView != null)
            mainMapView.onResume();
        setFollowMe(MyApplication.getPreferencesProvider().isMainMapFollowMeEnabled());
        myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void configureOnPause() {
        super.configureOnPause();
        if (mainMapView != null)
            mainMapView.onPause();
        myLocationOverlay.disableFollowLocation();
        myLocationOverlay.disableMyLocation();
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
        overlay.setMaxClusteringZoomLevel(13);
        return overlay;
    }

    @Override
    protected void configureControls(View view) {
        super.configureControls(view);
        mainMapView = view.findViewById(R.id.main_map);
        followMeButton = view.findViewById(R.id.main_map_follow_me_button);
        ImageButton myLocationButton = view.findViewById(R.id.main_map_my_location_button);

        mainMapView.setTileSource(TileSourceFactory.MAPNIK);
        mainMapView.setMultiTouchControls(true);
        mainMapView.setMinZoomLevel(5.0);
        mainMapView.setMaxZoomLevel(20.0);

        if (MyApplication.getCurrentAppTheme() == R.style.DarkAppTheme && !MyApplication.getPreferencesProvider().isMainMapForceLightThemeEnabled())
            mainMapView.getOverlayManager().getTilesOverlay().setColorFilter(TilesOverlay.INVERT_COLORS);

        IMapController mapController = mainMapView.getController();
        mapController.setZoom(MyApplication.getPreferencesProvider().getMainMapZoomLevel());

        myLocationOverlay = new FollowMyLocationOverlay(mainMapView);
        myLocationOverlay.setFollowMyLocationChangeListener(this);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        myLocationOverlay.setEnableAutoStop(true);
        setFollowMe(MyApplication.getPreferencesProvider().isMainMapFollowMeEnabled());
        mainMapView.getOverlays().add(myLocationOverlay);

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

        mainMapView.addMapListener(new DelayedMapListener(new MapListener() {
            private final String INNER_TAG = MainMapFragment.class.getSimpleName() + "." + DelayedMapListener.class.getSimpleName();

            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                Timber.tag(INNER_TAG).d("onScroll(): Scrolling to x=%1$s, y=%2$s", scrollEvent.getX(), scrollEvent.getY());
                reloadMarkers();
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                Timber.tag(INNER_TAG).d("onZoom(): Changing zoom level to %s", zoomEvent.getZoomLevel());
                MyApplication.getPreferencesProvider().setMainMapZoomLevel((float) zoomEvent.getZoomLevel());
                if (zoomEvent.getZoomLevel() < 9.0) {
                    //TODO for zoom above certain point don't load actual cells but only the number of them and display toast? level=8-10?
                    Toast.makeText(MyApplication.getApplication(), "Zoom level " + zoomEvent.getZoomLevel(), Toast.LENGTH_SHORT).show();
                } else {
                    //TODO as above?
                }
                reloadMarkers();
                return false;
            }
        }, MAP_DATA_LOAD_DELAY_IN_MILLIS));

        dateTimeFormatStandard = new SimpleDateFormat(getString(R.string.date_time_format_standard), new Locale(getString(R.string.locale)));
    }

    private void reloadMarkers() {
        if (backgroundMarkerLoaderTask == null) {
            this.backgroundMarkerLoaderTask = new BackgroundMarkerLoaderTask();
            Boundaries boundaries = getVisibleBoundaries();
            this.backgroundMarkerLoaderTask.execute(boundaries);
        } else {
            // background load is active, we miss the scroll/zoom
            missedMapZoomScrollUpdates = true;
        }
    }

    private void displayMarkers(RadiusMarkerClusterer newMarkersOverlay) {
        mainMapView.getOverlays().remove(markersOverlay);
        markersOverlay.onDetach(mainMapView);
        markersOverlay = newMarkersOverlay;
        mainMapView.getOverlays().add(markersOverlay);
        if (mainMapView.isAnimating()) {
            mainMapView.postInvalidate();
        } else {
            mainMapView.invalidate();
        }
        markersAddedIndividually = 0;
    }

    private Boundaries getVisibleBoundaries() {
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
        return new Boundaries(minLat, minLon, maxLat, maxLon);
    }

    private Marker createMarker(MapMeasurement m) {
        List<MapCell> mainCells = m.getMainCells();
        @DrawableRes int iconId;
        if (mainCells.size() == 1) {
            iconId = NetworkTypeUtils.getNetworkGroupIcon(mainCells.get(0).getNetworkType());
        } else {
            iconId = NetworkTypeUtils.getNetworkGroupIcon(mainCells.get(0).getNetworkType(), mainCells.get(1).getNetworkType());
        }
        Marker item = new Marker(mainMapView);
        item.setIcon(getResources().getDrawable(iconId, getActivity().getTheme()));
        item.setTitle(dateTimeFormatStandard.format(new Date(m.getMeasuredAt())));
        item.setSnippet(String.valueOf(m.getDescription(MyApplication.getApplication())));
        item.setPosition(new GeoPoint(m.getLatitude(), m.getLongitude()));
        item.setOnMarkerClickListener(MARKER_CLICK_LISTENER);
        return item;
    }

    private void moveToLastMeasurement() {
        Measurement lastMeasurement = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLastMeasurement();
        if (lastMeasurement != null) {
            moveToLocation(lastMeasurement.getLatitude(), lastMeasurement.getLongitude());
        } else {
            Timber.d("moveToLastMeasurement(): No measurements");
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
            followMeButton.setImageResource(R.drawable.map_follow_me_enabled);
        } else {
            Timber.i("onFollowMeClick(): Disabling follow me");
            myLocationOverlay.disableFollowLocation();
            followMeButton.setImageResource(R.drawable.map_follow_me);
        }
        MyApplication.getPreferencesProvider().setMainMapFollowMeEnabled(myLocationOverlay.isFollowLocationEnabled());
    }

    private Bitmap getClusterIcon() {
        if (clusterIcon == null) {
            clusterIcon = ResourceUtils.getDrawableBitmap(MyApplication.getApplication(), R.drawable.dot_cluster);
        }
        return clusterIcon;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementSavedEvent event) {
        if (++markersAddedIndividually <= MAX_MARKERS_ADDED_INDIVIDUALLY) {
            Timber.d("onEvent(): Adding single measurement to the map, added %s of %s", markersAddedIndividually, MAX_MARKERS_ADDED_INDIVIDUALLY);
            MapMeasurement m = MapMeasurement.fromMeasurement(event.getMeasurement());
            markersOverlay.add(createMarker(m));
        } else {
            reloadMarkers();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PrintMainWindowEvent event) {
        reloadMarkers();
    }

    @Override
    public void onFollowMyLocationChanged(boolean enabled) {
        setFollowMe(enabled);
    }

    private class BackgroundMarkerLoaderTask extends AsyncTask<Boundaries, Void, RadiusMarkerClusterer> {
        private final String INNER_TAG = MainMapFragment.class.getSimpleName() + "." + BackgroundMarkerLoaderTask.class.getSimpleName();

        @Override
        protected RadiusMarkerClusterer doInBackground(Boundaries... boundariesParams) {
            RadiusMarkerClusterer result = createMarkersOverlay();
            try {
                Boundaries boundaries = boundariesParams[0];
                List<MapMeasurement> measurements = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsInArea(boundaries);
                for (MapMeasurement m : measurements) {
                    if (isCancelled())
                        return null;
                    result.add(createMarker(m));
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
            backgroundMarkerLoaderTask = null;
            // reload if scroll/zoom occurred while loading
            if (missedMapZoomScrollUpdates) {
                Timber.tag(INNER_TAG).d("onPostExecute(): Missed scroll/zoom updates - reloading");
                missedMapZoomScrollUpdates = false;
                reloadMarkers();
            }
        }
    }

    private static final Marker.OnMarkerClickListener MARKER_CLICK_LISTENER = new Marker.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker, MapView mapView) {
            marker.showInfoWindow();
            return true;
        }
    };
}
