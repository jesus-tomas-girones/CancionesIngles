package com.sacedonmg.cancionesingles;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.sacedonmg.cancionesingles.MainActivity.SECCION_DESCARGADAS;
import static com.sacedonmg.cancionesingles.MainActivity.SECCION_REMOTAS;

public class TabbedActivity extends Fragment implements ViewPager.OnPageChangeListener{
    private String LOG_TAG = "CI::TabbedActivity";

    private SectionsPagerAdapter mSectionsPagerAdapter;
        private final int SECTIONS[] = {
            R.string.section_downloaded,
            R.string.section_availables,
    };

    private static ViewPager mViewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.activity_tabbed, container, false);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        mViewPager = (ViewPager) rootView.findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(this);

        TabLayout tabLayout = MainActivity.getTabLayout();
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(mViewPager);

        return rootView;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        Log.d(LOG_TAG, "onPageSelected " + position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case SECCION_DESCARGADAS:
                    return ListaCanciones.newInstance();
                case SECCION_REMOTAS:
                    return ListaCancionesRemoto.newInstance();
                default:
                    return ListaCanciones.newInstance();
            }
        }

        @Override
        public int getCount() {
            return SECTIONS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position < SECTIONS.length) {
                return getString(SECTIONS[position]);
            }

            return null;
        }
    }

    public static ViewPager getViewPager() {
        return mViewPager;
    }
}
