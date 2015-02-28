package com.upsud.ui_imi;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

/**
 * Created by Maxence Bobin on 27/02/15.
 */
public class ContactsManager {

    private Context context;
    private String contactID;

    public ContactsManager (Context context) {
        this.context = context;
    }

    public Contact getRandomContact () {
        ContentResolver cr = context.getContentResolver();

        ArrayList<Integer> idList = new ArrayList<Integer>();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            contactID = contactId;

            idList.add(Integer.parseInt(contactId));
        }
        cursor.close();

        Random randGen = new Random();

        int contactId = idList.get(randGen.nextInt(idList.size()));
        
        Uri my_contact_Uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(contactId));

        String[] projection = {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

        String name = null;
        Cursor c = cr.query(my_contact_Uri, projection, null, null, null);
        if(!c.moveToFirst()) { Log.d("contact", "cursor empty"); }
        else { name = c.getString(0); }
        c.close();

        Bitmap photo = retrieveContactPhoto();

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
        String num = "";

        while (phones.moveToNext()) {
            String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int type = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            switch (type) {
                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                    num = number;
                    break;
            }
        }

        phones.close();

        if (!num.equals("")) {
            return new Contact(name, num, photo);

        }
        else {
            return getRandomContact();
        }
    }

    public Bitmap openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;

    }

    private Bitmap retrieveContactPhoto() {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

            if (inputStream != null) {
                photo = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            }
           
        } catch (IOException e) {
            e.printStackTrace();
        }

        return photo;

    }
}
