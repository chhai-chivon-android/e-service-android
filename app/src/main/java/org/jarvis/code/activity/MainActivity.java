package org.jarvis.code.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jarvis.code.R;
import org.jarvis.code.api.RequestClient;
import org.jarvis.code.core.adapter.FragmentAdapter;
import org.jarvis.code.core.fragment.IFragment;
import org.jarvis.code.core.fragment.ProductFragment;
import org.jarvis.code.core.model.read.Advertisement;
import org.jarvis.code.core.model.read.ResponseEntity;
import org.jarvis.code.util.AnimateAD;
import org.jarvis.code.util.Constant;
import org.jarvis.code.util.Loggy;
import org.jarvis.code.util.RequestFactory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback, Callback<ResponseEntity<Advertisement>> {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentAdapter viewPagerAdapter;
    private ImageView imageAd;
    private SearchView searchView;
    private RequestClient requestClient;
    public static List<Integer> advertisements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        requestClient = RequestFactory.build(RequestClient.class);
        requestClient.fetchAdvertisement().enqueue(this);
        checkRunTimePermission();
        FirebaseMessaging.getInstance().subscribeToTopic("Testing");
        FirebaseInstanceId.getInstance().getToken();
    }

    private void init() {

        viewPagerAdapter = new FragmentAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(ProductFragment.newInstance("WED", this), getResources().getString(R.string.wedding_fragment));
        viewPagerAdapter.addFragment(ProductFragment.newInstance("CER", this), getResources().getString(R.string.ceremony_fragment));
        viewPagerAdapter.addFragment(ProductFragment.newInstance("DES", this), getResources().getString(R.string.design_fragment));

        viewPager = (ViewPager) findViewById(R.id.tabPager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        imageAd = (ImageView) findViewById(R.id.imgAd);

        searchView = (SearchView) findViewById(R.id.search_view);
        searchView.setQueryHint(getResources().getString(R.string.string_search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ((IFragment) viewPagerAdapter.getItem(viewPager.getCurrentItem())).search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ((IFragment) viewPagerAdapter.getItem(viewPager.getCurrentItem())).search(newText);
                return true;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setIconified(false);
            }
        });
    }

    private void checkRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    Toast.makeText(this, "Write External Storage permission allows us to do store advertisements. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
                else
                    ActivityCompat.requestPermissions(this, Constant.MY_PERMISSIONS, Constant.REQUEST_PERMISSIONS_CODE);
            }
        }
    }

    public void onRefreshAD() {
        requestClient.fetchAdvertisement().enqueue(this);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
            fragmentManager.beginTransaction().commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constant.REQUEST_PERMISSIONS_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Loggy.e(MainActivity.class, "Permission Granted, Now you can use local drive .");
                else
                    Loggy.e(MainActivity.class, "Permission Denied, You cannot use local drive .");
                break;
        }
    }

    @Override
    public void onResponse(Call<ResponseEntity<Advertisement>> call, Response<ResponseEntity<Advertisement>> response) {
        if (response.code() == 200) {
            ResponseEntity<Advertisement> responseEntity = response.body();
            if (responseEntity.getData() != null && !responseEntity.getData().isEmpty()) {
                advertisements.clear();
                for (Advertisement advertisement : responseEntity.getData()) {
                    advertisements.add(advertisement.getImage());
                }
                AnimateAD.animate(imageAd, advertisements, 0, true, this);
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseEntity<Advertisement>> call, Throwable t) {
        Loggy.i(AnimateAD.class, t.getMessage());
        Toast.makeText(this, t.getMessage(), Toast.LENGTH_LONG).show();
    }

}
