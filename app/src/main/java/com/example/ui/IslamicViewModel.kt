package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.IslamicRepository
import com.example.data.local.AppDatabase
import com.example.data.local.PrayerTimingsEntity
import com.example.data.local.QuranAyahEntity
import com.example.data.local.QuranSurahEntity
import com.example.sensor.QiblaSensorManager
import com.example.service.AdhanAlarmScheduler
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class IslamicViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = IslamicRepository(db.appDao())
    private val qiblaSensorManager = QiblaSensorManager(application)
    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(application)

    private val TAG = "IslamicViewModel"

    // === STATE SECTIONS ===

    // Location
    private val _userCity = MutableStateFlow("Your City")
    val userCity: StateFlow<String> = _userCity.asStateFlow()

    private val _userLatitude = MutableStateFlow(0.0)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(0.0)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    // Prayer Timings state
    private val _prayerTimings = MutableStateFlow<PrayerTimingsEntity?>(null)
    val prayerTimings: StateFlow<PrayerTimingsEntity?> = _prayerTimings.asStateFlow()

    private val _timingsLoading = MutableStateFlow(false)
    val timingsLoading: StateFlow<Boolean> = _timingsLoading.asStateFlow()

    private val _timingsError = MutableStateFlow<String?>(null)
    val timingsError: StateFlow<String?> = _timingsError.asStateFlow()

    // Countdowns
    private val _nextPrayerName = MutableStateFlow("Salah")
    val nextPrayerName: StateFlow<String> = _nextPrayerName.asStateFlow()

    private val _nextPrayerCountdown = MutableStateFlow("00:00:00")
    val nextPrayerCountdown: StateFlow<String> = _nextPrayerCountdown.asStateFlow()

    // Quran State
    private val _surahs = MutableStateFlow<List<QuranSurahEntity>>(emptyList())
    val surahs: StateFlow<List<QuranSurahEntity>> = _surahs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSurahs: StateFlow<List<QuranSurahEntity>> = combine(_surahs, _searchQuery) { list, query ->
        if (query.isBlank()) list else {
            list.filter {
                it.englishName.contains(query, ignoreCase = true) ||
                        it.name.contains(query, ignoreCase = true) ||
                        it.englishNameTranslation.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSurah = MutableStateFlow<QuranSurahEntity?>(null)
    val selectedSurah: StateFlow<QuranSurahEntity?> = _selectedSurah.asStateFlow()

    private val _ayahsLoading = MutableStateFlow(false)
    val ayahsLoading: StateFlow<Boolean> = _ayahsLoading.asStateFlow()

    private val _ayahs = MutableStateFlow<List<QuranAyahEntity>>(emptyList())
    val ayahs: StateFlow<List<QuranAyahEntity>> = _ayahs.asStateFlow()

    // Audio playback state
    private var mediaPlayer: MediaPlayer? = null
    private val _isPlayingAudio = MutableStateFlow(false)
    val isPlayingAudio: StateFlow<Boolean> = _isPlayingAudio.asStateFlow()

    private val _currentPlayingIndex = MutableStateFlow(-1) // active ayah index playing (0-based)
    val currentPlayingIndex: StateFlow<Int> = _currentPlayingIndex.asStateFlow()

    private val _textSize = MutableStateFlow(22f)
    val textSize: StateFlow<Float> = _textSize.asStateFlow()

    // Qibla state
    val compassHeading: StateFlow<Double> = qiblaSensorManager.compassHeading
    private val _qiblaBearing = MutableStateFlow(135.0) // default Mecca angle
    val qiblaBearing: StateFlow<Double> = _qiblaBearing.asStateFlow()

    // Settings / Preferences (cached synchronously in db, observed reactively)
    private val _calculationMethod = MutableStateFlow(2) // Default ISNA
    val calculationMethod: StateFlow<Int> = _calculationMethod.asStateFlow()

    private val _appLanguage = MutableStateFlow("en")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _lastReadSurahNum = MutableStateFlow(1)
    val lastReadSurahNum: StateFlow<Int> = _lastReadSurahNum.asStateFlow()

    private val _lastReadAyahNum = MutableStateFlow(1)
    val lastReadAyahNum: StateFlow<Int> = _lastReadAyahNum.asStateFlow()

    // Tasbih State
    private val _tasbihCount = MutableStateFlow(0)
    val tasbihCount: StateFlow<Int> = _tasbihCount.asStateFlow()

    private val _tasbihTarget = MutableStateFlow(33)
    val tasbihTarget: StateFlow<Int> = _tasbihTarget.asStateFlow()

    private val _selectedDhikrIndex = MutableStateFlow(0)
    val selectedDhikrIndex: StateFlow<Int> = _selectedDhikrIndex.asStateFlow()

    // Jobs
    private var countdownJob: Job? = null

    init {
        // Load initial settings from DB
        viewModelScope.launch {
            _calculationMethod.value = repository.getPreference("pref_calc_method", "2").toInt()
            _appLanguage.value = repository.getPreference("pref_language", Locale.getDefault().language)
            _lastReadSurahNum.value = repository.getPreference("pref_last_read_surah", "1").toInt()
            _lastReadAyahNum.value = repository.getPreference("pref_last_read_ayah", "1").toInt()
            _tasbihCount.value = repository.getPreference("pref_tasbih_cnt", "0").toInt()
            _tasbihTarget.value = repository.getPreference("pref_tasbih_target", "33").toInt()
            _selectedDhikrIndex.value = repository.getPreference("pref_selected_dhikr", "0").toInt()

            // Fetch list of Surahs
            refreshSurahs()
        }

        // Start countdown loop
        startCountdownTicker()
    }

    // === SETTINGS ACTIONS ===

    fun updateCalculationMethod(methodId: Int) {
        _calculationMethod.value = methodId
        viewModelScope.launch {
            repository.savePreference("pref_calc_method", methodId.toString())
            // Re-fetch timings with new configuration
            fetchPrayerTimings()
        }
    }

    fun updateLanguage(langCode: String) {
        _appLanguage.value = langCode
        viewModelScope.launch {
            repository.savePreference("pref_language", langCode)
        }
    }

    fun updateLastRead(surahNum: Int, ayahNum: Int) {
        _lastReadSurahNum.value = surahNum
        _lastReadAyahNum.value = ayahNum
        viewModelScope.launch {
            repository.savePreference("pref_last_read_surah", surahNum.toString())
            repository.savePreference("pref_last_read_ayah", ayahNum.toString())
        }
    }

    // === PRAYER TIMES CORE ===

    @SuppressLint("MissingPermission")
    fun loadLocationAndFetchTimes() {
        _timingsLoading.value = true
        Log.d(TAG, "Requesting physical location...")
        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        _userLatitude.value = location.latitude
                        _userLongitude.value = location.longitude
                        // Dynamic reverse-geocaching estimation for city visual labels
                        val cityTag = estimateCity(location.latitude, location.longitude)
                        _userCity.value = cityTag
                        _qiblaBearing.value = repository.calculateQiblaDirection(location.latitude, location.longitude)
                        fetchPrayerTimings()
                    } else {
                        // Fallback: network cellular prediction
                        fusedLocationClient.getLastLocation().addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                _userLatitude.value = lastLoc.latitude
                                _userLongitude.value = lastLoc.longitude
                                _userCity.value = estimateCity(lastLoc.latitude, lastLoc.longitude)
                                _qiblaBearing.value = repository.calculateQiblaDirection(lastLoc.latitude, lastLoc.longitude)
                                fetchPrayerTimings()
                            } else {
                                // Default offline or network city names
                                useDefaultCityCallback()
                            }
                        }.addOnFailureListener {
                            useDefaultCityCallback()
                        }
                    }
                }
                .addOnFailureListener {
                    useDefaultCityCallback()
                }
        } catch (e: Exception) {
            useDefaultCityCallback()
        }
    }

    private fun useDefaultCityCallback() {
        Log.d(TAG, "Location failed, falling back to IP/Default settings.")
        _userCity.value = "New York"
        _userLatitude.value = 40.7128
        _userLongitude.value = -74.0060
        _qiblaBearing.value = repository.calculateQiblaDirection(40.7128, -74.0060)
        fetchPrayerTimings()
    }

    private fun fetchPrayerTimings() {
        viewModelScope.launch {
            _timingsLoading.value = true
            val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.US).format(Date())
            val res = repository.getPrayerTimings(
                date = dateStr,
                method = _calculationMethod.value,
                city = _userCity.value,
                latitude = _userLatitude.value,
                longitude = _userLongitude.value
            )

            res.fold(
                onSuccess = { timings ->
                    _prayerTimings.value = timings
                    _timingsError.value = null
                    _timingsLoading.value = false

                    // Schedule exact adhan alerts automatically!
                    val enabledPrayers = setOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                    AdhanAlarmScheduler.scheduleAlarms(getApplication(), timings, enabledPrayers)
                },
                onFailure = { err ->
                    _timingsError.value = err.message ?: "Failed to retrieve timings"
                    _timingsLoading.value = false
                }
            )
        }
    }

    private fun estimateCity(lat: Double, lng: Double): String {
        // A compact estimation list representing major worldwide coordinates to map user visuals without bulkyGeocoder
        return when {
            lat in 21.0..22.0 && lng in 39.0..40.0 -> "Makkah"
            lat in 24.0..25.0 && lng in 39.0..40.0 -> "Medina"
            lat in 40.0..41.0 && lng in -75.0..-73.0 -> "New York"
            lat in 51.0..52.0 && lng in -0.5..0.5 -> "London"
            lat in 48.0..49.0 && lng in 2.0..3.0 -> "Paris"
            lat in 35.0..36.0 && lng in 139.0..140.0 -> "Tokyo"
            lat in 3.0..4.0 && lng in 101.0..102.0 -> "Kuala Lumpur"
            lat in -6.5..-6.0 && lng in 106.0..107.0 -> "Jakarta"
            lat in 25.0..26.0 && lng in 55.0..56.0 -> "Dubai"
            lat in 30.0..31.0 && lng in 31.0..32.0 -> "Cairo"
            lat in 28.0..29.0 && lng in 77.0..78.0 -> "New Delhi"
            lat in 31.0..32.0 && lng in 74.0..75.0 -> "Lahore"
            lat in 41.0..42.0 && lng in 28.0..29.0 -> "Istanbul"
            lat in 55.0..56.0 && lng in 37.0..38.0 -> "Moscow"
            lat in -34.0..-33.0 && lng in 151.0..152.0 -> "Sydney"
            else -> "My City"
        }
    }

    // Countdown clock calculation
    private fun startCountdownTicker() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch(Dispatchers.Default) {
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US)
            while (true) {
                val timings = _prayerTimings.value
                if (timings != null) {
                    val now = Calendar.getInstance()
                    val prayerTimes = listOf(
                        "Fajr" to timings.fajr,
                        "Sunrise (Shuruq)" to timings.sunrise,
                        "Dhuhr" to timings.dhuhr,
                        "Asr" to timings.asr,
                        "Maghrib" to timings.maghrib,
                        "Isha" to timings.isha,
                        "Midnight" to timings.midnight
                    )

                    var targetCal: Calendar? = null
                    var targetName = "Fajr"

                    for ((name, timeStr) in prayerTimes) {
                        try {
                            val cleanTime = timeStr.substringBefore(" ").trim()
                            val dateStr = "${timings.date} $cleanTime"
                            val pCal = Calendar.getInstance().apply {
                                val parsed = sdf.parse(dateStr)
                                if (parsed != null) time = parsed
                            }
                            if (pCal.after(now)) {
                                targetCal = pCal
                                targetName = name
                                break
                            }
                        } catch (e: Exception) {
                            // no-op
                        }
                    }

                    // If all prayers passed today, target is tomorrow's Fajr
                    if (targetCal == null) {
                        try {
                            val cleanTime = timings.fajr.substringBefore(" ").trim()
                            val nextFajrCal = Calendar.getInstance().apply {
                                val parsed = sdf.parse("${timings.date} $cleanTime")
                                if (parsed != null) time = parsed
                            }
                            nextFajrCal.add(Calendar.DAY_OF_YEAR, 1)
                            targetCal = nextFajrCal
                            targetName = "Fajr (Tomorrow)"
                        } catch (e: Exception) {
                            // no-op
                        }
                    }

                    if (targetCal != null) {
                        val diffMs = targetCal.timeInMillis - now.timeInMillis
                        val diffSec = diffMs / 1000
                        val sec = diffSec % 60
                        val min = (diffSec / 60) % 60
                        val hr = diffSec / 3600
                        _nextPrayerName.value = targetName
                        _nextPrayerCountdown.value = String.format(Locale.getDefault(), "%02d:%02d:%02d", hr, min, sec)
                    }
                }
                delay(1000)
            }
        }
    }


    // === QURAN READER ===

    private fun refreshSurahs() {
        viewModelScope.launch {
            val res = repository.refreshSurahs()
            res.fold(
                onSuccess = { _surahs.value = it },
                onFailure = { Log.e(TAG, "Refresh surahs failed, but showing cached ones.") }
            )
            // Observe local database directly for real-time reactivity
            repository.getCachedSurahsFlow().collect {
                _surahs.value = it
            }
        }
    }

    fun selectSurah(surah: QuranSurahEntity) {
        _selectedSurah.value = surah
        _currentPlayingIndex.value = -1
        stopAudio()

        viewModelScope.launch {
            _ayahsLoading.value = true
            _ayahs.value = emptyList()

            // Resolve custom translation language based on app settings mapping
            val transEdition = getTranslationEditionByLang(_appLanguage.value)
            val reciterEdition = repository.getPreference("pref_reciter", "ar.alafasy")

            val res = repository.getOrFetchAyahs(surah.number, transEdition, reciterEdition)
            res.fold(
                onSuccess = {
                    _ayahs.value = it
                    _ayahsLoading.value = false
                    updateLastRead(surah.number, 1)
                },
                onFailure = {
                    _ayahsLoading.value = false
                }
            )
        }
    }

    fun changeTextSize(increase: Boolean) {
        val delta = if (increase) 2f else -2f
        _textSize.value = (_textSize.value + delta).coerceIn(12f, 40f)
    }

    fun searchSurah(query: String) {
        _searchQuery.value = query
    }

    private fun getTranslationEditionByLang(langCode: String): String {
        return when (langCode) {
            "ar" -> "en.sahih" // default parallel English, handles RTL natively
            "es" -> "es.cortes"
            "fr" -> "fr.hamidullah"
            "pt" -> "pt.elhayek"
            "it" -> "it.piccardo"
            "de" -> "de.aburida"
            "ru" -> "ru.kuliev"
            "tr" -> "tr.yazir"
            "ur" -> "ur.ahmedali"
            "hi" -> "hi.farooq"
            "id" -> "id.indonesian"
            "bn" -> "bn.bengali"
            "fa" -> "fa.makarem"
            "bs" -> "bs.korkut"
            "vi" -> "vi.khassan"
            else -> "en.sahih"
        }
    }


    // === AUDIO PLAYER SUPPORT ===

    fun toggleAudioPlay(index: Int) {
        if (_currentPlayingIndex.value == index && _isPlayingAudio.value) {
            pauseAudio()
        } else {
            playAudio(index)
        }
    }

    private fun playAudio(index: Int) {
        val list = _ayahs.value
        if (index < 0 || index >= list.size) return

        _currentPlayingIndex.value = index
        val item = list[index]

        mediaPlayer?.release()
        _isPlayingAudio.value = false

        if (item.audioUrl.isNotBlank()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(item.audioUrl)
                prepareAsync()
                setOnPreparedListener {
                    start()
                    _isPlayingAudio.value = true
                    updateLastRead(item.surahNumber, item.ayahNumber)
                }
                setOnCompletionListener {
                    _isPlayingAudio.value = false
                    // Auto advance to next verse in Surah!
                    val next = index + 1
                    if (next < list.size) {
                        playAudio(next)
                    } else {
                        _currentPlayingIndex.value = -1
                    }
                }
                setOnErrorListener { _, _, _ ->
                    _isPlayingAudio.value = false
                    _currentPlayingIndex.value = -1
                    true
                }
            }
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlayingAudio.value = false
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.let {
            it.stop()
            it.release()
        }
        mediaPlayer = null
        _isPlayingAudio.value = false
    }


    // === SENSORS COMPASS ACTIONS ===

    fun startCompass() {
        qiblaSensorManager.startListening()
    }

    fun stopCompass() {
        qiblaSensorManager.stopListening()
    }


    // === DIGITAL TASBIH ===

    fun incrementTasbih() {
        _tasbihCount.value++
        if (_tasbihCount.value > _tasbihTarget.value) {
            _tasbihCount.value = 1
        }
        viewModelScope.launch {
            repository.savePreference("pref_tasbih_cnt", _tasbihCount.value.toString())
        }
    }

    fun resetTasbih() {
        _tasbihCount.value = 0
        viewModelScope.launch {
            repository.savePreference("pref_tasbih_cnt", "0")
        }
    }

    fun updateTasbihTarget(target: Int) {
        _tasbihTarget.value = target
        viewModelScope.launch {
            repository.savePreference("pref_tasbih_target", target.toString())
        }
    }

    fun selectDhikr(index: Int) {
        _selectedDhikrIndex.value = index
        viewModelScope.launch {
            repository.savePreference("pref_selected_dhikr", index.toString())
        }
    }


    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        stopAudio()
        stopCompass()
    }
}
