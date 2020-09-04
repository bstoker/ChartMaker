/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.common.export_dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.KITKAT)
class CreateDocument : ActivityResultContract<CreateDocument.Args, Uri?>() {

    override fun createIntent(context: Context, args: Args): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT)
            .setType(args.type)
            .addCategory(args.category)
            .putExtra(Intent.EXTRA_TITLE, args.title)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data!!
    }

    data class Args(val title: String, val type: String? = "*/*", val category: String? = null)
}