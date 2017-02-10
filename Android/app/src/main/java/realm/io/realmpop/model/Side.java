package realm.io.realmpop.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Side extends RealmObject {

    @Required
    private String playerId;

    @Required
    private String name;
    private long left;
    private double time;
    private boolean failed;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; } 

    public long getLeft() { return left; }

    public void setLeft(long left) { this.left = left; } 

    public double getTime() { return time; }

    public void setTime(double time) { this.time = time; } 

    public boolean isFailed() { return failed; }

    public void setFailed(boolean failed) { this.failed = failed; }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
