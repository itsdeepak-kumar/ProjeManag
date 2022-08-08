package com.example.trelloclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.trelloclone.databinding.ActivitySplashScreenBinding
import com.example.trelloclone.firebase.fireStoreClass

class SplashScreenActivity : AppCompatActivity() {
    private var binding : ActivitySplashScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

//        val typeFace : Typeface = Typeface.createFromAsset(assets, "carbon bl.ttf")
//        binding?.tvAppName!!.typeface = typeFace

        Handler().postDelayed({

            var currentUserId = fireStoreClass().getCurrentUserID()

            if(currentUserId.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }

            finish()
        },2500)

    }
}