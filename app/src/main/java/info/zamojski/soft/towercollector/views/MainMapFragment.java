/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.preference.PreferenceManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.events.MeasurementSavedEvent;
import info.zamojski.soft.towercollector.events.PrintMainWindowEvent;
import info.zamojski.soft.towercollector.utils.FileUtils;

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
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MeasurementSavedEvent event) {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PrintMainWindowEvent event) {
    }
}
