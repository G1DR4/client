package org.msf.records.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.msf.records.R;
import org.msf.records.cache.PatientOpenHelper;
import org.msf.records.model.Patient;
import org.msf.records.model.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by akalachman on 11/20/14.
 */
public class ExpandablePatientListAdapter extends BaseExpandableListAdapter {

    private Activity context;
    private Map<String, List<Patient>> patientsByZone;
    private List<String> zones;
    private final String UNKNOWN_ZONE = "Unknown Zone";

    private PatientOpenHelper patientDb;

    public ExpandablePatientListAdapter(Activity context) {
        this.context = context;
        patientsByZone = new HashMap<String, List<Patient>>();
        zones = new ArrayList<String>();
        patientDb = new PatientOpenHelper(context);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return patientsByZone.get(zones.get(groupPosition)).get(childPosition);
    }

    public Patient getPatient(int groupPosition, int childPosition) {
        return (Patient)getChild(groupPosition, childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final Patient patient = (Patient) getChild(groupPosition, childPosition);

        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = LayoutInflater.from(context).inflate(R.layout.listview_cell_search_results, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        holder.mPatientName.setText(patient.given_name + " " + patient.family_name);
        holder.mPatientId.setText(patient.id);

        if (patient.age.type != null && patient.age.type.equals("months")) {
            holder.mPatientAge.setText("<1");
        }

        if (patient.age.type != null && patient.age.type.equals("years")) {
            holder.mPatientAge.setText("" + patient.age.years);
        }

        if (patient.age.type == null) {
            holder.mPatientAge.setText("99");
            holder.mPatientAge.setTextColor(context.getResources().getColor(R.color.transparent));
        }

        if (patient.gender != null && patient.gender.equals("M")) {
            holder.mPatientGender.setImageDrawable(context.getResources().getDrawable(R.drawable.gender_man));
        }

        if (patient.gender != null && patient.gender.equals("F")) {
            if (patient.pregnant != null && patient.pregnant) {
                holder.mPatientGender.setImageDrawable(context.getResources().getDrawable(R.drawable.gender_pregnant));
            } else {
                holder.mPatientGender.setImageDrawable(context.getResources().getDrawable(R.drawable.gender_woman));
            }
        }

        if (patient.gender == null) {
            holder.mPatientGender.setVisibility(View.GONE);
        }

        if (patient.status == null) {
            holder.mPatientListStatusColorIndicator.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        }

        if (patient.status != null && Status.getStatus(patient.status) != null) {
            holder.mPatientListStatusColorIndicator.setBackgroundColor(context.getResources().getColor(Status.getStatus(patient.status).colorId));
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition >= zones.size()) {
            return 0;
        }

        return patientsByZone.get(zones.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return zones.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return zones.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String zone = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_zone_header, null);
        }
        TextView item = (TextView) convertView.findViewById(R.id.patient_list_zone_tv);
        item.setText(zone);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void clear() {
        patientsByZone.clear();
        zones.clear();
    }

    public void addAll(Collection<? extends Patient> patients) {
        // Inject entries from local cache.
        for (Patient patient : patients) {
            add(patient);
        }
    }

    public void add(final Patient patient) {
        // If the patient exists in local cache, inject it.
        // TODO(akalachman): Remove after demo?
        Patient patientToAdd = patientDb.getPatient(patient.uuid);
        if (patientToAdd == null) {
            patientToAdd = patient;
        }

        String zone = UNKNOWN_ZONE;
        if (patientToAdd.assigned_location != null &&
                patientToAdd.assigned_location.zone != null) {
            zone = patientToAdd.assigned_location.zone;
        }
        if (!patientsByZone.containsKey(zone)) {
            patientsByZone.put(zone, new ArrayList<Patient>());
            zones.add(zone);
        }
        patientsByZone.get(zone).add(patientToAdd);
    }

    // TODO(akalachman): Use this for grouping.
    private class Tent {
        public String name;
        public List<Patient> patients = new ArrayList<Patient>();
    }

    static class ViewHolder {
        @InjectView(R.id.listview_cell_search_results_color_indicator) ImageView mPatientListStatusColorIndicator;
        @InjectView(R.id.listview_cell_search_results_name) TextView mPatientName;
        @InjectView(R.id.listview_cell_search_results_id) TextView mPatientId;
        @InjectView(R.id.listview_cell_search_results_gender) ImageView mPatientGender;
        @InjectView(R.id.listview_cell_search_results_age) TextView mPatientAge;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}