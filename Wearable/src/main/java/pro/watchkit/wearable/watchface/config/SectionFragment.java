/*
 * Copyright (C) 2018-2019 Terence Tan
 *
 *  This file is free software: you may copy, redistribute and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  This file is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 *     Copyright (C) 2017 The Android Open Source Project
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package pro.watchkit.wearable.watchface.config;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import pro.watchkit.wearable.watchface.R;


/**
 * Basic section container.
 *
 * <p>TODO: Replace these with implementations to match your apps functionality.
 */
public class SectionFragment extends Fragment {

    public static final String EXTRA_SECTION =
            "com.example.android.wearable.navaction.EXTRA_SECTION";
    private Section mSection;
    private ImageView mEmojiView;
    private TextView mTitleView;

    /**
     * Helper method to quickly create sections.
     *
     * @param section The {@link Section} to use.
     * @return A new SectionFragment with arguments set based on the provided Section.
     */
    public static SectionFragment getSection(final Section section) {
        final SectionFragment newSection = new SectionFragment();
        final Bundle arguments = new Bundle();
        arguments.putSerializable(EXTRA_SECTION, section);
        newSection.setArguments(arguments);
        return newSection;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_section, container, false);
        mEmojiView = view.findViewById(R.id.emoji);
        mTitleView = view.findViewById(R.id.title);

        if (getArguments() != null) {
            mSection = (Section) getArguments().getSerializable(EXTRA_SECTION);
            final Drawable imageDrawable =
                    ContextCompat.getDrawable(getContext(), mSection.drawableRes);
            mEmojiView.setImageDrawable(imageDrawable);
            mTitleView.setText(getResources().getString(mSection.titleRes));
        }

        return view;
    }

    public enum Section {
        Sun(R.string.color_gray, R.drawable.ic_lock_open_white_24dp),
        Moon(R.string.color_green, R.drawable.ic_add_white_24dp),
        Earth(R.string.color_black, R.drawable.ic_more_horiz_24dp_wht),
        Settings(R.string.color_blue, R.drawable.ic_notifications_white_24dp);

        final int titleRes;
        final int drawableRes;

        Section(final int titleRes, final int drawableRes) {
            this.titleRes = titleRes;
            this.drawableRes = drawableRes;
        }
    }
}