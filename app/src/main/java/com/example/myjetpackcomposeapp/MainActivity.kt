package com.example.myjetpackcomposeapp

import android.content.Intent

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.myjetpackcomposeapp.ui.theme.MyJetPackComposeAppTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var activityScannerPhotoLauncher: ActivityResultLauncher<IntentSenderRequest>
    private var scannerOptions: GmsDocumentScannerOptions = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(false)
        .setPageLimit(2)
        .setResultFormats(RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()
    private lateinit var scanner: GmsDocumentScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerActivityScanner()
        setContent {
            MyJetPackComposeAppTheme(
            ) {

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    BixSizeBox()
                }

            }
        }
    }


    private fun registerActivityScanner() {
        Log.d("Scanner", "register activity scanner")
        scanner = GmsDocumentScanning.getClient(scannerOptions)
        activityScannerPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->

                if (result.resultCode == RESULT_OK) {
                    val resultFromScanner = GmsDocumentScanningResult.fromActivityResultIntent(
                        result.data
                    )

                    resultFromScanner?.pdf?.uri?.path.let { path ->
                        val externalUri =
                            FileProvider.getUriForFile(this, "$packageName.provider", File(path))
                        val shareIntent =
                            Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_STREAM, externalUri)
                                type = "application/pdf"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                        startActivity(Intent.createChooser(shareIntent, "share pdf"))
                    }
                }
            }
    }

    @Composable
    fun BixSizeBox() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White)
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ButtonOpenScanner()
            }
        }
    }

    @Composable
    fun ButtonOpenScanner(modifier: Modifier = Modifier) {
        Button(
            modifier = modifier.padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            onClick = {
                Log.d("Scanner", "tapped button")
                scanner.getStartScanIntent(this).addOnSuccessListener { intentSender ->
                    Log.d("Scanner", "launched")
                    activityScannerPhotoLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                }
                    .addOnFailureListener {
                        Log.d("Scanner", "error on launching scanner ${it.message}")
                    }
            }) {
            Text(text = "Scan file")
        }
    }


}