package com.ebuspass.smartpassapp
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.ebuspass.smartpassapp.databinding.ActivityInfoBinding
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class InfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoBinding
    private lateinit var dataBase: DatabaseReference
    private lateinit var dataBaseTc: DatabaseReference
    private val request: GetPhoneNumberHintIntentRequest =
        GetPhoneNumberHintIntentRequest.builder().build()
    private lateinit var phoneNumber: EditText
    private lateinit var phoneNumber2: EditText
    private lateinit var passLoadingRegister: LottieAnimationView
    private lateinit var passLoadingLogin: LottieAnimationView
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val phoneNumberHintIntentResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                try {
                    var phoneNum =
                        Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                    phoneNum = phoneNum.replace("+191", "")
                    phoneNum = phoneNum.replace("+91", "")
                    phoneNumber.setText("+91$phoneNum")
                } catch (e: Exception) {
                    Log.e(TAG, "Phone Number Hint failed")
                }
            }
        dataBase = FirebaseDatabase.getInstance().getReference("Users")
        dataBaseTc = FirebaseDatabase.getInstance().getReference("TicketChecker")
        binding.animationView.playAnimation()
        binding.animationView.repeatCount = LottieDrawable.INFINITE
        val registerDialog = BottomSheetDialog(this)
        val registerView = layoutInflater.inflate(R.layout.register_bottom_sheet, null)
        binding.registerButton.setOnClickListener {
            registerDialog.setContentView(registerView)
            registerDialog.show()
            passLoadingRegister = registerView.findViewById(R.id.pass_loading)
            hideProgressAnimation(passLoadingRegister)
            Identity.getSignInClient(this).getPhoneNumberHintIntent(request)
                .addOnSuccessListener { result: PendingIntent ->
                    try {
                        phoneNumberHintIntentResultLauncher.launch(
                            IntentSenderRequest.Builder(result).build()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Launching the PendingIntent failed")
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "Phone Number Hint failed")
                }
            val button: MaterialButton = registerView.findViewById(R.id.register_button)
            val name: EditText = registerView.findViewById(R.id.editTextName)
            phoneNumber = registerView.findViewById(R.id.editTextPhoneNumber)
            val radioButtonGroup: RadioGroup = registerView.findViewById(R.id.radioGroupGender)
            val password: EditText = registerView.findViewById(R.id.editTextPassword)

            button.setOnClickListener {
                showProgressAnimation(passLoadingRegister)
                if (name.text.isEmpty() || phoneNumber.text.isEmpty() || password.text.isEmpty()) {
                    Snackbar.make(registerView, "Enter all details", Snackbar.LENGTH_SHORT).show()
                    hideProgressAnimation(passLoadingRegister)
                } else if (!phoneNumber.text.startsWith("+91") || phoneNumber.text.length != 13) {
                    Snackbar.make(
                        registerView,
                        "Enter correct Phone Number with +91 country code",
                        Snackbar.LENGTH_LONG
                    ).show()
                    hideProgressAnimation(passLoadingRegister)
                } else if (radioButtonGroup.checkedRadioButtonId == -1) {
                    Snackbar.make(
                        registerView, "Select gender", Snackbar.LENGTH_LONG
                    ).show()
                    hideProgressAnimation(passLoadingRegister)
                } else {
                    val phoneNumberToCheck = phoneNumber.text.toString()
                    val checkedRadioButtonId = radioButtonGroup.checkedRadioButtonId
                    val radioButton: RadioButton = registerView.findViewById(checkedRadioButtonId)
                    val selectedGender = radioButton.text.toString()
                    dataBase.orderByChild("phoneNumber").equalTo(phoneNumberToCheck)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    Snackbar.make(
                                        registerView,
                                        "Account already exists with this phone number, try signing in",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    hideProgressAnimation(passLoadingRegister)
                                } else {
                                    val user = AllUsers(
                                        name.text.toString(),
                                        phoneNumberToCheck,
                                        selectedGender,
                                        password.text.toString()
                                    )
                                    val userKey = dataBase.push().key
                                    dataBase.child(userKey!!).setValue(user).addOnSuccessListener {
                                        name.text.clear()
                                        phoneNumber.text.clear()
                                        radioButton.isChecked = false
                                        password.text.clear()
                                        val intent =
                                            Intent(applicationContext, MainActivity::class.java)
                                        intent.putExtra("phone_number", phoneNumber.toString())
                                        startActivity(intent)
                                        finish()
                                        hideProgressAnimation(passLoadingRegister)
                                        val sharedPreferences =
                                            getSharedPreferences("FDatabase", Context.MODE_PRIVATE)
                                        val editor = sharedPreferences.edit()
                                        editor.putBoolean("isLoggedIn", true)
                                        editor.apply()
                                    }.addOnFailureListener {
                                        Snackbar.make(
                                            registerView,
                                            "Registration Failed",
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                        hideProgressAnimation(passLoadingRegister)
                                    }
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                Log.e(TAG, "Database Error: ${error.message}")
                                hideProgressAnimation(passLoadingRegister)
                            }
                        })
                }
            }
        }
        val phoneNumberHintIntentResultLauncher2 =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                try {
                    var phoneNum =
                        Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                    phoneNum = phoneNum.replace("+191", "")
                    phoneNumber2.setText("+91$phoneNum")
                } catch (e: Exception) {
                    Log.e(TAG, "Phone Number Hint failed")
                }
            }
        val loginDialog = BottomSheetDialog(this)
        val loginView = layoutInflater.inflate(R.layout.login_bottom_sheet, null)
        phoneNumber2 = loginView.findViewById(R.id.editTextPhoneNumber)
        val password: EditText = loginView.findViewById(R.id.editTextPassword)
        val loginButton: MaterialButton = loginView.findViewById(R.id.login_button)
        passLoadingLogin = loginView.findViewById(R.id.pass_loading)
        binding.loginButton.setOnClickListener {
            loginDialog.setContentView(loginView)
            loginDialog.show()
            hideProgressAnimation(passLoadingLogin)
            Identity.getSignInClient(this).getPhoneNumberHintIntent(request)
                .addOnSuccessListener { result: PendingIntent ->
                    try {
                        phoneNumberHintIntentResultLauncher2.launch(
                            IntentSenderRequest.Builder(result).build()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Launching the PendingIntent failed")
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "Phone Number Hint failed")
                }
            loginButton.setOnClickListener {
                showProgressAnimation(passLoadingLogin)
                val userPhoneNumber = phoneNumber2.text.toString()
                val userPassword = password.text.toString()
                val valueEventListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var isValidCredentials = false
                        if (userPhoneNumber.isEmpty() && userPassword.isEmpty()) {
                            Snackbar.make(loginView, "Enter all details", Snackbar.LENGTH_SHORT)
                                .show()
                            hideProgressAnimation(passLoadingLogin)
                        } else {
                            for (userSnapshot in dataSnapshot.children) {
                                val allUsers = userSnapshot.getValue(AllUsers::class.java)
                                if (allUsers != null && allUsers.phoneNumber == userPhoneNumber && allUsers.password == userPassword) {
                                    isValidCredentials = true
                                    break
                                }
                            }
                            if (isValidCredentials) {
                                startActivity(Intent(this@InfoActivity, MainActivity::class.java))
                                val sharedPreferences =
                                    getSharedPreferences("FDatabase", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putBoolean("isLoggedIn", true)
                                editor.apply()
                                loginDialog.dismiss()
                                hideProgressAnimation(passLoadingLogin)

                            } else {
                                hideProgressAnimation(passLoadingLogin)
                                Snackbar.make(
                                    loginView, "Invalid credentials", Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                        hideProgressAnimation(passLoadingLogin)
                        Snackbar.make(
                            loginView, "Error: ${databaseError.message}", Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
                dataBase.orderByChild("phoneNumber").equalTo(userPhoneNumber)
                    .addListenerForSingleValueEvent(valueEventListener)
            }
        }
    }
    private fun showProgressAnimation(animationView: LottieAnimationView) {
        animationView.visibility = View.VISIBLE
        animationView.playAnimation()
        animationView.repeatCount = LottieDrawable.INFINITE
    }
    private fun hideProgressAnimation(animationView: LottieAnimationView) {
        animationView.visibility = View.GONE
        animationView.cancelAnimation()
    }
}