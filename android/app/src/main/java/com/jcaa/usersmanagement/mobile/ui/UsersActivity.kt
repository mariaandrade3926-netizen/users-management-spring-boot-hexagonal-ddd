package com.jcaa.usersmanagement.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.jcaa.usersmanagement.mobile.R
import com.jcaa.usersmanagement.mobile.data.ApiClient
import com.jcaa.usersmanagement.mobile.data.SessionStore
import com.jcaa.usersmanagement.mobile.data.UserResponse
import com.jcaa.usersmanagement.mobile.data.userMessage
import com.jcaa.usersmanagement.mobile.databinding.ActivityUsersBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUsersBinding
    private val session by lazy { SessionStore(this) }
    private val adapter = UserAdapter { user -> openEditor(user) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!session.isLoggedIn()) {
            openLogin()
            return
        }
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.inflateMenu(R.menu.menu_users)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.logout) {
                session.clearSession()
                openLogin()
                true
            } else {
                false
            }
        }
        binding.sessionLabel.text = "${session.email()} · ${session.role()}"
        binding.usersList.layoutManager = LinearLayoutManager(this)
        binding.usersList.adapter = adapter
        binding.addButton.isVisible = session.isAdmin()
        binding.addButton.setOnClickListener {
            startActivity(UserEditorActivity.createIntent(this))
        }
    }

    override fun onResume() {
        super.onResume()
        if (session.isLoggedIn()) {
            loadUsers()
        }
    }

    private fun loadUsers() {
        lifecycleScope.launch {
            try {
                val users = withContext(Dispatchers.IO) {
                    ApiClient.create(session.baseUrl(), session.token()).list()
                }
                adapter.submit(users)
                binding.emptyLabel.isVisible = users.isEmpty()
            } catch (error: Exception) {
                toast(error.userMessage())
            }
        }
    }

    private fun openEditor(user: UserResponse) {
        startActivity(UserEditorActivity.editIntent(this, user))
    }

    private fun openLogin() {
        startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
