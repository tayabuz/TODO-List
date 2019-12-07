package com.tayabuz.todolist.stacklayoutmanager

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.tayabuz.todolist.BuildConfig
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import kotlin.math.abs

internal class StackLayoutManager() : RecyclerView.LayoutManager() {

    internal enum class Align(var layoutDirection: Int) {
        LEFT(1),
        RIGHT(-1),
        TOP(1),
        BOTTOM(-1)
    }


    //the space unit for the stacked item
    private var mSpace = 60
    /**
     * the offset unit,deciding current position(the sum of  [.mItemWidth] and [.mSpace])
     */
    private var mUnit: Int = 0
    //item width
    private var mItemWidth: Int = 0
    private var mItemHeight: Int = 0
    //the counting variable ,record the total offset including parallax
    private var mTotalOffset: Int = 0
    //record the total offset without parallax
    private var mRealOffset: Int = 0
    private var animator: ObjectAnimator? = null
    private val duration = 300
    private var recycler: RecyclerView.Recycler? = null
    private var lastAnimateValue: Int = 0
    //the max stacked item count;
    private var maxStackCount = 4
    //initial stacked item
    private var initialStackCount = 4
    private var secondaryScale = 0.8f
    private var scaleRatio = 0.4f
    private var parallax = 1f
    private var initialOffset: Int = 0
    private var initial: Boolean = false
    private var mMinVelocityX: Int = 0
    private val mVelocityTracker = VelocityTracker.obtain()
    private var pointerId: Int = 0
    private var direction =
        Align.LEFT
    private var mRV: RecyclerView? = null
    private var sSetScrollState: Method? = null
    private var mPendingScrollPosition = NO_POSITION

    private var isLinearTransparentGradientEnable = false

    override fun isAutoMeasureEnabled() : Boolean { return true }

    private val mTouchListener = View.OnTouchListener { v, event ->
        mVelocityTracker.addMovement(event)
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (animator != null && animator!!.isRunning)
                animator!!.cancel()
            pointerId = event.getPointerId(0)

        }
        if (event.action == MotionEvent.ACTION_UP) {
            if (v.isPressed) v.performClick()
            mVelocityTracker.computeCurrentVelocity(1000, 14000f)
            val xVelocity = mVelocityTracker.getXVelocity(pointerId)
            val o = mTotalOffset % mUnit
            val scrollX: Int
            if (abs(xVelocity) < mMinVelocityX && o != 0) {
                scrollX = if (o >= mUnit / 2)
                    mUnit - o
                else
                    -o
                val dur = (abs((scrollX + 0f) / mUnit) * duration).toInt()
                Log.i(TAG, "onTouch: ======BREW===")
                brewAndStartAnimator(dur, scrollX)
            }
        }
        false
    }

    private val mOnFlingListener = object : RecyclerView.OnFlingListener() {
        override fun onFling(velocityX: Int, velocityY: Int): Boolean {
            val o = mTotalOffset % mUnit
            val s = mUnit - o
            val scrollX: Int
            val vel = absMax(velocityX, velocityY)
            scrollX = if (vel * direction.layoutDirection > 0) {
                s
            } else
                -o
            val dur = computeSettleDuration(abs(scrollX), abs(vel).toFloat())
            brewAndStartAnimator(dur, scrollX)
            setScrollStateIdle()
            return true
        }
    }

    constructor(config: Config) : this() {
        this.maxStackCount = config.maxStackCount
        this.mSpace = config.space
        this.initialStackCount = config.initialStackCount
        this.secondaryScale = config.secondaryScale
        this.scaleRatio = config.scaleRatio
        this.direction = config.align!!
        this.parallax = config.parallax
        this.isLinearTransparentGradientEnable = config.isLinearTransparentGradientEnable
    }



    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (itemCount <= 0)
            return
        this.recycler = recycler
        detachAndScrapAttachedViews(recycler)
        //got the mUnit basing on the first child,of course we assume that  all the item has the same size
        val anchorView = recycler.getViewForPosition(0)
        measureChildWithMargins(anchorView, 0, 0)
        mItemWidth = anchorView.measuredWidth
        mItemHeight = anchorView.measuredHeight
        mUnit = if (canScrollHorizontally())
            mItemWidth + mSpace
        else
            mItemHeight + mSpace
        //because this method will be called twice
        initialOffset = resolveInitialOffset()
        mMinVelocityX = ViewConfiguration.get(anchorView.context).scaledMinimumFlingVelocity
        fill(recycler, 0)

    }

    //we need take direction into account when calc initialOffset
    private fun resolveInitialOffset(): Int {
        var offset = initialStackCount * mUnit
        if (mPendingScrollPosition != NO_POSITION) {
            offset = mPendingScrollPosition * mUnit
            mPendingScrollPosition = NO_POSITION
        }

        if (direction === Align.LEFT)
            return offset
        if (direction === Align.RIGHT)
            return -offset
        return if (direction === Align.TOP)
            offset
        else
            offset
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        super.onLayoutCompleted(state)
        if (itemCount <= 0)
            return
        if (!initial) {
            fill(recycler, initialOffset, false)
            initial = true
        }
    }

    override fun onAdapterChanged(oldAdapter: Adapter<*>?, newAdapter: Adapter<*>?)
    {
        initial = false
        mRealOffset = 0
        mTotalOffset = mRealOffset
    }

    /**
     * the magic function :).all the work including computing ,recycling,and layout is done here
     *
     * @param recycler ...
     */
    private fun fill(recycler: RecyclerView.Recycler?, dy: Int, apply: Boolean): Int {
        var delta = direction.layoutDirection * dy
        // multiply the parallax factor
        if (apply)
            delta = (delta * parallax).toInt()
        if (direction === Align.LEFT)
            return fillFromLeft(recycler, delta)
        if (direction === Align.RIGHT)
            return fillFromRight(recycler, delta)
        return if (direction === Align.TOP)
            fillFromTop(recycler, delta)
        else
            dy//bottom alignment is not necessary,we don't support that
    }

    private fun fill(recycler: RecyclerView.Recycler, dy: Int): Int {
        return fill(recycler, dy, true)
    }

    private fun fillFromTop(recycler: RecyclerView.Recycler?, dy: Int): Int {
        if (mTotalOffset + dy < 0 || (mTotalOffset.toFloat() + dy.toFloat() + 0f) / mUnit > itemCount - 1)
            return 0
        if (recycler != null) {
            detachAndScrapAttachedViews(recycler)
        }
        mTotalOffset += direction.layoutDirection * dy
        val count = childCount
        //removeAndRecycle  views
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (recycleVertically(child, dy))
                removeAndRecycleView(child!!, recycler!!)
        }
        val currPos = mTotalOffset / mUnit
        val leavingSpace = height - (left(currPos) + mUnit)
        val itemCountAfterBaseItem = leavingSpace / mUnit + 2
        val e = currPos + itemCountAfterBaseItem

        val start = if (currPos - maxStackCount >= 0) currPos - maxStackCount else 0
        val end = if (e >= itemCount) itemCount - 1 else e

        val left = width / 2 - mItemWidth / 2
        //layout views
        for (i in start..end) {
            val view = recycler!!.getViewForPosition(i)

            val scale = scale(i)
            val alpha = alpha(i)

            addView(view)
            measureChildWithMargins(view, 0, 0)
            val top = (left(i) - (1 - scale) * view.measuredHeight / 2).toInt()
            val right = view.measuredWidth + left
            val bottom = view.measuredHeight + top
            layoutDecoratedWithMargins(view, left, top, right, bottom)
            view.alpha = alpha
            view.scaleY = scale
            view.scaleX = scale
        }

        return dy
    }

    private fun fillFromRight(recycler: RecyclerView.Recycler?, dy: Int): Int {

        if (mTotalOffset + dy < 0 || (mTotalOffset.toFloat() + dy.toFloat() + 0f) / mUnit > itemCount - 1)
            return 0
        detachAndScrapAttachedViews(recycler!!)
        mTotalOffset += dy
        val count = childCount
        //removeAndRecycle  views
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (recycleHorizontally(child, dy))
                removeAndRecycleView(child!!, recycler)
        }


        val currPos = mTotalOffset / mUnit
        val leavingSpace = left(currPos)
        val itemCountAfterBaseItem = leavingSpace / mUnit + 2
        val e = currPos + itemCountAfterBaseItem

        val start = if (currPos - maxStackCount <= 0) 0 else currPos - maxStackCount
        val end = if (e >= itemCount) itemCount - 1 else e

        //layout view
        for (i in start..end) {
            val view = recycler.getViewForPosition(i)

            val scale = scale(i)
            val alpha = alpha(i)

            addView(view)
            measureChildWithMargins(view, 0, 0)
            val left = (left(i) - (1 - scale) * view.measuredWidth / 2).toInt()
            val top = 0
            val right = left + view.measuredWidth
            val bottom = view.measuredHeight

            layoutDecoratedWithMargins(view, left, top, right, bottom)
            view.alpha = alpha
            view.scaleY = scale
            view.scaleX = scale
        }

        return dy
    }

    private fun fillFromLeft(recycler: RecyclerView.Recycler?, dy: Int): Int {
        if (mTotalOffset + dy < 0 || (mTotalOffset.toFloat() + dy.toFloat() + 0f) / mUnit > itemCount - 1)
            return 0
        detachAndScrapAttachedViews(recycler!!)
        mTotalOffset += direction.layoutDirection * dy
        val count = childCount
        //removeAndRecycle  views
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (recycleHorizontally(child, dy))
                removeAndRecycleView(child!!, recycler)
        }


        val currPos = mTotalOffset / mUnit
        val leavingSpace = width - (left(currPos) + mUnit)
        val itemCountAfterBaseItem = leavingSpace / mUnit + 2
        val e = currPos + itemCountAfterBaseItem

        val start = if (currPos - maxStackCount >= 0) currPos - maxStackCount else 0
        val end = if (e >= itemCount) itemCount - 1 else e

        //layout view
        for (i in start..end) {
            val view = recycler.getViewForPosition(i)

            val scale = scale(i)
            val alpha = alpha(i)

            addView(view)
            measureChildWithMargins(view, 0, 0)
            val left = (left(i) - (1 - scale) * view.measuredWidth / 2).toInt()
            val top = 0
            val right = left + view.measuredWidth
            val bottom = top + view.measuredHeight
            layoutDecoratedWithMargins(view, left, top, right, bottom)
            view.alpha = alpha
            view.scaleY = scale
            view.scaleX = scale
        }

        return dy
    }

    private fun absMax(a: Int, b: Int): Int {
        return if (abs(a) > abs(b))
            a
        else
            b
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        mRV = view
        //check when raise finger and settle to the appropriate item
        view.setOnTouchListener(mTouchListener)

        view.onFlingListener = mOnFlingListener
    }

    private fun computeSettleDuration(distance: Int, xvel: Float): Int {
        val sWeight = 0.5f * distance / mUnit
        val velWeight = if (xvel > 0) 0.5 * mMinVelocityX / xvel else 0.0

        return ((sWeight.plus(velWeight)) * duration).toInt()
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun brewAndStartAnimator(dur: Int, finalXorY: Int) {
        animator = ObjectAnimator.ofInt(this@StackLayoutManager, "animateValue", 0, finalXorY)
        animator!!.duration = dur.toLong()
        animator!!.start()
        animator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                lastAnimateValue = 0
            }

            override fun onAnimationCancel(animation: Animator) {
                lastAnimateValue = 0
            }
        })
    }

    /******************************precise math method */
    private fun alpha(position: Int): Float {
        if(isLinearTransparentGradientEnable){
            val alpha: Float
            val currPos = mTotalOffset / mUnit
            val n = (mTotalOffset + .0f) / mUnit
            alpha = if (position > currPos)
                1.0f
            else {
                //temporary linear map,barely ok
                1 - (n - position) / maxStackCount
            }
            //for precise checking,oh may be kind of dummy
            return if (alpha <= 0.001f) 0.0F else alpha
        }
        return 1.0f
    }

    private fun scale(position: Int): Float {
        return when (direction) {
            Align.LEFT, Align.RIGHT -> scaleDefault(position)
            else -> scaleDefault(position)
        }
    }

    private fun scaleDefault(position: Int): Float {

        val scale: Float
        val currPos = this.mTotalOffset / mUnit
        val n = (mTotalOffset + .0f) / mUnit
        val x = n - currPos
        // position >= currPos+1;
        if (position >= currPos) {
            scale = when (position) {
                currPos -> 1 - scaleRatio * (n - currPos) / maxStackCount
                currPos + 1
                    //let the item's (index:position+1) scale be 1 when the item slide 1/2 mUnit,
                    // this have better visual effect
                -> //                scale = 0.8f + (0.4f * x >= 0.2f ? 0.2f : 0.4f * x);
                    secondaryScale + if (x > 0.5f) 1 - secondaryScale else 2f * (1 - secondaryScale) * x
                else -> secondaryScale
            }
        } else {//position <= currPos
            scale = if (position < currPos - maxStackCount)
                0f
            else {
                1f - scaleRatio * (n - currPos + currPos - position) / maxStackCount
            }
        }
        return scale
    }

    /**
     * @param position the index of the item in the adapter
     * @return the accurate left position for the given item
     */
    private fun left(position: Int): Int {


        val currPos = mTotalOffset / mUnit
        val tail = mTotalOffset % mUnit
        val n = (mTotalOffset + .0f) / mUnit
        val x = n - currPos

        return when (direction) {
            /*Align.LEFT, Align.TOP ->
                //from left to right or top to bottom
                //these two scenario are actually same
                LeftToRight(position, currPos, tail, x)*/
            Align.RIGHT -> rightToLeft(position, currPos, tail, x)
            else -> leftToRight(position, currPos, tail, x)
        }
    }

    /**
     * @param position ..
     * @param currPos  ..
     * @param tail     .. change
     * @param x        ..
     * @return the left position for given item
     */
    private fun rightToLeft(position: Int, currPos: Int, tail: Int, x: Float): Int {
        //虽然是做对称变换，但是必须考虑到scale给 对称变换带来的影响
        val scale = scale(position)
        val ltr = leftToRight(position, currPos, tail, x)
        return (width - ltr - mItemWidth * scale).toInt()
    }

    private fun leftToRight(position: Int, currPos: Int, tail: Int, x: Float): Int {
        var left: Int

        if (position <= currPos) {

            left = if (position == currPos) {
                (mSpace * (maxStackCount - x)).toInt()
            } else {
                (mSpace * (maxStackCount.toFloat() - x - (currPos - position).toFloat())).toInt()

            }
        } else {
            if (position == currPos + 1)
                left = mSpace * maxStackCount + mUnit - tail
            else {
                val closestBaseItemScale = scale(currPos + 1)

                //调整因为scale导致的left误差
                //                left = (int) (mSpace * maxStackCount + (position - currPos) * mUnit - tail
                //                        -(position - currPos)*(mItemWidth) * (1 - closestBaseItemScale));

                val baseStart =
                    ((mSpace * maxStackCount + mUnit - tail).toFloat() + closestBaseItemScale * (mUnit - mSpace) + mSpace.toFloat()).toInt()
                left =
                    (baseStart + (position - currPos - 2) * mUnit - (position - currPos - 2).toFloat() * (1 - secondaryScale) * (mUnit - mSpace).toFloat()).toInt()
                if (BuildConfig.DEBUG) {
                    Log.i(
                        TAG, "ltr: currPos " + currPos
                                + "  pos:" + position
                                + "  left:" + left
                                + "   baseStart" + baseStart
                                + " currPos+1:" + left(currPos + 1)
                    )
                }
            }
            left = if (left <= 0) 0 else left
        }
        return left
    }

    /**
     * should recycle view with the given dy or say check if the
     * view is out of the bound after the dy is applied
     *
     * @param view ..
     * @param dy   ..
     * @return ..
     */
    private fun recycleHorizontally(view: View?/*int position*/, dy: Int): Boolean {
        return view != null && (view.left - dy < 0 || view.right - dy > width)
    }

    private fun recycleVertically(view: View?, dy: Int): Boolean {
        return view != null && (view.top - dy < 0 || view.bottom - dy > height)
    }


    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return fill(recycler, dx)
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return fill(recycler, dy)
    }

    override fun canScrollHorizontally(): Boolean {
        return direction === Align.LEFT || direction === Align.RIGHT
    }

    override fun canScrollVertically(): Boolean {
        return direction === Align.TOP || direction === Align.BOTTOM
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.WRAP_CONTENT,
            RecyclerView.LayoutParams.WRAP_CONTENT
        )
    }

    /**
     * we need to set scrollstate to [RecyclerView.SCROLL_STATE_IDLE] idle
     * stop RV from intercepting the touch event which block the item click
     */
    private fun setScrollStateIdle() {
        try {
            if (sSetScrollState == null)
                sSetScrollState = RecyclerView::class.java.getDeclaredMethod(
                    "setScrollState",
                    Int::class.javaPrimitiveType
                )
            sSetScrollState!!.isAccessible = true
            sSetScrollState!!.invoke(mRV, RecyclerView.SCROLL_STATE_IDLE)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }

    override fun scrollToPosition(position: Int) {
        if (position > itemCount - 1) {
            Log.i(TAG, "position is $position but itemCount is $itemCount")
            return
        }
        val currPosition = mTotalOffset / mUnit
        val distance = (position - currPosition) * mUnit
        val dur = computeSettleDuration(abs(distance), 0f)
        brewAndStartAnimator(dur, distance)
    }

    override fun requestLayout() {
        super.requestLayout()
        initial = false
    }

    companion object {
        private const val TAG = "StackLayoutManager"
    }
}