package realm.io.realmpop.controller.playername

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.animation.AnimationUtils
import kotlinx.android.synthetic.main.activity_playername.*
import org.jetbrains.anko.startActivity
import realm.io.realmpop.R
import realm.io.realmpop.controller.BaseAuthenticatedActivity
import realm.io.realmpop.controller.gameroom.GameRoomActivity
import realm.io.realmpop.databinding.ActivityPlayernameBinding

class PlayerNameActivity : BaseAuthenticatedActivity() {

    lateinit var playerNameObserver: TextListener
    lateinit var viewModel: PlayerNameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(PlayerNameViewModel::class.java)
        val binding = DataBindingUtil.setContentView<ActivityPlayernameBinding>(this, R.layout.activity_playername)
        binding.vm = viewModel
        setContentView(R.layout.activity_playername)
        bindState()
        playerNameObserver = TextListener(this, lifecycle, playerNameEditText!!) {
            viewModel.updateName()
        }
    }

    fun bindState() {
        viewModel.state.observe(this, Observer { state ->
            when(state) {
                PlayerNameViewModel.State.COMPLETED -> moveToGameRoom()
                PlayerNameViewModel.State.BAD_INPUT -> shakeText()
                PlayerNameViewModel.State.APP_RESTART_NEEDED -> restartApp()
                PlayerNameViewModel.State.WAITING_USER -> Unit
            }
        })
    }

    private fun shakeText() =
            playerNameEditText?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake))

    private fun moveToGameRoom() {
        viewModel.updateName(
                onSuccess={startActivity<GameRoomActivity>()},
                onFailure = {restartApp()})
    }

}
