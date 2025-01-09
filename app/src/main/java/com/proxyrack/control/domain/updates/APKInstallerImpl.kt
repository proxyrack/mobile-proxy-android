package com.proxyrack.control.domain.updates

import android.content.Context
import java.io.File
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log
import androidx.core.content.FileProvider;

class APKInstallerImpl(private val context: Context): APKInstaller {
    override fun install(file: File) {
        if (!file.exists()) {
            Log.e(javaClass.simpleName, "file does not exist")
            return
        }

        val intent = Intent(Intent.ACTION_VIEW);
        val apkUri: Uri

        // Using FileProvider for API 24+ (Android 7.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            apkUri = Uri.fromFile(file);
        }

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }
}
