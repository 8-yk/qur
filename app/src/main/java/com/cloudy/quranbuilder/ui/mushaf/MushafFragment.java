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
    private MushafPagerAdapter    pagerAdapter;

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
            @Override public void onPageSelected(int position) { updateNav(position); }
        });

        binding.btnMushafNext.setOnClickListener(v -> {
            int cur = binding.viewPager.getCurrentItem();
            if (cur < pagerAdapter.getItemCount() - 1)
                binding.viewPager.setCurrentItem(cur + 1, true);
        });
        binding.btnMushafPrev.setOnClickListener(v -> {
            int cur = binding.viewPager.getCurrentItem();
            if (cur > 0) binding.viewPager.setCurrentItem(cur - 1, true);
        });
        binding.btnMushafJump.setOnClickListener(v -> openJumpSheet());

        loadSurahsFromDb();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSurahsFromDb();
    }

    /** يجلب فقط السور التي فيها آيات محفوظة */
    private void loadSurahsFromDb() {
        if (!isAdded() || binding == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
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
                    binding.mushafTopProgress.setVisibility(View.GONE);
                    binding.mushafBottomBar.setVisibility(View.GONE);
                } else {
                    binding.tvMushafEmpty.setVisibility(View.GONE);
                    binding.viewPager.setVisibility(View.VISIBLE);
                    binding.mushafTopProgress.setVisibility(View.VISIBLE);
                    binding.mushafBottomBar.setVisibility(View.VISIBLE);

                    int prevPos = binding.viewPager.getCurrentItem();
                    pagerAdapter.updateList(infos);
                    // نحاول نحافظ على الموضع إن كان ما زال صالحاً
                    int clampedPos = Math.min(prevPos, infos.size() - 1);
                    binding.viewPager.setCurrentItem(Math.max(0, clampedPos), false);
                    updateNav(binding.viewPager.getCurrentItem());
                }
            });
        });
    }

    private void updateNav(int position) {
        if (binding == null) return;
        int total = pagerAdapter.getItemCount();
        if (total == 0) return;

        int progress = Math.max(1, (int) ((position + 1) / (float) total * 100));
        binding.mushafTopProgress.setProgress(progress);
        binding.tvMushafPageNum.setText((position + 1) + " / " + total);
        binding.btnMushafPrev.setAlpha(position == 0         ? 0.25f : 1f);
        binding.btnMushafNext.setAlpha(position == total - 1  ? 0.25f : 1f);
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
    public void onDestroyView() { super.onDestroyView(); binding = null; }

    // ── تأثير تقليب الصفحات ──────────────────────────────────
    static class PageFlipTransformer implements ViewPager2.PageTransformer {
        @Override
        public void transformPage(@NonNull View page, float position) {
            float abs = Math.abs(position);
            page.setAlpha(1f - abs * 0.25f);
            page.setScaleX(1f - abs * 0.02f);
            page.setScaleY(1f - abs * 0.02f);
            page.setRotationY(position * -5f);
        }
    }
}
