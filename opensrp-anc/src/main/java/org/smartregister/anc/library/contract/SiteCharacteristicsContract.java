package org.smartregister.anc.library.contract;

import org.json.JSONException;
import org.smartregister.domain.ServerSetting;

import java.util.Map;

/**
 * Created by ndegwamartin on 27/08/2018.
 */
public interface SiteCharacteristicsContract {

    interface Presenter {

        SiteCharacteristicsContract.View getSiteCharacteristicsView();

        void launchSiteCharacteristicsForm();

        void launchSiteCharacteristicsFormForEdit();

        void onDestroy(boolean isChangingConfiguration);

        void saveSiteCharacteristics(String jsonString);
    }

    interface View {

        void launchSiteCharacteristicsSettingsForm();

        void showProgressDialog(int messageStringIdentifier);

        void hideProgressDialog();

        void goToLastPage();

        void launchSiteCharacteristicsSettingsFormForEdit(Map<String, String> characteristics);

    }

    interface Model {

        Map<String, ServerSetting> processSiteCharacteristics(String jsonString);
    }

    interface Interactor {

        void updateSiteCharacteristics(Map<String, ServerSetting> updatedCharacteristics) throws JSONException;
        void saveSiteCharacteristics(Map<String, String> siteCharacteristicsSettingsMap) throws JSONException;

    }
}
