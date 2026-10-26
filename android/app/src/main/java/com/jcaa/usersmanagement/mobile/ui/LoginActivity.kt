package com.jcaa.usersmanagement.mobile.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.jcaa.usersmanagement.mobile.data.ApiClient
import com.jcaa.usersmanagement.mobile.data.JwtPayload
import com.jcaa.usersmanagement.mobile.data.LoginRequest
import com.jcaa.usersmanagement.mobile.data.SessionStore
import com.jcaa.usersmanagement.mobile.data.userMessage
import com.jcaa.usersmanagement.mobile.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val session by lazy { SessionStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.baseUrlInput.setText(session.baseUrl())
        binding.loginButton.setOnClickListener { login() }
        binding.registerButton.setOnClickListener {
            session.saveBaseUrl(currentBaseUrl())
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        if (session.isLoggedIn()) {
            openUsers()
        }
    }

    private fun login() {
        val baseUrl = currentBaseUrl()
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()
        if (baseUrl.isBlank() || email.isBlank() || password.length < 8) {
            toast("Completa la URL, el correo y una contrasena de al menos 8 caracteres.")
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.create(baseUrl, null).login(LoginRequest(email, password))
                }
                session.saveSession(baseUrl, response.accessToken, email, JwtPayload.role(response.accessToken))
                openUsers()
            } catch (error: Exception) {
                toast(error.userMessage())
            } finally {
                if (!isFinishing) {
                    setLoading(false)
                }
            }
        }
    }

    private fun currentBaseUrl(): String = binding.baseUrlInput.text?.toString()?.trim().orEmpty()

    private fun openUsers() {
        startActivity(Intent(this, UsersActivity::class.java))
        finish()
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.isVisible = loading
        binding.loginButton.isEnabled = !loading
        binding.registerButton.isEnabled = !loading
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
