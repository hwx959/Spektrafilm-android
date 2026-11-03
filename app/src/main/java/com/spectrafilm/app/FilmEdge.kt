/*
 * Spektrafilm for Android — film edge (片边). GPLv3.
 * Film modeling powered by spektrafilm.
 *
 * 把成片印到一条片基上。画幅模型与配色对着 CN 版（SpektraLab-CN）的 FilmEdgeCompositor
 * 还原：他家只有 OFF / 120 / 135 三种，配色由 Color.rgb() 现算，取值如下——
 *
 *   135   片基 rgb(8,9,8) 近黑 · 边缘字 rgb(232,126,44) 橙 · 齿孔白 + rgb(182,168,168) 描边
 *   120   同样近黑片基，但没有齿孔（120 卷片本来就没有），只在片基上印字
 *   白边  片基 rgb(240,236,228) 米白 · 字 rgb(28,28,28) 近黑
 *
 * 另有帧号、条码开关、日期背印三项（CN 版同样是独立设置项）。
 *
 * 关键在于它是**在像素上合成**，不是画在 UI 上的装饰层：预览位图合完再交给画布，
 * 于是缩放、拖动、前后对比全部不用改一行；导出时对全尺寸位图跑同一个函数，所见即所得。
 * 切换画幅不需要重跑引擎——照片已经渲染好了，只是外面多了一圈片基。
 */
package com.spectrafilm.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 片边画幅。NONE 表示不合成。 */
enum class FilmEdgeStyle {
    NONE,

    /** 135：近黑片基 + 白齿孔 + 橙色片型号/帧号 + DX 条码带。 */
    F135,

    /** 120：近黑片基 + 橙色印字，无齿孔、无条码。 */
    F120,

    /** 白边：米白片基 + 近黑小字，无齿孔。 */
    MINIMAL,
}

/** 片边设置。日期为空时不印。 */
data class FilmEdgeSettings(
    val style: FilmEdgeStyle = FilmEdgeStyle.NONE,
    val dateText: String? = null,
    val frameNumber: Int = 18,
    /** DX 条码带只在 135 上出现，可单独关掉。 */
    val barcode: Boolean = true,
)

private class Margins(val top: Int, val bottom: Int, val left: Int, val right: Int)

// CN 版 FilmEdgeCompositor 的实测配色
private const val BASE_BLACK = 0xFF080908.toInt()
private const val BASE_WHITE = 0xFFF0ECE4.toInt()
private const val INK_ORANGE = 0xFFE87E2C.toInt()
private const val INK_DARK = 0xFF1C1C1C.toInt()
private const val PERF_WHITE = 0xFFFFFFFF.toInt()
private const val PERF_EDGE = 0xFFB6A8A8.toInt()
private const val DX_AMBER = 0xFFD9A227.toInt()

/**
 * 把 [src] 印到一条片基上，返回新的位图。样式为 [FilmEdgeStyle.NONE] 时直接返回 [src]。
 *
 * 新位图沿用 [src] 的颜色空间：JPEG/PNG 导出会校验位图的 ColorSpace 与输出描述符一致，
 * 换了空间就会被导出路径拒绝。
 */
fun composeFilmEdge(src: Bitmap, settings: FilmEdgeSettings, stockLabel: String): Bitmap {
    if (settings.style == FilmEdgeStyle.NONE) return src

    val w = src.width
    val h = src.height
    val m = marginsFor(settings.style, w, h)
    val outW = w + m.left + m.right
    val outH = h + m.top + m.bottom

    // API 26+ 才能指定位图颜色空间；24–25 退回默认 sRGB（那两版导出本来也只支持 sRGB）
    val out = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val space = src.colorSpace
            ?: android.graphics.ColorSpace.get(android.graphics.ColorSpace.Named.SRGB)
        Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888, false, space)
    } else {
        Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
    }
    val canvas = Canvas(out)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val whiteBase = settings.style == FilmEdgeStyle.MINIMAL
    paint.color = if (whiteBase) BASE_WHITE else BASE_BLACK
    canvas.drawRect(0f, 0f, outW.toFloat(), outH.toFloat(), paint)

    // 照片本体
    canvas.drawBitmap(src, m.left.toFloat(), m.top.toFloat(), null)

    val imageRight = (m.left + w).toFloat()
    val imageBottom = (m.top + h).toFloat()

    when (settings.style) {
        FilmEdgeStyle.F135 -> draw135(
            canvas, paint, outW, outH, m, imageRight, imageBottom,
            stockLabel, settings.frameNumber, settings.barcode,
        )
        FilmEdgeStyle.F120 -> draw120(
            canvas, outW, outH, m, stockLabel, settings.frameNumber,
        )
        FilmEdgeStyle.MINIMAL -> drawMinimal(
            canvas, outW, outH, m, stockLabel, settings.frameNumber,
        )
        FilmEdgeStyle.NONE -> Unit
    }

    // 橙色 LED 味日期：打在画面右下角内侧
    settings.dateText?.takeIf { it.isNotBlank() }?.let { date ->
        val dp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = (m.top * 0.26f).coerceAtLeast(10f)
            color = 0xFFFF8A1E.toInt()
        }
        val pad = m.top * 0.26f
        canvas.drawText(date, imageRight - dp.measureText(date) - pad, imageBottom - pad, dp)
    }

    return out
}

/** 一排齿孔：白色填充 + 浅灰描边，横向均匀分布。 */
private fun drawPerfRow(
    canvas: Canvas, outW: Int, left: Int, right: Int, centerY: Float, perfH: Float,
) {
    val perfW = perfH * 1.30f
    val gap = perfW * 1.02f
    val span = (right - left).toFloat()
    val count = ((span + gap) / (perfW + gap)).toInt().coerceAtLeast(2)
    val totalW = count * perfW + (count - 1) * gap
    val startX = left + (span - totalW) / 2f
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = PERF_WHITE }
    val edge = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = (perfH * 0.05f).coerceAtLeast(1f)
        color = PERF_EDGE
    }
    for (i in 0 until count) {
        val x = startX + i * (perfW + gap)
        val r = RectF(x, centerY - perfH / 2f, x + perfW, centerY + perfH / 2f)
        canvas.drawRoundRect(r, perfW * 0.16f, perfW * 0.16f, fill)
        canvas.drawRoundRect(r, perfW * 0.16f, perfW * 0.16f, edge)
    }
    @Suppress("UNUSED_EXPRESSION") outW
}

private fun draw135(
    canvas: Canvas, paint: Paint, outW: Int, outH: Int, m: Margins,
    imageRight: Float, imageBottom: Float, stockLabel: String, frame: Int, barcode: Boolean,
) {
    val band = m.top.toFloat()
    val perfH = band * 0.30f
    val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = (band * 0.24f).coerceAtLeast(9f)
        color = INK_ORANGE
    }
    // 上带：文字在上，齿孔在下
    canvas.drawText(stockLabel.uppercase(), m.left + outW * 0.006f, band * 0.28f, ink)
    val mid = "${frame}A"
    canvas.drawText(mid, (outW - ink.measureText(mid)) / 2f, band * 0.28f, ink)
    val next = "${frame + 1}"
    canvas.drawText(next, outW - m.right - ink.measureText(next), band * 0.28f, ink)
    drawPerfRow(canvas, outW, m.left, outW - m.right, band * 0.68f, perfH)

    // 下带：齿孔在上，条码带在下
    val bottomBand = outH - imageBottom
    drawPerfRow(canvas, outW, m.left, outW - m.right, imageBottom + bottomBand * 0.22f, perfH)

    if (barcode) {
        val bcTop = imageBottom + bottomBand * 0.48f
        paint.color = DX_AMBER
        canvas.drawRect(0f, bcTop, outW.toFloat(), outH.toFloat(), paint)
        val tick = Paint(paint).apply { color = 0xFF12100C.toInt() }
        val unit = outW / 190f
        var x = 0f
        var i = 0
        val pattern = intArrayOf(1, 3, 1, 2, 4, 1, 2, 1, 3, 2, 1, 1, 4, 2, 1, 3)
        while (x < outW) {
            val wUnit = pattern[i % pattern.size]
            canvas.drawRect(x, bcTop, x + unit * wUnit, outH.toFloat(), tick)
            x += unit * (wUnit + 2)
            i++
        }
        val bcText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = bottomBand * 0.24f
            color = 0xFF12100C.toInt()
        }
        val baseline = outH - bottomBand * 0.16f
        val leftLabel = "→ ${frame}A"
        val rightLabel = "${frame + 1}"
        // 文字底衬：先把那两段条码挖空再写字，真实 DX 码就是这么排的
        paint.color = DX_AMBER
        canvas.drawRect(m.left.toFloat(), bcTop, m.left + bcText.measureText(leftLabel) + unit * 5f, outH.toFloat(), paint)
        canvas.drawRect(
            outW - m.right - bcText.measureText(rightLabel) - unit * 5f,
            bcTop, outW.toFloat(), outH.toFloat(), paint,
        )
        canvas.drawText(leftLabel, m.left + outW * 0.02f, baseline, bcText)
        canvas.drawText(
            rightLabel,
            outW - m.right - outW * 0.02f - bcText.measureText(rightLabel),
            baseline,
            bcText,
        )
    }

    // 照片四周一道白细边
    val stroke = (band * 0.04f).coerceAtLeast(1.5f)
    val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        color = PERF_WHITE
    }
    canvas.drawRect(
        RectF(
            m.left - stroke / 2f,
            m.top - stroke / 2f,
            imageRight + stroke / 2f,
            imageBottom + stroke / 2f,
        ),
        line,
    )
}

/** 120 卷片没有齿孔，只在片基上印字。 */
private fun draw120(
    canvas: Canvas, outW: Int, outH: Int, m: Margins, stockLabel: String, frame: Int,
) {
    val band = m.top.toFloat()
    val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        textSize = (band * 0.30f).coerceAtLeast(9f)
        color = INK_ORANGE
    }
    canvas.drawText(stockLabel.uppercase(), m.left + outW * 0.01f, band * 0.58f, ink)
    val right = "${frame}A"
    canvas.drawText(right, outW - m.right - outW * 0.01f - ink.measureText(right), band * 0.58f, ink)
    canvas.drawText("120  ·  ${frame}A", m.left + outW * 0.01f, outH - m.bottom * 0.42f, ink)
}

private fun drawMinimal(
    canvas: Canvas, outW: Int, outH: Int, m: Margins, stockLabel: String, frame: Int,
) {
    val ink = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        textSize = (m.bottom * 0.28f).coerceAtLeast(8f)
        color = 0x991C1C1C.toInt()
    }
    canvas.drawText(
        "$stockLabel  ·  ${frame}A",
        m.left + outW * 0.012f,
        outH - m.bottom * 0.45f,
        ink,
    )
}

/** 边距按短边比例给，横幅竖幅都不会失衡。 */
private fun marginsFor(style: FilmEdgeStyle, w: Int, h: Int): Margins {
    val short = minOf(w, h).toFloat()
    return when (style) {
        // 上带放型号与齿孔，下带放齿孔 + 条码，所以下带更高
        FilmEdgeStyle.F135 -> {
            val top = (short * 0.115f).toInt().coerceAtLeast(14)
            val bottom = (short * 0.165f).toInt().coerceAtLeast(20)
            val side = (short * 0.012f).toInt().coerceAtLeast(3)
            Margins(top, bottom, side, side)
        }
        // 120 的片基更宽（无齿孔，整条都是片基）
        FilmEdgeStyle.F120 -> {
            val top = (short * 0.145f).toInt().coerceAtLeast(16)
            val bottom = (short * 0.145f).toInt().coerceAtLeast(16)
            val side = (short * 0.018f).toInt().coerceAtLeast(4)
            Margins(top, bottom, side, side)
        }
        FilmEdgeStyle.MINIMAL -> {
            val side = (short * 0.045f).toInt().coerceAtLeast(10)
            val bottom = (short * 0.11f).toInt().coerceAtLeast(24)
            Margins(side, bottom, side, side)
        }
        FilmEdgeStyle.NONE -> Margins(0, 0, 0, 0)
    }
}

/**
 * 片边选择面板：画幅 + 帧号 + 条码 + 日期，选完立刻生效。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FilmEdgeSheet(
    current: FilmEdgeSettings,
    onPick: (FilmEdgeStyle) -> Unit,
    onDateToggle: (Boolean) -> Unit,
    onBarcodeToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("片边", style = MaterialTheme.typography.headlineSmall)
            Text(
                "把成片印到一条片基上：齿孔、边缘印字、DX 条码。只在导出 JPEG / 8 位 PNG 时合成。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilmEdgeStyle.entries.forEach { style ->
                val label = when (style) {
                    FilmEdgeStyle.NONE -> "关闭"
                    FilmEdgeStyle.F135 -> "135 画幅"
                    FilmEdgeStyle.F120 -> "120 画幅"
                    FilmEdgeStyle.MINIMAL -> "极简白边"
                }
                val selected = style == current.style
                Surface(
                    onClick = { onPick(style) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("DX 条码带（仅 135）", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = current.barcode, onCheckedChange = onBarcodeToggle)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("日期背印", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = current.dateText != null, onCheckedChange = onDateToggle)
            }
        }
    }
}

/** 画布左上角的小按钮：当前画幅一眼可见，点开面板。 */
@Composable
internal fun FilmEdgeChip(active: FilmEdgeStyle, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val label = when (active) {
        FilmEdgeStyle.NONE -> "片边"
        FilmEdgeStyle.F135 -> "片边 · 135"
        FilmEdgeStyle.F120 -> "片边 · 120"
        FilmEdgeStyle.MINIMAL -> "片边 · 白边"
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (active == FilmEdgeStyle.NONE) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.78f)
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        modifier = modifier,
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
