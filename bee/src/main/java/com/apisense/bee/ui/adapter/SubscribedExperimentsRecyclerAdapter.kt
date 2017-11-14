package com.apisense.bee.ui.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.utils.SensorsDrawer
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.store.Crop
import kotterknife.bindView

class SubscribedExperimentsRecyclerAdapter(private var installedCrops: List<Crop>,
                                           private val itemListener: OnItemClickListener)
    : RecyclerView.Adapter<SubscribedExperimentsRecyclerAdapter.ViewHolder>() {
    private lateinit var context: Context
    private lateinit var sensorsDrawer: SensorsDrawer
    private lateinit var apisenseSdk: APISENSE.Sdk

    constructor(listener: OnItemClickListener) : this(emptyList<Crop>(), listener) {}

    interface OnItemClickListener {
        fun onItemClick(crop: Crop)
    }

    fun setInstalledCrops(installedCrops: List<Crop>) {
        this.installedCrops = installedCrops
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscribedExperimentsRecyclerAdapter.ViewHolder {
        context = parent.context
        apisenseSdk = (context.applicationContext as BeeApplication).sdk
        val mAvailableSensors = apisenseSdk.preferencesManager.retrieveAvailableSensors()
        sensorsDrawer = SensorsDrawer(mAvailableSensors)

        val inflater = LayoutInflater.from(context)
        val cropView = inflater.inflate(R.layout.list_item_home_experiment, parent, false)

        return ViewHolder(cropView)
    }

    override fun onBindViewHolder(holder: SubscribedExperimentsRecyclerAdapter.ViewHolder, position: Int) {
        val crop = installedCrops[position]

        holder.mCropTitle.text = crop.name
        holder.mCropOwner.text = context.getString(R.string.exp_details_organization, crop.owner)
        holder.mCropDescription.text = crop.shortDescription

        if (apisenseSdk.cropManager.isRunning(crop)) {
            holder.mCropStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_play_blck))
        } else {
            holder.mCropStatus.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_action_break_blck))
        }
        holder.mCropStatus.alpha = ALPHA_STATUS_ICON

        sensorsDrawer.draw(context, holder.mSensorsContainer, crop.usedStings)

        holder.bind(crop, itemListener)
    }

    override fun getItemCount(): Int {
        return installedCrops.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val mCropStatus: ImageView by bindView(R.id.crop_status)
        internal val mCropTitle: TextView by bindView(R.id.crop_title)
        internal val mCropOwner: TextView by bindView(R.id.crop_owner)
        internal val mCropDescription: TextView by bindView(R.id.crop_description)
        internal val mSensorsContainer: ViewGroup by bindView(R.id.sensors_container)

        fun bind(crop: Crop, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(crop) }
        }
    }

    companion object {
        private const val ALPHA_STATUS_ICON = 0.5f
    }
}
