package com.kevin.kevinzhuo.recyclerviewswipedismisskevin;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.kevin.kevinzhuo.mylibrary.SwipeDismissRecyclerView;

import java.util.LinkedList;
import java.util.List;

public class RecyclerViewSwipeDismis extends Activity {

    private void showDialog(String msg) {
        AlertDialog alert = new AlertDialog.Builder(RecyclerViewSwipeDismis.this).setTitle("alert").setMessage(msg).setCancelable(false).create();
        alert.setCanceledOnTouchOutside(true);
        alert.show();
    }

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

        SwipeDismissRecyclerView listener = new SwipeDismissRecyclerView.Builder(recyclerView, new SwipeDismissRecyclerView.DismissCallbacls() {

            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(View view) {
                int id = recyclerView.getChildPosition(view);
                adapter.mDataset.remove(id);
                adapter.notifyDataSetChanged();

                Toast.makeText(getBaseContext(), String.format("Delete item %d", id), Toast.LENGTH_LONG).show();
            }
        }).setIsVertical(false).setItemTouchCallback(new SwipeDismissRecyclerView.OnItemTouchCallBack() {
            @Override
            public void onTouch(int index) {
                showDialog(String.format("Click item %d", index));
            }
        }).create();

        recyclerView.setOnTouchListener(listener);

        SwipeDismissRecyclerView verticalListener = new SwipeDismissRecyclerView.Builder(anotherRecyclerView, new SwipeDismissRecyclerView.DismissCallbacls() {
            @Override
            public boolean canDismiss(int position) {
                return true;
            }

            @Override
            public void onDismiss(View view) {
                int id = recyclerView.getChildPosition(view);
                adapter.mDataset.remove(id);
                adapter.notifyDataSetChanged();

                Toast.makeText(getBaseContext(), String.format("Delete item %d", id), Toast.LENGTH_LONG).show();
            }
        }).setIsVertical(true).create();

        anotherRecyclerView.setOnTouchListener(verticalListener);
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        public List<String> mDataset;

        public MyAdapter(List<String> dataset) {
            super();
            mDataset = dataset;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), android.R.layout.simple_list_item_1, null);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTextView.setText(mDataset.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView mTextView;

            public ViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView;
            }
        }
    }
}
