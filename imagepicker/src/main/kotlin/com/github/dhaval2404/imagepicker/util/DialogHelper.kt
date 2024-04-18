package com.github.dhaval2404.imagepicker.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.github.dhaval2404.imagepicker.R
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.github.dhaval2404.imagepicker.listener.DismissListener
import com.github.dhaval2404.imagepicker.listener.ResultListener

/**
 * Show Dialog
 *
 * @author Dhaval Patel
 * @version 1.0
 * @since 04 January 2018
 */
internal object DialogHelper {

    const val PERMISSION_CAMERA_REQUEST_CODE = 101
    const val PERMISSION_GALLERY_REQUEST_CODE = 102

    /**
     * Show Image Provider Picker Dialog. This streamlines the code to pick/capture images
     *
     */
    fun showChooseAppDialog(
        context: Context,
        listener: ResultListener<ImageProvider>,
        dismissListener: DismissListener?,
        activity: Activity // Add an Activity reference to handle permissions
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val customView = layoutInflater.inflate(R.layout.dialog_choose_app, null)

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_choose_image_provider)
            .setView(customView)
            .setOnCancelListener {
                listener.onResult(null)
            }
            .setNegativeButton(R.string.action_cancel) { _, _ ->
                listener.onResult(null)
            }
            .setOnDismissListener {
                dismissListener?.onDismiss()
            }
            .show()

        // Handle Camera option click
        customView.findViewById<View>(R.id.lytCameraPick).setOnClickListener {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.CAMERA),
                    PERMISSION_CAMERA_REQUEST_CODE
                )
            } else {
                listener.onResult(ImageProvider.CAMERA)
                dialog.dismiss()
            }
        }

        // Handle Gallery option click
        customView.findViewById<View>(R.id.lytGalleryPick).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For Android 13 and above, modify this as necessary
                Toast.makeText(context, "Limited gallery access in Android 13+", Toast.LENGTH_LONG).show()
            } else {
                // Request permission before returning the gallery option
                if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_GALLERY_REQUEST_CODE
                    )
                } else {
                    listener.onResult(ImageProvider.GALLERY)
                    dialog.dismiss()
                }
            }
        }
    }
}