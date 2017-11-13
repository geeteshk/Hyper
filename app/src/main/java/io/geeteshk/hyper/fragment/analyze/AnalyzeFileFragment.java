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

package io.geeteshk.hyper.fragment.analyze;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.geeteshk.hyper.R;
import io.geeteshk.hyper.helper.Prefs;
import io.geeteshk.hyper.helper.ProjectManager;

public class AnalyzeFileFragment extends Fragment {

    @BindView(R.id.count_text) TextView countText;
    @BindView(R.id.size_text) TextView sizeText;
    @BindView(R.id.switch_file) SwitchCompat fileSwitch;
    @BindView(R.id.pie_chart_analyze) PieChart pieChart;

    ArrayList<Integer> pieColors;
    File projectDir;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean darkTheme = Prefs.get(getActivity(), "dark_theme", false);
        int lightColor = ContextCompat.getColor(getActivity(), android.support.v7.appcompat.R.color.abc_primary_text_material_light);
        int darkColor = ContextCompat.getColor(getActivity(), android.support.v7.appcompat.R.color.abc_primary_text_material_dark);
        projectDir = new File(getArguments().getString("project_file"));
        View rootView = inflater.inflate(R.layout.fragment_analyze_file, container, false);
        ButterKnife.bind(this, rootView);

        pieColors = new ArrayList<>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            pieColors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            pieColors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            pieColors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            pieColors.add(c);

        pieColors.add(ColorTemplate.getHoloBlue());
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(8, 12, 8, 8);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(0x00000000);
        pieChart.setTransparentCircleColor(darkTheme ? lightColor : darkColor);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setRotationAngle(0);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setCenterText(ProjectManager.humanReadableByteCount(FileUtils.sizeOfDirectory(projectDir), true));
        pieChart.setCenterTextSize(48f);
        pieChart.setCenterTextColor(darkTheme ? darkColor : lightColor);
        pieChart.setDrawCenterText(true);
        pieChart.setDrawEntryLabels(false);

        setData(true);

        Legend pieLegend = pieChart.getLegend();
        pieLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        pieLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        pieLegend.setOrientation(Legend.LegendOrientation.VERTICAL);
        pieLegend.setDrawInside(false);
        pieLegend.setXEntrySpace(0f);
        pieLegend.setYEntrySpace(0f);
        pieLegend.setYOffset(10f);
        pieLegend.setFormSize(12f);
        pieLegend.setTextSize(12f);
        pieLegend.setTypeface(Typeface.DEFAULT_BOLD);
        pieLegend.setTextColor(darkTheme ? darkColor : lightColor);

        fileSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sizeText.animate().alpha(1);
                    countText.animate().alpha(0.4f);
                    setData(false);
                } else {
                    countText.animate().alpha(1);
                    sizeText.animate().alpha(0.4f);
                    setData(true);
                }
            }
        });

        return rootView;
    }

    private void setData(boolean isCount) {
        if (isCount) {
            List<PieEntry> entries = new ArrayList<>();
            List<File> files = (List<File>) FileUtils.listFiles(projectDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            HashMap<String, Integer> filesAndCounts = new HashMap<>();
            for (File file : files) {
                String extension = FilenameUtils.getExtension(file.getName());
                if (filesAndCounts.containsKey(extension)) {
                    filesAndCounts.put(extension, filesAndCounts.get(extension) + 1);
                } else {
                    filesAndCounts.put(extension, 1);
                }
            }

            for (Map.Entry<String, Integer> fileEntry : filesAndCounts.entrySet()) {
                String fileType = fileEntry.getKey();
                int count = fileEntry.getValue();
                entries.add(new PieEntry(count, fileType));
            }

            PieDataSet pieDataSet = new PieDataSet(entries, "");
            pieDataSet.setDrawIcons(false);
            pieDataSet.setSliceSpace(3f);
            pieDataSet.setSelectionShift(3f);
            pieDataSet.setColors(pieColors);

            PieData pieData = new PieData(pieDataSet);
            pieData.setValueFormatter(new DefaultValueFormatter(0));
            pieData.setValueTextColor(0x85000000);
            pieData.setValueTextSize(20f);
            pieData.setValueTypeface(Typeface.DEFAULT_BOLD);
            pieChart.setData(pieData);
        } else {
            List<PieEntry> entries = new ArrayList<>();
            List<File> files = (List<File>) FileUtils.listFiles(projectDir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            HashMap<String, Long> filesAndCounts = new HashMap<>();
            for (File file : files) {
                String extension = FilenameUtils.getExtension(file.getName());
                if (filesAndCounts.containsKey(extension)) {
                    filesAndCounts.put(extension, filesAndCounts.get(extension) + file.length());
                } else {
                    filesAndCounts.put(extension, file.length());
                }
            }

            for (Map.Entry<String, Long> fileEntry : filesAndCounts.entrySet()) {
                String fileType = fileEntry.getKey();
                long count = fileEntry.getValue();
                entries.add(new PieEntry(count, fileType));
            }

            PieDataSet pieDataSet = new PieDataSet(entries, "");
            pieDataSet.setDrawIcons(false);
            pieDataSet.setSliceSpace(3f);
            pieDataSet.setSelectionShift(3f);
            pieDataSet.setColors(pieColors);

            PieData pieData = new PieData(pieDataSet);
            pieData.setValueFormatter(new SizeValueFormatter());
            pieData.setValueTextColor(0x85000000);
            pieData.setValueTextSize(16f);
            pieData.setValueTypeface(Typeface.DEFAULT_BOLD);
            pieChart.setData(pieData);
        }

        pieChart.highlightValues(null);
        pieChart.invalidate();
        pieChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
    }

    private class SizeValueFormatter implements IValueFormatter {

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return ProjectManager.humanReadableByteCount((long) value, true);
        }
    }
}
