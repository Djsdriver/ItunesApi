package com.example.itunesapi


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.itunesapi.retrofit.Track

class TrackAdapter: RecyclerView.Adapter<TrackHolder>() {
    var tracks=ArrayList<Track>()
        set(newTracks) {
            val diffCallback = TracksDiffCallback(field, newTracks)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            field = newTracks
            diffResult.dispatchUpdatesTo(this)
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.item_track,parent,false)
        return TrackHolder(view)
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
       holder.bind(tracks[position])
        /*holder.bind(differ.currentList[position])
        holder.setIsRecyclable(false)*/
    }

    override fun getItemCount(): Int =tracks.size

    fun clear(){
        tracks.clear()
        notifyDataSetChanged()
    }

    fun setTrackList(list: List<Track>){
        tracks.clear()
        tracks.addAll(list)
        notifyDataSetChanged()
    }



}