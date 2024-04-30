package com.mvv.gui.util

import java.lang.reflect.Field


fun Any.reflectionField(field: String): Field {

    var cls: Class<Any>? = this.javaClass

    while (cls != null) {
        try {
            val f = cls.getDeclaredField(field)
            f.trySetAccessible()
            return f
        } catch (_: Exception) {  }

        cls = cls.superclass
    }

    throw NoSuchFieldException("No field [$field] in class ${this.javaClass.name}.")
}


fun Any.findReflectionField(vararg fields: String): Field? {
    for (field in fields) {
        try {
            return this.reflectionField(field)
        }
        catch (_: NoSuchFieldException) {
        }
    }
    return null
}
