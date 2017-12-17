package dev.jojo.agilus.objects;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import dev.jojo.agilus.R;

public class PilotActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pilot);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initFAB();

    }
    private void initSessionLocationList(){

    }

    /**
     * Adds new flying session.
     */
    private void initFAB(){

    }

}
