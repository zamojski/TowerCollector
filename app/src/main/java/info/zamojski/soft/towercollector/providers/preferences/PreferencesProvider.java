/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.providers.preferences;

import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.enums.ExportAction;
import info.zamojski.soft.towercollector.enums.FileType;
import info.zamojski.soft.towercollector.utils.Cache;

import android.content.Context;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PreferencesProvider {

    private static final String ENUM_SERIALIZATION_DELIMITER = ";";

    private final BooleanPreferenceProvider booleanPreferenceProvider;
    private final IntegerPreferenceProvider integerPreferenceProvider;
    private final FloatPreferenceProvider floatPreferenceProvider;
    private final StringPreferenceProvider stringPreferenceProvider;

    private final Cache<Integer, Boolean> booleanCache = new Cache<>();

    public PreferencesProvider(Context context) {
        this.booleanPreferenceProvider = new BooleanPreferenceProvider(context);
        this.integerPreferenceProvider = new IntegerPreferenceProvider(context);
        this.floatPreferenceProvider = new FloatPreferenceProvider(context);
        this.stringPreferenceProvider = new StringPreferenceProvider(context);
    }

    public String getApiKey() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_api_key_key, R.string.preferences_api_key_default_value);
        return value.trim();
    }

    public boolean getUseImperialUnits() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_imperial_units_key, R.bool.preferences_imperial_units_default_value);
        return value;
    }

    public String getAppTheme() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_app_theme_mode_key, R.string.preferences_app_theme_mode_default_value);
        return value;
    }

    public boolean getGpsOptimizationsEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_gps_optimizations_enabled_key, R.bool.preferences_gps_optimizations_enabled_default_value);
        return value;
    }

    public boolean getTrackingEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_tracking_enabled_key, R.bool.preferences_tracking_enabled_default_value);
        return value;
    }

    public boolean getReportErrorsSilently() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_error_reporting_silent_key, R.bool.preferences_error_reporting_silent_default_value);
        return value;
    }

    public boolean getUpdateCheckEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_update_check_enabled_key, R.bool.preferences_update_check_enabled_default_value);
        return value;
    }

    public void setUpdateCheckEnabled(boolean value) {
        booleanPreferenceProvider.setPreference(R.string.preferences_update_check_enabled_key, value);
    }

    public long getLastUpdateCheckDate() {
        int value = integerPreferenceProvider.getPreference(R.string.preferences_last_update_check_date_key, R.integer.preferences_last_update_check_date_default_value);
        return ((long) value) * 1000;
    }

    public void setLastUpdateCheckDate(long value) {
        integerPreferenceProvider.setPreference(R.string.preferences_last_update_check_date_key, (int) (value / 1000));
    }

    public boolean getMainKeepScreenOnMode() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_main_keep_screen_on_mode_key, R.bool.preferences_main_keep_screen_on_mode_default_value);
        return value;
    }

    public String getCollectorKeepScreenOnMode() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_collector_keep_screen_on_mode_key, R.string.preferences_collector_keep_screen_on_mode_default_value);
        return value;
    }

    public boolean getCollectNeighboringCells() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_collect_neighboring_cells_key, R.bool.preferences_collect_neighboring_cells_default_value);
        return value;
    }

    public boolean getNotifyMeasurementsCollected() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_notify_measurements_collected_key, R.bool.preferences_notify_measurements_collected_default_value);
        return value;
    }

    public boolean getHideCollectorNotification() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_hide_collector_notification_key, R.bool.preferences_hide_collector_notification_default_value);
        return value;
    }

    public String getCollectorLowBatteryAction() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_collector_low_battery_action_key, R.string.preferences_collector_low_battery_action_default_value);
        return value;
    }

    public boolean getStartCollectorAtBoot() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_start_collector_at_boot_key, R.bool.preferences_start_collector_at_boot_default_value);
        return value;
    }

    public String getCollectorApiVersion() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_collector_api_version_key, R.string.preferences_collector_api_version_default_value);
        return value;
    }

    public String getFileLoggingLevel() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_file_logging_level_key, R.string.preferences_file_logging_level_default_value);
        return value;
    }

    public boolean getShowIntroduction() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_show_introduction_key, R.bool.preferences_show_introduction_default_value);
        return value;
    }

    public void setShowIntroduction(boolean value) {
        booleanPreferenceProvider.setPreference(R.string.preferences_show_introduction_key, value);
    }

    public boolean getShowCompatibilityWarning() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_show_compatibility_warning_key, R.bool.preferences_show_compatibility_warning_default_value);
        return value;
    }

    public void setShowCompatibilityWarning(boolean value) {
        booleanPreferenceProvider.setPreference(R.string.preferences_show_compatibility_warning_key, value);
    }

    public int getPreviousDeveloperMessagesVersion() {
        int value = integerPreferenceProvider.getPreference(R.string.preferences_latest_developer_message_key, R.integer.preferences_latest_developer_message_default_value);
        return value;
    }

    public void setRecentDeveloperMessagesVersion(int version) {
        integerPreferenceProvider.setPreference(R.string.preferences_latest_developer_message_key, version);
    }

    public int getMainWindowRecentTab() {
        int value = integerPreferenceProvider.getPreference(R.string.preferences_main_window_recent_tab_key, R.integer.preferences_main_window_recent_tab_default_value);
        return value;
    }

    public void setMainWindowRecentTab(int tabIndex) {
        integerPreferenceProvider.setPreference(R.string.preferences_main_window_recent_tab_key, tabIndex);
    }

    public boolean getShowConfiguratorBeforeUpload() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_upload_show_configurator_key, R.bool.preferences_upload_show_configurator_default_value);
        return value;
    }

    public void setShowConfiguratorBeforeUpload(boolean value) {
        booleanPreferenceProvider.setPreference(R.string.preferences_upload_show_configurator_key, value);
    }

    public boolean isOpenCellIdUploadEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_opencellid_enabled_key, R.bool.preferences_opencellid_enabled_default_value);
        return value;
    }

    public void setOpenCellIdUploadEnabled(boolean value) {
        booleanPreferenceProvider.setPreference(R.string.preferences_opencellid_enabled_key, value);
    }

    public boolean isMlsUploadEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_mls_enabled_key, R.bool.preferences_mls_enabled_default_value);
        return value;
    }

    public void setMlsUploadEnabled(boolean value) {
        booleanPreferenceProvider.setPreference(R.string.preferences_mls_enabled_key, value);
    }

    public boolean isReuploadIfUploadFailsEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_reupload_if_upload_fails_key, R.bool.preferences_reupload_if_upload_fails_default_value);
        return value;
    }

    public void setReuploadIfUploadFailsEnabled(boolean value) {
        booleanPreferenceProvider.setPreference(R.string.preferences_reupload_if_upload_fails_key, value);
    }

    public List<FileType> getEnabledExportFileTypes() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_enabled_export_types_key, R.string.preferences_enabled_export_types_default_value);
        FileType[] fileTypes = FileType.valuesOf(TextUtils.split(value, ENUM_SERIALIZATION_DELIMITER));
        return Arrays.asList(fileTypes);
    }

    public void setEnabledExportFileTypes(List<FileType> fileTypes) {
        String value = TextUtils.join(ENUM_SERIALIZATION_DELIMITER, (fileTypes == null ? Collections.EMPTY_LIST : fileTypes));
        stringPreferenceProvider.setPreference(R.string.preferences_enabled_export_types_key, value);
    }

    public String getExportCompressionFormat() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_export_compression_format_key, R.string.preferences_export_compression_format_default_value);
        return value;
    }

    public boolean isShowCollectorStatusBarEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_show_collector_status_bar_key, R.bool.preferences_show_collector_status_bar_default_value);
        return value;
    }

    public ExportAction getExportAction() {
        String value = stringPreferenceProvider.getPreference(R.string.preferences_export_action_key, R.string.preferences_export_action_default_value);
        return ExportAction.valueOf(value);
    }

    public void setExportAction(ExportAction value) {
        stringPreferenceProvider.setPreference(R.string.preferences_export_action_key, value.toString());
    }

    public float getMainMapZoomLevel() {
        float value = floatPreferenceProvider.getPreference(R.string.preferences_main_map_zoom_level_key, R.integer.preferences_main_map_zoom_level_default_value);
        return value;
    }

    public void setMainMapZoomLevel(float zoomLevel) {
        floatPreferenceProvider.setPreference(R.string.preferences_main_map_zoom_level_key, zoomLevel);
    }

    public boolean isMainMapFollowMeEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_main_map_follow_me_enabled_key, R.bool.preferences_main_map_follow_me_enabled_default_value);
        return value;
    }

    public void setMainMapFollowMeEnabled(boolean enabled) {
        booleanPreferenceProvider.setPreference(R.string.preferences_main_map_follow_me_enabled_key, enabled);
    }

    public boolean isMainMapEnabled() {
        Boolean value = booleanCache.get(R.string.preferences_main_map_enable_key);
        if(value == null) {
            value = booleanPreferenceProvider.getPreference(R.string.preferences_main_map_enable_key, R.bool.preferences_main_map_enable_default_value);
            booleanCache.set(R.string.preferences_main_map_enable_key, value);
        }
        return value;
    }

    public void setMainMapEnabled(boolean enabled) {
        booleanPreferenceProvider.setPreference(R.string.preferences_main_map_enable_key, enabled);
        booleanCache.set(R.string.preferences_main_map_enable_key, enabled);
    }

    public void invalidateMainMapEnabledCache() {
        booleanCache.invalidate(R.string.preferences_main_map_enable_key);
    }

    public boolean isMainMapConfigured() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_main_map_is_configured_key, R.bool.preferences_main_map_is_configured_default_value);
        return value;
    }

    public void setMainMapConfigured(boolean configured) {
        booleanPreferenceProvider.setPreference(R.string.preferences_main_map_is_configured_key, configured);
    }

    public boolean isMainMapForceLightThemeEnabled() {
        boolean value = booleanPreferenceProvider.getPreference(R.string.preferences_main_map_force_light_theme_key, R.bool.preferences_main_map_force_light_theme_default_value);
        return value;
    }
}
