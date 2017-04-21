package realm.io.realmpop.controller.playername;

import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.ObjectChangeSet;
import io.realm.Realm;
import io.realm.RealmObjectChangeListener;
import realm.io.realmpop.R;
import realm.io.realmpop.controller.BaseAuthenticatedActivity;
import realm.io.realmpop.controller.gameroom.GameRoomActivity;
import realm.io.realmpop.model.Player;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class PlayerNameActivity extends BaseAuthenticatedActivity {

    private static final String TAG = PlayerNameActivity.class.getName();

    private Subscription nameTextViewSubscription;
    private Player me;

    @BindView(R.id.playerNameEditText) public EditText playerNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playername);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        me = Player.byId(getRealm(), getPlayerId());
        me.addChangeListener(onMeChanged);
        playerNameEditText.setText(me.getName());
        nameTextViewSubscription = RxTextView.textChangeEvents(playerNameEditText)
                .subscribeOn(AndroidSchedulers.mainThread())
                // .debounce(500, TimeUnit.MILLISECONDS) // Slows down the updates, might want to in a production app.
                .observeOn(Schedulers.io())
                .subscribe(onTextChangeHandler);
    }

    @Override
    protected void onPause() {
        super.onPause();
        me.removeAllChangeListeners();
        nameTextViewSubscription.unsubscribe();
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

       final String nameText = playerNameEditText.getText().toString();

       getRealm().executeTransactionAsync(new Realm.Transaction() {
           @Override
           public void execute(Realm bgRealm) {
               Player me = Player.byId(bgRealm, getPlayerId());
               me.setName(nameText);
           }

           // afterward, on the foreground, lauch the GameRoomActivity.
       }, new Realm.Transaction.OnSuccess() {
           @Override
           public void onSuccess() {
               goTo(GameRoomActivity.class);
           }
       },
     new Realm.Transaction.OnError() {
           @Override
           public void onError(Throwable error) {
               restartApp();
           }
       });
    }


    private Action1<TextViewTextChangeEvent> onTextChangeHandler = new Action1<TextViewTextChangeEvent>() {
        @WorkerThread
        @Override
        public void call(final TextViewTextChangeEvent textViewTextChangeEvent) {
            try (Realm bgRealm = Realm.getDefaultInstance()) {
                bgRealm.beginTransaction();
                String nameText = textViewTextChangeEvent.text().toString();
                Player bgMe = Player.byId(bgRealm, getPlayerId());
                if(bgMe != null) {
                    bgMe.setName(nameText);
                }
                bgRealm.commitTransaction();
            }
        }
    };

    private void shakeText() {
        playerNameEditText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    private boolean isPlayerNameValid() {
        return !TextUtils.isEmpty(playerNameEditText.getText());
    }

    private RealmObjectChangeListener<Player> onMeChanged = new RealmObjectChangeListener<Player>() {
        @Override
        public void onChange(Player player, ObjectChangeSet objectChangeSet) {
            if (objectChangeSet.isDeleted() || !player.isValid()) {
                restartApp();
            }
        }
    };

}
