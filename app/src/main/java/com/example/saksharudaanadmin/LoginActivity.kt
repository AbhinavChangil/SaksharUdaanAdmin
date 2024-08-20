package com.example.saksharudaanadmin

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.saksharudaanadmin.databinding.ActivityLoginBinding
import com.example.saksharudaanadmin.model.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)


        // Initialize Google Sign-In options
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance()
        database = Firebase.database

        // Initialize Google Sign-In client
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogle.setOnClickListener {
            val signinIntent = googleSignInClient.signInIntent
            launcher.launch(signinIntent)
        }

        // Initialize loading dialog
        loadingDialog = Dialog(this@LoginActivity).apply {
            setContentView(R.layout.loading_dialog)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCancelable(false)
        }

        binding.tvSignupLink.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.linkForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.edtEmail.error = "Enter your email"
            } else if (password.isEmpty()) {
                binding.edtPassword.error = "Enter password"
            } else {
                loadingDialog.show()
                login(email, password)
            }
        }
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                val account: GoogleSignInAccount? = task.result
                val credential: AuthCredential = GoogleAuthProvider.getCredential(account?.idToken, null)
                auth.signInWithCredential(credential).addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        val user: FirebaseUser? = auth.currentUser
                        val googleName = user?.displayName.toString()
                        val googleEmail = user?.email.toString()
                        saveUserData(googleName, googleEmail)
                        showToast("Google Sign-in Successful!")
                        updateUIOnSuccess()
                    } else {
                        loadingDialog.dismiss()
                        showToast("Google Sign-in failed!")
                    }
                }
            } else {
                loadingDialog.dismiss()
                showToast("Google Sign-in failed!")
            }
        } else {
            loadingDialog.dismiss()
            showToast("Google Sign-in failed!")
        }
    }

    private fun saveUserData(name: String, email: String) {
        val userId = auth.currentUser!!.uid
        val databaseRef = database.getReference("admin/$userId/profile")

        // Check if user data already exists
        databaseRef.get().addOnSuccessListener { dataSnapshot ->
            if (!dataSnapshot.exists()) {
                // Data does not exist, so save the user data
                val user = UserModel(name, email)
                databaseRef.setValue(user).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Database", "User data saved successfully.")
                    } else {
                        Log.d("Database", "Failed to save user data: ${task.exception}")
                    }
                }
            } else {
                // Data exists, skip saving
                Log.d("Database", "User data already exists, skipping save.")
            }
        }.addOnFailureListener { exception ->
            Log.d("Database", "Error checking user data: $exception")
        }
    }


    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null && user.isEmailVerified) {
                    showToast("Logged In Successfully!")
                    updateUIOnSuccess()
                } else {
                    auth.signOut()
                    showToast("Please verify your email address before logging in.")
                    loadingDialog.dismiss()
                }
            } else {
                updateUIOnFailure()
                showToast("Login Failed!")
                Log.d("Authentication", "loginAccount: Authentication Failed", task.exception)
            }
        }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            updateUIOnSuccess()
        }
    }

    private fun updateUIOnSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        loadingDialog.dismiss()
    }

    private fun updateUIOnFailure() {
        loadingDialog.dismiss()
        binding.edtEmail.text?.clear()
        binding.edtPassword.text?.clear()
        binding.edtEmail.error = "Wrong Credentials"
        binding.edtPassword.error = "Wrong Credentials"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
