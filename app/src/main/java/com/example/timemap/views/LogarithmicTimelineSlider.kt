package com.example.timemap.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.timemap.R
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min

/**
 * 对数时间轴滑动条
 * 支持公元前(负数)到现在的年份选择
 * 使用对数刻度来平衡长时间跨度的显示
 */
class LogarithmicTimelineSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 年份范围
    var earliestYear: Int = -221  // 默认：秦始皇统一中国
        set(value) {
            field = value
            invalidate()
        }

    var latestYear: Int = Calendar.getInstance().get(Calendar.YEAR)
        set(value) {
            field = value
            invalidate()
        }

    // 当前选中的年份
    var selectedYear: Int = latestYear
        set(value) {
            field = value
            onYearChangedListener?.invoke(value)
            invalidate()
        }

    // 年份变化监听
    var onYearChangedListener: ((Int) -> Unit)? = null

    // 绘制相关
    private val paintTrack = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintThumb = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintTick = Paint(Paint.ANTI_ALIAS_FLAG)

    private var thumbX: Float = 0f
    private var isDragging: Boolean = false

    // 颜色
    private val trackColor = Color.parseColor("#E0E0E0")
    private val progressColor = Color.parseColor("#1565C0")
    private val thumbColor = Color.parseColor("#0D47A1")
    private val textColor = Color.parseColor("#212121")
    private val tickColor = Color.parseColor("#757575")

    // 尺寸
    private val trackHeight = 8f * resources.displayMetrics.density
    private val thumbRadius = 16f * resources.displayMetrics.density
    private val textSize = 11f * resources.displayMetrics.density
    private val tickLength = 10f * resources.displayMetrics.density

    init {
        setupPaints()
    }

    private fun setupPaints() {
        paintTrack.color = trackColor
        paintTrack.style = Paint.Style.FILL
        paintTrack.strokeCap = Paint.Cap.ROUND

        paintProgress.color = progressColor
        paintProgress.style = Paint.Style.FILL
        paintProgress.strokeCap = Paint.Cap.ROUND

        paintThumb.color = thumbColor
        paintThumb.style = Paint.Style.FILL

        paintText.color = textColor
        paintText.textSize = textSize
        paintText.textAlign = Paint.Align.CENTER

        paintTick.color = tickColor
        paintTick.style = Paint.Style.STROKE
        paintTick.strokeWidth = 2f
    }

    /**
     * 将年份转换为对数坐标位置 (0-1)
     */
    private fun yearToPosition(year: Int): Float {
        if (earliestYear >= latestYear) return 0f

        val logEarliest = log10(max(1.0, abs(earliestYear).toDouble() + 1))
        val logLatest = log10(max(1.0, latestYear.toDouble() + 1))

        return if (year >= 0) {
            // 公元后：使用对数映射
            val logYear = log10(year.toDouble() + 1)
            ((logYear - logEarliest) / (logLatest - logEarliest)).toFloat().coerceIn(0f, 1f)
        } else {
            // 公元前：线性映射到负数区域
            val totalSpan = latestYear - earliestYear
            val position = (year - earliestYear).toFloat() / totalSpan
            position.coerceIn(0f, 1f)
        }
    }

    /**
     * 将位置转换为年份
     */
    private fun positionToYear(position: Float): Int {
        val clampedPos = position.coerceIn(0f, 1f)

        // 简化处理：使用线性映射（对数映射的反向计算较复杂）
        // 为了更好的用户体验，这里使用改进的映射方式
        return if (earliestYear < 0) {
            // 有公元前的情况
            val totalSpan = latestYear - earliestYear
            val linearYear = earliestYear + (clampedPos * totalSpan).toInt()

            // 应用非线性变换，让近期年份有更精细的控制
            val normalizedPos = clampedPos
            val adjustedPos = if (normalizedPos > 0.7) {
                // 近期：更精细
                0.7f + (normalizedPos - 0.7f) * 0.5f / 0.3f
            } else {
                normalizedPos * 0.7f / 0.7f
            }

            earliestYear + (adjustedPos * totalSpan).toInt()
        } else {
            earliestYear + (clampedPos * (latestYear - earliestYear)).toInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = thumbRadius + 20f
        val left = padding
        val right = width - padding
        val top = height / 2f - trackHeight / 2
        val bottom = height / 2f + trackHeight / 2
        val centerY = height / 2f

        // 绘制轨道
        canvas.drawRoundRect(left, top, right, bottom, trackHeight / 2, trackHeight / 2, paintTrack)

        // 计算滑块位置
        val position = yearToPosition(selectedYear).coerceIn(0f, 1f)
        thumbX = left + position * (right - left)

        // 绘制进度
        canvas.drawRoundRect(left, top, thumbX, bottom, trackHeight / 2, trackHeight / 2, paintProgress)

        // 绘制刻度
        drawTicks(canvas, left, right, centerY)

        // 绘制滑块
        canvas.drawCircle(thumbX, centerY, thumbRadius, paintThumb)

        // 绘制滑块内的年份文本
        paintText.color = Color.WHITE
        paintText.textSize = textSize * 0.9f
        val yearText = if (selectedYear < 0) "${abs(selectedYear)} BC" else "$selectedYear"
        canvas.drawText(yearText, thumbX, centerY + textSize * 0.3f, paintText)
        paintText.color = textColor
        paintText.textSize = textSize
    }

    private fun drawTicks(canvas: Canvas, left: Float, right: Float, centerY: Float) {
        // 绘制主要刻度
        val tickYears = generateTickYears()

        for (year in tickYears) {
            val position = yearToPosition(year)
            val x = left + position * (right - left)

            paintTick.color = tickColor
            canvas.drawLine(x, centerY + trackHeight, x, centerY + trackHeight + tickLength, paintTick)

            // 绘制刻度标签
            if (year % 100 == 0 || year == earliestYear || year == latestYear) {
                paintText.color = textColor
                val label = if (year < 0) "${abs(year)}BC" else "$year"
                canvas.drawText(label, x, centerY + trackHeight + tickLength + textSize + 2, paintText)
            }
        }
    }

    private fun generateTickYears(): List<Int> {
        val ticks = mutableListOf<Int>()

        if (earliestYear < 0) {
            // 公元前刻度
            for (year in earliestYear..min(0, latestYear) step calculateStepForRange(earliestYear, 0)) {
                if (year % 100 == 0 || year == earliestYear) ticks.add(year)
            }
        }

        // 公元后刻度
        val startYear = max(0, earliestYear)
        for (year in startYear..latestYear step calculateStepForRange(startYear, latestYear)) {
            if (year % 100 == 0 || year == latestYear) ticks.add(year)
        }

        return ticks.distinct().sorted()
    }

    private fun calculateStepForRange(start: Int, end: Int): Int {
        val span = end - start
        return when {
            span > 2000 -> 500
            span > 1000 -> 200
            span > 500 -> 100
            span > 100 -> 50
            else -> 10
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isTouchOnThumb(event.x, event.y)) {
                    isDragging = true
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    updateYearFromPosition(event.x)
                    return true
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                return true
            }
        }

        // 点击直接跳转
        if (event.action == MotionEvent.ACTION_DOWN && !isDragging) {
            updateYearFromPosition(event.x)
            isDragging = true
            return true
        }

        return super.onTouchEvent(event)
    }

    private fun isTouchOnThumb(x: Float, y: Float): Boolean {
        val dx = x - thumbX
        val dy = y - height / 2f
        return (dx * dx + dy * dy) <= (thumbRadius * 2) * (thumbRadius * 2)
    }

    private fun updateYearFromPosition(x: Float) {
        val padding = thumbRadius + 20f
        val left = padding
        val right = width - padding

        val position = ((x - left) / (right - left)).coerceIn(0f, 1f)
        selectedYear = positionToYear(position)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (80 * resources.displayMetrics.density).toInt()
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }
}
