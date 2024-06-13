package com.example.viajeai

import android.content.Intent
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DURATION: Long = 3000
    private lateinit var progressBar: ProgressBar
    private var progressStatus = 0
    private val handler = Handler()

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val animationView: ImageView = findViewById(R.id.animation_view)
        progressBar = findViewById(R.id.progress_bar)


        val animationDrawable: Drawable = animationView.drawable
        if (animationDrawable is AnimatedImageDrawable) {
            animationDrawable.start()
        }


        Thread(Runnable {
            while (progressStatus < 100) {
                progressStatus += 1
                handler.post {
                    progressBar.progress = progressStatus
                }
                try {
                    Thread.sleep(SPLASH_DURATION / 100) // ajustar para simular la carga
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }


            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }).start()
    }
}
