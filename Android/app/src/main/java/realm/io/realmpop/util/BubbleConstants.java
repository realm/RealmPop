package realm.io.realmpop.util;

import realm.io.realmpop.BuildConfig;

public class BubbleConstants {

    public static final String REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/game";
    public static final String AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth";
    public static final String ID = "default@realm";
    public static final String PASSWORD = "password";


    public static final int bubbleCount = 7;
    public static final int bubbleValueMax = 80;
}
