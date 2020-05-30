/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.preference.PreferenceManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.model.Cell;
import info.zamojski.soft.towercollector.model.Measurement;
import info.zamojski.soft.towercollector.utils.FileUtils;
import info.zamojski.soft.towercollector.utils.NetworkTypeUtils;

public class MainMapFragment extends MainFragmentBase {

    private MapView mainMapView;
    private MyLocationNewOverlay myLocationOverlay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        configureMapPreferences();
        View rootView = inflater.inflate(R.layout.main_map_fragment, container, false);
        configureControls(rootView);
        return rootView;
    }

    private void configureMapPreferences() {
        Context context = MyApplication.getApplication();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setOsmdroidBasePath(new File(FileUtils.getExternalStorageAppDir(), "MapCache"));
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(150 * 1024 * 1024);
        Configuration.getInstance().setTileFileSystemCacheTrimBytes(100 * 1024 * 1024);
    }

    @Override
    protected void configureOnResume() {
        super.configureOnResume();
        mainMapView.onResume();
    }

    @Override
    protected void configureOnPause() {
        super.configureOnPause();
        mainMapView.onPause();
    }

    @Override
    protected void configureControls(View view) {
        super.configureControls(view);
        mainMapView = view.findViewById(R.id.main_map);
        mainMapView.setTileSource(TileSourceFactory.MAPNIK);
        mainMapView.setMultiTouchControls(true);
        mainMapView.setMinZoomLevel(null);
        mainMapView.setMaxZoomLevel(20.0);
        mainMapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);

        IMapController mapController = mainMapView.getController();
        mapController.setZoom(MyApplication.getPreferencesProvider().getMainMapZoomLevel());
        mainMapView.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                return false;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                MyApplication.getPreferencesProvider().setMainMapZoomLevel((float) zoomEvent.getZoomLevel());
                return false;
            }
        }));

        printLastMeasurements(mapController);
    }

    private void printLastMeasurements(IMapController mapController) {
        Measurement lastMeasurement = MeasurementsDatabase.getInstance(getContext()).getLastMeasurement();
        if (lastMeasurement != null) {
            GeoPoint startPoint = new GeoPoint(lastMeasurement.getLatitude(), lastMeasurement.getLongitude());
            mapController.setCenter(startPoint);

            // TODO: temporarily load 1000 measurements instead of these visible
            List<Measurement> measurements = MeasurementsDatabase.getInstance(getContext()).getMeasurementsPart(0, 1000);

            // TODO: don't print on main thread
            // create markers
            List<Marker> items = new ArrayList<>();
            for (Measurement m : measurements) {
                Marker item = new Marker(mainMapView);
                List<Cell> cells = m.getMainCells();
                @DrawableRes int iconId;
                if (cells.size() == 1) {
                    iconId = NetworkTypeUtils.getNetworkGroupIcon(cells.get(0).getNetworkType());
                } else {
                    iconId = NetworkTypeUtils.getNetworkGroupIcon(cells.get(0).getNetworkType(), cells.get(1).getNetworkType());
                }
                item.setIcon(getResources().getDrawable(iconId));
                item.setPosition(new GeoPoint(m.getLatitude(), m.getLongitude()));
                items.add(item);
            }

            // add overlay
            mainMapView.getOverlays().addAll(items);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementSavedEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PrintMainWindowEvent event) {
    }
}
