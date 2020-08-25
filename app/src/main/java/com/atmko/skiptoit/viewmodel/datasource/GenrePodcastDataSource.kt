package com.atmko.skiptoit.viewmodel.datasource

import com.atmko.skiptoit.model.ApiResults
import com.atmko.skiptoit.model.Podcast
import com.atmko.skiptoit.model.PodcastsApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GenrePodcastDataSource(
    private val podcastsApi: PodcastsApi
) : PodcastDataSource() {

    var genreId: Int? = null

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Podcast>
    ) {
        val call: Call<ApiResults> = podcastsApi.getPodcastsByGenre(genreId!!, startingPage)
        call.enqueue(object : Callback<ApiResults> {
            override fun onFailure(call: Call<ApiResults>, t: Throwable) {
                loading.postValue(false)
                loadError.postValue(true)
            }

            override fun onResponse(call: Call<ApiResults>, response: Response<ApiResults>) {
                loading.postValue(false)
                loadError.postValue(false)

                val body: ApiResults = response.body()!!
                callback.onResult(body.podcasts, null, getNextPage(body, startingPage))
            }
        })
    }

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Podcast>) {
        val call: Call<ApiResults> = podcastsApi.getPodcastsByGenre(genreId!!, params.key)
        call.enqueue(object : Callback<ApiResults> {
            override fun onFailure(call: Call<ApiResults>, t: Throwable) {
                loading.postValue(false)
                loadError.postValue(true)
            }

            override fun onResponse(call: Call<ApiResults>, response: Response<ApiResults>) {
                loading.postValue(false)
                loadError.postValue(false)

                val body: ApiResults = response.body()!!
                callback.onResult(body.podcasts, getNextPage(body, params.key))
            }
        })
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Podcast>) {

    }
}