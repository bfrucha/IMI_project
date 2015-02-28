package com.upsud.ui_imi;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class CallDialog extends DialogFragment {

	private String contactName = "Homer Simpson";
	private String phoneNumber = "555-3223";
	private Bitmap photo;
	private boolean calling = true;
	
	public CallDialog(String contactName, String phoneNumber, Bitmap photo, boolean calling) {
		this.contactName = contactName;
		this.phoneNumber = phoneNumber;
		this.photo = photo;
		this.calling = calling;
	}
	
	@SuppressLint("NewApi")
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.call_layout, null);
		builder.setView(layout);
		
		final Dialog dialog = builder.setTitle(R.string.incoming_call).create();
		
		// set the custom dialog components - text, image and button
        final TextView name = (TextView) layout.findViewById(R.id.nameCall);
        name.setText(contactName);

        TextView phone = (TextView) layout.findViewById(R.id.phoneNumber);
        phone.setText(phoneNumber);

        ImageView image = (ImageView) layout.findViewById(R.id.contactPhoto);
        if(photo != null) { image.setImageBitmap(photo); }
        else 			  { image.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.person_icon)); }
        
        
        final ImageButton callButton = (ImageButton) layout.findViewById(R.id.callButton);
        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setTitle(R.string.on_line);
                callButton.setVisibility(View.GONE);
            }
        });

        ImageButton endCallButton = (ImageButton) layout.findViewById(R.id.endCallButton);
        endCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
		
		return dialog;
	}
	 
}
