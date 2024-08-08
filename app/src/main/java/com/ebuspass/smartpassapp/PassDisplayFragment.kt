package com.ebuspass.smartpassapp
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ebuspass.smartpassapp.databinding.FragmentPassDisplayBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
class PassDisplayFragment : Fragment() {
    private lateinit var binding: FragmentPassDisplayBinding
    private lateinit var database: DatabaseReference
    private lateinit var loadingDialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPassDisplayBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance().reference.child("Passes")
        return binding.root
    }
    override fun onResume() {
        super.onResume()
        showAadhaarOrPhoneDialog()
    }
    private fun showAadhaarOrPhoneDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_aadhaar_or_phone, null)
        val editTextInput = dialogView.findViewById<EditText>(R.id.editTextInput)
        val buttonSearch = dialogView.findViewById<Button>(R.id.button_search)
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Enter Aadhaar Last 4 Digits or Phone Number")
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
        buttonSearch.setOnClickListener {
            showLoadingDialog()
            val input = editTextInput.text.toString().trim()
            if (input.length == 13) {
                searchPassByPhoneNumber(input)
                alertDialog.dismiss()
            } else if (input.length == 4 && input.all { it.isDigit() }) {
                searchPassByAadhaarLast4(input)
                alertDialog.dismiss()
            } else {
                Snackbar.make(
                    requireView(),
                    "Please enter valid Aadhaar last 4 digits or phone number",
                    Snackbar.LENGTH_SHORT
                ).show()
                hideLoadingDialog()
            }
        }
    }
    private fun searchPassByPhoneNumber(phoneNumber: String) {
        database.orderByChild("phoneNumber").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (passSnapshot in dataSnapshot.children) {
                            hideLoadingDialog()
                            val passInfo = passSnapshot.getValue(PassInfo::class.java)
                            if (passInfo != null) {
                                displayPassInfo(passInfo)
                            }
                        }
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Pass not found for phone number: $phoneNumber",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        hideLoadingDialog()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Snackbar.make(
                        requireView(),
                        "Error: ${databaseError.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    hideLoadingDialog()
                }
            })
    }
    private fun searchPassByAadhaarLast4(last4Digits: String) {
        database.orderByChild("adhaarCardNumber").equalTo(last4Digits)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (passSnapshot in dataSnapshot.children) {
                            val passInfo = passSnapshot.getValue(PassInfo::class.java)
                            if (passInfo != null) {
                                displayPassInfo(passInfo)
                            }
                            hideLoadingDialog()
                        }
                    } else {
                        Snackbar.make(
                            requireView(),
                            "Pass not found for Aadhaar last 4 digits: $last4Digits",
                            Snackbar.LENGTH_SHORT
                        ).show()
                        hideLoadingDialog()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Snackbar.make(
                        requireView(),
                        "Error: ${databaseError.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    hideLoadingDialog()
                }
            })
    }
    @SuppressLint("SetTextI18n")
    private fun displayPassInfo(passInfo: PassInfo) {
        binding.errorTextView.isVisible = false
        binding.nameTextView.text = "Name: ${passInfo.name}"
        binding.phoneTextView.text = "Ph.no.: ${passInfo.phoneNumber}"
        binding.genderTextView.text = "Gender: ${passInfo.gender}"
        binding.ageTextView.text = "Age: ${passInfo.age}"
        binding.adhaarNumberTextView.text = "${passInfo.adhaarCardNumber}"
        binding.selectedPassTextView.text = "${passInfo.selectedPass}"
        binding.passTimeTextView.text = "${passInfo.getFormattedTime()}"
        Glide.with(requireContext())
            .load(passInfo.imageUrl)
            .apply(RequestOptions().placeholder(R.drawable.while_loading_img))
            .into(binding.passPicImage)
        val phoneNumberQRBitmap = generateQRCode(passInfo.phoneNumber ?: "")
        binding.qrCodeImageView.setImageBitmap(phoneNumberQRBitmap)
        binding.qrCodeImageView.visibility = View.VISIBLE
    }
    private fun generateQRCode(data: String): Bitmap {
        val text = data
        val width = 800
        val height = 800
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix: BitMatrix =
                multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            val bitmap: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) -0x1000000 else -0x1)
                }
            }
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }
    private fun showLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        loadingDialog.setCancelable(false)
        loadingDialog.setContentView(R.layout.loading_dialog)
        loadingDialog.show()
        val passLoaderAnimation =
            loadingDialog.findViewById<LottieAnimationView>(R.id.pass_loading)
        passLoaderAnimation.playAnimation()
        passLoaderAnimation.repeatCount = LottieDrawable.INFINITE
    }
    private fun hideLoadingDialog() {
        if (::loadingDialog.isInitialized && loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }
}
