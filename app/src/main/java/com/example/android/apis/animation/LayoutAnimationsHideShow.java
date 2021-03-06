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
import android.annotation.TargetApi;
import android.os.Build;
import android.widget.LinearLayout;
import com.example.android.apis.R;

import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

/**
 * This application demonstrates how to use LayoutTransition to automate transition animations
 * as items are hidden or shown in a container. Pressing the "Show Buttons" button while the
 * "Custom Animations" CheckBox is checked causes a crash which blanks out the system wallpaper.
 * (Sometimes?)
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class LayoutAnimationsHideShow extends Activity {
    /**
     * {@code LinearLayout} into which we place the buttons we are hiding or showing.
     */
    ViewGroup container = null;
    /**
     * {@code LayoutTransition} used by our {@code ViewGroup container}, either a default one or the
     * custom one created when we check the "Custom Animations" CheckBox.
     */
    private LayoutTransition mTransitioner;

    /**
     * Called when the activity is starting. First we call our super's implementation of {@code onCreate},
     * then we set our content view to our Layout file layout_animations_hideshow. We initialize our
     * variable {@code CheckBox hideGoneCB} by finding the view with id R.id.hideGoneCB ("Hide (GONE)").
     * We initialize our field {@code ViewGroup container} with a new instance and set its layout params
     * to have both width and height set to MATCH_PARENT. We then loop over {@code int i} from 1 to 4
     * creating a new instance for {@code Button newButton}, setting its text to the string value of
     * {@code i}, adding it to {@code container} and setting its {@code OnClickListener} to an anonymous
     * class whose {@code onClick} override sets the visibility of the button to GONE if {@code hideGoneCB}
     * is checked or to INVISIBLE if it is not checked. When done adding the 4 buttons to {@code container}
     * we call our method {@code resetTransition} which creates a new instance of {@code LayoutTransition}
     * (the default LayoutTransition) for {@code LayoutTransition mTransitioner} and sets it to be the
     * LayoutTransition used by {@code container}. We then initialize {@code ViewGroup parent} by finding
     * the view with id R.id.parent and add {@code container} to it. We initialize {@code Button addButton}
     * by finding the view with id R.id.addNewButton ("Show Buttons") and set its {@code OnClickListener}
     * to an anonymous class whose {@code onClick} override loops through all the children in the view group
     * {@code container} setting their visibility to VISIBLE. We initialize {@code CheckBox customAnimCB}
     * by finding the view with id R.id.customAnimCB ("Custom Animations") and set its OnCheckedChangeListener
     * to either create a  custom LayoutTransition mTransitioner if the CheckBox is checked, or reset it
     * to the default animation if unchecked.
     *
     * @param savedInstanceState always null since onSaveInstanceState is not overridden
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_animations_hideshow);

        final CheckBox hideGoneCB = findViewById(R.id.hideGoneCB);

        container = new LinearLayout(this);
        container.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Add a slew of buttons to the container. We won't add any more buttons at runtime, but
        // will just show/hide the buttons we've already created
        for (int i = 0; i < 4; ++i) {
            Button newButton = new Button(this);
            newButton.setText(String.valueOf(i));
            container.addView(newButton);
            newButton.setOnClickListener(new View.OnClickListener() {
                /**
                 * If the hideGoneCB is checked, the visibility of the Button clicked is
                 * set to GONE, if not it is set to INVISIBLE.
                 *
                 * @param v View which was clicked
                 */
                @Override
                public void onClick(View v) {
                    v.setVisibility(hideGoneCB.isChecked() ? View.GONE : View.INVISIBLE);
                }
            });
        }

        resetTransition();

        ViewGroup parent = findViewById(R.id.parent);
        parent.addView(container);

        Button addButton = findViewById(R.id.addNewButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            /**
             * Set the visibility of all four Button's in the LinearLayout container to
             * VISIBLE.
             *
             * @param v Button which was clicked
             */
            @Override
            public void onClick(View v) {
                for (int i = 0; i < container.getChildCount(); ++i) {
                    View view = container.getChildAt(i);
                    view.setVisibility(View.VISIBLE);
                }
            }
        });

        CheckBox customAnimCB = findViewById(R.id.customAnimCB);
        customAnimCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * This callback either configures the LayoutTransition mTransitioner to perform
             * a custom layout change animation, or resets it to a new LayoutTransition by
             * calling resetTransition. The custom animation is created by first setting the
             * stagger (delay between animations) of the CHANGE_APPEARING and CHANGE_DISAPPEARING
             * layout transitions to 30 milliseconds. It then calls setupCustomAnimations() which
             * puts together a rather complex combination of animations which it then sets in the
             * LayoutTransition mTransitioner (see setupCustomAnimations) The duration of the
             * custom animations if 500 milliseconds, while the default animations is set to
             * 300 milliseconds.
             *
             * @param buttonView CheckBox customAnimCB whose state has changed
             * @param isChecked whether the CheckBox has changed to "custom" (true) or been cleared
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                long duration;
                if (isChecked) {
                    mTransitioner.setStagger(LayoutTransition.CHANGE_APPEARING, 30);
                    mTransitioner.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 30);
                    setupCustomAnimations();
                    duration = 500;
                } else {
                    resetTransition();
                    duration = 300;
                }
                mTransitioner.setDuration(duration);
            }
        });
    }

    /**
     * Create a new default LayoutTransition and set the LinearLayout container to use
     * this LayoutTransition
     */
    private void resetTransition() {
        mTransitioner = new LayoutTransition();
        container.setLayoutTransition(mTransitioner);
    }

    /**
     * This method creates complex CHANGE_APPEARING, CHANGE_DISAPPEARING, APPEARING, and
     * DISAPPEARING animations and configures the LayoutTransition mTransitioner to use them.
     * <p>
     * For the CHANGE_APPEARING (Changing while Adding) part of the animation it defines property
     * value holders to animate property "left" from 0 to 1, "top" from 0 to 1, "right" from 0 to 1,
     * "bottom" from 0 to 1, "scaleX" from 1f to 0f to 1f, "scaleY" from 1f to 0f to 1f. It then
     * creates an ObjectAnimator changeIn for these properties, sets its duration to use the
     * same duration of the current LayoutTransition mTransitioner and sets changeIn to be the
     * CHANGE_APPEARING animation of mTransitioner. It adds an AnimatorListenerAdapter to changeIn
     * which overrides onAnimationEnd and scales the Button added to full size. This animation has
     * has the appearance of a card flipping right to left from the back side to the front side.
     * You can see this animation in action by clicking the SHOW BUTTONS Button after deleting
     * Button's with the "Hide (GONE)" CheckBox checked.
     * <p>
     * For the CHANGE_DISAPPEARING (Changing while Removing) part of the animation it defines an
     * additional PropertyValueHolder for "rotation" constructed of three KeyFrame's (kf0 - a
     * starting value of the rotation of 0f lasting 0f, kf1 - a rotation of 360f degrees lasting
     * .9999f of the frame, and kf2 - an ending rotation of 0f degrees. It combines these in the
     * "rotation" property value holder pvhRotation, then combines the "left", "top", "right", and
     * "bottom" PropertyValuesHolder's used for the CHANGE_APPEARING animation to create the
     * ObjectAnimator changeOut, sets the duration of changeOut to be the same as the current
     * LayoutTransition mTransitioner, and sets changeOut to be the CHANGE_DISAPPEARING animation
     * of mTransitioner. It adds an AnimatorListenerAdapter to changeOut which overrides
     * onAnimationEnd to set the rotation of the Button to 0f degrees. It has the effect of rotating
     * the Button's to the right of the Button removed clockwise when the "Hide (GONE)" CheckBox
     * is checked while moving them into their new positions.
     * <p>
     * For the APPEARING (Adding) part of the animation it creates a simple "rotationY"
     * ObjectAnimator animIn which rotates the Button from 90f degrees to 0f degrees, sets
     * the duration of animIn to be the same as the current LayoutTransition mTransitioner, and
     * sets animIn to be the APPEARING animation of mTransitioner. It add an AnimatorListenerAdapter
     * to animIn which overrides onAnimationEnd to set the rotation of the Button to 0f degrees.
     * It has the effect of rotating the appearing Button's about the y axis when the SHOW BUTTONS
     * button is pressed (after removing a Button or two), starting from sticking directly out of
     * the plane of the View, to flat.
     * <p>
     * For the DISAPPEARING (Removing) part of the animation it creates a simple "rotationX"
     * ObjectAnimator animOut which rotates the Button from 0f degrees (flat) to 90f degrees
     * (sticking out of the plane), sets the duration of animOut to be the same as the current
     * LayoutTransition mTransitioner, and sets animOut to be the DISAPPEARING animation of
     * mTransitioner. It add an AnimatorListenerAdapter to animOut which overrides onAnimationEnd
     * to set the rotation of the Button to 0f degrees. It has the effect of rotating the
     * disappearing Button about the x axis when the Button is clicked starting from flat to
     * sticking vertically out of the plane.
     */
    private void setupCustomAnimations() {
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
        final ObjectAnimator changeIn = ObjectAnimator.ofPropertyValuesHolder(
                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY).
                setDuration(mTransitioner.getDuration(LayoutTransition.CHANGE_APPEARING));
        mTransitioner.setAnimator(LayoutTransition.CHANGE_APPEARING, changeIn);
        changeIn.addListener(new AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. We initialize {@code View view} with the target of
             * our parameter {@code Animator anim} then set both the x and y scaling factor of
             * {@code view} to 1f.
             *
             * @param anim The animation which reached its end.
             */
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
        final ObjectAnimator changeOut = ObjectAnimator.ofPropertyValuesHolder(
                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation).
                setDuration(mTransitioner.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
        mTransitioner.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, changeOut);
        changeOut.addListener(new AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. We initialize {@code View view} with the target of
             * our parameter {@code Animator anim} and set its rotation to 0.
             *
             * @param anim The animation which reached its end.
             */
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotation(0f);
            }
        });

        // Adding
        ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "rotationY", 90f, 0f).
                setDuration(mTransitioner.getDuration(LayoutTransition.APPEARING));
        mTransitioner.setAnimator(LayoutTransition.APPEARING, animIn);
        animIn.addListener(new AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. We initialize {@code View view} with the target of
             * our parameter {@code Animator anim} and set its rotation around the vertical axis to 0.
             *
             * @param anim The animation which reached its end.
             */
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationY(0f);
            }
        });

        // Removing
        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "rotationX", 0f, 90f).
                setDuration(mTransitioner.getDuration(LayoutTransition.DISAPPEARING));
        mTransitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        animOut.addListener(new AnimatorListenerAdapter() {
            /**
             * Notifies the end of the animation. We initialize {@code View view} with the target of
             * our parameter {@code Animator anim} and set its rotation around the horizontal axis to 0.
             *
             * @param anim The animation which reached its end.
             */
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationX(0f);
            }
        });
    }

    // The following are here to silence an error warning.
    @SuppressWarnings("unused")
    public void setLeft(int duh) {
        throw new RuntimeException("I should not be called");
    }
    @SuppressWarnings("unused")
    public void setTop(int duh) {
        throw new RuntimeException("I should not be called");
    }
    @SuppressWarnings("unused")
    public void setRight(int duh) {
        throw new RuntimeException("I should not be called");
    }
    @SuppressWarnings("unused")
    public void setBottom(int duh) {
        throw new RuntimeException("I should not be called");
    }
    @SuppressWarnings("unused")
    public void setScaleX(float duh) {
        throw new RuntimeException("I should not be called");
    }
    @SuppressWarnings("unused")
    public void setScaleY(float duh) {
        throw new RuntimeException("I should not be called");
    }

}