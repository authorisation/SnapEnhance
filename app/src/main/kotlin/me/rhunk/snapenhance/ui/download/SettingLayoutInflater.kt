package me.rhunk.snapenhance.ui.download

import android.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import me.rhunk.snapenhance.R
import me.rhunk.snapenhance.bridge.common.impl.file.BridgeFileType
import java.io.File

class SettingAdapter(
    private val activity: DownloadManagerActivity,
    private val layoutId: Int,
    private val actions: Array<Pair<String, () -> Unit>>
) : ArrayAdapter<Pair<String, () -> Unit>>(activity, layoutId, actions) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: activity.layoutInflater.inflate(layoutId, parent, false)
        val action = actions[position]
        view.isClickable = true

        view.findViewById<TextView>(R.id.feature_text).text = action.first
        view.setOnClickListener {
            action.second()
        }

        return view
    }
}

class SettingLayoutInflater(
    private val activity: DownloadManagerActivity
) {
    private fun confirmAction(title: String, message: String, action: () -> Unit) {
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes") { _, _ ->
                    action()
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
        }
    }


    fun inflate(parent: ViewGroup) {
        val settingsView = activity.layoutInflater.inflate(R.layout.settings_page, parent, false)

        settingsView.findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
            parent.removeView(settingsView)
        }

        settingsView.findViewById<ListView>(R.id.setting_page_list).apply {
            adapter = SettingAdapter(activity, R.layout.setting_item, mutableListOf<Pair<String, () -> Unit>>().apply {
                add("Clear Cache" to {
                    context.cacheDir.listFiles()?.forEach {
                        it.deleteRecursively()
                    }
                    Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                })

                BridgeFileType.values().forEach { fileType ->
                    val actionName = "Clear ${fileType.displayName} File"
                    add(actionName to {
                        confirmAction(actionName, "Are you sure you want to clear ${fileType.displayName} file?") {
                            fileType.resolve(context).deleteRecursively()
                            Toast.makeText(context, "${fileType.displayName} file cleared", Toast.LENGTH_SHORT).show()
                        }
                    })
                }

                add("Reset All" to {
                    confirmAction("Reset All", "Are you sure you want to reset all?") {
                        arrayOf(context.cacheDir, context.filesDir, File(context.dataDir, "databases"), File(context.dataDir, "shared_prefs")).forEach {
                            it.listFiles()?.forEach { file ->
                                file.deleteRecursively()
                            }
                        }
                        Toast.makeText(context, "Success!", Toast.LENGTH_SHORT).show()
                    }
                })
            }.toTypedArray())
        }

        activity.registerBackCallback {
            parent.removeView(settingsView)
        }

        parent.addView(settingsView)
    }
}