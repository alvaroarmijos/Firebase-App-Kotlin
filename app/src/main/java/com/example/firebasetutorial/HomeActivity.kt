package com.example.firebasetutorial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.android.synthetic.main.activity_home.*


enum class ProviderType {
    BASIC,
    GOOGLE
}
class HomeActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //setup
        val bundle:Bundle? = intent.extras
        val email:String ? = bundle?.getString("email")
        val provider:String ? = bundle?.getString("provider")
        setup(email ?: "", provider?:"")

        //Guardar datos
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()

        //Remote config
        errorBtn.visibility = View.INVISIBLE
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener{ test ->
            if (test.isSuccessful){
                val showErrorBtn = Firebase.remoteConfig.getBoolean("show_error_btn")
                val errorBtnText = Firebase.remoteConfig.getString("error_btn_text")

                if (showErrorBtn){
                    errorBtn.visibility = View.VISIBLE
                    errorBtn.text = errorBtnText
                }
            }

        }

    }

    private fun setup(email: String, provider:String){
        title = "Inicio"

        emailTextView.text= email
        providerTextView.text = provider

        logoutBtn.setOnClickListener {
            //Borrar datos
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

        errorBtn.setOnClickListener {
            //Forzar error

            //Enviar informacion adicional
            FirebaseCrashlytics.getInstance().setUserId(email)
            FirebaseCrashlytics.getInstance().setCustomKey("provider", provider)

            //Enviar log de cconttexto
            FirebaseCrashlytics.getInstance().log("Se ha pulzado el boton Forzar Error")

            throw RuntimeException("Forzado de error")
        }

        saveBtn.setOnClickListener {

            db.collection("users_data").document(email).set(
                hashMapOf("provider" to provider,
                "addresss" to addressEditText.text.toString(),
                "phone" to phoneEditText.text.toString())
            )
        }

        getBtn.setOnClickListener {
            db.collection("users_data").document(email).get().addOnSuccessListener {
                addressEditText.setText(it.get("addresss") as String?)
                phoneEditText.setText(it.get("phone") as String?)
            }
        }

        deleteBtn.setOnClickListener {
            db.collection("users_data").document(email).delete()
        }

    }
}