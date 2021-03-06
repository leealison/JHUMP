package com.example.jhump;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AllListings factory method to
 * create an instance of this fragment.
 */
public class AllListings extends Fragment {

    private ListView listingList;
    private Context context;
    private FirebaseDatabase db;
    private DatabaseReference dbref;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private Button filtersButton;
    private FloatingActionButton fab;

    public AllListings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final String TAG = "itemList";
//        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
//        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        View view =  inflater.inflate(R.layout.fragment_all_listings, container, false);
        db = FirebaseDatabase.getInstance();
        dbref = db.getReference().child("listings");
        listingList = (ListView)view.findViewById(R.id.all_listings_list);

        NavigationDrawer.aa = new ItemAdapter(getActivity(),R.layout.listing_item_layout, NavigationDrawer.listingItem);
        listingList.setAdapter(NavigationDrawer.aa);
        registerForContextMenu(listingList);

        if (NavigationDrawer.fromFilters) {
            Log.d("here", "got here");
            ArrayList<Item> temp = new ArrayList<Item>();
            for (int i = 0; i < Filters.list.size(); i++) {
                if (Filters.list.get(i).isSold()) {
                    temp.add(Filters.list.get(i));
                    Filters.list.remove(i);
                }
            }
            Filters.list.addAll(temp);
            NavigationDrawer.listingItem.clear();
            NavigationDrawer.listingItem.addAll(Filters.list);
            NavigationDrawer.aa.notifyDataSetChanged();
        }
        else {
            dbref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    NavigationDrawer.listingItem.clear();
                    ArrayList<Item> temp = new ArrayList<Item>();
                    ArrayList<Item> sold_items = new ArrayList<Item>();
                    for (DataSnapshot pair : dataSnapshot.getChildren()) {
                        temp.add(pair.getValue(Item.class));
                    }
                    for (int i = temp.size() - 1; i >= 0; i--) {
                        if (!temp.get(i).isSold()) {
                            NavigationDrawer.listingItem.add(temp.get(i));
                        } else {
                            sold_items.add(temp.get(i));
                        }
                    }
                    NavigationDrawer.listingItem.addAll(sold_items);
                    NavigationDrawer.aa.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "Failed to read value.", databaseError.toException());
                }
            });
        }

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new CreateListings());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        filtersButton = view.findViewById(R.id.filters_button);
        filtersButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new Filters());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        // program a short click on the list item
        listingList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ItemDescription.class);
                Item item = (Item) parent.getItemAtPosition(position);
                //Item item = NavigationDrawer.listingItem.get(position);
                intent.putExtra("listing", item.getName());
                intent.putExtra("seller", item.getSeller());
                intent.putExtra("category", item.getCategory());
                intent.putExtra("condition", item.getCondition());
                intent.putExtra("description", item.getDescription());
                intent.putExtra("price", item.getPrice());
                intent.putExtra("sold", item.isSold());
                intent.putExtra("sellerID", item.getSellerID());
                intent.putExtra("picture", item.getPicture());

                String filename = "bitmap.jpg";
                FileOutputStream stream = null;
                try {
                    stream = getActivity().openFileOutput(filename, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                //ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//                item.getPicture().get(0).compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                try {
//                    stream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                //item.getPicture().get(0).recycle();
                intent.putExtra("pics", filename /*bStream.toByteArray()*/);

                startActivity(intent);
            }
        });
        return view;
    }

    private String saveToInternalStorage(Bitmap picture, int i) {
        ContextWrapper cw = new ContextWrapper(getActivity());
        File dir = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath = new File(dir, "image" + i + ".jpg");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            picture.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (Exception e){
                    e.printStackTrace();
            }
        }
        return dir.getAbsolutePath();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.options_menu_with_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);


        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.i("onQueryTextChange", newText);
                    final String entry = newText;

                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ArrayList<Item> newList = new ArrayList<Item>();

                            // If nothing put in search, reset to show all listings
                            if (entry.isEmpty()) {
                                NavigationDrawer.listingItem.clear();
                                for(DataSnapshot pair: dataSnapshot.getChildren()) {
                                    newList.add(pair.getValue(Item.class));
                                }
                                NavigationDrawer.listingItem.addAll(newList);
                                NavigationDrawer.aa.notifyDataSetChanged();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("Failed to read value.", databaseError.toException());
                        }
                    });

                    return true;
                }

                //start searching after user submits their search key word(s)
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.i("onQueryTextSubmit", query);
                    final String entry = query;

                    dbref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            NavigationDrawer.listingItem.clear();
                            ArrayList<Item> newList = new ArrayList<Item>();

                            String[] tokens = entry.split(" ");

                            for(DataSnapshot pair: dataSnapshot.getChildren()) {
                                //NavigationDrawer.listingItem.add(pair.getValue(Item.class));
                                for (DataSnapshot pair2: pair.getChildren()) {
                                    for (String token: tokens) {
                                        if (pair2.getValue() != null && pair2.getValue().equals(token)) {
                                            newList.add(pair.getValue(Item.class));
                                        }
                                    }
                                }
                            }

                            NavigationDrawer.listingItem.addAll(newList);
                            NavigationDrawer.aa.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.w("Failed to read value.", databaseError.toException());
                        }
                    });
                    return true;
                }
            };
            searchView.setOnQueryTextListener(queryTextListener);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    /*
    @Override
    public void setOnQueryTextListener(SearchView.OnQueryTextListener listener) {
        super.setOnQueryTextListener(listener);
        this.listener = listener;
        mSearchSrcTextView = this.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        mSearchSrcTextView.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (listener != null) {
                listener.onQueryTextSubmit(getQuery().toString());
            }
            return true;
        });
    }

     */

}
