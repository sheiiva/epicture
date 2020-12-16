package com.example.epicture.ui.login


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.epicture.MainActivity
import com.example.epicture.R

class LoginActivity : AppCompatActivity() {

    var AUTH: String = "https://api.imgur.com/oauth2/authorize"
    var CLIENT_ID: String = "16d1131b081fbfe"
    var RESPONSE_TYPE: String = "token"

    private lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var url: String = "$AUTH?client_id=$CLIENT_ID&response_type=$RESPONSE_TYPE"
        // Setup SharedPreferences
        sharedPref = getSharedPreferences("EpictureSharedPref", Context.MODE_PRIVATE);
        editor = sharedPref.edit()
        // Add client_id
        editor.putString("client_id", "$CLIENT_ID")
        editor.commit()
        //
        var webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = object: WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                url: String?
            ): Boolean {
                if (url!!.contains("access_token=")) {
                    splitUrl(url)
                }
                return true
            }
        }
        webView.loadUrl(url)
    }

    fun splitUrl(url: String) {
        val myUri = Uri.parse(url.toString().replace('#', '?'))
        var accessToken: String? = myUri.getQueryParameter("access_token")
        var refreshToken: String? = myUri.getQueryParameter("refresh_token")
        var accountUsername: String? = myUri.getQueryParameter("account_username")
        editor.putString("access_token", accessToken)
        editor.putString("refresh_token", refreshToken)
        editor.putString("account_username", accountUsername)
        editor.commit()
        // Go back to MainActivity
        val myIntent: Intent = Intent(this, MainActivity::class.java)
        startActivity(myIntent)
    }
}