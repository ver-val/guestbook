<#include "../fragments/header.ftl">
<!DOCTYPE html>
<html lang="uk">
<head>
    <@head/>
</head>
<body>
<div class="wrapper">
    <@topbar showCatalog=true/>

    <div class="card" style="text-align:center; padding:2rem;">
        <h1>500</h1>
        <p style="margin:0.5rem 0 1.5rem;"><@spring.message "label.internalErrorText"/></p>
        <#if errorMessage?? && errorMessage?has_content>
            <p style="color:#9ca3af; font-size:0.95rem;">${errorMessage}</p>
        </#if>
        <a class="button primary" href="/books">← <@spring.message "action.backToCatalog"/></a>
    </div>
</div>
</body>
</html>
