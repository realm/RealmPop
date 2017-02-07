package realm.io.realmpop.model;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Score extends RealmObject {
    @Required
    private String name;
    private double time;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; } 

    public double getTime() { return time; }

    public void setTime(double time) { this.time = time; } 

}
