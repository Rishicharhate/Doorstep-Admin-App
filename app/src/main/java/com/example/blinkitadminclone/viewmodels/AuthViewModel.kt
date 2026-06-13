package com.example.blinkitadminclone.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinkitadminclone.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.OtpType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class NavigationEvent {
    object NavigateToHome : NavigationEvent()
    object NavigateToLogin : NavigationEvent()
}

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    fun checkLoginStatus() {
        viewModelScope.launch {
            val isLoggedIn = SupabaseClient.client.auth.currentSessionOrNull() != null
            if (isLoggedIn) {
                _navigationEvent.emit(NavigationEvent.NavigateToHome)
            } else {
                _navigationEvent.emit(NavigationEvent.NavigateToLogin)
            }
        }
    }

    fun sendOtp(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signInWith(OTP) {
                    this.email = email
                }
                _authState.value = AuthState.OtpSent
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Failed to send OTP")
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.verifyEmailOtp(
                    type = OtpType.Email.EMAIL,
                    email = email,
                    token = otp
                )
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Invalid OTP")
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return SupabaseClient.client.auth.currentSessionOrNull() != null
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}