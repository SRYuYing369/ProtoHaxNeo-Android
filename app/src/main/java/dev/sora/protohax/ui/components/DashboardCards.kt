package dev.sora.protohax.ui.components

import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.sora.protohax.R
import dev.sora.protohax.relay.AccountManager
import dev.sora.protohax.ui.activities.AppPickerActivity
import dev.sora.protohax.ui.navigation.PHaxTopLevelDestination
import dev.sora.protohax.ui.navigation.TOP_LEVEL_DESTINATIONS
import dev.sora.protohax.util.ContextUtils.getApplicationName
import dev.sora.protohax.util.ContextUtils.getPackageInfo
import dev.sora.relay.game.GameSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardLoginAlert(
    navigateToTopLevelDestination: (PHaxTopLevelDestination) -> Unit
) {
	if (AccountManager.currentAccount == null) {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(18.dp, 10.dp),
			onClick = {
				navigateToTopLevelDestination(TOP_LEVEL_DESTINATIONS.find { it.iconTextId == R.string.tab_accounts }
					?: return@Card)
			}
		) {
			Row(
				modifier = Modifier.padding(15.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
				Spacer(Modifier.size(12.dp))
				Text(
					stringResource(R.string.dashboard_no_account_selected),
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}
	} else {
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.padding(18.dp, 10.dp),
			onClick = {
				navigateToTopLevelDestination(TOP_LEVEL_DESTINATIONS.find { it.iconTextId == R.string.tab_accounts }
					?: return@Card)
			}
		) {
			Row(
				modifier = Modifier.padding(15.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(Icons.Default.SupervisedUserCircle, null, tint = MaterialTheme.colorScheme.error)
				Spacer(Modifier.size(12.dp))
				Text(
					stringResource(R.string.dashboard_account_selected, AccountManager.currentAccount!!.remark),
					color = MaterialTheme.colorScheme.onBackground
				)
			}
		}
	}
}
