package com.yugiri.pengcit;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity
		implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	private Toolbar toolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment)
				getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		switch (position){
			case 0: fragmentManager.beginTransaction()
						.replace(R.id.container, GreyScaleFragment.newInstance())
						.commit();
					break;
			case 1: fragmentManager.beginTransaction()
						.replace(R.id.container, PlatNomerFragment.newInstance())
						.commit();
					break;
			case 2: fragmentManager.beginTransaction()
						.replace(R.id.container, CharacterGridFragment.newInstance())
						.commit();
					break;
			case 3: fragmentManager.beginTransaction()
						.replace(R.id.container, ThinningFragment.newInstance())
						.commit();
					break;
			case 4: fragmentManager.beginTransaction()
						.replace(R.id.container, FaceRecognizerFragment.newInstance())
						.commit();
					break;
			case 5: fragmentManager.beginTransaction()
					.replace(R.id.container, SkinDetectorFragment.newInstance())
					.commit();
				break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();


		return super.onOptionsItemSelected(item);
	}

}
