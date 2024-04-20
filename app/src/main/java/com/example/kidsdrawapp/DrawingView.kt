package com.example.kidsdrawapp
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import yuku.ambilwarna.AmbilWarnaDialog

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var mDrawPaths: MutableList<CustomPath> = mutableListOf()  // список для хранения всех нарисованных путей
    private var mCanvasBitmap: Bitmap? = null //это переменная для хранения битмапа
    private var mDrawPaint: Paint = Paint()
    private var mCanvasPaint: Paint = Paint()  //используется для настройки параметров рисования для холста.
    private var mBrushSize: Float = 20f //это переменная для хранения размера кисти (brush size) для рисования.
    private var color: Int = Color.BLACK
    private var canvas: Canvas? = null
    private var btnEraser: ImageButton? = null
    private var btnColor: ImageButton? = null
    private var mSeekBar: SeekBar? = null

    init {
        setUpDrawing()
    }

    fun setBackgroundImage(bitmap: Bitmap) {
        mCanvasBitmap = bitmap
        invalidate()  // Обновляем вид, чтобы отобразить новый фон
    }
    fun getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        draw(canvas)
        return bitmap
    }

    fun setColor(newColor: Int) {
        color = newColor
        mDrawPaint.color = newColor  // Устанавливаем цвет рисования
    }

    fun setButtons(eraser: ImageButton, color: ImageButton) {
        btnEraser = eraser
        btnColor = color
        // Установка обработчиков для кнопок и других элементов управления
        setUpControls()
    }

    fun setSeekBar(seekBar: SeekBar) {
        mSeekBar = seekBar
        mSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Изменяем размер кисти при изменении значения SeekBar
                mBrushSize = progress.toFloat()
                mDrawPaint.strokeWidth = mBrushSize
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Дополнительные действия при начале изменения значения SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Дополнительные действия при окончании изменения значения SeekBar
                invalidate()  // Обновляем рисунок после изменения размера кисти
            }
        })
    }


    private fun setUpControls() {
        btnEraser?.setOnClickListener {
            // Очистка списка путей
            mDrawPaths.clear()
            // Создание нового прозрачного битмапа и установка его в качестве фона
            mCanvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            mCanvasBitmap?.eraseColor(Color.TRANSPARENT)
            invalidate()
        }

        btnColor?.setOnClickListener {
            showColorPickerDialog()
        }
        // Установка обработчиков для других элементов управления, если необходимо
    }


    private fun setUpDrawing() {
        mDrawPaint.apply {
            color = this@DrawingView.color
            isAntiAlias = true
            isDither = true
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mBrushSize
        }
        mCanvasPaint.apply {
            isDither = true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)
        for (path in mDrawPaths) {
            mDrawPaint.strokeWidth = path.brushThickness
            mDrawPaint.color = path.color
            canvas.drawPath(path, mDrawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val newPath = CustomPath(color, mBrushSize)
                mDrawPaths.add(newPath)
                newPath.reset()
                if (touchX != null && touchY != null) {
                    newPath.moveTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null && touchY != null) {
                    mDrawPaths.last().lineTo(touchX, touchY)
                }
            }
            MotionEvent.ACTION_UP -> {
                // Дополнительные действия при отпускании пальца, если необходимо
            }
            else -> return false
        }

        invalidate()
        return true
    }

    private fun showColorPickerDialog() {
        val initialColor = Color.BLACK
        val dialog = AmbilWarnaDialog(context, initialColor, object :
            AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {
                // Обработка отмены выбора цвета
            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                // Устанавливаем выбранный цвет в DrawingView
                setColor(color)
            }
        })
        dialog.show()
    }

    inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
}
