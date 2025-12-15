<html>
<head>
    <meta charset="UTF-8">
    <title>Підтвердження акаунту</title>
</head>
<body style="font-family: Arial, sans-serif; background: #f6f8fa; margin: 0; padding: 0;">
<table role="presentation" style="width:100%; border-collapse:collapse; padding: 20px 0;">
    <tr>
        <td align="center">
            <table role="presentation" style="width:600px; max-width:90%; background:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 2px 8px rgba(0,0,0,0.06);">
                <tr>
                    <td style="padding: 20px; text-align: center; background:#f0f4ff;">
                        <h1 style="margin: 0; color:#0f172a; font-size:22px;">Підтвердження акаунту</h1>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 20px;">
                        <p style="color:#0f172a;">Вітаємо, ${username}! Для завершення реєстрації підтвердіть акаунт.</p>
                        <div style="text-align:center; margin: 20px 0;">
                            <a href="${confirmLink}" style="background:#2563eb; color:#fff; padding:12px 18px; border-radius:6px; text-decoration:none; font-weight:600;">
                                Підтвердити акаунт
                            </a>
                        </div>
                        <p style="color:#475569;">Якщо кнопка не працює, скористайтеся посиланням: <br><a href="${confirmLink}">${confirmLink}</a></p>
                    </td>
                </tr>
                <tr>
                    <td style="padding: 14px 20px; text-align: center; background:#e2e8f0; color:#475569; font-size:12px; border-top:1px solid #cbd5e1;">
                        <div style="font-weight:600; color:#1f2937;">BookApp © 2025</div>
                        <div>Це автоматичний лист, будь ласка, не відповідайте на нього.</div>
                        <div>Питання: ${support!''}</div>
                    </td>
                </tr>
            </table>
        </td>
    </tr>
</table>
</body>
</html>
