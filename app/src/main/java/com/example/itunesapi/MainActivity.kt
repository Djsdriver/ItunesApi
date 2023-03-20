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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    val adapter=TrackAdapter()
    val listSong= ArrayList<Track>()

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

    val retrofit=Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    val itemTrack=retrofit.create(TrackApi::class.java)



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
        adapter.tracks=listSong



        binding.editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // ВЫПОЛНЯЙТЕ ПОИСКОВЫЙ ЗАПРОС ЗДЕСЬ
                itemTrack.getTrackByTerm(binding.editTextSearch.text.toString())
                    .enqueue(object : Callback<TrackResultResponse>{
                        override fun onResponse(call: Call<TrackResultResponse>, response: Response<TrackResultResponse>) {
                            when(response.code()) {
                                200 -> {
                                    if (response.body()?.results?.isNotEmpty() == true) {
                                        listSong.clear()
                                        binding.recyclerViewSearch.visibility=View.VISIBLE
                                        binding.placeHolder.visibility=View.GONE
                                        listSong.addAll(response.body()?.results!!)
                                        adapter.notifyDataSetChanged()
                                    } else {
                                        binding.recyclerViewSearch.visibility=View.GONE
                                        binding.placeHolder.visibility=View.VISIBLE
                                    }
                                }
                                else -> {
                                    binding.recyclerViewSearch.visibility=View.GONE
                                    binding.placeHolder.visibility=View.VISIBLE
                                }
                            }

                        }

                        override fun onFailure(call: Call<TrackResultResponse>, t: Throwable) {
                            binding.recyclerViewSearch.visibility=View.GONE
                            binding.placeHolder.visibility=View.VISIBLE
                        }

                    })
                true
            }
            false
        }

       fun showMessage(text: String, additionalMessage: String) {
            if (text.isNotEmpty()) {
                listSong.clear()
                adapter.notifyDataSetChanged()
                if (additionalMessage.isNotEmpty()) {
                    Toast.makeText(applicationContext, additionalMessage, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }



        binding.imClearEditText.setOnClickListener {
            binding.editTextSearch.setText("")
            listSong.clear()
            adapter.notifyDataSetChanged()
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



}