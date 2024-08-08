package com.ebuspass.smartpassapp
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ebuspass.smartpassapp.databinding.FragmentSettingsBinding
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        binding.logoutButton.setOnClickListener {
            val sharedPreferences =
                requireActivity().getSharedPreferences("FDatabase", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.apply()
            startActivity(Intent(requireContext(), InfoActivity::class.java))
            requireActivity().finish()
        }
        binding.chatbotButton.setOnClickListener {
            startActivity(Intent(requireActivity(),ChatBotActivity::class.java))
        }
        return binding.root
    }
}