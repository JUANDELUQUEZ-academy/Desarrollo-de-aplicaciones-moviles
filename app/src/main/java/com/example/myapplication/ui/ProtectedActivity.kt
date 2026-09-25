package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.Session
import com.example.myapplication.data.AccessDeniedException
import com.example.myapplication.data.AppException
import com.example.myapplication.data.ParcelRepository
import com.example.myapplication.data.UserRepository
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

abstract class ProtectedActivity : AppCompatActivity() {
    protected abstract val requiredRole: String
    protected val db get() = AppDatabase.getInstance(applicationContext)
    protected val users get() = UserRepository(db.userDao())
    protected val parcels get() = ParcelRepository(db)
    protected val actorId get() = Session.userId ?: throw AccessDeniedException()
    private lateinit var screen: View
    private lateinit var status: TextView
    private lateinit var progress: View
    private var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (!busy) finish() }
        })
    }

    protected fun bindScreen(root: View, status: TextView, progress: View) {
        screen = root
        this.status = status
        this.progress = progress
        setContentView(root)
        applyScreenInsets(root)
    }

    override fun onStart() {
        super.onStart()
        runAction { onAuthenticated(users.requireRole(actorId, requiredRole)) }
    }

    protected abstract suspend fun onAuthenticated(user: User)

    protected fun showMessage(message: String) {
        status.text = message
        status.isVisible = true
    }

    protected fun runAction(action: suspend () -> Unit) {
        if (busy) return
        busy = true
        status.isVisible = false
        progress.isVisible = true
        enableChildren(screen, false)
        lifecycleScope.launch {
            try { action() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: AccessDeniedException) { logout() }
            catch (error: AppException) { showMessage(error.message.orEmpty()) }
            catch (_: Exception) { showMessage(getString(R.string.database_error)) }
            finally {
                busy = false
                progress.isVisible = false
                enableChildren(screen, true)
            }
        }
    }

    protected fun logout() {
        Session.end()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun enableChildren(view: View, enabled: Boolean) {
        view.isEnabled = enabled
        if (view is ViewGroup) for (i in 0 until view.childCount) enableChildren(view.getChildAt(i), enabled)
    }
}

fun formatTimestamp(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-MX")).format(Date(timestamp))
