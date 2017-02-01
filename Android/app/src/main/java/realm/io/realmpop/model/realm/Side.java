package realm.io.realmpop.model.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Side extends RealmObject {
    @Required
    private String name;
    private double time;
    private boolean failed;
    private RealmList<Bubble> bubbles;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; } 

    public double getTime() { return time; }

    public void setTime(double time) { this.time = time; } 

    public boolean isFailed() { return failed; }

    public void setFailed(boolean failed) { this.failed = failed; } 

    public RealmList<Bubble> getBubbles() { return bubbles; }

    public void setBubbles(RealmList<Bubble> bubbles) { this.bubbles = bubbles; } 

}
