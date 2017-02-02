package realm.io.realmpop.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import realm.io.realmpop.R;
import realm.io.realmpop.model.realm.Player;

public class GameRoomActivity extends AppCompatActivity {

    private Realm realm;
    private PlayerRecyclerViewAdapter adapter;

    @BindView(R.id.player_list)
    public RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameroom);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();

        RealmResults<Player> playerList = realm.where(Player.class).equalTo("available", true).findAllSortedAsync("name");
        recyclerView.setAdapter(new PlayerRecyclerViewAdapter(this, playerList));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.removeAllChangeListeners();
        realm.close();
        realm = null;
    }
}
