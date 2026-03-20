package com.cloudy.quranbuilder.ui.mushaf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.databinding.FragmentMushafBinding;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.cloudy.quranbuilder.ui.add.SurahPickerBottomSheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

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

        pagerAdapter = new MushafPagerAdapter(requireContext(), new ArrayList<>());
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setOffscreenPageLimit(1);
        binding.viewPager.setPageTransformer(new PageFlipTransformer());

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateBars(position);
            }
        });

        binding.btnMushafJump.setOnClickListener(v -> openJumpSheet());

        binding.btnMushafNext.setOnClickListener(v -> {
            int cur = binding.viewPager.getCurrentItem();
            if (cur < pagerAdapter.getItemCount() - 1)
                binding.viewPager.setCurrentItem(cur + 1, true);
        });
        binding.btnMushafPrev.setOnClickListener(v -> {
            int cur = binding.viewPager.getCurrentItem();
            if (cur > 0) binding.viewPager.setCurrentItem(cur - 1, true);
        });

        loadSurahsFromDb();
    }

    @Override
    public void onResume() {
        super.onResume();
        // نعيد التحميل عند العودة للتبويب (لو أضاف المستخدم بيانات جديدة)
        loadSurahsFromDb();
    }

    /**
     * يجلب فقط السور التي تحتوي آيات من قاعدة البيانات.
     */
    private void loadSurahsFromDb() {
        if (!isAdded() || binding == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            // السور التي فيها آيات فعلية
            List<Integer> nums = AppDatabase.getInstance(requireContext())
                    .ayahDao().getSurahNumbersWithData();

            Collections.sort(nums);

            List<SurahInfo> infos = new ArrayList<>();
            for (int n : nums) {
                SurahInfo info = SurahInfo.getByNumber(n);
                if (info != null) infos.add(info);
            }

            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;

                if (infos.isEmpty()) {
                    binding.tvMushafEmpty.setVisibility(View.VISIBLE);
                    binding.viewPager.setVisibility(View.GONE);
                    binding.mushafBottomBar.setVisibility(View.GONE);
                } else {
                    binding.tvMushafEmpty.setVisibility(View.GONE);
                    binding.viewPager.setVisibility(View.VISIBLE);
                    binding.mushafBottomBar.setVisibility(View.VISIBLE);
                    pagerAdapter.updateList(infos);
                    updateBars(binding.viewPager.getCurrentItem());
                }
            });
        });
    }

    private void updateBars(int position) {
        if (binding == null) return;
        int total = pagerAdapter.getItemCount();
        if (total == 0) return;

        int progress = Math.max(1, (int) ((position + 1) / (float) total * 100));
        binding.mushafTopProgress.setProgress(progress);
        binding.tvMushafPageNum.setText((position + 1) + " / " + total);
        binding.btnMushafPrev.setAlpha(position == 0        ? 0.3f : 1f);
        binding.btnMushafNext.setAlpha(position == total - 1 ? 0.3f : 1f);
    }

    private void openJumpSheet() {
        SurahPickerBottomSheet sheet = new SurahPickerBottomSheet();
        sheet.setOnSurahSelectedListener(info -> {
            int pos = pagerAdapter.getPositionForSurah(info.number);
            if (pos >= 0 && binding != null)
                binding.viewPager.setCurrentItem(pos, true);
        });
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
            page.setAlpha(1f - absPos * 0.3f);
            page.setScaleX(1f - absPos * 0.03f);
            page.setScaleY(1f - absPos * 0.03f);
            page.setRotationY(position * -6f);
        }
    }
}
