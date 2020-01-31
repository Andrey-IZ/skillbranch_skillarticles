package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View.BaseSavedState
import android.view.View
import android.view.ViewAnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.android.synthetic.main.layout_bootombar.view.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior
import kotlin.math.hypot

class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    private var isSearchMode = false
    init {
      View.inflate(context, R.layout.layout_bootombar, this)
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context, elevation)
//        materialBg.elevation = elevation
        background = materialBg
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<Bottombar> {
        return BottombarBehavior()
    }

    // save state
    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedSate(super.onSaveInstanceState())
        savedState.ssIsSearchMode = isSearchMode
        return savedState
    }

    fun setSearchMode(search: Boolean) {
        if (isSearchMode == search || !isAttachedToWindow) return
        isSearchMode = search
        if (isSearchMode) animateShowSearchPanel()
        else animateHideSearchPanel()
    }

    private fun animateHideSearchPanel() {
        group_bottom.isVisible = true
        val endRadius = hypot(width.toFloat(), height/2f)
        val va = ViewAnimationUtils.createCircularReveal(
            reveal,
            width,
            height/2,
            endRadius,
            0f
        )
        va.duration = 5000
        va.doOnEnd { reveal.isVisible = false }
        va.start()
    }

    private fun animateShowSearchPanel() {
        reveal.isVisible = true
        val endRadius = hypot(width.toFloat(), height/2f)
        val va = ViewAnimationUtils.createCircularReveal(
            reveal,
            width,
            height/2,
            0f,
            endRadius
        )
        va.doOnEnd { group_bottom.isVisible = false }
        va.start()
    }

    fun bindSerachInfo(searchCount: Int = 0, position: Int = 0) {
        if (searchCount == 0) {
            tv_search_result.text = "not found"
            btn_result_down.isEnabled = false
            btn_result_down.isEnabled = false
        } else {
            tv_search_result.text ="${position.inc()} of $searchCount"
            btn_result_down.isEnabled = true
            btn_result_down.isEnabled = true
        }

        //lock button presses in min/max positions
        when (position) {
            0 -> btn_result_up.isEnabled = false
            searchCount - 1 ->btn_result_down.isEnabled = false
        }
    }

    //restore state
    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        if (state is SavedSate) {
            isSearchMode = state.ssIsSearchMode
            reveal.isVisible = !isSearchMode
            group_bottom.isVisible = !isSearchMode
        }
    }

    private class SavedSate : BaseSavedState,Parcelable {
        var ssIsSearchMode: Boolean = false

        constructor(parcel: Parcelable?) : super(parcel)

        constructor(parcel: Parcel) : super(parcel) {
            ssIsSearchMode = parcel.readInt() == 1
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(if (ssIsSearchMode) 1 else 0)
        }

        override fun describeContents() = 0

        companion object CREATOR : Parcelable.Creator<SavedSate> {
            override fun createFromParcel(parcel: Parcel): SavedSate {
                return SavedSate(parcel)
            }

            override fun newArray(size: Int): Array<SavedSate?> {
                return arrayOfNulls(size)
            }
        }
    }

}
