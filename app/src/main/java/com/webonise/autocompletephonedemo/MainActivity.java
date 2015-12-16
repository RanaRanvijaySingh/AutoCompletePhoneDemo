package com.webonise.autocompletephonedemo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 99;
    AutoCompleteTextView txtPhoneNo;
    List<String> listNames = new ArrayList<>();
    List<String> listNumbers = new ArrayList<>();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_main);
        initializePermissionCheck();
    }

    public void initializePermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission
                .READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            fetchUserContact();
        }else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(MainActivity.this, "Explain why this permission is needed", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
        }
    }

    private void fetchUserContact() {
        String[] columns = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER};
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, columns, null, null, null);
        int ColumeIndex_DISPLAY_NAME = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int ColumeIndex_HAS_PHONE_NUMBER = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        while(cursor.moveToNext()){
            String phoneNumber = "";
            String name = cursor.getString(ColumeIndex_DISPLAY_NAME);
            String has_phone = cursor.getString(ColumeIndex_HAS_PHONE_NUMBER);
            if(!has_phone.endsWith("0")){
                phoneNumber = getPhoneNumber(name);
                listNames.add(name);
                listNumbers.add(phoneNumber);
            }
        }
        cursor.close();
        initializeAutoCompleteView();
    }

    private String getPhoneNumber(String name) {
        ContentResolver cr = getContentResolver();
        Cursor cursor1 = cr.query(ContactsContract.Contacts.CONTENT_URI, null,
                "DISPLAY_NAME = '" + name + "'", null, null);
        if (cursor1.moveToFirst()) {
            String contactId =
                    cursor1.getString(cursor1.getColumnIndex(ContactsContract.Contacts._ID));

            Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
            while (phones.moveToNext()) {
                return phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone
                        .NUMBER));
            }
            phones.close();
        }
        cursor1.close();
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchUserContact();
                }
                return;
            }
        }
    }

    private void initializeAutoCompleteView() {
        txtPhoneNo = (AutoCompleteTextView) findViewById(R.id.txtPhoneNo);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, listNames);
        txtPhoneNo.setAdapter(adapter);
    }
}