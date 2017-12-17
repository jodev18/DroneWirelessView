package dev.jojo.agilus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.jojo.agilus.core.Globals;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.btnLogin) Button bLogin;
    @BindView(R.id.btnSignup) Button bSignup;

    @BindView(R.id.etUsername) EditText eUsername;
    @BindView(R.id.etPassword) EditText ePassword;



    private ProgressDialog prg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("AGILUS");
        ButterKnife.bind(this);

        setButtonListeners();
        prg = new ProgressDialog(LoginActivity.this);

        if(ParseUser.getCurrentUser() != null){

            if(ParseUser.getCurrentUser().getString(Globals.USER_ROLE).equals(Globals.ROLE_PILOT)){
                startActivity(new Intent().setClass(getApplicationContext(), PilotActivity.class));
            }
            else{
                if(ParseUser.getCurrentUser().getString(Globals.USER_ROLE).equals(Globals.ROLE_ADMIN)){
                    startActivity(new Intent(getApplicationContext(),AdminActivity.class));
                }
                else{
                    //Something's wrong. You quit.
                    finish();
                }
            }
            //finish();
        }

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
                                prg.dismiss();
                                if(e==null){
                                    startActivity(new Intent(getApplicationContext(),AccountsActivity.class));
                                }
                                else{
                                    invokeSnackBar("There was a problem encountered" +
                                            " while logging in. Please check your internet connection.");
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


}
