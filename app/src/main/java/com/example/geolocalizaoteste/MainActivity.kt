package com.example.vivs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.geolocalizaoteste.R
import com.example.geolocalizaoteste.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private val locationRequestCode = 1000  // Renomeado para seguir convenções de Kotlin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o Data Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // Inicializa o cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Verifica se a permissão de localização foi concedida; caso contrário, solicita a permissão
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationRequestCode
            )
        } else {
            getLastKnownLocation()
        }

        // Ação do botão flutuante
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Obtendo localização e enviando ao servidor", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
            getLastKnownLocation()
        }
    }

    // Função para obter a última localização conhecida
    fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    sendLocationToServer(latitude, longitude)
                }
            }
        } else {
            // Caso a permissão não tenha sido concedida
            Snackbar.make(binding.root, "Permissão de localização não concedida.", Snackbar.LENGTH_SHORT).show()
        }
    }

    // Função para enviar a localização ao servidor usando Retrofit
    private fun sendLocationToServer(latitude: Double, longitude: Double) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost:8080/")  // Substitua pelo URL do seu servidor de teste
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)
        val locationData = LocationData(latitude, longitude)

        apiService.sendLocation(locationData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Snackbar.make(binding.root, "Localização enviada com sucesso!", Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(binding.root, "Falha ao enviar localização.", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Snackbar.make(binding.root, "Erro: ${t.message}", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    // Manipula a resposta da solicitação de permissão do usuário
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation()
            } else {
                Snackbar.make(binding.root, "Permissão de localização negada.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
