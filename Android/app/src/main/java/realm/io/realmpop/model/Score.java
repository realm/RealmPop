package realm.io.realmpop.model;

import io.realm.Realm;
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

    public static void addNewScore(final String winnerName, final double finishTime) {
        try(Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Score score = new Score();
                    score.setName(winnerName);
                    score.setTime(finishTime);
                    bgRealm.copyToRealm(score);
                }
            });
        }
    }
    
}
