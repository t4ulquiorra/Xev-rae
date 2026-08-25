package multiplatform.network.cmptoast

import android.widget.Toast
import com.xevrae.ui.AppGlobalContext

enum class ToastDuration {
    Short,
    Long,
}

enum class ToastGravity {
    Top,
    Center,
    Bottom,
}

fun showToast(
    message: String,
    duration: ToastDuration = ToastDuration.Short,
    gravity: ToastGravity = ToastGravity.Bottom,
) {
    val context = AppGlobalContext.get() ?: return
    val length = if (duration == ToastDuration.Short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG
    Toast.makeText(context, message, length).show()
}
