package edu.stanford.gaborc.simpleyelp

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.yelp.com/v3/"
private const val API_KEY = ""
class MainActivity : AppCompatActivity() {

    private val restaurants = mutableListOf<YelpRestaurant>()
    private val adapter = RestaurantAdapter(this, restaurants)
    private val retrofit = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
    private val yelpService = retrofit.create(YelpService::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handleIntent(intent)
        rvRestaurants.adapter = adapter
        rvRestaurants.layoutManager = LinearLayoutManager(this)
        searchItems("Avocado Toast")

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu?.findItem(R.id.search)?.actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        return true
    }

    private fun handleIntent(intent: Intent) {

        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            Log.i(TAG, "onsearch $query")
            if (query != null) {
                searchItems(query)
            }
        }
    }


    private fun searchItems(keyword: String) {

        yelpService.searchRestaurants("Bearer $API_KEY", keyword, "New York").enqueue(object: Callback<YelpSearchResult> {
            override fun onFailure(call: Call<YelpSearchResult>, t: Throwable) {

                Log.i(TAG, "onFailure $t")
            }

            override fun onResponse(call: Call<YelpSearchResult>, response: Response<YelpSearchResult>) {
                Log.i(TAG, "onReponse $response")
                val body = response.body()
                if (body == null) {
                    Log.w(TAG, "Did not recieve valid response")
                    return
                }
                restaurants.clear()
                restaurants.addAll(body.restaurants)
                adapter.notifyDataSetChanged()
            }

        })
    }
}