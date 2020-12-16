package com.example.epicture.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.epicture.R
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPrefEditor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = requireActivity().getSharedPreferences("EpictureSharedPref", Context.MODE_PRIVATE)
        sharedPrefEditor = sharedPref.edit()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onStart() {
        super.onStart()
        var clientId: String? = sharedPref.getString(
            "client_id",
            "no client_id"
        )

        if (clientId != "no client_id") {
            var showViral = "true"
            var showMature = "true"
            var albumePreview = "false"
            var url: String = "https://api.imgur.com/3/gallery"
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
                            println(response.body()!!.string())
                        } catch (e: JSONException) {
                            e.printStackTrace();
                        }
                    }
                }
            })
        } else {
            Log.e("Error", "No accountUsername or client_id")
        }
    }
}