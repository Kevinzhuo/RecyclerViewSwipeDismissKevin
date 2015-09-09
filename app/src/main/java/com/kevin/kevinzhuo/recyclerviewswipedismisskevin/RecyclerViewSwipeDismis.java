package com.kevin.kevinzhuo.recyclerviewswipedismisskevin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.kevin.kevinzhuo.mylibrary.SwipeDismissRecyclerView;

import java.util.LinkedList;
import java.util.List;

public class RecyclerViewSwipeDismis extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view_swipe_dismis);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        final RecyclerView anotherRecyclerView = (RecyclerView) findViewById(R.id.recyclerHorizontalView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this);
        horizontalLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        recyclerView.setLayoutManager(layoutManager);
        anotherRecyclerView.setLayoutManager(horizontalLayoutManager);

        List<String> dataset = new LinkedList<String>();
        for (int i = 0; i < 50; i++) {
            dataset.add("item " + i);
        }
        final MyAdapter adapter = new MyAdapter(dataset);

        recyclerView.setAdapter(adapter);
        anotherRecyclerView.setAdapter(adapter);

        SwipeDismissRecyclerViewTouch
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recycler_view_swipe_dismis, menu);
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
}
