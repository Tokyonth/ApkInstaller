package com.tokyonth.installer.activity

import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.FreezeAdapter
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.database.SQLiteUtil
import com.tokyonth.installer.databinding.ActivityFreezeBinding
import com.tokyonth.installer.databinding.LayoutFreezeDialogBinding
import com.tokyonth.installer.utils.AppPackageUtils
import com.tokyonth.installer.utils.ShellUtils
import com.tokyonth.installer.utils.bind
import com.tokyonth.installer.utils.visibleOrGone
import com.tokyonth.installer.view.CustomizeDialog
import java.util.ArrayList

class FreezeActivity : BaseActivity() {

    private lateinit var listData: ArrayList<String>

    private lateinit var freezeAdapter: FreezeAdapter

    private lateinit var customizeDialog: AlertDialog

    private lateinit var vb: ActivityFreezeBinding

    override fun initView(): ViewBinding {
        vb = bind()
        return vb
    }

    override fun initData() {
        listData = SQLiteUtil.getAllData(this)
        freezeAdapter = FreezeAdapter(this, listData)

        vb.rvFreezeList.apply {
            layoutManager = LinearLayoutManager(this@FreezeActivity)
            adapter = freezeAdapter
        }

        freezeAdapter.setItemListener { position, pkgName ->
            val dialogViewVb = LayoutFreezeDialogBinding.bind(View.inflate(this@FreezeActivity, R.layout.layout_freeze_dialog, null))
            customizeDialog = CustomizeDialog.getInstance(this@FreezeActivity)
                    .setView(dialogViewVb.root)
                    .setNegativeButton(R.string.dialog_btn_cancel, null)
                    .create()
            customizeDialog.show()

            dialogViewVb.tvUnfreeze.setOnClickListener {
                dialogAppAction(position, pkgName, false)
            }
            dialogViewVb.tvUninstall.setOnClickListener {
                dialogAppAction(position, pkgName, true)
            }
        }

        setSupportActionBar(vb.toolbar)
        setTitle(R.string.uninstall_dialog_disable)
        supportActionBar.apply {
            actionBar?.setHomeButtonEnabled(true)
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        vb.freezeNullView.visibleOrGone(freezeAdapter.itemCount == 0)
        vb.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorText))
        vb.toolbar.navigationIcon?.setTint(ContextCompat.getColor(this@FreezeActivity, R.color.colorText))
    }

    private fun dialogAppAction(position: Int, pkgName: String, isUninstall: Boolean) {
        val appName = AppPackageUtils.getAppNameByPackageName(this@FreezeActivity, pkgName)
        isUninstall.let {
            val command = if (it) {
                Constants.UNINSTALL_COMMAND + pkgName
            } else {
                Constants.UNFREEZE_COMMAND + pkgName
            }
            ShellUtils.execWithRoot(command).let { it1 ->
                val str = if (it1 == 0) {
                    SQLiteUtil.delData(this@FreezeActivity, pkgName)
                    listData.removeAt(position)
                    if (it) {
                        getString(R.string.text_uninstall_complete, appName)
                    } else {
                        getString(R.string.unfreeze_app_complete)
                    }
                } else {
                    if (it) {
                        getString(R.string.text_uninstall_failure)
                    } else {
                        getString(R.string.unfreeze_app_failure)
                    }
                }
                showToast(str)
            }
        }
        freezeAdapter.notifyDataSetChanged()
        customizeDialog.dismiss()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

}
