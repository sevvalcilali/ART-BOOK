package com.example.artbookkotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.artbookkotlin.databinding.ActivityArtBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class ArtActivity : AppCompatActivity() {

    private var selectedBitmap: Bitmap? = null
    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

        // ⭐ Tabloyu her ihtimale karşı burada oluştur
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)"
        )

        registerLauncher()

        val info = intent.getStringExtra("info")

        if (info == "new") {
            binding.artistNameText.setText("")
            binding.artNameText2.setText("")
            binding.yearText.setText("")
            binding.button.visibility = View.VISIBLE
            val selectedImageBackground =
                BitmapFactory.decodeResource(applicationContext.resources, R.drawable.selectimage)
            binding.imageView.setImageBitmap(selectedImageBackground)
        } else {
            binding.button.visibility = View.INVISIBLE

            val selectedId = intent.getIntExtra("id", -1)
            if (selectedId != -1) {
                val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))
                val artNameIx = cursor.getColumnIndex("artname")
                val artistNameIx = cursor.getColumnIndex("artistname")
                val yearIx = cursor.getColumnIndex("year")
                val imageIx = cursor.getColumnIndex("image")

                while (cursor.moveToNext()) {
                    binding.artNameText2.setText(cursor.getString(artNameIx))
                    binding.artistNameText.setText(cursor.getString(artistNameIx))
                    binding.yearText.setText(cursor.getString(yearIx))

                    val byteArray = cursor.getBlob(imageIx)

                    // ⭐ Log: byteArray veritabanından geliyor mu?
                    Log.d("SEVO_TEST", "Veritabanından gelen byteArray size → ${byteArray.size}")

                    if (byteArray != null && byteArray.isNotEmpty()) {
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        binding.imageView.setImageBitmap(bitmap)
                    }
                }
                cursor.close()
            }

            binding.artNameText2.isEnabled = false
            binding.artistNameText.isEnabled = false
            binding.yearText.isEnabled = false
            binding.imageView.isEnabled = false
        }
    }

    fun saveButtonClick(view: View) {
        val artName = binding.artNameText2.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        // ⭐ Log: bitmap seçilmiş mi?
        Log.d("SEVO_TEST", "selectedBitmap null mu? → ${selectedBitmap == null}")

        if (selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            // ⭐ Log: kayıt edilecek byte array boyutu
            Log.d("SEVO_TEST", "Kayıt edilecek byteArray size → ${byteArray.size}")

            try {
                val sqlString =
                    "INSERT INTO arts(artname, artistname, year, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, artName)
                statement.bindString(2, artistName)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)
                statement.execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val intent = Intent(this@ArtActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Lütfen bir görsel seçin", Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int): Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio: Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1) {
            width = maximumSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maximumSize
            width = (height * bitmapRatio).toInt()
        }

        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun selectImage(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Galeri izni gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver") {
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                        }.show()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                openGallery()
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Galeri izni gerekli", Snackbar.LENGTH_INDEFINITE)
                        .setAction("İzin ver") {
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }.show()
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activityResultLauncher.launch(intentToGallery)
    }

    private fun registerLauncher() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    if (imageData != null) {
                        try {
                            selectedBitmap = if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver, imageData)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                            }
                            binding.imageView.setImageBitmap(selectedBitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                openGallery()
            } else {
                Toast.makeText(this@ArtActivity, "İzin gerekli!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
