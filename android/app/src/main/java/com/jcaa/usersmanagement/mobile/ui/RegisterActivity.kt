package com.jcaa.usersmanagement.mobile.ui

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.jcaa.usersmanagement.mobile.data.ApiClient
import com.jcaa.usersmanagement.mobile.data.CreateUserRequest
import com.jcaa.usersmanagement.mobile.data.SessionStore
import com.jcaa.usersmanagement.mobile.data.userMessage
import com.jcaa.usersmanagement.mobile.databinding.ActivityRegisterBinding
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val session by lazy { SessionStore(this) }
    private val roles = listOf("MEMBER", "REVIEWER", "ADMIN")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.roleInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, roles))
        binding.roleInput.setText(roles.first(), false)
        binding.saveButton.setOnClickListener { register() }
    }

    private fun register() {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()
        val role = binding.roleInput.text?.toString()?.trim().orEmpty()
        if (name.length < 3 || !email.contains("@") || password.length < 8 || role !in roles) {
            toast("Nombre de 3 caracteres, correo valido, contrasena de 8 y un rol.")
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    ApiClient.create(session.baseUrl(), null).create(
                        CreateUserRequest(UUID.randomUUID().toString(), name, email, password, role)
                    )
                }
                toast("Cuenta creada. Queda PENDING hasta que un administrador la active.")
                finish()
            } catch (error: Exception) {
                toast(error.userMessage())
            } finally {
                if (!isFinishing) {
                    setLoading(false)
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.progress.isVisible = loading
        binding.saveButton.isEnabled = !loading
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
