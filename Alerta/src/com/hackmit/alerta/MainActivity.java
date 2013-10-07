package com.hackmit.alerta;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.hackmit.alerta.adapters.PeopleAdapter;
import com.hackmit.alerta.datatypes.PickedContact;
import com.hackmit.alerta.utils.PreferenceUtils;
import com.vicv.promises.Promise;
import com.vicv.promises.PromiseListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends Activity {

    private ArrayList<PickedContact> _contacts = new ArrayList<PickedContact>();
    private final static UUID PEBBLE_APP_UUID = UUID.fromString("6E7659AA-7763-401E-A9BE-1E88E865B8B5");
    private final static int CMD_KEY = 0x00;
    private final static int CMD_UP = 0x01;
    private PebbleKit.PebbleDataReceiver dataReceiver;
    private Handler mHandler;
    private ListView _listView;
    private PeopleAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.contactButtons)).setOnClickListener(contactsClick());
        ((Button) findViewById(R.id.alertbutton)).setOnClickListener(emergencyClick());
        _listView = (ListView) findViewById(R.id.listView);

        mHandler = new Handler();

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

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
        if (dataReceiver != null) {
            unregisterReceiver(dataReceiver);
            dataReceiver = null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // In order to interact with the UI thread from a broadcast receiver, we need to perform any updates through
        // an Android handler. For more information, see: http://developer.android.com/reference/android/os/Handler.html

        // To receive data back from the app, android
        // applications must register a "DataReceiver" to operate on the
        // dictionaries received from the watch.
        //
        // In this example, we're registering a receiver to listen for
        // button presses sent from the watch

        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLE_APP_UUID) {
            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
                final int cmd = data.getUnsignedInteger(CMD_KEY).intValue();
                Toast.makeText(getApplicationContext(), "OH MY GOD", Toast.LENGTH_SHORT).show();

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // All data received from the Pebble must be ACK'd, otherwise you'll hit time-outs in the
                        // watch-app which will cause the watch to feel "laggy" during periods of frequent
                        // communication.
                        PebbleKit.sendAckToPebble(context, transactionId);

                        switch (cmd) {
                            // send SMS when the up button is pressed
                            case CMD_KEY:
                                emergency();
                                break;
                            case CMD_UP:
                                emergency();
                            default:
                                break;
                        }
                    }
                });
            }
        };
        PebbleKit.registerReceivedDataHandler(this, dataReceiver);
        startWatchApp(null);
    }

    // Send a broadcast to launch the specified application on the connected Pebble
    public void startWatchApp(View view) {
        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLE_APP_UUID);
    }

    private void emergency() {
        {
            for (PickedContact contact : _contacts) {
                try {
                    if (!contact.getNumber().equals("") && !contact.getMessage().equals("")) {
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(contact.getNumber(), null, contact.getMessage(), null, null);
                        Toast.makeText(getApplicationContext(), "SMS Sent to " + contact.getName(),
                                Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "SMS sent to" + contact.getNumber() + " failed due to " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            vibrateWatch(this);
        }
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
                emergency();
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
        String image = "";

        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                final String photoId = c.getString(c.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));

                // Get photo data for this contact
                if (photoId != null) {
                    final Cursor photo = managedQuery(
                            ContactsContract.Data.CONTENT_URI,
                            new String[]{ContactsContract.Contacts.Photo.PHOTO},
                            ContactsContract.Data._ID + "=?",
                            new String[]{photoId}, null);

                    if (photo.moveToFirst()) {
                        byte[] photoBlob = photo.getBlob(photo.getColumnIndex(ContactsContract.Contacts.Photo.PHOTO));
                        try {
                            image = new String(photoBlob, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                        }
                    }
                }

                //Get phone number data for contact
                if (hasPhone.equalsIgnoreCase("1")) {
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                    phones.moveToFirst();
                    number = phones.getString(phones.getColumnIndex("data1"));
                }

                //Get email data for contact
                try {
                    Cursor emails = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
                    emails.moveToFirst();
                    email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                } catch (Exception e) {

                }
            }
        }
        PickedContact newContact = new PickedContact(name, number, email, "", image);

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

    public static void vibrateWatch(Context c) {
        PebbleDictionary data = new PebbleDictionary();
        data.addUint8(CMD_KEY, (byte) CMD_UP);
        PebbleKit.sendDataToPebble(c, PEBBLE_APP_UUID, data);
    }
}
