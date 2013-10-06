package com.hackmit.alerta.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hackmit.alerta.datatypes.PickedContact;
import com.hackmit.alerta.R;

public class PeopleAdapter extends ArrayAdapter<PickedContact> {

	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<PickedContact> contacts = new ArrayList<PickedContact>();

	public PeopleAdapter(Context context, int resource,
                         List<PickedContact> objects, LayoutInflater layoutInflater) {
		super(context, resource, objects);
		mContext = context;
		mInflater = layoutInflater;
		contacts = (ArrayList<PickedContact>) objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LocationViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item, null);
			holder = new LocationViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.name);
			convertView.setTag(holder);

		} else {
			holder = (LocationViewHolder) convertView.getTag();
		}

		holder.name.setText(contacts.get(position).getName());
		return convertView;
	}
}

class LocationViewHolder {
	public TextView name;
}
