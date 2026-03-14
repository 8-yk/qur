package com.cloudy.quranbuilder.ui.mushaf;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.cloudy.quranbuilder.R;
import com.cloudy.quranbuilder.databinding.FragmentMushafBinding;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.cloudy.quranbuilder.ui.add.SurahPickerBottomSheet;

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

        // تأثير تقليب الصفحات
        binding.viewPager.setPageTransformer(new PageFlipTransformer());

        // مراقبة تغيير الصفحة
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateTopBar(position);
                updateBottomBar(position);
            }
        });

        // إعداد زر الانتقال
        binding.btnMushafJump.setOnClickListener(v -> openJumpSheet());

        // أزرار التنقل
        binding.btnMushafNext.setOnClickListener(v -> {
            int cur = binding.viewPager.getCurrentItem();
            if (cur < 113) binding.viewPager.setCurrentItem(cur + 1, true);
        });
        binding.btnMushafPrev.setOnClickListener(v -> {
            int cur = binding.viewPager.getCurrentItem();
            if (cur > 0) binding.viewPager.setCurrentItem(cur - 1, true);
        });

        // تهيئة
        updateTopBar(0);
        updateBottomBar(0);
    }

    private void updateTopBar(int position) {
        if (binding == null) return;
        SurahInfo info = SurahInfo.ALL_SURAHS[position];
        binding.tvMushafSurahName.setText("سورة " + info.name);
        binding.tvMushafSurahMeta.setText(
                info.getRevelationType() + " · " + info.totalAyahs + " آية · الجزء");
        int progress = Math.max(1, (int) ((position + 1) / 114.0 * 100));
        binding.mushafTopProgress.setProgress(progress);
    }

    private void updateBottomBar(int position) {
        if (binding == null) return;
        binding.tvMushafPageNum.setText((position + 1) + " / 114");
        binding.btnMushafPrev.setAlpha(position == 0 ? 0.3f : 1f);
        binding.btnMushafNext.setAlpha(position == 113 ? 0.3f : 1f);
    }

    private void openJumpSheet() {
        SurahPickerBottomSheet sheet = new SurahPickerBottomSheet();
        sheet.setOnSurahSelectedListener(info ->
                binding.viewPager.setCurrentItem(info.number - 1, true));
        sheet.show(getParentFragmentManager(), "mushaf_jump");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ── تأثير تقليب الصفحات ──────────────────────────────────
    static class PageFlipTransformer implements ViewPager2.PageTransformer {
        @Override
        public void transformPage(@NonNull View page, float position) {
            float absPos = Math.abs(position);
            page.setAlpha(1f - absPos * 0.35f);
            page.setScaleX(1f - absPos * 0.04f);
            page.setScaleY(1f - absPos * 0.04f);
            // دوران خفيف يشبه تقليب الورق
            page.setRotationY(position * -8f);
        }
    }
}
