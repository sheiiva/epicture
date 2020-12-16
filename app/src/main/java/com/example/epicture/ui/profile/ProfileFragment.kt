package com.example.epicture.ui.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.epicture.R
import com.example.epicture.ui.login.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*


class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences(
            "EpictureSharedPref",
            Context.MODE_PRIVATE
        )
        sharedPrefEditor = sharedPref.edit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel =
                ViewModelProvider(this).get(ProfileViewModel::class.java)
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navView: BottomNavigationView = profile_nav_view

        val navController = requireActivity().findNavController(R.id.profile_nav_host_fragment)
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_profile_posts, R.id.navigation_profile_favorites))
        navView.setupWithNavController(navController)
    }

    override fun onStart() {
        super.onStart()
        if (sharedPref.getString("access_token", "no_token") == "no_token") {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        } else {
            getUserInfo()
            bio_textbox.text = sharedPref.getString("bio", "")
            username_box.text = sharedPref.getString("url", "[USER]")
            created_date.text = toDate(sharedPref.getString("created", "0"))
            reputation.text = sharedPref.getString("reputation", "0") + " points - " + sharedPref.getString("reputation_name", "neutral")

            val url: String = sharedPref.getString("avatar", "no avatar")
            if (url != "no avatar")
                Picasso.get()
                    .load(url)
                    .resize(150, 150)
                    .into(profile_picture)
        }
    }

     private fun toDate(date: String) : String {
         // Create date object
         var dateObject = Date(date.toLong() * 1000)
         // Parse current date
         var splitDate = dateObject.toString().split(" ")
         return "Created on " + splitDate[0] + " " + splitDate[1] + " " + splitDate[2] + ", " + splitDate[5]
     }

    private fun getUserInfo() {
        var accountUsername: String? = sharedPref.getString(
            "account_username",
            "no account_username"
        )
        var clientId: String? = sharedPref.getString(
            "client_id",
            "no client_id"
        )

        if (accountUsername != "no account_username" && clientId != "no client_id") {
            var url: String = "https://api.imgur.com/3/account/$accountUsername"
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Client-ID $clientId")
                .build()

            var client: OkHttpClient = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")

                        val json = JSONObject(response.body()!!.string())
                        try {
                            val data = JSONObject(json.getString("data").toString())
                            sharedPrefEditor.putString("id", data.getString("id"))
                            sharedPrefEditor.putString("url", data.getString("url"))
                            sharedPrefEditor.putString("bio", data.getString("bio"))
                            sharedPrefEditor.putString("avatar", data.getString("avatar"))
                            sharedPrefEditor.putString("reputation", data.getString("reputation"))
                            sharedPrefEditor.putString("reputation_name", data.getString("reputation_name"))
                            sharedPrefEditor.putString("created", data.getString("created"))
                            sharedPrefEditor.putString("pro_expiration", data.getString("pro_expiration"))
                            sharedPrefEditor.commit()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                }
            })
        } else {
            Log.e("Error", "No accountUsername or client_id")
        }
    }
}