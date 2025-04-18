package com.example.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.artbookkotlin.databinding.RecyclerRowBinding

class ArtAdapter(private val artList: ArrayList<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    // ViewHolder tanımı
    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        val art = artList[position]

        // TextView'e sanat eseri adını yaz
        holder.binding.recyclerViewTextView.text = art.name

        // Tıklanınca detay ekranına git
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ArtActivity::class.java)
            intent.putExtra("info", "old") // detay ekranı olduğunu belirt
            intent.putExtra("id", art.id)  // hangi sanat eseri olduğunu belirt
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }
}
