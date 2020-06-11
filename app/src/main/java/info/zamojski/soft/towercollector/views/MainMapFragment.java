/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.List;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.model.Boundaries;
import info.zamojski.soft.towercollector.model.MapCell;
import info.zamojski.soft.towercollector.model.MapMeasurement;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.FileUtils;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;
import info.zamojski.soft.towercollector.utils.ResourceUtils;
import timber.log.Timber;

public class MainMapFragment extends MainFragmentBase {

    private static final int MAP_DATA_LOAD_DELAY_IN_MILLIS = 200;

    private MapView mainMapView;
    //    private MyLocationNewOverlay myLocationOverlay;
    private RadiusMarkerClusterer markersOverlay;
    private Bitmap clusterIcon;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        configureMapPreferences();
        View rootView = inflater.inflate(R.layout.main_map_fragment, container, false);
        configureControls(rootView);
        return rootView;
    }

    @Override
    protected void configureOnResume() {
        super.configureOnResume();
        if (mainMapView != null)
            mainMapView.onResume();
//        myLocationOverlay.enableFollowLocation();
//        myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void configureOnPause() {
        super.configureOnPause();
        if (mainMapView != null)
            mainMapView.onPause();
//        myLocationOverlay.disableFollowLocation();
//        myLocationOverlay.disableMyLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
//        displayCurrentLocation();
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
        mainMapView.setTileSource(TileSourceFactory.MAPNIK);
        mainMapView.setMultiTouchControls(true);
        mainMapView.setMinZoomLevel(5.0);
        mainMapView.setMaxZoomLevel(20.0);

//        // configure zoom using mouse wheel
        //todo doesnt work in emulator, probably due to tabs
//        mainMapView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
//            @Override
//            public boolean onGenericMotion(View v, MotionEvent event) {
//                if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
//                    if (event.getAction() == MotionEvent.ACTION_SCROLL) {
//                        if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0.0f)
//                            mainMapView.getController().zoomOut();
//                        else {
//                            mainMapView.getController().zoomIn();
//                        }
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });


        IMapController mapController = mainMapView.getController();
        mapController.setZoom(MyApplication.getPreferencesProvider().getMainMapZoomLevel());
        mainMapView.addMapListener(new DelayedMapListener(new MapListener() {
            private final String INNER_TAG = MainMapFragment.class.getSimpleName() + "." + DelayedMapListener.class.getSimpleName();

            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                Timber.tag(INNER_TAG).d("onScroll(): Scrolling to x=%1$s, y=%2$s", scrollEvent.getX(), scrollEvent.getY());
                loadMarkers();
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
                loadMarkers();
                return false;
            }
        }, MAP_DATA_LOAD_DELAY_IN_MILLIS));
    }

//    private void displayCurrentLocation() {
//        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(MyApplication.getApplication()), mainMapView);
//        myLocationOverlay.enableFollowLocation();
//        myLocationOverlay.setDrawAccuracyEnabled(true);
//        //TODO myLocationOverlay.setPersonIcon();
//        myLocationOverlay.setEnableAutoStop(true);
//        mainMapView.getOverlayManager().add(myLocationOverlay);
//
//        ImageButton btCenterMap = getView().findViewById(R.id.ic_center_map);
//
//        btCenterMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //Log.i(TAG, "centerMap clicked ");
//                //TODO get current location and move to it
//                Measurement lastMeasurement = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLastMeasurement();
//                if (lastMeasurement != null) {
//                    GeoPoint myPosition = new GeoPoint(lastMeasurement.getLatitude(), lastMeasurement.getLongitude());
//                    mainMapView.getController().animateTo(myPosition);
//                }
//            }
//        });
//
//        ImageButton btFollowMe = getView().findViewById(R.id.ic_follow_me);
//
//        btFollowMe.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //TODO logging Log.i(TAG, "btFollowMe clicked ");
//                if (!myLocationOverlay.isFollowLocationEnabled()) {
//                    myLocationOverlay.enableFollowLocation();
//                    btFollowMe.setImageResource(R.drawable.ic_follow_me_on);
//                } else {
//                    myLocationOverlay.disableFollowLocation();
//                    btFollowMe.setImageResource(R.drawable.ic_follow_me);
//                }
//            }
//        });
//    }

    private void loadMarkers() {
        //TODO async loading
        Boundaries boundaries = getVisibleBoundaries();
        List<MapMeasurement> measurements = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getMeasurementsInArea(boundaries);
        Timber.d("loadMarkers(): Loaded %s markers", measurements.size());
        mainMapView.getOverlays().remove(markersOverlay);
        markersOverlay.onDetach(mainMapView);
        markersOverlay = createMarkersOverlay();
        for (MapMeasurement m : measurements) {
            addMarker(m);
        }
        mainMapView.getOverlays().add(markersOverlay);
        if (mainMapView.isAnimating()) {
            mainMapView.postInvalidate();
        } else {
            mainMapView.invalidate();
        }
    }

    private Boundaries getVisibleBoundaries() {
        BoundingBox boundingBox = mainMapView.getProjection().getBoundingBox();
        BoundingBox boundingBoxWithReserve = boundingBox.increaseByScale(1.2f); // 10% more each side
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

    private void addMarker(MapMeasurement m) {
        List<MapCell> mainCells = m.getMainCells();
        @DrawableRes int iconId;
        if (mainCells.size() == 1) {
            iconId = NetworkTypeUtils.getNetworkGroupIcon(mainCells.get(0).getNetworkType());
        } else {
            iconId = NetworkTypeUtils.getNetworkGroupIcon(mainCells.get(0).getNetworkType(), mainCells.get(1).getNetworkType());
        }
        Marker item = new Marker(mainMapView);
        item.setIcon(getResources().getDrawable(iconId));
        item.setTitle(String.valueOf(m.getDescription()));
        item.setPosition(new GeoPoint(m.getLatitude(), m.getLongitude()));
        markersOverlay.add(item);
    }

    private void moveToLastMeasurement() {
        Measurement lastMeasurement = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getLastMeasurement();
        if (lastMeasurement != null) {
            moveToMeasurement(lastMeasurement.getLatitude(), lastMeasurement.getLongitude());
        } else {
            Timber.d("moveToLastMeasurement(): No measurements");
        }
    }

    private void moveToMeasurement(double lat, double lon) {
        Timber.d("moveToMeasurement(): Moving screen to lat=%1$s, lon=%2$s", lat, lon);
        GeoPoint startPoint = new GeoPoint(lat, lon);
        mainMapView.getController().setCenter(startPoint);
    }

    private void configureMapPreferences() {
        Context context = MyApplication.getApplication();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        File mapDataFolder = new File(FileUtils.getExternalStorageAppDir(), "MapData");
        Configuration.getInstance().setOsmdroidBasePath(mapDataFolder);
        Configuration.getInstance().setOsmdroidTileCache(new File(mapDataFolder, "Cache"));
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(150 * 1024 * 1024);
        Configuration.getInstance().setTileFileSystemCacheTrimBytes(100 * 1024 * 1024);
    }

    private Bitmap getClusterIcon() {
        if (clusterIcon == null) {
            clusterIcon = ResourceUtils.getDrawableBitmap(MyApplication.getApplication(), R.drawable.dot_cluster);
        }
        return clusterIcon;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementSavedEvent event) {
        Timber.d("onEvent(): MeasurementSavedEvent");
        //TODO don't add infinitely
        MapMeasurement m = MapMeasurement.fromMeasurement(event.getMeasurement());
        addMarker(m);
        moveToMeasurement(m.getLatitude(), m.getLongitude());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PrintMainWindowEvent event) {
        Timber.d("onEvent(): PrintMainWindowEvent");
        loadMarkers();
    }
}
