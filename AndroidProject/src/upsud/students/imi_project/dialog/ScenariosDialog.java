package upsud.students.imi_project.dialog;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.openrdf.model.Resource;

import java.io.File;
import java.lang.reflect.Field;

import upsud.students.imi_project.R;
import upsud.students.imi_project.queries.Queries;

/**
 * Created by Bruno on 06/02/2015.
 */
public class ScenariosDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ListView mainView = new ListView(builder.getContext());

        /* récupération des fichiers de scenario */
        Field[] fields = R.raw.class.getFields();
        String[] filesnames = new String[fields.length-1];

        // graph.rdf fait aussi partie du dossier raw/
        int index = 0;
        for(int count=0; count < fields.length; count++){
            if(!fields[count].getName().equals("graph")) {
                filesnames[index++] = fields[count].getName();
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(builder.getContext(),
                R.layout.menu_list_item, filesnames) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                v.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent me) {

                        switch(me.getActionMasked()) {
                            case(MotionEvent.ACTION_DOWN):
                                v.setBackgroundResource(android.R.color.holo_blue_light);
                                try {
                                    String res = Queries.adaptToScenario(getActivity(),
                                            getResources().getIdentifier(((TextView) v).getText() + "", "raw/", "upsud.students.imi_project"));
                                    Log.d("files", "Res : " + res);
                                } catch (Exception exn) {
                                    exn.printStackTrace();
                                }
                                return false;

                            case(MotionEvent.ACTION_UP):
                                ScenariosDialog.this.dismiss();

                            case(MotionEvent.ACTION_CANCEL):
                                v.setBackgroundResource(android.R.color.background_light); return false;
                        }
                        return true;
                    }
                });

                return v;
            }
        };

        mainView.setAdapter(adapter);

        builder.setView(mainView);

        builder.setTitle("Choose a scenario")
                /*.setPositiveButton("Select this scenario", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })*/
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}
