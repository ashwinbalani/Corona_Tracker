package com.fitech.coronatracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OneReportDisplayListAdapter extends ArrayAdapter<OneReport>
{

    Context context;
    List<OneReport> objects;
    Activity activity;

    //Controls from the view
    public OneReportDisplayListAdapter(@NonNull Context context, int resource, @NonNull List<OneReport> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;

    }

    public OneReportDisplayListAdapter(@NonNull Activity activity, @NonNull Context context, int resource, @NonNull List<OneReport> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
        this.activity = activity;
    }

    @Override
    public void add(@Nullable OneReport object) {
        for(OneReport obj:objects)
        {
            if(obj.getId() == (object.getId()))
                return;
        }

        super.add(object);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ResizableImageView mItemImg;
        final TextView mItemTimeAgo;
        final ProgressBar mProgressBar;
        final MaterialRippleLayout mItemRepostButton;
        final TextView eventTitle;

        LayoutInflater layoutInf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = layoutInf.inflate(R.layout.item_adapter_view, parent,false);

        final OneReport p = objects.get(position);
        mItemImg = (ResizableImageView) v.findViewById(R.id.itemImg);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mItemRepostButton = (MaterialRippleLayout) v.findViewById(R.id.itemRepostButton);
        eventTitle = (TextView) v.findViewById(R.id.eventTitle);
        String text = p.newsLocation + "\n" + p.newsDate;
        if(p.getDistance() <= 1) {
            text = text + "\nVery near You!";
        }
        else {
            text = text + "\n" + p.distance + " kms away";
        }
        eventTitle.setText(text);

        mProgressBar.setVisibility(View.GONE);

        if (p.getImage().length() != 0){

            mItemImg.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);

            final ProgressBar progressView = mProgressBar;
            final ImageView imageView = mItemImg;

            Picasso.with(context)
                    .load(p.getImage())
                    .transform(new RoundedTransformation(10, 1))
                    .into(mItemImg, new Callback() {

                        @Override
                        public void onSuccess() {
                            progressView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            progressView.setVisibility(View.GONE);
                            imageView.setVisibility(View.GONE);
                            //imageView.setImageResource(R.drawable.img_loading_error);
                        }
                    });

        }
        else {
            mItemImg.setVisibility(View.GONE);
        }

        mItemRepostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                shareItem(p,view); //view is sent for snackbar
            }
        });

        return v;
    }

    public static OneReportDisplayListAdapter thisAdapter;
    public static OneReport thisNewsItem;
    public static View thisView;
    public void shareItem(final OneReport item, final View v)
    {
        //Check for Permission before sharing
        thisAdapter = this;
        thisNewsItem = item;
        thisView = v;

        AsyncTask<String, String, Void> ataskShare = new AsyncTask<String, String, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                DisplayHelper.showSnackbar(activity,"Preparing image for sharing");
            }

            @Override
            protected Void doInBackground(String... strings) {
                try {
                    String url1 = item.image;
                    URL ulrn = new URL(url1);
                    HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
                    InputStream is = con.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/Share.png";
                    OutputStream out = null;
                    File file=new File(path);
                    out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.flush();
                    out.close();

                    path=file.getPath();

                    publishProgress(path);
                } catch(Exception ex) {
                    publishProgress("");
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);
                String path = values[0].trim();

                Intent shareIntent;
                shareIntent = new Intent(android.content.Intent.ACTION_SEND);
                if(path.equals("")) {
                    //Image not loaded
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, item.getNewsLocation() + " " + item.getNewsDate());
                } else {
                    //Image is loaded
                    File pathFile = new File(path);
                    Uri bmpUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider",pathFile);
                    //Uri bmpUri = Uri.parse("file://"+path);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    shareIntent.setType("image/png");
                }
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                shareIntent.putExtra(Intent.EXTRA_TEXT,"The person in the photo was not following lockdown\n" + item.getNewsLocation() + "\n On " + item.getNewsDate() + "\n\nReport people who are not following lock-down\n" + activity.getString(R.string.app_site_for_news_reading));
                activity.startActivity(Intent.createChooser(shareIntent,"Share with"));
            }
        };
        ataskShare.execute("");
    }

    @Override
    public int getCount() {
        return objects.size();
    }
}
