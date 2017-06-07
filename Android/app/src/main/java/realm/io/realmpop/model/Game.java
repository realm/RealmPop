package realm.io.realmpop.model;

import java.util.Arrays;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Required;
import realm.io.realmpop.util.PopUtils;

import static realm.io.realmpop.util.RandomNumberUtils.generateNumbersArray;

public class Game extends RealmObject {

    private Side player1;
    private Side player2;

    @Required
    private String numbers;

    public Game() {}

    public Game(Player me, Player challenger, int[] numberArray) {
        player1 = new Side(me, numberArray.length);
        player2 = new Side(challenger, numberArray.length);
        numbers = numberArrayToString(numberArray);
    }

    public Side getPlayer1() { return player1; }

    public void setPlayer1(Side player1) { this.player1 = player1; } 

    public Side getPlayer2() { return player2; }

    public void setPlayer2(Side player2) { this.player2 = player2; } 

    public String getNumbers() { return numbers; }

    public void setNumbers(String numbers) { this.numbers = numbers; } 

    public int[] getNumberArray() {
        if(getNumbers() == null) {
            return new int[]{};
        } else {
            String [] numberStrArr = getNumbers().split(",");
            int[] numArr = new int[numberStrArr.length];
            for(int i = 0; i < numArr.length; i++) {
                numArr[i] = Integer.valueOf(numberStrArr[i]);
            }
            return numArr;
        }
    }

    public boolean isGameOver() {
        return player1.isFailed() || player2.isFailed();
    }

    public Side sideWithPlayerId(final String playerId) {
        if(player1 != null && player1.getPlayerId().equals(playerId)) {
            return player1;
        } else if (player2 != null && player2.getPlayerId().equals(playerId)) {
            return player2;
        } else {
            return null;
        }
    }

    public void failUnfinishedSides() {

        final String p1id = getPlayer1().getPlayerId();
        final String p2id = getPlayer2().getPlayerId();

        try(Realm r = Realm.getDefaultInstance()) {

            r.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    Game currentGame = Player.byId(bgRealm, p1id).getCurrentGame();
                    Side p1Side = currentGame.sideWithPlayerId(p1id);
                    Side p2Side = currentGame.sideWithPlayerId(p2id);

                    if(p1Side.getTime() == 0) {
                        p1Side.setFailed(true);
                    }
                    if(p2Side.getTime() == 0) {
                        p2Side.setFailed(true);
                    }
                }
            });

        }
    }

    private static String numberArrayToString(int[] numArray) {
        String numStr = Arrays.toString(numArray);
        return numStr.replaceAll("\\[|\\]|\\s", "");
    }

}
