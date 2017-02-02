package realm.io.realmpop.model;

import java.util.List;

import io.realm.Realm;
import realm.io.realmpop.model.realm.Player;
import realm.io.realmpop.util.SharedPrefsUtils;

public class GameModel {

    private Realm realm;

    public GameModel(Realm realmInstance) {
        realm = realmInstance;
    }

    public Player currentPlayer() {

        String currentId = SharedPrefsUtils.getInstance().idForCurrentPlayer();

        Player player = realm.where(Player.class).equalTo("id", currentId).findFirst();

        if(player == null) {
            try {
                realm.beginTransaction();
                player = new Player();
                player.setId(currentId);
                player.setName("Anonymous");
                player = realm.copyToRealm(player);
                realm.commitTransaction();
            } catch (Exception e) {
                realm.cancelTransaction();
                throw e;
            }
        }

        return player;
    }

    public void makePlayerUnavailableWithNoChallenger(final String id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm r) {
                Player player = r.where(Player.class).equalTo("id", id).findFirst();
                if(player != null) {
                    player.setChallenger(null);
                    player.setAvailable(false);
                }
            }
        });
    }


}
