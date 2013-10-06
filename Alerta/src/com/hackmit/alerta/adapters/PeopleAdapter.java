package com.hackmit.alerta.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hackmit.alerta.R;
import com.hackmit.alerta.datatypes.PickedContact;
import com.hackmit.alerta.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class PeopleAdapter extends ArrayAdapter<PickedContact> {

    private Context mContext;
    private PeopleAdapter adapter;
    private LayoutInflater mInflater;
    private ArrayList<PickedContact> contacts = new ArrayList<PickedContact>();

    public PeopleAdapter(Context context, int resource,
                         List<PickedContact> objects, LayoutInflater layoutInflater) {
        super(context, resource, objects);
        mContext = context;
        mInflater = layoutInflater;
        contacts = (ArrayList<PickedContact>) objects;
        adapter = this;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LocationViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new LocationViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.image = (ImageView) convertView.findViewById(R.id.imageView);
            holder.minus = (ImageView) convertView.findViewById(R.id.minusButton);
            convertView.setTag(holder);
        } else {
            holder = (LocationViewHolder) convertView.getTag();
        }

        holder.name.setText(contacts.get(position).getName());
        holder.minus.setTag(position);
        holder.name.setTag(position);
        if (!contacts.get(position).getPhoto().equals("")) {
            //try {
            // byte[] photoBlob = contacts.get(position).getPhoto().getBytes("UTF-8");
            //holder.image.setImageBitmap(BitmapFactory.decodeByteArray(photoBlob, 0, photoBlob.length));

            //            } catch (UnsupportedEncodingException e) {
            //
            // }

        }
        holder.minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (Integer) view.getTag();
                if (tag != (contacts.size())) {
                    PreferenceUtils.removeContact(contacts.get(tag), mContext);
                    //contacts.remove(tag);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tag = (Integer) view.getTag();
                if (tag != (contacts.size())) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

                    final PickedContact contact = getItem(tag);

                    alert.setTitle("Set an emergency message");
                    alert.setMessage("Message");


                    // Set an EditText view to get user input
                    final EditText input = new EditText(mContext);
                    alert.setView(input);
                    input.setText(contact.getMessage());

                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            contact.setMessage(input.getText().toString());
                            PreferenceUtils.updateContactMessage(contact, mContext);
                        }
                    });

                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    alert.show();
                }
            }
        });
        return convertView;
    }
}

class LocationViewHolder {
    public TextView name;
    public ImageView minus;
    public ImageView image;
}
