package org.smartregister.anc.library.task;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.smartregister.anc.library.contract.BaseCharacteristicsContract;
import org.smartregister.anc.library.contract.PopulationCharacteristicsContract;
import org.smartregister.anc.library.util.ConstantsUtils;
import org.smartregister.domain.ServerSetting;
import org.smartregister.sync.helper.ServerSettingsHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ndegwamartin on 28/08/2018.
 */
public class FetchSiteCharacteristicsTask extends AsyncTask<Void, Void, List<ServerSetting>> {

    private BaseCharacteristicsContract.BasePresenter presenter;

    public FetchSiteCharacteristicsTask(PopulationCharacteristicsContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected List<ServerSetting> doInBackground(final Void... params) {
        ServerSettingsHelper helper = new ServerSettingsHelper(ConstantsUtils.PrefKeyUtils.SITE_CHARACTERISTICS);

        // Get site characteristics from server
        List<ServerSetting> serverSiteCharacteristics = helper.getServerSettings();

        // Remove duplicate items
        List<ServerSetting> localSiteCharacteristics = new ArrayList<ServerSetting>();
        HashMap<String, ServerSetting> cleanSettings = new HashMap<String, ServerSetting>();
        for (ServerSetting setting : serverSiteCharacteristics) {
            String key = setting.getKey();
            cleanSettings.put(key, setting);
        }

        // Add settings to localSiteCharacteristics
        for (Map.Entry<String, ServerSetting> item : cleanSettings.entrySet()) {
            localSiteCharacteristics.add(item.getValue());
        }

        return localSiteCharacteristics;
    }

    @Override
    protected void onPostExecute(final List<ServerSetting> result) {
        presenter.renderView(result);
    }
}
