package ru.skillbranch.skillarticles.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams

fun View.setMarginOptionally(bottom: Int) {
     this.updateLayoutParams {
//         this.height += bottom
     }
}

