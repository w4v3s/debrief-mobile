package com.kaiamelung.debrief;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DayAdapter extends
        RecyclerView.Adapter<DayAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView mDate;
        public ArrayList<Article> mArticles;
        public RecyclerView mArticleView;
        public ArticleAdapter mArticleAdapter;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(final View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            mDate = (TextView) itemView.findViewById(R.id.date);
            mArticleView = (RecyclerView) itemView.findViewById(R.id.article_view);
        }
    }

    // Store a member variable for the contacts
    private List<Day> mDays;
    private List<Article> articles;

    // Store the context for easy access
    private Context mContext;

    // Pass in the contact array into the constructor
    public DayAdapter(Context context, List<Day> contacts) {
        mDays = contacts;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public DayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.day_layout, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(DayAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Day day = mDays.get(position);

        // Set item views based on your views and data model
        TextView date = viewHolder.mDate;
        RecyclerView mArticles = viewHolder.mArticleView;
        ArticleAdapter adapter = viewHolder.mArticleAdapter;

        date.setText(day.getDate());
        adapter = new ArticleAdapter(this, articles);
        // Attach the adapter to the recyclerview to populate items
        mArticles.setAdapter(adapter);
        // Set layout manager to position the items
        mArticles.setLayoutManager(new LinearLayoutManager(this));
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mDays.size();
    }
}