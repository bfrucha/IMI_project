package com.upsud.ui_imi.dialogs;

import java.lang.reflect.Field;

import java.util.HashMap;

import com.upsud.ui_imi.GI_Activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.upsud.ui_imi.R;

public class ScenarioDialog extends DialogFragment {

	GI_Activity mainActivity;
	
	HashMap<String, Integer> ids = new HashMap<String, Integer>();
	
	public ScenarioDialog(GI_Activity mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	@SuppressLint("NewApi")
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        ListView mainView = new ListView(builder.getContext());
        
        Field[] fields = R.raw.class.getFields();
        String[] filenames = new String[fields.length-1]; 
        
        int index = 0;
        for(int count=0; count < fields.length; count++) {
        	if(!fields[count].getName().equals("graph")) {
        		filenames[index++] = fields[count].getName();
        		
        		// bind name with android id
        		ids.put(filenames[index-1], builder.getContext().getResources().getIdentifier(filenames[index-1], "raw", builder.getContext().getPackageName()));
        	}
        }
                
        mainView.setAdapter(new ArrayAdapter<String>(builder.getContext(), android.R.layout.simple_list_item_1, filenames) {
        	
        	public View getView(int position, View convertView, ViewGroup parent) {
        			convertView = super.getView(position, convertView, parent);
        		
        			convertView.setOnClickListener(new OnClickListener() {
        				
						@Override
						public void onClick(View v) {
							mainActivity.changeScenario(ids.get(((TextView) v).getText()));
							dismiss();
						}
        				
        			});
        			
        			return convertView;
        	}
        	
        });
        
        builder.setView(mainView);
        
        builder.setMessage(R.string.title_scenario)
               .setNegativeButton(R.string.cancel_scenario, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
	}
	
}
