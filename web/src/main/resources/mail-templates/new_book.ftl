<html>
<head>
    <meta charset="UTF-8">
    <title>Нова книга додана</title>
</head>
<body style="font-family: Arial, sans-serif; background: #f6f8fa; margin: 0; padding: 0;">
<table role="presentation" style="width:100%; border-collapse:collapse; padding: 20px 0;">
    <tr>
        <td align="center">
            <table role="presentation" style="width:600px; max-width:90%; background:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                <tr>
                    <td style="padding: 20px; text-align: center; background:#f0f4ff;">
                        <img src="https://raw.githubusercontent.com/ver-val/guestbook/lab7-freemarker/web/src/main/resources/static/img/logo.webp" 
                             alt="BookApp" style="width:150px; margin-bottom:10px;">
                        <h1 style="margin: 0; color:#0f172a; font-size:22px;">Додано нову книгу</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 20px;">
                        <div style="background:#f8fafc; border:1px solid #e2e8f0; border-radius:6px; padding:16px; margin-bottom:12px;">
                            <p style="margin:4px 0; color:#0f172a;"><strong>Назва:</strong> ${title}</p>
                            <p style="margin:4px 0; color:#0f172a;"><strong>Автор:</strong> ${author}</p>
                            <#if year??>
                                <p style="margin:4px 0; color:#0f172a;"><strong>Рік:</strong> ${year?replace(",", "")}</p>
                            <#else>
                                <p style="margin:4px 0; color:#0f172a;"><strong>Рік:</strong> —</p>
                            </#if>
                            <p style="margin:4px 0; color:#475569;"><strong>Додано:</strong> ${added}</p>
                            <#if year?? && (year?replace(",", "")?number < 2000)>
                                <p style="color:#a36b00; margin:8px 0;"><b>Раритетне видання!</b></p>
                            </#if>
                        </div>
                        <p style="color:#475569; margin:12px 0 0 0;">Дякуємо, що користуєтесь нашим каталогом книг!</p>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 14px 20px; text-align: center; background:#e2e8f0; color:#475569; font-size:12px; border-top:1px solid #cbd5e1;">
                        <div style="font-weight:600; color:#1f2937;">BookApp © 2025</div>
                        <div>Це автоматичний лист, будь ласка, не відповідайте на нього.</div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>
