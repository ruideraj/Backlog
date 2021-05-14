package com.ruideraj.backlog.util

import android.content.res.Resources
import android.util.TypedValue

fun Int.asDp(resources: Resources)
    = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), resources.displayMetrics)