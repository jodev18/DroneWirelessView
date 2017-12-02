package dev.jojo.agilus.objects;

/**
 * Created by myxroft on 25/11/2017.
 */

public class AccountObject {

    public String USERNAME;
    public String PASSWORD;
    public String OBJECT_ID;
    public String NAME;
    public String DRONE_ID;

    public String IS_ACTIVE;

    public AccountObject(String username, String password, String pilotName, String droneName){
        this.USERNAME = username;
        this.PASSWORD = password;
        this.NAME = pilotName;
        this.DRONE_ID = droneName;
    }

    public AccountObject(String username, String password, String pilotName, String droneName,String objectID){
        this.USERNAME = username;
        this.PASSWORD = password;
        this.NAME = pilotName;
        this.DRONE_ID = droneName;
        this.OBJECT_ID = objectID;
    }

    public AccountObject(String username, String password, String pilotName, String droneName,String objectID,String isActiveStat){
        this.USERNAME = username;
        this.PASSWORD = password;
        this.NAME = pilotName;
        this.DRONE_ID = droneName;
        this.OBJECT_ID = objectID;
        this.IS_ACTIVE = isActiveStat;
    }

}
