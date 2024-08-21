package com.example.saksharudaanadmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.example.saksharudaanadmin.databinding.ActivityEditProfileBinding
import com.example.saksharudaanadmin.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.example.saksharudaanadmin.ForgotPasswordActivity

class EditProfileActivity : AppCompatActivity() {
    private val binding: ActivityEditProfileBinding by lazy {
        ActivityEditProfileBinding.inflate(layoutInflater)
    }
    private var imageUri1: Uri? = null
    private var downloadUri: Uri? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // We should not change email and name
        binding.edtEmail.isEnabled = false
        binding.edtName.isEnabled = false

        // Enable or Disable editing
        fun enableDisable(enableDisable: Boolean) {
            binding.edtProfileHeadline.isEnabled = enableDisable
            binding.edtPhone.isEnabled = enableDisable
            binding.listOfStates.isEnabled = enableDisable
        }

        // By default, disable editing
        enableDisable(true)

        // Setup state drop-down menu
        val StateList: Array<String> = arrayOf("Andaman and Nicobar Islands", "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chandigarh", "Chhattisgarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jammu and Kashmir", "Jharkhand", "Karnataka", "Kerala", "Ladakh", "Lakshadweep", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Puducherry", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, StateList)
        val autoCompleteTextView = binding.listOfStates
        autoCompleteTextView.setAdapter(adapter)

        // Initialize auth and database
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        // Set user data
        setUserData()

        // Adding image from gallery
        binding.cvImgEditProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }

        // Update profile data
        binding.btnSave.setOnClickListener {
            if (imageUri1 != null) {
                uploadImageAndSaveUri { uri ->
                    updateProfileData(uri)
                }
            } else {
                updateProfileData(downloadUri)
            }
        }

        //reset password
        binding.btnResetPassword.setOnClickListener {
            val email = auth.currentUser?.email?:""
            sendPasswordResetEmail(email)
        }
    }


    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Password reset link sent to your email.")
                } else {
                    Log.d("ForgotPassword", "sendPasswordResetEmail: Failed", task.exception)
                    showToast(task.exception!!.message.toString())
                }
            }
    }

    private fun updateProfileData(uri: Uri?) {
        val userId = auth.currentUser?.uid ?: ""
        val name = binding.edtName.text.toString()
        val email = binding.edtEmail.text.toString()
        val userReference = database.getReference("admin").child(userId).child("profile")
        val selectedGender = when {
            binding.radioMale.isChecked -> "Male"
            binding.radioFemale.isChecked -> "Female"
            binding.radioOthers.isChecked -> "Others"
            else -> ""
        }


        // Get current password from the database
        userReference.child("password").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentPassword = snapshot.getValue(String::class.java) ?: ""

                val userData = mutableMapOf<String, Any>(
                    "name" to name,
                    "email" to email,
                    "profileHeadline" to binding.edtProfileHeadline.text.toString(),
                    "phone" to binding.edtPhone.text.toString(),
                    "state" to binding.listOfStates.text.toString(),
                    "gender" to selectedGender,
                    "password" to currentPassword
                )

                // Only update imageUri if a new image has been selected
                uri?.let {
                    userData["imageUri"] = it.toString()
                }

                userReference.setValue(userData).addOnSuccessListener {
                    showToast("Profile Updated Successfully!")
//                    enableDisable(false)
                }.addOnFailureListener {
                    showToast("Profile Update Failed!")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Failed to load user data")
            }
        })
    }

    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userId = auth.currentUser?.uid ?: ""
            val userReference = database.getReference("admin").child(userId).child("profile")

            // Set all values
            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if (userProfile != null) {
                            binding.apply {
                                edtName.setText(userProfile.name)
                                edtEmail.setText(userProfile.email)
                                edtProfileHeadline.setText(userProfile.profileHeadline)
                                edtPhone.setText(userProfile.phone)
                                listOfStates.setText(userProfile.state)

                                //setting image
                                val image = userProfile.imageUri
                                downloadUri = Uri.parse(image)
                                if (image.isNullOrEmpty()) {
                                    imgProfile.setImageResource(R.drawable.ic_profile_img_foreground)
                                } else {
                                    Glide.with(this@EditProfileActivity)
                                        .load(downloadUri)
                                        .into(imgProfile)
                                }


                                //setting gender
                                when(userProfile.gender){
                                    "Male" -> binding.radioMale.isChecked = true
                                    "Female" -> binding.radioFemale.isChecked = true
                                    "Others" -> binding.radioOthers.isChecked = true
                                }

                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    showToast("Failed to load user data")
                }
            })
        }
    }

    // To add image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                binding.imgProfile.setImageURI(uri)
                imageUri1 = uri
            }
        }
    }

    // Upload image to storage and get URL
    private fun uploadImageAndSaveUri(callback: (Uri) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().reference.child("user/$userId/imageUrl")

        imageUri1?.let { uri ->
            val uploadTask = storageReference.putFile(uri)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result
                    callback(downloadUri!!)
                } else {
                    showToast("Image upload failed!")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
