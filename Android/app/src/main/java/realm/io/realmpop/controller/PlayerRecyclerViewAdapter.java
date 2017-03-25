package realm.io.realmpop.controller;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import realm.io.realmpop.R;
import realm.io.realmpop.model.Player;

public class PlayerRecyclerViewAdapter extends RealmRecyclerViewAdapter<Player, PlayerRecyclerViewAdapter.ViewHolder> {

    private GameRoomActivity gameRoomActivity;

    public PlayerRecyclerViewAdapter(@NonNull GameRoomActivity gameRoomActivity, @NonNull OrderedRealmCollection<Player> players) {
        super(players, true);
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
        if (player.isAvailable()) {
            holder.titleView.setTextColor(ContextCompat.getColor(gameRoomActivity, R.color.playerAvailableColor));
        } else {
            holder.titleView.setTextColor(ContextCompat.getColor(gameRoomActivity, R.color.playerUnavailableColor));
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            gameRoomActivity.challengePlayer(player);
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
    }
    

}
