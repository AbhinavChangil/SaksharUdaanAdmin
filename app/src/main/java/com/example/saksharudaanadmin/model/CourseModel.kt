package com.example.saksharudaanadmin.model

import android.net.Uri

data class CourseModel(
    var courseThumbnailUrl: String? = "",
    var courseVideoUrl: String? = "",
    var courseTitle: String? = null,
    var courseDescription: String? = null,
    var courseDuration: String? = null,
    var coursePrice: String? = null,
    var postId: String? = "",
    var postedBy: String? = "",
    var enable: String? = null
)
