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

package io.geeteshk.hyper.ui.adapter

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

import io.geeteshk.hyper.R
import io.geeteshk.hyper.ui.fragment.IntroFragment

class IntroAdapter(private val context: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    private val bgColors: IntArray = context.resources.getIntArray(R.array.bg_screens)
    private val images = intArrayOf(R.drawable.ic_intro_logo_n, R.drawable.ic_intro_editor, R.drawable.ic_intro_git, R.drawable.ic_intro_done)
    private val titles: Array<String> = context.resources.getStringArray(R.array.slide_titles)
    private val desc: Array<String> = context.resources.getStringArray(R.array.slide_desc)

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putInt("bg", bgColors[position])
        bundle.putInt("image", images[position])
        bundle.putString("title", titles[position])
        bundle.putString("desc", desc[position])
        return Fragment.instantiate(context, IntroFragment::class.java.name, bundle)
    }

    override fun getCount(): Int = images.size
}
