package realm.io.realmpop.view;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.ObjectChangeSet;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmObjectChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import realm.io.realmpop.R;
import realm.io.realmpop.model.Game;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.model.Side;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class GameRoomActivity extends BaseAuthenticatedActivity {

    @BindView(R.id.player_list) public RecyclerView recyclerView;
    private Player me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameroom);
        ButterKnife.bind(this);
        RealmResults<Player> otherPlayers = getRealm()
                .where(Player.class)
                .notEqualTo("id", getPlayerId())
                .findAllSortedAsync("available", Sort.DESCENDING);
        recyclerView.setAdapter(new PlayerRecyclerViewAdapter(this, otherPlayers));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        me = Player.byId(getRealm(), getPlayerId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        me.addChangeListener(onMeChanged);
        Player.assignAvailability(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        me.removeChangeListener(onMeChanged);
        Player.assignAvailability(false);
    }

    @Override
    public void onBackPressed() {
        goTo(PlayerNameActivity.class);
    }

    private RealmObjectChangeListener<Player> onMeChanged = new RealmObjectChangeListener<Player>() {
        @Override
        public void onChange(Player player, ObjectChangeSet objectChangeSet) {
            if(objectChangeSet.isDeleted() || !player.isValid()) {
//                finish(); // TODO: Go back to finish() instad of SplashActivity when https://github.com/realm/realm-java/issues/4502 gets resolved.
                goTo(SplashActivity.class);
                return;
            }

            if(objectChangeSet.isFieldChanged("currentGame") && player.getCurrentGame() != null) {
                goToGame();
            } else if (objectChangeSet.isFieldChanged("challenger") && player.getChallenger() != null) {
                final String challengerName = player.getChallenger().getName();
                final String challengerId = player.getChallenger().getId();
                ChallengeDialog.presentChallenge(GameRoomActivity.this, challengerName, challengerId);
            }
        }
    };

    private void goToGame() {
        Map<String, String> extras = new HashMap<>();
        //extras.put(Game.class.getName(), me.getCurrentGame().getId());
        extras.put(Side.class.getName(), me.getId());
        goTo(GameActivity.class, extras);
    }

    public void onChallengeResponse(boolean isChallengeAccepted, String challengerId) {
        if (isChallengeAccepted) {
            Game.startNewGame(getPlayerId(), challengerId);
        } else {
            Player.assignAvailability(true);
        }
    }

    public void challengePlayer(Player playerToChallenge) {
        Player.challengePlayer(playerToChallenge.getId());
    }

}
