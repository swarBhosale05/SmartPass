package com.ebuspass.smartpassapp
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.ebuspass.smartpassapp.databinding.FragmentPassCreateBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
class PassCreateFragment : Fragment() {
    private lateinit var binding: FragmentPassCreateBinding
    private lateinit var passItems: List<String>
    private lateinit var loadingDialog: Dialog
    private val request: GetPhoneNumberHintIntentRequest = GetPhoneNumberHintIntentRequest.builder().build()
    private var selectedImageUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPassCreateBinding.inflate(inflater, container, false)
        passItems = listOf(
            "PMC (Pune)", "PCMC (Pimpri Chinchwad)", "PMC and PCMC"
        )
        binding.editTextAge.setOnClickListener {
            showDatePicker()
        }
        binding.passPicImage.setOnClickListener {
            ImagePicker.with(this).cropSquare().compress(1024).maxResultSize(1080, 1080).start()
        }
        binding.buttonPrice.setOnClickListener {
            showLoadingDialog()
            savePassDataToFirebase()
        }
        binding.spinnerView.setItems(passItems)
        binding.spinnerView.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            binding.buttonPrice.text = when (newItem) {
                "PMC (Pune)", "PCMC (Pimpri Chinchwad)" -> "Pay ₹40"
                "PMC and PCMC" -> "Pay ₹50"
                else -> ""
            }
        }
        Identity.getSignInClient(requireActivity()).getPhoneNumberHintIntent(request)
            .addOnSuccessListener { result ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(result.intentSender).build()
                    phoneNumberHintIntentResultLauncher.launch(intentSenderRequest)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Launching the PendingIntent failed", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Phone Number Hint failed", Toast.LENGTH_SHORT).show()
            }
        return binding.root
    }
    private val phoneNumberHintIntentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val phoneNum =
                    Identity.getSignInClient(requireActivity()).getPhoneNumberFromIntent(result.data)
                        ?.replace("+191", "")
                        ?.replace("+91", "")
                binding.editTextPhoneNumber.setText("+91$phoneNum")
            } catch (e: Exception) {
                Log.e(TAG, "Phone Number Hint failed", e)
            }
        }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            selectedImageUri = data?.data
            binding.passPicImage.setImageURI(selectedImageUri)
        }
    }
    private fun showDatePicker() {
        val materialDatePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select your birthdate")
            .setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setEnd(System.currentTimeMillis())
                    .setOpenAt(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 365 * 3)
                    .build()
            )
            .build()
        materialDatePicker.addOnPositiveButtonClickListener { selection ->
            val currentDate = System.currentTimeMillis()
            val dateDifferenceMillis = kotlin.math.abs(selection - currentDate)
            val millisecondsInYear = (365.25 * 24 * 60 * 60 * 1000).toLong()
            val dateDifferenceYears = dateDifferenceMillis / millisecondsInYear
            binding.editTextAge.setText("Age : $dateDifferenceYears")
        }
        materialDatePicker.show(requireFragmentManager(), "MaterialDatePicker")
    }
    private fun savePassDataToFirebase() {
        val name = binding.editTextName.text.toString()
        val phoneNumber = binding.editTextPhoneNumber.text.toString()
        val gender = if (binding.radioMale.isChecked) "Male" else "Female"
        val ageText = binding.editTextAge.text.toString().removePrefix("Age : ")
        val age = ageText.ifBlank { "0" }
        val selectedItemIndex = binding.spinnerView.selectedIndex
        val adhaarCardNumber = binding.editTextAdhaarNumber.text.toString()
        val selectedPass = if (selectedItemIndex != -1) {
            passItems[selectedItemIndex]
        } else {
            ""
        }
        val createTimeMillis = System.currentTimeMillis()

        if (name.isBlank() || phoneNumber.isBlank() || gender.isBlank() || age == "0" || selectedPass.isBlank() || selectedImageUri == null || adhaarCardNumber.isBlank()) {
            Snackbar.make(requireView(), "Please provide all details", Snackbar.LENGTH_SHORT).show()
            hideLoadingDialog()
            return
        }
        selectedImageUri?.let { uri ->
            val storageRef = FirebaseStorage.getInstance().reference.child("pass_images").child("${UUID.randomUUID()}")
            storageRef.putFile(uri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val passInfo = PassInfo(name, phoneNumber, gender, age, selectedPass, imageUrl, adhaarCardNumber, createTimeMillis)
                        savePassInfoToDatabase(passInfo)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(requireContext(), "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    hideLoadingDialog()
                }
        }
    }
    private fun savePassInfoToDatabase(passInfo: PassInfo) {
        val database = FirebaseDatabase.getInstance().getReference("Passes")
        val passId = database.push().key ?: ""
        database.child(passId).setValue(passInfo)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pass created successfully", Toast.LENGTH_SHORT).show()
                hideLoadingDialog()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to create pass: ${exception.message}", Toast.LENGTH_SHORT).show()
                hideLoadingDialog()
            }
    }
    private fun showLoadingDialog() {
        if (isAdded) {
            loadingDialog = Dialog(requireContext())
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            loadingDialog.setCancelable(false)
            loadingDialog.setContentView(R.layout.loading_dialog)
            loadingDialog.show()
            val passLoaderAnimation = loadingDialog.findViewById<LottieAnimationView>(R.id.pass_loading)
            passLoaderAnimation.playAnimation()
            passLoaderAnimation.repeatCount = LottieDrawable.INFINITE
        }
    }
    private fun hideLoadingDialog() {
        if (isAdded && ::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
}
