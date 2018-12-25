/*
 * Copyright 2016 Geetesh Kalakoti <kalakotig@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.hyper.ui.fragment.analyze

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.ViewPortHandler
import io.geeteshk.hyper.R
import io.geeteshk.hyper.extensions.compatColor
import io.geeteshk.hyper.extensions.inflate
import io.geeteshk.hyper.util.Prefs.defaultPrefs
import io.geeteshk.hyper.util.Prefs.get
import io.geeteshk.hyper.util.project.ProjectManager
import kotlinx.android.synthetic.main.fragment_analyze_file.*
import java.io.File
import java.util.*

class AnalyzeFileFragment : Fragment() {

    private lateinit var pieColors: ArrayList<Int>
    private lateinit var projectDir: File
    internal lateinit var activity: FragmentActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            container?.inflate(R.layout.fragment_analyze_file)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = defaultPrefs(activity)
        val darkTheme = prefs["dark_theme", false]!!

        val lightColor = activity.compatColor(androidx.appcompat.R.color.abc_primary_text_material_light)
        val darkColor = activity.compatColor(androidx.appcompat.R.color.abc_primary_text_material_dark)

        projectDir = File(arguments!!.getString("project_file")!!)
        pieColors = ArrayList()

        for (c in ColorTemplate.VORDIPLOM_COLORS)
            pieColors.add(c)

        for (c in ColorTemplate.JOYFUL_COLORS)
            pieColors.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS)
            pieColors.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS)
            pieColors.add(c)

        pieColors.add(ColorTemplate.getHoloBlue())
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(8f, 12f, 8f, 8f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(0x00000000)
        pieChart.setTransparentCircleColor(if (darkTheme) lightColor else darkColor)
        pieChart.setTransparentCircleAlpha(110)
        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f
        pieChart.rotationAngle = 0f
        pieChart.isRotationEnabled = false
        pieChart.isHighlightPerTapEnabled = true

        var byteSize = 0L
        projectDir.walkTopDown().forEach { byteSize += it.length() }
        pieChart.centerText = ProjectManager.humanReadableByteCount(byteSize)

        pieChart.setCenterTextSize(48f)
        pieChart.setCenterTextColor(if (darkTheme) darkColor else lightColor)
        pieChart.setDrawCenterText(true)
        pieChart.setDrawEntryLabels(false)

        setData(false)

        val pieLegend = pieChart.legend
        pieLegend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        pieLegend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        pieLegend.orientation = Legend.LegendOrientation.VERTICAL
        pieLegend.setDrawInside(false)
        pieLegend.xEntrySpace = 0f
        pieLegend.yEntrySpace = 0f
        pieLegend.yOffset = 10f
        pieLegend.formSize = 12f
        pieLegend.textSize = 12f
        pieLegend.typeface = Typeface.DEFAULT_BOLD
        pieLegend.textColor = if (darkTheme) darkColor else lightColor

        switchFile.setOnCheckedChangeListener { _, b ->
            if (b) {
                sizeText!!.animate().alpha(1f)
                countText!!.animate().alpha(0.4f)
                setData(false)
            } else {
                countText!!.animate().alpha(1f)
                sizeText!!.animate().alpha(0.4f)
                setData(true)
            }
        }
    }

    private fun setData(isCount: Boolean) {
        if (isCount) {
            val entries = ArrayList<PieEntry>()
            val files = projectDir.listFiles()
            val filesAndCounts = HashMap<String, Int>()
            files.map { it.extension }
                    .forEach {
                        if (filesAndCounts.containsKey(it)) {
                            filesAndCounts[it] = filesAndCounts[it]!!.plus(1)
                        } else {
                            filesAndCounts[it] = 1
                        }
                    }

            for ((fileType, count) in filesAndCounts) {
                entries.add(PieEntry(count.toFloat(), fileType))
            }

            val pieDataSet = PieDataSet(entries, "")
            pieDataSet.setDrawIcons(false)
            pieDataSet.sliceSpace = 3f
            pieDataSet.selectionShift = 3f
            pieDataSet.colors = pieColors

            val pieData = PieData(pieDataSet)
            pieData.setValueFormatter(DefaultValueFormatter(0))
            pieData.setValueTextColor(-0x7b000000)
            pieData.setValueTextSize(20f)
            pieData.setValueTypeface(Typeface.DEFAULT_BOLD)
            pieChart.data = pieData
        } else {
            val entries = ArrayList<PieEntry>()
            val files = projectDir.listFiles()
            val filesAndCounts = HashMap<String, Long>()
            for (file in files) {
                val extension = file.extension
                if (filesAndCounts.containsKey(extension)) {
                    filesAndCounts[extension] = filesAndCounts[extension]!!.plus(file.length())
                } else {
                    filesAndCounts[extension] = file.length()
                }
            }

            for ((fileType, count) in filesAndCounts) {
                entries.add(PieEntry(count.toFloat(), fileType))
            }

            val pieDataSet = PieDataSet(entries, "")
            pieDataSet.setDrawIcons(false)
            pieDataSet.sliceSpace = 3f
            pieDataSet.selectionShift = 3f
            pieDataSet.colors = pieColors

            val pieData = PieData(pieDataSet)
            pieData.setValueFormatter(SizeValueFormatter())
            pieData.setValueTextColor(-0x7b000000)
            pieData.setValueTextSize(16f)
            pieData.setValueTypeface(Typeface.DEFAULT_BOLD)
            pieChart.data = pieData
        }

        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad)
    }

    private inner class SizeValueFormatter : IValueFormatter {

        override fun getFormattedValue(value: Float, entry: Entry, dataSetIndex: Int, viewPortHandler: ViewPortHandler): String =
                ProjectManager.humanReadableByteCount(value.toLong())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as FragmentActivity
    }
}
