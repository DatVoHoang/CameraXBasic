/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.example.cameraxbasic.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import java.io.File
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Build
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.example.cameraxbasic.BuildConfig
import com.android.example.cameraxbasic.utils.padWithDisplayCutout
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.navArgs
import com.android.example.cameraxbasic.utils.showImmersive
import com.android.example.cameraxbasic.R
import com.android.example.cameraxbasic.model.*
import com.android.example.cameraxbasic.remote.GoogleVisionService
import kotlinx.android.synthetic.main.fragment_gallery.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


val EXTENSION_WHITELIST = arrayOf("JPG")

/** Fragment used to present the user with a gallery of photos taken */
class GalleryFragment internal constructor() : Fragment() {

    /** AndroidX navigation arguments */
    private val args: GalleryFragmentArgs by navArgs()

    private lateinit var mediaList: MutableList<File>

    /** Google Vision set-up*/

    private var API_KEY = BuildConfig.API_KEY
    private var mService: GoogleVisionService? = null
    private var myBitmap: Bitmap? = null
    //private var imageView = view?.findViewById<ImageView>(R.id.image_view)
    /** Adapter class used to present a fragment containing one photo or video as a page */
    inner class MediaPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = mediaList.size
        override fun getItem(position: Int): Fragment = PhotoFragment.create(mediaList[position])
        override fun getItemPosition(obj: Any): Int = POSITION_NONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Service initial */
        mService = GoogleVisionService.ApiUtils.gvService

        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true

        // Get root directory of media from navigation arguments
        val rootDirectory = File(args.rootDirectory)

        // Walk through all files in the root directory
        // We reverse the order of the list to present the last photos first
        mediaList = rootDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.toUpperCase())
        }.sorted().reversed().toMutableList()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gallery, container, false)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate the ViewPager and implement a cache of two media items
        val mediaViewPager = view.findViewById<ViewPager>(R.id.photo_view_pager).apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }

        // Make sure that the cutout "safe area" avoids the screen notch if any
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Use extension method to pad "inside" view containing UI using display cutout's bounds
            view.findViewById<ConstraintLayout>(R.id.cutout_safe_area).padWithDisplayCutout()
        }

        // Handle back button press
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            fragmentManager?.popBackStack()
        }

        // Handle share button press
        view.findViewById<ImageButton>(R.id.share_button).setOnClickListener {
            // Make sure that we have a file to share
            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                myBitmap = BitmapFactory.decodeFile(mediaFile.absolutePath)
                val bytes = mediaFile.readBytes()
                val base64 = Base64.getEncoder().encodeToString(bytes)
                Log.d("STRING64",base64)
                postRequest(base64)
            }
        }

        // Handle delete button press
        view.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {
            AlertDialog.Builder(view.context, android.R.style.Theme_Material_Dialog)
                    .setTitle(getString(R.string.delete_title))
                    .setMessage(getString(R.string.delete_dialog))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _, _ ->
                        mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                            // Delete current photo
                            mediaFile.delete()

                            // Send relevant broadcast to notify other apps of deletion
                            MediaScannerConnection.scanFile(
                                    view.context, arrayOf(mediaFile.absolutePath), null, null)

                            // Notify our view pager
                            mediaList.removeAt(mediaViewPager.currentItem)
                            mediaViewPager.adapter?.notifyDataSetChanged()

                            // If all photos have been deleted, return to camera
                            if (mediaList.isEmpty()) {
                                fragmentManager?.popBackStack()
                            }
                        }}

                    .setNegativeButton(android.R.string.no, null)
                    .create().showImmersive()
        }
    }
    private fun postRequest(imageUri: String)
    {
        mService!!.getAnnotations(API_KEY,getTestRequest(imageUri))
                .enqueue(object : Callback<VisionResponse> {
                    override fun onResponse(call: Call<VisionResponse>, response: Response<VisionResponse>) {

                            val fdBoundingPoly: FdBoundingPoly = response.body()!!.responses[0].faceAnnotations[0].fdBoundingPoly

                        Log.d("FdBoundingPoly",fdBoundingPoly.toString())
                            drawBoundingPoly(fdBoundingPoly)
                    }

                    override fun onFailure(call: Call<VisionResponse>?, t: Throwable?) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }
                })
    }
    private fun drawBoundingPoly(fdBoundingPoly: FdBoundingPoly)
    {
        val tempBitmap = Bitmap.createBitmap(myBitmap!!.width,myBitmap!!.height, Bitmap.Config.RGB_565)
        if(tempBitmap!= null)
        {
            Log.d("tempBitmap","not null")
        }
        else
            Log.d("tempBitmap","null")
        val tempCanvas = Canvas(tempBitmap)
        val left: Float = fdBoundingPoly.vertices[0].x.toFloat()
        val top: Float = fdBoundingPoly.vertices[0].y.toFloat()
        val right: Float = fdBoundingPoly.vertices[2].x.toFloat()
        val bottom: Float = fdBoundingPoly.vertices[2].y.toFloat()

        tempCanvas.drawBitmap(myBitmap!!,0f,0f,null)
        val rectPaint = Paint()
        rectPaint.strokeWidth = 9f
        rectPaint.color = Color.WHITE
        rectPaint.style = Paint.Style.STROKE

        val rectF = RectF(left,top,right,bottom)
        tempCanvas.drawRect(rectF,rectPaint)

        image_view!!.setImageDrawable(BitmapDrawable(resources,tempBitmap))

        //imageView!!.setImageBitmap(tempBitmap)
    }

    private fun getTestRequest(content:String): VisionRequest
    {
        val list = ArrayList<Requests>()
        list.add(
                Requests(
                        Image(content), listOf(Features("FACE_DETECTION"))
                )
        )
        return VisionRequest(list)
    }
}