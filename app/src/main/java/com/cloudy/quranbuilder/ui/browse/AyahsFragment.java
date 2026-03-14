package com.cloudy.quranbuilder.ui.browse;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.cloudy.quranbuilder.data.*;
import com.cloudy.quranbuilder.databinding.FragmentAyahsBinding;
import com.cloudy.quranbuilder.model.SurahInfo;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;
import java.util.concurrent.Executors;

public class AyahsFragment extends Fragment {

    private static final String ARG_NUM  = "surah_number";
    private static final String ARG_NAME = "surah_name";

    private FragmentAyahsBinding binding;
    private AyahAdapter adapter;
    private int     surahNumber;
    private String  surahName;
    private boolean surahIsMeccan    = true;
    private int     surahAyahsTotal  = 0;

    public static AyahsFragment newInstance(int num, String name) {
        AyahsFragment f = new AyahsFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_NUM, num); b.putString(ARG_NAME, name);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle s) {
        binding = FragmentAyahsBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle s) {
        super.onViewCreated(view, s);

        if (getArguments() != null) {
            surahNumber = getArguments().getInt(ARG_NUM);
            surahName   = getArguments().getString(ARG_NAME);
        }

        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.tvSurahTitle.setText(surahName);

        // قيمة مبدئية من SurahInfo الثابتة
        SurahInfo info = SurahInfo.getByNumber(surahNumber);
        if (info != null) surahIsMeccan = info.isMeccan;

        adapter = new AyahAdapter();
        binding.recyclerAyahs.setAdapter(adapter);
        binding.fabAddAyahs.setOnClickListener(v -> openQuickAdd());

        loadAyahs();
    }

    private void openQuickAdd() {
        QuickAddSheet sheet = QuickAddSheet.newInstance(surahNumber, surahName, surahIsMeccan);
        sheet.setOnAyahsSavedListener(count -> {
            if (!isAdded() || binding == null) return;
            Snackbar.make(binding.getRoot(), "✓ تم حفظ " + count + " آية", Snackbar.LENGTH_SHORT).show();
            loadAyahs();
        });
        sheet.show(getParentFragmentManager(), "quick_add");
    }

    private void loadAyahs() {
        if (!isAdded() || binding == null) return;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);
        binding.recyclerAyahs.setVisibility(View.GONE);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<AyahEntity> ayahs = db.ayahDao().getAyahsForSurahSync(surahNumber);

            // جلب بيانات السورة من DB
            SurahEntity dbSurah = db.surahDao().getByNumber(surahNumber);
            if (dbSurah != null) {
                surahIsMeccan   = dbSurah.isMeccan;
                surahAyahsTotal = dbSurah.ayahsInSurah;
            }

            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.progressBar.setVisibility(View.GONE);

                // شريط البيانات
                String type  = surahIsMeccan ? "مكية" : "مدنية";
                String total = surahAyahsTotal > 0 ? " · " + surahAyahsTotal + " آية" : "";
                binding.tvSurahMeta.setText("سورة " + surahNumber + " · " + type + total);

                if (ayahs.isEmpty()) {
                    binding.emptyState.setVisibility(View.VISIBLE);
                    binding.tvAyahCount.setVisibility(View.GONE);
                } else {
                    binding.recyclerAyahs.setVisibility(View.VISIBLE);
                    adapter.submitList(ayahs);
                    String saved = ayahs.size() + (surahAyahsTotal > 0 ? "/" + surahAyahsTotal : "") + " آية";
                    binding.tvAyahCount.setText(saved);
                    binding.tvAyahCount.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    @Override public void onDestroyView() { super.onDestroyView(); binding = null; }
}
