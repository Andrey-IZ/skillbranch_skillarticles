package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

fun View.setMarginOptionally(
    left: Int = marginLeft,
    top: Int = marginTop,
    right: Int = marginRight,
    bottom: Int = marginBottom
) {
    val l = if (left != marginLeft) left + marginLeft else left
    val t = if (top != marginTop) top + marginTop else top
    val r = if (right != marginRight) right + marginRight else right
    val b = if (bottom != marginBottom) bottom + marginBottom else bottom

    val lp = layoutParams as ViewGroup.MarginLayoutParams
    lp.setMargins(l, t, r, b)
    layoutParams = lp
}

