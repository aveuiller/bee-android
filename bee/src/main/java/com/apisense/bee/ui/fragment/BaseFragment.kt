package com.apisense.bee.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.apisense.bee.ui.activity.HomeActivity

open class BaseFragment : Fragment() {

    protected lateinit var homeActivity: HomeActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeActivity = activity as HomeActivity
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}
