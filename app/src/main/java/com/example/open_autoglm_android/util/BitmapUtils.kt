package com.example.open_autoglm_android.util

import android.graphics.Bitmap
import android.util.Log

object BitmapUtils {
    /**
     * 检查位图是否全黑或几乎全黑
     * @param bitmap 要检查的位图
     * @param threshold 阈值，如果黑色像素比例超过此值，认为是全黑（默认 0.98，即 98%）
     * @return true 如果位图是全黑的
     */
    
    /**
     * 计算两个位图的相似度
     * @param bitmap1 第一个位图
     * @param bitmap2 第二个位图
     * @param threshold 相似度阈值，超过此值认为相似（默认 0.9，即 90%）
     * @return 相似度百分比 (0.0 - 1.0)
     */
    fun calculateSimilarity(bitmap1: Bitmap, bitmap2: Bitmap, threshold: Double = 0.9): Double {
        if (bitmap1.width == 0 || bitmap1.height == 0 || bitmap2.width == 0 || bitmap2.height == 0) {
            Log.w("BitmapUtils", "位图尺寸为 0")
            return 0.0
        }
        
        // 如果位图尺寸不同，缩放到相同尺寸
        val targetWidth = minOf(bitmap1.width, bitmap2.width)
        val targetHeight = minOf(bitmap1.height, bitmap2.height)
        
        val scaledBitmap1 = if (bitmap1.width != targetWidth || bitmap1.height != targetHeight) {
            Bitmap.createScaledBitmap(bitmap1, targetWidth, targetHeight, false)
        } else bitmap1
        
        val scaledBitmap2 = if (bitmap2.width != targetWidth || bitmap2.height != targetHeight) {
            Bitmap.createScaledBitmap(bitmap2, targetWidth, targetHeight, false)
        } else bitmap2
        
        val tempBitmap1 = if (scaledBitmap1 !== bitmap1) scaledBitmap1 else null
        val tempBitmap2 = if (scaledBitmap2 !== bitmap2) scaledBitmap2 else null
        
        try {
            val targetBitmap1 = tempBitmap1 ?: bitmap1
            val targetBitmap2 = tempBitmap2 ?: bitmap2
            
            var samePixels = 0
            var totalPixels = 0
            var totalDiff = 0
            
            // 采样比较
            val stepX = maxOf(1, targetWidth / 20)
            val stepY = maxOf(1, targetHeight / 20)
            
            for (y in 0 until targetHeight step stepY) {
                for (x in 0 until targetWidth step stepX) {
                    val pixel1 = targetBitmap1.getPixel(x, y)
                    val pixel2 = targetBitmap2.getPixel(x, y)
                    
                    val r1 = (pixel1 shr 16) and 0xFF
                    val g1 = (pixel1 shr 8) and 0xFF
                    val b1 = pixel1 and 0xFF
                    
                    val r2 = (pixel2 shr 16) and 0xFF
                    val g2 = (pixel2 shr 8) and 0xFF
                    val b2 = pixel2 and 0xFF
                    
                    val diff = kotlin.math.abs(r1 - r2) + kotlin.math.abs(g1 - g2) + kotlin.math.abs(b1 - b2)
                    totalDiff += diff
                    
                    // 如果差异很小（小于30），认为是相似像素
                    if (diff < 30) {
                        samePixels++
                    }
                    totalPixels++
                }
            }
            
            val similarity = samePixels.toDouble() / totalPixels
            val avgDiff = totalDiff.toDouble() / (totalPixels * 3 * 255) // 归一化到 0-1
            val finalSimilarity = similarity * (1 - avgDiff * 0.3) // 综合相似度
            
            Log.d("BitmapUtils", "相似度检测: 尺寸=${targetWidth}x${targetHeight}, " +
                    "采样点数=$totalPixels, 相同像素=$samePixels, " +
                    "相似度=${String.format("%.2f%%", finalSimilarity * 100)}")
            
            return finalSimilarity
        } finally {
            tempBitmap1?.recycle()
            tempBitmap2?.recycle()
        }
    }
    
    /**
     * 判断两个位图是否相似
     * @param bitmap1 第一个位图
     * @param bitmap2 第二个位图
     * @param threshold 相似度阈值（默认 0.9）
     * @return true 如果两个位图相似
     */
    fun areSimilar(bitmap1: Bitmap, bitmap2: Bitmap, threshold: Double = 0.9): Boolean {
        val similarity = calculateSimilarity(bitmap1, bitmap2, threshold)
        val isSimilar = similarity >= threshold
        
        if (isSimilar) {
            Log.d("BitmapUtils", "两个位图相似 (${String.format("%.2f%%", similarity * 100)})")
        } else {
            Log.d("BitmapUtils", "两个位图不相似 (${String.format("%.2f%%", similarity * 100)})")
        }
        
        return isSimilar
    }
    
    fun isBitmapBlack(bitmap: Bitmap, threshold: Double = 0.98): Boolean {
        if (bitmap.width == 0 || bitmap.height == 0) {
            Log.w("BitmapUtils", "Bitmap 尺寸为 0")
            return true
        }
        
        // 如果 Bitmap 是 HARDWARE 格式，需要先转换
        val accessibleBitmap = if (bitmap.config == Bitmap.Config.HARDWARE) {
            Log.d("BitmapUtils", "转换 HARDWARE Bitmap 为 ARGB_8888")
            bitmap.copy(Bitmap.Config.ARGB_8888, false)
        } else {
            null
        }
        
        val targetBitmap = accessibleBitmap ?: bitmap
        
        try {
            // 采样检查，使用更多的采样点以获得更准确的结果
            // 在 1080x1920 的屏幕上，采样约 100 个点
            val samplePoints = 100
            val stepX = maxOf(1, targetBitmap.width / 10)
            val stepY = maxOf(1, targetBitmap.height / 10)
            
            var blackPixels = 0
            var totalPixels = 0
            var minR = 255
            var minG = 255
            var minB = 255
            var maxR = 0
            var maxG = 0
            var maxB = 0
            
            for (y in 0 until targetBitmap.height step stepY) {
                for (x in 0 until targetBitmap.width step stepX) {
                    val pixel = targetBitmap.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF
                    
                    // 记录 RGB 值的范围
                    minR = minOf(minR, r)
                    minG = minOf(minG, g)
                    minB = minOf(minB, b)
                    maxR = maxOf(maxR, r)
                    maxG = maxOf(maxG, g)
                    maxB = maxOf(maxB, b)
                    
                    // 如果 RGB 值都很低（小于 10），认为是黑色
                    if (r < 10 && g < 10 && b < 10) {
                        blackPixels++
                    }
                    totalPixels++
                }
            }
            
            val blackRatio = blackPixels.toDouble() / totalPixels
            Log.d("BitmapUtils", "截图检测: 尺寸=${targetBitmap.width}x${targetBitmap.height}, " +
                    "采样点数=$totalPixels, 黑色像素=$blackPixels, 黑色比例=${String.format("%.2f%%", blackRatio * 100)}, " +
                    "RGB范围: R[$minR-$maxR] G[$minG-$maxG] B[$minB-$maxB], " +
                    "阈值=${String.format("%.2f%%", threshold * 100)}")
            
            val isBlack = blackRatio >= threshold
            if (isBlack) {
                Log.w("BitmapUtils", "检测到截图是全黑的 (${String.format("%.2f%%", blackRatio * 100)} 黑色像素)")
            }
            
            return isBlack
        } finally {
            // 如果创建了临时 Bitmap，需要回收
            accessibleBitmap?.recycle()
        }
    }
}