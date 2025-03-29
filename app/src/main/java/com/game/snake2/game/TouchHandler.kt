package com.game.snake2.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun TouchHandler(
    modifier: Modifier = Modifier,
    onDirectionChange: (Direction) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val screenWidth = with(density) { 400.dp.toPx() }
    val screenHeight = with(density) { 800.dp.toPx() }
    
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                
                // Calculate the movement based on screen dimensions
                val horizontalMovement = (x / 10) * screenWidth
                val verticalMovement = (y / 10) * screenHeight
                
                if (abs(horizontalMovement) > abs(verticalMovement)) {
                    if (horizontalMovement > 0) onDirectionChange(Direction.RIGHT)
                    else onDirectionChange(Direction.LEFT)
                } else {
                    if (verticalMovement > 0) onDirectionChange(Direction.DOWN)
                    else onDirectionChange(Direction.UP)
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
        }
    }

    modifier
}  