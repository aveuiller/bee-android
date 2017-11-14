package com.apisense.bee.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.utils.SensorsDrawer
import io.apisense.sdk.core.store.Crop
import kotterknife.bindView

class AvailableExperimentsRecyclerAdapter(private val availableCrops: List<Crop>,
                                          private val itemListener: OnItemClickListener)
    : RecyclerView.Adapter<AvailableExperimentsRecyclerAdapter.ViewHolder>() {
    private lateinit var context: Context
    private lateinit var sensorsDrawer: SensorsDrawer

    interface OnItemClickListener {
        fun onItemClick(crop: Crop)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val apisenseSdk = (context.applicationContext as BeeApplication).sdk
        val mAvailableSensors = apisenseSdk.preferencesManager.retrieveAvailableSensors()
        sensorsDrawer = SensorsDrawer(mAvailableSensors)
        val inflater = LayoutInflater.from(context)
        val cropView = inflater.inflate(R.layout.list_item_store_experiment, parent, false)

        return AvailableExperimentsRecyclerAdapter.ViewHolder(cropView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val crop = availableCrops[position]

        holder.mCropTitle.text = crop.name
        holder.mCropOwner.text = context.getString(R.string.exp_details_organization, crop.owner)
        holder.mCropDescription.text = crop.shortDescription
        holder.mCropVersion.text = context.getString(R.string.exp_details_version, crop.version)

        sensorsDrawer.draw(context, holder.mSensorsContainer, crop.usedStings)

        holder.bind(crop, itemListener)
    }

    override fun getItemCount(): Int {
        return availableCrops.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val mCropTitle: TextView by bindView(R.id.store_item_name)
        internal val mCropOwner: TextView by bindView(R.id.store_item_owner)
        internal val mCropDescription: TextView by bindView(R.id.store_item_description)
        internal val mSensorsContainer: ViewGroup by bindView(R.id.store_sensors_container)
        internal val mCropVersion: TextView by bindView(R.id.store_item_version)

        fun bind(crop: Crop, listener: AvailableExperimentsRecyclerAdapter.OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(crop) }
        }
    }
}
