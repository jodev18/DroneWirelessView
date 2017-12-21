package dev.jojo.agilus;

import android.app.ProgressDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import dev.jojo.agilus.core.CodeGenerator;
import dev.jojo.agilus.core.Globals;

public class NewPilotAccountActivity extends AppCompatActivity {

    @BindView(R.id.etPilotUserName) EditText ePilotUser;
    @BindView(R.id.etPilotPassword) EditText ePilotPass;
    @BindView(R.id.etPilotName) EditText ePilotName;
    @BindView(R.id.etDroneName) EditText eDroneName;

    @BindView(R.id.btnSaveAccount) Button bSave;

    private ProgressDialog prg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_pilot_account);

        ButterKnife.bind(this);
        generateAuth();
        initButton();
        initPilotUser();

        prg = new ProgressDialog(NewPilotAccountActivity.this);

    }

    private void generateAuth(){
        CodeGenerator cgen = new CodeGenerator();
        ePilotPass.setText(cgen.getAuthCode());
    }

    private void initPilotUser(){
        CodeGenerator code = new CodeGenerator();

        final String prefix = "pilot-" + code.getPilotCode()+"-";

        ePilotUser.setText(prefix);
        ePilotUser.setSelection(prefix.length());

        ePilotUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() < prefix.length()){
                    ePilotUser.setText(prefix);
                    ePilotUser.setSelection(prefix.length());
                }
            }
        });
    }

    private void initButton(){
        bSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                prg.setMessage("Creating pilot account...");

                final String pUser = ePilotUser.getText().toString();
                final String pPass = ePilotPass.getText().toString();
                String pName = ePilotName.getText().toString();
                String pDrone = eDroneName.getText().toString();

                final String currUserToken = ParseUser.getCurrentUser().getSessionToken();

                if(pUser.length() >=8){
                    if(pName.length() >= 2){
                        if(pDrone.length() >= 3){

                            prg.show();

                            final ParseObject parseObject = new ParseObject(Globals.PILOT_CLASS_NAME);

                            parseObject.put(Globals.PILOT_USER,pUser);
                            parseObject.put(Globals.PILOT_PASS,pPass);
                            parseObject.put(Globals.PILOT_NAME,pName);
                            parseObject.put(Globals.PILOT_DRONE,pDrone);
                            parseObject.put(Globals.PILOT_ADMIN,ParseUser.getCurrentUser().getObjectId());

                            parseObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {

                                    if(e==null){

                                        prg.setMessage("Creating login account...");

                                        ParseUser pilotUser = new ParseUser();

                                        pilotUser.setUsername(pUser);
                                        pilotUser.setPassword(pPass);
                                        pilotUser.put(Globals.USER_ROLE,Globals.ROLE_PILOT);
                                        pilotUser.put(Globals.PILOT_INFO_TRACKER,parseObject.getObjectId());

                                        pilotUser.signUpInBackground(new SignUpCallback() {
                                            @Override
                                            public void done(ParseException e) {

                                                prg.dismiss();

                                                try{
                                                    ParseUser.become(currUserToken);
                                                }
                                                catch (ParseException pEx){
                                                    Log.e("PARSE_RESTORE_SESSION","Restore session failed. Reason: " + pEx.getMessage());
                                                }

                                                if(e==null){
                                                    Snackbar.make(ePilotName,"Successfully created account!",Snackbar.LENGTH_LONG).show();
                                                    finish();
                                                }
                                                else{
                                                    Log.e("Parse problem",e.getMessage());
                                                    Snackbar.make(ePilotName,"There was a problem saving " +
                                                            "account. Please check internet connection." + e.getMessage(),Snackbar.LENGTH_LONG).show();
                                                }
                                            }
                                        });


                                    }
                                    else{
                                        prg.dismiss();
                                        Snackbar.make(ePilotName,"There was a problem saving " +
                                                "account. Please check internet connection." + e.getMessage(),Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else{
                            Snackbar.make(eDroneName,"Drones must be at least " +
                                    "3 characters in length.",Snackbar.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Snackbar.make(eDroneName,"Pilot name must be at least " +
                                "3 characters in length.",Snackbar.LENGTH_LONG).show();
                    }
                }
                else{
                    Snackbar.make(eDroneName,"Pilot username must be at least " +
                            "3 characters in length.",Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }
}
