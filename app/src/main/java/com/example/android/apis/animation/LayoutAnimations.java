/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.example.android.apis.animation;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import android.animation.Animator;
import android.animation.ObjectAnimator;
import com.example.android.apis.R;

import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

/**
 * This application demonstrates how to use LayoutTransition to automate transition animations
 * as items are removed from or added to a container.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LayoutAnimations extends Activity {

    private static final String TAG = "LayoutAnimations";
    private int numButtons = 1;
    ViewGroup container = null;
    Animator defaultAppearingAnim, defaultDisappearingAnim;
    Animator defaultChangingAppearingAnim, defaultChangingDisappearingAnim;
    Animator customAppearingAnim, customDisappearingAnim;
    Animator customChangingAppearingAnim, customChangingDisappearingAnim;
    Animator currentAppearingAnim, currentDisappearingAnim;
    Animator currentChangingAppearingAnim, currentChangingDisappearingAnim;

    /** Called when the activity is first created. */
    /**
     * Sets the content view to layout_animations. Creates a FixedGridLayout container instance
     * and configures its cell height, and cell width, creates LayoutTransition transitioner with
     * default animations, sets it as the LayoutTransition for container, and squirrels away the
     * default animations for later use. It then calls the method createCustomAnimations to create
     * custom animations using its argument transitioner only to fetch the default value of the
     * duration of the animations (see createCustomAnimations). setupTransition() is used to
     * switch LayoutTransition transitioner between the default and custom animations for the four
     * different animations used in a layout transition based on the state of the CheckBox's and is
     * only called when the state of one of the 5 CheckBox's changes (the CheckBox for choosing
     * custom instead of default animations, as well as the 4 CheckBox's selecting "In", "Out",
     * "Changing-In" and "Changing-Out" animations or disabling them if un-checked."
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_animations);

        container = new FixedGridLayout(this);
        container.setClipChildren(false);
        ((FixedGridLayout)container).setCellHeight(200);
        ((FixedGridLayout)container).setCellWidth(400);
        final LayoutTransition transitioner = new LayoutTransition();
        container.setLayoutTransition(transitioner);
        defaultAppearingAnim = transitioner.getAnimator(LayoutTransition.APPEARING);
        defaultDisappearingAnim =
                transitioner.getAnimator(LayoutTransition.DISAPPEARING);
        defaultChangingAppearingAnim =
                transitioner.getAnimator(LayoutTransition.CHANGE_APPEARING);
        defaultChangingDisappearingAnim =
                transitioner.getAnimator(LayoutTransition.CHANGE_DISAPPEARING);
        createCustomAnimations(transitioner);
        currentAppearingAnim = defaultAppearingAnim;
        currentDisappearingAnim = defaultDisappearingAnim;
        currentChangingAppearingAnim = defaultChangingAppearingAnim;
        currentChangingDisappearingAnim = defaultChangingDisappearingAnim;

        ViewGroup parent = (ViewGroup) findViewById(R.id.parent);
        parent.addView(container);
        parent.setClipChildren(false);
        Button addButton = (Button) findViewById(R.id.addNewButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Creates a new Button using this activity as its context, sets the minimum height
             * to 64 pixels and minimum width to 64 pixels, sets the text displayed in the button
             * to the number of buttons created and increments that number, sets the OnClickListener
             * of the Button to remove itself when clicked. It then adds the Button to the ViewGroup
             * (FixedGridLayout) container at position 1 (or 0 if no Button's have been created yet.
             *
             * @param v addButton View when it is clicked
             */
            @Override
            public void onClick(View v) {
                Button newButton = new Button(LayoutAnimations.this);
                newButton.setMinHeight(64);
                newButton.setMinWidth(64);
                newButton.setText(String.valueOf(numButtons++));
                newButton.setOnClickListener(new View.OnClickListener() {
                    /**
                     * Removes the Button when it is clicked
                     *
                     * @param v Button View which was clicked
                     */
                    @Override
                    public void onClick(View v) {
                        container.removeView(v);
                    }
                });
                container.addView(newButton, Math.min(1, container.getChildCount()));
            }
        });

        CheckBox customAnimCB = (CheckBox) findViewById(R.id.customAnimCB);
        customAnimCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Custom Checkbox");
                setupTransition(transitioner);
            }
        });

        // Check for disabled animations
        CheckBox appearingCB = (CheckBox) findViewById(R.id.appearingCB);
        appearingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Appearing Checkbox");
                setupTransition(transitioner);
            }
        });
        CheckBox disappearingCB = (CheckBox) findViewById(R.id.disappearingCB);
        disappearingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Disappearing Checkbox");
                setupTransition(transitioner);
            }
        });
        CheckBox changingAppearingCB = (CheckBox) findViewById(R.id.changingAppearingCB);
        changingAppearingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Changing Appearing Checkbox");
                setupTransition(transitioner);
            }
        });
        CheckBox changingDisappearingCB = (CheckBox) findViewById(R.id.changingDisappearingCB);
        changingDisappearingCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "Changing Disappearing Checkbox");
                setupTransition(transitioner);
            }
        });
    }

    private void setupTransition(LayoutTransition transition) {
        CheckBox customAnimCB = (CheckBox) findViewById(R.id.customAnimCB);
        CheckBox appearingCB = (CheckBox) findViewById(R.id.appearingCB);
        CheckBox disappearingCB = (CheckBox) findViewById(R.id.disappearingCB);
        CheckBox changingAppearingCB = (CheckBox) findViewById(R.id.changingAppearingCB);
        CheckBox changingDisappearingCB = (CheckBox) findViewById(R.id.changingDisappearingCB);
        transition.setAnimator(LayoutTransition.APPEARING, appearingCB.isChecked() ?
                (customAnimCB.isChecked() ? customAppearingAnim : defaultAppearingAnim) : null);
        transition.setAnimator(LayoutTransition.DISAPPEARING, disappearingCB.isChecked() ?
                (customAnimCB.isChecked() ? customDisappearingAnim : defaultDisappearingAnim) : null);
        transition.setAnimator(LayoutTransition.CHANGE_APPEARING, changingAppearingCB.isChecked() ?
                (customAnimCB.isChecked() ? customChangingAppearingAnim :
                        defaultChangingAppearingAnim) : null);
        transition.setAnimator(LayoutTransition.CHANGE_DISAPPEARING,
                changingDisappearingCB.isChecked() ?
                (customAnimCB.isChecked() ? customChangingDisappearingAnim :
                        defaultChangingDisappearingAnim) : null);
    }

    private void createCustomAnimations(LayoutTransition transition) {
        // Changing while Adding
        PropertyValuesHolder pvhLeft =
                PropertyValuesHolder.ofInt("left", 0, 1);
        PropertyValuesHolder pvhTop =
                PropertyValuesHolder.ofInt("top", 0, 1);
        PropertyValuesHolder pvhRight =
                PropertyValuesHolder.ofInt("right", 0, 1);
        PropertyValuesHolder pvhBottom =
                PropertyValuesHolder.ofInt("bottom", 0, 1);
        PropertyValuesHolder pvhScaleX =
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f);
        PropertyValuesHolder pvhScaleY =
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f);
        customChangingAppearingAnim = ObjectAnimator.ofPropertyValuesHolder(
                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY).
                setDuration(transition.getDuration(LayoutTransition.CHANGE_APPEARING));
        customChangingAppearingAnim.addListener(new AnimatorListenerAdapter() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setScaleX(1f);
                view.setScaleY(1f);
            }
        });

        // Changing while Removing
        Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        Keyframe kf1 = Keyframe.ofFloat(.9999f, 360f);
        Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder pvhRotation =
                PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        customChangingDisappearingAnim = ObjectAnimator.ofPropertyValuesHolder(
                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation).
                setDuration(transition.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
        customChangingDisappearingAnim.addListener(new AnimatorListenerAdapter() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotation(0f);
            }
        });

        // Adding
        customAppearingAnim = ObjectAnimator.ofFloat(null, "rotationY", 90f, 0f).
                setDuration(transition.getDuration(LayoutTransition.APPEARING));
        customAppearingAnim.addListener(new AnimatorListenerAdapter() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationY(0f);
            }
        });

        // Removing
        customDisappearingAnim = ObjectAnimator.ofFloat(null, "rotationX", 0f, 90f).
                setDuration(transition.getDuration(LayoutTransition.DISAPPEARING));
        customDisappearingAnim.addListener(new AnimatorListenerAdapter() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationX(0f);
            }
        });

    }
}