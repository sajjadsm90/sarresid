package com.example.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object JalaliCalendar {

    data class JalaliDate(
        val year: Int,
        val month: Int,
        val day: Int
    ) {
        override fun toString(): String {
            return String.format("%04d/%02d/%02d", year, month, day)
        }

        fun formatPersian(includeMonthName: Boolean = false): String {
            if (!includeMonthName) {
                return toPersianDigits(toString())
            }
            val monthName = getPersianMonthName(month)
            return "${toPersianDigits(day)} $monthName ${toPersianDigits(year)}"
        }
    }

    private val persianMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    fun getPersianMonthName(month: Int): String {
        return if (month in 1..12) persianMonths[month - 1] else ""
    }

    fun toPersianDigits(input: Any?): String {
        val str = input?.toString() ?: return ""
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (c in str) {
            if (c in '0'..'9') {
                sb.append(persianDigits[c - '0'])
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun formatPrice(amount: Long): String {
        val formatted = String.format("%,d", amount)
        return toPersianDigits(formatted)
    }

    fun now(): JalaliDate {
        val today = LocalDate.now()
        return gregorianToJalali(today.year, today.monthValue, today.dayOfMonth)
    }

    fun gregorianToJalali(gYear: Int, gMonth: Int, gDay: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy = gYear - 1600
        var gm = gMonth - 1
        var gd = gDay - 1

        var gDayNo = 365 * gy + (gy + 3) / 4 - (gy + 99) / 100 + (gy + 399) / 400

        for (i in 0 until gm) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79

        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        for (i in 0..11) {
            if (jDayNo < jDaysInMonth[i]) {
                jm = i + 1
                break
            }
            jDayNo -= jDaysInMonth[i]
        }
        val jd = jDayNo + 1

        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jYear: Int, jMonth: Int, jDay: Int): LocalDate {
        var jy = jYear - 979
        var jm = jMonth - 1
        var jd = jDay - 1

        var jDayNo = 365 * jy + (jy / 33) * 8 + ((jy % 33 + 3) / 4)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        for (i in 0 until jm) {
            jDayNo += jDaysInMonth[i]
        }
        jDayNo += jd

        var gDayNo = jDayNo + 79

        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524

            if (gDayNo >= 365) {
                gDayNo++
            } else {
                leap = false
            }
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gDaysInMonth = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        for (i in 0..11) {
            if (gDayNo < gDaysInMonth[i]) {
                gm = i + 1
                break
            }
            gDayNo -= gDaysInMonth[i]
        }
        val gd = gDayNo + 1

        return LocalDate.of(gy, gm, gd)
    }

    fun parseJalali(dateStr: String): JalaliDate? {
        val cleaned = dateStr.replace(" ", "").replace("-", "/")
        val parts = cleaned.split("/")
        if (parts.size != 3) return null
        val y = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val d = parts[2].toIntOrNull() ?: return null
        return JalaliDate(y, m, d)
    }

    fun daysBetween(fromJalali: String, toJalali: String): Long {
        val from = parseJalali(fromJalali) ?: return 0
        val to = parseJalali(toJalali) ?: return 0
        val gFrom = jalaliToGregorian(from.year, from.month, from.day)
        val gTo = jalaliToGregorian(to.year, to.month, to.day)
        return java.time.temporal.ChronoUnit.DAYS.between(gFrom, gTo)
    }

    fun isLeapYear(year: Int): Boolean {
        // A simple way to check Jalali leap year is to check if Esfand has 30 days
        // Or check Gregorian date difference between 1 Farvardin of this year and next year
        val gStart = jalaliToGregorian(year, 1, 1)
        val gEnd = jalaliToGregorian(year + 1, 1, 1)
        return java.time.temporal.ChronoUnit.DAYS.between(gStart, gEnd) == 366L
    }

    fun getDaysInMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    /**
     * Returns 0 for Saturday (شنبه) to 6 for Friday (جمعه)
     */
    fun getFirstDayOfWeekInMonth(year: Int, month: Int): Int {
        val gDate = jalaliToGregorian(year, month, 1)
        return when (gDate.dayOfWeek) {
            java.time.DayOfWeek.SATURDAY -> 0
            java.time.DayOfWeek.SUNDAY -> 1
            java.time.DayOfWeek.MONDAY -> 2
            java.time.DayOfWeek.TUESDAY -> 3
            java.time.DayOfWeek.WEDNESDAY -> 4
            java.time.DayOfWeek.THURSDAY -> 5
            java.time.DayOfWeek.FRIDAY -> 6
        }
    }

    fun addMonths(jalaliDate: JalaliDate, monthsToAdd: Int): JalaliDate {
        var totalMonths = (jalaliDate.year * 12 + (jalaliDate.month - 1)) + monthsToAdd
        val newYear = totalMonths / 12
        val newMonth = (totalMonths % 12) + 1
        val maxDay = getDaysInMonth(newYear, newMonth)
        val newDay = jalaliDate.day.coerceAtMost(maxDay)
        return JalaliDate(newYear, newMonth, newDay)
    }
}
