package com.anto426.uniapp.ui.scanner
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.anto426.uniapp.codes.createCodeReader
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
@Composable
actual fun UniQrScannerScreen(
    title: String, subtitle: String?, onScanned: (String) -> Unit, onClose: () -> Unit,
    onManualInput: (() -> Unit)?, modifier: Modifier,
) {
    val scope = rememberCoroutineScope()
    val reader = remember { createCodeReader() }
    var message by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    Column(modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        subtitle?.let { Text(it) }
        Text("Importa un’immagine con un codice QR o a barre.")
        LiquidButton(enabled = !busy, onClick = {
            scope.launch {
                busy = true
                try {
                    val file = JFileChooser().apply {
                        fileFilter = FileNameExtensionFilter("PNG / JPEG", "png", "jpg", "jpeg")
                    }.let { if (it.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) it.selectedFile else null }
                    if (file != null) {
                        val bytes = withContext(Dispatchers.IO) {
                            require(file.length() <= 32L * 1024 * 1024) { "Immagine troppo grande" }
                            file.readBytes()
                        }
                        val code = reader.read(bytes).firstOrNull()
                        if (code != null) onScanned(code.value) else message = "Nessun codice trovato nell’immagine."
                    }
                } catch (error: kotlinx.coroutines.CancellationException) { throw error }
                  catch (_: Exception) { message = "Impossibile leggere l’immagine." }
                finally { busy = false }
            }
        }) { Text("Scegli immagine") }
        onManualInput?.let { LiquidButton(onClick = it) { Text("Inserisci codice manualmente") } }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        LiquidButton(onClick = onClose) { Text("Chiudi") }
    }
}
