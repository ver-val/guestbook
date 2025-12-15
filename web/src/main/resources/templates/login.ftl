<#include "fragments/header.ftl">
<!DOCTYPE html>
<html lang="uk">
<head>
    <@head/>
</head>
<body>
<div class="wrapper">
    <@topbar showCatalog=false currentPath="/login"/>
    <h1><@spring.message "title.login"/></h1>

    <#if confirmed??>
        <div class="flash success"><@spring.message "msg.confirmSuccess"/></div>
    </#if>
    <#if confirmError??>
        <div class="flash error">${confirmError!springMacroRequestContext.getMessage('msg.confirmError')}</div>
    </#if>
    <#if loginError??>
        <div class="flash error"><@spring.message "msg.loginError"/></div>
    </#if>
    <#if loginDisabled??>
        <div class="flash error"><@spring.message "msg.loginDisabled"/></div>
    </#if>
    <#if confirmEmailSent??>
        <div class="flash success"><@spring.message "msg.confirmEmailSent"/></div>
    </#if>

    <form action="/login" method="post" class="card form-card">
        <label for="username"><@spring.message "label.username"/></label>
        <input id="username" name="username" type="text" required autofocus>

        <label for="password"><@spring.message "label.password"/></label>
        <input id="password" name="password" type="password" required>

        <div class="actions">
            <button type="submit"><@spring.message "action.login"/></button>
            <a class="button" href="/register"><@spring.message "action.register"/></a>
        </div>
    </form>
</div>
</body>
</html>
