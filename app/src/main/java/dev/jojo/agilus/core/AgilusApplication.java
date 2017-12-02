package dev.jojo.agilus.core;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by myxroft on 25/11/2017.
 */

public class AgilusApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
              .applicationId("9au9JPemzzIAu3rAuXB75VDvsgo6pb8m1FaOJNK5")
            .server("https://parseapi.back4app.com/")
          .clientKey("sp8344QLRnKYUqHhPp54uJVjl7cCLAkh6IgRz4HT")
        .build()
        );
    }
}
