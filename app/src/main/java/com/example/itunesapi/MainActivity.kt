package com.example.itunesapi

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.itunesapi.databinding.ActivityMainBinding


import com.example.itunesapi.retrofit.Track
import com.example.itunesapi.retrofit.TrackApi
import com.example.itunesapi.retrofit.TrackResultResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity(), TrackAdapter.ClickListener{
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val adapter=TrackAdapter(this)
    val adapterHistory=TrackAdapter(this)

    companion object {
        const val KEY_EDIT_TEXT = "KEY_EDIT_TEXT"
        const val BASE_URL = "https://itunes.apple.com"

        private fun viewVisible(s: CharSequence?): Int {
            return if (s.isNullOrBlank()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }




    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_EDIT_TEXT, binding.editTextSearch.toString())
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle?,
        persistentState: PersistableBundle?
    ) {
        super.onRestoreInstanceState(savedInstanceState, persistentState)
        if (savedInstanceState != null) {
            val editText = savedInstanceState.getString(KEY_EDIT_TEXT)
            binding.editTextSearch.setText(editText)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.recyclerViewSearch.layoutManager=LinearLayoutManager(this)
        binding.recyclerViewSearch.adapter=adapter


        binding.rcHistory.adapter=adapterHistory
        binding.rcHistory.layoutManager=LinearLayoutManager(this)
        loadData()



        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
        val retrofit=Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build()
        val itemTrack=retrofit.create(TrackApi::class.java)

        binding.editTextSearch.setOnFocusChangeListener { view, hasFocus ->
            binding.listHistory.visibility = if (hasFocus && binding.editTextSearch.text.isEmpty()) View.VISIBLE else View.GONE
            loadData()
        }
        binding.clearHistory.setOnClickListener {
            binding.editTextSearch.setText("")
            adapterHistory.clear()
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.editTextSearch.windowToken, 0)


        }


        fun showPlaceholder(flag: Boolean?, message: String = "") = with(binding){
            if (flag != null) {
                if (flag == true) {
                    badConnectionWidget.visibility = View.GONE
                    notFoundWidget.visibility = View.VISIBLE
                } else {
                    notFoundWidget.visibility = View.GONE
                    badConnectionWidget.visibility = View.VISIBLE
                    badConnection.text = message
                }
                adapter.clear()
            } else {
                notFoundWidget.visibility = View.GONE
                badConnectionWidget.visibility = View.GONE
            }
        }



        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // ВЫПОЛНЯЙТЕ ПОИСКОВЫЙ ЗАПРОС ЗДЕСЬ
                itemTrack.getTrackByTerm(binding.editTextSearch.text.toString())
                    .enqueue(object : Callback<TrackResultResponse>{
                        override fun onResponse(call: Call<TrackResultResponse>, response: Response<TrackResultResponse>) {
                            when(response.code()) {
                                 HttpCodeResult.SUCCESS.code-> {
                                    if (response.body()?.results?.isNotEmpty() == true) {
                                        adapter.setTrackList(response.body()!!.results)
                                        showPlaceholder(null)
                                        Log.d("MyLog", "${response.code()}")
                                    } else {
                                        showPlaceholder(true)
                                        Log.d("MyLog", "${response.code()}")
                                    }
                                }
                                HttpCodeResult.ERROR.code -> {
                                    showPlaceholder(false, getString(R.string.server_error))
                                    Log.d("MyLog", "${response.code()}")
                                }
                            }

                        }

                        override fun onFailure(call: Call<TrackResultResponse>, t: Throwable) {

                            showPlaceholder(false, getString(R.string.bad_connection))
                            Log.d("MyLog", "${t.message}")
                        }

                    })
                true
            }
            false
        }



        binding.imClearEditText.setOnClickListener {
            binding.editTextSearch.setText("")
            adapter.clear()
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.editTextSearch.windowToken, 0)
        }

        val simpleWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }



            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                when {
                    s.isNullOrEmpty() -> {
                        binding.imClearEditText.visibility = viewVisible(s)
                    }
                    else -> {
                        binding.imClearEditText.visibility = viewVisible(s)
                    }
                }
                binding.listHistory.visibility = if (binding.editTextSearch.hasFocus() && s?.isEmpty() == true) View.VISIBLE else View.GONE
                loadData()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }
        binding.editTextSearch.addTextChangedListener(simpleWatcher)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(track: List<Track>) {
        adapterHistory.setHistoryList(track)
        //
        saveData()
        //Toast.makeText(this,"Name ${track.artistName}", Toast.LENGTH_LONG).show()
    }

    private fun loadData() {

        val sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE)
        val json =sharedPreferences.getString("courses","[]")
        val type = object : TypeToken<List<Track>>() {}.type
        adapterHistory.tracks= Gson().fromJson(json, type)
        Log.d("MyLog1","${adapterHistory.tracks}")

    }


    private fun saveData() {
        val sharedPreferences = getSharedPreferences("shared_preferences", MODE_PRIVATE)
        val json = Gson().toJson(adapterHistory.tracks)
        sharedPreferences.edit().putString("courses", json).apply()
        Toast.makeText(this, "Saved Array List to Shared preferences. ", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        super.onStop()
    }




}