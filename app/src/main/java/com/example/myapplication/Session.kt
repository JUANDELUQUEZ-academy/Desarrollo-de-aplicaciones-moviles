package com.example.myapplication

/** La sesión dura mientras viva el proceso. Las cuentas sí persisten en Room. */
object Session {
    var userId: Long? = null
        private set
    fun start(id: Long) { userId = id }
    fun end() { userId = null }
}

