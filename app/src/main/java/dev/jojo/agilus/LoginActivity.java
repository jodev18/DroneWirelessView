package dev.jojo.agilus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

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

        ButterKnife.bind(this);


        setButtonListeners();
        prg = new ProgressDialog(LoginActivity.this);

        if(ParseUser.getCurrentUser() != null){
            startActivity(new Intent(getApplicationContext(),AccountsActivity.class));
            //finish();
        }
        else{
            setButtonListeners();
            prg = new ProgressDialog(LoginActivity.this);
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

                                if(e==null){

                                    prg.setMessage("Checking account...");

                                    String role = user.getString("Role");

                                    if(role.equals("Admin")){
                                        startActivity(new Intent(getApplicationContext(),AccountsActivity.class));
                                    }
                                    else{
                                        startActivity(new Intent(getApplicationContext(),ScannedAreaListActivity.class));
                                    }
                                }
                                else{
                                    prg.dismiss();
                                    Snackbar.make(eUsername,"There was a problem encountered" +
                                            " while logging in. Please check your internet connection.",Snackbar.LENGTH_LONG).show();
                                    Log.e("Login Problem",e.getMessage());
                                }
                            }
                        });
                    }
                    else{
                        Snackbar.make(eUsername,"Please enter your password, " +
                                "which is should be at least 8 characters.",Snackbar.LENGTH_LONG).show();
                    }
                }
                else {
                    Snackbar.make(eUsername,"Please enter your username, " +
                            "which is should be at least 8 characters.",Snackbar.LENGTH_LONG).show();
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


}
