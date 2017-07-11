package com.kaiamelung.debrief;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DayFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static final String ARG_OBJECT = "object";
    public static final String ARG_OBJECT2 = "objecta";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ArrayList<Tag> tags;
    private RecyclerView mTag;
    private TagAdapter adapter;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private DateFormat mDateFormat = new SimpleDateFormat("M-d");
    private Date date = new Date();
    private java.util.Calendar cal = java.util.Calendar.getInstance();
    private SharedPreferences sharedPref;

    private Gson gson;
    private boolean save_articles;

    private OnFragmentInteractionListener mListener;

    public DayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DayFragment newInstance(String param1, String param2) {
        DayFragment fragment = new DayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void fetchData(){

        tags.clear();
        adapter.notifyDataSetChanged();
        final ArrayList<Tag> temptags = new ArrayList<Tag>();
        int a =0;
        while(true){
            String name = sharedPref.getString("" + a, "none");
            String color= sharedPref.getString("" + a + "c", "none");
            if(name.equals("none") || color.equals("none")){
                break;
            }
            else{
                temptags.add(new Tag(name, null, color));
            }
            a++;
        }

        for(final Tag b : temptags) {
            String name = b.getTag();
            if(name.equals("tech")){
                name = "technology";
            }
            else if(name.equals("tv")){
                name = "entertainment";
            }
            final String tagname = name;

            ArrayList<Article> temp = readFile(mDateFormat.format(date), tagname);
            if(temp != null){
                if(temp.size() != 0){
                    b.setArticle(temp);
                    tags.add(b);
                    adapter.notifyDataSetChanged();
                }
            }
            else{
                DatabaseReference myRef = database.getReference("debriefings/"+mDateFormat.format(date)+"/"+name);
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ArrayList<Article> temp = new ArrayList<Article>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ThreadGroup art = snapshot.getValue(ThreadGroup.class);
                            Article art2 = new Article(art.title, art.shortsum, art.longsum, art.url, b.getColor());
                            temp.add(art2);
                        }
                        if(temp.size() != 0){
                            b.setArticle(temp);
                            tags.add(b);
                        }
                        if(save_articles == true) {
                            saveFile(mDateFormat.format(date), tagname, temp);
                        }
                        adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("ERROR");
                    }
                });
            }
        }
    }

    private void saveFile(String date, String tag, ArrayList<Article> articleList){
        String filename = date+" "+tag;
        String string = gson.toJson(articleList);

        FileOutputStream outputStream;
        try {
            outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(string.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Article> readFile(String date, String tag){
        String filename = date+" "+tag;

        File file = new File(getContext().getFilesDir(), filename);
        int length = (int) file.length();

        byte[] bytes = new byte[length];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(bytes);
            String contents = new String(bytes);
            Type type = new TypeToken<ArrayList<Article>>() {}.getType();
            return gson.fromJson(contents, type);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        // Lookup the recyclerview in activity layout
        sharedPref = this.getActivity().getSharedPreferences(getString(R.string.saved_threads),Context.MODE_PRIVATE);
        save_articles = sharedPref.getBoolean(getString(R.string.save_articles), true);
        gson = new Gson();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        int daysBack = getArguments().getInt(ARG_OBJECT);
        int count = getArguments().getInt(ARG_OBJECT2);
        Calendar cal = Calendar.getInstance();
        date = new Date();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR,(-1*(count-daysBack+1)));
        date = cal.getTime();

        View v = inflater.inflate(R.layout.fragment_day, container, false);
        DateFormat dateFormat = new SimpleDateFormat("MMMM d");
        ((TextView)v.findViewById(R.id.date)).setText(dateFormat.format(date));

        mTag = (RecyclerView) v.findViewById(R.id.tag_recyc_view);
        tags = new ArrayList<Tag>();

//        tags.add(new Tag("test",null, "#FFFFFF"));

        // Create adapter passing in the sample user data
        adapter = new TagAdapter(this.getContext(), tags);

        // Attach the adapter to the recyclerview to populate items
        mTag.setAdapter(adapter);
        // Set layout manager to position the items
        mTag.setLayoutManager(new LinearLayoutManager(this.getContext()));
        fetchData();
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
