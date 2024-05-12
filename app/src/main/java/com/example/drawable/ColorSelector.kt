package com.example.drawable

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff.Mode
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.toRect
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor


class ColorSelector : Fragment() {


    var huePanelSize = 275;
    @Composable
    fun HueBar(
        setColor: (Float) -> Unit
    ) {
        val scope = rememberCoroutineScope()
        val interactionSource = remember {
            MutableInteractionSource()
        }
        val pressOffset = remember {
            mutableStateOf(Offset.Zero)
        }
        Canvas(
            modifier = Modifier
                .height(18.dp)
                .width(500.dp)
                .clip(RoundedCornerShape(50))
                .emitDragGesture(interactionSource)
        ) {
            val drawScopeSize = size
            val bitmap = Bitmap.createBitmap(800, 50,
                Bitmap.Config.ARGB_8888
            )
            val hueCanvas = Canvas(bitmap)
            val huePanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

            val hueColors = IntArray((huePanel.width()).toInt())
            var hue = 0f

            for (i in hueColors.indices) {
                hueColors[i] = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))
                hue += 360f / hueColors.size
            }

            val linePaint = Paint()
            linePaint.strokeWidth = 0F
            for (i in hueColors.indices) {
                linePaint.color = hueColors[i]
                hueCanvas.drawLine(i.toFloat(), 0F, i.toFloat(), huePanel.bottom, linePaint)
            }

            drawBitmap(
                bitmap = bitmap,
                panel = huePanel
            )

            fun pointToHue(pointX: Float): Float {
                val width = huePanel.width()
                val x = when {
                    pointX < huePanel.left -> 0F
                    pointX > huePanel.right -> width
                    else -> pointX - huePanel.left
                }
                return x * 360f / width
            }

            scope.collectForPress(interactionSource) { pressPosition ->
                val pressPos = pressPosition.x.coerceIn(0f..drawScopeSize.width)
                pressOffset.value = Offset(pressPos, 0f)
                val selectedHue = pointToHue(pressPos)
                setColor(selectedHue)
            }

            drawCircle(
                color = Color.White,
                radius = size.height / 2,
                center = Offset(pressOffset.value.x, size.height / 2),
                style = Stroke(
                    width = 2.dp.toPx()
                )
            )
        }
    }

    fun CoroutineScope.collectForPress(
        interactionSource: InteractionSource,
        setOffset: (Offset) -> Unit
    ) {
        launch {
            interactionSource.interactions.collect { interaction ->
                (interaction as? PressInteraction.Press)
                    ?.pressPosition
                    ?.let(setOffset)
            }
        }
    }

    private fun Modifier.emitDragGesture(
        interactionSource: MutableInteractionSource
    ): Modifier = composed {
        val scope = rememberCoroutineScope()
        pointerInput(Unit) {
            detectDragGestures { input, _ ->
                scope.launch {
                    interactionSource.emit(PressInteraction.Press(input.position))
                }
            }
        }.clickable(interactionSource, null) {
        }
    }

    private fun DrawScope.drawBitmap(
        bitmap: Bitmap,
        panel: RectF
    ) {
        drawIntoCanvas {
            it.nativeCanvas.drawBitmap(
                bitmap,
                null,
                panel.toRect(),
                null
            )
        }
    }

    /**************  Hue Bar  ************/
    @Composable
    fun HuePanel() {
        val interactionSource = remember {
            MutableInteractionSource()
        }
        val scope = rememberCoroutineScope()
        var sat: Float
        var value: Float
        val pressOffset = remember {
            mutableStateOf(Offset.Zero)
        }
        Canvas(
            modifier = Modifier
                .size(huePanelSize.dp)
                .emitDragGesture(interactionSource)
                .clip(RoundedCornerShape(12.dp))
        ) {
            val cornerRadius = 12.dp.toPx()
            val satValSize = size
            val bitmap = Bitmap.createBitmap(
                size.width.toInt(),
                size.height.toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            val satValPanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        }
    }

    /**************  Saturation Bar  ************/
    @Composable
    fun SatValPanel(
        hue: Float,
        setSatVal: (Float, Float) -> Unit
    ) {
        val interactionSource = remember {
            MutableInteractionSource()
        }
        val scope = rememberCoroutineScope()
        var sat: Float
        var value: Float
        val pressOffset = remember {
            mutableStateOf(Offset.Zero)
        }
        Canvas(
            modifier = Modifier
                .size(huePanelSize
                    .dp)
                .emitDragGesture(interactionSource)
                .clip(RoundedCornerShape(12.dp))
        ) {
            val cornerRadius = 12.dp.toPx()
            val satValSize = size
            val bitmap = Bitmap.createBitmap(
                size.width.toInt(),
                size.height.toInt(),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            val satValPanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
            val rgb = AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f))


            val satShader = LinearGradient(
                satValPanel.left, satValPanel.top, satValPanel.right, satValPanel.top,
                -0x1, rgb, Shader.TileMode.CLAMP
            )
            val valShader = LinearGradient(
                satValPanel.left, satValPanel.top, satValPanel.left, satValPanel.bottom,
                -0x1, -0x1000000, Shader.TileMode.CLAMP
            )
            canvas.drawRoundRect(
                satValPanel,
                cornerRadius,
                cornerRadius,
                Paint().apply {
                    shader = ComposeShader(
                        valShader,
                        satShader,
                        Mode.MULTIPLY
                    )
                }
            )
            drawBitmap(
                bitmap = bitmap,
                panel = satValPanel
            )

            fun pointToSatVal(pointX: Float, pointY: Float): Pair<Float, Float> {
                val width = satValPanel.width()
                val height = satValPanel.height()
                val x = when {
                    pointX < satValPanel.left -> 0f
                    pointX > satValPanel.right -> width
                    else -> pointX - satValPanel.left
                }
                val y = when {
                    pointY < satValPanel.top -> 0f
                    pointY > satValPanel.bottom -> height
                    else -> pointY - satValPanel.top
                }
                val satPoint = 1f / width * x
                val valuePoint = 1f - 1f / height * y
                return satPoint to valuePoint
            }
            scope.collectForPress(interactionSource) { pressPosition ->
                val pressPositionOffset = Offset(
                    pressPosition.x.coerceIn(0f..satValSize.width),
                    pressPosition.y.coerceIn(0f..satValSize.height)
                )

                pressOffset.value = pressPositionOffset
                val (satPoint, valuePoint) = pointToSatVal(
                    pressPositionOffset.x,
                    pressPositionOffset.y
                )
                sat = satPoint
                value = valuePoint
                setSatVal(sat, value)
            }
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx(),
                center = pressOffset.value,
                style = Stroke(
                    width = 2.dp.toPx()
                )
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = pressOffset.value,
            )

        }
    }

    /**************************  Color Picker  ****************************/
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ColorPicker() {
        val onDismissRequest = {
//            if (Patterns.color.matches(color)) {
//                onChoice(color)
//            }
        }
        Dialog(
            onDismissRequest = onDismissRequest,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp)
                    .padding(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(){
                    Spacer(modifier = Modifier.width(5.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val hsv = remember {
                            val hsv = floatArrayOf(0f, 0f, 0f)
                            AndroidColor.colorToHSV(Color.Blue.toArgb(), hsv)
                            mutableStateOf(
                                Triple(hsv[0], hsv[1], hsv[2])
                            )
                        }
                        val backgroundColor = remember(hsv.value) {
                            mutableStateOf(
                                Color.hsv(
                                    hsv.value.first,
                                    hsv.value.second,
                                    hsv.value.third
                                )
                            )
                        }

                        SatValPanel(hue = hsv.value.first) { sat, value ->
                            hsv.value = Triple(hsv.value.first, sat, value)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        HueBar { hue ->
                            hsv.value = Triple(hue, hsv.value.second, hsv.value.third)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier
                                .height(75.dp)
                                .width(300.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(75.dp)
                                    .background(backgroundColor.value)
                            )

                            Spacer(modifier = Modifier.width(15.dp))

                            ColorTextField(title = "HEX",
                                colorValueText = String.format("#%06X", 0xFFFFFF and backgroundColor.value.toArgb()),
                                columnHeight = 300.dp,
                                textEntryWidth = 75.dp,
                                textEntryHeight = 30.dp,
                                onValueChanged = {})

                            Spacer(modifier = Modifier.width(15.dp))

                            ColorTextField(title = "Alpha",
                                colorValueText = "100",
                                columnHeight = 300.dp,
                                textEntryWidth = 60.dp,
                                textEntryHeight = 30.dp,
                                onValueChanged = {})

//                        Text(
//                            text = String.format("#%06X", 0xFFFFFF and backgroundColor.value.toArgb()),
//                            style = TextStyle(
//                                fontSize = 12.sp,
//                                color = Color(0xFF1E1E1E),
//                                textAlign = TextAlign.Center,
//                                fontWeight = FontWeight.Bold
//                            )
//                        )
                        }

                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
        }

    }

    @Composable
    fun ColorTextField(
        title: String,
        colorValueText: String,
        columnHeight: Dp,
        textEntryWidth: Dp,
        textEntryHeight: Dp,
        onValueChanged: () -> Unit
    ) {
        var text by remember { mutableStateOf(colorValueText.toString()) }
        Column(
            modifier = Modifier
                .height(columnHeight),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            TextField(
                value = text,
                onValueChange = { },
                shape = RoundedCornerShape(5.dp),
                textStyle = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF1E1E1E),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .width(textEntryWidth)
                    .height(textEntryHeight)
                    .border(
                        1.dp,
                        color = Color(0xFF1E1E1E),
                        shape = RoundedCornerShape(5.dp)
                    )
            )

            Spacer(modifier = Modifier.height(7.dp))
            Text(
                text = title, style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF1E1E1E),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
    @Preview(showBackground = true, backgroundColor=  -0xff0100)
    @Composable
    fun ColorPickerPreview() {
       ColorPicker()
    }
}


