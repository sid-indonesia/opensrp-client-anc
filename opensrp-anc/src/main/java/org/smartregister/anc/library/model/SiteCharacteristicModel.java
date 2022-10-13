package org.smartregister.anc.library.model;

import org.smartregister.anc.library.contract.SiteCharacteristicsContract;
import org.smartregister.anc.library.util.ANCJsonFormUtils;
import org.smartregister.domain.ServerSetting;

import java.util.Map;

public class SiteCharacteristicModel implements SiteCharacteristicsContract.Model {


    @Override
    public Map<String, ServerSetting> processSiteCharacteristics(String jsonString) {
        return ANCJsonFormUtils.processSiteCharacteristics(jsonString);
    }

}
