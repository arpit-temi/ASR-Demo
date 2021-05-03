package com.temicanada.asrdemo

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.robotemi.sdk.Robot
import com.robotemi.sdk.TtsRequest
import com.robotemi.sdk.constants.SdkConstants.METADATA_OVERRIDE_NLU
import com.robotemi.sdk.listeners.OnRobotReadyListener

class MainActivity : AppCompatActivity(), OnRobotReadyListener, Robot.AsrListener {

    lateinit var robot: Robot
    lateinit var webBrowser: WebView

    override fun onStart() {
        super.onStart()
        Robot.getInstance().addOnRobotReadyListener(this)
        Robot.getInstance().addAsrListener(this)
    }

    override fun onStop() {
        super.onStop()
        Robot.getInstance().removeOnRobotReadyListener(this)
        Robot.getInstance().removeAsrListener(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webBrowser = findViewById(R.id.webView)
//        var back: Button = findViewById(R.id.button)

        webBrowser.settings.setJavaScriptEnabled(true);
        webBrowser.settings.useWideViewPort = true;
        webBrowser.settings.loadWithOverviewMode = true;
        webBrowser.settings.setSupportZoom(true);
        webBrowser.settings.setSupportMultipleWindows(true);
        webBrowser.settings.domStorageEnabled = true;
        webBrowser.settings.allowContentAccess = true;
        webBrowser.settings.allowFileAccess = true;
        webBrowser.settings.javaScriptCanOpenWindowsAutomatically = true;
        webBrowser.settings.loadsImagesAutomatically = true;
        webBrowser.settings.setGeolocationEnabled(true);
        webBrowser.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY;
        webBrowser.setBackgroundColor(Color.WHITE);

        webBrowser.loadUrl("https://www.ouac.on.ca/")
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

            Robot.getInstance().speak(
                TtsRequest.create(
                    "Hi,. My Name is Temi. I am your guide today. Just say, \"Hey Temi\" , and your question",
                    false
                )
            )

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


        if (asrResult.equals("Hello", ignoreCase = true)) {
            robot.askQuestion("Hello, I'm temi, what can I do for you?")
        } else if (asrResult.equals("Play music", ignoreCase = true)) {
            robot.speak(TtsRequest.create("Okay, please enjoy.", false))
//            robot.finishConversation()
        } else if (asrResult.equals("What is UOF", ignoreCase = true)) {
            robot.speak(
                TtsRequest.create(
                    "UOF is an innovative French-language institution, shining in an English-speaking world",
                    true
                )
            )
        } else if (asrResult.equals("what are your programs", ignoreCase = true)) {
            robot.speak(
                TtsRequest.create(
                    "UOF’s initial programs of study are unique. Rather than be constrained by the traditional divisions of academic disciplines, our programs address the major issues of the 21st century from a transdisciplinary perspective. This promotes the development of the skills and knowledge necessary to respond to these issues, all while capitalizing on the dynamism of the Toronto area and Ontario economy.",
                    true
                )
            )
        }
        else if (asrResult.equals("When do classes begin", ignoreCase = true)) {
            robot.speak(TtsRequest.create("Classes begin in September 2021. Hope to see you there!, Please check my screen if you are not sure...", true))
            webBrowser.loadUrl("https://www.ouac.on.ca/guide/101-ontario-francais/#admission")
//            robot.finishConversation()
        }
        else if (asrResult.equals("Give me Important Dates", ignoreCase = true)) {
            robot.speak(TtsRequest.create("Okay Here is the list...", true))
            webBrowser.loadUrl("https://www.ouac.on.ca/guide/101-dates/")
//            robot.finishConversation()
        }
        else if (asrResult.equals("Give me a Contact Information", ignoreCase = true)) {
            robot.speak(TtsRequest.create("Okay Here it is..", true))
            webBrowser.loadUrl("https://www.ouac.on.ca/guide/101-ontario-francais/#info")
//            robot.finishConversation()
        }
        else if (asrResult.equals("Play movie", ignoreCase = true)) {
            robot.speak(TtsRequest.create("Okay, please enjoy.", false))
            robot.finishConversation()
        } else if (asrResult.toLowerCase().contains("follow me")) {
            robot.finishConversation()
            robot.beWithMe()
        } else if (asrResult.toLowerCase().contains("go to home base")) {
            robot.finishConversation()
            robot.goTo("home base")
        } else if (asrResult.toLowerCase().contains("go to entrance")) {
            robot.finishConversation()
            robot.goTo("entrance")
        } else {
            robot.askQuestion("Sorry I can't understand you, could you please ask something else?")
        }
    }

    private fun printLog(tag: String, msg: String) {
        Log.d(tag, msg)
    }

    fun requestToBeKioskApp() {
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
}