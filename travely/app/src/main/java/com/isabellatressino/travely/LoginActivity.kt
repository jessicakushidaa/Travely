package com.isabellatressino.travely

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            buttonContinue.setOnClickListener {
                validUser(editUser.text.toString().trim(), editPassword.text.toString())
            }
        }

    }

    private fun validUser(email: String, password: String) {

        if (email.isBlank() or password.isBlank()) {
            Toast.makeText(this, "Preencha os campos para continuar", Toast.LENGTH_LONG).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                if (authResult.user?.isEmailVerified == false) {
                    auth.signOut()
                    Toast.makeText(
                        this,
                        "Por favor, ative a conta através do link enviado no email e tente novamente!",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }
                val uid = authResult.user?.uid
                if(uid != null) {
                    FirebaseFirestore.getInstance("default2")
                        .collection("users")
                        .whereEqualTo("authId", uid)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                Toast.makeText(this, "Login feito com sucesso", Toast.LENGTH_LONG)
                                    .show()
                                // Aqui você pode redirecionar para a próxima tela
                            } else {
                                Toast.makeText(
                                    this,
                                    "Usuário não encontrado na base de dados.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("[ERRO] Login Auth:", exception.message.toString())
                            Toast.makeText(
                                this,
                                "Erro ao acessar os dados do usuário.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }

            }.addOnFailureListener { exception ->
                if (exception.message.toString() == "The email address is badly formatted.") {
                    Toast.makeText(
                        this,
                        "Endereço de email inválido, por favor digite novamente!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Erro: Usuário e/ou senha inválidos.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}