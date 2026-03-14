package com.cloudy.quranbuilder;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.cloudy.quranbuilder.databinding.ActivityMainBinding;
import com.cloudy.quranbuilder.ui.add.AddFragment;
import com.cloudy.quranbuilder.ui.browse.BrowseFragment;
import com.cloudy.quranbuilder.ui.importexport.ImportExportFragment;
import com.cloudy.quranbuilder.ui.mushaf.MushafFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private BrowseFragment       browseFragment;
    private MushafFragment       mushafFragment;
    private AddFragment          addFragment;
    private ImportExportFragment importExportFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFragments(savedInstanceState);
        setupBottomNav();
    }

    private void initFragments(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            browseFragment       = new BrowseFragment();
            mushafFragment       = new MushafFragment();
            addFragment          = new AddFragment();
            importExportFragment = new ImportExportFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, importExportFragment, "data").hide(importExportFragment)
                    .add(R.id.fragmentContainer, addFragment,          "add").hide(addFragment)
                    .add(R.id.fragmentContainer, mushafFragment,       "mushaf").hide(mushafFragment)
                    .add(R.id.fragmentContainer, browseFragment,       "browse")
                    .commit();
        } else {
            browseFragment       = (BrowseFragment)       getSupportFragmentManager().findFragmentByTag("browse");
            mushafFragment       = (MushafFragment)       getSupportFragmentManager().findFragmentByTag("mushaf");
            addFragment          = (AddFragment)          getSupportFragmentManager().findFragmentByTag("add");
            importExportFragment = (ImportExportFragment) getSupportFragmentManager().findFragmentByTag("data");
        }
        activeFragment = browseFragment;
    }

    private void setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if      (id == R.id.nav_browse) showFragment(browseFragment);
            else if (id == R.id.nav_mushaf) showFragment(mushafFragment);
            else if (id == R.id.nav_add)    showFragment(addFragment);
            else if (id == R.id.nav_data)   showFragment(importExportFragment);
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

    public void openAyahsScreen(int surahNumber, String surahName) {
        Fragment f = com.cloudy.quranbuilder.ui.browse.AyahsFragment
                .newInstance(surahNumber, surahName);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left, android.R.anim.fade_out,
                    android.R.anim.fade_in,       android.R.anim.slide_out_right
                )
                .replace(R.id.fragmentContainer, f)
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
