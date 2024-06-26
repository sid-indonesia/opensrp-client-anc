package org.smartregister.anc.application;

import org.smartregister.SyncConfiguration;
import org.smartregister.SyncFilter;
import org.smartregister.anc.BuildConfig;
import org.smartregister.anc.activity.LoginActivity;
import org.smartregister.anc.library.AncLibrary;
import org.smartregister.anc.library.util.ANCJsonFormUtils;
import org.smartregister.anc.library.util.ConstantsUtils;
import org.smartregister.domain.jsonmapping.util.LocationTree;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.intent.BaseSyncIntentService;
import org.smartregister.view.activity.BaseLoginActivity;

import java.util.LinkedHashSet;
import java.util.List;

import timber.log.Timber;

/**
 * Created by samuelgithengi on 10/19/18.
 */
public class AncSyncConfiguration extends SyncConfiguration {
    private static final String POPULATION_CHARACTERISTICS = "population_characteristics";

    @Override
    public int getSyncMaxRetries() {
        return BuildConfig.MAX_SYNC_RETRIES;
    }

    @Override
    public SyncFilter getSyncFilterParam() {
        if(BuildConfig.SYNC_TYPE.equals(SyncFilter.LOCATION_ID.value()))
            return SyncFilter.LOCATION_ID;
        else if(BuildConfig.SYNC_TYPE.equals(SyncFilter.TEAM_ID.value()))
            return SyncFilter.TEAM_ID;
        else
            return SyncFilter.PROVIDER;
    }

    @Override
    public SyncFilter getSettingsSyncFilterParam() {
        return super.getSettingsSyncFilterParam();
    }

    @Override
    public boolean resolveSettings() {
        return BuildConfig.RESOLVE_SETTINGS;
    }

    @Override
    public boolean hasExtraSettingsSync() {
        return BuildConfig.HAS_EXTRA_SETTINGS_SYNC_FILTER;
    }

    @Override
    public String getExtraStringSettingsParameters() {
        AllSharedPreferences sharedPreferences = AncLibrary.getInstance().getContext().userService().getAllSharedPreferences();
        String providerId = sharedPreferences.fetchRegisteredANM();
        BaseSyncIntentService.RequestParamsBuilder builder = new BaseSyncIntentService.RequestParamsBuilder()
                .addParam(ConstantsUtils.SettingsSyncParamsUtils.LOCATION_ID, sharedPreferences.fetchDefaultLocalityId(providerId))
                .addParam(ConstantsUtils.SettingsSyncParamsUtils.IDENTIFIER, POPULATION_CHARACTERISTICS);
        return builder.toString();
    }

    @Override
    public String getSyncFilterValue() {
        AllSharedPreferences sharedPreferences =
                AncLibrary.getInstance().getContext().userService().getAllSharedPreferences();

        if(BuildConfig.SYNC_TYPE.equals(SyncFilter.LOCATION_ID.value()))
            return getLocationsString(sharedPreferences.fetchDefaultLocalityId(sharedPreferences.fetchRegisteredANM()));
        else if(BuildConfig.SYNC_TYPE.equals(SyncFilter.TEAM_ID.value()))
            return sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM());
        else
            return sharedPreferences.fetchRegisteredANM();

    }

    protected String getLocationsString(String parentLocationId) {
        StringBuilder syncLocations = new StringBuilder(parentLocationId);
        try {
            String anmLocation = AncLibrary.getInstance().getContext().allSettings().fetchANMLocation();
            if (anmLocation != null) {
                LocationTree locationTree = ANCJsonFormUtils.gson.fromJson(anmLocation, LocationTree.class);
                if (locationTree != null) {
                    LinkedHashSet<String> children = locationTree.getChildParent().get(parentLocationId);
                    if (children != null) {
                        for (String child : children) {
                            syncLocations.append(",").append(child);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return syncLocations.toString();
    }

    @Override
    public int getUniqueIdSource() {
        return BuildConfig.OPENMRS_UNIQUE_ID_SOURCE;
    }

    @Override
    public int getUniqueIdBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_BATCH_SIZE;
    }

    @Override
    public int getUniqueIdInitialBatchSize() {
        return BuildConfig.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
    }

    @Override
    public boolean isSyncSettings() {
        return BuildConfig.IS_SYNC_SETTINGS;
    }

    @Override
    public SyncFilter getEncryptionParam() {
        if(BuildConfig.SYNC_TYPE.equals(SyncFilter.LOCATION_ID.value()))
            return SyncFilter.LOCATION;
        else if(BuildConfig.SYNC_TYPE.equals(SyncFilter.TEAM_ID.value()))
            return SyncFilter.TEAM;
        else
            return SyncFilter.PROVIDER;

    }

    @Override
    public boolean updateClientDetailsTable() {
        return true;
    }

    @Override
    public List<String> getSynchronizedLocationTags() {
        return null;
    }

    @Override
    public String getTopAllowedLocationLevel() {
        return null;
    }

    @Override
    public String getOauthClientId() {
        return BuildConfig.OAUTH_CLIENT_ID;
    }

    @Override
    public String getOauthClientSecret() {
        return BuildConfig.OAUTH_CLIENT_SECRET;
    }

    @Override
    public Class<? extends BaseLoginActivity> getAuthenticationActivity() {
        return LoginActivity.class;
    }
}
