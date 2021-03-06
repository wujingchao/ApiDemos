/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.app;

import android.app.Activity;

/**
 * This is an empty Activity which demonstrates the use of the android:maxAspectRatio attribute in
 * the AndroidManifest.xml entry for an Activity
 */
public abstract class MaxAspectRatio extends Activity {

    /**
     * Used in the entry for "App/Activity/Max Aspect Ratio/1:1" with an android:maxAspectRatio="1"
     */
    public static class Square extends MaxAspectRatio {
    }

    /**
     * Used in the entry for "App/Activity/Max Aspect Ratio/16:9" with an android:maxAspectRatio="1.77777778"
     */
    public static class SixteenToNine extends MaxAspectRatio {
    }

    /**
     * Used in the entry for "App/Activity/Max Aspect Ratio/Any" with no android:maxAspectRatio attribute
     */
    public static class Any extends MaxAspectRatio {
    }
}
