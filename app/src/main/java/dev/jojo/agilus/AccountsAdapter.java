package dev.jojo.agilus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

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

        pilotName = (TextView)convertView.findViewById(R.id.tvPilotName);
        droneName = (TextView)convertView.findViewById(R.id.tvDroneName);
        isActiveStat = (TextView)convertView.findViewById(R.id.tvIsActiveStat);
        pilotUser = (TextView)convertView.findViewById(R.id.tvPilotUsername);
        pilotPass = (TextView)convertView.findViewById(R.id.tvPilotPassword);

        bRemove = (ImageButton)convertView.findViewById(R.id.imgbtRemovePilot);
        bEdit = (ImageButton)convertView.findViewById(R.id.imgbtEditPilot);

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
