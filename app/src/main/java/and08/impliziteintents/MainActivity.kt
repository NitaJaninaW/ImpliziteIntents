package and08.impliziteintents

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.AlarmClock
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class MainActivity : AppCompatActivity() {

    lateinit var photoURI: Uri

    companion object {
        val REQUEST_IMAGE_CAPTURE = 1
        val THUMBNAIL = "thumbnail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(
            arrayOf<String>(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.SET_ALARM
            ), 0
        )
        val bitmap = savedInstanceState?.getParcelable(THUMBNAIL) as Bitmap?
        if (bitmap != null) {
            Log.d(javaClass.simpleName, "onCreate: restore bitmap")
            findViewById<ImageView>(R.id.imageview_thumbnail).setImageBitmap(bitmap)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val locationIndex = permissions.indexOf(Manifest.permission.CALL_PHONE)
        if (grantResults[locationIndex] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                this,
                R.string.brechetigung_nicht_erteilt,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun onButtonCallClick(view: View) {
        val callIntent = Intent().setAction(Intent.ACTION_CALL).setData(
            Uri.parse("tel: 0123456789")
        )
        if (callIntent.resolveActivity(packageManager) != null)
            startActivity(callIntent)
        else
            Toast.makeText(
                this,
                "keine geeignete Anwendung installiert", Toast.LENGTH_LONG
            ).show()
    }

    fun onButtonCameraClick(view: View) {
        dispatchTakePictureIntent()
    }

    fun onButtonSendClick(view: View) {
        with(Intent(Intent.ACTION_SEND)) {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("decostacarrol2@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Test-Mail aus AND08, Kapitel 2")
            putExtra(Intent.EXTRA_TEXT, "Beispiel-Text der E-Mail")
            putExtra(Intent.EXTRA_STREAM, photoURI)
            startActivity(Intent.createChooser(this, "Mail versenden"))
        }
    }

    fun onButtonAlarmClick(view: View) {
        // Erstellen eines Intent, der die Alarm-App startet
        val alarmIntent = with(Intent(AlarmClock.ACTION_SET_ALARM)) {
            putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm in 5 Minuten")
            putExtra(AlarmClock.EXTRA_HOUR, Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
            putExtra(AlarmClock.EXTRA_MINUTES, Calendar.getInstance().get(Calendar.MINUTE) + 5)
        }
        // Überprüfung , ob es eine App gibt, die den Intent verarbeiten kann
        if (alarmIntent.resolveActivity(packageManager) != null) {
            startActivity(alarmIntent)
        } else {
            Toast.makeText(this, "Keine geeignete Anwendung installiert",
                Toast.LENGTH_LONG).show()
        }
    }


    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)
         if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
             //ggf. kein bitmap vorhanden wegen EXTRA_OUTPUT
              val imageBitmap = data?.extras?.get("data") as Bitmap
         }
     }*/

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        this, "and08.impliziteintents.fileprovider", it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
// Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        Log.i(javaClass.simpleName, storageDir.toString())
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val drawable = (findViewById(R.id.imageview_thumbnail) as ImageView).getDrawable()
        if (drawable != null) {
            Log.d(javaClass.simpleName, "onSaveInstanceState: save thumbnail")
            outState.putParcelable(THUMBNAIL, (drawable as BitmapDrawable).bitmap)
        }
    }
}