package realm.io.realmpop.view;

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

    public interface PlayerChallengeDelegate {
        void onChallengePlayer(String playerId);
    }

    private PlayerChallengeDelegate playerChallengeDelegate;

    public PlayerRecyclerViewAdapter(@NonNull PlayerChallengeDelegate delegate, @NonNull OrderedRealmCollection<Player> players) {
        super(players, true);
        this.playerChallengeDelegate = delegate;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        OrderedRealmCollection data = getData();
        if(data != null) {
            final Player player = getData().get(position);
            holder.playerId = player.getId();

            TextView tv = holder.titleView;
            tv.setText(player.getName());
            if (player.isAvailable()) {
                tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.playerAvailableColor));
            } else {
                tv.setTextColor(ContextCompat.getColor(tv.getContext(), R.color.playerUnavailableColor));
            }

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playerChallengeDelegate.onChallengePlayer(player.getId());
                }
            });
        }
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
