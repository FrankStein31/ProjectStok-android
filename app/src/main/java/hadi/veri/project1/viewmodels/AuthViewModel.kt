package hadi.veri.project1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import hadi.veri.project1.api.ApiResponse
import hadi.veri.project1.api.AuthResponse
import hadi.veri.project1.models.User
import hadi.veri.project1.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    
    private val _loginResult = MutableLiveData<Result<ApiResponse<AuthResponse>>>()
    val loginResult: LiveData<Result<ApiResponse<AuthResponse>>> = _loginResult
    
    private val _registerResult = MutableLiveData<Result<ApiResponse<AuthResponse>>>()
    val registerResult: LiveData<Result<ApiResponse<AuthResponse>>> = _registerResult
    
    private val _logoutResult = MutableLiveData<Result<ApiResponse<Nothing>>>()
    val logoutResult: LiveData<Result<ApiResponse<Nothing>>> = _logoutResult
    
    private val _profileResult = MutableLiveData<Result<ApiResponse<User>>>()
    val profileResult: LiveData<Result<ApiResponse<User>>> = _profileResult
    
    private val _updateProfileResult = MutableLiveData<Result<ApiResponse<User>>>()
    val updateProfileResult: LiveData<Result<ApiResponse<User>>> = _updateProfileResult
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginResult.value = repository.login(email, password)
        }
    }
    
    fun register(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        phone: String? = null,
        address: String? = null
    ) {
        viewModelScope.launch {
            _registerResult.value = repository.register(name, email, password, passwordConfirmation, phone, address)
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _logoutResult.value = repository.logout()
        }
    }
    
    fun getProfile() {
        viewModelScope.launch {
            _profileResult.value = repository.getProfile()
        }
    }
    
    fun updateProfile(name: String, phone: String? = null, address: String? = null) {
        viewModelScope.launch {
            _updateProfileResult.value = repository.updateProfile(name, phone, address)
        }
    }
    
    fun isLoggedIn(): Boolean = repository.isLoggedIn()
    
    fun isAdmin(): Boolean = repository.isAdmin()
    
    fun getUser(): User = repository.getUser()
} 