package com.example.newsapp.data.local

import androidx.room.TypeConverter
import com.example.newsapp.data.local.model.Source


class Converters {

    @TypeConverter
    fun getSource(source: Source): String = source.name

    @TypeConverter
    fun setSource(name: String): Source = Source(name, name)
}