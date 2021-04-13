package com.tokyonth.installer.activity

import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
import com.tokyonth.installer.utils.AppPackageUtils
import com.tokyonth.installer.utils.CommonUtil.bind
import com.tokyonth.installer.utils.CommonUtil.visibleOrGone
import com.tokyonth.installer.utils.ShellUtils
import com.tokyonth.installer.view.CustomizeDialog
import java.util.ArrayList

class FreezeActivity : BaseActivity() {

    private lateinit var listData: ArrayList<String>

    private lateinit var freezeAdapter: FreezeAdapter

    private lateinit var customizeDialog: AlertDialog

    private lateinit var vb: ActivityFreezeBinding

    override fun initView(): ViewBinding? {
        vb = bind()
        return vb
    }

    override fun initData() {
        listData = SQLiteUtil.getAllData(this)
        freezeAdapter = FreezeAdapter(this, listData as ArrayList<String>?)

        vb.rvFreezeList.apply {
            layoutManager = LinearLayoutManager(this@FreezeActivity)
            adapter = freezeAdapter
        }

        freezeAdapter.setItemListener(object : FreezeAdapter.OnItemClickListener {
            override fun onClick(position: Int, pkgName: String) {
                val dialogView: View = View.inflate(this@FreezeActivity, R.layout.layout_freeze_dialog, null)
                customizeDialog = CustomizeDialog.getInstance(this@FreezeActivity)
                        .setView(dialogView)
                        .setNegativeButton(R.string.dialog_btn_cancel, null)
                        .create()
                customizeDialog.show()

                val tvUnfreeze: TextView = dialogView.findViewById(R.id.tv_unfreeze)
                val tvUninstall: TextView = dialogView.findViewById(R.id.tv_uninstall)
                tvUnfreeze.setOnClickListener {
                    unfreezeApp(position, pkgName)
                }
                tvUninstall.setOnClickListener {
                    uninstallApp(position, pkgName)
                }
            }
        })

        vb.freezeNullView.visibleOrGone(freezeAdapter.itemCount == 0)

        setSupportActionBar(vb.toolbar)
        setTitle(R.string.uninstall_dialog_disable)
        supportActionBar.apply {
            actionBar?.setHomeButtonEnabled(true)
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        vb.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorText))
        vb.toolbar.navigationIcon?.apply {
            setTint(ContextCompat.getColor(this@FreezeActivity, R.color.colorText))
        }
    }

    private fun uninstallApp(position: Int, pkgName: String) {
        val result = ShellUtils.execWithRoot(Constants.UNINSTALL_COMMAND + pkgName)
        val appName = AppPackageUtils.getAppNameByPackageName(this@FreezeActivity, pkgName)
        val str: String
        if (result == 0) {
            SQLiteUtil.delData(this@FreezeActivity, pkgName)
            str = getString(R.string.text_uninstall_complete, appName)
            listData.removeAt(position)
        } else {
            str = getString(R.string.text_uninstall_failure)
        }
        freezeAdapter.notifyDataSetChanged()
        customizeDialog.dismiss()
        showToast(str)
    }

    private fun unfreezeApp(position: Int, pkgName: String) {
        val result = ShellUtils.execWithRoot(Constants.UNFREEZE_COMMAND + pkgName)
        val str: String
        if (result == 0) {
            SQLiteUtil.delData(this@FreezeActivity, pkgName)
            str = getString(R.string.unfreeze_app_complete)
            listData.removeAt(position)
        } else {
            str = getString(R.string.unfreeze_app_failure)
        }
        freezeAdapter.notifyDataSetChanged()
        customizeDialog.dismiss()
        showToast(str)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

}
