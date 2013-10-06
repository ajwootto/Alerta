package com.hackmit.alerta;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.contactButtons)).setOnClickListener(contactsClick());
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
        String cNumber ="";
        String email ="";

        if (resultCode == Activity.RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                Toast.makeText(this, name, Toast.LENGTH_SHORT).show();

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
                    cNumber = phones.getString(phones.getColumnIndex("data1"));
                    Toast.makeText(this, cNumber, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "aint got no shit", Toast.LENGTH_SHORT).show();
                }

                //Sloppity slop
                try {
                    Cursor Emails = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + id,
                            null, null);
                    Emails.moveToFirst();
                    email = Emails.getString(
                            Emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Toast.makeText(this, email, Toast.LENGTH_SHORT).show();

                } catch (Exception e) {

                }

            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
