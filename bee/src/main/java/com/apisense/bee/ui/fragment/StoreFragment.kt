package com.apisense.bee.ui.fragment

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.apisense.bee.BeeApplication
import com.apisense.bee.R
import com.apisense.bee.callbacks.BeeAPSCallback
import com.apisense.bee.callbacks.OnCropStarted
import com.apisense.bee.ui.activity.HomeActivity
import com.apisense.bee.ui.activity.QRScannerActivity
import com.apisense.bee.ui.adapter.AvailableExperimentsRecyclerAdapter
import com.apisense.bee.ui.adapter.DividerItemDecoration
import com.apisense.bee.utils.CropPermissionHandler
import io.apisense.sdk.APISENSE
import io.apisense.sdk.core.store.Crop
import kotterknife.bindView


class StoreFragment : BaseFragment() {

    private var apisenseSdk: APISENSE.Sdk = (activity.application as BeeApplication).sdk
    private var lastCropPermissionHandler: CropPermissionHandler? = null

    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    private val qrCodeButton: FloatingActionButton by bindView(R.id.action_read_qrcode)
    private val recyclerView: RecyclerView by bindView(R.id.store_experiments_list)
    private val emptyListView: TextView by bindView(R.id.store_empty_list)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val root = inflater.inflate(R.layout.fragment_store, container, false)

        homeActivity.supportActionBar?.setTitle(R.string.title_activity_store)
        homeActivity.selectDrawerItem(HomeActivity.DRAWER_STORE_IDENTIFIER)

        recyclerView.setHasFixedSize(true) // Performances
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(DividerItemDecoration(activity))

        qrCodeButton.setOnClickListener { installCropFromQRCode(qrCodeButton) }
        getExperiments()

        return root
    }

    internal fun installCropFromQRCode(view: View) {
        if (cameraPermissionGranted()) {
            installFromQRCode()
        } else {
            val permissions = arrayOf(android.Manifest.permission.CAMERA)
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSION_QR_CODE)
        }
    }

    /**
     * Change the adapter dataSet with a newly fetched List of Experiment
     *
     * @param experiments The new list of experiments to show
     */
    private fun setExperiments(experiments: List<Crop>) {
        mAdapter = AvailableExperimentsRecyclerAdapter(experiments, object : AvailableExperimentsRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(crop: Crop) {
                val extra = Bundle()
                extra.putParcelable("crop", crop)

                val storeDetailsFragment = StoreDetailsFragment()
                storeDetailsFragment.arguments = extra
                activity.supportFragmentManager.beginTransaction()
                        .replace(R.id.exp_container, storeDetailsFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit()
            }
        })
        recyclerView.adapter = mAdapter
    }

    private fun getExperiments() {
        apisenseSdk.storeManager.findAllCrops(OnExperimentsRetrieved(activity))
    }

    // Callbacks definitions

    private inner class OnExperimentsRetrieved(activity: Activity) : BeeAPSCallback<List<Crop>>(activity) {

        override fun onDone(crops: List<Crop>) {
            Log.i(TAG, "Number of Active Experiments: " + crops.size)
            if (crops.size > 0) {
                emptyListView.visibility = View.GONE
            }
            setExperiments(crops)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        lastCropPermissionHandler?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun cameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun installFromQRCode() {
        val qrActivity = Intent(activity, QRScannerActivity::class.java)
        startActivityForResult(qrActivity, QRScannerActivity.INSTALL_FROM_QR)
    }

    override fun onActivityResult(request: Int, response: Int, data: Intent?) {
        super.onActivityResult(request, response, data)
        // React only if the user actually scanned a QRcode
        if (request == QRScannerActivity.INSTALL_FROM_QR && response == RESULT_OK) {
            val cropID = data!!.getStringExtra(QRScannerActivity.CROP_ID_KEYWORD)
            apisenseSdk.cropManager.installOrUpdate(cropID, object : BeeAPSCallback<Crop>(activity) {
                override fun onDone(crop: Crop) {
                    lastCropPermissionHandler = CropPermissionHandler(getActivity(), crop, OnCropStarted(getActivity()))
                    lastCropPermissionHandler!!.startOrRequestPermissions()
                }

                override fun onError(e: Exception) {
                    super.onError(e)
                    Toast.makeText(
                            getActivity(),
                            e.message,
                            Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    companion object {
        private const val TAG = "StoreFragment"
        private const val REQUEST_PERMISSION_QR_CODE = 1
    }
}
