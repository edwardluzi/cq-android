package org.goldenroute.cq;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.goldenroute.cq.model.TestModel;

public class MainActivity extends AppCompatActivity {

    private static final String POSITION = "position";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                MainActivity.this.setTitle(getString(R.string.app_name) + " - " + mSectionsPagerAdapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        int currentPage = settings.getInt(POSITION, 0);

        if (currentPage >= mSectionsPagerAdapter.getCount()) {
            currentPage = 0;
        }

        mViewPager.setCurrentItem(currentPage);
        MainActivity.this.setTitle(getString(R.string.app_name) + " - " + mSectionsPagerAdapter.getPageTitle(currentPage));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {

            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            MainActivity.this.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(POSITION, mViewPager.getCurrentItem());
        editor.apply();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return MainFragment.newInstance(TestModel.Review);
            } else {
                return MainFragment.newInstance(TestModel.Test);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Review";
                case 1:
                    return "Test";
            }
            return null;
        }
    }
}
