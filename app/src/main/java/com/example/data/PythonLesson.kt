package com.example.data

data class PythonLesson(
    val id: String,
    val title: String,
    val englishTitle: String,
    val category: String,
    val description: String,
    val codeSnippet: String,
    val difficulty: String, // آسان / متوسط
    val duration: String, // مثلاً "۵ دقیقه"
    val expectedOutput: String,
    val tips: List<String>
)

object PythonLessonsProvider {
    val lessons = listOf(
        PythonLesson(
            id = "variables",
            title = "متغیرها و نام‌گذاری",
            englishTitle = "Variables & Data Types",
            category = "مفاهیم پایه",
            description = "در پایتون، متغیرها مثل جعبه‌هایی برای ذخیره اطلاعات هستند. شما نیازی به تعریف نوع متغیر ندارید، پایتون خودش هوشمندانه نوع آن (متن، عدد یا بله/خیر) را متوجه می‌شود. برای قرار دادن مقدار داخل متغیر از علامت = استفاده می‌کنیم.",
            codeSnippet = """# تعریف متغیرهای مختلف
name = "پایتون یار"
age = 5
is_awesome = True

# چاپ مقادیر در خروجی
print("سلام! من " + name + " هستم.")
print("من " + str(age) + " سال دارم.")
print("آیا پایتون عالی است؟ " + str(is_awesome))
""",
            difficulty = "آسان",
            duration = "۳ دقیقه",
            expectedOutput = """سلام! من پایتون یار هستم.
من ۵ سال دارم.
آیا پایتون عالی است؟ True
""",
            tips = listOf(
                "نام متغیرها نمی‌تواند با عدد شروع شود.",
                "پایتون به حروف بزرگ و کوچک حساس است (مثلاً name با Name فرق دارد).",
                "با استفاده از تابع str() می‌توان عددها را به متن تبدیل کرد تا در پرینت بچسبند."
            )
        ),
        PythonLesson(
            id = "conditions",
            title = "دستورات شرطی (تصمیم‌گیری)",
            englishTitle = "If-Else Conditions",
            category = "جریان کنترل",
            description = "دستورات شرطی به برنامه‌ی شما اجازه می‌دهند بر اساس شرایط مختلف تصمیم‌گیری کند. مانند اینکه بگوییم: «اگر باران ببارد، چتر برمی‌دارم، در غیر این صورت راهم را ادامه می‌دهم». در پایتون از کلمات کلیدی if، elif و els استفاده می‌شود.",
            codeSnippet = """score = 85

if score >= 90:
    print("شگفت‌انگیز! نمره شما عالی است.")
elif score >= 75:
    print("بسیار خوب! شما قبول شدید.")
else:
    print("تلاش بیشتری کنید، شما می‌توانید!")
""",
            difficulty = "آسان",
            duration = "۵ دقیقه",
            expectedOutput = "بسیار خوب! شما قبول شدید.\n",
            tips = listOf(
                "فراموش نکنید که در پایان خط شرط، حتماً از دو نقطه (:) استفاده کنید.",
                "دستورات داخل شرط حتماً باید کمی جلوتر نوشته شوند (تورفتگی یا Indentation). این تورفتگی ساختار بلاک را مشخص می‌کند."
            )
        ),
        PythonLesson(
            id = "loops",
            title = "حلقه‌های تکرار (For & While)",
            englishTitle = "Loops",
            category = "جریان کنترل",
            description = "وقتی می‌خواهید کاری چندین بار به صورت خودکار تکرار شود، از حلقه‌ها استفاده می‌کنید. حلقه for معمولاً برای چرخیدن روی لیستی از آیتم‌ها یا تکرار به تعداد مشخص کاربرد دارد. حلقه while تا زمانی که یک شرط درست باشد تکرار را ادامه می‌دهد.",
            codeSnippet = """# تکرار ۳ بار با استفاده از range
print("شمارش معکوس پرتاب:")
for i in range(3, 0, -1):
    print(str(i) + "...")

# تکرار روی لیست میوه‌ها
print("\nلیست خرید:")
fruits = ["سیب", "موز", "پرتقال"]
for fruit in fruits:
    print("- " + fruit)
""",
            difficulty = "متوسط",
            duration = "۷ دقیقه",
            expectedOutput = """شمارش معکوس پرتاب:
۳...
۲...
۱...

لیست خرید:
- سیب
- موز
- پرتقال
""",
            tips = listOf(
                "تابع range(شروع، پایان، گام) برای ایجاد بازه‌ای از اعداد استفاده می‌شود. عدد پایان خود شامل بازی نیست.",
                "در حلقه‌ها هم تورفتگی یا فاصله از ابتدای خط الزامی است تا پایتون بفهمد چه کدهایی متعلق به حلقه هستند."
            )
        ),
        PythonLesson(
            id = "functions",
            title = "توابع (ساخت ابزار جادویی وب)",
            englishTitle = "Functions",
            category = "توسعه ساختار",
            description = "تابع یک قطعه کد است که کار مشخصی را انجام می‌دهد و فقط زمانی که صدایش کنید اجرا می‌شود. توابع به شما کمک می‌کنند تا از تکرار کدهای خود جلوگیری کنید. برای ساخت تابع از کلمه کلیدی def استفاده می‌کنیم.",
            codeSnippet = """# تعریف یک تابع برای خوش‌آمدگویی
def say_hello(username, language):
    if language == "fa":
        return "سلام " + username + "! خوش آمدی."
    else:
        return "Hello " + username + "! Welcome."

# صدا زدن تابع با مقادیر مختلف و چاپ خروجی
msg1 = say_hello("محمد", "fa")
msg2 = say_hello("Sarah", "en")

print(msg1)
print(msg2)
""",
            difficulty = "متوسط",
            duration = "۸ دقیقه",
            expectedOutput = """سلام محمد! خوش آمدی.
Hello Sarah! Welcome.
""",
            tips = listOf(
                "توابع می‌توانند ورودی داشته باشند (آرگومان‌ها) و خروجی بفرستند (با دستور return).",
                "هر ورودی با کاما (,) جدا می‌شود.",
                "نام توابع معمولاً با حروف کوچک و علامت زیرخط (_) نوشته می‌شود."
            )
        ),
        PythonLesson(
            id = "lists",
            title = "لیست‌ها (جعبه‌های ذخیره گروهی)",
            englishTitle = "Lists & Arrays",
            category = "ساختمان داده",
            description = "لیست‌ها به شما اجازه می‌دهند چند داده را در یک متغیر واحد نگهداری کنید. آیتم‌ها داخل کروشه [] قرار می‌گیرند و با کاما از هم جدا می‌شوند. لیست‌ها ترتیب دارند و می‌توانید به راحتی آیتم‌ها را اضافه، حذف یا ویرایش کنید.",
            codeSnippet = """# تعریف یک لیست از رنگ‌ها
colors = ["قرمز", "آبی", "سبز"]

# اضافه کردن رنگ جدید به آخر لیست
colors.append("زرد")

# چاپ تعداد آیتم‌ها و آیتم دوم (اندیس‌ها از صفر شروع می‌شوند!)
print("تعداد رنگ‌ها: " + str(len(colors)))
print("رنگ دوم لیست: " + colors[1])

# چاپ تمام رنگ‌ها با یک حلقه
print("\nهمه رنگ‌ها:")
for c in colors:
    print("رنگ: " + c)
""",
            difficulty = "آسان",
            duration = "۶ دقیقه",
            expectedOutput = """تعداد رنگ‌ها: ۴
رنگ دوم لیست: آبی

همه رنگ‌ها:
رنگ: قرمز
رنگ: آبی
رنگ: سبز
رنگ: زرد
""",
            tips = listOf(
                "شمارش خانه‌های لیست یا اندیس‌ها در پایتون از صفر (0) شروع می‌شود.",
                "بنابراین اولین آیتم colors[0] و آیتم دوم colors[1] است.",
                "با متد list.append(item) می‌توانید به انتهای لیست مقدار جدید اضافه کنید."
            )
        )
    )
}
