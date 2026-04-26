package com.example.fastconnect.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.fastconnect.R
import com.example.fastconnect.models.Society

class SocietyAdapter(
    private val societies: List<Society>,
    private val onFollowClick: (Society) -> Unit
) : RecyclerView.Adapter<SocietyAdapter.SocietyViewHolder>() {

    inner class SocietyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSocietyName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvSocietyDesc)
        val btnFollow: Button = itemView.findViewById(R.id.btnFollow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SocietyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_society, parent, false)
        return SocietyViewHolder(view)
    }

    override fun onBindViewHolder(holder: SocietyViewHolder, position: Int) {
        val society = societies[position]
        holder.tvName.text = society.name
        holder.tvDesc.text = society.description
        
        holder.btnFollow.setOnClickListener {
            onFollowClick(society)
            holder.btnFollow.text = "Followed"
            holder.btnFollow.isEnabled = false
        }
    }

    override fun getItemCount(): Int = societies.size
}
