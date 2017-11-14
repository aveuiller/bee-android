package com.apisense.bee.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.callbacks.BeeAPSCallback
import com.apisense.bee.callbacks.OnCropStarted
import com.apisense.bee.utils.CropPermissionHandler
import com.apisense.bee.utils.SensorsDrawer
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.store.Crop
import io.apisense.sting.lib.Sensor
import kotterknife.bindView

open class CommonDetailsFragment : BaseFragment() {

    private val nameView: TextView by bindView(R.id.crop_detail_title)
    private val organizationView: TextView by bindView(R.id.crop_detail_owner_and_version)
    private val descriptionView: TextView by bindView(R.id.crop_detail_description)
    private val stingGridView: ViewGroup by bindView(R.id.crop_sensors_detail_container)

    protected val apisenseSdk: APISENSE.Sdk = (activity.application as BeeApplication).sdk
    protected lateinit var crop: Crop
    protected lateinit var cropPermissionHandler: CropPermissionHandler

    private val availableSensors: Set<Sensor> = apisenseSdk.preferencesManager.retrieveAvailableSensors()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val view = inflater.inflate(R.layout.fragment_common_details, container, false)
        setHasOptionsMenu(true)

        crop = this.arguments.getParcelable("crop")
        cropPermissionHandler = prepareCropPermissionHandler()

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayExperimentInformation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        cropPermissionHandler.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    protected open fun prepareCropPermissionHandler(): CropPermissionHandler =
            CropPermissionHandler(activity, crop, OnCropStarted(activity))

    protected open fun displayExperimentInformation() {
        nameView.text = getString(R.string.exp_details_name, crop.name)
        organizationView.text = "%s - %s"
                .format(getString(R.string.exp_details_organization, crop.owner),
                        getString(R.string.exp_details_version, crop.version))
        descriptionView.text = crop.shortDescription

        SensorsDrawer(availableSensors).draw(context, stingGridView, crop.usedStings)
    }

    protected fun doUpdate() {
        apisenseSdk.cropManager.update(crop.location, object : BeeAPSCallback<Crop>(activity) {
            override fun onDone(crop: Crop) {
                this@CommonDetailsFragment.crop = crop
                displayExperimentInformation()

                Toast.makeText(
                        getActivity(),
                        getString(R.string.experiment_updated, crop.name),
                        Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
