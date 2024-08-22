package com.example.saksharudaanadmin

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saksharudaanadmin.Adapter.CourseAdapter
import com.example.saksharudaanadmin.adapter.PlaylistAdapter
import com.example.saksharudaanadmin.databinding.ActivityUploadPlaylistBinding
import com.example.saksharudaanadmin.model.PlaylistModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class UploadPlaylistActivity : AppCompatActivity() {
    private val binding: ActivityUploadPlaylistBinding by lazy {
        ActivityUploadPlaylistBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var loadingDialog: Dialog
    private lateinit var playlist: ArrayList<PlaylistModel>
    private lateinit var playlistAdapter: PlaylistAdapter

    private var videoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)

        //initialize auth and database and storage
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize Loading Dialog
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.loading_dialog)
        if (loadingDialog.window != null) {
            loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog.setCancelable(false)
            }

        val courseItemTitle = intent.getStringExtra("courseTitle")


        binding.tvCourseTitle.text = courseItemTitle

        // Adding video from gallery
        binding.cvVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, 102)
        }

        binding.apply {
            fabUploadPlaylist.setOnClickListener {
                val playlistVideoUri = videoUri
                val playlistVideoTitle = edtVideoTitle.text.toString().trim()

                if(playlistVideoUri.toString().isEmpty() || playlistVideoTitle.isEmpty() ){
                    showToast("Please fill all details!")
                }else{
                    if (playlistVideoUri != null) {
                        uploadPlaylistVideo(playlistVideoUri, playlistVideoTitle)
                    }
                    else{
                        showToast("Please Select Video!")
                    }
                }
            }
        }

        retrievePlaylist()

    }

    private fun retrievePlaylist() {
        playlist = arrayListOf()
        val postId = intent.getStringExtra("postId")
        val databaseRef = postId?.let { database.reference.child("course").child(it).child("playlist") }
        if (databaseRef != null) {
            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(list in snapshot.children){
                        val listData = list.getValue(PlaylistModel::class.java)
                        listData?.let {
                            playlist.add(it)
                        }
                    }
                    setAdapter(playlist)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@UploadPlaylistActivity, "Failed to retrieve playlist: ${error.message}", Toast.LENGTH_SHORT).show()
                }

            })
        }
    }

    private fun setAdapter(playlist: ArrayList<PlaylistModel>) {
        playlistAdapter = PlaylistAdapter(playlist)
        binding.rvPlaylist.layoutManager = LinearLayoutManager(this)
        binding.rvPlaylist.adapter = playlistAdapter
        playlistAdapter.notifyDataSetChanged()
    }

    private fun uploadPlaylistVideo(playlistVideoUri: Uri, playlistVideoTitle: String) {
        loadingDialog.show()
        val childKey = System.currentTimeMillis().toString()
        val videoStorageRef = storage.reference.child("course").child("playlist/$childKey-video")
        // Upload Video
        videoStorageRef.putFile(playlistVideoUri).addOnSuccessListener {
            videoStorageRef.downloadUrl.addOnSuccessListener { playlistVideoUrl ->
                val model = auth.uid?.let { it1 ->
                    PlaylistModel(
                        playlistVideoTitle = playlistVideoTitle,
                        playlistVideoUrl = playlistVideoUrl.toString(),
                        playlistEnable = "false"
                    )
                }
                val postId = intent.getStringExtra("postId")
                if (postId != null) {
                    database.reference.child("course").child(postId).child("playlist")
                        .push()
                        .setValue(model).addOnSuccessListener {
                            loadingDialog.dismiss()
                            Toast.makeText(this, "Course Uploaded", Toast.LENGTH_SHORT).show()
                            onBackPressed()
                        }
                }
            }.addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(this, "Failed to get video URL", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            loadingDialog.dismiss()
            Toast.makeText(this, "Failed to upload video", Toast.LENGTH_SHORT).show()
            }

    }

    // To add image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 102 && resultCode == Activity.RESULT_OK) {
            binding.video.visibility = View.VISIBLE
            data?.data?.let { uri ->
                binding.video.setVideoURI(uri)
                binding.video.start()
                binding.videoImg.visibility = View.GONE
                videoUri = uri
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}