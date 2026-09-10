package com.example.data

import com.example.model.DailyPrayerSchedule
import com.example.model.DailyRamadanDua
import com.example.model.HajjRitualInfo
import com.example.model.HijriDateDetails
import com.example.model.IslamicSeason
import com.example.model.RadioStation
import com.example.model.SeasonModeOverride
import com.example.model.SeasonStreamSource
import com.example.model.StationCategory
import com.example.model.TaraweehLiveStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.floor

/**
 * Islamic Season Calculation Engine.
 * Handles Ramadan, Hajj, Arafah, and Eid automatic detection,
 * Taraweeh timing, Iftar/Imsak countdowns, Hajj rituals guide, and authentic supplications.
 */
object IslamicSeasonEngine {

    val HIJRI_MONTHS_AR = listOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
        "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
        "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    /**
     * Compute exact Hijri Date components with an optional moon sighting offset (-2..+2).
     */
    fun computeHijriDate(calendar: Calendar, dayOffset: Int = 0): HijriDateDetails {
        val cal = (calendar.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
        }
        val gYear = cal.get(Calendar.YEAR)
        val gMonth = cal.get(Calendar.MONTH) + 1
        val gDay = cal.get(Calendar.DAY_OF_MONTH)

        val jd = julianDate(gYear, gMonth, gDay).toInt()
        val l = jd - 1948440 + 10632
        val n = ((l - 1) / 10631).toInt()
        val l2 = l - 10631 * n + 354
        val j = (((10985 - l2) / 5316).toInt()) * (((50 * l2) / 17719).toInt()) +
                ((l2 / 5670).toInt()) * (((43 * l2) / 15238).toInt())
        val l3 = l2 - (((30 - j) / 15).toInt()) * (((17719 * j) / 50).toInt()) -
                ((j / 16).toInt()) * (((15238 * j) / 43).toInt()) + 29
        val hMonth = ((24 * l3) / 709).toInt()
        val hDay = l3 - ((709 * hMonth) / 24).toInt()
        val hYear = (30 * n + j - 30).toInt()

        val safeMonth = hMonth.coerceIn(1, 12)
        val monthAr = HIJRI_MONTHS_AR.getOrElse(safeMonth - 1) { "رمضان" }
        val monthEn = HIJRI_MONTHS_EN.getOrElse(safeMonth - 1) { "Ramadan" }

        val formattedAr = "$hDay $monthAr $hYear هـ"

        return HijriDateDetails(
            day = hDay,
            month = safeMonth,
            year = hYear,
            monthNameAr = monthAr,
            monthNameEn = monthEn,
            formattedAr = formattedAr
        )
    }

    private fun julianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    /**
     * Resolve active Islamic season based on Hijri date and override setting.
     */
    fun resolveSeason(
        hijri: HijriDateDetails,
        overrideMode: SeasonModeOverride
    ): IslamicSeason {
        return when (overrideMode) {
            SeasonModeOverride.FORCE_RAMADAN -> IslamicSeason.RAMADAN
            SeasonModeOverride.FORCE_HAJJ -> IslamicSeason.HAJJ
            SeasonModeOverride.FORCE_ARAFAH -> IslamicSeason.ARAFAH
            SeasonModeOverride.FORCE_EID -> if (hijri.month == 10) IslamicSeason.EID_FITR else IslamicSeason.EID_ADHA
            SeasonModeOverride.DISABLED -> IslamicSeason.NORMAL
            SeasonModeOverride.AUTO -> {
                when {
                    // Ramadan: Month 9
                    hijri.month == 9 -> IslamicSeason.RAMADAN
                    // Shawwal 1..3: Eid Al-Fitr
                    hijri.month == 10 && hijri.day in 1..3 -> IslamicSeason.EID_FITR
                    // Dhu al-Hijjah 9: Day of Arafah
                    hijri.month == 12 && hijri.day == 9 -> IslamicSeason.ARAFAH
                    // Dhu al-Hijjah 10..13: Eid Al-Adha & Tashreeq
                    hijri.month == 12 && hijri.day in 10..13 -> IslamicSeason.EID_ADHA
                    // Dhu al-Hijjah 1..13: Hajj Season
                    hijri.month == 12 && hijri.day in 1..13 -> IslamicSeason.HAJJ
                    else -> IslamicSeason.NORMAL
                }
            }
        }
    }

    /**
     * Check Taraweeh / Tahajjud status based on local prayer schedule or standard time.
     */
    fun calculateTaraweehStatus(
        dailySchedule: DailyPrayerSchedule?,
        currentTimeMillis: Long
    ): Pair<TaraweehLiveStatus, Long> {
        val ishaTime = dailySchedule?.isha?.timeInMillis ?: run {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
            }
            cal.timeInMillis
        }

        // Taraweeh starts 15 minutes after Isha and lasts for ~ 2.5 hours
        val taraweehStart = ishaTime + (15 * 60 * 1000L)
        val taraweehEnd = taraweehStart + (150 * 60 * 1000L)

        return when {
            currentTimeMillis < taraweehStart -> {
                val diff = (taraweehStart - currentTimeMillis).coerceAtLeast(0L)
                TaraweehLiveStatus.UPCOMING to diff
            }
            currentTimeMillis in taraweehStart..taraweehEnd -> {
                val diff = (taraweehEnd - currentTimeMillis).coerceAtLeast(0L)
                TaraweehLiveStatus.LIVE_NOW to diff
            }
            else -> {
                // If it passed midnight, upcoming next is tomorrow's Taraweeh
                val tomorrowStart = taraweehStart + (24 * 3600 * 1000L)
                val diff = (tomorrowStart - currentTimeMillis).coerceAtLeast(0L)
                TaraweehLiveStatus.COMPLETED to diff
            }
        }
    }

    /**
     * Calculate Suhoor / Imsak (15 mins prior to Fajr) & Last Third of Night (Thuluth Al-Layl).
     */
    fun calculateNightMilestones(
        dailySchedule: DailyPrayerSchedule?,
        currentTimeMillis: Long
    ): NightMilestones {
        val maghribMillis = dailySchedule?.maghrib?.timeInMillis ?: run {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 18)
                set(Calendar.MINUTE, 30)
            }
            cal.timeInMillis
        }
        val fajrMillis = dailySchedule?.fajr?.timeInMillis ?: run {
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 4)
                set(Calendar.MINUTE, 30)
            }
            cal.timeInMillis
        }

        // Imsak is 15 minutes before Fajr
        val imsakMillis = fajrMillis - (15 * 60 * 1000L)

        // Night span calculation between Maghrib and Fajr
        val nextFajr = if (fajrMillis < maghribMillis) fajrMillis + (24 * 3600 * 1000L) else fajrMillis
        val nightDuration = (nextFajr - maghribMillis).coerceAtLeast(1L)
        val lastThirdStart = maghribMillis + (2 * nightDuration / 3)

        val iftarDiff = (maghribMillis - currentTimeMillis).coerceAtLeast(0L)
        val imsakDiff = (imsakMillis - currentTimeMillis).coerceAtLeast(0L)
        val lastThirdDiff = (lastThirdStart - currentTimeMillis).coerceAtLeast(0L)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        return NightMilestones(
            millisUntilIftar = iftarDiff,
            millisUntilImsak = imsakDiff,
            millisUntilLastThird = lastThirdDiff,
            iftarFormatted = timeFormat.format(Date(maghribMillis)),
            imsakFormatted = timeFormat.format(Date(imsakMillis)),
            lastThirdFormatted = timeFormat.format(Date(lastThirdStart))
        )
    }

    data class NightMilestones(
        val millisUntilIftar: Long,
        val millisUntilImsak: Long,
        val millisUntilLastThird: Long,
        val iftarFormatted: String,
        val imsakFormatted: String,
        val lastThirdFormatted: String
    )

    /**
     * Get specific Hajj Ritual Info for days 1..13 Dhul-Hijjah.
     */
    fun getHajjRitualForDay(day: Int): HajjRitualInfo {
        return when (day) {
            8 -> HajjRitualInfo(
                dayNumber = 8,
                dayNameAr = "يوم التروية",
                locationAr = "مشعر منى المبارك",
                summaryAr = "الإحرام والتوجه إلى منى والمبيت بها وصلاة الصلوات قصراً بلا جمع، والإكثار من التلبية.",
                detailsAr = "يسن للحاج في اليوم الثامن من ذي الحجة الإحرام بالحج من مكانه، ثم التوجه إلى مشعر منى للمبيت بها وصلاة الظهر والعصر والمغرب والعشاء وفجر يوم عرفة قصراً بلا جمع، مع الإكثار من التلبية: (لبيك اللهم لبيك).",
                keyDuaAr = "لَبَّيْكَ اللَّهُمَّ لَبَّيْكَ، لَبَّيْكَ لا شَرِيكَ لَكَ لَبَّيْكَ، إِنَّ الْحَمْدَ وَالنِّعْمَةَ لَكَ وَالْمُلْكَ، لا شَرِيكَ لَكَ.",
                talbiyahTextAr = "لبيك اللهم لبيك، لبيك لا شريك لك لبيك"
            )
            9 -> HajjRitualInfo(
                dayNumber = 9,
                dayNameAr = "يوم عرفة المبارك (الحج عرفة)",
                locationAr = "صعيد عرفات الطاهر",
                summaryAr = "أعظم أيام السنة، الوقوف بعرفة، الاستماع لخطبة عرفة، والجمع والقصر، والتضرع بالدعاء حتى غروب الشمس ثم النفرة لمزدلفة.",
                detailsAr = "يتوجه الحجاج بعد شروق شمس التاسع من ذي الحجة إلى صعيد عرفات، ويصلون الظهر والعصر جمع تقديم وقصراً بأذان وإقامتين، ويقضون النهار في التضرع والاستغفار وذكر الله وخير الدعاء حتى تغرب الشمس، ثم ينفرون بسكينة إلى مزدلفة للمبيت وجمع الحصى.",
                keyDuaAr = "لا إِلَهَ إِلا اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.",
                talbiyahTextAr = "خير الدعاء دعاء يوم عرفة",
                isArafah = true
            )
            10 -> HajjRitualInfo(
                dayNumber = 10,
                dayNameAr = "يوم النحر (عيد الأضحى المبارك)",
                locationAr = "مشعر منى ومكة المكرمة",
                summaryAr = "رمي جمرة العقبة الكبرى بسبع حصيات، ذبح الهدي، الحلق أو التقصير، ثم طواف الإفاضة والسعي بالمسجد الحرام.",
                detailsAr = "يوم الحج الأكبر؛ بعد صلاة فجر يوم النحر بمزدلفة وذكر الله عند المشعر الحرام، يتوجه الحجاج لمنى لرمي جمرة العقبة الكبرى بـ 7 حصيات والتكبير مع كل حصاة، ثم ذبح الهدي، والحلق أو التقصير ليتحلل التحلل الأول، ثم التوجه للكعبة لطواف الإفاضة والسعي.",
                keyDuaAr = "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، لا إِلَهَ إِلا اللَّهُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، وَلِلَّهِ الْحَمْدُ.",
                talbiyahTextAr = "الله أكبر كبيراً والحمد لله كثيراً",
                isEidDay = true
            )
            11 -> HajjRitualInfo(
                dayNumber = 11,
                dayNameAr = "أول أيام التشريق (يوم القر)",
                locationAr = "مشعر منى",
                summaryAr = "المبيت بمنى ورمي الجمرات الثلاث (الصغرى فالوسطى فالكبرى) بعد الزوال، والتكبير والدعاء.",
                detailsAr = "يقيم الحجاج بمشعر منى بذكر الله وشكره، ويرمون بعد زوال الشمس الجمرات الثلاث بالتتابع: الجمرة الصغرى بـ 7 حصيات ثم الدعاء طويلاً، ثم الوسطى بـ 7 حصيات والدعاء، ثم جمرة العقبة الكبرى بـ 7 حصيات.",
                keyDuaAr = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ.",
                talbiyahTextAr = "واذكروا الله في أيام معدودات"
            )
            12 -> HajjRitualInfo(
                dayNumber = 12,
                dayNameAr = "ثاني أيام التشريق (يوم النفر الأول)",
                locationAr = "مشعر منى",
                summaryAr = "رمي الجمرات الثلاث بعد الزوال. يجوز للمتعجلين مغادرة منى قبل غروب الشمس والتوجه لطواف الوداع.",
                detailsAr = "يرمي الحجاج الجمرات الثلاث كما في اليوم الأول، ومن تعجل فله الخروج من منى قبل مغيب الشمس والتوجه للمسجد الحرام لأداء طواف الوداع وإتمام مناسكه: (فَمَن تَعَجَّلَ فِي يَوْمَيْنِ فَلَا إِثْمَ عَلَيْهِ).",
                keyDuaAr = "اللَّهُمَّ إِنِّي أَسْأَلُكَ حَجّاً مَبْرُوراً، وَسَعْياً مَشْكُوراً، وَذَنْباً مَغْفُوراً.",
                talbiyahTextAr = "حج مبرور وسعي مشكور وذنب مغفور"
            )
            13 -> HajjRitualInfo(
                dayNumber = 13,
                dayNameAr = "ثالث أيام التشريق (يوم النفر الثاني)",
                locationAr = "مشعر منى ومكة المكرمة",
                summaryAr = "للمتأخرين: رمي الجمرات الثلاث بعد الزوال ثم النفر النهائي إلى المسجد الحرام لطواف الوداع.",
                detailsAr = "لغير المتعجلين الذين بقوا في منى، يرمون الجمرات الثلاث بعد الزوال، ثم يفيضون إلى المسجد الحرام لأداء طواف الوداع، ليكون آخر عهدهم بالبيت الحرام صلاةً وطوافاً، وتم بحمد الله الحج.",
                keyDuaAr = "اللَّهُمَّ زِدْ هَذَا الْبَيْتَ تَشْرِيفاً وَتَعْظِيماً وَتَكْرِيماً وَمَهَابَةً، وَزِدْ مَنْ شَرَّفَهُ وَعَظَّمَهُ مِمَّنْ حَجَّهُ أَوِ اعْتَمَرَهُ تَشْرِيفاً وَتَكْرِيماً وَبِرّاً.",
                talbiyahTextAr = "الحمد لله الذي بنعمته تتم الصالحات"
            )
            else -> HajjRitualInfo(
                dayNumber = day,
                dayNameAr = "عشر ذي الحجة المباركة (يوم $day)",
                locationAr = "مكة المكرمة والمشاعر المقدسة",
                summaryAr = "أيام العمل الصالح التي أقسم الله بها في كتابه العزيز، يسن فيها الإكثار من التهليل والتكبير والتحميد والصيام والصدقة.",
                detailsAr = "قال رسول الله صلى الله عليه وسلم: (ما من أيام العمل الصالح فيها أحب إلى الله من هذه الأيام - يعني أيام العشر)، يسن فيها التكبير المطلق والاستعداد للحج وصيام غير الحاج.",
                keyDuaAr = "اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، لا إِلَهَ إِلا اللَّهُ، اللَّهُ أَكْبَرُ، اللَّهُ أَكْبَرُ، وَلِلَّهِ الْحَمْدُ.",
                talbiyahTextAr = "لبيك اللهم لبيك"
            )
        }
    }

    /**
     * Authentic Daily Supplications for the 30 days of Ramadan.
     */
    val RAMADAN_DUAS = listOf(
        DailyRamadanDua(
            day = 1,
            title = "دعاء رؤية الهلال ودخول الشهر",
            arabicText = "اللَّهُمَّ أَهِلَّهُ عَلَيْنَا بِالأَمْنِ وَالإِيمَانِ، وَالسَّلامَةِ وَالإِسْلامِ، وَالتَّوْفِيقِ لِمَا تُحِبُّ وَتَرْضَى، رَبُّنَا وَرَبُّكَ اللَّهُ.",
            translation = "O Allah, bring it over us with security and faith, safety and Islam, and success in what You love and are pleased with.",
            reference = "سنن الترمذي وحسنه الألباني",
            virtue = "دعاء مأثور عن النبي ﷺ عند رؤية هلال أول الشهر"
        ),
        DailyRamadanDua(
            day = 2,
            title = "دعاء الإفطار وذهاب الظمأ",
            arabicText = "ذَهَبَ الظَّمَأُ، وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الأَجْرُ إِنْ شَاءَ اللَّهُ.",
            translation = "The thirst has gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
            reference = "سنن أبي داود",
            virtue = "دعاء النبي ﷺ عند الفطر من الصيام"
        ),
        DailyRamadanDua(
            day = 3,
            title = "دعاء المغفرة والرحمة",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ بِرَحْمَتِكَ الَّتِي وَسِعَتْ كُلَّ شَيْءٍ أَنْ تَغْفِرَ لِي.",
            translation = "O Allah, I ask You by Your mercy which encompasses all things, that You forgive me.",
            reference = "سنن ابن ماجه",
            virtue = "دعاء مستجاب عند الفطر ثبت عن عبد الله بن عمرو رضي الله عنهما"
        ),
        DailyRamadanDua(
            day = 4,
            title = "دعاء العفو والتجاوز",
            arabicText = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي.",
            translation = "O Allah, You are Most Forgiving, and You love forgiveness; so forgive me.",
            reference = "جامع الترمذي وصححه",
            virtue = "وصية النبي ﷺ لأم المؤمنين عائشة رضي الله عنها"
        ),
        DailyRamadanDua(
            day = 5,
            title = "دعاء قبول الصيام والقيام",
            arabicText = "رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنتَ السَّمِيعُ الْعَلِيمُ، وَتُبْ عَلَيْنَا إِنَّكَ أَنتَ التَّوَّابُ الرَّحِيمُ.",
            translation = "Our Lord, accept from us; indeed You are the Hearing, the Knowing.",
            reference = "سورة البقرة: 127-128",
            virtue = "دعاء خليل الرحمن إبراهيم وإسماعيل عليهما السلام"
        ),
        DailyRamadanDua(
            day = 6,
            title = "دعاء سعة الرزق والبركة",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْماً نَافِعاً، وَرِزْقاً طَيِّباً، وَعَمَلاً مُتَقَبَّلاً.",
            translation = "O Allah, I ask You for beneficial knowledge, good provision, and accepted deeds.",
            reference = "سنن ابن ماجه",
            virtue = "دعاء نبوي مبارك يبدأ به المسلم يومه"
        ),
        DailyRamadanDua(
            day = 7,
            title = "دعاء الثبات على الدين",
            arabicText = "يَا مُقَلِّبَ الْقُلُوبِ ثَبِّتْ قَلْبِي عَلَى دِينِكَ، وَيَا مُصَرِّفَ الْقُلُوبِ صَرِّفْ قُلُوبَنَا عَلَى طَاعَتِكَ.",
            translation = "O Turner of the hearts, keep my heart firm upon Your religion.",
            reference = "صحيح مسلم والترمذي",
            virtue = "من أكثر أدعية النبي ﷺ"
        ),
        DailyRamadanDua(
            day = 8,
            title = "دعاء كشف الهم وقضاء الدين",
            arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْجُبْنِ وَالْبُخْلِ، وَضَلَعِ الدَّيْنِ وَغَلَبَةِ الرِّجَالِ.",
            translation = "O Allah, I seek refuge with You from grief and sadness, from weakness and laziness...",
            reference = "صحيح البخاري",
            virtue = "دعاء النبي ﷺ لتفريج الكروب والهموم"
        ),
        DailyRamadanDua(
            day = 9,
            title = "دعاء الهداية والتقوى",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى.",
            translation = "O Allah, I ask You for guidance, piety, chastity and self-sufficiency.",
            reference = "صحيح مسلم",
            virtue = "من جوامع دعاء المصطفى ﷺ"
        ),
        DailyRamadanDua(
            day = 10,
            title = "دعاء العتق من النيران",
            arabicText = "اللَّهُمَّ أَعْتِقْ رِقَابَنَا وَرِقَابَ آبَائِنَا وَأُمَّهَاتِنَا وَذُرِّيَّاتِنَا مِنَ النَّارِ يَا رَبَّ الْعَالَمِينَ.",
            translation = "O Allah, emancipate our necks and the necks of our parents and descendants from the Fire.",
            reference = "مأثور في فضل الليالي الرمضانية",
            virtue = "إن لله في كل ليلة عتقاء من النار"
        ),
        DailyRamadanDua(
            day = 11,
            title = "دعاء صلاح الشأن كله",
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ، وَلا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ.",
            translation = "O Ever Living, O Sustainer, by Your mercy I seek help; rectify all my affairs.",
            reference = "سنن الترمذي وصحيح الجامع",
            virtue = "من أعظم أذكار الصباح والمساء"
        ),
        DailyRamadanDua(
            day = 12,
            title = "دعاء النور والهداية التامة",
            arabicText = "اللَّهُمَّ اجْعَلْ فِي قَلْبِي نُوراً، وَفِي بَصَرِي نُوراً، وَفِي سَمْعِي نُوراً، وَعَنْ يَمِينِي نُوراً، وَعَنْ يَسَارِي نُوراً، وَفَوْقِي نُوراً، وَتَحْتِي نُوراً.",
            translation = "O Allah, place light in my heart, light in my sight, light in my hearing...",
            reference = "صحيح مسلم",
            virtue = "دعاء النبي ﷺ في صلاة الليل والتهجد"
        ),
        DailyRamadanDua(
            day = 13,
            title = "دعاء الاستغفار وسيد الاستغفار",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَهَ إِلا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لا يَغْفِرُ الذُّنُوبَ إِلا أَنْتَ.",
            translation = "O Allah, You are my Lord; none has the right to be worshipped but You...",
            reference = "صحيح البخاري",
            virtue = "سيد الاستغفار من قاله موقناً به ومات دخل الجنة"
        ),
        DailyRamadanDua(
            day = 14,
            title = "دعاء الخشوع وحفظ القلب",
            arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عِلْمٍ لا يَنْفَعُ، وَمِنْ قَلْبٍ لا يَخْشَعُ، وَمِنْ نَفْسٍ لا تَشْبَعُ، وَمِنْ دَعْوَةٍ لا يُسْتَجَابُ لَهَا.",
            translation = "O Allah, I seek refuge with You from knowledge that does not benefit, from a heart that is not humble...",
            reference = "صحيح مسلم",
            virtue = "دعاء النبي ﷺ للسلامة من آفات العبادة"
        ),
        DailyRamadanDua(
            day = 15,
            title = "دعاء الانتصاف وبلوغ النعيم",
            arabicText = "اللَّهُمَّ قَسِّمْ لَنَا مِنْ خَشْيَتِكَ مَا يَحُولُ بَيْنَنَا وَبَيْنَ مَعَاصِيكَ، وَمِنْ طَاعَتِكَ مَا تُبَلِّغُنَا بِهِ جَنَّتَكَ، وَمِنَ الْيَقِينِ مَا تُهَوِّنُ بِهِ عَلَيْنَا مَصَائِبَ الدُّنْيَا.",
            translation = "O Allah, apportion to us of Your fear that which will stand between us and disobedience to You...",
            reference = "جامع الترمذي",
            virtue = "دعاء النبي ﷺ الجامع لخير الدنيا والآخرة"
        ),
        DailyRamadanDua(
            day = 16,
            title = "دعاء حسن الخاتمة والوفاة على الإسلام",
            arabicText = "رَبَّنَا أَفْرِغْ عَلَيْنَا صَبْراً وَتَوَفَّنَا مُسْلِمِينَ، وَأَلْحِقْنَا بِالصَّالِحِينَ غَيْرَ خَزَايَا وَلا مَفْتُونِينَ.",
            translation = "Our Lord, pour upon us patience and cause us to die as Muslims.",
            reference = "سورة الأعراف: 126",
            virtue = "دعاء قرآني جامع للثبات حتى الممات"
        ),
        DailyRamadanDua(
            day = 17,
            title = "دعاء يوم بدر والظفر بالحق",
            arabicText = "اللَّهُمَّ أَنْجِزْ لَنَا مَا وَعَدْتَنَا، اللَّهُمَّ انْصُرِ الإِسْلامَ وَأَعِزَّ الْمُسْلِمِينَ، وَانْصُرْ إِخْوَانَنَا الْمُسْتَضْعَفِينَ فِي كُلِّ مَكَانٍ.",
            translation = "O Allah, fulfill what You have promised us; give victory to Islam and Muslims.",
            reference = "مستوحى من دعاء النبي ﷺ يوم الفرقان (بدر)",
            virtue = "ذكرى غزوة بدر الكبرى (17 رمضان)"
        ),
        DailyRamadanDua(
            day = 18,
            title = "دعاء شكر النعم والتوفيق للطاعات",
            arabicText = "رَبِّ أَوْزِعْنِي أَنْ أَشْكُرَ نِعْمَتَكَ الَّتِي أَنْعَمْتَ عَلَيَّ وَعَلَى وَالِدَيَّ وَأَنْ أَعْمَلَ صَالِحاً تَرْضَاهُ وَأَدْخِلْنِي بِرَحْمَتِكَ فِي عِبَادِكَ الصَّالِحِينَ.",
            translation = "My Lord, enable me to be grateful for Your favor which You have bestowed upon me...",
            reference = "سورة النمل: 19",
            virtue = "دعاء نبي الله سليمان عليه السلام"
        ),
        DailyRamadanDua(
            day = 19,
            title = "دعاء الاستعداد للعشر الأواخر",
            arabicText = "اللَّهُمَّ بَلِّغْنَا لَيْلَةَ الْقَدْرِ، وَاجْعَلْنَا فِيهَا مِنَ الْمَقْبُولِينَ الْفَائِزِينَ، وَوَفِّقْنَا فِيمَا بَقِيَ مِنْ شَهْرِنَا لِصَالِحِ الأَعْمَالِ.",
            translation = "O Allah, let us reach Laylat al-Qadr, and make us among those accepted and triumphant.",
            reference = "مأثور السلف الصالح",
            virtue = "التهيؤ لنفحات العشر الأواخر المباركة"
        ),
        DailyRamadanDua(
            day = 20,
            title = "دعاء شد المئزر والاجتهاد",
            arabicText = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ، اللَّهُمَّ اجْعَلْنَا مِنْ عُتَقَائِكَ مِنَ النَّارِ.",
            translation = "O Allah, help me to remember You, thank You, and worship You in the best manner.",
            reference = "سنن أبي داود والنسائي",
            virtue = "وصية النبي ﷺ لمعاذ بن جبل رضي الله عنه"
        ),
        DailyRamadanDua(
            day = 21,
            title = "دعاء ليلة القدر الأولى (العشر الأواخر)",
            arabicText = "اللَّهُمَّ إِنَّكَ عَفُوٌّ كَرِيمٌ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي، اللَّهُمَّ اكْتُبْنَا فِي هَذِهِ اللَّيَالِي الْمُبَارَكَةِ مِنْ سُعَدَاءِ الدَّارَيْنِ.",
            translation = "O Allah, You are Most Forgiving and Generous, You love to forgive, so forgive me.",
            reference = "سنن الترمذي",
            virtue = "أولى الليالي الوترية من العشر الأواخر"
        ),
        DailyRamadanDua(
            day = 22,
            title = "دعاء تفريج الكرب وسؤال الجنة",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْجَنَّةَ وَمَا قَرَّبَ إِلَيْهَا مِنْ قَوْلٍ أَوْ عَمَلٍ، وَأَعُوذُ بِكَ مِنَ النَّارِ وَمَا قَرَّبَ إِلَيْهَا مِنْ قَوْلٍ أَوْ عَمَلٍ.",
            translation = "O Allah, I ask You for Paradise and whatever brings me closer to it of word or deed...",
            reference = "سنن ابن ماجه وأحمد",
            virtue = "دعاء نبوي جامع للفوز بالجنة والنجاة من النار"
        ),
        DailyRamadanDua(
            day = 23,
            title = "دعاء الليلة الوترية الثانية",
            arabicText = "اللَّهُمَّ لا تَدَعْ لَنَا فِي هَذِهِ اللَّيْلَةِ الْمُبَارَكَةِ ذَنْباً إِلا غَفَرْتَهُ، وَلا هَمّاً إِلا فَرَّجْتَهُ، وَلا دَيْناً إِلا قَضَيْتَهُ، وَلا مَرِيضاً إِلا شَفَيْتَهُ.",
            translation = "O Allah, do not leave for us any sin unforgotten, nor any worry unrelieved...",
            reference = "مأثور في دعاء القنوت والتهجد",
            virtue = "ليلة وترية يرجى فيها موافقة ليلة القدر"
        ),
        DailyRamadanDua(
            day = 24,
            title = "دعاء الوالدين والأهل والذرية",
            arabicText = "رَبَّنَا اغْفِرْ لِي وَلِوَالِدَيَّ وَلِلْمُؤْمِنِينَ يَوْمَ يَقُومُ الْحِسَابُ، رَبِّ ارْحَمْهُمَا كَمَا رَبَّيَانِي صَغِيراً.",
            translation = "Our Lord, forgive me and my parents and the believers the Day the account is established.",
            reference = "سورة إبراهيم: 41 والإسراء: 24",
            virtue = "بر الوالدين بالدعاء الصالح في أقدس الليالي"
        ),
        DailyRamadanDua(
            day = 25,
            title = "دعاء ليلة 25 وتحري ليلة القدر",
            arabicText = "اللَّهُمَّ اجْعَلْنَا مِمَّنْ قَامَ لَيْلَةَ الْقَدْرِ إِيمَاناً وَاحْتِسَاباً فَغُفِرَ لَهُ مَا تَقَدَّمَ مِنْ ذَنْبِهِ، اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنَّا.",
            translation = "O Allah, make us among those who stand on Laylat al-Qadr with faith and seeking reward...",
            reference = "متفق عليه",
            virtue = "من قام ليلة القدر إيماناً واحتساباً غفر له ما تقدم من ذنبه"
        ),
        DailyRamadanDua(
            day = 26,
            title = "دعاء الطمأنينة وحفظ الإيمان",
            arabicText = "اللَّهُمَّ أَلِّفْ بَيْنَ قُلُوبِنَا، وَأَصْلِحْ ذَاتَ بَيْنِنَا، وَاهْدِنَا سُبُلَ السَّلامِ، وَنَجِّنَا مِنَ الظُّلُمَاتِ إِلَى النُّورِ.",
            translation = "O Allah, reconcile our hearts, resolve our disputes, guide us to paths of peace...",
            reference = "سنن أبي داود والحاكم",
            virtue = "دعاء للمحبة والألفة بين المؤمنين"
        ),
        DailyRamadanDua(
            day = 27,
            title = "دعاء ليلة 27 (أرجى ليالي القدر)",
            arabicText = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي، اللَّهُمَّ يَا سَمِيعَ الدُّعَاءِ يَا بَارِئَ النَّسَمِ، انْشُرْ عَلَيْنَا رَحْمَتَكَ وَمَغْفِرَتَكَ وَرِضْوَانَكَ، وَاكْتُبْنَا مِنَ الْفَائِزِينَ بِجَنَّاتِ النَّعِيمِ.",
            translation = "O Allah, You are Most Forgiving, loving forgiveness, so forgive me...",
            reference = "أرجى ليالي القدر عند جمهور الصحابة",
            virtue = "ليلة سبع وعشرين التي كان أبي بن كعب يحلف أنها ليلة القدر"
        ),
        DailyRamadanDua(
            day = 28,
            title = "دعاء الرضا وتيسير الأمور",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الرِّضَا بَعْدَ الْقَضَاءِ، وَبَرْدَ الْعَيْشِ بَعْدَ الْمَوْتِ، وَلَذَّةَ النَّظَرِ إِلَى وَجْهِكَ الْكَرِيمِ، وَالشَّوْقَ إِلَى لِقَائِكَ فِي غَيْرِ ضَرَّاءَ مُضِرَّةٍ وَلا فِتْنَةٍ مُضِلَّةٍ.",
            translation = "O Allah, I ask You for pleasure after judgment, the delight of living after death, and the pleasure of looking at Your Face...",
            reference = "سنن النسائي وأحمد",
            virtue = "من أجمع وأعذب الأدعية النبوية"
        ),
        DailyRamadanDua(
            day = 29,
            title = "دعاء ختام الشهر المبارك",
            arabicText = "اللَّهُمَّ لا تَجْعَلْهُ آخِرَ الْعَهْدِ مِنْ صِيَامِنَا إِيَّاهُ، فَإِنْ جَعَلْتَهُ فَاجْعَلْنِي مَرْحُوماً وَلا تَجْعَلْنِي مَحْرُوماً، وَأَعِدْهُ عَلَيْنَا أَعْوَاماً عَدِيدَةً وَأَزْمِنَةً مَدِيدَةً وَنَحْنُ فِي صِحَّةٍ وَعَافِيَةٍ وَإِيمَانٍ.",
            translation = "O Allah, do not make this the last of our fasting, and return it to us for many years in good health and faith.",
            reference = "مأثور ختام الشهر عند العلماء",
            virtue = "دعاء توديع رمضان وسؤال القبول وإدراكه أعواماً مديدة"
        ),
        DailyRamadanDua(
            day = 30,
            title = "دعاء ليلة الفطر وشكر التمام",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي هَدَانَا لِهَذَا وَمَا كُنَّا لِنَهْتَدِيَ لَوْلا أَنْ هَدَانَا اللَّهُ، اللَّهُمَّ تَقَبَّلْ صِيَامَنَا وَقِيَامَنَا وَصَالِحَ أَعْمَالِنَا، وَاجْعَلْ عِيدَنَا فَرَحاً وَسُرُوراً وَمَغْفِرَةً.",
            translation = "Praise be to Allah, who has guided us to this... O Allah accept our fasting and prayers.",
            reference = "سورة الأعراف: 43 ومأثور العيد",
            virtue = "شكر الله على التمام واستقبال بهجة العيد"
        )
    )

    fun getDuaForDay(day: Int): DailyRamadanDua {
        val safeDay = day.coerceIn(1, 30)
        return RAMADAN_DUAS.getOrElse(safeDay - 1) { RAMADAN_DUAS[0] }
    }

    /**
     * Dedicated Official Stations for Makkah, Madinah, Talbiyah, and Taraweeh live stream.
     */
    val MAKKAH_LIVE_STATION = RadioStation(
        id = "stream_makkah_live",
        name = "بث مباشر الحرم المكي الشريف (صلاة التراويح ومناسك الحج)",
        country = "المملكة العربية السعودية",
        countryCode = "SA",
        city = "مكة المكرمة",
        category = StationCategory.OFFICIAL,
        streamUrl = "https://backup.qurango.net/radio/mix",
        fallbackStreamUrl = "https://qurango.net/radio/mix",
        logoUrl = "https://images.unsplash.com/photo-1565552645632-d725f8bfc19a?w=600&q=80",
        description = "البث المباشر لصلاة التراويح والتهجد من المسجد الحرام بمكة المكرمة، ونقل حي لمناسك الحج ويوم عرفة.",
        bitrate = "128 kbps",
        isFeatured = true,
        priority = 200
    )

    val MADINAH_LIVE_STATION = RadioStation(
        id = "stream_madinah_live",
        name = "بث مباشر الحرم النبوي الشريف (التراويح والزيارة)",
        country = "المملكة العربية السعودية",
        countryCode = "SA",
        city = "المدينة المنورة",
        category = StationCategory.OFFICIAL,
        streamUrl = "https://backup.qurango.net/radio/saheh-bokharee",
        fallbackStreamUrl = "https://qurango.net/radio/saheh-bokharee",
        logoUrl = "https://images.unsplash.com/photo-1590076215667-875d4ef2d7ee?w=600&q=80",
        description = "البث الحي المباشر من المسجد النبوي الشريف بالمدينة المنورة لصلاة التراويح والصلوات الخمس.",
        bitrate = "128 kbps",
        isFeatured = true,
        priority = 199
    )

    val HAJJ_TALBIYAH_STATION = RadioStation(
        id = "stream_hajj_talbiyah",
        name = "تلبية الحج وتكبيرات العيد (لبيك اللهم لبيك)",
        country = "العالم الإسلامي",
        countryCode = "SA",
        city = "المشاعر المقدسة",
        category = StationCategory.ADHKAR,
        streamUrl = "https://backup.qurango.net/radio/eid",
        fallbackStreamUrl = "https://qurango.net/radio/eid",
        logoUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=600&q=80",
        description = "بث مستمر لتلبية الحجاج (لبيك اللهم لبيك) وتكبيرات عشر ذي الحجة وعيد الأضحى المبارك بصوت عذب.",
        bitrate = "128 kbps",
        isFeatured = true,
        priority = 205
    )

    val CAIRO_LIVE_STATION = RadioStation(
        id = "stream_cairo_ramadan",
        name = "إذاعة القرآن الكريم من القاهرة (بث التراويح والأدعية)",
        country = "مصر",
        countryCode = "EG",
        city = "القاهرة",
        category = StationCategory.OFFICIAL,
        streamUrl = "https://service.webvideocore.net/CL1olYogIrDWvwqiIKK7eCxOS4PStqG9DuEjAr2ZjZQtvS3d4y9r0cvRhvS17SGN/a_7a4vuubc6mo8.m3u8",
        fallbackStreamUrl = "https://stream.radiojar.com/8s5u5tpdtwzuv",
        logoUrl = "https://images.unsplash.com/photo-1591604129939-f1efa4d9f7fa?w=400&q=80",
        description = "نقل مباشر لصلاة التراويح والتهجد من المساجد الكبرى بالقاهرة وابتهالات رمضانية نادرة.",
        bitrate = "128 kbps",
        isFeatured = true,
        priority = 195
    )

    fun getStationForSource(source: SeasonStreamSource): RadioStation {
        return when (source) {
            SeasonStreamSource.MAKKAH -> MAKKAH_LIVE_STATION
            SeasonStreamSource.MADINAH -> MADINAH_LIVE_STATION
            SeasonStreamSource.CAIRO -> CAIRO_LIVE_STATION
        }
    }
}
