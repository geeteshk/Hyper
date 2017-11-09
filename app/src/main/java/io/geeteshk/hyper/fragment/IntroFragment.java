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

package io.geeteshk.hyper.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.geeteshk.hyper.R;

public class IntroFragment extends Fragment {

    public IntroFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_intro, container, false);
        RelativeLayout slideLayout = rootView.findViewById(R.id.slide_layout);
        ImageView slideImage = rootView.findViewById(R.id.slide_image);
        TextView slideTitle = rootView.findViewById(R.id.slide_title);
        TextView slideDesc = rootView.findViewById(R.id.slide_desc);
        Bundle arguments = getArguments();

        if (arguments != null) {
            slideLayout.setBackgroundColor(arguments.getInt("bg"));
            slideImage.setImageResource(arguments.getInt("image"));
            slideTitle.setText(arguments.getString("title"));
            slideDesc.setText(arguments.getString("desc"));
        }

        return rootView;
    }
}
