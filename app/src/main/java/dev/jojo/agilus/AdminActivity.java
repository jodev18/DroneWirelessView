package dev.jojo.agilus;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import java.util.ArrayList;

import devlight.io.library.ntb.NavigationTabBar;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_admin);

        final String[] colors = getResources().getStringArray(R.array.vertical_ntb);

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_vertical);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(android.R.drawable.ic_menu_myplaces),
                        Color.parseColor(colors[0]))
                        .title("ic_first")
                        .selectedIcon(getResources().getDrawable(android.R.drawable.ic_menu_myplaces))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(android.R.drawable.ic_dialog_map),
                        Color.parseColor(colors[1]))
                        .selectedIcon(getResources().getDrawable(android.R.drawable.ic_dialog_map))
                        .badgeTitle("HEY")
                        .title("ic_second")
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(android.R.drawable.ic_menu_info_details),
                        Color.parseColor(colors[2]))
                        .selectedIcon(getResources().getDrawable(android.R.drawable.ic_menu_info_details))
                        .title("ic_third")
                        .build()
        );
        navigationTabBar.setModels(models);
        navigationTabBar.setIsBadged(true);
        //navigationTabBar.setViewPager(viewPager, 4);

    }
}
