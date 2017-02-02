package realm.io.realmpop.util;

import realm.io.realmpop.BuildConfig;

public class RealmConstants {

    public static final String REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/game";
    public static final String AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth";
    public static final String ID = "default";
    public static final String PASSWORD = "password";


}
