package com.kaiamelung.debrief;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.gesture.GestureOverlayView;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FeedActivity extends AppCompatActivity implements /*GestureDetector.OnGestureListener,*/ DayFragment.OnFragmentInteractionListener {

    private ArrayList<Tag> tags;
    private RecyclerView mTag;
    private TagAdapter adapter;

    private TextView mDate;
    private SharedPreferences sharedPref;

    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyMgr;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DateFormat mDateFormat = new SimpleDateFormat("M-d");
    private Date date = new Date();
    private Calendar cal = Calendar.getInstance();


    private GestureDetector detector;

    DayCollectionPagerAdapter mDayCollectionPagerAdapter;
    ViewPager mViewPager;

    private void fetchData(){
        tags.clear();
        adapter.notifyDataSetChanged();
        final ArrayList<Tag> temptags = new ArrayList<Tag>();
//        adapter.notifyDataSetChanged();
        int a =0;
        System.out.println("BEGIN");
        while(true){
            String name = sharedPref.getString("" + a, "none");
            String color= sharedPref.getString("" + a + "c", "none");
            if(name.equals("none") || color.equals("none")){
                break;
            }
            else{
                if(name.equals("tech")){
                    name = "technology";
                }
                else if(name.equals("tv")){
                    name = "entertainment";
                }
                temptags.add(new Tag(name, null, color));
                System.out.println(name);
            }
            a++;
        }

        DatabaseReference myRef = database.getReference("debriefings/"+mDateFormat.format(date));

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println("TAGS FOUND");

                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    System.out.println(test);
                }

                for(Tag b : temptags) {
                    ArrayList<Article> temp = new ArrayList<Article>();
                    System.out.println(b.getTag());
                    System.out.println(dataSnapshot.child(b.getTag()));
                    System.out.println(dataSnapshot.child(b.getTag()).getValue());
                    for (DataSnapshot snapshot : dataSnapshot.child(b.getTag()).getChildren()) {
                        ThreadGroup art = snapshot.getValue(ThreadGroup.class);
                        Article art2 = new Article(art.title, art.shortsum, art.longsum, art.url, b.getColor());
                        temp.add(art2);
                    }
                    if(temp.size() != 0){
                        b.setArticle(temp);

                        System.out.println(temp);
                        tags.add(b);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("ERROR");
            }
        });
    }
    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                //fetchData();
                mDayCollectionPagerAdapter =
                        new DayCollectionPagerAdapter(
                                getSupportFragmentManager());
                mViewPager = (ViewPager) findViewById(R.id.pager);
                mViewPager.setAdapter(mDayCollectionPagerAdapter);
                mViewPager.setCurrentItem(mDayCollectionPagerAdapter.getCount()-1);
                System.out.println("OK VERY GOOD AWESOME");
            }
        }
    }

   /* @Override
    public boolean onTouchEvent(MotionEvent event) {
        return detector.onTouchEvent(event);
    }
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        if( Math.abs(velocityX)> Math.abs(2*velocityY)){
            if(velocityX > 0){
                cal.add(Calendar.DAY_OF_YEAR,-1);
            }
            else if (velocityX < 0){
                cal.add(Calendar.DAY_OF_YEAR,+1);
            }
            date = cal.getTime();
            mDate.setText(mDateFormat.format(date));
            fetchData();
        }
        System.out.println("X:"+velocityX);
        System.out.println("Y:"+velocityY);
        return true;
    }
    @Override
    public void onLongPress(MotionEvent e) {
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return false;
    }
    @Override
    public void onShowPress(MotionEvent e) {
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_feed);

        /*cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR,-1);
        date = cal.getTime();

        detector=new GestureDetector(getApplicationContext(), this);

        mDate = (TextView) findViewById(R.id.date);
        mDate.setText(mDateFormat.format(date));


        // Lookup the recyclerview in activity layout
        mTag = (RecyclerView) findViewById(R.id.tag_list_view);
        tags = new ArrayList<Tag>();

//        tags.add(new Tag("test",null, "#FFFFFF"));

        // Create adapter passing in the sample user data
        adapter = new TagAdapter(this, tags);

        // Attach the adapter to the recyclerview to populate items
        mTag.setAdapter(adapter);
        // Set layout manager to position the items
        mTag.setLayoutManager(new LinearLayoutManager(this));*/

        sharedPref = this.getSharedPreferences(getString(R.string.saved_threads),Context.MODE_PRIVATE);
        int tagNumber = sharedPref.getInt(getString(R.string.saved_tag_num), 0);
        if(tagNumber==0){
            //launch chooser
            Intent intent = new Intent(this, ChooseTagActivity.class);
            startActivityForResult(intent, 1);
        }
        else{
            mDayCollectionPagerAdapter =
                    new DayCollectionPagerAdapter(
                            getSupportFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mDayCollectionPagerAdapter);
            mViewPager.setCurrentItem(mDayCollectionPagerAdapter.getCount()-1);
        }
    }
}