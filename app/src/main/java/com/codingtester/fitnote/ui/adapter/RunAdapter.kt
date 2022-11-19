package com.codingtester.fitnote.ui.adapter

import android.icu.util.Calendar
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codingtester.fitnote.data.local.db.Run
import com.codingtester.fitnote.databinding.RunItemBinding
import com.codingtester.fitnote.helper.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.ViewHolder>() {

    inner class ViewHolder(val itemBinding: RunItemBinding) : RecyclerView.ViewHolder(itemBinding.root)

    private val differCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, differCallback)

    fun setList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RunItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemBinding.apply {

            Glide.with(this.imgRun.context)
                .load(run.image)
                .into(imgRun)

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val format = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            txtRunDate.text = format.format(calendar.time)

            val avgSpeed = "${run.avgSpeedInKMH} km/h"
            txtRunAvgSpedd.text = avgSpeed

            val distanceInKM = "${run.distanceInMeters} km"
            txtRunDistance.text = distanceInKM

            txtRunTime.text = TrackingUtility.formatTrackingTime(run.timeInMillis)

            val totalCal = "${run.caloriesBurned} kcal"
            txtRunCal.text = totalCal
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}