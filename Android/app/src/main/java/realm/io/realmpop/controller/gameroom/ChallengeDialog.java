package realm.io.realmpop.controller.gameroom;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;

import realm.io.realmpop.R;

public class ChallengeDialog {

    public static void presentChallenge(final GameRoomActivity gameRoomActivity, final String challengerName, final String challengerId) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            switch (which){
               case DialogInterface.BUTTON_POSITIVE:
                    gameRoomActivity.onChallengeResponse(true, challengerId);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    gameRoomActivity.onChallengeResponse(false, challengerId);
                    break;

            }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(gameRoomActivity, R.style.AppTheme_RealmPopDialog);
        builder.setMessage(gameRoomActivity.getString(R.string.game_challenge_proposition, challengerName))
                .setPositiveButton(gameRoomActivity.getString(R.string.game_challenge_accept), dialogClickListener)
                .setNegativeButton(gameRoomActivity.getString(R.string.game_challenge_decline), dialogClickListener)
                .create()
                .show();
    }

}
