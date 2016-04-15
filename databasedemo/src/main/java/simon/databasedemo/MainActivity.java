package simon.databasedemo;

import android.support.v7.app.ActionBarDrawerToggle;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.Parse;


public class MainActivity extends ActionBarActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "MnmWGFhvJZRlTa2p3JkttUiJAgixb1K8YCLAswTf", "V6B7NG3X97COT37GV8959laZU7FUP8HD3oVxfxu7");


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        initDrawerToggle();

        String[] screens = {"2016年2月", "2016年1月", "2015年12月", "2015年11月",
                "2015年10月"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, screens);
        ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (GooglePlayServicesErrorDialogFragment.showDialogIfNotAvailable(MainActivity.this)) {
                    startExample(position);
                }
            }
        });
        if (savedInstanceState == null) {
            if (GooglePlayServicesErrorDialogFragment.showDialogIfNotAvailable(this)) {
                replaceMainFragment(new DemoFragment(0));

            }
        }
    }

    private void startExample(int position) {
        DemoFragment demo = new DemoFragment(position);
        replaceMainFragment(demo);
        drawerLayout.closeDrawers();
    }

    private void initDrawerToggle() {
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.setDrawerListener(toggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    private void replaceMainFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        tx.replace(R.id.main_container, fragment);
        tx.commit();
    }
}
