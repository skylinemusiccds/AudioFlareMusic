package com.maxrave.simpmusic.ui.fragment.home

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.ListPopupWindow
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.maxrave.simpmusic.R
import com.maxrave.simpmusic.adapter.home.GenreAdapter
import com.maxrave.simpmusic.data.model.explore.mood.Genre
import com.maxrave.simpmusic.data.model.explore.mood.Mood
import com.maxrave.simpmusic.data.model.explore.mood.MoodsMoment
import com.maxrave.simpmusic.adapter.home.HomeItemAdapter
import com.maxrave.simpmusic.adapter.home.MoodsMomentAdapter
import com.maxrave.simpmusic.adapter.home.QuickPicksAdapter
import com.maxrave.simpmusic.adapter.home.chart.ArtistChartAdapter
import com.maxrave.simpmusic.adapter.home.chart.TrackChartAdapter
import com.maxrave.simpmusic.common.Config
import com.maxrave.simpmusic.data.model.browse.album.Track
import com.maxrave.simpmusic.data.model.home.Content
import com.maxrave.simpmusic.data.model.home.chart.Chart
import com.maxrave.simpmusic.data.model.home.chart.ItemArtist
import com.maxrave.simpmusic.data.model.home.chart.ItemVideo
import com.maxrave.simpmusic.data.model.home.chart.toTrack
import com.maxrave.simpmusic.data.model.home.homeItem
import com.maxrave.simpmusic.data.queue.Queue
import com.maxrave.simpmusic.databinding.FragmentHomeBinding
import com.maxrave.simpmusic.extension.toTrack
import com.maxrave.simpmusic.utils.Resource
import com.maxrave.simpmusic.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import java.text.SimpleDateFormat
import java.util.Calendar

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<HomeViewModel>()


    private lateinit var mAdapter: HomeItemAdapter
    private lateinit var moodsMomentAdapter: MoodsMomentAdapter
    private lateinit var genreAdapter: GenreAdapter
    private lateinit var trackChartAdapter: TrackChartAdapter
    private lateinit var artistChartAdapter: ArtistChartAdapter
    private lateinit var quickPicksAdapter: QuickPicksAdapter

    private var homeItemList: ArrayList<homeItem>? = null
    private var homeItemListWithoutQuickPicks: ArrayList<homeItem>? = null
    private var exploreMoodItem: Mood? = null
    private var moodsMoment: ArrayList<MoodsMoment>? = null
    private var genre: ArrayList<Genre>? = null

    private var chart: Chart? = null
    private var trackChart: ArrayList<ItemVideo>? = null
    private var artistChart: ArrayList<ItemArtist>? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.root.applyInsetter {
            type(statusBars = true) {
                margin()
            }
        }
        return binding.root
    }

    override fun onResume() {
        if (viewModel.loading.value == true) {
            binding.shimmerLayout.startShimmer()
            binding.shimmerLayout.visibility = View.VISIBLE
        } else {
            binding.shimmerLayout.stopShimmer()
            binding.shimmerLayout.visibility = View.GONE
        }
        super.onResume()
    }

    override fun onPause() {
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        super.onPause()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getLocation()
        binding.shimmerLayout.stopShimmer()
        binding.shimmerLayout.visibility = View.GONE
        val items = arrayOf("US", "ZZ", "AR", "AU", "AT", "BE", "BO", "BR", "CA", "CL", "CO", "CR", "CZ", "DK", "DO", "EC", "EG", "SV", "EE", "FI", "FR", "DE", "GT", "HN", "HU", "IS", "IN", "ID", "IE", "IL", "IT", "JP", "KE", "LU", "MX", "NL", "NZ", "NI", "NG", "NO", "PA", "PY", "PE", "PL", "PT", "RO", "RU", "SA", "RS", "ZA", "KR", "ES", "SE", "CH", "TZ", "TR", "UG", "UA", "AE", "GB", "UY", "ZW")
        val itemsData = arrayOf("United States", "Global", "Argentina", "Australia", "Austria", "Belgium", "Bolivia", "Brazil", "Canada", "Chile", "Colombia", "Costa Rica", "Czech Republic", "Denmark", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Estonia", "Finland", "France", "Germany", "Guatemala", "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Ireland", "Israel", "Italy", "Japan", "Kenya", "Luxembourg", "Mexico", "Netherlands", "New Zealand", "Nicaragua", "Nigeria", "Norway", "Panama", "Paraguay", "Peru", "Poland", "Portugal", "Romania", "Russia", "Saudi Arabia", "Serbia", "South Africa", "South Korea", "Spain", "Sweden", "Switzerland", "Tanzania", "Turkey", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "Uruguay", "Zimbabwe")

        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat("HH")
        val check = formatter.format(date).toInt()
        if (check in 6..12)
        {
            binding.topAppBar.subtitle = "Good Morning"
        }
        else if (check in 13..17)
        {
            binding.topAppBar.subtitle = "Good Afternoon"
        }
        else if (check in 18..23)
        {
            binding.topAppBar.subtitle = "Good Evening"
        }
        else
        {
            binding.topAppBar.subtitle = "Good Night"
        }
        Log.d("Check",formatter.format(date))
        Log.d("Date", "onCreateView: $date")
        binding.fullLayout.visibility = View.GONE
        mAdapter = HomeItemAdapter(arrayListOf(), requireContext(), findNavController())
        moodsMomentAdapter = MoodsMomentAdapter(arrayListOf())
        genreAdapter = GenreAdapter(arrayListOf())
        trackChartAdapter = TrackChartAdapter(arrayListOf(), requireContext())
        artistChartAdapter = ArtistChartAdapter(arrayListOf(), requireContext())
        quickPicksAdapter = QuickPicksAdapter(arrayListOf(), requireContext(), findNavController())
        val gridLayoutManager1 = GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        val gridLayoutManager2 = GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false)
        val gridLayoutManager3 = GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false) //artist chart
        val gridLayoutManager4 = GridLayoutManager(requireContext(), 3, GridLayoutManager.HORIZONTAL, false) //quick picks
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        val layoutManager2 = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHome.apply {
            this.adapter = mAdapter
            this.layoutManager = layoutManager
        }
        binding.rvMoodsMoment.apply {
            this.adapter = moodsMomentAdapter
            this.layoutManager = gridLayoutManager1
        }
        binding.rvGenre.apply {
            this.adapter = genreAdapter
            this.layoutManager = gridLayoutManager2
        }
        binding.rvTopTrack.apply {
            this.adapter = trackChartAdapter
            this.layoutManager = layoutManager2
        }
        binding.rvTopArtist.apply {
            this.adapter = artistChartAdapter
            this.layoutManager = gridLayoutManager3
        }
        binding.rvQuickPicks.apply {
            this.adapter = quickPicksAdapter
            this.layoutManager = gridLayoutManager4
        }
        if (viewModel.homeItemList.value == null || viewModel.homeItemList.value?.data == null || viewModel.homeItemList.value?.data!!.isEmpty()){
            fetchHomeData()
            viewModel.loading.observe(viewLifecycleOwner) { loading ->
                if (!loading)
                {
                    observerChart()
                    if (viewModel.regionCodeChart.value != null)
                    {
                        for (i in 1..items.size)
                        {
                            if (viewModel.regionCodeChart.value == items[i])
                            {
                                binding.btRegionCode.text = itemsData[i]
                                break
                            }
                        }
                        Log.d("Region Code", "onViewCreated: ${viewModel.regionCodeChart.value}")
                    }
                    viewModel.exploreMoodItem.observe(viewLifecycleOwner) { result ->
                        when (result) {
                            is Resource.Success -> {
                                result.let {
                                    exploreMoodItem = result.data as Mood
                                    moodsMoment = exploreMoodItem?.moodsMoments as ArrayList<MoodsMoment>
                                    genre = exploreMoodItem?.genres as ArrayList<Genre>
                                    Log.d("Moods & Moment", "onViewCreated: $moodsMoment")
                                    Log.d("Genre", "onViewCreated: $genre")
                                    moodsMomentAdapter.updateData(moodsMoment!!)
                                    genreAdapter.updateData(genre!!)
                                }
                            }
                            is Resource.Error -> {}
                        }
                    }
                }
                else {
                    binding.fullLayout.visibility = View.GONE
                    binding.swipeRefreshLayout.isRefreshing = true
                }
            }
        }
        else {
            if (viewModel.regionCodeChart.value != null)
            {
                for (i in 1..items.size)
                {
                    if (viewModel.regionCodeChart.value == items[i])
                    {
                        binding.btRegionCode.text = itemsData[i]
                        break
                    }
                }
                Log.d("Region Code", "onViewCreated: ${viewModel.regionCodeChart.value}")
                binding.chartResultLayout.visibility = View.GONE
            }
            Log.d("Data from Result", "Load lại từ viewModel: ${viewModel.homeItemList.value}")
            if (homeItemList == null) {
                homeItemList = ArrayList()
                homeItemListWithoutQuickPicks = ArrayList()
            }
            else
            {
                homeItemList?.clear()
                homeItemListWithoutQuickPicks?.clear()
            }
            chart = viewModel.chart.value?.data!!
            Log.d("Chart", "onViewCreated: $chart")
            trackChart = chart?.videos?.items as ArrayList<ItemVideo>
            artistChart = chart?.artists?.itemArtists as ArrayList<ItemArtist>
            trackChartAdapter.updateData(viewModel.chart.value?.data?.videos?.items!! as ArrayList<ItemVideo>)
            artistChartAdapter.updateData(viewModel.chart.value?.data?.artists?.itemArtists!! as ArrayList<ItemArtist>)
            binding.chartResultLayout.visibility = View.VISIBLE
            binding.chartLoadingLayout.visibility = View.GONE
            homeItemList?.addAll(viewModel.homeItemList.value?.data!!)
            Log.d("Data", "onViewCreated: $homeItemList")
            if (homeItemList!![0].title == "Quick picks")
            {
                val temp = homeItemList!![0].contents as ArrayList<Content>
                quickPicksAdapter.updateData(temp)
                for (i in 1 until homeItemList!!.size){
                    homeItemListWithoutQuickPicks?.add(homeItemList!![i])
                }
            }
            else
            {
                homeItemListWithoutQuickPicks = homeItemList
            }
            mAdapter.updateData(homeItemListWithoutQuickPicks!!)
            exploreMoodItem = viewModel.exploreMoodItem.value?.data as Mood
            moodsMoment = exploreMoodItem?.moodsMoments as ArrayList<MoodsMoment>
            genre = exploreMoodItem?.genres as ArrayList<Genre>
            Log.d("Moods & Moment", "onViewCreated: $moodsMoment")
            Log.d("Genre", "onViewCreated: $genre")
//            binding.tvTitleMoodsMoment.visibility = View.VISIBLE
//            binding.tvTitleGenre.visibility = View.VISIBLE
            moodsMomentAdapter.updateData(moodsMoment!!)
            genreAdapter.updateData(genre!!)
            binding.fullLayout.visibility = View.VISIBLE
            binding.swipeRefreshLayout.isRefreshing = false
        }
        moodsMomentAdapter.setOnMoodsMomentClickListener(object : MoodsMomentAdapter.onMoodsMomentItemClickListener {
            override fun onMoodsMomentItemClick(position: Int) {
                val args = Bundle()
                args.putString("params", moodsMoment?.get(position)?.params.toString())
                findNavController().navigate(R.id.action_global_moodFragment, args)
            }
        })
        genreAdapter.setOnGenreClickListener(object : GenreAdapter.onGenreItemClickListener {
            override fun onGenreItemClick(position: Int) {
                val args = Bundle()
                args.putString("params", genre?.get(position)?.params.toString())
                findNavController().navigate(R.id.action_global_moodFragment, args)
            }
        })
        artistChartAdapter.setOnArtistClickListener(object : ArtistChartAdapter.onArtistItemClickListener {
            override fun onArtistItemClick(position: Int) {
                val args = Bundle()
                args.putString("channelId", artistChart?.get(position)?.browseId.toString())
                findNavController().navigate(R.id.action_global_artistFragment, args)
            }
        })
        quickPicksAdapter.setOnClickListener(object : QuickPicksAdapter.OnClickListener {
            override fun onClick(position: Int) {
                val song = quickPicksAdapter.getData()[position]
                if (song.videoId != null){
                    Queue.clear()
                    val firstQueue: Track = song.toTrack()
                    Queue.setNowPlaying(firstQueue)
                    val args = Bundle()
                    args.putString("videoId", song.videoId)
                    args.putString("from", "\"${song.title}\" Radio")
                    args.putString("type", Config.SONG_CLICK)
                    findNavController().navigate(R.id.action_global_nowPlayingFragment, args)
                }
                else
                {
                    Toast.makeText( requireContext(),"This song is not available", Toast.LENGTH_SHORT).show()
                }
            }
        })
        trackChartAdapter.setOnItemClickListener(object : TrackChartAdapter.setOnItemClickListener{
            override fun onItemClick(position: Int) {
                val song = trackChart?.get(position)
                if (song != null){
                    Queue.clear()
                    val firstQueue: Track = song.toTrack()
                    Queue.setNowPlaying(firstQueue)
                    val args = Bundle()
                    args.putString("videoId", song.videoId)
                    args.putString("from", "\"${song.title}\" in Charts")
                    args.putString("type", Config.SONG_CLICK)
                    findNavController().navigate(R.id.action_global_nowPlayingFragment, args)
                }
            }
        })
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchHomeData()
        }
        val listPopup = ListPopupWindow(requireContext(), null, com.google.android.material.R.attr.listPopupWindowStyle)
        listPopup.anchorView = binding.btRegionCode
        val codeAdapter = ArrayAdapter(requireContext(), R.layout.item_list_popup, itemsData)
        listPopup.setAdapter(codeAdapter)
        listPopup.setOnItemClickListener { _, _, position, _ ->
            binding.btRegionCode.text = itemsData[position]
            binding.chartResultLayout.visibility = View.GONE
            binding.chartLoadingLayout.visibility = View.VISIBLE
            viewModel.exploreChart(items[position])
            viewModel.loadingChart.observe(viewLifecycleOwner) { loading ->
                if (!loading)
                {
                    observerChart()
                }
            }
            listPopup.dismiss()
        }
        binding.btRegionCode.setOnClickListener {
            listPopup.show()
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home_fragment_menu_item_recently_played -> {
                    findNavController().navigate(R.id.action_bottom_navigation_item_home_to_recentlySongsFragment)
                    true
                }
                R.id.home_fragment_menu_item_settings -> {
                    findNavController().navigate(R.id.action_bottom_navigation_item_home_to_settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchHomeData() {
        binding.shimmerLayout.startShimmer()
        fetchResponse()
        viewModel.homeItemList.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let {
                        Log.d("Data from Result", "onViewCreated: $it")
                        if (homeItemList == null) {
                            homeItemList = ArrayList()
                            homeItemListWithoutQuickPicks = ArrayList()
                        }
                        else
                        {
                            homeItemList?.clear()
                            homeItemListWithoutQuickPicks?.clear()
                        }
                        homeItemList?.addAll(it!!)
                        if (homeItemList!![0].title == "Quick picks")
                        {
                            val temp = homeItemList!![0].contents as ArrayList<Content>
                            quickPicksAdapter.updateData(temp)
                            for (i in 1 until homeItemList!!.size){
                                homeItemListWithoutQuickPicks?.add(homeItemList!![i])
                            }
                        }
                        else
                        {
                            homeItemListWithoutQuickPicks = homeItemList
                        }
                        Log.d("Data", "onViewCreated: $homeItemList")
                        mAdapter.updateData(homeItemListWithoutQuickPicks!!)
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.visibility = View.GONE
                        binding.fullLayout.visibility = View.VISIBLE
                        binding.swipeRefreshLayout.isRefreshing = false

                    }
                }
                is Resource.Error -> {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                    binding.fullLayout.visibility = View.VISIBLE
                    binding.swipeRefreshLayout.isRefreshing = false
                    response.message?.let { message ->
                        Snackbar.make(binding.root, "Home Data Error "+message, Snackbar.LENGTH_LONG)
                            .setAction("Retry") {
                                fetchHomeData()
                            }
                            .setDuration(3000)
                            .show()
                    }
                }
            }

        }
        viewModel.exploreMoodItem.observe(viewLifecycleOwner) {response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let {
                        exploreMoodItem = response.data as Mood
                        moodsMoment = response.data.moodsMoments
                        genre = response.data.genres
                        Log.d("Moods & Moment", "onViewCreated: $moodsMoment")
                        Log.d("Genre", "onViewCreated: $genre")
                        moodsMomentAdapter.updateData(moodsMoment!!)
                        genreAdapter.updateData(genre!!)

                    }
                }
                is Resource.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    response.message?.let { message ->
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                            .setAction("Retry") {
                                fetchHomeData()
                            }
                            .setDuration(5000)
                            .show()
                    }
                }
            }
        }
        observerChart()
    }

    private fun fetchResponse() {
        viewModel.getHomeItemList()
    }
    private fun observerChart(){
        viewModel.chart.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let {
                        chart = response.data
                        trackChart = chart?.videos?.items as ArrayList<ItemVideo>
                        artistChart = chart?.artists?.itemArtists as ArrayList<ItemArtist>
                        trackChartAdapter.updateData(trackChart!!)
                        artistChartAdapter.updateData(artistChart!!)
                    }
                    binding.chartResultLayout.visibility = View.VISIBLE
                    binding.chartLoadingLayout.visibility = View.GONE
                }
                is Resource.Error -> {
                    response.message?.let { message ->
                        Snackbar.make(binding.root, "Chart Load Error "+ message, Snackbar.LENGTH_LONG)
                            .setAction("Retry") {
                                fetchHomeData()
                            }
                            .setDuration(5000)
                            .show()
                    }
                    binding.chartResultLayout.visibility = View.GONE
                    binding.chartLoadingLayout.visibility = View.GONE
                }
            }
        }
    }
}