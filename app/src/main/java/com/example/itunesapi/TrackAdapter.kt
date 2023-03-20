package com.example.itunesapi


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.itunesapi.retrofit.Track

class TrackAdapter: RecyclerView.Adapter<TrackHolder>() {
    var tracks=ArrayList<Track>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_track,parent,false)
        return TrackHolder(view)
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int =tracks.size

    fun clear(){
        tracks.clear()
        notifyDataSetChanged()
    }



}