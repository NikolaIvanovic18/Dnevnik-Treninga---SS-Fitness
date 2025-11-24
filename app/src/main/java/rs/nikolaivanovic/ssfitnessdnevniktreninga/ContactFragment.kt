package rs.nikolaivanovic.ssfitnessdnevniktreninga

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.core.net.toUri

class ContactFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contact, container, false)

        val instagramStefan = view.findViewById<LinearLayout>(R.id.instagramStefanClickable)
        instagramStefan.setOnClickListener {
            val uri = "https://instagram.com/stefaninho__966".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        val instagramSGYM = view.findViewById<LinearLayout>(R.id.instagramSGYMClickable)
        instagramSGYM.setOnClickListener {
            val uri = "https://instagram.com/teretanasgym".toUri()
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }

        val phone = view.findViewById<TextView>(R.id.phoneNumber)
        phone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = "tel:+381612134400".toUri()
            startActivity(intent)
        }

        return view
    }
}