package com.tkara.lovent.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import HttpJsonClient
import com.tkara.lovent.R
import com.tkara.lovent.SessionManager
import com.tkara.lovent.StaticValues
import com.tkara.lovent.adapters.EventFeedAdapter
import com.tkara.lovent.models.Event
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvWelcome: TextView
    private lateinit var tvEmptyState: TextView

    private lateinit var eventAdapter: EventFeedAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var httpClient: HttpJsonClient

    private val eventList = mutableListOf<Event>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        setupRecyclerView()
        setupSwipeRefresh()
        setupWelcomeMessage()

        // Load events
        loadEvents()
    }

    private fun initializeComponents() {
        sessionManager = SessionManager.getInstance(requireContext())
        httpClient = HttpJsonClient()

        swipeRefresh = requireView().findViewById(R.id.swipe_refresh)
        recyclerView = requireView().findViewById(R.id.recycler_events)
        tvWelcome = requireView().findViewById(R.id.tv_welcome)
        tvEmptyState = requireView().findViewById(R.id.tv_empty_state)
    }

    private fun setupRecyclerView() {
        eventAdapter = EventFeedAdapter(eventList) { event ->
            // Event item clicked
            onEventClicked(event)
        }

        recyclerView.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeColors(
            requireContext().getColor(R.color.primary_color)
        )

        swipeRefresh.setOnRefreshListener {
            loadEvents()
        }
    }

    private fun setupWelcomeMessage() {
        val userName = sessionManager.getUserName()
        val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        val greeting = when (currentHour) {
            in 6..11 -> "Günaydın"
            in 12..17 -> "İyi günler"
            in 18..21 -> "İyi akşamlar"
            else -> "İyi geceler"
        }

        tvWelcome.text = "$greeting $userName! 🎉"
    }

    private fun loadEvents() {
        swipeRefresh.isRefreshing = true
        tvEmptyState.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val requestData = mapOf(
                    "user_id" to sessionManager.getUserId(),
                    "page" to 1,
                    "limit" to 20
                )

                val response = httpClient.postJson(StaticValues.EVENTS_FEED, requestData)
                val responseMap = httpClient.parseJsonResponse(response)

                if (responseMap["success"] == true) {
                    val eventsData = responseMap["events"] as? List<Map<String, Any?>> ?: emptyList()

                    eventList.clear()
                    eventsData.forEach { eventData ->
                        val event = Event.fromMap(eventData)
                        eventList.add(event)
                    }

                    updateUI()
                } else {
                    showError("Etkinlikler yüklenemedi")
                }

            } catch (e: Exception) {
                StaticValues.debugLog("HomeFragment", "Events load error: ${e.message}")
                showError("Bağlantı hatası")
                showMockData() // Development için mock data
            } finally {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun updateUI() {
        if (eventList.isEmpty()) {
            recyclerView.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = "Henüz etkinlik yok.\nArkadaşlarını takip et veya yeni etkinlik oluştur! 🎈"
        } else {
            recyclerView.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            eventAdapter.notifyDataSetChanged()
        }
    }

    private fun showError(message: String) {
        tvEmptyState.visibility = View.VISIBLE
        tvEmptyState.text = message
    }

    private fun onEventClicked(event: Event) {
        // Event detayına git
        StaticValues.debugLog("HomeFragment", "Event clicked: ${event.title}")
        // TODO: Navigate to EventDetailFragment
    }

    // Development için mock data
    private fun showMockData() {
        eventList.clear()
        eventList.addAll(getMockEvents())
        updateUI()
    }

    private fun getMockEvents(): List<Event> {
        return listOf(
            Event(
                id = "1",
                title = "Boğaz'da Piknik 🧺",
                description = "Haftasonu güzel bir piknik yapalım! Yemekleri ben getireceğim.",
                creatorName = "Ayşe Yılmaz",
                creatorPhoto = null,
                location = "Emirgan Korusu",
                dateTime = "2025-01-15 14:00",
                participantCount = 8,
                maxParticipants = 15,
                category = "Açık Hava",
                imageUrl = null,
                isJoined = false
            ),
            Event(
                id = "2",
                title = "Sinema Gecesi 🎬",
                description = "Yeni çıkan filmi birlikte izleyelim. Film seçimi için oylama yapacağız.",
                creatorName = "Mehmet Özkan",
                creatorPhoto = null,
                location = "Zorlu Center Cinemaximum",
                dateTime = "2025-01-12 20:00",
                participantCount = 4,
                maxParticipants = 8,
                category = "Eğlence",
                imageUrl = null,
                isJoined = true
            ),
            Event(
                id = "3",
                title = "Futbol Maçı ⚽",
                description = "5'er kişilik takımlarla dostluk maçı yapalım. Seviye fark etmez!",
                creatorName = "Can Demir",
                creatorPhoto = null,
                location = "Maçka Spor Tesisleri",
                dateTime = "2025-01-13 16:00",
                participantCount = 9,
                maxParticipants = 10,
                category = "Spor",
                imageUrl = null,
                isJoined = false
            )
        )
    }
}