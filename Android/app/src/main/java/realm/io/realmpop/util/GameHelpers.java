package realm.io.realmpop.util;

import io.realm.Realm;
import realm.io.realmpop.model.Player;
import realm.io.realmpop.model.Side;

public class GameHelpers {

    public static Player playerWithId(String id, Realm realm) {
        return realm.where(Player.class).equalTo("id", id).findFirst();
    }

    public static Side sideWithPlayerId(String id, Realm realm) {
        return realm.where(Side.class).equalTo("playerId", id).findFirst();
    }

    public static Player currentPlayer(Realm realm) {

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

}
