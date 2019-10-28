package com.android.example.cameraxbasic.model
import com.google.gson.annotations.SerializedName

/*
Copyright (c) 2019 Kotlin Data Classes Generated from JSON powered by http://www.json2kotlin.com

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

For support, please feel free to contact me at https://www.linkedin.com/in/syedabsar */


data class FaceAnnotations (

		@SerializedName("boundingPoly") val boundingPoly : BoundingPoly,
		@SerializedName("fdBoundingPoly") val fdBoundingPoly : FdBoundingPoly,
		@SerializedName("landmarks") val landmarks : List<Landmarks>,
		@SerializedName("rollAngle") val rollAngle : Double,
		@SerializedName("panAngle") val panAngle : Double,
		@SerializedName("tiltAngle") val tiltAngle : Double,
		@SerializedName("detectionConfidence") val detectionConfidence : Double,
		@SerializedName("landmarkingConfidence") val landmarkingConfidence : Double,
		@SerializedName("joyLikelihood") val joyLikelihood : String,
		@SerializedName("sorrowLikelihood") val sorrowLikelihood : String,
		@SerializedName("angerLikelihood") val angerLikelihood : String,
		@SerializedName("surpriseLikelihood") val surpriseLikelihood : String,
		@SerializedName("underExposedLikelihood") val underExposedLikelihood : String,
		@SerializedName("blurredLikelihood") val blurredLikelihood : String,
		@SerializedName("headwearLikelihood") val headwearLikelihood : String
)