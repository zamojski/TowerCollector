/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.views;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.events.MapEnabledChanged;
import info.zamojski.soft.towercollector.utils.MapUtils;
import info.zamojski.soft.towercollector.utils.PermissionUtils;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainMapConfigureFragment extends MainFragmentBase {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MapUtils.configureMap(MyApplication.getApplication());
        View rootView = inflater.inflate(R.layout.main_map_configure_fragment, container, false);
        configureControls(rootView);
        return rootView;
    }

    @Override
    protected void configureControls(View view) {
        super.configureControls(view);
        Button enableMapButton = view.findViewById(R.id.main_map_enable_button);
        enableMapButton.setOnClickListener(v -> {
            MainMapConfigureFragmentPermissionsDispatcher.requestEnableMapWithPermissionCheck(MainMapConfigureFragment.this);
        });
        Button disableMapButton = view.findViewById(R.id.main_map_disable_button);
        disableMapButton.setOnClickListener(v -> {
            configureMap(false);
        });
    }

    private void configureMap(boolean enabled) {
        MyApplication.getPreferencesProvider().setMainMapConfigured(true);
        MyApplication.getPreferencesProvider().setMainMapEnabled(enabled);
        EventBus.getDefault().postSticky(new MapEnabledChanged());
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void requestEnableMap() {
        configureMap(true);
    }

    @OnShowRationale({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onStartAtBootRationale(final PermissionRequest request) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.permission_required)
                .setMessage(R.string.permission_map_rationale_message)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_proceed, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onStartAtBootPermissionDenied() {
        Toast.makeText(getActivity(), R.string.permission_map_denied_message, Toast.LENGTH_LONG).show();
    }

    @OnNeverAskAgain({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void onStartAtBootNeverAskAgain() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.permission_denied)
                .setMessage(R.string.permission_map_never_ask_again_message)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionUtils.openAppSettings(getActivity());
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String @NotNull [] permissions, int @NotNull [] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainMapConfigureFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}
