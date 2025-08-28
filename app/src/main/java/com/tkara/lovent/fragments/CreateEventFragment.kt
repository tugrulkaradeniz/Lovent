package com.tkara.lovent.fragments

import HttpJsonClient
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.tkara.lovent.R
import com.tkara.lovent.SessionManager
import com.tkara.lovent.StaticValues
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ==================== CreateEventFragment ====================
class CreateEventFragment : Fragment() {

    private lateinit var tilTitle: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var tilDescription: TextInputLayout
    private lateinit var etDescription: TextInputEditText
    private lateinit var tilLocation: TextInputLayout
    private lateinit var etLocation: TextInputEditText
    private lateinit var etDateTime: TextInputEditText
    private lateinit var tilMaxParticipants: TextInputLayout
    private lateinit var etMaxParticipants: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var tilPrice: TextInputLayout
    private lateinit var etPrice: TextInputEditText
    private lateinit var tilRequirements: TextInputLayout
    private lateinit var etRequirements: TextInputEditText
    private lateinit var etTags: TextInputEditText
    private lateinit var chipGroupTags: ChipGroup
    private lateinit var btnCreateEvent: MaterialButton

    private lateinit var sessionManager: SessionManager
    private lateinit var httpClient: HttpJsonClient

    private var selectedDateTime: Calendar? = null
    private val tagsList = mutableListOf<String>()

    private val categories = arrayOf(
        "Genel", "Spor", "Eğlence", "Açık Hava", "Yemek",
        "Kültür", "Eğitim", "Teknoloji", "Sanat", "Müzik"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        setupCategorySpinner()
        setupDateTimePicker()
        setupTagsInput()
        setupCreateButton()
    }

    private fun initializeComponents() {
        sessionManager = SessionManager.getInstance(requireContext())
        httpClient = HttpJsonClient()

        tilTitle = requireView().findViewById(R.id.til_title)
        etTitle = requireView().findViewById(R.id.et_title)
        tilDescription = requireView().findViewById(R.id.til_description)
        etDescription = requireView().findViewById(R.id.et_description)
        tilLocation = requireView().findViewById(R.id.til_location)
        etLocation = requireView().findViewById(R.id.et_location)
        etDateTime = requireView().findViewById(R.id.et_date_time)
        tilMaxParticipants = requireView().findViewById(R.id.til_max_participants)
        etMaxParticipants = requireView().findViewById(R.id.et_max_participants)
        spinnerCategory = requireView().findViewById(R.id.spinner_category)
        tilPrice = requireView().findViewById(R.id.til_price)
        etPrice = requireView().findViewById(R.id.et_price)
        tilRequirements = requireView().findViewById(R.id.til_requirements)
        etRequirements = requireView().findViewById(R.id.et_requirements)
        etTags = requireView().findViewById(R.id.et_tags)
        chipGroupTags = requireView().findViewById(R.id.chip_group_tags)
        btnCreateEvent = requireView().findViewById(R.id.btn_create_event)

        // Default değerler
        etMaxParticipants.setText("10")
        etPrice.setText("0")
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun setupDateTimePicker() {
        etDateTime.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()

        // Tarih seçici
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)

                // Saat seçici
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)

                        selectedDateTime = calendar
                        updateDateTimeDisplay()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Bugünden önce seçilemez
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateTimeDisplay() {
        selectedDateTime?.let { calendar ->
            val format = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr", "TR"))
            etDateTime.setText(format.format(calendar.time))
        }
    }

    private fun setupTagsInput() {
        etTags.setOnEditorActionListener { _, _, _ ->
            addTag()
            true
        }

        requireView().findViewById<Button>(R.id.btn_add_tag).setOnClickListener {
            addTag()
        }
    }

    private fun addTag() {
        val tagText = etTags.text.toString().trim()
        if (tagText.isNotEmpty() && !tagsList.contains(tagText) && tagsList.size < 5) {
            tagsList.add(tagText)
            addTagChip(tagText)
            etTags.setText("")
        }
    }

    private fun addTagChip(tagText: String) {
        val chip = Chip(requireContext())
        chip.text = tagText
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            chipGroupTags.removeView(chip)
            tagsList.remove(tagText)
        }
        chipGroupTags.addView(chip)
    }

    private fun setupCreateButton() {
        btnCreateEvent.setOnClickListener {
            createEvent()
        }
    }

    private fun createEvent() {
        if (!validateForm()) return

        val userIdString = sessionManager.getUserId()
        StaticValues.debugLog("CreateEventFragment", "User ID String: '$userIdString'")

        val creatorId = userIdString.toIntOrNull()
        StaticValues.debugLog("CreateEventFragment", "Creator ID Int: $creatorId")

        if (creatorId == null || creatorId == 0) {
            showToast("Kullanıcı ID hatası: '$userIdString' - Tekrar giriş yapın")
            return
        }

        btnCreateEvent.isEnabled = false
        btnCreateEvent.text = "Oluşturuluyor..."

        lifecycleScope.launch {
            try {

                val eventData = mapOf(
                    "creator_id" to creatorId,
                    "title" to etTitle.text.toString().trim(),
                    "description" to etDescription.text.toString().trim(),
                    "location" to etLocation.text.toString().trim(),
                    "date_time" to getFormattedDateTime(),
                    "max_participants" to etMaxParticipants.text.toString().toInt(),
                    "category" to categories[spinnerCategory.selectedItemPosition],
                    "price" to (etPrice.text.toString().toDoubleOrNull() ?: 0.0),
                    "currency" to "TL",
                    "requirements" to etRequirements.text.toString().trim(),
                    "tags" to tagsList
                )

                val response = httpClient.postJson(StaticValues.EVENTS_CREATE, eventData)
                val responseMap = httpClient.parseJsonResponse(response)

                if (responseMap["success"] == true) {
                    showToast("Etkinlik başarıyla oluşturuldu! 🎉")
                    clearForm()

                    // Ana sayfaya dön
                    parentFragmentManager.popBackStack()
                } else {
                    val errorMessage = responseMap["error"] as? String ?: "Etkinlik oluşturulamadı"
                    showToast(errorMessage)
                }

            } catch (e: Exception) {
                showToast("Bağlantı hatası: ${e.message}")
                StaticValues.debugLog("CreateEventFragment", "Error: ${e.message}")
            } finally {
                btnCreateEvent.isEnabled = true
                btnCreateEvent.text = "Etkinliği Oluştur"
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Başlık kontrolü
        val title = etTitle.text.toString().trim()
        if (title.isEmpty()) {
            tilTitle.error = "Başlık gerekli"
            isValid = false
        } else if (title.length < 3) {
            tilTitle.error = "Başlık en az 3 karakter olmalı"
            isValid = false
        } else {
            tilTitle.error = null
        }

        // Konum kontrolü
        val location = etLocation.text.toString().trim()
        if (location.isEmpty()) {
            tilLocation.error = "Konum gerekli"
            isValid = false
        } else {
            tilLocation.error = null
        }

        // Tarih kontrolü
        if (selectedDateTime == null) {
            showToast("Lütfen tarih ve saat seçin")
            isValid = false
        } else if (selectedDateTime!!.timeInMillis <= System.currentTimeMillis()) {
            showToast("Etkinlik tarihi gelecekte olmalı")
            isValid = false
        }

        // Katılımcı sayısı kontrolü
        val maxParticipants = etMaxParticipants.text.toString().toIntOrNull()
        if (maxParticipants == null || maxParticipants < 1) {
            tilMaxParticipants.error = "Geçerli katılımcı sayısı girin"
            isValid = false
        } else if (maxParticipants > 1000) {
            tilMaxParticipants.error = "Maksimum 1000 kişi olabilir"
            isValid = false
        } else {
            tilMaxParticipants.error = null
        }

        // Fiyat kontrolü
        val price = etPrice.text.toString().toDoubleOrNull()
        if (price == null || price < 0) {
            tilPrice.error = "Geçerli fiyat girin (0 ücretsiz için)"
            isValid = false
        } else {
            tilPrice.error = null
        }

        return isValid
    }

    private fun getFormattedDateTime(): String {
        selectedDateTime?.let { calendar ->
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return format.format(calendar.time)
        }
        return ""
    }

    private fun clearForm() {
        etTitle.setText("")
        etDescription.setText("")
        etLocation.setText("")
        etDateTime.setText("")
        etMaxParticipants.setText("10")
        etPrice.setText("0")
        etRequirements.setText("")
        etTags.setText("")
        chipGroupTags.removeAllViews()
        tagsList.clear()
        selectedDateTime = null
        spinnerCategory.setSelection(0)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}

// ==================== Diğer Fragment'lar ====================




