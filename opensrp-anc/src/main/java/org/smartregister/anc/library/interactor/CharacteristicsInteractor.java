package org.smartregister.anc.library.interactor;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.anc.library.AncLibrary;
import org.smartregister.anc.library.contract.SiteCharacteristicsContract;
import org.smartregister.anc.library.util.ConstantsUtils;
import org.smartregister.anc.library.util.SiteCharacteristicsFormUtils;
import org.smartregister.domain.ServerSetting;
import org.smartregister.domain.Setting;
import org.smartregister.domain.SyncStatus;
import org.smartregister.repository.AllSettings;
import org.smartregister.util.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

/**
 * Created by ndegwamartin on 13/07/2018.
 */
public class CharacteristicsInteractor implements SiteCharacteristicsContract.Interactor {

    // Update local values of site_characteristic settings
    // using Map of <String, ServerSetting>.
    @Override
    public void updateSiteCharacteristics(Map<String, ServerSetting> updatedCharacteristics) throws JSONException {

        Context context = AncLibrary.getInstance().getApplicationContext();
        Setting characteristics = getAllSettingsRepo().getSetting("site_characteristics");

        JSONArray localSettings;
        JSONObject settingsObject;

        boolean canSaveInitialSettings = getPropertyForInitialSaveAction(context);

        // If there's no characteristics before,
        // create assign characteristics as new Setting object & populate initial settingObject value.
        if (characteristics == null) {

            // Check whether it can save initial settings.
            if (canSaveInitialSettings) {
                try {
                    settingsObject = SiteCharacteristicsFormUtils.structureFormForRequest(context);
                    characteristics = new Setting();
                } catch (Exception e) {
                    Timber.e(e);
                    return;
                }
            }

            // Cannot save initial settings, exit.
            else {
                return;
            }
        }

        // Characteristics is not empty,
        // set settingsObject value from characteristics variable.
        else {
            settingsObject = new JSONObject(characteristics.getValue());
        }

        // Get local settings data
        localSettings = settingsObject.has("settings") ? settingsObject.getJSONArray("settings") : null;

        // Remove duplicates from current local settings
        HashMap<String, JSONObject> uniqueLocalSettings = new HashMap<String, JSONObject>();
        if (localSettings != null) {
            for (int i = 0; i < localSettings.length(); i++) {
                JSONObject item = localSettings.getJSONObject(i);
                String key = String.valueOf(item.get("key"));
                uniqueLocalSettings.put(key, item);
            }
        }

        // Add updates from updatedCharacteristics
        for (Map.Entry<String, ServerSetting> item : updatedCharacteristics.entrySet()) {

            // Updated data

            String key = item.getKey();
            ServerSetting itemSettings = item.getValue();

            Boolean value = itemSettings.getValue();
            String label = itemSettings.getLabel();
            String description = itemSettings.getDescription();

            // Update local settings

            JSONObject settingsItem = uniqueLocalSettings.get(key);
            if (settingsItem == null) settingsItem = new JSONObject();

            settingsItem.put("label", label);
            settingsItem.put("description", description);
            settingsItem.put("value", value);

        }

        // Create a JSONArray for updated settings
        JSONArray newSettings = new JSONArray();
        for (Map.Entry<String,JSONObject> item : uniqueLocalSettings.entrySet()) {
            newSettings.put(item.getValue());
        }

        settingsObject.put(AllConstants.SETTINGS, newSettings);

        characteristics.setValue(settingsObject.toString());
        characteristics.setKey(ConstantsUtils.PrefKeyUtils.SITE_CHARACTERISTICS);
        characteristics.setSyncStatus(SyncStatus.PENDING.name());

        getAllSettingsRepo().putSetting(characteristics);
        AncLibrary.getInstance().populateGlobalSettings();

    }

    @Override
    public void saveSiteCharacteristics(Map<String, String> siteCharacteristicsSettingsMap) throws JSONException {

        JSONArray localSettings;
        JSONObject settingObject;
        Context context = AncLibrary.getInstance().getApplicationContext();
        Setting characteristic = getAllSettingsRepo().getSetting(ConstantsUtils.PrefKeyUtils.SITE_CHARACTERISTICS);
        boolean canSaveInitialSetting = getPropertyForInitialSaveAction(context);
        if (characteristic == null) {

            if (canSaveInitialSetting) {
                try {
                    settingObject = SiteCharacteristicsFormUtils.structureFormForRequest(context);
                    characteristic = new Setting();
                } catch (Exception e) {
                    Timber.e(e);
                    return;
                }
            } else {
                return;
            }
        } else {
            settingObject = new JSONObject(characteristic.getValue());
        }
        localSettings = settingObject.has(AllConstants.SETTINGS) ? settingObject.getJSONArray(AllConstants.SETTINGS) : null;

        HashMap<String, JSONObject> cleanSettings = new HashMap<String, JSONObject>();

        // Remove duplicates from current localSettings
        if (localSettings != null) {
            for (int i = 0; i < localSettings.length(); i++) {
                JSONObject localSetting = localSettings.getJSONObject(i);

                String key = String.valueOf(localSetting.get("key"));
                Boolean value = localSetting.get("value").equals("true");
                cleanSettings.put(key, localSetting);

                Log.v("ASDE", String.valueOf(localSetting));
            }
        }


        // Add updates from new siteCharacteristicsSettingsMap
        for (Map.Entry<String,String> charItem : siteCharacteristicsSettingsMap.entrySet()) {
            String key = charItem.getKey();
            Boolean value = charItem.getValue().equals("1");
            JSONObject setting = cleanSettings.get(key);
            setting.put("value", value);
        }

        // Create a JSONArray for updated settings
        JSONArray cleanSettingsArray = new JSONArray();
        for (Map.Entry<String,JSONObject> item : cleanSettings.entrySet()) {
            cleanSettingsArray.put(item.getValue());
        }

        settingObject.put(AllConstants.SETTINGS, cleanSettingsArray);

        characteristic.setValue(settingObject.toString());
        characteristic.setKey(ConstantsUtils.PrefKeyUtils.SITE_CHARACTERISTICS);
        characteristic.setSyncStatus(SyncStatus.PENDING.name());

        getAllSettingsRepo().putSetting(characteristic);
        AncLibrary.getInstance().populateGlobalSettings();
    }

    protected AllSettings getAllSettingsRepo() {
        return AncLibrary.getInstance().getContext().allSettings();
    }

    private Boolean getPropertyForInitialSaveAction(Context context) {
        String value = Utils.getProperties(context).getProperty(ConstantsUtils.Properties.CAN_SAVE_SITE_INITIAL_SETTING, "false");
        return Boolean.valueOf(value);
    }
}
