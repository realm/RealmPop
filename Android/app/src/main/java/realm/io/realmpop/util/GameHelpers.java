package realm.io.realmpop.util;

import io.realm.Realm;
import io.realm.SyncUser;
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
        return realm.where(Player.class).equalTo("id", currentId).findFirst();
    }

}
