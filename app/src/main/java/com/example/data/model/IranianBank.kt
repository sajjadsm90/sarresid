package com.example.data.model

data class IranianBank(
    val name: String,
    val code: String,
    val ussd: String = "",
    val website: String = "",
    val phone: String = "",
    val cardPrefix: List<String> = emptyList()
)

object IranianBankList {
    val banks = listOf(
        IranianBank("بانک ملی ایران", "BMI", "*733#", "https://bmi.ir", "02164140", listOf("603799")),
        IranianBank("بانک ملت", "BML", "*724#", "https://bankmellat.ir", "0211556", listOf("610433", "991975")),
        IranianBank("بانک صادرات ایران", "BSI", "*719#", "https://bsi.ir", "09602", listOf("603769")),
        IranianBank("بانک تجارت", "BTE", "*788#", "https://tejaratbank.ir", "1554", listOf("585983", "627353")),
        IranianBank("بانک سپه", "SEP", "*770#", "https://banksepah.ir", "1557", listOf("589210")),
        IranianBank("بانک پاسارگاد", "BPA", "*720#", "https://bpi.ir", "02182890", listOf("502229", "639347")),
        IranianBank("بانک سامان", "BSM", "*724#", "https://sb24.ir", "0216422", listOf("621986")),
        IranianBank("بانک پارسیان", "BPR", "*708#", "https://parsian-bank.ir", "02189111", listOf("622106", "627884")),
        IranianBank("بانک کشاورزی", "BKI", "*730#", "https://bki.ir", "02181301", listOf("603770", "639217")),
        IranianBank("بانک مسکن", "BMK", "*737#", "https://bank-maskan.ir", "02161088", listOf("628023")),
        IranianBank("بانک آینده", "BAY", "*740#", "https://ba24.ir", "02127663200", listOf("636214")),
        IranianBank("بانک شهر", "BSH", "*787#", "https://shahr-bank.ir", "02183386", listOf("504706", "502806")),
        IranianBank("بانک رفاه کارگران", "BRK", "*713#", "https://refah-bank.ir", "0218525", listOf("589463")),
        IranianBank("بانک سینا", "BSN", "*727#", "https://sinabank.ir", "02141704", listOf("639346")),
        IranianBank("بانک رسالت", "BRS", "", "https://rqbank.ir", "0214747", listOf("504172")),
        IranianBank("بانک مهر ایران", "QMB", "", "https://qmb.ir", "0214322", listOf("606373")),
        IranianBank("بانک خاورمیانه", "MEB", "", "https://middleeastbank.ir", "02188506000", listOf("585949")),
        IranianBank("بانک دی", "BDY", "", "https://day24.ir", "02128930", listOf("502938")),
        IranianBank("بانک کارآفرین", "BKA", "", "https://karafarinbank.ir", "02123950", listOf("627488", "502908")),
        IranianBank("پست بانک ایران", "POST", "*747#", "https://postbank.ir", "02184284", listOf("627760"))
    )

    fun getBankNames(): List<String> = banks.map { it.name }
}
