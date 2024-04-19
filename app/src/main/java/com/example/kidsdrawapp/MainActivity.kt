package com.example.kidsdrawapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var drawingView: DrawingView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //val drawingView: DrawingView = findViewById(R.id.drawing_view)
        drawingView = findViewById(R.id.drawing_view)
        val btnColor: ImageButton = findViewById(R.id.btnColor)
        val btnEraser: ImageButton = findViewById(R.id.btnEraser)
        val seekBar: SeekBar = findViewById(R.id.penSize)

        // Установка обработчика клика на кнопку "Выбрать цвет"
        btnColor.setOnClickListener {
            showColorPickerDialog(drawingView)
        }
        drawingView.setButtons(btnEraser, btnColor)
        drawingView.setSeekBar(seekBar)
        val btnOptionsMenu: ImageButton = findViewById(R.id.btnOptionsMenu)
        btnOptionsMenu.setOnClickListener {
            val popupMenu = PopupMenu(this, btnOptionsMenu)
            popupMenu.menuInflater.inflate(R.menu.pop, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_button1 -> {
                            pickImageFromGallery()
                            true
                        }
                        R.id.action_button2 -> {
                            saveDrawingToStorage(drawingView.getBitmap(), "filename") // замените "filename" на соответствующее имя файла
                            true
                        }
                        R.id.action_button3 -> {
                            shareDrawing(drawingView.getBitmap()) // передайте ваш объект Bitmap
                            true
                        }
                        else -> false
                    }
                }

                popupMenu.show()
            }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            Log.d("MyActivity", "imageUri: $imageUri") // Добавьте эту строку для отладочного вывода
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            Log.d("MyActivity", "bitmap: $bitmap") // Добавьте эту строку для отладочного вывода
            drawingView.setBackgroundImage(bitmap)
        }
    }


    private fun saveDrawingToStorage(bitmap: Bitmap, fileName: String) {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val uniqueFileName = "drawing_$timeStamp.png"

        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YourFolderName")
        if (!folder.exists()) {
            folder.mkdirs()
        }
        val file = File(folder, uniqueFileName)
        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            Toast.makeText(this, "Рисунок сохранен", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun shareDrawing(bitmap: Bitmap) {
        val cachePath = File(externalCacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "image.png")
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val uri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        startActivity(Intent.createChooser(shareIntent, "Поделиться с помощью"))
    }

    private fun showColorPickerDialog(drawingView: DrawingView) {
        val initialColor = Color.BLACK
        val dialog = AmbilWarnaDialog(this, initialColor, object :
            AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                // Обработка отмены выбора цвета
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                // Устанавливаем выбранный цвет в DrawingView
                drawingView.setColor(color)
                drawingView.invalidate()  // Обновляем рисунок после изменения цвета
            }
        })
        dialog.show()
    }
}
