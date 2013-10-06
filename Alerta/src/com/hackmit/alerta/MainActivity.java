package com.hackmit.alerta;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.hackmit.alerta.adapters.PeopleAdapter;
import com.hackmit.alerta.datatypes.PickedContact;
import com.hackmit.alerta.utils.PreferenceUtils;
import com.vicv.promises.Promise;
import com.vicv.promises.PromiseListener;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ArrayList<PickedContact> _contacts = new ArrayList<PickedContact>();
    private ListView _listView;
    private PeopleAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.contactButtons)).setOnClickListener(contactsClick());
        ((Button) findViewById(R.id.alertbutton)).setOnClickListener(emergencyClick());
        _listView = (ListView) findViewById(R.id.listView);

        Promise<ArrayList<PickedContact>> contactPromise = PreferenceUtils.loadSavedContacts(this);
        contactPromise.add(new PromiseListener<ArrayList<PickedContact>>() {
            @Override
            public void succeeded(ArrayList<PickedContact> result) {
                _contacts = result;
                setupListView();
                super.succeeded();
            }
        });
    }


    private void setupListView() {
        _adapter = new PeopleAdapter(this,
                R.layout.list_item, _contacts, this
                .getLayoutInflater());
        _listView.setAdapter(_adapter);
    }

    private View.OnClickListener emergencyClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (PickedContact contact : _contacts) {
                    try {
                        if (!contact.getNumber().equals("") && !contact.getMessage().equals("")) {
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(contact.getNumber(), null, contact.getMessage(), null, null);
                            Toast.makeText(getApplicationContext(), "SMS Sent to "+contact.getName(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "SMS sent to" +contact.getNumber()+" failed due to "+e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
    }

    private View.OnClickListener contactsClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 1);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        String name = "";
        String number = "";
        String email = "";

        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                String id =
                        c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                String hasPhone =
                        c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));


                if (hasPhone.equalsIgnoreCase("1")) {
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null, null);
                    phones.moveToFirst();
                    number = phones.getString(phones.getColumnIndex("data1"));
                } else {
                }

                //Sloppity slop
                try {
                    Cursor emails = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
                            null, null);
                    emails.moveToFirst();
                    email = emails.getString(
                            emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                } catch (Exception e) {

                }

            }
            PickedContact newContact = new PickedContact(name, number, email, "");

            Promise<ArrayList<PickedContact>> updatePromise = PreferenceUtils.storeContact(newContact, this);
            updatePromise.add(new PromiseListener<ArrayList<PickedContact>>() {
                @Override
                public void succeeded(ArrayList<PickedContact> result) {
                    _contacts = result;
                    _adapter.notifyDataSetChanged();
                    super.succeeded(result);
                }
            });


            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
