package ru.ksart.pomodoro.presentation.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.AttrRes
import ru.ksart.pomodoro.R
import ru.ksart.pomodoro.utils.DebugHelper
import kotlin.math.max

class ProgressCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var periodMs = 0L
    private var currentMs = 0L
    private var sweepAngle = 0F

    private var colorFill = 0
    private var colorStroke = 0
    private var mStyle = FILL
    private val paint = Paint()
    private val paintStroke = Paint()

    init {
        if (attrs != null) {
            val styledAttrs = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ProgressCircleView,
                defStyleAttr,
                0
            )
            colorFill = styledAttrs.getColor(R.styleable.ProgressCircleView_colorFill, Color.RED)
            colorStroke =
                styledAttrs.getColor(R.styleable.ProgressCircleView_colorStroke, Color.RED)
            mStyle = styledAttrs.getInt(R.styleable.ProgressCircleView_fillStyle, FILL)
            // не забываем релизнуть аттрибуты, после того как их прочитали styledAttrs.recycle()
            styledAttrs.recycle()
        }
        paint.run {
            color = if (mStyle == FILL) colorFill else colorStroke
            style = if (mStyle == FILL) Paint.Style.FILL else Paint.Style.STROKE
            strokeWidth = 5F
            isAntiAlias = true
        }
        paintStroke.run {
            color = colorStroke
            style = Paint.Style.STROKE
            strokeWidth = 5F
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (periodMs == 0L || currentMs == 0L) return
        val mWidth = max(1, width - 2).toFloat()
        val mHeight = max(1, height - 2).toFloat()
        DebugHelper.log("onDraw periodMs=$periodMs currentMs=$currentMs sweepAngle=$sweepAngle")
        canvas.drawArc(
            1f,
            1f,
            mWidth,
            mHeight,
            270F,
            sweepAngle,
            true,
            paint
        )
        // нарисуем контур окружности
        if (mStyle == FILL)
            canvas.drawArc(
                1f,
                1f,
                mWidth,
                mHeight,
                0F,
                360F,
                true,
                paintStroke
            )
    }

    fun setCurrent(current: Long) {
        if (periodMs == 0L) return
        currentMs = periodMs - current
        calculateAngle()
    }

    fun setPeriod(period: Long) {
        periodMs = period
        setCurrent(0)
    }

    private fun calculateAngle() {
        // пересчитаем угол
        sweepAngle = currentMs * 360F / periodMs
        invalidate()
    }

    private companion object {
        private const val FILL = 0
    }
}
