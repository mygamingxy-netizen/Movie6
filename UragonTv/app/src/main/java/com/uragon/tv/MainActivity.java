package com.uragon.tv;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabs;
    private ViewPager2 viewPager;
    private EditText searchInput;
    private ImageView searchBtn;
    private ProgressBar loading;
    private TextView emptyText;

    private final String[] tabTitles = {"Live TV", "Movies", "Series", "Favorites"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabs = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.contentFrame);
        searchInput = findViewById(R.id.searchInput);
        searchBtn = findViewById(R.id.searchBtn);
        loading = findViewById(R.id.loading);
        emptyText = findViewById(R.id.emptyText);

        // Setup ViewPager with fragments
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(ContentFragment.newInstance(ContentFragment.TYPE_LIVE));
        fragments.add(ContentFragment.newInstance(ContentFragment.TYPE_MOVIES));
        fragments.add(ContentFragment.newInstance(ContentFragment.TYPE_SERIES));
        fragments.add(ContentFragment.newInstance(ContentFragment.TYPE_FAVORITES));

        ContentPagerAdapter adapter = new ContentPagerAdapter(this, fragments);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        new TabLayoutMediator(tabs, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();

        // Search
        searchBtn.setOnClickListener(v -> doSearch());
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearch();
                return true;
            }
            return false;
        });
    }

    private void doSearch() {
        String q = searchInput.getText().toString().trim();
        if (TextUtils.isEmpty(q)) return;
        Intent intent = new Intent(this, SearchActivity.class);
        intent.putExtra("query", q);
        startActivity(intent);
    }

    public void showLoading(boolean show) {
        loading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showEmpty(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
