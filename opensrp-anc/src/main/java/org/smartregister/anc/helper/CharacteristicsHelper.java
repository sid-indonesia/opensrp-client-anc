package org.smartregister.anc.helper;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.anc.application.AncApplication;
import org.smartregister.anc.domain.Characteristic;
import org.smartregister.anc.util.AncPreferenceHelper;
import org.smartregister.domain.Setting;
import org.smartregister.domain.SyncStatus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by ndegwamartin on 13/08/2018.
 */
public class CharacteristicsHelper {

    private static final String TAG = CharacteristicsHelper.class.getCanonicalName();

    public static List<Characteristic> characteristics;

    private static final Type CHARACTERISTIC_TYPE = new TypeToken<List<Characteristic>>() {
    }.getType();

    public CharacteristicsHelper(String key) {
        initializeCharacteristics(key);

    }

    public List<Characteristic> getCharacteristics() {
        return characteristics;
    }

    public static List<Characteristic> fetchCharacteristicsByTypeKey(String typeKey) {
        try {

            Gson gson = new Gson();

            Setting characteristic = AncApplication.getInstance().getContext().allSettings().getSetting(typeKey);

            String jsonstring = characteristic.getValue();

            return gson.fromJson(jsonstring, CHARACTERISTIC_TYPE); // contains the whole reviews list

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return new ArrayList<>();
        }
    }

    public static JSONArray saveSetting(JSONArray serverSettings) throws JSONException {
        for (int i = 0; i < serverSettings.length(); i++) {

            JSONObject jsonObject = serverSettings.getJSONObject(i);
            Setting characteristic = new Setting();
            characteristic.setKey(jsonObject.getString("identifier"));
            characteristic.setValue(jsonObject.getString("settings"));
            characteristic.setSyncStatus(SyncStatus.SYNCED.name());

            AncApplication.getInstance().getContext().allSettings().put(SyncSettingsServiceHelper.SETTINGS_LAST_SYNC_FROM_SERVER_TIMESTAMP, characteristic.getVersion());
            AncApplication.getInstance().getContext().allSettings().putSetting(characteristic);

        }

        return serverSettings;
    }


    public static void updateLastSettingServerSyncTimetamp() {

        AncPreferenceHelper preferenceHelper = AncPreferenceHelper.getInstance(AncApplication.getInstance().getApplicationContext());
        preferenceHelper.updateLastSettingsSyncFromServerTimeStamp(Calendar.getInstance().getTimeInMillis());
    }

    private static void initializeCharacteristics(String key) {

        characteristics = CharacteristicsHelper.fetchCharacteristicsByTypeKey(key);
    }
}