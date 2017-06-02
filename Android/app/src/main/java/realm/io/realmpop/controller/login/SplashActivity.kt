package realm.io.realmpop.controller.login

import android.app.Activity
import android.app.ProgressDialog
import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.widget.Button
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import io.realm.ObjectServerError
import kotlinx.android.synthetic.main.activity_splash.*
import org.jetbrains.anko.startActivity
import realm.io.realmpop.R
import realm.io.realmpop.controller.playername.PlayerNameActivity
import realm.io.realmpop.databinding.ActivitySplashBinding
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

class SplashActivity : LifecycleActivity() {

    lateinit var progressDialog: ProgressDialog
    lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)
        val binding = DataBindingUtil.setContentView<ActivitySplashBinding>(this, R.layout.activity_splash)
        binding.vm = viewModel
        bindProgressDialog()
        bindErrorSnackbar()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }

    private fun bindProgressDialog() {
        progressDialog = ProgressDialog(this, R.style.AppTheme_RealmPopDialog)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage(getString(R.string.login_connecting_text))
        viewModel.state.observe(this, Observer { loginState ->
            when(loginState) {
                LoginState.WAITING_USER -> hideProgress()
                LoginState.ATTEMPTING_LOGIN -> showProgress()
                LoginState.AUTHENTICATED -> goToPlayerNameScreen()
            }
        })
    }

    private fun bindErrorSnackbar() {
        viewModel.error.observe(this, Observer { errorMsg -> // TODO: Why is this optional
            showConnectionError(errorMsg!!)
        })
    }

    private fun goToPlayerNameScreen() {
        hideProgress()
        startActivity<PlayerNameActivity>()
    }

    private fun showConnectionError(error: String) {
        val snackbar = Snackbar.make(snackbar_container, error, Snackbar.LENGTH_LONG)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlack))
        snackbar.show()
    }

    private fun showProgress() {
        loginButton.setEnabled(false);
        progressDialog.show();
    }

    private fun hideProgress() {
        loginButton.setEnabled(true);
        progressDialog.dismiss();
    }


}
