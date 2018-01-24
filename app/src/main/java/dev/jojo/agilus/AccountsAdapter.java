package dev.jojo.agilus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;

import javax.microedition.khronos.opengles.GL;

import dev.jojo.agilus.core.Globals;
import dev.jojo.agilus.objects.AccountObject;

/**
 * Created by myxroft on 25/11/2017.
 */

public class AccountsAdapter extends BaseAdapter {

    private Activity ac;
    private List<AccountObject> accountObjects;

    private Handler h;

    private ProgressDialog prg;

    public AccountsAdapter(Activity act, List<AccountObject> accountObjectList){
        this.ac = act;
        this.accountObjects = accountObjectList;
        this.h = new Handler(ac.getMainLooper());
        this.prg = new ProgressDialog(this.ac);
    }


    @Override
    public int getCount() {
        return accountObjects.size();
    }

    @Override
    public AccountObject getItem(int position) {
        return accountObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView  = this.ac.getLayoutInflater().inflate(R.layout.list_item_accounts,null);
        }

        final AccountObject currAcc = accountObjects.get(position);

        final TextView pilotName, droneName, isActiveStat, pilotUser, pilotPass;

        final ImageButton bRemove,bEdit;

        pilotName = convertView.findViewById(R.id.tvPilotName);
        droneName = convertView.findViewById(R.id.tvDroneName);
        isActiveStat = convertView.findViewById(R.id.tvIsActiveStat);
        pilotUser = convertView.findViewById(R.id.tvPilotUsername);
        pilotPass = convertView.findViewById(R.id.tvPilotPassword);

        bRemove = convertView.findViewById(R.id.imgbtRemovePilot);
        bEdit = convertView.findViewById(R.id.imgbtEditPilot);

        final int pos = position;

        bRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmRemove(currAcc.NAME,currAcc.OBJECT_ID,pos);
            }
        });

        bEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditPilotDialog(currAcc,pilotName,droneName);
            }
        });

        pilotName.setText(currAcc.NAME);
        droneName.setText(currAcc.DRONE_ID);
        isActiveStat.setText((currAcc.IS_ACTIVE != null) ? currAcc.IS_ACTIVE : "Inactive");

        pilotUser.setText(currAcc.USERNAME);
        pilotPass.setText("******************");

        pilotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pilotPass.getText().toString().equals("******************")){
                    pilotPass.setText(currAcc.PASSWORD);

                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pilotPass.setText("******************");
                        }
                    },10000);
                }
                else{
                    pilotPass.setText("******************");
                }
            }
        });

        return convertView;
    }

    private void showEditPilotDialog(final AccountObject accToEdit, final TextView pilot, final TextView drone){

        AlertDialog.Builder eAccDg = new AlertDialog.Builder(this.ac);
        eAccDg.setTitle("Edit Account Info");

        //View
        View accView = this.ac.getLayoutInflater()
                .inflate(R.layout.dialog_edit_account,null);

        final EditText pName = accView.findViewById(R.id.etEditPilotName);
        final EditText dName = accView.findViewById(R.id.etEditDroneName);

        pName.setText(accToEdit.NAME);
        dName.setText(accToEdit.DRONE_ID);

        eAccDg.setView(accView);
        eAccDg.setPositiveButton("Save", null);

        eAccDg.setNegativeButton("Cancel", null);

        final AlertDialog adEdit = eAccDg.create();

        adEdit.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                Button saveEntry = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                saveEntry.setTextColor(Color.parseColor("#1d3356"));

                saveEntry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder confSave = new AlertDialog.Builder(ac);

                        confSave.setTitle("Save Changes?");

                        confSave.setMessage("Are you sure you want to save the changes?" +
                                " You won't be able to revert back once you save it.");

                        confSave.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                adEdit.dismiss();
                                //Perform save

                                ParseQuery<ParseObject> qSave = ParseQuery.getQuery(Globals.PILOT_CLASS_NAME);

                                prg = new ProgressDialog(ac);

                                qSave.getInBackground(accToEdit.OBJECT_ID, new GetCallback<ParseObject>() {
                                    @Override
                                    public void done(ParseObject object, ParseException e) {

                                        if(e==null){

                                            final String pilotName = pName.getText().toString();
                                            final String droneName = dName.getText().toString();

                                            object.put(Globals.PILOT_NAME,pilotName);
                                            object.put(Globals.PILOT_DRONE,droneName);

                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if(e==null){
                                                        pilot.setText(pilotName);
                                                        drone.setText(droneName);
                                                        Toast.makeText(ac, "Successfully saved pilot!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Toast.makeText(ac, e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            Toast.makeText(ac, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                        confSave.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        confSave.create().show();

                    }
                });

                Button cancelSave = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);

                cancelSave.setTextColor(Color.parseColor("#1d3356"));

                cancelSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder confChange = new AlertDialog.Builder(ac);

                        confChange.setTitle("Cancel");

                        confChange.setMessage("Cancel editing?");

                        confChange.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adEdit.dismiss();
                                dialog.dismiss();
                            }
                        });

                        confChange.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        confChange.create().show();
                    }
                });
            }
        });

        adEdit.show();

    }

    private void confirmRemove(String pilotName, final String objectId, final int currIndex){

        AlertDialog.Builder rem = new AlertDialog.Builder(this.ac);


        rem.setTitle("Remove Pilot");
        rem.setMessage("Are you sure you want to remove " + pilotName + "?");

        rem.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                prg.setMessage("Checking pilot...");
                prg.show();

                ParseQuery<ParseObject> pq = ParseQuery.getQuery(Globals.PILOT_CLASS_NAME);

                pq.whereEqualTo("objectId",objectId);

                //Gets the pilotaccounts class
                pq.getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(final ParseObject object, ParseException e) {

                        prg.setMessage("Removing from records...");

                        if(e==null){
                            if(object != null){
                                final String userObjId = object.getObjectId();

                                Log.d("REMOVE_OBJ_ID",userObjId);

                                //remove the record from pilotaccounts
                                object.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {

                                        prg.setMessage("Removing account...");

                                        if (e == null){
                                            prg.dismiss();

                                            Toast.makeText(AccountsAdapter.this.ac, "Removed account.", Toast.LENGTH_SHORT).show();

                                            AccountsAdapter.this.accountObjects.remove(currIndex);
                                            notifyDataSetChanged();
                                        }
                                        else{
                                            Log.e("ERRORR_REMOVE_ACCOUNT",e.getMessage());
                                            Toast.makeText(ac, "There was a problem encountered while removing this pilot.", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            }
                            else{
                                Log.e("PILOT_DATA","Error locating pilot data.");
                            }
                        }
                        else{
                            prg.dismiss();
                            Log.e("ERRORR_FIND_REMOVE",e.getMessage());
                            Toast.makeText(ac, "There was a problem encountered while removing this pilot.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

        rem.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        rem.create().show();
    }
}
