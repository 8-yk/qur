package com.cloudy.quranbuilder.ui.mushaf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cloudy.quranbuilder.databinding.FragmentMushafBinding;
import com.cloudy.quranbuilder.model.SurahInfo;

public class MushafFragment extends Fragment {

    private FragmentMushafBinding binding;
    private MushafPagerAdapter pagerAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle saved) {
        binding = FragmentMushafBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        super.onViewCreated(view, saved);

        pagerAdapter = new MushafPagerAdapter(requireContext());
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setOffscreenPageLimit(1);

        // تأثير التقليب الجميل
        binding.viewPager.setPageTransformer((page, position) -> {
            float absPos = Math.abs(position);
            page.setAlpha(1f - absPos * 0.4f);
            page.setScaleY(1f - absPos * 0.05f);
        });

        // مؤشر السورة الحالية
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                SurahInfo info = SurahInfo.ALL_SURAHS[position];
                binding.tvMushafSurahIndicator.setText(
                        info.number + " · سورة " + info.name
                );
                int progress = (int) ((position + 1) / 114.0 * 100);
                binding.mushafTopProgress.setProgress(progress);
            }
        });

        // الإعداد الأولي
        SurahInfo first = SurahInfo.ALL_SURAHS[0];
        binding.tvMushafSurahIndicator.setText("1 · سورة " + first.name);
        binding.mushafTopProgress.setProgress(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
