package com.example.chatapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v7.app.ActionBarDrawerToggle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.chatapp.adapters.ViewPagerAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.activity_dash_board.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.alert_dialog_edit.view.*
import kotlinx.android.synthetic.main.header_navigation.*
import java.io.ByteArrayOutputStream
import java.io.File

class DashBoardActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener,View.OnClickListener {

    private var mAuth: FirebaseUser? = null
    private var mRef: DatabaseReference? = null
    private val GALERY_ID:Int = 1
    private var mStorageRef: StorageReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        mAuth = FirebaseAuth.getInstance().currentUser
        mRef = FirebaseDatabase.getInstance().reference.child("Users").child(mAuth!!.uid)
        mStorageRef = FirebaseStorage.getInstance().reference


        dashNavView.setNavigationItemSelectedListener(this)
        val drawerToggle:ActionBarDrawerToggle = object : ActionBarDrawerToggle(this,
            drawerNavLayout,
            R.string.drawer_open,
            R.string.drawer_close){
            override fun onDrawerClosed(view:View){
                super.onDrawerClosed(view)
            }
            override fun onDrawerOpened(drawerView: View){
                super.onDrawerOpened(drawerView)
                headerChangeImg.setOnClickListener(this@DashBoardActivity)
                setUpProfile()
            }
        }
        drawerNavLayout.addDrawerListener(drawerToggle)


        var sectionAdapter = ViewPagerAdapter(supportFragmentManager)
        dashViewPager.adapter = sectionAdapter
        mainTabs.setupWithViewPager(dashViewPager)
        mainTabs.setTabTextColors(Color.WHITE,Color.WHITE)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_dialog_edit,null)
        val mBuilder = AlertDialog.Builder(this).setView(mDialogView)

        when(item.itemId){
            R.id.LogOut ->{
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            R.id.changeEmail ->{
                val mAlertDialog = mBuilder.show()
                mDialogView.dialogEditTitle.text = "Change email"
                mDialogView.dialogEditField.setHint(R.string.hint_email)
                mDialogView.dialogEditPasswordField.setHint(R.string.hint_password)
                mDialogView.dialogConfirmButton.setOnClickListener {

                    if(!TextUtils.isEmpty(mDialogView.dialogEditField.text.toString())&&
                        !TextUtils.isEmpty(mDialogView.dialogEditPasswordField.text.toString())){

                        if(mDialogView.dialogEditField.text.toString().contains("@")&&
                            mDialogView.dialogEditPasswordField.text.toString().contains("@")) {

                            val newEmail = mDialogView.dialogEditField.text.toString()
                            val password = mDialogView.dialogEditPasswordField.text.toString()
                            val credential = EmailAuthProvider
                                .getCredential(mAuth!!.email!!, password)
                            mAuth!!.reauthenticate(credential)
                                ?.addOnCompleteListener { task: Task<Void> ->
                                    if (task.isSuccessful) {
                                        Log.d("Email", "user reauthenticate succes")
                                        mAuth!!.updateEmail(newEmail)?.addOnCompleteListener { task: Task<Void> ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(applicationContext, "Email changed", Toast.LENGTH_SHORT)
                                                    .show()
                                            } else {
                                                Log.d("Email", task.exception.toString())
                                            }
                                        }
                                    }
                                }

                            mAlertDialog.dismiss()
                        }
                        else{
                            Toast.makeText(applicationContext,"please enter correct email",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            R.id.changeName ->{
                val mAlertDialog = mBuilder.show()
                mDialogView.dialogEditTitle.text = "Change name"
                mDialogView.dialogEditField.setHint(R.string.hint_name)
                mDialogView.dialogEditPasswordField.visibility = View.GONE
                mDialogView.dialogConfirmButton.setOnClickListener {
                    if(!TextUtils.isEmpty(mDialogView.dialogEditField.text.toString())){
                        val newName = mDialogView.dialogEditField.text.toString()
                        mRef!!.child("nickname").setValue(newName).addOnCompleteListener {
                            task: Task<Void> ->
                            if(task.isSuccessful){
                                Toast.makeText(applicationContext,"Name was changed",Toast.LENGTH_SHORT).show()
                            }
                        }
                        mAlertDialog.dismiss()
                    }
                }
            }
            R.id.changePassword ->{
                val mAlertDialog = mBuilder.show()
                mDialogView.dialogEditTitle.text = "Change password"
                mDialogView.dialogEditField.setHint(R.string.hint_new_password)
                mDialogView.dialogEditPasswordField.setHint(R.string.hint_old_password)
                mDialogView.dialogConfirmButton.setOnClickListener {

                    if(!TextUtils.isEmpty(mDialogView.dialogEditField.text.toString())&&
                        !TextUtils.isEmpty(mDialogView.dialogEditPasswordField.text.toString())) {

                        if (mDialogView.dialogEditField.text.toString().length > 6 &&
                            mDialogView.dialogEditPasswordField.text.toString().length > 6
                        ) {

                            val newPassword = mDialogView.dialogEditField.text.toString()
                            val oldPassword = mDialogView.dialogEditPasswordField.text.toString()
                            val credential = EmailAuthProvider
                                .getCredential(mAuth!!.email!!, oldPassword)
                            mAuth!!.reauthenticate(credential)
                                ?.addOnCompleteListener { task: Task<Void> ->
                                    if (task.isSuccessful) {
                                        Log.d("Email", "user reauthenticate succes")
                                        mAuth!!.updatePassword(newPassword)?.addOnCompleteListener { task: Task<Void> ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Password changed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                Log.d("Email", task.exception.toString())
                                            }
                                        }
                                    }
                                }
                            mAlertDialog.dismiss()
                        }
                    }
                    else{
                        Toast.makeText(this,"Password must have min 6 signs",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return true
    }



    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.headerChangeImg ->{
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
                headerChangeImg.setImageURI(resultUri)

                //compress image
                var thumbFile = File(resultUri.path)
                var thumbBitmat = Compressor(this)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(65)
                    .compressToBitmap(thumbFile)


                //Add into firebase storage
                var byteArray = ByteArrayOutputStream()
                thumbBitmat.compress(Bitmap.CompressFormat.JPEG,100,byteArray)
                var thumbByteArray:ByteArray = byteArray.toByteArray()
                var filePath = mStorageRef!!.child("chat_profile_images").child("${mAuth!!.uid}.jpg")
                var thumbFilePath = mStorageRef!!.child("chat_profile_images").child("thumbs").child("$${mAuth!!.uid}.jpg")

                filePath.putFile(resultUri!!).continueWithTask { task ->
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
                                var updateObj = HashMap<String, Any>()
                                updateObj.put("image", downloadUri)
                                updateObj.put("thumb_image", thumbUri)

                                mRef!!.updateChildren(updateObj).addOnCompleteListener {
                                    task: Task<Void> ->
                                    if(task.isSuccessful){
                                        Toast.makeText(applicationContext,"Your image changed",Toast.LENGTH_SHORT).show()
                                    }else{
                                        Log.d("Erorr",task.exception.toString())
                                    }
                                }
                            }
                        }
                    }
                }


            } else{
                val intent = intent
                finish()
                startActivity(intent)
            }
        }
    }

   private fun setUpProfile(){
        mRef!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                p0.toException().printStackTrace()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var name = snapshot.child("nickname").value.toString()
                var imageUrl = snapshot.child("image").value.toString()

                headerNickName.text = name
                Picasso.get().load(imageUrl).into(headerChangeImg)
            }

        })
    }

   private fun setStatus(status:String){
       mRef!!.child("Users").child(mAuth!!.uid)
       var map = HashMap<String,Any>()
       map.put("status",status)
       mRef!!.updateChildren(map)
   }

    override fun onResume() {
        super.onResume()
        setStatus("Online")
    }

    override fun onPause() {
        super.onPause()
        setStatus("Offline")
    }


}
