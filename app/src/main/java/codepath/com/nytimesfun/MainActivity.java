package codepath.com.nytimesfun;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    ArrayList<Article> articles;
    ArticleRecyclerViewAdapter adapter;
//    ArticleArrayAdapter adapter;
    String searchQuery;

    HashSet<String> articlesSeen = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) findViewById(R.id.rvResults);
        articles = new ArrayList<>();
/*        Article article = new Article(new JSONObject());
        article.snippet = "hey";
        articles.add(article);*/

        //adapter = new ArticleArrayAdapter(this, articles);
        //GridView lv = (GridView) findViewById(R.id.lvResults);
        //lv.setAdapter(adapter);
        adapter = new ArticleRecyclerViewAdapter(articles);
        mRecyclerView.setAdapter(adapter);

        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        //layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                Log.d("here", String.valueOf(page));
                loadDataFromApi(page);
            }
        });

/*        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Article article = articles.get(position);
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                intent.putExtra(ArticleActivity.ARTICLE, article);
                startActivity(intent);
            }
        });

        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                Log.d("here", String.valueOf(page));
                loadDataFromApi(page);
                return true;
            }
        });*/
    }

    public void loadDataFromApi(int page) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("api-key", "e863004d8ddc2900d7111aa358a8bdbb:19:4701790");
        params.put("q", searchQuery);
//        params.put("fq", "news_desk:(\"Sports\" \"Science\")");

//        params.put("fq", "news_desk:(\"Fashion & Style\")");
// params.put("fq", "news_desk:(\"Arts\")");

        params.put("fq", "news_desk:(\"Sports\" \"Arts\" \"Fashion & Style\")");

        String url = "http://api.nytimes.com/svc/search/v2/articlesearch.json";
        params.put("page", page);
//        params.put("fq", "type_of_material:(\"News\")");
//        url = String.format("http://api.nytimes.com/svc/topstories/v1/%s.json", "home");
//        params.put("api-key", "8c0c9f857fb5e21ca09d54f3fad286ce:3:4701790");

        client.get(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
            }

            @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        JSONArray articleJsonResults = null;
                        try {
                            articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                            int curPosition = articles.size();
                            ArrayList<Article> newArticles = Article.fromJSONArray(articleJsonResults);

                            /*
                            for (Article a: newArticles) {
                                if (!articlesSeen.contains(a.getWebUrl())) {
                                    articlesSeen.add(a.getWebUrl());
                                    articles.add(a);
                                } else {
                                    Log.d("DEBUG", "**here");
                                }
                            }*/
                            articles.addAll(newArticles);
                            adapter.notifyItemRangeInserted(curPosition, curPosition + newArticles.size() - 1);
                            adapter.notifyDataSetChanged();
                            Log.d("DEBUG", articleJsonResults.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                searchQuery = query;
                searchView.clearFocus();
                initiateSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                // Handle this selection
                launchSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void search(View view) {
        initiateSearch();
    }

    public void launchSettings()  {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void initiateSearch() {
        articles.clear();
        adapter.notifyDataSetChanged();
        loadDataFromApi(0);
    }
}
