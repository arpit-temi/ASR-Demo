package com.temicanada.asrdemo.model

import com.google.gson.annotations.SerializedName

data class QuestionAnswer(
    @SerializedName("range")
    var range: String,
    @SerializedName("majorDimension")
    var majorDimension: String,
    @SerializedName("values")
    var values: ArrayList<ArrayList<String>>
//    var values: ArrayList<List<Item>>

)

data class Item(
    var question: String,
    var answer: String
)