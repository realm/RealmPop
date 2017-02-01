package realm.io.realmpop.model.realm;

import io.realm.RealmObject;

public class Game extends RealmObject {
    private Side player1;
    private Side player2;

    public Side getPlayer1() { return player1; }

    public void setPlayer1(Side player1) { this.player1 = player1; } 

    public Side getPlayer2() { return player2; }

    public void setPlayer2(Side player2) { this.player2 = player2; } 

}
