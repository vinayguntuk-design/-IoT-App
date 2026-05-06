package com.premiumiot.app.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.premiumiot.app.R
import com.premiumiot.app.domain.AuthManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModelFactory() }
    private lateinit var authManager: AuthManager
    private lateinit var adapter: DeviceAdapter
    private lateinit var roomTabs: LinearLayout
    private lateinit var syncModeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager(this)
        if (!authManager.isLoggedIn()) {
            showLogin()
            return
        }
        showDashboard()
    }

    private fun showLogin() {
        setContentView(R.layout.activity_login)
        val usernameInput = findViewById<EditText>(R.id.usernameInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val errorText = findViewById<TextView>(R.id.loginErrorText)
        findViewById<TextView>(R.id.loginButton).setOnClickListener {
            val accepted = authManager.login(
                username = usernameInput.text.toString().trim(),
                password = passwordInput.text.toString()
            )
            if (accepted) {
                showDashboard()
            } else {
                errorText.text = "Invalid username or password"
                errorText.visibility = View.VISIBLE
            }
        }
    }

    private fun showDashboard() {
        setContentView(R.layout.activity_main)

        roomTabs = findViewById(R.id.roomTabs)
        syncModeText = findViewById(R.id.syncModeText)
        adapter = DeviceAdapter(onControl = viewModel::sendControl)
        findViewById<TextView>(R.id.logoutButton).setOnClickListener {
            authManager.logout()
            showLogin()
        }

        findViewById<RecyclerView>(R.id.deviceRecycler).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submit(state.devices)
                    syncModeText.text = state.devices.firstOrNull()?.state?.transport?.name ?: "LOCAL"
                    renderRooms(state)
                }
            }
        }

        viewModel.start()
    }

    private fun renderRooms(state: DashboardUiState) {
        roomTabs.removeAllViews()
        state.rooms.forEach { room ->
            val tab = TextView(this).apply {
                text = room
                setTextColor(getColor(if (room == state.selectedRoom) R.color.accent_yellow else R.color.text_secondary))
                setBackgroundResource(R.drawable.bg_room_tab)
                gravity = android.view.Gravity.CENTER
                textSize = 14f
                setPadding(18.dp, 0, 18.dp, 0)
                setOnClickListener { viewModel.selectRoom(room) }
            }
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 40.dp).apply {
                setMargins(0, 0, 10.dp, 0)
            }
            roomTabs.addView(tab, params)
        }
    }
}

val Int.dp: Int
    get() = (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
