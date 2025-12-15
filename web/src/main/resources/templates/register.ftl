<#include "fragments/header.ftl">
<!DOCTYPE html>
<html lang="uk">
<head>
    <@head/>
</head>
<body>
<div class="wrapper">
    <@topbar showCatalog=false currentPath="/register"/>
    <h1><@spring.message "title.register"/></h1>

    <#if errors??>
        <div class="errors">
            <strong><@spring.message "msg.confirmError"/></strong>
            <ul>
                <#list errors?keys as k>
                    <li>${k}: ${errors[k]}</li>
                </#list>
            </ul>
        </div>
    </#if>
    <#if errorMessage??>
        <div class="errors">${errorMessage}</div>
    </#if>

    <form action="/register" method="post" class="card form-card">
        <label for="username"><@spring.message "label.username"/></label>
        <input id="username" name="username" type="text" required maxlength="128" value="${username!''}">

        <label for="email"><@spring.message "label.email"/></label>
        <input id="email" name="email" type="email" required maxlength="255" value="${email!''}">

        <label for="password"><@spring.message "label.password"/></label>
        <input id="password" name="password" type="password" required>

        <div class="actions">
            <button type="submit"><@spring.message "action.register"/></button>
            <a class="button" href="/login"><@spring.message "action.login"/></a>
        </div>
    </form>
</div>
</body>
</html>
