package com.example.blinkitadminclone.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.blinkitadminclone.R
import com.example.blinkitadminclone.activity.UserMainActivity
import com.example.blinkitadminclone.viewmodels.AuthViewModel
import com.example.blinkitadminclone.viewmodels.NavigationEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeNavigationEvents()

        lifecycleScope.launch {
            delay(2000) // 2 seconds splash delay
            viewModel.checkLoginStatus()
        }
    }

    private fun observeNavigationEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigationEvent.collect { event ->
                    when (event) {
                        is NavigationEvent.NavigateToHome -> {
                            startActivity(Intent(requireContext(), UserMainActivity::class.java))
                            requireActivity().finish()
                        }
                        is NavigationEvent.NavigateToLogin -> {
                            findNavController().navigate(R.id.action_splashFragment_to_signinFragment)
                        }
                    }
                }
            }
        }
    }
}
