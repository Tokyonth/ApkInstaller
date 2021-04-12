package com.tokyonth.installer.activity

import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.tokyonth.installer.Constants
import com.tokyonth.installer.R
import com.tokyonth.installer.adapter.FreezeAdapter
import com.tokyonth.installer.base.BaseActivity
import com.tokyonth.installer.database.SQLiteUtil
import com.tokyonth.installer.databinding.ActivityFreezeBinding
import com.tokyonth.installer.utils.AppPackageUtils
import com.tokyonth.installer.utils.CommonUtil.bind
import com.tokyonth.installer.utils.ShellUtils
import com.tokyonth.installer.view.CustomizeDialog
import java.util.ArrayList

class FreezeActivity : BaseActivity() {

    override fun hasView(): Boolean {
        return true
    }

    override fun initView(): ViewBinding {
        return bind<ActivityFreezeBinding>()
    }

    override fun initData() {
        val listData = SQLiteUtil.getAllData(this)
        val recyclerView = findViewById<RecyclerView>(R.id.rv_freeze_list)
        val adapter = FreezeAdapter(this, listData as ArrayList<String>?)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.setListener(object : FreezeAdapter.OnItemClickListener {
            override fun onClick(position: Int, pkgName: String) {
                val dialogView: View = View.inflate(this@FreezeActivity, R.layout.layout_freeze_dialog, null)
                val customizeDialog: androidx.appcompat.app.AlertDialog = CustomizeDialog.getInstance(this@FreezeActivity)
                        .setView(dialogView)
                        .setNegativeButton(R.string.dialog_btn_cancel, null)
                        .create()
                customizeDialog.show()

                val tvUnfreeze: TextView = dialogView.findViewById(R.id.tv_unfreeze)
                val tvUninstall: TextView = dialogView.findViewById(R.id.tv_uninstall)
                tvUnfreeze.setOnClickListener {
                    val result = ShellUtils.execWithRoot(Constants.UNFREEZE_COMMAND + pkgName)
                    val str: String
                    if (result == 0) {
                        SQLiteUtil.delData(this@FreezeActivity, pkgName)
                        str = getString(R.string.unfreeze_app_complete)
                        listData.removeAt(position)
                    } else {
                        str = getString(R.string.unfreeze_app_failure)
                    }
                    adapter.notifyDataSetChanged()
                    customizeDialog.dismiss()
                    showToast(str)
                }
                tvUninstall.setOnClickListener {
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
                    adapter.notifyDataSetChanged()
                    customizeDialog.dismiss()
                    showToast(str)
                }
            }
        })

        if (adapter.itemCount != 0) {
            findViewById<LinearLayout>(R.id.freeze_null_view).visibility = View.GONE
        }
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        setTitle(R.string.uninstall_dialog_disable)
        supportActionBar.apply {
            actionBar?.setHomeButtonEnabled(true)
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorText))
        toolbar.navigationIcon?.apply {
            setTint(ContextCompat.getColor(this@FreezeActivity, R.color.colorText))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

}
