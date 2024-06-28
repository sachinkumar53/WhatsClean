package com.sachin.app.whatsclean.data.model

enum class GridType {
    RECEIVED,
    SENT,
    DUPLICATES;

    companion object {
        fun getTypeByPosition(position: Int): GridType {
            return if (position < 0 || position > values().size)
                RECEIVED
            else values()[position]

        }
    }
}