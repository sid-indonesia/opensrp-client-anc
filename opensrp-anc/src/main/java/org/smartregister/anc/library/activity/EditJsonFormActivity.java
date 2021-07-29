package org.smartregister.anc.library.activity;

import android.content.Context;
import android.os.Bundle;

import com.vijay.jsonwizard.activities.FormConfigurationJsonFormActivity;

import org.smartregister.anc.library.R;
import org.smartregister.anc.library.util.LocaleHelper;

public class EditJsonFormActivity extends FormConfigurationJsonFormActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setConfirmCloseMessage(getString(R.string.any_changes_you_make));
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }
}
