package realm.io.realmpop.model.realm;

import java.util.Arrays;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Game extends RealmObject {

    private Side player1;
    private Side player2;

    @Required
    private String numbers;

    public Side getPlayer1() { return player1; }

    public void setPlayer1(Side player1) { this.player1 = player1; } 

    public Side getPlayer2() { return player2; }

    public void setPlayer2(Side player2) { this.player2 = player2; } 

    public String getNumbers() { return numbers; }

    public void setNumbers(String numbers) { this.numbers = numbers; } 

    public void setNumberArray(int[] numArray) {
        String numStr = Arrays.toString(numArray);
        numStr = numStr.replaceAll("\\[|\\]|\\s", "");
        setNumbers(numStr);
    }

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

}
