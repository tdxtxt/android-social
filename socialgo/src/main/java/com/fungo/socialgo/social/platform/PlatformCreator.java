package com.fungo.socialgo.social.platform;

import android.content.Context;

/**
 * CreateAt : 2018/2/11
 * Describe :
 *
 * @author chendong
 */
public interface PlatformCreator {
    IPlatform create(Context context, int target);
}
