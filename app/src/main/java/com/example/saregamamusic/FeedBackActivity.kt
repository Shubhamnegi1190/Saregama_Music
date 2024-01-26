package com.example.saregamamusic

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.widget.Toast
import com.example.saregamamusic.databinding.ActivityFeedBackBinding
import com.example.saregamamusic.databinding.ActivitySelectionBinding
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message.RecipientType
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@Suppress("DEPRECATION")
class FeedBackActivity : AppCompatActivity() {

    lateinit var binding: ActivityFeedBackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_SaregamaMusic)
        binding = ActivityFeedBackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "FeedBack"


        binding.SendFeedBack.setOnClickListener {
            val feedbackmsg = binding.feedbackMsgFA.text.toString() + "\n" + binding.emailFA.text.toString()
            val subject = binding.topic.text.toString()
            val userName = "negih2126@gmail.com"
            val pass = "7217746412@123"
            val cm  =  this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if(feedbackmsg.isNotEmpty() && subject.isNotEmpty() && (cm.activeNetworkInfo?.isConnectedOrConnecting == true)){
                Thread{
                    try {
                        val properties = Properties()
                        properties["mail.smtp.auth"] = "true"
                        properties["mail.smtp.starttls.enable"] = "true"
                        properties["mail.smtp.host"] = "smtp.gmail.com"
                        properties["mail.smtp.port"] = "587"
                        val session = Session.getInstance(properties , object :Authenticator(){
                            override fun getPasswordAuthentication(): PasswordAuthentication {
                                return PasswordAuthentication(userName, pass)
                            }
                        })
                        val mail = MimeMessage(session)
                        mail.subject = subject
                        mail.setText(feedbackmsg)
                        mail.setFrom(InternetAddress(userName))
                        mail.setRecipients( RecipientType.TO, InternetAddress.parse(userName))
                        Transport.send(mail)
                    }catch (e:Exception) {Toast.makeText(this , e.toString() , Toast.LENGTH_LONG).show()}

                }.start()
                Toast.makeText(this , "Thanks for your Feedback" , Toast.LENGTH_LONG).show()
                  finish()
            }

            else Toast.makeText(this , "SomeThing went wrong!" , Toast.LENGTH_LONG).show()
        }
    }
}