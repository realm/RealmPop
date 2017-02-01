package realm.io.realmpop;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

import static realm.io.realmpop.RealmConstants.AUTH_URL;
import static realm.io.realmpop.RealmConstants.ID;
import static realm.io.realmpop.RealmConstants.PASSWORD;
import static realm.io.realmpop.RealmConstants.REALM_URL;

public class PreGameRoomActivity extends AppCompatActivity {

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregameroom);
        ButterKnife.bind(this);

        final SyncCredentials syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, true);
        SyncUser.loginAsync(syncCredentials, AUTH_URL, new SyncUser.Callback() {
            @Override
            public void onSuccess(SyncUser user) {
                final SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, REALM_URL).build();
                Realm.setDefaultConfiguration(syncConfiguration);
                realm = Realm.getDefaultInstance();
            }

            @Override
            public void onError(ObjectServerError error) {
            }
        });
    }
}
