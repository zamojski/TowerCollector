/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package info.zamojski.soft.towercollector.preferences;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import info.zamojski.soft.towercollector.BuildConfig;
import info.zamojski.soft.towercollector.CollectorService;
import info.zamojski.soft.towercollector.MyApplication;
import info.zamojski.soft.towercollector.R;
import info.zamojski.soft.towercollector.dao.MeasurementsDatabase;
import info.zamojski.soft.towercollector.dev.DatabaseOperations;
import info.zamojski.soft.towercollector.dev.PreferencesOperations;
import info.zamojski.soft.towercollector.utils.StorageUtils;
import timber.log.Timber;

public class AdvancedPreferenceFragment extends DialogEnabledPreferenceFragment implements OnSharedPreferenceChangeListener {

    private ListPreference collectorApiVersionPreference;
    private ListPreference fileLoggingLevelPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_advanced);

        collectorApiVersionPreference = findPreference(getString(R.string.preferences_collector_api_version_key));
        fileLoggingLevelPreference = findPreference(getString(R.string.preferences_file_logging_level_key));

        setupApiVersionDialog();
        setupErrorReportingAvailability();
        setupDatabaseImport();
        setupDatabaseExport();
        setupPreferencesImport();
        setupPreferencesExport();
    }

    private void setupDatabaseImport() {
        showConfirmationDialog(R.string.preferences_import_database_key, R.string.unsafe_operation_warning_title,
                R.string.unsafe_operation_warning_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importDatabase();
                    }
                });
    }

    private void setupDatabaseExport() {
        int measurementsToUploadCount = MeasurementsDatabase.getInstance(MyApplication.getApplication()).getAllLocationsCount(true);
        if (measurementsToUploadCount == 0) {
            showConfirmationDialog(R.string.preferences_export_database_key, R.string.preferences_export_database_overwrite_warning_title,
                    R.string.preferences_export_database_overwrite_warning_message, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            exportDatabase();
                        }
                    });
        } else {
            setupOnClick(R.string.preferences_export_database_key, new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    exportDatabase();
                    return true;
                }
            });
        }
    }

    private void setupPreferencesImport() {
        showConfirmationDialog(R.string.preferences_import_preferences_key, R.string.unsafe_operation_warning_title,
                R.string.unsafe_operation_warning_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importPreferences();
                    }
                });
    }

    private void setupPreferencesExport() {
        setupOnClick(R.string.preferences_export_preferences_key, new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                exportPreferences();
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        // set summaries
        setupListPreferenceSummary(collectorApiVersionPreference, R.string.preferences_collector_api_version_summary);
        setupListPreferenceSummary(fileLoggingLevelPreference, R.string.preferences_file_logging_level_summary);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.preferences_collector_api_version_key))) {
            String collectorApiVersionValue = collectorApiVersionPreference.getValue();
            CharSequence collectorApiVersionLabel = collectorApiVersionPreference.getEntry();
            Timber.d("onSharedPreferenceChanged(): User set api version = \"%s\"", collectorApiVersionValue);
            collectorApiVersionPreference.setSummary(formatValueString(R.string.preferences_collector_api_version_summary, collectorApiVersionLabel));
            if (MyApplication.isBackgroundTaskRunning(CollectorService.class)) {
                Toast.makeText(getActivity(), R.string.preferences_restart_collector, Toast.LENGTH_SHORT).show();
            }
        } else if (key.equals(getString(R.string.preferences_file_logging_level_key))) {
            String fileLoggingLevelValue = fileLoggingLevelPreference.getValue();
            CharSequence fileLoggingLevelLabel = fileLoggingLevelPreference.getEntry();
            Timber.d("onSharedPreferenceChanged(): User set file logging level = \"%s\"", fileLoggingLevelValue);
            fileLoggingLevelPreference.setSummary(formatValueString(R.string.preferences_file_logging_level_summary, fileLoggingLevelLabel));
            requestLoggerChange();
        }
    }

    private void setupApiVersionDialog() {
        setupDialog(R.string.preferences_about_collector_api_version_key, R.string.info_about_collector_api_version_title, R.raw.info_about_collector_api_version_content);
    }

    private void setupErrorReportingAvailability() {
        boolean available = BuildConfig.ACRA_SETTINGS_AVAILABLE;
        if (!available) {
            PreferenceCategory settingsCategoryPreference = findPreference(getString(R.string.preferences_advanced_category_settings_key));
            SwitchPreferenceCompat errorReportingSilentPreference = findPreference(getString(R.string.preferences_error_reporting_silent_key));
            settingsCategoryPreference.removePreference(errorReportingSilentPreference);
        }
    }

    private void requestLoggerChange() {
        Timber.d("requestLoggerChange(): Reinitializing logger");
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        if (StorageUtils.canWriteStorageUri(storageUri)) {
            MyApplication.getApplication().initLogger();
        } else {
            if (!MyApplication.getPreferencesProvider().getFileLoggingLevel().equals(getString(R.string.preferences_file_logging_level_default_value))) {
                StorageUtils.requestStorageUri(getActivity());
            }
        }
    }

    private void importDatabase() {
        Timber.d("importDatabase(): Importing database");
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        if (StorageUtils.canReadStorageUri(storageUri)) {
            DatabaseOperations.importDatabase(MyApplication.getApplication());
        } else {
            StorageUtils.requestStorageUri(getActivity());
        }
    }

    private void exportDatabase() {
        Timber.d("exportDatabase(): Exporting database");
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        if (StorageUtils.canWriteStorageUri(storageUri)) {
            DatabaseOperations.exportDatabase(MyApplication.getApplication());
        } else {
            StorageUtils.requestStorageUri(getActivity());
        }
    }

    private void importPreferences() {
        Timber.d("importPreferences(): Importing preferences");
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        if (StorageUtils.canReadStorageUri(storageUri)) {
            PreferencesOperations.importPreferences(MyApplication.getApplication());
        } else {
            StorageUtils.requestStorageUri(getActivity());
        }
    }

    private void exportPreferences() {
        Timber.d("exportPreferences(): Exporting preferences");
        Uri storageUri = MyApplication.getPreferencesProvider().getStorageUri();
        if (StorageUtils.canWriteStorageUri(storageUri)) {
            PreferencesOperations.exportPreferences(MyApplication.getApplication());
        } else {
            StorageUtils.requestStorageUri(getActivity());
        }
    }
}
