package realm.io.realmpop.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import realm.io.realmpop.R;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.util.GameHelpers;

public class PreGameRoomActivity extends BaseActivity implements TextWatcher {

    private static final String TAG = PreGameRoomActivity.class.getName();

    @BindView(R.id.playerNameEditText) public EditText playerNameEditText;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregameroom);
        ButterKnife.bind(this);
        realm = Realm.getDefaultInstance();
        playerNameEditText.setText(GameHelpers.currentPlayer(realm).getName());
        playerNameEditText.addTextChangedListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(realm != null) {
            realm.removeAllChangeListeners();
            realm.close();
            realm = null;
        }
    }

    @OnClick(R.id.playerEnteredButton)
    public void onEnterPressed() {
        if(!isPlayerNameValid()) {
            shakeText();
        } else {
            moveToGameRoom();
        }
    }

    private void moveToGameRoom() {

       if(realm != null) {

           final String nameText = playerNameEditText.getText().toString();

           realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Player me = GameHelpers.currentPlayer(bgRealm);
                    me.setName(nameText);
                }

           // afterward, on the foreground, lauch the GameRoomActivity.
           }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    Intent gameRoomIntent = new Intent(PreGameRoomActivity.this, GameRoomActivity.class);
                    startActivity(gameRoomIntent);
                }
           });
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(realm != null) {
            final String nameText = playerNameEditText.getText().toString();

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Player me = GameHelpers.currentPlayer(bgRealm);
                    me.setName(nameText);
                }

                // afterward, on the foreground, lauch the GameRoomActivity.
            });
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private void shakeText() {
        // do nothing for now.
    }

    private boolean isPlayerNameValid() {
        return !TextUtils.isEmpty(playerNameEditText.getText());
    }
}
