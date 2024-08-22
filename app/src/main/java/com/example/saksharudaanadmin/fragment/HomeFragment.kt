package com.example.saksharudaanadmin.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.saksharudaanadmin.Adapter.CourseAdapter
import com.example.saksharudaanadmin.R
import com.example.saksharudaanadmin.UploadCourseActivity
import com.example.saksharudaanadmin.databinding.FragmentHomeBinding
import com.example.saksharudaanadmin.model.CourseModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var loadingDialog: Dialog
    private lateinit var courseList:ArrayList<CourseModel>
    private lateinit var courseAdapter:CourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentHomeBinding.inflate(inflater, container, false)

        //initialize firebase database and auth and storage
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        val userId = auth.currentUser?.uid?:""
        val userRef = database.reference.child("admin").child(userId).child("profile")

        // Get current user name from the database
        userRef.child("name").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java) ?: ""
                binding.tvName.text = "Hi, $username"
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


        // Initialize Loading Dialog
        loadingDialog = Dialog(requireContext())
        loadingDialog.setContentView(R.layout.loading_dialog)

        if (loadingDialog.window != null) {

            loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            loadingDialog.setCancelable(false)
        }

        retrieveAndDisplayCourseDetails()



        binding.fab.setOnClickListener {
            startActivity(Intent(requireContext(), UploadCourseActivity::class.java))
        }


        return binding.root
    }

    private fun retrieveAndDisplayCourseDetails() {
        loadingDialog.show()
        val courseRef =  database.reference.child("course").orderByChild("postedBy").equalTo(auth.uid)
        courseList = arrayListOf()
        courseRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded) return // Check if Fragment is still added
                courseList.clear()
                for(courseSnapshot in snapshot.children){
                    val courseItem = courseSnapshot.getValue(CourseModel::class.java)
                    courseItem?.let {
                        courseList.add(it)
                    }
                }
                setAdapter(courseList)
                loadingDialog.dismiss()
            }


            override fun onCancelled(error: DatabaseError) {
                if (!isAdded) return // Check if Fragment is still added
                Toast.makeText(requireContext(),error.message, Toast.LENGTH_SHORT).show()
                loadingDialog.dismiss()
            }

        })
    }

    private fun setAdapter(courseList: ArrayList<CourseModel>) {
        courseAdapter = CourseAdapter(requireContext(), courseList)
        binding.gvHome.layoutManager = GridLayoutManager(context,2)
        binding.gvHome.adapter = courseAdapter
        courseAdapter.notifyDataSetChanged()
    }

}