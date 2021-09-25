package com.tokyonth.installer.data

import androidx.annotation.DrawableRes

data class SettingsEntity(var title: String,
                          var sub: String,
                          @DrawableRes
                        var icon: Int,
                          var color: Int,
                          var selected: Boolean)
