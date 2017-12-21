package dev.jojo.agilus;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import dev.jojo.agilus.objects.AccountObject;

/**
 * Created by myxroft on 25/11/2017.
 */

public class AccountsAdapter extends BaseAdapter {

    private Activity ac;
    private List<AccountObject> accountObjects;

    private Handler h;

    public AccountsAdapter(Activity act, List<AccountObject> accountObjectList){
        this.ac = act;
        this.accountObjects = accountObjectList;
        this.h = new Handler(ac.getMainLooper());
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

        pilotName = (TextView)convertView.findViewById(R.id.tvPilotName);
        droneName = (TextView)convertView.findViewById(R.id.tvDroneName);
        isActiveStat = (TextView)convertView.findViewById(R.id.tvIsActiveStat);
        pilotUser = (TextView)convertView.findViewById(R.id.tvPilotUsername);
        pilotPass = (TextView)convertView.findViewById(R.id.tvPilotPassword);

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
}
