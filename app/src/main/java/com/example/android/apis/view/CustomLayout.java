/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.example.android.apis.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Rect;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import com.example.android.apis.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;

/**
 * Example of writing a custom layout manager. This is a fairly full-featured
 * layout manager that is relatively general, handling all layout cases. You
 * can simplify it for more specific cases.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@RemoteViews.RemoteView
public class CustomLayout extends ViewGroup {
    /**
     * The amount of space used by children in the left gutter.
     */
    private int mLeftWidth;

    /**
     * The amount of space used by children in the right gutter.
     */
    private int mRightWidth;

    /**
     * These are used for computing child frames based on their gravity.
     * The frame of the containing space, in which the object will be placed.
     */
    private final Rect mTmpContainerRect = new Rect();
    /**
     * Receives the computed frame of the object in its container.
     */
    private final Rect mTmpChildRect = new Rect();

    /**
     * Our constructor, simply calls our super's constructor.
     *
     * @param context {@code Context} to use to access resources
     */
    public CustomLayout(Context context) {
        super(context);
    }

    /**
     * Constructor that is called when inflating from xml.
     *
     * @param context The Context the view is running in, through which it can
     *                access the current theme, resources, etc.
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public CustomLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Perform inflation from XML and apply a class-specific base style from a
     * theme attribute or style resource.
     *
     * @param context  The Context the view is running in, through which it can
     *                 access the current theme, resources, etc.
     * @param attrs    The attributes of the XML tag that is inflating the view.
     * @param defStyle An attribute in the current theme that contains a
     *                 reference to a style resource that supplies default values for
     *                 the view. Can be 0 to not look for defaults.
     */
    public CustomLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Any layout manager that doesn't scroll will want to implement this. We return false to the
     * caller because we do not scroll.
     */
    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this layout based on
     * the children. This method is invoked by {@code measure(int, int)} and should be overridden
     * by subclasses to provide accurate and efficient measurement of their contents.
     * <p>
     * First we initialize our variable {@code int count} with the number of children we have. Then
     * we initialize our fields {@code mLeftWidth} and {@code mRightWidth} to 0. Next we initialize
     * our variables {@code int maxHeight}, {@code int maxWidth}, and {@code int childState} to 0.
     * <p>
     * Now we loop over all {@code count} of our children using {@code i} as the index. We initialize
     * our variable {@code View child} with the child at position {@code i}, and if the visibility of
     * {@code child} is not GONE we ask {@code child} to measure itself given the {@code widthMeasureSpec}
     * and {@code heightMeasureSpec} passed us. Next we initialize {@code LayoutParams lp} with the
     * layout parameters of {@code child}. If the {@code position} field of {@code lp} is POSITION_LEFT
     * the child needs to go in the left gutter so we add the maximum of {@code maxWidth} and the
     * measured width of {@code child} plus the {@code leftMargin} plus the {@code rightMargin} fields
     * of {@code lp} to {@code mLeftWidth}. If the {@code position} field of {@code lp} is POSITION_RIGHT
     * the child needs to go in the right gutter so we add the maximum of {@code maxWidth} and the
     * measured width of {@code child} plus the {@code leftMargin} plus the {@code rightMargin} fields
     * of {@code lp} to {@code mRightWidth}. Otherwise we add the maximum of {@code maxWidth} and the
     * measured width of {@code child} plus the {@code leftMargin} plus the {@code rightMargin} fields
     * of {@code lp} to {@code maxWidth}. We set {@code maxHeight} to the maximum of {@code maxHeight}
     * and the sum of the measured height of {@code child} plus the {@code topMargin} plus the
     * {@code bottomMargin} fields of {@code lp}. We set {@code childState} to the result of combining
     * the previous value of {@code childState} with the state bits of {@code child} then loop around
     * for the next of our children.
     * <p>
     * Having processed all of our children we add {@code mLeftWidth} and {@code mRightWidth} to
     * {@code maxWidth} (total width is the maximum width of all inner children plus the width of
     * the children in the gutters). We then make sure the {@code maxHeight} and {@code maxWidth}
     * are bigger than our suggested minimum height and width.
     * <p>
     * Finally we call the method {@code setMeasuredDimension} to store the measured width and
     * measured height, where the width is given by the return value of {@code resolveSizeAndState}
     * with {@code maxWidth} as how big our view wants to be, {@code widthMeasureSpec} as the restraints
     * imposed by our parent, and {@code childState} as the size information bit mask for our children,
     * and where the height is given by the return value of {@code resolveSizeAndState} with
     * {@code maxHeight} as how big our view wants to be, {@code heightMeasureSpec} as the restraints
     * imposed by our parent, and {@code childState} left shifted by MEASURED_HEIGHT_STATE_SHIFT (16)
     * as the size information bit mask for our children.
     *
     * @param widthMeasureSpec  horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec vertical space requirements as imposed by the parent.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        // These keep track of the space we are using on the left and right for
        // views positioned there; we need member variables so we can also use
        // these for layout later.
        mLeftWidth = 0;
        mRightWidth = 0;

        // Measurement will ultimately be computing these values.
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                // Measure the child.
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);

                // Update our size information based on the layout params.  Children
                // that asked to be positioned on the left or right go in those gutters.
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (lp.position == LayoutParams.POSITION_LEFT) {
                    mLeftWidth += Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                } else if (lp.position == LayoutParams.POSITION_RIGHT) {
                    mRightWidth += Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                } else {
                    maxWidth = Math.max(maxWidth,
                            child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                }
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }

        // Total width is the maximum width of all inner children plus the gutters.
        maxWidth += mLeftWidth + mRightWidth;

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Report our final dimensions.
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    /**
     * Called from layout when this view should assign a size and position to each of its children.
     * First we initialize our variable {@code int count} with the number of children we have. We
     * initialize {@code int leftPos} with the left padding of our view, and {@code int rightPos}
     * with the result of subtracting our parameter {@code left} and our right padding from our
     * parameter {@code right}. We initialize {@code int middleLeft} with the result of adding
     * {@code leftPos} to {@code mLeftWidth} (the left side of the middle region between the gutters)
     * and {@code int middleRight} with the result of subtracting {@code mRightWidth} from
     * {@code rightPos} (the right side of the middle region between the gutters). We initialize
     * {@code int parentTop} with our top padding, and {@code int parentBottom} with the result of
     * subtracting {@code top} and our bottom padding from {@code bottom}.
     * <p>
     * Now we loop over all {@code count} of our children using {@code i} as the index. We initialize
     * our variable {@code View child} with the child at position {@code i}, and if the visibility of
     * {@code child} is not GONE we initialize {@code LayoutParams lp} with the layout parameters
     * of {@code child}, initialize {@code width} with the measured width of {@code child} and
     * {@code height} with its measured height.
     * <p>
     * Left gutter:
     * If the {@code position} field of {@code lp} is POSITION_LEFT we set the {@code left} field
     * of {@code mTmpContainerRect} to the sum of {@code leftPos} and the {@code leftMargin} field
     * of {@code lp} and the {@code right} field of {@code mTmpContainerRect} to {@code leftPos}
     * plus {@code width} plus the {@code rightMargin} field of {@code lp}. We then set {@code leftPos}
     * to the {@code right} field of {@code mTmpContainerRect}.
     * <p>
     * Right gutter:
     * If the {@code position} field of {@code lp} is POSITION_RIGHT we set the {@code right} field
     * of {@code mTmpContainerRect} to {@code rightPos} minus the {@code rightMargin} field of {@code lp}
     * and the {@code left} field of {@code mTmpContainerRect} to {@code rightPos} minus {@code width}
     * minus the {@code leftMargin} field of {@code lp}. We then set {@code rightPos} to the {@code left}
     * field of {@code mTmpContainerRect}.
     * <p>
     * Middle:
     * Otherwise the child is in the middle so we set the {@code left} field of {@code mTmpContainerRect}
     * to {@code middleLeft} plus the {@code leftMargin} field of {@code lp}, and the {@code right}
     * field of {@code mTmpContainerRect} to {@code middleRight} minus the {@code rightMargin} field
     * of {@code lp}.
     * <p>
     * In all cases we set the {@code top} field of {@code mTmpContainerRect} to {@code parentTop}
     * plus the {@code topMargin} field of {@code lp} and the {@code bottom} field of
     * {@code mTmpContainerRect} to {@code parentBottom} minus the {@code bottomMargin} field of
     * {@code lp}.
     * <p>
     * Having determined where we want {@code child} positioned we now call {@code Gravity.apply}
     * which takes the {@code gravity} field of {@code lp} and applies it along with {@code width}
     * and {@code height} of the child and uses {@code mTmpContainerRect} as the preliminary frame
     * of the containing space, in which the object will be placed to create the computed frame of
     * the object in its container {@code mTmpChildRect}.
     * <p>
     * Finally we instruct {@code child} to layout itself with its left X coordinate the {@code left}
     * field of {@code mTmpChildRect}, its top Y coordinate the {@code top} field of {@code mTmpChildRect},
     * its right X coordinate the {@code right} field of {@code mTmpChildRect}, and its bottom Y
     * coordinate the {@code bottom} field of {@code mTmpChildRect}. Then we loop around for the next
     * child view.
     *
     * @param changed This is a new size or position for this view if true
     * @param left    Left position, relative to parent
     * @param top     Top position, relative to parent
     * @param right   Right position, relative to parent
     * @param bottom  Bottom position, relative to parent
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int count = getChildCount();

        // These are the far left and right edges in which we are performing layout.
        int leftPos = getPaddingLeft();
        int rightPos = right - left - getPaddingRight();

        // This is the middle region inside of the gutter.
        final int middleLeft = leftPos + mLeftWidth;
        final int middleRight = rightPos - mRightWidth;

        // These are the top and bottom edges in which we are performing layout.
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                // Compute the frame in which we are placing this child.
                if (lp.position == LayoutParams.POSITION_LEFT) {
                    mTmpContainerRect.left = leftPos + lp.leftMargin;
                    mTmpContainerRect.right = leftPos + width + lp.rightMargin;
                    leftPos = mTmpContainerRect.right;
                } else if (lp.position == LayoutParams.POSITION_RIGHT) {
                    mTmpContainerRect.right = rightPos - lp.rightMargin;
                    mTmpContainerRect.left = rightPos - width - lp.leftMargin;
                    rightPos = mTmpContainerRect.left;
                } else {
                    mTmpContainerRect.left = middleLeft + lp.leftMargin;
                    mTmpContainerRect.right = middleRight - lp.rightMargin;
                }
                mTmpContainerRect.top = parentTop + lp.topMargin;
                mTmpContainerRect.bottom = parentBottom - lp.bottomMargin;

                // Use the child's gravity and size to determine its final
                // frame within its container.
                Gravity.apply(lp.gravity, width, height, mTmpContainerRect, mTmpChildRect);

                // Place the child.
                child.layout(mTmpChildRect.left, mTmpChildRect.top,
                        mTmpChildRect.right, mTmpChildRect.bottom);
            }
        }
    }

    // ----------------------------------------------------------------------
    // The rest of the implementation is for custom per-child layout parameters.
    // If you do not need these (for example you are writing a layout manager
    // that does fixed positioning of its children), you can drop all of this.

    /**
     * Returns a new set of layout parameters based on the supplied attributes set. We return a new
     * instance of our {@code MarginLayoutParams} descendant {@code LayoutParams} created using the
     * context we are running in, and our parameter {@code AttributeSet attrs}.
     *
     * @param attrs the attributes to build the layout parameters from
     * @return an instance of {@code LayoutParams} or one of its descendants
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CustomLayout.LayoutParams(getContext(), attrs);
    }

    /**
     * Returns a set of default layout parameters. We just return a new instance of {@code LayoutParams}
     * with its width and height both set to MATCH_PARENT.
     *
     * @return a set of default layout parameters or null
     */
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    /**
     * Returns a safe set of layout parameters based on the supplied layout params. We just return
     * an instance of our {@code CustomLayout.LayoutParams}, which in turn just calls its super's
     * constructor with its parameter {@code p}.
     *
     * @param p The layout parameters to convert into a suitable set of layout parameters
     *          for this ViewGroup.
     * @return an instance of {@code ViewGroup.LayoutParams} or one of its descendants
     */
    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /**
     * Returns true if {@code p} is an instance of our {@code CustomLayout.LayoutParams}, false
     * otherwise.
     *
     * @param p instance of {@code ViewGroup.LayoutParams} we are to check
     * @return true if {@code p} is an instance of our {@code CustomLayout.LayoutParams}.
     */
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    /**
     * Custom per-child layout information.
     */
    @SuppressWarnings("WeakerAccess")
    public static class LayoutParams extends MarginLayoutParams {
        /**
         * The gravity to apply with the View to which these layout parameters are associated.
         */
        public int gravity = Gravity.TOP | Gravity.START;

        /**
         * Constant for a child located in the middle of its parent
         */
        public static int POSITION_MIDDLE = 0;
        /**
         * Constant for a child located in the left gutter
         */
        public static int POSITION_LEFT = 1;
        /**
         * Constant for a child located in the right gutter
         */
        public static int POSITION_RIGHT = 2;

        /**
         * Position of the child whose to which these layout parameters are associated.
         */
        public int position = POSITION_MIDDLE;

        /**
         * Creates a new set of layout parameters. The values are extracted from the supplied
         * attributes set and context. This constructor is called when a layout is inflated from an
         * xml file. First we call our super's constructor, then we initialize {@code TypedArray a}
         * with styled attribute information in this Context's theme for the attribute declare-styleable
         * with resource ID R.styleable.CustomLayoutLP (which defines the attr layout_gravity and
         * layout_position). We then initialize our field {@code gravity} with the value stored in
         * {@code a} under the key R.styleable.CustomLayoutLP_android_layout_gravity (defaulting to
         * the current value of {@code gravity} if none is given), and initialize {@code position}
         * with the value stored in {@code a} under the key R.styleable.CustomLayoutLP_layout_position
         * (defaulting to the current value of {@code position} if none is given). Finally we recycle
         * {@code a}.
         *
         * @param c     the application environment
         * @param attrs the set of attributes from which to extract the layout parameters' values
         */
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            // Pull the layout param values from the layout XML during
            // inflation.  This is not needed if you don't care about
            // changing the layout behavior in XML.
            @SuppressLint("CustomViewStyleable")
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.CustomLayoutLP);
            gravity = a.getInt(R.styleable.CustomLayoutLP_android_layout_gravity, gravity);
            position = a.getInt(R.styleable.CustomLayoutLP_layout_position, position);
            a.recycle();
        }

        /**
         * Creates a new set of layout parameters with the specified width and height. We just call
         * our super's constructor.
         *
         * @param width  width of the view
         * @param height height of the view
         */
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        /**
         * Copy constructor. Clones the width and height values of the source. We just call our super's
         * constructor.
         *
         * @param source The layout params to copy from.
         */
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
}

