package com.airat.kidsdrawingapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airat.kidsdrawingapplication.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup_size_of_brush.view.*
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private var widghtWindow: Int = 0
    private var heightWindow: Int = 0
    private var actionBarHeight: Int = 0
    private var sizeOfBrush: Float = 1.0F
    private var mImageButtonCurrentPaint: ImageButton? = null


    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        val mainLayout = activityMainBinding.root
        setContentView(mainLayout)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        getMetricsForWindow()

        activityMainBinding.ibBrush.setOnClickListener {
            changingSizeForBrush()
        }

        activityMainBinding.ibColor.setOnClickListener {
            ChangingColorForBrush()
        }

        activityMainBinding.ibGallery.setOnClickListener {
            if (isReadStorageAllowed()) {

                val pickPhotoIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

                startActivityForResult(pickPhotoIntent, GALLERY)


            } else {
                requestStoragePermission()
            }
        }

        activityMainBinding.ibUndo.setOnClickListener {
            activityMainBinding.drawingView.onClickUndo()
        }

        activityMainBinding.ibSave.setOnClickListener {
             if(isWriteStorageAllowed()){

             } else {
                 requestStoragePermission()
             }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data.data)
                    } else {
                        Toast.makeText(
                            this, "Error in parsing the image or its corrupted ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun paintClicked(view: View) {

        if (view !== mImageButtonCurrentPaint) {
            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.background_unpressed_color_element)
            )
            val imageButton = view as ImageButton
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.background_pressed_color_element)
            )
            mImageButtonCurrentPaint = imageButton
            val colorTag = imageButton.tag.toString()
            activityMainBinding.drawingView.setColorForBrush(colorTag)
        }
    }

    private fun ChangingColorForBrush() {
        val view = layoutInflater.inflate(R.layout.popup_color_of_brush, null)

        val popupWindow = PopupWindow(
            view,
            (widghtWindow * .50).toInt(),
            (heightWindow * .40).toInt()
        )

        popupWindow.contentView = view
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAtLocation(
            activityMainBinding.scrollView,
            Gravity.TOP,
            Gravity.CENTER_VERTICAL,
            scroll_view.y.toInt() - actionBarHeight
        )
        popupWindow.setOnDismissListener {
            if (mImageButtonCurrentPaint != null){
                ib_brush.background = mImageButtonCurrentPaint!!.background
            }
        }

    }

    private fun changingSizeForBrush() {
        val view = layoutInflater.inflate(R.layout.popup_size_of_brush, null)
        view.seek_bar_dialog.progress = sizeOfBrush.toInt()

        val popupWindow = PopupWindow(
            view,
            (widghtWindow * .70).toInt(),
            (heightWindow * .08).toInt()
        )

        popupWindow.contentView = view
        popupWindow.isTouchable = true
        popupWindow.isFocusable = true
        popupWindow.showAtLocation(
            activityMainBinding.scrollView,
            Gravity.TOP,
            Gravity.CENTER_VERTICAL,
            scroll_view.y.toInt() - actionBarHeight
        )

        popupWindow.setOnDismissListener {
            sizeOfBrush = view.seek_bar_dialog.progress.toFloat()
            activityMainBinding.drawingView.setSizeForBrush(sizeOfBrush)
        }
    }

    private fun getMetricsForWindow() {
        val metrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= 30) {
            display?.apply {
                getRealMetrics(metrics)
                widghtWindow = metrics.widthPixels
                heightWindow = metrics.heightPixels
            }
        } else {
            windowManager.defaultDisplay.getMetrics(metrics)
            widghtWindow = metrics.widthPixels
            heightWindow = metrics.heightPixels
        }

        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight =
                TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            Toast.makeText(this, "Need permission to add a Background", Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), STORAGE_PERMISSION_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Permission granted. Now you can read the storage files",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Ooops! You just denied the permission", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun isWriteStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }
}
