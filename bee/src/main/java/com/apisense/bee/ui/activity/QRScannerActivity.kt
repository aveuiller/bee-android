package com.apisense.bee.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log

import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView

class QRScannerActivity : Activity(), ZBarScannerView.ResultHandler {
    private var scannerView: ZBarScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        scannerView = ZBarScannerView(this)
        setContentView(scannerView)
    }

    public override fun onResume() {
        super.onResume()
        // Register ourselves as a handler for scan results.
        scannerView!!.setResultHandler(this)
        scannerView!!.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        scannerView!!.stopCamera()
    }

    override fun handleResult(result: Result) {
        val cropID = result.contents
        Log.d(TAG, "Got value in QR code: " + cropID)
        val data = Intent()
        data.putExtra(CROP_ID_KEYWORD, cropID)
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    companion object {
        private val TAG = "QRScannerActivity"
        val INSTALL_FROM_QR = 0
        val CROP_ID_KEYWORD = "crop_id"
    }
}
