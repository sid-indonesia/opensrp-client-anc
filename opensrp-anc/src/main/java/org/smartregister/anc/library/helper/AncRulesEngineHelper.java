package org.smartregister.anc.library.helper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.rules.RuleConstant;
import com.vijay.jsonwizard.rules.RulesEngineHelper;
import com.vijay.jsonwizard.utils.FormUtils;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.InferenceRulesEngine;
import org.jeasy.rules.core.RulesEngineParameters;
import org.jeasy.rules.mvel.MVELRule;
import org.jeasy.rules.mvel.MVELRuleFactory;
import org.jeasy.rules.support.YamlRuleDefinitionReader;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.anc.library.AncLibrary;
import org.smartregister.anc.library.R;
import org.smartregister.anc.library.rule.AlertRule;
import org.smartregister.anc.library.rule.ContactRule;
import org.smartregister.anc.library.util.ANCFormUtils;
import org.smartregister.anc.library.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

public class AncRulesEngineHelper extends RulesEngineHelper {
    private final String RULE_FOLDER_PATH = "rule/";
    private final Context context;
    private final RulesEngine inferentialRulesEngine;
    private final RulesEngine defaultRulesEngine;
    private final Map<String, Rules> ruleMap;
    private JSONObject mJsonObject = new JSONObject();
    private final YamlRuleDefinitionReader yamlRuleDefinitionReader = new YamlRuleDefinitionReader();
    private final MVELRuleFactory mvelRuleFactory = new MVELRuleFactory(yamlRuleDefinitionReader);

    public AncRulesEngineHelper(Context context) {
        this.context = context;
        this.inferentialRulesEngine = new InferenceRulesEngine();
        RulesEngineParameters parameters = new RulesEngineParameters().skipOnFirstAppliedRule(true);
        this.defaultRulesEngine = new DefaultRulesEngine(parameters);
        this.ruleMap = new HashMap<>();

    }

    public void setJsonObject(JSONObject jsonObject) {
        mJsonObject = jsonObject;
    }

    public List<Integer> getContactVisitSchedule(ContactRule contactRule, String rulesFile) {
        Facts facts = new Facts();
        facts.put(ContactRule.RULE_KEY, contactRule);

        Rules rules = getRulesFromAsset(RULE_FOLDER_PATH + rulesFile);
        if (rules == null) {
            return null;
        }

        processInferentialRules(rules, facts);

        Set<Integer> contactList = contactRule.set;
        List<Integer> list = new ArrayList<>(contactList);
        Collections.sort(list);

        return list;
    }

    private Rules getRulesFromAsset(String fileName) {
        try {
            if (!ruleMap.containsKey(fileName)) {
                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
                ruleMap.put(fileName, mvelRuleFactory.createRules(bufferedReader));
            }
            return ruleMap.get(fileName);
        } catch (IOException e) {
            Timber.e(e, "%s getRulesFromAsset()", this.getClass().getCanonicalName());
            return null;
        } catch (Exception e) {
            Timber.e(e, "%s getRulesFromAsset()", this.getClass().getCanonicalName());
            return null;
        }
    }

    protected void processInferentialRules(Rules rules, Facts facts) {
        inferentialRulesEngine.fire(rules, facts);
    }

    public String getButtonAlertStatus(AlertRule alertRule, String rulesFile) {
        Facts facts = new Facts();
        facts.put(AlertRule.RULE_KEY, alertRule);

        Rules rules = getRulesFromAsset(RULE_FOLDER_PATH + rulesFile);
        if (rules == null) {
            return null;
        }

        processDefaultRules(rules, facts);

        return alertRule.buttonStatus;
    }

    protected void processDefaultRules(Rules rules, Facts facts) {
        defaultRulesEngine.fire(rules, facts);
    }

    public boolean getRelevance(Facts relevanceFacts, String rule) {
        relevanceFacts.put("helper", this);
        relevanceFacts.put(RuleConstant.IS_RELEVANT, false);

        Rules rules = new Rules();
        Rule mvelRule = new MVELRule().name(UUID.randomUUID().toString()).when(rule).then("isRelevant = true;");
        rules.register(mvelRule);

        processDefaultRules(rules, relevanceFacts);

        return relevanceFacts.get(RuleConstant.IS_RELEVANT);
    }

    /**
     * Strips the ANC Gestation age to just get me the weeks age
     *
     * @param gestAge {@link String}
     * @return ga {@link String}
     */
    public String stripGaNumber(String gestAge) {
        String ga = "";
        if (!TextUtils.isEmpty(gestAge)) {
            String[] gestAgeSplit = gestAge.split(" ");
            if (gestAgeSplit.length >= 1) {
                int gaWeeks = Integer.parseInt(gestAgeSplit[0]);
                ga = String.valueOf(gaWeeks);
            }
        }
        return ga;
    }

    public static String replaceTranslatedWeeks(String gestAge)
    {
        Context context = AncLibrary.getInstance().getApplicationContext();
        return gestAge.replace("weeks",context.getString(R.string.weeks)).replace("days",context.getString(R.string.days));
    }

    /***
     * Gets value form accordion
     * @param accordion accordion to get the value from
     * @param widget to get its value
     * @return return empty when no value is found otherwise return the value
     */
    public String getValueFromAccordion(String accordion, String widget) throws JSONException {
        String result = "";
        if (mJsonObject.length() > 0) {
            String[] splitWidget = widget.split("_");
            String step = splitWidget[0];
            String key = ANCFormUtils.removeKeyPrefix(widget, step);
            if (mJsonObject.has(step)) {
                JSONObject stepsObject = mJsonObject.getJSONObject(step);
                JSONArray fields = stepsObject.getJSONArray(JsonFormConstants.FIELDS);
                if (fields.length() > 1)
                    for (int i = 0; i < fields.length(); i++) {
                        JSONObject accordionObject = fields.getJSONObject(i);
                        if (accordionObject.getString(JsonFormConstants.KEY).equals(accordion)) {
                            JSONArray value = accordionObject.optJSONArray(JsonFormConstants.VALUE);
                            if (value == null) {
                                return result;
                            }
                            result = ANCFormUtils.obtainValue(key, value);
                            break;
                        }
                    }
            }
        }
        return result;
    }

    public boolean compareDateAgainstContactDate(String firstDate, String contactDate) throws ParseException {
        int comparisonValue = compareTwoDates(firstDate, convertContactDateToTestDate(contactDate));
        boolean isLessOrEqual = false;
        if (comparisonValue == -1 || comparisonValue == 0) {
            isLessOrEqual = true;
        }
        return isLessOrEqual;
    }

    /**
     * Given two dates compare if they are equal
     *
     * @param firstDate  the first date entered
     * @param secondDate the second date entered
     * @return returns {-1} when first date occurs before second date, {0} when both dates are equal
     * {1} when second date is greater than first date and {-2} if any of the dates passed is null
     * or is empty
     */
    public int compareTwoDates(String firstDate, String secondDate) {
        if (!TextUtils.isEmpty(firstDate) && !TextUtils.isEmpty(secondDate)) {
            Calendar dateOne = FormUtils.getDate(firstDate);
            Calendar dateTwo = FormUtils.getDate(secondDate);
            if (dateOne.before(dateTwo)) {
                return -1;
            } else if (dateOne.equals(dateTwo)) {
                return 0;
            } else {
                return 1;
            }
        }
        return -2;
    }

    public String convertContactDateToTestDate(String contactDate) throws ParseException {
        String convertedContactDate = "";
        if (!TextUtils.isEmpty(contactDate)) {
            Date lastContactDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(contactDate);
            if (lastContactDate != null) {
                convertedContactDate =
                        new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(lastContactDate);
            }
        }
        return convertedContactDate;
    }

    /**
     * Gets the dates string and the duration to be added then does a date comparison with the current date
     *
     * @param dateString {@link String}
     * @param duration   {@link String}
     * @return comparison {@link Integer}
     */
    public int compareDateWithDurationsAddedAgainstToday(String dateString, String duration) {
        return compareDateAgainstToday(addDuration(dateString, duration));
    }

    @Override
    public String addDuration(String dateString, String durationString) {
        return ancAddDuration(dateString,durationString);
    }

    public  String ancAddDuration (String date , String duration)
    {
                String givenDate = date;
                int daysToAdd = Integer.parseInt(duration.replace("d",""));
                // Convert the given date string to a DateTime object
                DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
                DateTime dateObj = DateTime.parse(givenDate, formatter);
                // Add days to the given date
                DateTime newDate = dateObj.plusDays(daysToAdd);
                // Format the new date as a string
                String resultDate = newDate.toString(formatter);
                return  resultDate;
    }

    /**
     * Compares date against today's date
     *
     * @param theDate passed as first date to first date
     * @return -1 if date is before today, 0 if equal, 1 if date is greater than today's date and -2
     * otherwise
     */
    public int compareDateAgainstToday(String theDate) {
        return compareTwoDates(theDate, (new LocalDate()).toString(FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN));
    }

    public Boolean validateVisitDate(String entityId, String visitDate) {
        // Valid if visitDate is null or an empty string
        if (visitDate == null || visitDate.equals("")) return true;
        // Validate visit date
        return Utils.isVisitDateValid(entityId, visitDate);
    }


    @Override
    public String getWeeksAndDaysFromDays(Integer days) {
        Context context = AncLibrary.getInstance().getApplicationContext();
        return super.getWeeksAndDaysFromDays(days).replace("weeks",context.getString(R.string.weeks)).replace("days",context.getString(R.string.days));
    }


    public String getTranslatedValues(String stringKey)
    {
        Context context = AncLibrary.getInstance().getApplicationContext();
        Timber.e("MACUM %s %s  ", stringKey, R.string.magnesium_and_calcium_for_cramps);
        switch (stringKey){
            case "calcium_supplementation":
                return context.getString(R.string.calcium_supplementation);
            case "vitamin_a_supplementation":
                return context.getString(R.string.vitamin_a_supplementation);
            case "seven_day_antibiotic":
                return context.getString(R.string.seven_day_antibiotic);
            case "magnesium_and_calcium_for_cramps":
                return context.getString(R.string.magnesium_and_calcium_for_cramps);
            case "pharmacologicals_for_persistent_nausea_and_vomiting":
                return context.getString(R.string.pharmacologicals_for_persistent_nausea_and_vomiting);
            case "antacids_for_persistent_heartburn":
                return context.getString(R.string.antacids_for_persistent_heartburn);
            case "erythromycin_or_azithromycin":
                return context.getString(R.string.erythromycin_or_azithromycin);
            case "penicillin_for_syphilis":
                return context.getString(R.string.penicillin_for_syphilis);
            case "iron_and_folic_acid_for_anaemia":
                return context.getString(R.string.iron_and_folic_acid_for_anaemia);
            case "asprin_for_pre_eclampsia_risk":
                return context.getString(R.string.asprin_for_pre_eclampsia_risk);
            case "prep":
                return context.getString(R.string.prep);
            case "iron_and_folic_supplement":
                return context.getString(R.string.iron_and_folic_supplement);
            case "albendazole_mebendazole_for_deworming":
                return context.getString(R.string.albendazole_mebendazole_for_deworming);

        }
        return "";
    }

    public String addEncounterDate(String entityId, String visitDate)
    {
        Facts facts = AncLibrary.getInstance().getPreviousContactRepository().getPreviousContactFacts(entityId, "1");
        if(facts!= null && facts.get("visit_date") != null )
        {
            return  facts.get("visit_date");
        }
        return  visitDate;
    }

    @Override
    public long getDifferenceDays(String dateString, String dateString2) {

        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
        // Convert the provided date string to a DateTime object
        DateTime firstDateObject = DateTime.parse(dateString, formatter);
        // Get the current date
        DateTime secondDateObject = DateTime.parse(dateString2, formatter);
        // Calculate the date difference in days
        int dateDifference = Days.daysBetween(firstDateObject, secondDateObject).getDays();
        return Math.abs(dateDifference);
    }
}
