package com.hackmit.alerta.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.hackmit.alerta.datatypes.PickedContact;
import com.vicv.promises.Promise;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by V on 10/6/13.
 */
public class PreferenceUtils {

    private static final String KEY_USERS = "KEY_USERS";

    private static ArrayList<PickedContact> contacts = new ArrayList<PickedContact>();

    public static Promise<ArrayList<PickedContact>> loadSavedContacts(Context c) {

        Promise<ArrayList<PickedContact>> contactPromise = new Promise<ArrayList<PickedContact>>();

        SharedPreferences prefs = c.getSharedPreferences(
                c.getPackageName(), Context.MODE_PRIVATE);

        String people = prefs.getString(KEY_USERS, "");
        try {
            JSONArray array = new JSONArray(people);
            contacts = fromJSON(array);
        } catch (JSONException e) {

        }
        contactPromise.finish(contacts);
        return contactPromise;
    }

    public static Promise<ArrayList<PickedContact>> storeContacts(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(
                c.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(KEY_USERS, toJSONArray(contacts).toString());
        edit.commit();
        Promise<ArrayList<PickedContact>> contactPromise = new Promise<ArrayList<PickedContact>>();
        contactPromise.finish(contacts);
        return contactPromise;
    }

    public static Promise<ArrayList<PickedContact>> storeContact(PickedContact contact, Context c) {
        if (!contacts.contains(contact)) {
            contacts.add(contact);
            return storeContacts(c);
        } else {
            Promise<ArrayList<PickedContact>> p = new Promise<ArrayList<PickedContact>>();
            p.finish(contacts);
            return p;
        }
    }

    public static Promise<ArrayList<PickedContact>> updateContactMessage(PickedContact contact, Context c) {
        if (!contacts.contains(contact)) {
            contacts.add(contact);
        } else {
            contacts.get(contacts.indexOf(contact)).setMessage(contact.getMessage());
        }
        return storeContacts(c);

    }

    public static Promise<ArrayList<PickedContact>> removeContact(PickedContact contact, Context c) {
        contacts.remove(contact);
        return storeContacts(c);
    }

    private static JSONArray toJSONArray(ArrayList<PickedContact> contacts) {
        JSONArray array = new JSONArray();
        for (PickedContact contact : contacts) {
            JSONObject object = new JSONObject();
            try {
                object.put("name", contact.getName());
                object.put("email", contact.getEmail());
                object.put("number", contact.getNumber());
                object.put("message", contact.getMessage());
                object.put("photo", contact.getPhoto());

            } catch (JSONException e) {
                //ffffffffuuuu
            }
            array.put(object);
        }
        return array;
    }

    private static ArrayList<PickedContact> fromJSON(JSONArray array) {
        ArrayList<PickedContact> contacts = new ArrayList<PickedContact>();
        for (int i = 0; i < array.length(); i++) {
            try {
                PickedContact contact = new PickedContact(((JSONObject) array.get(i)).optString("name"), ((JSONObject) array.get(i)).optString("number"), ((JSONObject) array.get(i)).optString("email"), ((JSONObject) array.get(i)).optString("message"), ((JSONObject) array.get(i)).optString("photo"));
                contacts.add(contact);
            } catch (JSONException e) {

            }
        }
        return contacts;
    }


}
