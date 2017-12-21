package dev.jojo.agilus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.etNewUsername) EditText nUser;
    @BindView(R.id.etNewPassword) EditText nPass;
    @BindView(R.id.etConfirmPassword) EditText nCPass;
    @BindView(R.id.etNewEmail) EditText nEmail;

    @BindView(R.id.btnCreateAccount) Button bCreate;

    private ProgressDialog prg;
    private AlertDialog alertInfoDialog;

    private Disposable netDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        setTitle("Sign up to Agilus");

        ButterKnife.bind(this);

        initButton();

        prg = new ProgressDialog(SignupActivity.this);

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
                            Snackbar.make(nUser,"Device connected.",Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            AlertDialog.Builder dc = new AlertDialog.Builder(SignupActivity.this);
                            dc.setTitle("Device offline");
                            dc.setMessage("The device is currently offline. Service is unavailable.");
                            dc.setCancelable(false);
                            alertInfoDialog = dc.create();
                            alertInfoDialog.show();
                        }
                    }
                });
    }

    private void invokeSnackBar(String message){
        Snackbar snackbar = Snackbar.make(nUser, message, Snackbar.LENGTH_LONG);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        snackbar.show();
    }

    private void initButton(){
        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                prg.setMessage("Signing up...");
                prg.show();

                String s_user = nUser.getText().toString();
                String s_pass = nPass.getText().toString();
                String s_c_pass = nCPass.getText().toString();
                String s_email = nEmail.getText().toString();

                if(s_user.length() >= 8){
                    if(s_pass.length() >= 8){
                        if(s_c_pass.length() >= 8){
                            if(s_c_pass.equals(s_pass)){
                                if(s_email.length()>=3 && s_email.contains("@") && s_email.contains(".")){
                                    ParseUser parseUser = new ParseUser();
                                    parseUser.setUsername(s_user);
                                    parseUser.setPassword(s_c_pass);
                                    parseUser.setEmail(s_email);
                                    parseUser.put("Role","Admin");

                                    parseUser.signUpInBackground(new SignUpCallback() {
                                        @Override
                                        public void done(ParseException e) {

                                            prg.dismiss();

                                            if(e == null){
                                                Toast.makeText(getApplicationContext(), "Signup success! You can now log in.", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                            else{
                                                invokeSnackBar("Error in signing up " +
                                                        "was encountered. Please check network connection.");
                                            }
                                        }
                                    });
                                }
                                else{
                                    prg.dismiss();
                                    invokeSnackBar("Please enter your correct email address.");
                                }
                            }
                            else{
                                prg.dismiss();
                                invokeSnackBar("Password and confirm " +
                                        "password must be the same.");

                            }
                        }
                        else{
                            prg.dismiss();
                            invokeSnackBar("Confirm password must be " +
                                    "at least 8 characters.");
                        }
                    }
                    else{
                        prg.dismiss();
                        invokeSnackBar("Password must be " +
                                "at least 8 characters.");
                    }
                }
                else{
                    prg.dismiss();
                    invokeSnackBar("Username must be " +
                            "at least 8 characters.");
                }
            }
        });
    }

    @Override
    public void onBackPressed(){

        AlertDialog.Builder bb = new AlertDialog.Builder(SignupActivity.this);

        bb.setTitle("Cancel Signup");
        bb.setMessage("Are you sure you want to cancel signup?");

        bb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SignupActivity.super.onBackPressed();
            }
        });

        bb.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        bb.create().show();
    }

    @Override
    public void onDestroy(){
        if (netDisposable != null && !netDisposable.isDisposed()) {
            netDisposable.dispose();
        }

        super.onDestroy();
    }
}
