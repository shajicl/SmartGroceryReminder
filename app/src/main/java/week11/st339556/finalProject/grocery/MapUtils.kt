package week11.st339556.finalProject.grocery


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object MapUtils {

    fun openStoreInMaps(context: Context, storeName: String, latitude: Double, longitude: Double) {
        try {
            // Create URI for Google Maps
            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(storeName)})")

            // Create an Intent to open in Google Maps
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")

            // Check if Google Maps is installed
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback to any map app
                val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, geoUri)

                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(fallbackIntent)
                } else {
                    // No map app available, show toast with coordinates
                    showCoordinatesToast(context, storeName, latitude, longitude)
                }
            }
        } catch (e: Exception) {
            // Handle any errors
            Toast.makeText(context, "Could not open maps: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun navigateToStore(context: Context, storeName: String, latitude: Double, longitude: Double) {
        try {
            // Create URI for navigation
            val navUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")

            val navIntent = Intent(Intent.ACTION_VIEW, navUri)
            navIntent.setPackage("com.google.android.apps.maps")

            if (navIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(navIntent)
            } else {
                // Fallback to standard navigation
                val fallbackUri = Uri.parse("http://maps.google.com/maps?daddr=$latitude,$longitude")
                val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri)

                if (fallbackIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(fallbackIntent)
                } else {
                    Toast.makeText(context, "No navigation app available", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not start navigation: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCoordinatesToast(context: Context, storeName: String, latitude: Double, longitude: Double) {
        // Copy coordinates to clipboard
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Coordinates", "$latitude, $longitude")
        clipboard.setPrimaryClip(clip)

        // Show toast message
        Toast.makeText(
            context,
            "Coordinates copied to clipboard: $latitude, $longitude",
            Toast.LENGTH_LONG
        ).show()
    }
}