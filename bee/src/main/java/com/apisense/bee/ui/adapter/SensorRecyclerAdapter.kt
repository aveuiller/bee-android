package com.apisense.bee.ui.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import com.apisense.bee.R
import io.apisense.sdk.core.store.Crop
import io.apisense.sting.lib.Sensor
import kotterknife.bindView
import java.util.*

class SensorRecyclerAdapter(val data: List<Sensor>) : RecyclerView.Adapter<SensorRecyclerAdapter.ViewHolder>() {
    private val enabledStings: MutableMap<String, Boolean> = HashMap()
    private lateinit var context: Context

    val disabledSensors: List<String>
        get() {
            val disabled = ArrayList<String>()
            for (stingName in enabledStings.keys) {
                if (!enabledStings.getOrDefault(stingName, false)) {
                    disabled.add(stingName)
                }
            }
            return disabled
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorRecyclerAdapter.ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val cropView = inflater.inflate(R.layout.list_item_sensor, parent, false)

        return SensorRecyclerAdapter.ViewHolder(cropView)
    }

    override fun onBindViewHolder(holder: SensorRecyclerAdapter.ViewHolder, position: Int) {
        val item = data[position]

        holder.title.text = item.name
        holder.title.setTypeface(null, Typeface.BOLD)
        holder.description.text = item.description
        holder.icon.setImageDrawable(ContextCompat.getDrawable(context, item.iconID))
        holder.icon.contentDescription = item.name
        if (enabledStings.containsKey(item.stingName)) {
            holder.enabled.setChecked(enabledStings.getOrDefault(item.stingName, false))
        }
        holder.enabled.setOnCheckedChangeListener(SwitchClickListener(item.stingName))
    }

    override fun getItemCount(): Int = data.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val title: TextView by bindView(R.id.sensor_name)
        internal val description: TextView by bindView(R.id.sensor_description)
        internal val icon: ImageView by bindView(R.id.sensor_icon)
        internal val enabled: SwitchCompat by bindView(R.id.sensor_enabled)

        fun bind(crop: Crop, listener: SubscribedExperimentsRecyclerAdapter.OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(crop) }
        }
    }

    fun setSensorActivation(stingName: String, enabled: Boolean) {
        enabledStings.put(stingName, enabled)
    }

    // Private methods

    private inner class SwitchClickListener(private val stingName: String) : CompoundButton.OnCheckedChangeListener {

        override fun onCheckedChanged(buttonView: CompoundButton, enabled: Boolean) {
            Log.d(TAG, "Setting sting ($stingName) activation to : $enabled")
            setSensorActivation(stingName, enabled)
        }
    }

    companion object {
        private const val TAG = "SensorAdapter"
    }
}
