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

package io.geeteshk.hyper.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import io.geeteshk.hyper.R;

public class CreateAdapter extends ArrayAdapter<String> {

    private Context context;

    public CreateAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View rootView;

        if (convertView == null) {
            rootView = LayoutInflater.from(context).inflate(R.layout.item_dialog_list, parent, false);
        } else {
            rootView = convertView;
        }

        ImageView imageView = rootView.findViewById(R.id.dialog_list_item_image);
        TextView textView = rootView.findViewById(R.id.dialog_list_item_text);

        switch (position) {
            case 0:
                imageView.setImageResource(R.drawable.ic_action_create);
                textView.setText("Create a new project");
                break;
            case 1:
                imageView.setImageResource(R.drawable.ic_action_clone);
                textView.setText("Clone a repository");
                break;
            case 2:
                imageView.setImageResource(R.drawable.ic_action_import);
                textView.setText("Import an external project");
                break;
        }

        return rootView;
    }

    @Override
    public int getCount() {
        return 3;
    }
}
