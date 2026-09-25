package com.example.myapplication.ui

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Ajusta el área visible del ScrollView para que sus controles no queden bajo el IME. */
fun applyScreenInsets(root: View) {
    val initial = root.layoutParams as ViewGroup.MarginLayoutParams
    val left = initial.leftMargin
    val top = initial.topMargin
    val right = initial.rightMargin
    val bottom = initial.bottomMargin
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
        val params = view.layoutParams as ViewGroup.MarginLayoutParams
        if (params.leftMargin != left + bars.left || params.topMargin != top + bars.top ||
            params.rightMargin != right + bars.right || params.bottomMargin != bottom + bars.bottom) {
            params.setMargins(left + bars.left, top + bars.top, right + bars.right, bottom + bars.bottom)
            view.layoutParams = params
        }
        insets
    }
    ViewCompat.requestApplyInsets(root)
}
