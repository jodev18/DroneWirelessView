package dev.jojo.agilus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.jojo.agilus.core.Globals;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.btnLogin) Button bLogin;
    @BindView(R.id.btnSignup) Button bSignup;

    @BindView(R.id.etUsername) EditText eUsername;
    @BindView(R.id.etPassword) EditText ePassword;

    private AlertDialog alertInfoDialog;

    private ProgressDialog prg;
    private Disposable netDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setButtonListeners();
        prg = new ProgressDialog(LoginActivity.this);

        if(ParseUser.getCurrentUser() != null){

            if(ParseUser.getCurrentUser().getString(Globals.USER_ROLE).equals(Globals.ROLE_PILOT)){
                startActivity(new Intent().setClass(getApplicationContext(), PilotActivity.class));
                finish();
            }
            else{
                if(ParseUser.getCurrentUser().getString(Globals.USER_ROLE).equals(Globals.ROLE_ADMIN)){
                    startActivity(new Intent(getApplicationContext(),AdminActivity.class));
                    finish();
                }
                else{
                    //Something's wrong. You quit.
                    finish();
                }
            }
            //finish();
        }

        initNetworkListener();

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
                            Snackbar.make(bLogin,"Device connected.",Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder dc = new AlertDialog.Builder(LoginActivity.this);
                            dc.setTitle("Device offline");
                            dc.setMessage("The device is currently offline. Service is unavailable.");
                            dc.setCancelable(false);
                            alertInfoDialog = dc.create();
                            alertInfoDialog.show();
                        }
                    }
                });
    }

    private void setButtonListeners(){

        bLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String uname = eUsername.getText().toString();
                String upword = ePassword.getText().toString();

                if(uname.length() >= 8){
                    if(upword.length() >= 8){
                        prg.setMessage("Logging in...");
                        prg.show();

                        ParseUser.logInInBackground(uname, upword, new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                prg.setMessage("Checking records...");
                                if(e==null){

                                    String role = user.getString(Globals.USER_ROLE);

                                    if(role.equals(Globals.ROLE_PILOT)){

                                        prg.setMessage("Authenticating pilot...");

                                        ParseQuery<ParseObject> pPilot = ParseQuery.getQuery(Globals.PILOT_CLASS_NAME);

                                        pPilot.whereEqualTo("objectId",user.getString("PilotTrackID"));

                                        pPilot.findInBackground(new FindCallback<ParseObject>() {
                                            @Override
                                            public void done(List<ParseObject> objects, ParseException e) {
                                                prg.dismiss();
                                                if(e==null){
                                                    if(objects.size() > 0){
                                                        Log.e("OBJECT_LOCATED",objects.get(0).getObjectId());
                                                        startActivity(new Intent().setClass(getApplicationContext(),PilotActivity.class));
                                                        finish();
                                                    }
                                                    else{
                                                        prg.setMessage("Your account has been deauthenticated. Revoking credentials...");
                                                        ParseUser.getCurrentUser().logOutInBackground(new LogOutCallback() {
                                                            @Override
                                                            public void done(ParseException e) {
                                                                if(e == null){
                                                                    invokeSnackBar("Login rejected.");
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                                else{
                                                    Log.e("ERROR_LOGIN",e.getMessage());
                                                    invokeSnackBar("Login Failed.");
                                                }
                                            }
                                        });

                                    }
                                    else if(role.equals(Globals.ROLE_ADMIN)){
                                        startActivity(new Intent().setClass(getApplicationContext(),AdminActivity.class));
                                        finish();
                                    }


                                }
                                else{
                                    if(e.getMessage().contains("i/o")){
                                        invokeSnackBar("There was a problem encountered" +
                                                " while logging in. Please check your internet connection.");
                                    }
                                    else{
                                        invokeSnackBar("Login failed.");
                                    }
                                }

                                eUsername.setText("");
                                ePassword.setText("");
                            }
                        });
                    }
                    else{
                        invokeSnackBar("Please enter your password, " +
                                "which is should be at least 8 characters.");
                    }
                }
                else {

                    //Snackbar.make(eUsername,,Snackbar.LENGTH_LONG).show();
                    invokeSnackBar("Please enter your username, " +
                                  "which is should be at least 8 characters.");
                }

            }
        });

        bSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),SignupActivity.class));
            }
        });
    }

    private void invokeSnackBar(String message){
        Snackbar snackbar = Snackbar.make(eUsername, message, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        snackbar.show();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder ab = new AlertDialog.Builder(LoginActivity.this);


        ab.setTitle("Exit?");

        ab.setMessage("Are you sure you want to exit?");

        ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LoginActivity.super.onBackPressed();
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
