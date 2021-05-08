package com.example.aop_part3_chapter5

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callBackManager : CallbackManager

    private val emailEditText: EditText by lazy {
        findViewById(R.id.emailEditText)
    }
    private val passwordEditText: EditText by lazy {
        findViewById(R.id.passwordEditText)
    }
    private val loginButton: Button by lazy {
        findViewById(R.id.loginButton)
    }
    private val signUpButton: Button by lazy {
        findViewById(R.id.signUpButton)
    }

    private val facebookLoginButton : LoginButton by lazy {
        findViewById(R.id.facebookLoginButton)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth  // FirebaseAuth.getInstance() 같은 코드
        callBackManager = CallbackManager.Factory.create()

        initLoginButton()
        initSignUpButton()
        initEmailAndPasswordEditText()
        initFacebookLoginButton()
    }

    private fun initLoginButton() {
        loginButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    if (task.isSuccessful) {
                        handleSuccessLogin()
                    } else {

                        Toast.makeText(
                            this,
                            "로그인에 실패했습니다. 이메일 또는 비밀번호를 확인해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun initSignUpButton() {
        signUpButton.setOnClickListener { task ->
            val email = getInputEmail()
            val password = getInputPassword()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, OnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.e("SignUpButtonStateTrue::", "${task.isSuccessful}")
                        Toast.makeText(
                            this,
                            "회원가입에 성공하였습니다. 로그인 버튼을 눌러 로그인 해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e("SignUpButtonStateFalse:", "${task.exception}")
                        Toast.makeText(
                            this,
                            "이미 가입한 이메일이거나, 회원가입에 실패하였습니다",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun getInputEmail(): String {
        return emailEditText.text.toString()
    }

    private fun getInputPassword(): String {
        return passwordEditText.text.toString()
    }

    private fun initEmailAndPasswordEditText() {
        emailEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }
        passwordEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }
    }

    private fun initFacebookLoginButton() {
        facebookLoginButton.setPermissions("email", "public_profile")
        facebookLoginButton.registerCallback(callBackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                //TODO: 로그인 성공적
                val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this@LoginActivity) { task ->
                        if(task.isSuccessful) {
                            handleSuccessLogin()
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callBackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSuccessLogin() {
        if(auth.currentUser == null) {
            Toast.makeText(this, "로그인에 실패하였습니다", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = auth.currentUser?.uid.orEmpty()
        val currentUserDB = Firebase.database.reference.child("Users").child(userId)
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId
        currentUserDB.updateChildren(user)

        finish()
    }
}