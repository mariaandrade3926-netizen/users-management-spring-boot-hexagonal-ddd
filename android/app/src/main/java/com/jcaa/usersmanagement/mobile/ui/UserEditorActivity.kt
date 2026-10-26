package com.jcaa.usersmanagement.mobile.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.jcaa.usersmanagement.mobile.data.ApiClient
import com.jcaa.usersmanagement.mobile.data.CreateUserRequest
import com.jcaa.usersmanagement.mobile.data.SessionStore
import com.jcaa.usersmanagement.mobile.data.UpdateUserRequest
import com.jcaa.usersmanagement.mobile.data.UserResponse
import com.jcaa.usersmanagement.mobile.data.userMessage
import com.jcaa.usersmanagement.mobile.databinding.ActivityUserEditorBinding
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class UserEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserEditorBinding
    private val session by lazy { SessionStore(this) }
    private val roles = listOf("ADMIN", "MEMBER", "REVIEWER")
    private val statuses = listOf("ACTIVE", "INACTIVE", "PENDING", "BLOCKED")
    private val editingId: String? by lazy { intent.getStringExtra(EXTRA_ID) }
    private val creating: Boolean by lazy { editingId.isNullOrBlank() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!session.isLoggedIn()) {
            finish()
            return
        }
        binding = ActivityUserEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.roleInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, roles))
        binding.statusInput.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, statuses))
        if (creating) {
            binding.titleLabel.setText(com.jcaa.usersmanagement.mobile.R.string.create_user)
            binding.statusLayout.isVisible = false
            binding.roleInput.setText("MEMBER", false)
            binding.passwordInput.hint = "Contrasena"
        } else {
            binding.titleLabel.setText(com.jcaa.usersmanagement.mobile.R.string.edit_user)
            binding.nameInput.setText(intent.getStringExtra(EXTRA_NAME))
            binding.emailInput.setText(intent.getStringExtra(EXTRA_EMAIL))
            binding.roleInput.setText(intent.getStringExtra(EXTRA_ROLE) ?: "MEMBER", false)
            binding.statusInput.setText(intent.getStringExtra(EXTRA_STATUS) ?: "PENDING", false)
            binding.deleteButton.isVisible = session.isAdmin()
        }
        val canEdit = session.isAdmin()
        binding.saveButton.isVisible = canEdit
        binding.nameInput.isEnabled = canEdit
        binding.emailInput.isEnabled = canEdit
        binding.passwordInput.isEnabled = canEdit
        binding.roleInput.isEnabled = canEdit
        binding.statusInput.isEnabled = canEdit
        binding.saveButton.setOnClickListener { save() }
        binding.deleteButton.setOnClickListener { confirmDelete() }
    }

    private fun save() {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()
        val role = binding.roleInput.text?.toString()?.trim().orEmpty()
        val status = binding.statusInput.text?.toString()?.trim().orEmpty()
        if (name.length < 3 || !email.contains("@") || role !in roles) {
            toast("Revisa nombre, correo y rol.")
            return
        }
        if (creating && password.length < 8) {
            toast("La contrasena debe tener al menos 8 caracteres.")
            return
        }
        if (!creating && password.isNotEmpty() && password.length < 8) {
            toast("La contrasena nueva debe tener al menos 8 caracteres, o dejala vacia.")
            return
        }
        setLoading(true)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val api = ApiClient.create(session.baseUrl(), session.token())
                    if (creating) {
                        api.create(CreateUserRequest(UUID.randomUUID().toString(), name, email, password, role))
                    } else {
                        api.update(
                            editingId!!,
                            UpdateUserRequest(name, email, password.ifBlank { null }, role, status)
                        )
                    }
                }
                toast(if (creating) "Usuario creado en estado PENDING." else "Usuario actualizado. Se envio la notificacion.")
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

    private fun confirmDelete() {
        val id = editingId ?: return
        AlertDialog.Builder(this)
            .setTitle("Eliminar usuario")
            .setMessage("Esta accion no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ -> delete(id) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun delete(id: String) {
        setLoading(true)
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val response = ApiClient.create(session.baseUrl(), session.token()).delete(id)
                    if (!response.isSuccessful) {
                        throw HttpException(response)
                    }
                }
                toast("Usuario eliminado.")
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
        binding.deleteButton.isEnabled = !loading
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val EXTRA_ID = "user_id"
        private const val EXTRA_NAME = "user_name"
        private const val EXTRA_EMAIL = "user_email"
        private const val EXTRA_ROLE = "user_role"
        private const val EXTRA_STATUS = "user_status"

        fun createIntent(context: Context): Intent = Intent(context, UserEditorActivity::class.java)

        fun editIntent(context: Context, user: UserResponse): Intent {
            return Intent(context, UserEditorActivity::class.java)
                .putExtra(EXTRA_ID, user.id)
                .putExtra(EXTRA_NAME, user.name)
                .putExtra(EXTRA_EMAIL, user.email)
                .putExtra(EXTRA_ROLE, user.role)
                .putExtra(EXTRA_STATUS, user.status)
        }
    }
}
