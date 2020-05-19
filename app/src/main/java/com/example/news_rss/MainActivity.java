package com.example.news_rss;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView lv;
    ArrayList<List_item> ListItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = findViewById(R.id.Lv);

        ListItem = new ArrayList<>();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Uri uri = Uri.parse(ListItem.get(i).link);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        new ProcessInBackground().execute();

    }

    public InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }

    }

    @SuppressLint("StaticFieldLeak")
    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception> {

        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Exception doInBackground(Integer... integers) {

            try {

                URL xmlurl = new URL("https://www.aljazeera.net/aljazeerarss/a7c186be-1baa-4bd4-9d80-a84db769f779/73d0e1b4-532f-45ef-b135-bfdff8b8cab9");//الجزيرة
                //URL xmlurl = new URL("https://www.alarabiya.net/.mrss/ar/aswaq/financial-markets.xml");//العربية
                //URL xmlurl = new URL("https://www.albayan.ae/1.451?ot=ot.AjaxPageLayout");//البيان



                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(xmlurl), "UTF_8");

                boolean GetItem = false;

                ArrayList<String> title = new ArrayList<>();
                ArrayList<String> link = new ArrayList<>();
                ArrayList<String> category = new ArrayList<>();
                ArrayList<String> description = new ArrayList<>();
                ArrayList<String> link_img = new ArrayList<>();

                int eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {

                        if (xpp.getName().equalsIgnoreCase("item")) {
                            GetItem = true;
                        } else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (GetItem) {
                                title.add(xpp.nextText());
                            }
                        }
                        else if (xpp.getName().equalsIgnoreCase("link")) {
                            if (GetItem) {
                                link.add(xpp.nextText());
                            }
                        }

                        else if (xpp.getName().equalsIgnoreCase("description")) {
                            if (GetItem) {
                                description.add(xpp.nextText());
                            }
                        }
                       /*else if (xpp.getName().equalsIgnoreCase("media:content")) {
                            if (GetItem) {
                                link_img.add(xpp.getAttributeValue(0));
                            }
                        }*/
                         else if (xpp.getName().equalsIgnoreCase("category")) {
                            if (GetItem) {
                                category.add(xpp.nextText());
                            }
                        }

                    } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        GetItem = false;
                    }

                    eventType = xpp.next();
                }

                for (int i = 0; i < title.size(); i++) {
                    List_item ls = new List_item();
                    ls.setTitle(title.get(i));
                    ls.setCategory(category.get(i));
                    ls.setDescription(description.get(i));
                    ls.setLink(link.get(i));
                    //ls.setImg(link_img.get(i));
                    ListItem.add(ls);
                }


            } catch (MalformedURLException e) {
                exception = e;
            } catch (XmlPullParserException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            }
            return exception;
        }

        @Override
        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);

            listAdapter list_Adapter = new listAdapter(ListItem);
            lv.setAdapter(list_Adapter);

        }
    }

    class listAdapter extends BaseAdapter {
        ArrayList<List_item> listItem = new ArrayList<>();

        listAdapter(ArrayList<List_item> list) {
            this.listItem = list;
        }

        @Override
        public int getCount() {
            return listItem.size();
        }

        @Override
        public Object getItem(int i) {
            return listItem.get(i).title;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            LayoutInflater layoutInflater = getLayoutInflater();

            @SuppressLint("ViewHolder")
            View view1 = layoutInflater.inflate(R.layout.row_items, null);

            TextView title = view1.findViewById(R.id.Text_title);
            TextView Text_category = view1.findViewById(R.id.Text_category);
            TextView Text_description = view1.findViewById(R.id.Text_description);
            ImageView img = view1.findViewById(R.id.Img);

            title.setText(listItem.get(i).title);
            Text_category.setText(listItem.get(i).category);

            String Des = (listItem.get(i).description);
            Text_description.setText(Html.fromHtml(Des, Html.FROM_HTML_MODE_COMPACT));

            try {
                Picasso.with(MainActivity.this).load(listItem.get(i).Img)
                        .error(R.drawable.ic_action_img)
                        .placeholder(R.drawable.ic_action_img).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                        .into(img);


            } catch (Exception e) {
            }
            return view1;

        }
    }
}
