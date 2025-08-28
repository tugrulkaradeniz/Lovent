package com.tkara.lovent.models

import java.text.SimpleDateFormat
import java.util.*

/**
 * Etkinlik model sınıfı
 */
data class Event(
    val id: String,
    val title: String,
    val description: String,
    val creatorId: String = "",
    val creatorName: String,
    val creatorPhoto: String? = null,
    val location: String,
    val dateTime: String, // "2025-01-15 14:00" formatında
    val participantCount: Int,
    val maxParticipants: Int,
    val category: String,
    val imageUrl: String? = null,
    val isJoined: Boolean = false,
    val isCreatedByUser: Boolean = false,
    val tags: List<String> = emptyList(),
    val price: Double = 0.0,
    val currency: String = "TL",
    val requirements: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
) {

    companion object {
        /**
         * API response'undan Event objesi oluştur
         */
        fun fromMap(data: Map<String, Any?>): Event {
            return Event(
                id = data["id"]?.toString() ?: "",
                title = data["title"]?.toString() ?: "",
                description = data["description"]?.toString() ?: "",
                creatorId = data["creator_id"]?.toString() ?: "",
                creatorName = data["creator_name"]?.toString() ?: "Bilinmeyen",
                creatorPhoto = data["creator_photo"]?.toString(),
                location = data["location"]?.toString() ?: "",
                dateTime = data["date_time"]?.toString() ?: "",
                participantCount = (data["participant_count"] as? Number)?.toInt() ?: 0,
                maxParticipants = (data["max_participants"] as? Number)?.toInt() ?: 0,
                category = data["category"]?.toString() ?: "Genel",
                imageUrl = data["image_url"]?.toString(),
                isJoined = data["is_joined"] as? Boolean ?: false,
                isCreatedByUser = data["is_created_by_user"] as? Boolean ?: false,
                tags = (data["tags"] as? List<*>)?.map { it.toString() } ?: emptyList(),
                price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                currency = data["currency"]?.toString() ?: "TL",
                requirements = data["requirements"]?.toString() ?: "",
                createdAt = data["created_at"]?.toString() ?: "",
                updatedAt = data["updated_at"]?.toString() ?: ""
            )
        }
    }

    /**
     * Tarih ve saati formatla
     */
    fun getFormattedDateTime(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("tr", "TR"))
            val date = inputFormat.parse(dateTime)
            date?.let { outputFormat.format(it) } ?: dateTime
        } catch (e: Exception) {
            dateTime
        }
    }

    /**
     * Sadece tarihi formatla
     */
    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("tr", "TR"))
            val date = inputFormat.parse(dateTime)
            date?.let { outputFormat.format(it) } ?: dateTime.split(" ")[0]
        } catch (e: Exception) {
            dateTime.split(" ")[0]
        }
    }

    /**
     * Sadece saati formatla
     */
    fun getFormattedTime(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateTime)
            date?.let { outputFormat.format(it) } ?: dateTime.split(" ")[1]
        } catch (e: Exception) {
            dateTime.split(" ")[1]
        }
    }

    /**
     * Etkinlik dolmuş mu kontrol et
     */
    fun isFull(): Boolean {
        return participantCount >= maxParticipants
    }

    /**
     * Etkinlik geçmiş mi kontrol et
     */
    fun isPast(): Boolean {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val eventDate = inputFormat.parse(dateTime)
            val currentDate = Date()
            eventDate?.before(currentDate) ?: false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Etkinlik bugün mü
     */
    fun isToday(): Boolean {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val eventDate = inputFormat.parse(dateTime)
            val today = Date()

            val eventCal = Calendar.getInstance().apply { time = eventDate!! }
            val todayCal = Calendar.getInstance().apply { time = today }

            eventCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    eventCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Kalan katılımcı sayısı
     */
    fun getRemainingSlots(): Int {
        return maxOf(0, maxParticipants - participantCount)
    }

    /**
     * Doluluk yüzdesi
     */
    fun getCapacityPercentage(): Int {
        return if (maxParticipants > 0) {
            (participantCount * 100) / maxParticipants
        } else 0
    }

    /**
     * Fiyat formatla
     */
    fun getFormattedPrice(): String {
        return if (price > 0) {
            "${price.toInt()} $currency"
        } else "Ücretsiz"
    }

    /**
     * Kategori rengini döndür
     */
    fun getCategoryColor(): String {
        return when (category.lowercase()) {
            "spor" -> "#4CAF50"
            "eğlence" -> "#FF9800"
            "açık hava" -> "#8BC34A"
            "yemek" -> "#FF5722"
            "kültür" -> "#9C27B0"
            "eğitim" -> "#2196F3"
            "teknoloji" -> "#607D8B"
            "sanat" -> "#E91E63"
            else -> "#6C63FF"
        }
    }

    /**
     * Map'e çevir (API'ye gönderim için)
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "creator_id" to creatorId,
            "location" to location,
            "date_time" to dateTime,
            "max_participants" to maxParticipants,
            "category" to category,
            "tags" to tags,
            "price" to price,
            "currency" to currency,
            "requirements" to requirements
        )
    }
}