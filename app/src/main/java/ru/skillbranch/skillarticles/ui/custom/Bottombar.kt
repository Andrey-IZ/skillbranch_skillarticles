package ru.skillbranch.skillarticles.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.shape.MaterialShapeDrawable
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.ui.custom.behaviors.BottombarBehavior

class Bottombar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), CoordinatorLayout.AttachedBehavior {

    init {
      View.inflate(context, R.layout.layout_bootombar, this)
        val materialBg = MaterialShapeDrawable.createWithElevationOverlay(context, elevation)
//        materialBg.elevation = elevation
        background = materialBg
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<Bottombar> {
        return BottombarBehavior()
    }
}