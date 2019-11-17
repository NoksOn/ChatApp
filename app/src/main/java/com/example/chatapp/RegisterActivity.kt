package com.example.chatapp

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_register.*
import java.io.ByteArrayOutputStream
import java.io.File

class RegisterActivity : AppCompatActivity(),View.OnClickListener{

    private var mAuth:FirebaseAuth? = null
    private var avatar:Uri? = null
    private val GALERY_ID:Int = 1
    private var mRef: DatabaseReference? = null
    private var mStorageRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference
        registerButton.setOnClickListener(this)
        LoadImg.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.registerButton ->{
                if(!TextUtils.isEmpty(registerEmailField.text.toString())&&
                    !TextUtils.isEmpty(registerPasswordField.text.toString())&&
                    !TextUtils.isEmpty(registerNameField.text.toString())){
                    if(avatar!=null){
                        if(registerEmailField.text.contains("@")){
                            if(registerPasswordField.text.length>6) {
                                createAcount()
                                showProgressDialog()
                            }
                            else{
                                Toast.makeText(this,"Password must have min 6 signs",Toast.LENGTH_SHORT).show()
                            }
                        }
                        else{
                            Toast.makeText(this,"please enter correct email",Toast.LENGTH_SHORT).show()
                        }

                    }
                    else{
                        Toast.makeText(this,"Add your image",Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(this,"You have empty fields",Toast.LENGTH_SHORT).show()
                }
            }
            R.id.LoadImg ->{
                var galleryIntent = Intent()
                galleryIntent.type = "image/*"
                galleryIntent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT_IMAGE"),GALERY_ID)

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GALERY_ID && resultCode == Activity.RESULT_OK){
            var image: Uri = data!!.data

            CropImage.activity(image)
                .setAspectRatio(1,1)
                .start(this)

        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            val result = CropImage.getActivityResult(data)
            if(resultCode == Activity.RESULT_OK){
                val resultUri = result.uri
                avatar = resultUri
                LoadImg.borderWidth = 10
                LoadImg.setImageURI(resultUri)

            }
        }
    }

    fun createAcount(){
        val email:String = registerEmailField.text.toString()
        val name:String = registerNameField.text.toString()
        val password:String = registerPasswordField.text.toString()

        mAuth!!.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            task: Task<AuthResult> ->
                if (task.isSuccessful){
                    val userId = mAuth!!.currentUser!!.uid

                    //compress image
                      var thumbFile = File(avatar!!.path)
                      var thumbBitmat = Compressor(this)
                                .setMaxHeight(200)
                                .setMaxWidth(200)
                                .setQuality(65)
                                .compressToBitmap(thumbFile)


                    //Add into firebase storage
                    var byteArray = ByteArrayOutputStream()
                    thumbBitmat.compress(Bitmap.CompressFormat.JPEG,100,byteArray)
                    var thumbByteArray:ByteArray = byteArray.toByteArray()
                    var filePath = mStorageRef!!.child("chat_profile_images").child("$userId.jpg")
                    var thumbFilePath = mStorageRef!!.child("chat_profile_images").child("thumbs").child("$userId.jpg")

                    filePath.putFile(avatar!!).continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        filePath.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUri = task.result.toString()

                            thumbFilePath.putBytes(thumbByteArray).continueWithTask { task ->
                                if (!task.isSuccessful) {
                                    task.exception?.let {
                                        throw it
                                    }
                                }
                                thumbFilePath.downloadUrl
                            }.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val thumbUri = task.result.toString()


                                    var userObject = HashMap<String,Any>()
                                    userObject.put("nickname",name)
                                    userObject.put("image",downloadUri)
                                    userObject.put("thumb_image",thumbUri)
                                    userObject.put("friends","null")
                                    mRef = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                                    mRef!!.setValue(userObject).addOnCompleteListener {
                                            task: Task<Void> ->
                                        if (task.isSuccessful){
                                            Toast.makeText(this,"Welcome to lemon people",Toast.LENGTH_LONG).show()
                                            var intent = Intent(this,DashBoardActivity::class.java)
                                            finish()
                                            startActivity(intent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            else{
                    Toast.makeText(this,"Email already exist",Toast.LENGTH_LONG).show()
                    var intent = intent
                    finish()
                    startActivity(intent)
                }
        }



    }

    fun showProgressDialog(){
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog_activity,null)
        val alertDialog = AlertDialog.Builder(this).setView(mDialogView)
         alertDialog.show()
    }


}



