package org.smartregister.anc.library.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;

import com.vijay.jsonwizard.utils.AllSharedPreferences;

import java.util.Locale;

/**
 * This class is used to change your application locale and persist this change for the next time
 * that your app is going to be used.
 * <p/>
 * You can also change the locale of your application on the fly by using the setLocale method.
 * <p/>
 * Created by gunhansancar on 07/10/15.
 */
public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context onAttach(Context context) {
        return onAttach(context, Locale.getDefault().getLanguage());
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLanguage(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLanguage(Context context, String language) {
        persist(context, language);

        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return localizedContext(context, locale);
        } else {
            return localizedContextLegacy(context, locale);
        }
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(AllSharedPreferences.LANGUAGE_PREFERENCE_KEY, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(AllSharedPreferences.LANGUAGE_PREFERENCE_KEY, language);
        editor.apply();
    }

    public static Configuration localizedConfiguration(Context context) {
        Resources resources = context.getResources();

        String language = getLanguage(context);
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        return localizedConfiguration(resources.getConfiguration(), locale);
    }

    private static Configuration localizedConfiguration(Configuration configuration, Locale locale) {
        Configuration newConfiguration = new Configuration(configuration);
        newConfiguration.setLayoutDirection(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            newConfiguration.setLocales(localeList);
        } else {
            newConfiguration.setLocale(locale);
        }

        return newConfiguration;
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context localizedContext(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = localizedConfiguration(resources.getConfiguration(), locale);

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context localizedContextLegacy(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = localizedConfiguration(resources.getConfiguration(), locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}