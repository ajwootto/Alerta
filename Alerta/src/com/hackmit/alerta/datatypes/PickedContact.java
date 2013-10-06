package com.hackmit.alerta.datatypes;

/**
 * Created by V on 10/6/13.
 */
public class PickedContact {

    String _name = "";

    String _number = "";

    String _email = "";

    String _message = "";


   public PickedContact(String name, String number, String email, String message){
        this._name = name;
        this._number = number;
        this._email = email;
        this._message = message;
    }

    public String getName() {
        return _name;
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

    public void setMessage(String message){
        _message = message;
    }
}


