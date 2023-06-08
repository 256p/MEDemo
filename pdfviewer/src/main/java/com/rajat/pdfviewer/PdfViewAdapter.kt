package com.rajat.pdfviewer

import android.graphics.Rect
import android.view.ViewGroup
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import com.rajat.pdfviewer.util.hide
import com.rajat.pdfviewer.util.show
import android.view.animation.AlphaAnimation
import androidx.core.view.updateLayoutParams
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Rajat on 11,July,2020
 */

internal class PdfViewAdapter(
    private val renderer: PdfRendererCore,
    private val pageSpacing: Rect,
    private val enableLoadingForPages: Boolean
) :
    RecyclerView.Adapter<PdfViewAdapter.PdfPageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_pdf_page, parent, false)

        return PdfPageViewHolder(view)
    }

    override fun getItemCount(): Int {
        return renderer.getPageCount()
    }

    override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class PdfPageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val pageView: ImageView
        private val containerView: FrameLayout
        private val loadingProgressView: ProgressBar

        init {
            pageView = view.findViewById(R.id.pageView)
            containerView = view.findViewById(R.id.container_view)
            loadingProgressView = view.findViewById(R.id.pdf_view_page_loading_progress)
        }

        fun bind(position: Int) {
            handleLoadingForPage(position)

            pageView.setImageBitmap(null)
            renderer.renderPage(position) { bitmap: Bitmap?, pageNo: Int ->
                if (pageNo != position)
                    return@renderPage
                bitmap?.let {
                    containerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        height =
                            (containerView.width.toFloat() / ((bitmap.width.toFloat() / bitmap.height.toFloat()))).toInt()
                        this.topMargin = pageSpacing.top
                        this.leftMargin = pageSpacing.left
                        this.rightMargin = pageSpacing.right
                        this.bottomMargin = pageSpacing.bottom
                    }
                    pageView.setImageBitmap(bitmap)
                    pageView.animation = AlphaAnimation(0F, 1F).apply {
                        interpolator = LinearInterpolator()
                        duration = 300
                    }

                    loadingProgressView.hide()
                }
            }
        }

        private fun handleLoadingForPage(position: Int) {
            if (!enableLoadingForPages) {
                loadingProgressView.hide()
                return
            }

            if (renderer.pageExistInCache(position)) {
                loadingProgressView.hide()
            } else {
                loadingProgressView.show()
            }
        }
    }
}