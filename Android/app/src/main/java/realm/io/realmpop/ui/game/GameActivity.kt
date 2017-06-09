package realm.io.realmpop.ui.game

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_game.*
import org.jetbrains.anko.find
import realm.io.realmpop.R
import realm.io.realmpop.ui.BaseAuthenticatedActivity
import realm.io.realmpop.databinding.ActivityGameBinding
import realm.io.realmpop.util.RandomNumberUtils.generateNumber

class GameActivity : BaseAuthenticatedActivity() {

    private lateinit var popSound: PopSound
    private lateinit var viewModel: GameViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityGameBinding>(this, R.layout.activity_game)
        viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)
        binding.vm = viewModel
        popSound = PopSound(applicationContext, R.raw.pop)
        setupBubbleBoard()
        bindState()
    }

    fun bindState() {
        viewModel.state.observe(this, Observer { state ->
            when(state) {
                GameViewModel.State.IN_PROGRESS -> Unit
                GameViewModel.State.GAME_FINISHED -> finish()
                GameViewModel.State.APP_RESTART_NEEDED -> restartApp()
            }
        })
    }

    override fun onBackPressed() {
        viewModel.exitGameAfterDelay(0)
    }

    private fun setupBubbleBoard() {
        
        val res = resources
        val display = res.displayMetrics
        val spaceTakenByButton = res.getDimensionPixelSize(R.dimen.bubble_button_diameter)
        val titleBarHeight = res.getDimensionPixelSize(res.getIdentifier("status_bar_height", "dimen", "android"))

        val MAX_X_MARGIN = display.widthPixels - spaceTakenByButton // bubble button diameter

        val MAX_Y_MARGIN = display.heightPixels - (
                
                  res.getDimensionPixelSize(R.dimen.activity_vertical_margin) //top margin

                + res.getDimensionPixelSize(R.dimen.activity_vertical_margin) // bottom margin

                + res.getDimensionPixelSize(R.dimen.realm_pop_status_bar_height) // pop status bar height

                + titleBarHeight // android status bar height

                + spaceTakenByButton) // bubble button diameter

        for (bubbleNumber in viewModel.numbers) {
            val bubbleView = layoutInflater.inflate(R.layout.bubble, bubbleBoard, false)
            val params = bubbleView.layoutParams as RelativeLayout.LayoutParams

            params.leftMargin = generateNumber(0, MAX_X_MARGIN)
            params.topMargin = generateNumber(0, MAX_Y_MARGIN)

            bubbleView.find<TextView>(R.id.bubbleValue).text = bubbleNumber.toString()
            bubbleView.setOnClickListener { v ->
                popSound.playSound()
                bubbleBoard.removeView(v)
                viewModel.onBubbleTap(bubbleNumber.toLong())
            }

            bubbleBoard.addView(bubbleView, params)
        }
    }
}

