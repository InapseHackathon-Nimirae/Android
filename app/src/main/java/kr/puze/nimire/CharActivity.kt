package kr.puze.nimire

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.actionbar_char.*
import kotlinx.android.synthetic.main.activity_char.*
import kotlinx.android.synthetic.main.activity_main.*

class CharActivity : AppCompatActivity() {

    private val CHAR_ACTIVITY = 100
    private val MAIN_ACTIVITY = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_char)
        supportActionBar!!.hide()



        var image = intent.getStringExtra("gif")

        Glide.with(applicationContext)
                .load(image)
                .into(gif)

        text.text = intent.getStringExtra("text")

        actionbar_back.setOnClickListener {
            finish()
        }
    }
}
