package com.apisense.bee.games.action;

import com.google.android.gms.games.achievement.Achievement;

/**
 * Created by Warnant on 19-02-15.
 */
public class ShareAceAchievement extends GameAchievement {

    public ShareAceAchievement(Achievement achievement) {
        super(achievement);
    }

    @Override
    public boolean process() {
        return true;
    }

    @Override
    public int getScore() {
        return 1;
    }
}

