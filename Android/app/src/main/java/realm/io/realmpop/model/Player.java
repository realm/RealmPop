package realm.io.realmpop.model;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

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
}
