package com.example.saksharudaanadmin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Visibility
import com.example.saksharudaanadmin.databinding.ActivityUploadCourseBinding
import com.example.saksharudaanadmin.model.CourseModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class UploadCourseActivity : AppCompatActivity() {
    private val binding: ActivityUploadCourseBinding by lazy {
        ActivityUploadCourseBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var thumbnailUri: Uri
    private lateinit var videoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(binding.root)

        //initialize auth and database and storage
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        //back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Adding thumbnail image from gallery
        binding.cvImgThumbnail.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 101)
        }


        // Adding video from gallery
        binding.cvVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "video/*"
            startActivityForResult(intent, 102)
        }

        binding.apply {
            btnUpload.setOnClickListener {
                val courseThumbnailUri = thumbnailUri
                val courseVideoUri = videoUri
                val courseTitle = edtCourseTitle.text.toString().trim()
                val courseDescription = edtCourseDes.text.toString().trim()
                val courseDuration = edtCourseDuration.text.toString().trim()
                val coursePrice = edtCoursePrice.text.toString().trim()

                if(courseThumbnailUri.toString().isEmpty() || courseVideoUri.toString().isEmpty() || courseTitle.isEmpty() || courseDescription.isEmpty() || courseDuration.isEmpty() || coursePrice.isEmpty()){
                    showToast("Please fill all details!")
                }else{
                    uploadCourse(courseThumbnailUri, courseVideoUri, courseTitle, courseDescription, courseDuration, coursePrice)
                }
            }
        }
    }

    private fun uploadCourse(courseThumbnailUri: Uri, courseVideoUri: Uri, courseTitle: String, courseDescription: String, courseDuration: String, coursePrice: String) {
        val userId = auth.currentUser?.uid?:""
        val courseReference = database.reference.child("course")
        val thumbnailReference = storage.reference.child("course").child("thumbnails")
        val videoReference = storage.reference.child("course").child("videos")
        val postId = courseReference.push().key


        thumbnailReference.putFile(courseThumbnailUri).addOnSuccessListener {
            thumbnailReference.downloadUrl.addOnSuccessListener {
                thumbnailUrl->
                    videoReference.putFile(courseVideoUri).addOnSuccessListener {
                       videoReference.downloadUrl.addOnSuccessListener {
                           videoUrl->

                           val model = CourseModel(
                               courseThumbnailUrl = thumbnailUrl.toString(),
                               courseVideoUrl = videoUrl.toString(),
                               courseTitle = courseTitle,
                               courseDescription = courseDescription,
                               courseDuration = courseDuration,
                               coursePrice = coursePrice,
                               postId = postId,
                               postedBy = userId,
                               enable = "false"
                           )

                           if (postId != null) {
                               courseReference.child(postId).setValue(model).addOnSuccessListener {
                                   showToast("Course Uploaded Successfully!")
                                   onBackPressed()
                               }.addOnFailureListener {
                                   showToast("Course Upload Failed!")

                               }
                           }

                       }

                       }
                    }.addOnFailureListener {
                showToast("Video Upload Failed!")
            }
        }.addOnFailureListener {
            showToast("Thumbnail Upload Failed!")
        }
    }


    // To add image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                binding.imgThumbnail.setImageURI(uri)
                thumbnailUri = uri
            }
        }

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


class FullScreenVideoView(context: Context, attrs: AttributeSet) : VideoView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = View.MeasureSpec.getSize(widthMeasureSpec)
        val height = View.MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width,height)
        }
}