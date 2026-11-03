/*
 * Spektrafilm for Android — restore-chrome hint. GPLv3.
 * Film modeling powered by spektrafilm.
 *
 * 单击照片会隐藏顶部与底部工具栏（干净的看片模式）。这个功能本身没问题，问题是隐藏之后
 * 画面上没有任何提示：唯一的回头路是"再点一次画面"或按返回键，两者都不可见。实测反馈
 * 就是这个——用户以为界面坏了，只能杀进程重启。
 *
 * 这个胶囊只在 chromeHidden 为真时出现，静置 2.5 秒后降到 40% 不透明度（不挡画面，
 * 但一直可点）。点它、点画面、按返回键，三条路都能回去。
 */
package com.spectrafilm.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * "恢复工具栏" pill shown while the chrome is hidden.
 *
 * Deliberately always present (dimmed after a moment rather than gone): the state it rescues
 * from is one where every other control has vanished, so a hint that also disappears would
 * reproduce the bug it exists to fix.
 */
@Composable
internal fun RestoreChromeHint(
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var dimmed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(2500)
        dimmed = true
    }
    val background by animateFloatAsState(
        targetValue = if (dimmed) 0.35f else 0.85f,
        label = "restoreChromeAlpha",
    )
    val label = stringResource(R.string.editor_restore_chrome)
    Surface(
        onClick = onRestore,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = background),
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        modifier = modifier.semantics {
            contentDescription = label
            role = Role.Button
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(SpectraIcons.Confirm, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}
