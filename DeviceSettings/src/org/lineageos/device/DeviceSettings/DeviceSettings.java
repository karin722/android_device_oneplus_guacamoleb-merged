/*
* Copyright (C) 2016 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.lineageos.device.DeviceSettings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.util.Log;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.TwoStatePreference;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_VIBSTRENGTH = "vib_strength";
    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_HBM_AUTOBRIGHTNESS_SWITCH = "hbm_autobrightness";
    public static final String KEY_HBM_AUTOBRIGHTNESS_THRESHOLD = "hbm_autobrightness_threshould";
    public static final String KEY_DC_SWITCH = "dc";
    public static final String KEY_DCI_SWITCH = "dci";
    public static final String KEY_NIGHT_SWITCH = "night";
    public static final String KEY_HAPTIC_FEEDBACK_SWITCH = "haptic_feedback";
    private static final String KEY_ENABLE_DOLBY_ATMOS = "enable_dolby_atmos";

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";

    private static TwoStatePreference mHBMModeSwitch;
    private static TwoStatePreference mHBMAutobrightnessSwitch;
    private static TwoStatePreference mDCModeSwitch;
    private static TwoStatePreference mHapticFeedbackSwitch;
    private static TwoStatePreference mEnableDolbyAtmos;
    private ListPreference mTopKeyPref;
    private ListPreference mMiddleKeyPref;
    private ListPreference mBottomKeyPref;
    private VibratorStrengthPreference mVibratorStrength;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.main);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        mVibratorStrength = (VibratorStrengthPreference) findPreference(KEY_VIBSTRENGTH);
        if (mVibratorStrength == null || !VibratorStrengthPreference.isSupported()) {
            getPreferenceScreen().removePreference((Preference) findPreference("vibrator"));
        }

        mTopKeyPref = (ListPreference) findPreference(Constants.NOTIF_SLIDER_TOP_KEY);
        mTopKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_TOP_KEY));
        mTopKeyPref.setOnPreferenceChangeListener(this);
        mMiddleKeyPref = (ListPreference) findPreference(Constants.NOTIF_SLIDER_MIDDLE_KEY);
        mMiddleKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_MIDDLE_KEY));
        mMiddleKeyPref.setOnPreferenceChangeListener(this);
        mBottomKeyPref = (ListPreference) findPreference(Constants.NOTIF_SLIDER_BOTTOM_KEY);
        mBottomKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_BOTTOM_KEY));
        mBottomKeyPref.setOnPreferenceChangeListener(this);

        mHBMModeSwitch = (TwoStatePreference) findPreference(KEY_HBM_SWITCH);
        mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported());
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled(this.getContext()));
        mHBMModeSwitch.setOnPreferenceChangeListener(new HBMModeSwitch());

        mHBMAutobrightnessSwitch = (TwoStatePreference) findPreference(KEY_HBM_AUTOBRIGHTNESS_SWITCH);
        mHBMAutobrightnessSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_HBM_AUTOBRIGHTNESS_SWITCH, false));
        mHBMAutobrightnessSwitch.setOnPreferenceChangeListener(this);

        mDCModeSwitch = (TwoStatePreference) findPreference(KEY_DC_SWITCH);
        mDCModeSwitch.setEnabled(DCModeSwitch.isSupported());
        mDCModeSwitch.setChecked(DCModeSwitch.isCurrentlyEnabled(this.getContext()));
        mDCModeSwitch.setOnPreferenceChangeListener(new DCModeSwitch());

        mHapticFeedbackSwitch = (TwoStatePreference) findPreference(KEY_HAPTIC_FEEDBACK_SWITCH);
        mHapticFeedbackSwitch.setEnabled(HapticFeedbackSwitch.isSupported());
        mHapticFeedbackSwitch.setChecked(HapticFeedbackSwitch.isCurrentlyEnabled(this.getContext()));
        mHapticFeedbackSwitch.setOnPreferenceChangeListener(new HapticFeedbackSwitch());

        mEnableDolbyAtmos = (TwoStatePreference) findPreference(KEY_ENABLE_DOLBY_ATMOS);
        mEnableDolbyAtmos.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mTopKeyPref) {
            Constants.setPreferenceInt(getContext(), preference.getKey(), Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mMiddleKeyPref) {
            Constants.setPreferenceInt(getContext(), preference.getKey(), Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mBottomKeyPref) {
            Constants.setPreferenceInt(getContext(), preference.getKey(), Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mHBMAutobrightnessSwitch) {
            Boolean enabled = (Boolean) newValue;
            SharedPreferences.Editor prefChange = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefChange.putBoolean(KEY_HBM_AUTOBRIGHTNESS_SWITCH, enabled).commit();
            Utils.enableService(getContext());
            return true;
        } else if (preference == mEnableDolbyAtmos) {
            boolean enabled = (Boolean) newValue;
            Intent daxService = new Intent();
            ComponentName name = new ComponentName("com.dolby.daxservice", "com.dolby.daxservice.DaxService");
            daxService.setComponent(name);
            if (enabled) {
                // enable service component and start service
                this.getContext().getPackageManager().setComponentEnabledSetting(name,
                        PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, 0);
                this.getContext().startService(daxService);
            } else {
                // disable service component and stop service
                this.getContext().stopService(daxService);
                this.getContext().getPackageManager().setComponentEnabledSetting(name,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            }
            return true;
        }
        return false;
    }

    public static boolean isHBMAutobrightnessEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_HBM_AUTOBRIGHTNESS_SWITCH, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        // Respond to the action bar's Up/Home button
        case android.R.id.home:
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
