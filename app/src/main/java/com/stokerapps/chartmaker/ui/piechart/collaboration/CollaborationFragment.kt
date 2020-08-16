/*
 * Copyright © 2020 Bram Stoker. All rights reserved.
 */

package com.stokerapps.chartmaker.ui.piechart.collaboration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.stokerapps.chartmaker.R

class CollaborationFragment: Fragment(R.layout.fragment_collaboration) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // roles:
        // owner / admin / collaborator / spectator

        // shared with + privileges

        // updates:
        // created on xx-xx-xx by xx
        // modified on xx-xx-xx by xx
    }

}