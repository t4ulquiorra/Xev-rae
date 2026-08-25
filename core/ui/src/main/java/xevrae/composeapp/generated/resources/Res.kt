package xevrae.composeapp.generated.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.xevrae.ui.AppGlobalContext
import com.xevrae.ui.R

object Res {
    val string = R.string
    val drawable = R.drawable
    val font = R.font
}

typealias StringResource = Int
typealias DrawableResource = Int
typealias FontResource = Int

@Composable
fun stringResource(id: Int): String = androidx.compose.ui.res.stringResource(id)

@Composable
fun stringResource(id: Int, vararg formatArgs: Any): String = androidx.compose.ui.res.stringResource(id, *formatArgs)

@Composable
fun painterResource(id: Int): Painter = androidx.compose.ui.res.painterResource(id)

@Composable
fun Font(
    resource: Int,
    weight: FontWeight = FontWeight.Normal,
    style: FontStyle = FontStyle.Normal,
) = androidx.compose.ui.text.font.Font(resource, weight, style)

fun getString(id: Int): String {
    val context = AppGlobalContext.get() ?: return ""
    return context.getString(id)
}

fun getString(id: Int, vararg formatArgs: Any): String {
    val context = AppGlobalContext.get() ?: return ""
    return context.getString(id, *formatArgs)
}
