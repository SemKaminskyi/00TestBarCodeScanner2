package com.example.a00testbarcodescanner

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.concurrent.Executors

class BottomDialog : BottomSheetDialogFragment(){
    private lateinit var tvTitle:TextView
    private lateinit var tvLink:TextView
    private lateinit var tvVisin:TextView
    private lateinit var tvClose :TextView

    private var fetchUrlStr = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_dialog, container,false)

        tvTitle = view.findViewById(R.id.title)
        tvLink = view.findViewById(R.id.link)
        tvVisin = view.findViewById(R.id.visit_link)
        tvClose = view.findViewById(R.id.close)

        tvTitle.text = fetchUrlStr

        tvVisin.setOnClickListener {
            val intent = Intent("android.intent.action.view")
            intent.setData(Uri.parse(fetchUrlStr))
            startActivity(intent)
        }
        tvClose.setOnClickListener {
            dismiss()
        }


        return view
    }

    fun fetchUrl(str: String){
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executorService.execute(Runnable {
            fetchUrlStr = str
        })
    }
}