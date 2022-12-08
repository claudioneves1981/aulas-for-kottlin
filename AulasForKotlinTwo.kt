package com.lglf77.calculatemultiplespointsinbetweenratio

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.io.InputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.*

class AulasForKotlinTwo : AppCompatActivity() {

    private lateinit var txtCountListView: TextView
    private lateinit var txtResult: TextView
    private lateinit var progressBar: ProgressBar

    private var countArrayList = 0
    private var json: String? = null

    private val earthRadius = 6371.0 // kilometers
    private var latUser: Double = -3.75438566945327
    private var lonUser: Double = -38.53915936101184

    private var arrayStringsFinal: String? = null

    private var jsonArrayListDistances: ArrayList<Int> = arrayListOf()
    private var jsonArrayListPlaces: ArrayList<String> = arrayListOf()

    private lateinit var seekBar: SeekBar
    private lateinit var txtResultSeekBar: TextView
    var intSeekBar: Int = 20

    private val sharedPrefsSeekBar = "sharedPrefersSeekBar"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aulas_for_kotlin_two)

        supportActionBar?.hide()

        txtCountListView = findViewById(R.id.txtCountListView)
        txtResult = findViewById(R.id.txtResult)
        progressBar = findViewById(R.id.progressBar)
        seekBar = findViewById(R.id.seekBar)
        txtResultSeekBar = findViewById(R.id.txtResultSeekBar)

        progressBar.visibility = View.VISIBLE

        // carregar as preferências do SeekBar
        loadSharedPrefersSeekBar()

        val inputStream: InputStream = assets.open("calcGeoCoordsFull.json")
        json = inputStream.bufferedReader().use { it.readText() }
        Log.d("TAG", "Result 01: " + json.toString())

        try {
            val jsonArray = JSONArray(json)
            Log.d("TAG", "Result 02: $jsonArray")

            for (n in 0 until jsonArray.length()) {
                countArrayList = ++countArrayList // contagem de resultados da lista
                val jsonObject = jsonArray.getJSONObject(n)

                val arrayCitysJson = jsonObject.getString("city")
                val arrayLatsJson = jsonObject.getString("lat")
                val arrayLonsJson = jsonObject.getString("lon")
                val arrayPlaceDetailsJson = jsonObject.getString("place_details")

                // ------------------ INÍCIO - cálculos da distância ------------------
                val nearbyLatitudes = Math.toRadians(arrayLatsJson.toDouble() - latUser)
                val nearbyLongitudes = Math.toRadians(arrayLonsJson.toDouble() - lonUser)

                // senos Latitude e longitudes
                val sinNearbyLatitudes = sin(nearbyLatitudes / 2)
                val sinNearbyLongitudes = sin(nearbyLongitudes / 2)

                // Cálculos de área com senos
                val totalAreaCoefNextPlaces =
                    sinNearbyLatitudes.pow(2.0) + (sinNearbyLongitudes.pow(2.0) * cos(
                        Math.toRadians(latUser)
                    ) * cos(Math.toRadians(arrayLatsJson.toDouble())))

                val squareNearbyPlace =
                    2 * atan2(sqrt(totalAreaCoefNextPlaces), sqrt(1 - totalAreaCoefNextPlaces))
                val distanceRealUserToNearest = earthRadius * squareNearbyPlace
                val resultFinalUserToNearest: Double = distanceRealUserToNearest // Km
                val df = DecimalFormat("#,###") // #,###.00 é tipo 1.000, 00 ou 10.000,00 ou 100.000, 00
                df.roundingMode = RoundingMode.CEILING
                val formatDistanceResult = df.format(resultFinalUserToNearest)
                // Montando Array com local e resultado do cálculo de aproximação
                arrayStringsFinal = "$arrayPlaceDetailsJson, $formatDistanceResult Km"
                Log.d("TAG", "Result 03: $arrayStringsFinal") // OUTPUT: abaiara_ce_br, 405 Km

                // ------------------ FIM - cálculos da distância ------------------

                // Adiciona no array os inteiros da distância para que a prograss bar desapareça,
                // ou seja jsonArrayListDistances não será nulo ou não tem nenhum objeto nulo
                jsonArrayListDistances.add(formatDistanceResult.toInt())
                Log.d("TAG", "Result 04: " + formatDistanceResult.toInt())
                // OUTPUT: ----> calculo saiu 405, 54, 193, 280, 359

                // SE O ARRAY DOS CALCULOS DAS DISTÂNCIAS NÃO TEM ELEMENTO NULO, ENTÃO A PROGRESS BAR DESAPARECE
                if (jsonArrayListDistances.isNotEmpty()) {
                    progressBar.visibility = View.GONE
                }

                // ARRAY COM NOMES DAS CIDADES VINDO DO JSON NA ORDEM de id
                jsonArrayListPlaces.add(arrayCitysJson)
                // OUTPUT: ----> Abaiara, Acarape, Acaraú, Acopiara, Aiuaba
                Log.d("TAG", "Result 05: $arrayCitysJson")

            } // ---------------- Final do Loop FOR ----------------

            // APENAS INFORMA número 5, ou seja tem 5 jsons no ArrayJson
            txtCountListView.text = countArrayList.toString() // contagem de resultados da lista

            Log.d("TAG", "Result 06: $jsonArrayListDistances") // [405, 54, 193, 280, 359]
            Log.d("TAG", "Result 07: $jsonArrayListPlaces") // [Abaiara, Acarape, Acaraú, Acopiara, Aiuaba]

            // ATUALIZAR SEEK BAR QUANDO INICIAR O ONCREATE
            updateResultInSeekBar(intSeekBar)

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TAG", "Result 0? - Error: " + e.message.toString())
        }

        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
                txtResultSeekBar.text = seek.progress.toString()
                Log.d("TAG", "Result 08: $fromUser") // return true enquanto usuário arrasta SEEK BAR

                // se detectar TRUE, limpar SQLite antes de ocorrer a nova gravação
                // no onStopTrackingTouch(seek: SeekBar)
                if (fromUser) {
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences(sharedPrefsSeekBar, MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.apply()
                }
            }

            override fun onStartTrackingTouch(seek: SeekBar) {}
            override fun onStopTrackingTouch(seek: SeekBar) {
                intSeekBar = seek.progress
                Log.d("TAG", "Result 09: $intSeekBar")// valor atualizado da SeekBar

                // Retornar progresso para 20 caso haja valor entre 0 e 20
                if (seek.progress <= 20) {
                    txtResultSeekBar.text = "20"
                    seek.progress = 20
                    intSeekBar = 20
                }

                // gravar no SQLite quando parar de arrastar o SEEKBAR
                Handler(Looper.getMainLooper()).postDelayed({
                    val sharedPreferences: SharedPreferences =
                        getSharedPreferences(sharedPrefsSeekBar, MODE_PRIVATE)
                    val id: Int = Integer.parseInt(intSeekBar.toString())
                    val editor: SharedPreferences.Editor = sharedPreferences.edit()
                    editor.putInt("id_key", id)
                    editor.apply()
                    Toast.makeText(this@AulasForKotlinTwo,
                        "Progresso SQLite em: $id%",
                        Toast.LENGTH_SHORT)
                        .show()
                    updateResultInSeekBar(intSeekBar)
                }, 250)
            }
        })

    } // Fim do OnCreate

    @SuppressLint("SetTextI18n")
    fun updateResultInSeekBar(intSeekBar: Int): String {

        val teste: List<Int> = jsonArrayListDistances
        val result: MutableList<Int?> = ArrayList()
        val teste2: ArrayList<String> = jsonArrayListPlaces

        // Exemplo, como o calculo não é correto dependendo da localização do usuário, então
        // é necessário pegar o index de cada resultado do array distâncias para comparar com
        // resultado da lista em ordem fixa do Json Array.

        // Saída tem que ser tipo valor xxx do array 1 com index de cada elemento do array 2 nomes de cidades
        val example: MutableMap<Int, String> = HashMap()
        example[jsonArrayListDistances[0]] = toString("Abaiara")
        example[jsonArrayListDistances[1]] = toString("Acarape")
        example[jsonArrayListDistances[2]] = toString("Acaraú")
        example[jsonArrayListDistances[3]] = toString("Acopiara")
        example[jsonArrayListDistances[4]] = toString("Aiuaba")

        for (key in example.keys) {
            // Capturar valores das chaves
            val value = example[key]
            Log.d("TAG", "Result 10: $key = $value")
            // OUTPUT ------> 193 = Acaraú, 405 = Abaiara, 280 = Acopiara, 359 = Aiuaba, 54 = Acarape
        }

        teste.forEach { resultSeekBar: Int ->
            if (resultSeekBar <= intSeekBar) {
                result.add(resultSeekBar)
            } else {
                result.add(null)
            }
        }
        Log.d("TAG", "Result 11: $result") // [null, null, null, null, null], SE SEEKBAR ABAIXO DE 20

        Log.d("TAG", "Result 12: $teste2") // OUTPUT: ---> [Abaiara, Acarape, Acaraú, Acopiara, Aiuaba]
        txtResult.text =
            "\n -------- Array nomes : -------- \n" + jsonArrayListPlaces.toList() +
                    "\n\n -------- Array distâncias : -------- \n" + jsonArrayListDistances.toList() +
                    "\n\n ----- Result SeekBar ----- \n" + result +
                    "\n\n ----- New Array (citys, null) ----- \n" +
                    "novo resultado com nome das cidades neste Array, falta complementar"

        return result.toString()
    }

    private fun toString(s: String): String {
        Log.d("TAG", "Result 13: $s")
        // OUTPUT ------> Abaiara, Acarape, Acaraú, Acopiara, Aiuaba
        return s
    }

    @SuppressLint("SetTextI18n")
    private fun loadSharedPrefersSeekBar() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences(sharedPrefsSeekBar, Context.MODE_PRIVATE)
        val sharedIdValue = sharedPreferences.getInt("id_key", 0)
        if (sharedIdValue == 0) { // acessar SQLite vazio
            // se SQLITE VAZIO então retorna 0, e todos os resultado do cálculo da distância null,
            // porque o SeekBar está programado para progresso abaixo de 20, e menor calculo é 54
            // no array de distâncias
            seekBar.progress = 20
            txtResultSeekBar.text = "20"
            Log.d("TAG", "Result 14: $sharedIdValue")
            intSeekBar = 20
        } else { // acessar SQLite preenchido
            seekBar.progress = sharedIdValue
            txtResultSeekBar.text = sharedIdValue.toString()
            Log.d("TAG", "Result 15: $sharedIdValue")  // se SQLITE PREENCHIDO
            intSeekBar = sharedIdValue
        }
    }

    override fun onBackPressed() {
        // super.onBackPressed()
        recreate()
    }

}


