package com.example.saksharudaanadmin.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.GridView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.saksharudaanadmin.UploadPlaylistActivity
import com.example.saksharudaanadmin.databinding.HomeGvItemBinding
import com.example.saksharudaanadmin.model.UserModel
import com.example.saksharudaanadmin.model.CourseModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CourseAdapter(
    private val context: Context,
    private val courseList: ArrayList<CourseModel>
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {
    val database = FirebaseDatabase.getInstance()
    val auth = FirebaseAuth.getInstance()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding =
            HomeGvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding)
    }

    override fun getItemCount(): Int = courseList.size

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener{
            val courseD = courseList[position]
            val intent = Intent(context, UploadPlaylistActivity::class.java)
            intent.putExtra("postId",courseD.postId)
            context.startActivity(intent)
        }
    }

    inner class CourseViewHolder(private val binding: HomeGvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val courseItem = courseList[position]
            binding.apply {
                courseTitle.text = courseItem.courseTitle
                price.text = courseItem.coursePrice.toString()
                val uriString = courseItem.courseThumbnailUrl.toString()
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(courseImage)

                // Fetch user data associated with this course
                if (courseItem.postedBy != null) {
                    database.reference.child("admin").child(courseItem.postedBy!!).child("profile")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val user = userSnapshot.getValue(UserModel::class.java)
                                user?.let {
                                    Glide.with(context).load(Uri.parse(it.imageUri)).into(imgProfile)
                                    binding.instructorName.text = it.name
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(context, "Failed to load user data", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
            }
        }

    }
}