package org.smartregister.anc.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.smartregister.anc.R;
import org.smartregister.anc.activity.ContactSummaryActivity;
import org.smartregister.anc.activity.PopulationCharacteristicsActivity;
import org.smartregister.anc.activity.SiteCharacteristicsActivity;
import org.smartregister.anc.application.AncApplication;
import org.smartregister.anc.contract.MeContract;
import org.smartregister.anc.helper.LocationHelper;
import org.smartregister.anc.presenter.MePresenter;
import org.smartregister.anc.util.Constants;
import org.smartregister.anc.util.Utils;
import org.smartregister.anc.view.LocationPickerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MeFragment extends Fragment implements MeContract.View {
    private MeFragmentActionHandler meFragmentActionHandler = new MeFragmentActionHandler();
    private MeContract.Presenter presenter;

    private TextView initials;
    private TextView userName;

    private RelativeLayout me_location_section;
    private RelativeLayout me_pop_characteristics_section;
    private RelativeLayout site_characteristics_section;
    private RelativeLayout setting_section;
    private RelativeLayout logout_section;
    private LocationPickerView facilitySelection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializePresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpViews(view);
        setClickListeners();
        presenter.updateInitials();
        presenter.updateName();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateLocationText();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLocationText();
    }

    private void setUpViews(View view) {
        initials = view.findViewById(R.id.initials);
        userName = view.findViewById(R.id.user_name);
        me_location_section = view.findViewById(R.id.me_location_section);
        me_pop_characteristics_section = view.findViewById(R.id.me_pop_characteristics_section);
        site_characteristics_section = view.findViewById(R.id.site_characteristics_section);
        setting_section = view.findViewById(R.id.setting_section);
        logout_section = view.findViewById(R.id.logout_section);
        facilitySelection = view.findViewById(R.id.facility_selection);
        if (me_location_section != null) {
            facilitySelection.init();
        }
        TextView application_version = view.findViewById(R.id.application_version);
        if (application_version != null) {
            try {
                application_version.setText(String.format(getString(R.string.app_version), Utils.getVersion(getActivity()), presenter.getBuildDate()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        TextView synced_data = view.findViewById(R.id.synced_data);
        if (synced_data != null) {
            //Todo Update this to the values after the sync functionality is added.
            synced_data.setText(String.format(getString(R.string.data_synced), new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new
                    Date()), new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date())));
        }
    }

    private void setClickListeners() {
        me_location_section.setOnClickListener(meFragmentActionHandler);
        me_pop_characteristics_section.setOnClickListener(meFragmentActionHandler);
        site_characteristics_section.setOnClickListener(meFragmentActionHandler);
        setting_section.setOnClickListener(meFragmentActionHandler);
        logout_section.setOnClickListener(meFragmentActionHandler);
    }

    private void initializePresenter() {
        presenter = new MePresenter(this);
    }

    @Override
    public void updateInitialsText(String userInitials) {
        if (initials != null) {
            initials.setText(userInitials);
        }
    }

    @Override
    public void updateNameText(String name) {
        if (userName != null) {
            userName.setText(name);
        }
    }

    protected void updateLocationText() {
        if (facilitySelection != null) {
            facilitySelection.setText(LocationHelper.getInstance().getOpenMrsReadableName(facilitySelection.getSelectedItem()));
            String locationId = LocationHelper.getInstance().getOpenMrsLocationId(facilitySelection.getSelectedItem());
            AncApplication.getInstance().getContext().allSharedPreferences().savePreference(Constants.CURRENT_LOCATION_ID, locationId);
        }
    }

    /**
     * Handles the Click actions on any of the section in the page.
     */
    private class MeFragmentActionHandler implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.logout_section:
                    AncApplication.getInstance().logoutCurrentUser();
                    break;
                case R.id.setting_section:
                    //ToDO Add the functionality for the setting page after that is decided.
                    break;
                case R.id.site_characteristics_section:
                    getContext().startActivity(new Intent(getContext(), SiteCharacteristicsActivity.class));
                    break;
                case R.id.me_pop_characteristics_section:
                    getContext().startActivity(new Intent(getContext(), PopulationCharacteristicsActivity.class));
                    break;
                case R.id.me_location_section:
                    if (facilitySelection != null) {
                        LocationPickerView locationPickerView = new LocationPickerView(getContext());
                        locationPickerView.init();
                        locationPickerView.onClick(facilitySelection);
                    }
                    break;
                default:
                    break;
            }
        }

    }
}