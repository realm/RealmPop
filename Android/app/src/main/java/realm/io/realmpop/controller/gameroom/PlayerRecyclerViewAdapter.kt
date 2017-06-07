package realm.io.realmpop.controller.gameroom

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import realm.io.realmpop.R
import realm.io.realmpop.model.Player

class PlayerRecyclerViewAdapter(private val viewModel: GameRoomViewModel) :
        RealmRecyclerViewAdapter<Player, PlayerRecyclerViewAdapter.ViewHolder>(viewModel.otherPlayers, true) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.player_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = data?.get(position)
        player?.let { player ->
            holder.playerId = player.id
            val tv = holder.titleView
            tv.text = player.name
            if (player.isAvailable) {
                tv.setTextColor(ContextCompat.getColor(tv.context, R.color.playerAvailableColor))
            } else {
                tv.setTextColor(ContextCompat.getColor(tv.context, R.color.playerUnavailableColor))
            }

            holder.view.setOnClickListener { viewModel.challengePlayer(player.id) }
        }
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var playerId: String? = null
        val titleView: TextView

        init {
            titleView = view.findViewById(R.id.title) as TextView
        }
    }


}
