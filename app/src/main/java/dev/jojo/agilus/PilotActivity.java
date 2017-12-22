package dev.jojo.agilus;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.jojo.agilus.adapters.PilotMenuAdapter;
import dev.jojo.agilus.core.Globals;
import dev.jojo.agilus.objects.PilotMenuItem;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class PilotActivity extends AppCompatActivity {

    @BindView(R.id.tvPilotName) TextView profName;
    @BindView(R.id.tvDroneName) TextView droneName;

    @BindView(R.id.lvPilotMenu) ListView lPilotMenu;


    private ProgressDialog prg;

    private Disposable netDisposable;
    private AlertDialog alertInfoDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.agilus_graphic);
        setSupportActionBar(toolbar);
        setTitle("");
        ButterKnife.bind(this);

        initFAB();
        //initSessionLocationList();
        initProgressDialog();
        initProfileData();
        initPilotMenu();
        initNetworkListener();

    }

    private void initProgressDialog(){
        prg = new ProgressDialog(PilotActivity.this);
    }

    private void initPilotMenu(){

        List<PilotMenuItem> pilotMenuItems = new ArrayList<>();

        String[] titles = {"Edit Info","Pinned Location","Images","Logout"};
        String[] descriptions = {"Edit your name, drone name...","List of all pinned location","Images saved during recon","Log out from account..."};

        for(int i =0; i<titles.length;i++){

            PilotMenuItem pmi = new PilotMenuItem();

            pmi.MENU_NAME = titles[i];
            pmi.MENU_DESC = descriptions[i];

            pilotMenuItems.add(pmi);

        }

        PilotMenuAdapter pAdapt = new PilotMenuAdapter(pilotMenuItems,PilotActivity.this);

        lPilotMenu.setAdapter(pAdapt);

        lPilotMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch(position){

                    case 0:
                        //Account info

                        break;

                    case 1:
                        //Pinned Locations

                        break;

                    case 2:

                        //Images

                        break;

                    case 3:
                        //Logout
                        confirmLogoutDialog();
                        break;


                    default:

                }

            }
        });


    }

    private void showEditInfo(){


    }

    private void showPinnedLocations(){

    }

    private void showAllSavedImages(){

    }

    private void confirmLogoutDialog(){


        AlertDialog.Builder ab = new AlertDialog.Builder(PilotActivity.this);

        ab.setTitle("Logout");

        ab.setMessage("Are you sure you want to logout?");

        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                prg.setMessage("Logging out...");
                prg.show();

                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {

                        prg.dismiss();

                        if(e == null){
                            Toast.makeText(PilotActivity.this, "Successfully logged out.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent().setClass(getApplicationContext(),LoginActivity.class));
                            finish();
                        }
                        else{
                            Toast.makeText(PilotActivity.this, "There was an error logging out.", Toast.LENGTH_SHORT).show();
                            Log.e("LOGOUT_ERR",e.getMessage());
                        }
                    }
                });

            }
        });

        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        ab.create().show();

    }
    private void initNetworkListener(){

        netDisposable = ReactiveNetwork.observeNetworkConnectivity(getApplicationContext())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Connectivity>() {
                    @Override public void accept(final Connectivity connectivity) {
                        // do something with connectivity
                        // you can call connectivity.getState();
                        // connectivity.getType(); or connectivity.toString();
                        if(connectivity.getState().equals(NetworkInfo.State.CONNECTED)){
                            if(alertInfoDialog != null){
                                if(alertInfoDialog.isShowing()){
                                    alertInfoDialog.dismiss();
                                }
                            }
                            Snackbar.make(profName,"Device connected.",Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder dc = new AlertDialog.Builder(PilotActivity.this);
                            dc.setTitle("Device offline");
                            dc.setMessage("The device is currently offline. Service is unavailable.");
                            dc.setCancelable(false);
                            alertInfoDialog = dc.create();
                            alertInfoDialog.show();
                        }
                    }
                });
    }

    /**
     * Gets location data from the parse database.

    private void initSessionLocationList(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("");
        query.whereEqualTo("","");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if(e==null){

                }
                else{

                }
            }
        });


    } */

    /**
     * Initializes menu and profile information.
     */
    private void initProfileData(){

        prg.setMessage("Loading profile information...");
        prg.show();


        ParseUser currUser = ParseUser.getCurrentUser();

        if(currUser != null){

            ParseQuery<ParseObject> pilotInfo = ParseQuery.getQuery(Globals.PILOT_CLASS_NAME);

            pilotInfo.whereEqualTo("objectId",currUser.getString(Globals.PILOT_INFO_TRACKER));

            pilotInfo.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if(e==null){
                        if(objects.size() > 0){
                            String sPilotName = objects.get(0).getString(Globals.PILOT_NAME);
                            String sDroneName = objects.get(0).getString(Globals.PILOT_DRONE);

                            profName.setText(sPilotName);
                            droneName.setText(sDroneName);

                        }


                        prg.dismiss();
                    }
                }
            });


        }
        else{

            prg.dismiss();


            AlertDialog.Builder sessExp = new AlertDialog.Builder(PilotActivity.this);

            sessExp.setTitle("Session Expired");
            sessExp.setMessage("Your session has expired. Please log in again.");

            sessExp.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {//TODO Handle logout callback

                            startActivity(new Intent().setClass(getApplicationContext(),LoginActivity.class));
                            finish();
                        }
                    });
                }
            });

            sessExp.setCancelable(false);
            sessExp.create().show();
        }


    }

    /**
     * TODO Adds new flying session.
     */
    private void initFAB(){

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onBackPressed(){

        AlertDialog.Builder ab = new AlertDialog.Builder(PilotActivity.this);

        ab.setTitle("Quit");

        ab.setMessage("Are you sure you want to quit?");

        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                PilotActivity.super.onBackPressed();
            }
        });

        ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        ab.create().show();
    }

    @Override
    public void onDestroy(){
        if (netDisposable != null && !netDisposable.isDisposed()) {
            netDisposable.dispose();
        }

        super.onDestroy();
    }

}
