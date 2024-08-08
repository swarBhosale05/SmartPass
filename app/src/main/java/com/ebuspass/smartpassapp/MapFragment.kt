package com.ebuspass.smartpassapp
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ebuspass.smartpassapp.databinding.FragmentMapBinding
class MapFragment : Fragment() {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGetDirections.setOnClickListener {
            val source = binding.editTextSource.text.toString()
            val destination = binding.editTextDestination.text.toString()
            openGoogleMaps(source, destination)
        }
    }
    private fun openGoogleMaps(source: String, destination: String) {
        val uri =
            "https://www.google.com/maps/dir/?api=1&origin=$source&destination=$destination&mode=transit"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(intent)
        } else {
            intent.setPackage(null)
            startActivity(intent)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
