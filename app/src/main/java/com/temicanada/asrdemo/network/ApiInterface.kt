package com.temicanada.asrdemo.network

import com.temicanada.asrdemo.model.QuestionAnswer
import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {
    @GET("1kqyDLJr7m9-g3p1ZgKIuOnS83V4d9u01gCMzLz0b6_E/values/Sheet1!A2:B10?key=AIzaSyDGR_FS6jELBp0i-gP1X0-36s4Lab1AlQY")
    fun getQuestionAnswer() : Call<QuestionAnswer>
}