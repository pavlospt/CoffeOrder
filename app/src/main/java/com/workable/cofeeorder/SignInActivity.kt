package com.workable.cofeeorder

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.workable.cofeeorder.user.toOrderUser
import kotlinx.android.synthetic.main.activity_sign_in.sign_in_button


class SignInActivity : AppCompatActivity() {

  private val TAG = "SignInActivity"
  private val RC_SIGN_IN = 111;

  private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestEmail()
      .build()

  private val signInClient by lazy {
    GoogleSignIn.getClient(this, gso)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sign_in)

    checkForSignedInUser()
  }

  private fun checkForSignedInUser() {
    val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this)
    updateUI(googleSignInAccount)
  }

  private fun updateUI(googleSignInAccount: GoogleSignInAccount?) {
    if (googleSignInAccount == null) {
      presentSignInUI()
    } else {
      navigateToOrderScreen(googleSignInAccount)
      finish()
    }
  }

  private fun presentSignInUI() {
    sign_in_button.visibility = View.VISIBLE
    setUpSignInClickListener()
  }

  private fun setUpSignInClickListener() {
    sign_in_button.setOnClickListener {
      signIn()
    }
  }

  private fun signIn() {
    val signInIntent = signInClient.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
  }

  private fun navigateToOrderScreen(googleSignInAccount: GoogleSignInAccount) {
    val orderUser = googleSignInAccount.toOrderUser()
    OrderActivity.startScreen(this, orderUser)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RC_SIGN_IN) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      handleSignInResult(task)
    }
  }

  private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
    try {
      val account = task?.getResult(ApiException::class.java)
      updateUI(account)
    } catch (e: ApiException) {
      Log.d(TAG, "signInResult:failed code=${e.statusCode}")
      updateUI(null)
    }
  }
}
