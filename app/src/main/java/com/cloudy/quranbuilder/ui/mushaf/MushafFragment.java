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
import com.cloudy.quranbuilder.data.AyahDao;
import com.cloudy.quranbuilder.databinding.FragmentMushafBinding;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.cloudy.quranbuilder.ui.add.SurahPickerBottomSheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MushafFragment extends Fragment {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private FragmentMushafBinding binding;
    private MushafPagerAdapter    pagerAdapter;
    private boolean               dataFresh = false;

    // ── Lifecycle ────────────────────────────────────────────

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
        binding.viewPager.setPageTransformer(new PageFlipTransformer());

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) {
                updateTopBar(position);
                updateNav(position);
            }
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

        loadData();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && !dataFresh) loadData();
    }

    /** يُستدعى من MainActivity عند الانتقال لتبويب التعديل */
    public void markDataStale() { dataFresh = false; }

    // ── تحميل البيانات ──────────────────────────────────────

    private void loadData() {
        if (!isAdded() || binding == null) return;

        EXECUTOR.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int ayahsWithPage = db.ayahDao().getAyahsWithPageCount();

            if (ayahsWithPage > 0) {
                // ── وضع الصفحات: استعلام واحد يُحضر كل البيانات ──
                List<AyahDao.PageMetaSummary> allMeta = db.ayahDao().getAllPageMeta();

                List<Integer>                         pageList = new ArrayList<>();
                Map<Integer, MushafPagerAdapter.PageMeta> metaMap  = new HashMap<>();

                for (AyahDao.PageMetaSummary m : allMeta) {
                    pageList.add(m.page);

                    // تحليل أسماء السور من "112,113,114"
                    List<String> names = new ArrayList<>();
                    if (m.surahNums != null) {
                        // نرتّب الأرقام تصاعدياً
                        String[] parts = m.surahNums.split(",");
                        List<Integer> nums = new ArrayList<>();
                        for (String p : parts) {
                            try { nums.add(Integer.parseInt(p.trim())); }
                            catch (NumberFormatException ignored) {}
                        }
                        Collections.sort(nums);
                        for (int n : nums) {
                            SurahInfo info = SurahInfo.getByNumber(n);
                            if (info != null) names.add(info.name);
                        }
                    }
                    metaMap.put(m.page,
                            new MushafPagerAdapter.PageMeta(m.page, m.juz, names));
                }

                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    dataFresh = true;

                    if (pageList.isEmpty()) { showEmpty(); return; }

                    int prevPos = binding.viewPager.getCurrentItem();
                    pagerAdapter.setPageMode(pageList, metaMap);
                    int clamp = Math.min(prevPos, pageList.size() - 1);
                    binding.viewPager.setCurrentItem(Math.max(0, clamp), false);
                    showContent();
                    // تحديث شريط العنوان بعد تعيين الموضع مباشرة
                    updateTopBar(binding.viewPager.getCurrentItem());
                    updateNav(binding.viewPager.getCurrentItem());
                });

            } else {
                // ── Fallback: سورة بسورة ──────────────────────
                List<Integer> surahNums = db.ayahDao().getSurahNumbersWithData();
                Collections.sort(surahNums);

                List<SurahInfo> infoList = new ArrayList<>();
                for (int n : surahNums) {
                    SurahInfo info = SurahInfo.getByNumber(n);
                    if (info != null) infoList.add(info);
                }

                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    dataFresh = true;

                    if (infoList.isEmpty()) { showEmpty(); return; }

                    int prevPos = binding.viewPager.getCurrentItem();
                    pagerAdapter.setSurahMode(infoList);
                    int clamp = Math.min(prevPos, infoList.size() - 1);
                    binding.viewPager.setCurrentItem(Math.max(0, clamp), false);
                    showContent();
                    updateTopBar(binding.viewPager.getCurrentItem());
                    updateNav(binding.viewPager.getCurrentItem());
                });
            }
        });
    }

    // ── تحديث الواجهة ────────────────────────────────────────

    /**
     * يُحدّث شريط العنوان عند الانتقال لصفحة جديدة.
     * اليمين: أسماء كل السور في هذه الصفحة (مثل "الإخلاص · الفلق · الناس")
     * اليسار: اسم الجزء كاملاً
     */
    private void updateTopBar(int position) {
        if (binding == null || pagerAdapter == null) return;
        MushafPagerAdapter.PageMeta meta = pagerAdapter.getMetaAt(position);
        if (meta == null) return;

        binding.tvTopSurahNames.setText(meta.getSurahNamesLabel());
        binding.tvTopJuzName.setText(
                meta.juz > 0 ? MushafPagerAdapter.juzName(meta.juz) : "");
    }

    private void updateNav(int position) {
        if (binding == null) return;
        int total = pagerAdapter.getItemCount();
        if (total == 0) return;

        int progress = Math.max(1, (int) ((position + 1) / (float) total * 100));
        binding.mushafTopProgress.setProgress(progress);
        binding.tvMushafPageNum.setText((position + 1) + " / " + total);
        binding.btnMushafPrev.setAlpha(position == 0         ? 0.25f : 1f);
        binding.btnMushafNext.setAlpha(position == total - 1 ? 0.25f : 1f);
    }

    private void showEmpty() {
        binding.tvMushafEmpty.setVisibility(View.VISIBLE);
        binding.viewPager.setVisibility(View.GONE);
        binding.mushafTopProgress.setVisibility(View.GONE);
        binding.mushafBottomBar.setVisibility(View.GONE);
        binding.mushafTopBar.setVisibility(View.GONE);
    }

    private void showContent() {
        binding.tvMushafEmpty.setVisibility(View.GONE);
        binding.viewPager.setVisibility(View.VISIBLE);
        binding.mushafTopProgress.setVisibility(View.VISIBLE);
        binding.mushafBottomBar.setVisibility(View.VISIBLE);
        binding.mushafTopBar.setVisibility(View.VISIBLE);
    }

    private void openJumpSheet() {
        SurahPickerBottomSheet sheet = new SurahPickerBottomSheet();
        sheet.setOnSurahSelectedListener(info -> {
            if (binding == null) return;
            int pos = pagerAdapter.getPositionForSurah(info.number);
            if (pos >= 0) binding.viewPager.setCurrentItem(pos, true);
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
            float abs = Math.abs(position);
            page.setAlpha(1f - abs * 0.2f);
            page.setScaleX(1f - abs * 0.015f);
            page.setScaleY(1f - abs * 0.015f);
            page.setRotationY(position * -4f);
        }
    }
}
