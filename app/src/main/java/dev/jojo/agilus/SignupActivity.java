package dev.jojo.agilus;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.etNewUsername) EditText nUser;
    @BindView(R.id.etNewPassword) EditText nPass;
    @BindView(R.id.etConfirmPassword) EditText nCPass;
    @BindView(R.id.etNewEmail) EditText nEmail;

    @BindView(R.id.btnCreateAccount) Button bCreate;

    private ProgressDialog prg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);

        initButton();

        prg = new ProgressDialog(SignupActivity.this);
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
                                if(s_email.length()>=3 && s_email.contains("@")){
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
                                                Snackbar.make(nUser,"Error in signing up " +
                                                        "was encountered. Please check network connection.",Snackbar.LENGTH_LONG).show();

                                                Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                else{
                                    prg.dismiss();
                                    Snackbar.make(nUser,"Please enter your correct email address.",Snackbar.LENGTH_LONG).show();
                                }
                            }
                            else{
                                prg.dismiss();
                                Snackbar.make(nUser,"Password and confirm " +
                                        "password must be the same.",Snackbar.LENGTH_LONG).show();
                            }
                        }
                        else{
                            prg.dismiss();
                            Snackbar.make(nUser,"Confirm password must be " +
                                    "at least 8 characters.",Snackbar.LENGTH_LONG).show();
                        }
                    }
                    else{
                        prg.dismiss();
                        Snackbar.make(nUser,"Password must be " +
                                "at least 8 characters.",Snackbar.LENGTH_LONG).show();
                    }
                }
                else{
                    prg.dismiss();
                    Snackbar.make(nUser,"Username must be " +
                            "at least 8 characters.",Snackbar.LENGTH_LONG).show();
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
}
