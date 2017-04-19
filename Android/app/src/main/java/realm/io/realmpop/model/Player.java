package realm.io.realmpop.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;
import realm.io.realmpop.util.SharedPrefsUtils;

public class Player extends RealmObject {

    @PrimaryKey
    @Required
    private String id;
    @Required
    private String name;
    private boolean available;
    private Player challenger;
    private Game currentGame;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; } 

    public String getName() { return name; }

    public void setName(String name) { this.name = name; } 

    public boolean isAvailable() { return available; }

    public void setAvailable(boolean available) { this.available = available; } 

    public Player getChallenger() { return challenger; }

    public void setChallenger(Player challenger) { this.challenger = challenger; } 

    public Game getCurrentGame() { return currentGame; }

    public void setCurrentGame(Game currentGame) { this.currentGame = currentGame; }

    public static Player byId(Realm realm, String id) {
        return realm.where(Player.class).equalTo("id", id).findFirst();
    }

    public static void challengePlayer(final String playerId) {
        try(Realm realm = Realm.getDefaultInstance()) {

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Player me = Player.byId(realm, myId());
                    Player theChallenged = Player.byId(realm, playerId);
                    if(me != null && theChallenged != null) {
                        theChallenged.setChallenger(me);
                    }

                }
            });

        }
    }

    public static void assignAvailability(final boolean isAvailable) {
        assignAvailability(isAvailable, myId());
    }

    public static void assignAvailability(final boolean isAvailable, final String playerId) {
        try(Realm realm = Realm.getDefaultInstance()) {

            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    Player me = Player.byId(realm, playerId);
                    me.setAvailable(isAvailable);
                    if(isAvailable) {
                        me.setChallenger(null);
                        me.setCurrentGame(null);
                    }
                }
            });

        }
    }

    private static String myId() {
        return SharedPrefsUtils.getInstance().idForCurrentPlayer();
    }

}
