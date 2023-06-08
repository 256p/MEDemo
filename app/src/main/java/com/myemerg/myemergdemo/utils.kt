package com.myemerg.myemergdemo

import androidx.annotation.StringRes

fun stringResource(@StringRes res: Int): String = appContext.getString(res)