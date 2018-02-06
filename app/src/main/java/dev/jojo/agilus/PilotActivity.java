package dev.jojo.agilus;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.etiennelawlor.imagegallery.library.activities.ImageGalleryActivity;
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
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

import static dev.jojo.agilus.core.Globals.STATE_INACTIVE;
import static dev.jojo.agilus.core.Globals.STATE_OFFLINE;
import static dev.jojo.agilus.core.Globals.STATE_ONLINE;

public class PilotActivity extends AppCompatActivity {

    @BindView(R.id.tvPilotName) TextView profName;
    @BindView(R.id.tvDroneName) TextView droneName;

    @BindView(R.id.lvPilotMenu) ListView lPilotMenu;


    private ProgressDialog prg;

    private Disposable netDisposable;
    private AlertDialog alertInfoDialog;

    private String pilotObjId;

    private SharedPreferences sp;

    public static final String KEY_PILOT = "sp_pilot_name";
    public static final String KEY_DRONE = "sp_drone_name";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot);
        Toolbar toolbar = findViewById(R.id.toolbar);
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

        sp = PreferenceManager.getDefaultSharedPreferences(PilotActivity.this);

        updateOnlineState(STATE_ONLINE);

    }

    private void saveCurrentPilotInfo(String pilotName, String droneName){

        SharedPreferences.Editor e = sp.edit();

        e.putString(KEY_PILOT,pilotName);
        e.putString(KEY_DRONE,droneName);

        e.commit();
    }

    private void disposeCurrentPilotInfo(){

        SharedPreferences.Editor e = sp.edit();

        e.remove(KEY_DRONE);
        e.remove(KEY_PILOT);

        e.commit();

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
                        showEditPilotDialog();
                        break;

                    case 1:
                        //Pinned Locations
                        showPinnedLocations();
                        break;

                    case 2:

                        //Images
                        showAllSavedImages();

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

    private void showPinnedLocations(){
        Intent goToPinnedLocations = new Intent(getApplicationContext(),PinnedLocations.class);
        startActivity(goToPinnedLocations);
    }

    private void showAllSavedImages(){

//        Intent goImages = new Intent(getApplicationContext(),ReconImagesActivity.class);
//        startActivity(goImages);

        File[] savedImages = getFilesDir().listFiles();
        
        if(savedImages.length > 0){

            //Load all saved images
            List<String> fileNames = new ArrayList<>();

            for(int i=0;i<savedImages.length;i++){
                if(savedImages[i].isFile()){
                    if(savedImages[i].getName().substring(savedImages[i]
                            .getName().lastIndexOf(".") + 1, savedImages[i].getName().length()).equals("png")){

                        fileNames.add(savedImages[i].getAbsolutePath());
                        Log.d("FILE",savedImages[i].getAbsolutePath());
                    }
                }
            }

            Intent intent = new Intent(getApplicationContext(), ImageGalleryActivity.class);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(ImageGalleryActivity.KEY_IMAGES, new ArrayList<>(fileNames));
            bundle.putString(ImageGalleryActivity.KEY_TITLE, "Saved Images");
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "No images saved yet.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmLogoutDialog(){


        AlertDialog.Builder ab = new AlertDialog.Builder(PilotActivity.this);

        ab.setTitle("Logout");

        ab.setMessage("Are you sure you want to logout?");

        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateOnlineState(STATE_OFFLINE);
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

    private void showEditPilotDialog(){

        AlertDialog.Builder eAccDg = new AlertDialog.Builder(PilotActivity.this);
        eAccDg.setTitle("Edit Account Info");

        //View
        View accView = this.getLayoutInflater()
                .inflate(R.layout.dialog_edit_account,null);

        final EditText pName = accView.findViewById(R.id.etEditPilotName);
        final EditText dName = accView.findViewById(R.id.etEditDroneName);

        pName.setText(profName.getText().toString());
        dName.setText(droneName.getText().toString());

        eAccDg.setView(accView);

        eAccDg.setPositiveButton("Save", null);
        eAccDg.setNegativeButton("Cancel", null);

        final AlertDialog pEdit = eAccDg.create();

        pEdit.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button saveEntry = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);

                saveEntry.setTextColor(Color.parseColor("#1d3356"));

                saveEntry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder confSave = new AlertDialog.Builder(PilotActivity.this);

                        confSave.setTitle("Save Changes?");

                        confSave.setMessage("Are you sure you want to save the changes?" +
                                " You won't be able to revert back once you save it.");

                        confSave.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                pEdit.dismiss();
                                //Perform save

                                ParseQuery<ParseObject> qSave = ParseQuery.getQuery(Globals.PILOT_CLASS_NAME);

                                prg = new ProgressDialog(PilotActivity.this);

                                qSave.getInBackground(pilotObjId, new GetCallback<ParseObject>() {
                                    @Override
                                    public void done(ParseObject object, ParseException e) {

                                        if(e==null){

                                            final String pilotNewName = pName.getText().toString();
                                            final String droneNewName = dName.getText().toString();

                                            object.put(Globals.PILOT_NAME,pilotNewName);
                                            object.put(Globals.PILOT_DRONE,droneNewName);

                                            object.saveInBackground(new SaveCallback() {
                                                @Override
                                                public void done(ParseException e) {
                                                    if(e==null){
                                                        profName.setText(pilotNewName);
                                                        droneName.setText(droneNewName);

                                                        saveCurrentPilotInfo(pilotNewName,droneNewName);
                                                        Toast.makeText(PilotActivity.this, "Successfully saved pilot!", Toast.LENGTH_SHORT).show();
                                                    }
                                                    else{
                                                        Toast.makeText(PilotActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                        else{
                                            Toast.makeText(PilotActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

                        AlertDialog.Builder confChange
                                = new AlertDialog.Builder(PilotActivity.this);

                        confChange.setTitle("Cancel");

                        confChange.setMessage("Cancel editing?");

                        confChange.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pEdit.dismiss();
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

        pEdit.show();
    }

    /**
     * Initializes menu and profile information.
     */
    private void initProfileData(){

        prg.setMessage("Loading profile information...");
        prg.show();


        final ParseUser currUser = ParseUser.getCurrentUser();

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

                            saveCurrentPilotInfo(sPilotName,sDroneName);

                            pilotObjId = currUser.getString(Globals.PILOT_INFO_TRACKER);
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

    private void updateOnlineState(final Integer state){

        if(ParseUser.getCurrentUser() != null){ParseQuery<ParseObject> pObj = ParseQuery.getQuery(Globals.PILOT_CLASS_NAME);
            pObj.whereEqualTo("PilotUser",ParseUser.getCurrentUser().getUsername());

            pObj.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    if (e==null){
                        if(objects.size() == 1){
                            Log.d("STATE",state.toString());
                            ParseObject parseObject = objects.get(0);
                            parseObject.put("OnlineState",state);
                            parseObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e==null)
                                        Log.d("UPDATE_STATE","Successfully updated!");
                                    else
                                        Log.d("UPDATE_STATE","Error:" + e.getMessage());
                                }
                            });
                        }
                    }
                }
            });

        }
    }

    /**
     * TODO Adds new flying session.
     */
    private void initFAB(){

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent().setClass(getApplicationContext(),VideoStream.class));

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
    public void onStop(){
        updateOnlineState(STATE_INACTIVE);

        super.onStop();
    }

    @Override
    public void onResume(){
        updateOnlineState(STATE_ONLINE);

        super.onResume();
    }

    @Override
    public void onDestroy(){
        if (netDisposable != null && !netDisposable.isDisposed()) {
            netDisposable.dispose();
        }
        updateOnlineState(STATE_OFFLINE);
        disposeCurrentPilotInfo();

        super.onDestroy();
    }

}
