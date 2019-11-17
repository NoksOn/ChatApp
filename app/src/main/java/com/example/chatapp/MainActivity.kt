package com.example.chatapp

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress_dialog_activity.view.*

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private var mAuth:FirebaseAuth? = null
    private var currentUser:FirebaseUser? = null
    var mAuthListener: FirebaseAuth.AuthStateListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        mAuthListener = FirebaseAuth.AuthStateListener {
            firebaseAuth: FirebaseAuth ->
            currentUser = firebaseAuth.currentUser
            if(currentUser!=null){
                var intent = Intent(this,DashBoardActivity::class.java)
                Log.d("user",firebaseAuth.currentUser.toString())
                Toast.makeText(this,"Welcome to Lemon People",Toast.LENGTH_LONG).show()
                startActivity(intent)
                finish()
            }
        }
        SignIn.setOnClickListener(this)
        SignUp.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.SignIn -> {
                if (!TextUtils.isEmpty(mainEmailField.text.toString())&&!TextUtils.isEmpty(mainPasswordField.text.toString())){
                    var userMail = mainEmailField.text.toString()
                    var userPassword = mainPasswordField.text.toString()
                    showProgressDialog()
                    authorizedUser(userMail,userPassword)
                }
                else{
                    Toast.makeText(this,"Enter your email and password or register",Toast.LENGTH_LONG).show()
                }

            }
            R.id.SignUp ->{
                var intent = Intent(this,RegisterActivity::class.java)
                startActivity(intent)
            }
        }

    }

    fun authorizedUser(mail:String,password:String){
        mAuth!!.signInWithEmailAndPassword(mail,password).addOnCompleteListener {
            task: Task<AuthResult> ->
            if (task.isSuccessful){
                var intent = Intent(this,DashBoardActivity::class.java)
                currentUser = mAuth!!.currentUser
                Toast.makeText(this,"Welcome to Lemon people",Toast.LENGTH_LONG).show()
                startActivity(intent)

            }
            else{
                Toast.makeText(this,"Not valid password or mail",Toast.LENGTH_LONG).show()
                var intent = intent
                finish()
                startActivity(intent)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        if(mAuthListener!=null){
            Log.d("user","User is removed")
            mAuth!!.removeAuthStateListener(mAuthListener!!)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("user","User is start")
        mAuth!!.addAuthStateListener(mAuthListener!!)
    }

    fun showProgressDialog(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog_activity,null)
        val alertDialog = AlertDialog.Builder(this).setView(mDialogView)
        alertDialog.show()
        mDialogView.progressDialogTitle.text = "Login"
    }

}



