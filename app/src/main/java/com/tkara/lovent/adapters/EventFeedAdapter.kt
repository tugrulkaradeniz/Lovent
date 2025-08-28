package com.tkara.lovent.adapters

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tkara.lovent.R
import com.tkara.lovent.models.Event
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class EventFeedAdapter(
    private val events: List<Event>,
    private val onEventClick: (Event) -> Unit
) : RecyclerView.Adapter<EventFeedAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event_feed, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount(): Int = events.size

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardEvent: CardView = itemView.findViewById(R.id.card_event)
        private val ivCreatorPhoto: ImageView = itemView.findViewById(R.id.iv_creator_photo)
        private val tvCreatorName: TextView = itemView.findViewById(R.id.tv_creator_name)
        private val tvEventTime: TextView = itemView.findViewById(R.id.tv_event_time)
        private val ivEventImage: ImageView = itemView.findViewById(R.id.iv_event_image)
        private val tvEventTitle: TextView = itemView.findViewById(R.id.tv_event_title)
        private val tvEventDescription: TextView = itemView.findViewById(R.id.tv_event_description)
        private val tvLocation: TextView = itemView.findViewById(R.id.tv_location)
        private val chipCategory: Chip = itemView.findViewById(R.id.chip_category)
        private val tvParticipantCount: TextView = itemView.findViewById(R.id.tv_participant_count)
        private val progressCapacity: ProgressBar = itemView.findViewById(R.id.progress_capacity)
        private val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
        private val layoutTags: LinearLayout = itemView.findViewById(R.id.layout_tags)
        private val btnJoin: MaterialButton = itemView.findViewById(R.id.btn_join)
        private val btnShare: MaterialButton = itemView.findViewById(R.id.btn_share)

        fun bind(event: Event) {
            // Creator info
            tvCreatorName.text = event.creatorName
            tvEventTime.text = event.getFormattedDateTime()

            // Creator photo (placeholder için initial)
            if (event.creatorPhoto.isNullOrEmpty()) {
                ivCreatorPhoto.setImageResource(R.drawable.ic_person_placeholder)
                // İsmin ilk harfini göster
                val initial = event.creatorName.firstOrNull()?.toString()?.uppercase() ?: "?"
                // TODO: Create circular text view for initials
            }

            // Event content
            tvEventTitle.text = event.title
            tvEventDescription.text = event.description
            tvLocation.text = "📍 ${event.location}"

            // Event image
            if (event.imageUrl.isNullOrEmpty()) {
                ivEventImage.visibility = View.GONE
            } else {
                ivEventImage.visibility = View.VISIBLE
                // TODO: Load image with Glide or similar
            }

            // Category
            chipCategory.text = event.category
            try {
                chipCategory.setChipBackgroundColorResource(android.R.color.transparent)
                chipCategory.setTextColor(Color.parseColor(event.getCategoryColor()))
                // strokeColor API 21'de mevcut değil, sadece renk ayarlıyoruz
                chipCategory.setChipBackgroundColorResource(R.color.background_grey)
            } catch (e: Exception) {
                chipCategory.setChipBackgroundColorResource(R.color.primary_color)
            }

            // Participants
            tvParticipantCount.text = "${event.participantCount}/${event.maxParticipants} katılımcı"
            progressCapacity.progress = event.getCapacityPercentage()

            // Price
            tvPrice.text = event.getFormattedPrice()
            if (event.price == 0.0) {
                tvPrice.setTextColor(ContextCompat.getColor(itemView.context, R.color.success_color))
            } else {
                tvPrice.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_color))
            }

            // Tags
            layoutTags.removeAllViews()
            event.tags.take(3).forEach { tag ->
                val chip = Chip(itemView.context)
                chip.text = "#$tag"
                chip.textSize = 12f
                chip.setChipBackgroundColorResource(R.color.background_grey)
                chip.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                layoutTags.addView(chip)
            }

            // Join button
            when {
                event.isCreatedByUser -> {
                    btnJoin.text = "Düzenle"
                    btnJoin.setIconResource(R.drawable.ic_edit)
                    btnJoin.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.text_secondary)
                }
                event.isJoined -> {
                    btnJoin.text = "Katıldım ✓"
                    btnJoin.setIconResource(R.drawable.ic_check)
                    btnJoin.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.success_color)
                }
                event.isFull() -> {
                    btnJoin.text = "Dolu"
                    btnJoin.setIconResource(R.drawable.ic_block)
                    btnJoin.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.error_color)
                    btnJoin.isEnabled = false
                }
                event.isPast() -> {
                    btnJoin.text = "Geçmiş"
                    btnJoin.setIconResource(R.drawable.ic_history)
                    btnJoin.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.text_secondary)
                    btnJoin.isEnabled = false
                }
                else -> {
                    btnJoin.text = "Katıl"
                    btnJoin.setIconResource(R.drawable.ic_add)
                    btnJoin.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.primary_color)
                    btnJoin.isEnabled = true
                }
            }

            // Special styling for today's events
            if (event.isToday()) {
                // API 21 için CardView elevation artırarak vurgulama
                cardEvent.cardElevation = 12f
                cardEvent.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.accent_light))
            } else {
                cardEvent.cardElevation = 4f
                cardEvent.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
            }

            // Click listeners
            cardEvent.setOnClickListener {
                onEventClick(event)
            }

            btnJoin.setOnClickListener {
                // Handle join/leave event
                onJoinClicked(event)
            }

            btnShare.setOnClickListener {
                // Handle share event
                onShareClicked(event)
            }
        }

        private fun onJoinClicked(event: Event) {
            // TODO: Implement join/leave functionality
            when {
                event.isCreatedByUser -> {
                    // Navigate to edit event
                }
                event.isJoined -> {
                    // Leave event
                }
                !event.isFull() && !event.isPast() -> {
                    // Join event
                }
            }
        }

        private fun onShareClicked(event: Event) {
            // TODO: Implement share functionality
            val shareText = "${event.title}\n${event.location}\n${event.getFormattedDateTime()}"
            // Create share intent
        }
    }
}