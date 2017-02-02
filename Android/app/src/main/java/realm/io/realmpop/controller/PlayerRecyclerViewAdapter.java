package realm.io.realmpop.controller;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import realm.io.realmpop.R;
import realm.io.realmpop.model.realm.Player;

public class PlayerRecyclerViewAdapter extends RealmRecyclerViewAdapter<Player, PlayerRecyclerViewAdapter.ViewHolder> {

    private GameRoomActivity gameRoomActivity;

    public PlayerRecyclerViewAdapter(@NonNull GameRoomActivity gameRoomActivity, @NonNull OrderedRealmCollection<Player> players) {
        super(gameRoomActivity, players, true);
        this.gameRoomActivity = gameRoomActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Player player = getData().get(position);
        holder.playerId = player.getId();
        holder.titleView.setText(player.getName());

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(player.isAvailable()) {
                gameRoomActivity.challengePlayer(Player player);
            }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public String playerId;
        public final TextView titleView;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            titleView = (TextView) view.findViewById(R.id.title);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + playerId + "' " + ":" + " '" + titleView.getText() + "'";
        }
    }
    

}
