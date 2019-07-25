package com.tokyonth.installer.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import com.tokyonth.installer.BaseApplication;
import com.tokyonth.installer.R;
import com.tokyonth.installer.info.DonateToMe;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        findPreference("donate").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DonateToMe.show(BaseApplication.getContext());
                return false;
            }
        });
    }
}
