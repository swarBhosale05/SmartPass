package com.ebuspass.smartpassapp
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ebuspass.smartpassapp.databinding.ActivityInformationBinding
class InformationActivity : AppCompatActivity() {
    private lateinit var binding : ActivityInformationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.sbLinkedIn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/swarada-bhosale-167754247/"))
            startActivity(intent)
        }
        binding.sbMail.setOnClickListener{
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:swarbhosale55@gmail.com")
            }
            startActivity(intent)
        }
        binding.abLinkedIn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/aditi-bankar-65a228280/"))
            startActivity(intent)
        }
        binding.abMail.setOnClickListener{
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:aditibankar26@gmail.com")
            }
            startActivity(intent)
        }
        binding.ab2LinkedIn.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/aditi-badiger-b16b93278/"))
            startActivity(intent)
        }
        binding.ab2Mail.setOnClickListener{
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:badigeraditi@gmail.com")
            }
            startActivity(intent)
        }
    }
}