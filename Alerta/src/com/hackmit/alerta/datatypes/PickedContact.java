package com.hackmit.alerta.datatypes;

/**
 * Created by V on 10/6/13.
 */
public class PickedContact {

    String _name = "";

    String _number = "";

    String _email = "";

    String _message = "";

    String _photo = null;


    public PickedContact(String name, String number, String email, String message, String photo) {
        _name = name;
        _number = number;
        _email = email;
        _message = message;
        _photo = photo;
    }

    public String getName() {
        return _name;
    }

    public String getPhoto() {
        return _photo;
    }

    public String getNumber() {
        return _number;
    }

    public String getEmail() {
        return _email;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

}


