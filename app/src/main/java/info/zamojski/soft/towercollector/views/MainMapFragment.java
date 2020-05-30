/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
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
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
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
import info.zamojski.soft.towercollector.utils.DateUtils;
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
            // TODO: temporarily load 1000 measurements instead of these visible
            List<Measurement> measurements = MeasurementsDatabase.getInstance(getContext()).getMeasurementsPart(0, 1000);

            GeoPoint startPoint = new GeoPoint(measurements.get(0).getLatitude(), measurements.get(0).getLongitude());
            mapController.setCenter(startPoint);

            // TODO: don't print on main thread
            // create markers
            RadiusMarkerClusterer markers = new RadiusMarkerClusterer(getContext());
            Bitmap clusterIcon = getBitmap(R.drawable.dot_cluster);
            markers.setIcon(clusterIcon);
            markers.setRadius(250);
            markers.setMaxClusteringZoomLevel(15);

            for (Measurement m : measurements) {
                Marker item = new Marker(mainMapView);
                List<Cell> mainCells = m.getMainCells();
                @DrawableRes int iconId;
                if (mainCells.size() == 1) {
                    iconId = NetworkTypeUtils.getNetworkGroupIcon(mainCells.get(0).getNetworkType());
                } else {
                    iconId = NetworkTypeUtils.getNetworkGroupIcon(mainCells.get(0).getNetworkType(), mainCells.get(1).getNetworkType());
                }
                item.setTitle(String.valueOf(m.getMeasuredAt()));
                item.setIcon(getResources().getDrawable(iconId));
                item.setPosition(new GeoPoint(m.getLatitude(), m.getLongitude()));
                markers.add(item);
            }

            // add overlay
            mainMapView.getOverlays().add(markers);
        }
    }

    private Bitmap getBitmap(@DrawableRes int drawableId) {
        Drawable drawable = getResources().getDrawable(drawableId);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementSavedEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PrintMainWindowEvent event) {
    }
}
