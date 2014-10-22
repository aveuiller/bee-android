package com.apisense.bee.ui.activity;

import com.robotium.solo.Solo;
import android.test.ActivityInstrumentationTestCase2;

public class SlideshowActivityTest extends ActivityInstrumentationTestCase2<SlideshowActivity> {

    private Solo activity;

    public SlideshowActivityTest() {
        super(SlideshowActivity.class);
    }

    public void setUp() {
        activity = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown()  {
        activity.finishOpenedActivities();
    }

    // - - - TESTS HERE

    public void testSkipButton() {
        activity.clickOnButton("Skip");
    }

}