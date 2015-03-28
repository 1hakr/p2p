package com.p2p;

import android.content.Context;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.cache.SimpleImageLoader;
import com.android.volley.error.VolleyError;
import com.android.volley.request.GsonRequest;
import com.p2p.entity.AllMenu;
import com.p2p.entity.PlaceDetails;
import com.p2p.entity.Places;
import com.p2p.entity.Profile;
import com.p2p.misc.DeviceUuidFactory;
import com.p2p.misc.Utils;
import com.p2p.ui.HorizontalListView;

import java.util.ArrayList;
import java.util.List;


public class PzoneDetails extends ActionBarActivity {

    private Places.Place place;
    private ImageView place_picture;
    private TextView place_name;
    private HorizontalListView checkins;
    private HorizontalListView royals;
    private SimpleImageLoader mImageFetcher;
    private CheckinAdapter mAdapter;
    private CheckinAdapter mAdapter2;
    private View checkins_layout;
    private View royals_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pzone_details);
        place = (Places.Place) getIntent().getExtras().getSerializable(Utils.BUNDLE_RESTAURANT);

        initCache();
        initControls();
        getPlace();
    }

    private void initCache() {
        mImageFetcher = P2PApplication.getInstance().getImageLoader();
        mImageFetcher.startProcessingQueue();
    }


    private void initControls() {
        place_picture = (ImageView) findViewById(R.id.place_picture);
        place_name = (TextView) findViewById(R.id.place_name);
        checkins = (HorizontalListView) findViewById(R.id.checkins);
        royals = (HorizontalListView) findViewById(R.id.royals);

        checkins_layout = findViewById(R.id.checkins_layout);
        royals_layout = findViewById(R.id.royals_layout);

        mAdapter = new CheckinAdapter(this, R.layout.fragment_menu);
        checkins.setAdapter(mAdapter);
        checkins.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Bundle bundle = new Bundle();
                bundle.putSerializable(Utils.BUNDLE_MENU, mAdapter.getItem(position));
                Intent intent = new Intent(PzoneDetails.this, PFile.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mAdapter2 = new CheckinAdapter(this, R.layout.fragment_menu);
        royals.setAdapter(mAdapter2);

        royals.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putSerializable(Utils.BUNDLE_MENU, mAdapter.getItem(position));
                Intent intent = new Intent(PzoneDetails.this, PFile.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_pzone_details, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getPlace(){
        final ArrayMap<String, String> param = new ArrayMap<String, String>();
        param.put("id", place.id);

        GsonRequest<PlaceDetails> request = new GsonRequest<PlaceDetails>(Request.Method.GET,
                Utils.API_URL + "/place",
                PlaceDetails.class,
                null,
                param,
                new Response.Listener<PlaceDetails>() {
                    @Override
                    public void onResponse(PlaceDetails place) {
                        showProfileDetails(place);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        P2PApplication.getInstance().addToRequestQueue(request);
    }

    public void showProfileDetails(PlaceDetails place){
        P2PApplication.getInstance().getBgLoader().get(place.photo_url, place_picture);
        place_name.setText(place.name);

        if(place.checkins.isEmpty()){
            checkins_layout.setVisibility(View.GONE);
        } else{
            mAdapter.setData(place.checkins, true);
        }

        if(place.royals.isEmpty()){
            royals_layout.setVisibility(View.GONE);
        } else{
            mAdapter2.setData(place.royals, true);

        }

    }


    private class CheckinAdapter extends ArrayAdapter<Profile.Checkin> {
        private LayoutInflater mInflater;

        public CheckinAdapter(Context context, int resource) {
            super(context, resource);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<Profile.Checkin> data, boolean add) {
            if (!add) {
                clear();
            }
            if (data != null) {
                for (Profile.Checkin relations : data) {
                    add(relations);
                }
            }
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView info_child_text;
            ImageView icon_thumb;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_menu, parent, false);
            }

            info_child_text = ViewHolder.get(convertView, android.R.id.title);
            icon_thumb = ViewHolder.get(convertView, R.id.icon_thumb);

            Profile.Checkin menuItem = getItem(position);
            info_child_text.setText(Utils.capitalizeFirstLetter(menuItem.name));

            if(null != menuItem.photo_url)
            mImageFetcher.get(menuItem.photo_url, icon_thumb);

            return convertView;
        }
    }

    public static class ViewHolder {
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {
            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }
            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }
            return (T) childView;
        }
    }
}
