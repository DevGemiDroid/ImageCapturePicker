package com.github.dhaval2404.imagepicker

import android.app.Activity
import com.github.dhaval2404.imagepicker.constant.ImageProvider
import com.github.dhaval2404.imagepicker.provider.CameraProvider
import com.github.dhaval2404.imagepicker.provider.CompressionProvider
import com.github.dhaval2404.imagepicker.provider.CropProvider
import com.github.dhaval2404.imagepicker.provider.GalleryProvider
import com.github.dhaval2404.imagepicker.util.FileUriUtils

/**
 * Pick Image
 *
 * @author Dhaval Patel
 * @version 1.0
 * @since 04 January 2019
 */
import android.Manifest
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
/*import com.example.imagepicker.ImagePicker
import com.example.imagepicker.utils.FileUriUtils*/

class ImagePickerActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "image_picker"
        private const val REQUEST_CODE_STORAGE_PERMISSION = 100
    }

    private var mGalleryProvider: GalleryProvider? = null
    private var mCameraProvider: CameraProvider? = null
    private lateinit var mCropProvider: CropProvider
    private lateinit var mCompressionProvider: CompressionProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadBundle(savedInstanceState)
    }

   /* private fun loadBundle(savedInstanceState: Bundle?) {
        // Create Crop Provider
        mCropProvider = CropProvider(this)
        mCropProvider.onRestoreInstanceState(savedInstanceState)

        // Create Compression Provider
        mCompressionProvider = CompressionProvider(this)

        // Check and request permissions based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Marshmallow and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API level 33) and above
                // Request permission only for accessing photos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), REQUEST_CODE_STORAGE_PERMISSION)
                } else {
                    initImageProvider(savedInstanceState)
                }
            } else {
                // Request permission for accessing photos and videos
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_STORAGE_PERMISSION)
                } else {
                    initImageProvider(savedInstanceState)
                }
            }
        } else {
            // For devices below Marshmallow, permissions are granted at installation
            initImageProvider(savedInstanceState)
        }
    }*/

    private fun loadBundle(savedInstanceState: Bundle?) {

        mCropProvider = CropProvider(this)
        mCropProvider.onRestoreInstanceState(savedInstanceState)


        mCompressionProvider = CompressionProvider(this)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >=33) {
                // Android 13 and above photos only
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        REQUEST_CODE_STORAGE_PERMISSION
                    )
                } else {
                    initImageProvider(savedInstanceState)
                }
            } else {
                // Android 12 and below
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_STORAGE_PERMISSION
                    )
                } else {
                    initImageProvider(savedInstanceState)
                }
            }
        } else {
            initImageProvider(savedInstanceState)
        }
    }





    private fun initImageProvider(savedInstanceState: Bundle?) {
        val provider: ImageProvider? =
            intent?.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PROVIDER) as ImageProvider?

        when (provider) {
            ImageProvider.GALLERY -> {
                mGalleryProvider = GalleryProvider(this)
                savedInstanceState ?: mGalleryProvider?.startIntent()
            }
            ImageProvider.CAMERA -> {
                mCameraProvider = CameraProvider(this)
                savedInstanceState ?: mCameraProvider?.startIntent()
            }
            else -> {
                Log.e(TAG, "Image provider cannot be null")
                setError(getString(R.string.error_task_cancelled))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initImageProvider(null)
            } else {
                setError("Permission denied")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mCameraProvider?.onSaveInstanceState(outState)
        mCropProvider.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCameraProvider?.onActivityResult(requestCode, resultCode, data)
        mGalleryProvider?.onActivityResult(requestCode, resultCode, data)
        mCropProvider.onActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        setResultCancel()
    }

    fun setImage(uri: Uri) {
        when {
            mCropProvider.isCropEnabled() -> mCropProvider.startIntent(uri)
            mCompressionProvider.isCompressionRequired(uri) -> mCompressionProvider.compress(uri)
            else -> setResult(uri)
        }
    }

    fun setCropImage(uri: Uri) {
        mCameraProvider?.delete()

        if (mCompressionProvider.isCompressionRequired(uri)) {
            mCompressionProvider.compress(uri)
        } else {
            setResult(uri)
        }
    }

    fun setCompressedImage(uri: Uri) {
        mCameraProvider?.delete()
        mCropProvider.delete()
        setResult(uri)
    }

    private fun setResult(uri: Uri) {
        val intent = Intent()
        intent.data = uri
        intent.putExtra(ImagePicker.EXTRA_FILE_PATH, FileUriUtils.getRealPath(this, uri))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun setResultCancel() {
        setResult(Activity.RESULT_CANCELED, getCancelledIntent(this))
        finish()
    }

    fun setError(message: String) {
        val intent = Intent()
        intent.putExtra(ImagePicker.EXTRA_ERROR, message)
        setResult(ImagePicker.RESULT_ERROR, intent)
        finish()
    }

    internal fun getCancelledIntent(context: Context): Intent {
        val intent = Intent()
        val message = context.getString(R.string.error_task_cancelled)
        intent.putExtra(ImagePicker.EXTRA_ERROR, message)
        return intent
    }
}