package dev.jojo.agilus.core;

import java.security.SecureRandom;
import java.util.Calendar;

public class CodeGenerator {

    public String getAuthCode(){

        Calendar c = Calendar.getInstance();

        Integer mo = c.get(c.MONTH) + 1;
        Integer yr = c.get(c.YEAR);
        Integer dd = c.get(c.DATE);

        String mchar = String.valueOf((char)(mo.intValue()+dd.intValue() + 40));
        String ychar = String.valueOf((char)(yr-1900));

        return mchar + getRandomString() + ychar;
    }

    /**
     * Thank you, StackOverflow.
     * @return
     */
    private String getRandomString(){
        String easy = RandomString.digits + "ACEFGHJKLMNPQRUVWXYabcdefhijkprstuvwx";
        RandomString randStr = new RandomString(9, new SecureRandom(), easy);

        return randStr.nextString();
    }

}
