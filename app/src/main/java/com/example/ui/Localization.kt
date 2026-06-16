package com.example.ui

object Localization {
    data class Language(val code: String, val name: String, val flag: String)

    val supportedLanguages = listOf(
        Language("ar", "العربية (الفصحى)", "🇸🇦"),
        Language("ar_pal", "الفلسطينية 🇵🇸", "🇵🇸"),
        Language("ar_alg", "الجزائرية 🇩🇿", "🇩🇿"),
        Language("ar_mar", "المغربية 🇲🇦", "🇲🇦"),
        Language("ar_tun", "التونسية 🇹🇳", "🇹🇳"),
        Language("ar_sau", "السعودية 🇸🇦", "🇸🇦"),
        Language("ar_jor", "الأردنية 🇯🇴", "🇯🇴"),
        Language("ar_syr", "السورية 🇸🇾", "🇸🇾"),
        Language("en", "English", "🇬🇧"),
        Language("en_us", "American English", "🇺🇸"),
        Language("fr", "Français", "🇫🇷"),
        Language("tr", "Türkçe", "🇹🇷"),
        Language("id", "Bahasa Indonesia", "🇮🇩"),
        Language("pt_br", "Português (Brasil)", "🇧🇷"),
        Language("es_arg", "Español (Argentina)", "🇦🇷")
    )

    private val translations = mapOf(
        "welcome" to mapOf(
            "ar" to "أهلاً وسهلاً بك",
            "ar_pal" to "يا هلا وغلا فيك يا غالي 🇵🇸",
            "ar_alg" to "واش راك يا خويا! ممتع بيلك 🇩🇿",
            "ar_mar" to "مرحباً بيك خويا العزيز ف بيلك 🇲🇦",
            "ar_tun" to "يا هلا ومرحب بيك يا باهي 🇹🇳",
            "ar_sau" to "يا هلا والله ومسهلا فيك يا طويل العمر 🇸🇦",
            "ar_jor" to "يا ميت أهلاً وسهلاً هداك الله 🇯🇴",
            "ar_syr" to "يا مية أهلاً وسهلاً فيك عيني 🇸🇾",
            "en" to "Welcome!",
            "en_us" to "Hey there, Welcome!",
            "fr" to "Bienvenue!",
            "tr" to "Hoş geldiniz!",
            "id" to "Selamat datang!",
            "pt_br" to "Seja bem-vindo!",
            "es_arg" to "¡Che, bienvenido!"
        ),
        "home" to mapOf(
            "ar" to "الرئيسية",
            "ar_pal" to "الرئيسية",
            "ar_alg" to "الدار / الرئيسية",
            "ar_mar" to "الصفحة الرئيسية",
            "ar_tun" to "الرئيسية",
            "ar_sau" to "الرئيسية",
            "ar_jor" to "الرئيسية",
            "ar_syr" to "الرئيسية",
            "en" to "Home",
            "en_us" to "Home Base",
            "fr" to "Accueil",
            "tr" to "Ana Sayfa",
            "id" to "Beranda",
            "pt_br" to "Início",
            "es_arg" to "Inicio"
        ),
        "kids" to mapOf(
            "ar" to "الأطفال 👶",
            "ar_pal" to "للأولاد الصغار 👶",
            "ar_alg" to "الدراري الصغار 👶",
            "ar_mar" to "الدراري الصغار 👶",
            "ar_tun" to "الصغيرات 👶",
            "ar_sau" to "البزارين 👶",
            "ar_jor" to "الأولاد الصغار 👶",
            "ar_syr" to "الصغارون 👶",
            "en" to "Kids Mode 👶",
            "en_us" to "BILLK Kids 🧸",
            "fr" to "Mode Enfants 👶",
            "tr" to "Çocuklar 👶",
            "id" to "Anak-Anak 👶",
            "pt_br" to "Crianças 👶",
            "es_arg" to "Pibitos 👶"
        ),
        "foot" to mapOf(
            "ar" to "بيلك فوت ⚽",
            "ar_pal" to "بيلك فوت ⚽",
            "ar_alg" to "بيلك بالفوت ⚽",
            "ar_mar" to "بيلك كورة ⚽",
            "ar_tun" to "بيلك فوت ⚽",
            "ar_sau" to "كورة بيلك ⚽",
            "ar_jor" to "بيلك فوت ⚽",
            "ar_syr" to "بيلك فوت ⚽",
            "en" to "BILLK FOOT ⚽",
            "en_us" to "Sports Room ⚽",
            "fr" to "BILLK FOOT ⚽",
            "tr" to "BILLK FUTBOL ⚽",
            "id" to "BILLK SEPAKBOLA ⚽",
            "pt_br" to "BILLK FUTEBOL ⚽",
            "es_arg" to "¡FÚTBOL CHABÓN! ⚽"
        ),
        "community" to mapOf(
            "ar" to "المجتمع 💬",
            "ar_pal" to "ملتقى الشباب 💬",
            "ar_alg" to "قصرة كوميونيتي 💬",
            "ar_mar" to "الجروب كورة 💬",
            "ar_tun" to "المجتمع 💬",
            "ar_sau" to "المجلس 💬",
            "ar_jor" to "المجلس 💬",
            "ar_syr" to "الجمعة 💬",
            "en" to "Community 💬",
            "en_us" to "Chat & Suggest 💬",
            "fr" to "Communauté 💬",
            "tr" to "Topluluk 💬",
            "id" to "Komunitas 💬",
            "pt_br" to "Comunidade 💬",
            "es_arg" to "Foro 💬"
        ),
        "settings" to mapOf(
            "ar" to "الإعدادات ⚙️",
            "ar_pal" to "الضبط ⚙️",
            "ar_alg" to "لي قلاجات ⚙️",
            "ar_mar" to "السيستيم ⚙️",
            "ar_tun" to "لي برامتر ⚙️",
            "ar_sau" to "الخيارات ⚙️",
            "ar_jor" to "الإعدادات ⚙️",
            "ar_syr" to "الإعدادات ⚙️",
            "en" to "Settings ⚙️",
            "en_us" to "Preferences ⚙️",
            "fr" to "Paramètres ⚙️",
            "tr" to "Ayarlar ⚙️",
            "id" to "Pengaturan ⚙️",
            "pt_br" to "Ajustes ⚙️",
            "es_arg" to "Configuraciones ⚙️"
        ),
        "subscription_required" to mapOf(
            "ar" to "يجب أن يكون لديك اشتراك نشط لمشاهدة هذا المحتوى.",
            "en" to "You must have an active subscription to watch this content.",
            "fr" to "Un abonnement actif est requis pour regarder ce contenu.",
            "tr" to "Bu içeriği izlemek için aktif bir aboneliğiniz olmalıdır.",
            "id" to "Anda memerlukan langganan aktif untuk menonton konten ini."
        ),
        "subscribe_now" to mapOf(
            "ar" to "اشترك الآن 💳",
            "en" to "Subscribe Now 💳",
            "fr" to "S'abonner 💳"
        ),
        "enter_code" to mapOf(
            "ar" to "أدخل كود الاشتراك للاستمرار",
            "en" to "Enter activation code to continue",
            "fr" to "Entrez le code d'activation pour continuer"
        ),
        "cancel" to mapOf(
            "ar" to "إلغاء",
            "en" to "Cancel",
            "fr" to "Annuler"
        ),
        "confirm" to mapOf(
            "ar" to "تأكيد",
            "en" to "Confirm",
            "fr" to "Confirmer"
        ),
        "logout" to mapOf(
            "ar" to "تسجيل الخروج",
            "en" to "Logout",
            "fr" to "Déconnexion"
        ),
        "nickname" to mapOf(
            "ar" to "اللقب (اسم مستخدم فريد)",
            "en" to "Username (unique)",
            "fr" to "Nom d'utilisateur"
        )
    )

    fun getText(key: String, langCode: String): String {
        val entry = translations[key] ?: return key
        return entry[langCode] ?: entry["ar"] ?: key
    }
}
