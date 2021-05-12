package com.temicanada.asrdemo

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.robotemi.sdk.NlpResult
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.constants.SdkConstants.METADATA_OVERRIDE_NLU
import com.robotemi.sdk.listeners.OnRobotReadyListener
import com.temicanada.asrdemo.model.QuestionAnswer
import com.temicanada.asrdemo.network.APIClient
import com.temicanada.asrdemo.network.ApiInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.logging.Handler
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), OnRobotReadyListener, Robot.AsrListener,
    Robot.NlpListener {

    val sheetLocation: String =
        "https://docs.google.com/spreadsheets/d/1kqyDLJr7m9-g3p1ZgKIuOnS83V4d9u01gCMzLz0b6_E/edit#gid=0"
    val spreadsheetJSON: String =
        "https://sheets.googleapis.com/v4/spreadsheets/1kqyDLJr7m9-g3p1ZgKIuOnS83V4d9u01gCMzLz0b6_E/values/Sheet1!A2:B10?key=AIzaSyDGR_FS6jELBp0i-gP1X0-36s4Lab1AlQY"

    private lateinit var robot: Robot
    private lateinit var webBrowser: WebView
    private val ACTION_OPEN_UOF = "open.uof"
    val questionList: ArrayList<String> = ArrayList()
    val answerList: ArrayList<String> = ArrayList()

    var questionAnswerList = mutableMapOf<String, String>()

    override fun onStart() {
        super.onStart()
        Robot.getInstance().addOnRobotReadyListener(this)
        Robot.getInstance().addAsrListener(this)
        Robot.getInstance().addNlpListener(this)
    }

    override fun onStop() {
        super.onStop()
        Robot.getInstance().removeOnRobotReadyListener(this)
        Robot.getInstance().removeAsrListener(this)
        Robot.getInstance().removeNlpListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webBrowser = findViewById(R.id.webView)
//        var back: Button = findViewById(R.id.button)

        webBrowser.settings.setJavaScriptEnabled(true)
        webBrowser.settings.useWideViewPort = true
        webBrowser.settings.loadWithOverviewMode = true
        webBrowser.settings.setSupportZoom(true)
        webBrowser.settings.setSupportMultipleWindows(true)
        webBrowser.settings.domStorageEnabled = true
        webBrowser.settings.allowContentAccess = true
        webBrowser.settings.allowFileAccess = true
        webBrowser.settings.javaScriptCanOpenWindowsAutomatically = true
        webBrowser.settings.loadsImagesAutomatically = true
        webBrowser.settings.setGeolocationEnabled(true)
        webBrowser.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webBrowser.setBackgroundColor(Color.WHITE)

        webBrowser.loadUrl("https://www.ouac.on.ca/")

        val fab: View = findViewById(R.id.refresh)
        fab.setOnClickListener {

            callAPi()
        }

        callAPi()

    }

    override fun onRobotReady(isReady: Boolean) {
        if (isReady) {
            try {
                val activityInfo =
                    packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA)
                Robot.getInstance().onStart(activityInfo)
                robot = Robot.getInstance()
                requestToBeKioskApp()
            } catch (e: PackageManager.NameNotFoundException) {
                throw RuntimeException(e)
            }
            Robot.getInstance().hideTopBar()

            callAPi()

//            Robot.getInstance().speak(
//                TtsRequest.create(
//                    "Hi,. My Name is Temi. I am your guide today. Just say, \"Hey Temi\" , and your question",
//                    false
//                )
//            )

        }
    }

    /**
     * If you want to cover the voice flow in Launcher OS,
     * please add following meta-data to AndroidManifest.xml.
     * <pre>
     * <meta-data android:name="com.robotemi.sdk.metadata.KIOSK" android:value="true"></meta-data>
     *
     * <meta-data android:name="com.robotemi.sdk.metadata.OVERRIDE_NLU" android:value="true"></meta-data>
     * <pre>
     * And also need to select this App as the Kiosk Mode App in Settings > Kiosk Mode.
     *
     * @param asrResult The result of the ASR after waking up temi.
    </pre></pre> */
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onAsrResult(asrResult: String) {
        printLog("onAsrResult", "asrResult = $asrResult")
        try {
            val metadata = packageManager
                .getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData ?: return
            if (!robot.isSelectedKioskApp()) {
                robot.speak(TtsRequest.create("Please provide me a Kiosk App Permission", false))
                return
            }
            if (!metadata.getBoolean(METADATA_OVERRIDE_NLU)) {
                robot.speak(
                    TtsRequest.create(
                        "override NLU metadata not exist on manifest ",
                        false
                    )
                )
                return
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return
        }

        questionAnswerList.forEach { (k, v) ->
            if (k.equals(asrResult, ignoreCase = true)) {
                println("Test: $k = $v")
                temiSpeak(v)
            }
        }

        when {
//            asrResult.equals("Hello", ignoreCase = true) -> {
//                temiSpeak("Hello, I'm temi, what can I do for you?")
//            }
//            asrResult.equals("What is UOF", ignoreCase = true) -> {
//                temiSpeak("UOF is an innovative French-language institution, shining in an English-speaking world")
//            }
//            asrResult.equals("what are your programs", ignoreCase = true) -> {
//                temiSpeak("UOF’s initial programs of study are unique. Rather than be constrained by the traditional divisions of academic disciplines, our programs address the major issues of the 21st century from a transdisciplinary perspective. This promotes the development of the skills and knowledge necessary to respond to these issues, all while capitalizing on the dynamism of the Toronto area and Ontario economy.")
//            }
            asrResult.equals("When do classes begin", ignoreCase = true) -> {
                temiSpeak("Classes begin in September 2021. Hope to see you there!")
                webBrowser.loadUrl("https://www.ouac.on.ca/guide/101-ontario-francais/#admission")
                //            robot.finishConversation()
            }
            asrResult.equals("Show me Important Dates", ignoreCase = true) -> {
                robot.speak(TtsRequest.create("Okay Here is the list...", false))
                webBrowser.loadUrl("https://www.ouac.on.ca/guide/101-dates/")
                //            robot.finishConversation()
            }
            asrResult.equals("Show me Programs", ignoreCase = true) -> {
                robot.speak(TtsRequest.create("Okay Here is the list of programs..", false))
                webBrowser.loadUrl("https://www.ouac.on.ca/guide/101-ontario-francais/#programs")
                //            robot.finishConversation()
            }
            asrResult.equals("Show me a Contact Information", ignoreCase = true) -> {
                robot.speak(TtsRequest.create("Okay Here it is..", true))
                webBrowser.loadUrl("https://www.ouac.on.ca/guide/101-ontario-francais/#info")
                //            robot.finishConversation()
            }
            asrResult.equals("Show me deadlines list", ignoreCase = true) -> {
                robot.speak(TtsRequest.create("Okay Here is the deadline list...", true))
                webBrowser.loadUrl("https://www.ouac.on.ca/deadlines/")
                //            robot.finishConversation()
            }
            asrResult.toLowerCase(Locale.ROOT).contains("show me library") -> {
                robot.speak(TtsRequest.create("Okay, please follow me.", false))
                robot.goTo("library")
            }
            asrResult.toLowerCase(Locale.ROOT).contains("go to home base") -> {
                robot.finishConversation()
                robot.speak(TtsRequest.create("Okay Going to Home Base", false))
                robot.goTo("home base")
            }
            asrResult.toLowerCase(Locale.ROOT).contains("follow me") -> {
                robot.finishConversation()
                robot.beWithMe()
            }
//            asrResult.equals("open settings", true) -> {
//                robot.startPage(Page.SETTINGS)
//            }
//            asrResult.equals("open contacts", true) -> {
//                robot.startPage(Page.CONTACTS)
//                robot.speak(
//                    TtsRequest.create(
//                        "Please tap on contact to talk with our representative..",
//                        false
//                    )
//                )
//            }
//            asrResult.equals("open locations", true) -> {
//                robot.startPage(Page.LOCATIONS)
//            }
//            asrResult.equals("open map editor", true) -> {
//                robot.startPage(Page.MAP_EDITOR)
//            }
//            asrResult.equals("open home", true) -> {
//                robot.startPage(Page.ALL_APPS)
//            }
            else -> {
//                robot.startDefaultNlu(asrResult)
//                robot.askQuestion("Sorry I can't understand you, could you please ask something else?")
            }
        }
    }

    private fun temiSpeak(string: String) {
//        robot.speak(
//            TtsRequest.create(
//                string,
//                true
//            )
//        )

        robot.askQuestion(string)
    }

    private fun printLog(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    private fun requestToBeKioskApp() {
        if (robot.isSelectedKioskApp()) {
            Toast.makeText(
                this,
                this.getString(R.string.app_name) + " was the selected Kiosk App.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        robot.requestToBeKioskApp()
    }

    override fun onNlpCompleted(nlpResult: NlpResult) {
        var result = nlpResult.action
        when (result) {
            ACTION_OPEN_UOF -> Intent(this, MainActivity::class.java)
        }
    }


    fun callAPi(){

        val apiInterface = APIClient.client.create(ApiInterface::class.java)

        val call = apiInterface.getQuestionAnswer()
        call.enqueue(object : Callback<QuestionAnswer> {
            override fun onResponse(
                call: Call<QuestionAnswer>,
                response: Response<QuestionAnswer>
            ) {
                Log.d("Success!", response.toString())
                var questionAnswer = response.body()
                var itemList = questionAnswer?.values
                questionAnswerList.clear()
                if (itemList != null) {
                    for (i in itemList.indices) {
                        printLog("/n  Question $i --> ", itemList[i][0])
                        printLog("/n  Answer $i --> ", itemList[i][1])
//                            questionList.add(itemList[i][0].toString())

                        questionAnswerList[itemList[i][0]] = itemList[i][1]
                    }

                    println("Q/A /n  $questionAnswerList")
                }
            }

            override fun onFailure(call: Call<QuestionAnswer>, t: Throwable) {
                Log.e("Failed Query :(", t.toString())
                Toast.makeText(
                    applicationContext,
                    "Error: ${t.message.toString()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }
}