package com.example.saksharudaanadmin

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saksharudaanadmin.databinding.ActivitySignupBinding
import com.example.saksharudaanadmin.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : AppCompatActivity() {

    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }
    private var imageUri: String? = ""
    private var profileHeadline: String? = ""
    private var gender: String? = ""
    private var phone: String? = ""
    private var state: String? = ""

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        loadingDialog = Dialog(this@SignupActivity).apply {
            setContentView(R.layout.loading_dialog)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnSignup.setOnClickListener {
            val name = binding.edtName.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val confirmPassword = binding.edtPassword2.text.toString().trim()
            val isCheckedRes = binding.policyCheckbox.isChecked

            when {
                name.isEmpty() -> binding.edtName.error = "Enter your name"
                email.isEmpty() -> binding.edtEmail.error = "Enter your email"
                password.isEmpty() -> binding.edtPassword.error = "Enter password"
                confirmPassword.isEmpty() -> binding.edtPassword2.error = "Re-enter password"
                password != confirmPassword -> binding.edtPassword2.error = "Passwords didn't match"
                !isCheckedRes -> showToast("You must accept our privacy policy")
                else -> signup(name, email, password)
            }
        }
    }

    private fun signup(name: String, email: String, password: String) {
        loadingDialog.show()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { verifyTask ->
                    if (verifyTask.isSuccessful) {
                        saveUserData(name, email, password, imageUri, profileHeadline, gender, phone, state)
                        showToast("Account created successfully, please verify your email id")
                        loadingDialog.dismiss()
                        onBackPressed()
                    } else {
                        loadingDialog.dismiss()
                        showToast("Failed to send verification email.")
                    }
                }
            } else {
                loadingDialog.dismiss()
                showToast("Account Creation Failed!")
                Log.d("Account", "createAccount: Failure", task.exception)
                updateUIOnFailure()
            }
        }
    }

    private fun saveUserData(
        name: String,
        email: String,
        password: String,
        imageUri: String?,
        profileHeadline: String?,
        gender: String?,
        phone: String?,
        state: String?
    ) {
        val user = UserModel(name, email, password, imageUri, profileHeadline, gender, phone, state)
        val userId = auth.currentUser!!.uid
        val databaseRef = database.getReference("admin/$userId/profile")
        databaseRef.setValue(user)
    }

    private fun updateUIOnFailure() {
        binding.edtEmail.text?.clear()
        binding.edtPassword.text?.clear()
        binding.edtPassword2.text?.clear()
        binding.edtName.text?.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
