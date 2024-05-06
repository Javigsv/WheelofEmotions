package com.example.wheelofemotions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.wheelofemotions.ui.theme.WheelOfEmotionsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WheelOfEmotionsTheme {
                // A surface container using the 'background' color from the theme
                Row(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Surface {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Wheel(
                                modifier = Modifier.background(Color.Red)
                            ) {
                                items(listOf("Peter", "Karl", "John", "Martha", "Lauren")) { item ->
                                    Text(text = item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WheelOfEmotionsTheme {

    }
}
