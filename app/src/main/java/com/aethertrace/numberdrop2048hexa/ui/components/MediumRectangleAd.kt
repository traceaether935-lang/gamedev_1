package com.aethertrace.numberdrop2048hexa.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aethertrace.numberdrop2048hexa.ads.AdMobManager
import com.aethertrace.numberdrop2048hexa.ui.theme._2049Theme
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun MediumRectangleAd(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.MEDIUM_RECTANGLE_AD_UNIT_ID,
    isAdFree: Boolean = AdMobManager.isAdFree
) {
    if (isAdFree) return

    val isPreview = LocalInspectionMode.current

    Surface(
        modifier = modifier.size(width = 300.dp, height = 250.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isPreview) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ADVERTISEMENT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "300 × 250 Medium Rectangle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            } else {
                Text(
                    text = "ADVERTISEMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                AndroidView(
                    factory = { context ->
                        AdView(context).apply {
                            setAdSize(AdSize.MEDIUM_RECTANGLE)
                            this.adUnitId = adUnitId
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    modifier = Modifier.size(width = 300.dp, height = 250.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MediumRectangleAdPreview() {
    _2049Theme {
        MediumRectangleAd()
    }
}
