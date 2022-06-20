package com.example.doan3.view.acticity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.doan3.data.UpUserData
import com.example.doan3.databinding.ActivityLoginBinding
import com.example.doan3.util.Utils
import com.example.example_learn.retrofit.api.RetrofitClient
import com.example.example_learn.retrofit.api.UserService
import com.example.example_learn.retrofit.model.LoginMessage
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.concurrent.timerTask


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var fAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    companion object {
        private const val RC_SIGN_IN = 120
        private const val TAG = "Google Sign In "
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // khai báo biến fAuth
        fAuth = FirebaseAuth.getInstance()

        // sét sự kiện click cho btn
        binding.btnLogin.setOnClickListener { /*loginToAccount()*/
            loginRetrofit()
        }
        loginToFaceBook()
        binding.btnGoogle.setOnClickListener { loginToGoogle() }

        // tắt bàn phím ảo bằng click vào màn hình
        binding.constraint.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                Utils.hideSoftKeyboard(this@LoginActivity, binding.root)
            }
        })
        binding.btnSignup.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val intent = Intent(this@LoginActivity, SignupActivity::class.java)
                startActivity(intent)
                finish()
            }

        })

        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("2848576765-0vro2u0sluhfqpjgca348e9hfku1obot.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

    }

    private fun loginRetrofit() {
        val retrofitClient = RetrofitClient.buildService(UserService::class.java)
        val newLoginData = retrofitClient.login(
            "c16c4d96ae7eae09f9e9100902c478ec",
            binding.edtEmail.text.toString(),
            binding.edtPass.text.toString()
        )
        newLoginData.enqueue((object : Callback<LoginMessage> {

            override fun onResponse(call: Call<LoginMessage>, response: Response<LoginMessage>) {
                if (response.isSuccessful) {
                    if (response.body()!!.api_status == 200) {
                        Log.d("Login :", " successs ${response.body()!!.api_status}")
                        Toast.makeText(this@LoginActivity, "Login Success", Toast.LENGTH_SHORT)
                            .show()
                        Timer().schedule(timerTask {
                            loginSuccessActivity()
                        }, 3000)
                    }
                    if (response.body()!!.api_status == 400) {
                        Log.d("Login :", " fail")
                        Toast.makeText(
                            this@LoginActivity,
                            "Login fail. ${response.body()!!.errors.error_text}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.d("Login :", " fail")
                    Toast.makeText(
                        this@LoginActivity,
                        "Login fail. Can't note connect server",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LoginMessage>, t: Throwable) {
                Log.d("Login :", " fail $t")
                Toast.makeText(
                    this@LoginActivity,
                    "Login fail. Can't note connect server",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }))


    }

    private fun loginSuccessActivity() {
        fAuth.signInWithEmailAndPassword("retrofi2@gmai.com", "123456789")
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user = fAuth.currentUser
                    Log.d("tag", user?.uid.toString())
                    UpdateUI(user)
                } else {
                    Log.w("TAG", "signInWithEmail:failure", it.exception)
                    Snackbar.make(
                        binding.root,
                        "Email or password is incorrect. Please check again",
                        Snackbar.LENGTH_LONG
                    )
                        .show()

                }
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "onActivityResult : Google SignIn intent result")
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = accountTask.exception
            if (accountTask.isSuccessful) {
                try {
                    val account = accountTask.getResult(ApiException::class.java)
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account!!.id)
                    firebaseAuthWithGoogle(account.idToken)
                } catch (e: ApiException) {
                    Log.d(TAG, "Google sign in failed", e)
                }
            } else Log.w(TAG, exception.toString())
        }
        callbackManager.onActivityResult(requestCode, resultCode, data)

    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        fAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = fAuth.currentUser
                    CheckDataProfile(user)
                    UpdateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    UpdateUI(null)
                }
            }
    }

    private fun CheckDataProfile(user: FirebaseUser?) {
        if (user != null) {
            val data = UpUserData(
                ServerValue.TIMESTAMP,
                ServerValue.TIMESTAMP,
                user.uid,
                user.displayName,
                user.photoUrl.toString(),
                user.email,
                null
            )
            val ref = FirebaseDatabase.getInstance().getReference("User")
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.hasChild(user.uid)) {
                        ref.child(user.uid).setValue(data).addOnSuccessListener {
                            Log.d("addUserProfile", "success")
                        }.addOnFailureListener {
                            Log.d("addUserProfile", "failure")
                        }
                    } else Log.e("addUserProfile ", "Already have a profile" + user.uid)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("addUserProfile ", error.message)
                }

            })
        }
    }

    private fun loginToAccount() {

        if (checkValid()) {
            val email = binding.edtEmail.text.toString()
            val pass = binding.edtPass.text.toString()
            fAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        val user = fAuth.currentUser
                        Log.d("tag", user?.uid.toString())
                        UpdateUI(user)
                    } else {
                        Log.w("TAG", "signInWithEmail:failure", it.exception)
                        Snackbar.make(
                            binding.root,
                            "Email or password is incorrect. Please check again",
                            Snackbar.LENGTH_LONG
                        )
                            .show()

                    }
                }
        } else {
            Log.d("checkValidation", "error")

        }


    }

    private fun loginToFaceBook() {
        callbackManager = CallbackManager.Factory.create()
        binding.btnFacebook.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                if (LoginManager.getInstance() == null) {
                    LoginManager.getInstance().logOut()
                } else {
                    LoginManager.getInstance().logInWithReadPermissions(
                        this@LoginActivity, Arrays.asList("email", "public_profile")
                    )
                    LoginManager.getInstance().registerCallback(callbackManager,
                        object : FacebookCallback<LoginResult> {
                            override fun onSuccess(loginResult: LoginResult) {
                                handleFacebookAccessToken(loginResult.accessToken)
                                Log.d("facebooklogin", loginResult.accessToken.toString())
                            }

                            override fun onCancel() {
                                Log.d("facebooklogin", "cancel")
                            }

                            override fun onError(error: FacebookException) {
                                Log.d("facebooklogin", "$error")
                            }
                        })
                }
            }
        })
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        fAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = fAuth.currentUser
                    CheckDataProfile(user)
                    UpdateUI(user)
                } else {
                    Snackbar.make(
                        binding.root,
                        "Authentication failed.",
                        Snackbar.LENGTH_LONG
                    )
                        .show()

                    UpdateUI(null)
                }
            }
    }

    private fun loginToGoogle() {
        googleSignInClient.signInIntent.also {
            this.startActivityForResult(it, RC_SIGN_IN)
        }
    }

    // check hợp lệ của email và pass word
    private fun checkValid(): Boolean {
        val emailValidation = ("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        if (binding.edtEmail.text.toString().isEmpty()) {
            binding.tilEmail.error = "Email not entered"
            return false
        } else {
            binding.tilEmail.error = null
        }
        if (!binding.edtEmail.text.toString().matches(emailValidation.toRegex())) {
            binding.tilEmail.error = "Email invalid"
            return false
        } else {
            binding.tilEmail.error = null
        }
        if (binding.edtPass.text.toString().isEmpty()) {
            binding.tilPass.error = "Password not entered"
            return false
        } else {
            binding.tilPass.error = null
        }
        if (binding.edtPass.text.toString().length < 8) {
            binding.tilPass.error = "Password not entered"
            return false
        } else {
            binding.tilPass.error = null
        }
        return true
    }

    private fun UpdateUI(user: FirebaseUser?) {
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}