package com.cloudy.quranbuilder.ui.importexport;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.cloudy.quranbuilder.data.AppDatabase;
import com.cloudy.quranbuilder.databinding.FragmentImportExportBinding;
import com.cloudy.quranbuilder.utils.JsonHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executors;

public class ImportExportFragment extends Fragment {

    private FragmentImportExportBinding binding;

    private final ActivityResultLauncher<Intent> exportLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) performExport(uri);
            }
        }
    );

    private final ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri != null) performImport(uri);
            }
        }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentImportExportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnExport.setOnClickListener(v -> openExportPicker());
        binding.btnImport.setOnClickListener(v -> openImportPicker());
        binding.btnClearAll.setOnClickListener(v -> confirmClearAll());

        loadStats();
    }

    // حذف onResume لتفادي التحميل المزدوج — loadStats يُستدعى من onViewCreated فقط

    private void loadStats() {
        if (!isAdded()) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int totalSurahs = db.surahDao().getSurahCount();
            int totalAyahs  = db.ayahDao().getTotalAyahCount();

            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                binding.tvStatSurahs.setText(String.valueOf(totalSurahs));
                binding.tvStatAyahs.setText(String.valueOf(totalAyahs));

                int progress = (int) Math.round(totalSurahs / 114.0 * 100);
                binding.progressCompletion.setProgress(progress);
                binding.tvProgressLabel.setText(progress + "% مكتمل");
                binding.btnExport.setEnabled(totalAyahs > 0);
            });
        });
    }

    private void openExportPicker() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "quran_data.json");
        exportLauncher.launch(intent);
    }

    private void openImportPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        importLauncher.launch(intent);
    }

    private void performExport(Uri uri) {
        if (!isAdded() || binding == null) return;
        binding.btnExport.setEnabled(false);
        binding.progressExport.setVisibility(View.VISIBLE);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JsonHelper.exportToUri(requireContext(), uri);
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressExport.setVisibility(View.GONE);
                    binding.btnExport.setEnabled(true);
                    Snackbar.make(binding.getRoot(), "✓ تم التصدير بنجاح", Snackbar.LENGTH_LONG).show();
                });
            } catch (Exception e) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressExport.setVisibility(View.GONE);
                    binding.btnExport.setEnabled(true);
                    Snackbar.make(binding.getRoot(), "✗ فشل التصدير: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void performImport(Uri uri) {
        if (!isAdded() || binding == null) return;
        binding.btnImport.setEnabled(false);
        binding.progressImport.setVisibility(View.VISIBLE);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                JsonHelper.ImportResult result = JsonHelper.importFromUri(requireContext(), uri);
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressImport.setVisibility(View.GONE);
                    binding.btnImport.setEnabled(true);
                    Snackbar.make(binding.getRoot(),
                        "✓ تم الاستيراد: " + result.surahsAdded + " سورة · " + result.ayahsAdded + " آية",
                        Snackbar.LENGTH_LONG).show();
                    loadStats();
                });
            } catch (Exception e) {
                if (!isAdded() || binding == null) return;
                requireActivity().runOnUiThread(() -> {
                    if (binding == null) return;
                    binding.progressImport.setVisibility(View.GONE);
                    binding.btnImport.setEnabled(true);
                    Snackbar.make(binding.getRoot(), "✗ فشل الاستيراد: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmClearAll() {
        if (!isAdded()) return;
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("مسح جميع البيانات")
            .setMessage("هل أنت متأكد؟ سيتم حذف جميع الآيات والسور المحفوظة نهائياً.")
            .setPositiveButton("مسح", (dialog, which) -> clearAll())
            .setNegativeButton("إلغاء", null)
            .show();
    }

    private void clearAll() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            db.ayahDao().deleteAll();
            db.surahDao().deleteAll();
            if (!isAdded() || binding == null) return;
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                loadStats();
                Snackbar.make(binding.getRoot(), "تم مسح جميع البيانات", Snackbar.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
