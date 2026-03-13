package com.cloudy.quranbuilder;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cloudy.quranbuilder.databinding.ActivityMainBinding;
import com.cloudy.quranbuilder.ui.add.AddFragment;
import com.cloudy.quranbuilder.ui.browse.BrowseFragment;
import com.cloudy.quranbuilder.ui.importexport.ImportExportFragment;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // الـ fragments الرئيسية — نحتفظ بها لتجنب إعادة الإنشاء
    private BrowseFragment browseFragment;
    private AddFragment addFragment;
    private ImportExportFragment importExportFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFragments();
        setupBottomNav();
    }

    private void initFragments() {
        browseFragment = new BrowseFragment();
        addFragment = new AddFragment();
        importExportFragment = new ImportExportFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, importExportFragment, "import_export").hide(importExportFragment)
                .add(R.id.fragmentContainer, addFragment, "add").hide(addFragment)
                .add(R.id.fragmentContainer, browseFragment, "browse")
                .commit();

        activeFragment = browseFragment;
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_browse) {
                showFragment(browseFragment);
            } else if (id == R.id.nav_add) {
                showFragment(addFragment);
            } else if (id == R.id.nav_data) {
                showFragment(importExportFragment);
            }
            return true;
        });
    }

    private void showFragment(Fragment target) {
        if (activeFragment == target) return;
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(target)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        activeFragment = target;
    }

    /** يُستدعى من BrowseFragment لفتح شاشة الآيات */
    public void openAyahsScreen(int surahNumber, String surahName) {
        Fragment ayahsFragment = com.cloudy.quranbuilder.ui.browse.AyahsFragment
                .newInstance(surahNumber, surahName);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.slide_out_right
                )
                .replace(R.id.fragmentContainer, ayahsFragment)
                .addToBackStack(null)
                .commit();

        binding.bottomNav.setVisibility(android.view.View.GONE);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            binding.bottomNav.setVisibility(android.view.View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
