package realm.io.realmpop.model;

import java.util.List;

import io.realm.Realm;
import realm.io.realmpop.model.realm.Player;

class GameModel {

    private Realm realm;

    public GameModel() {
       realm = Realm.getDefaultInstance();
    }

    public Player currentPlayer() {

        String currentId = ""; // UserDefaults.idForCurrentPlayer()

        Player player = realm.where(Player.class).equalTo("id", currentId).findFirst();

        if(player == null) {
            try {
                realm.beginTransaction();
                player = new Player();
                player.setId(currentId);
                player = realm.copyToRealm(player);
                realm.commitTransaction();
            } catch (Exception e) {
                realm.cancelTransaction();
                throw e;
            }
        }

        return player;
    }

    private List<Player> otherPlayers() {
        String myId = "foo";
        return realm.where(Player.class)
                .equalTo("available", true)
                .notEqualTo("id", myId)
                .findAllSorted("name");
    }

    func otherPlayers(than me: Player) -> Results<Player> {
        return realm.objects(Player.self)
            .filter("available = true")
            //.filter("id != %@", me.id)
            .sorted(byKeyPath: "name")
    }

}
