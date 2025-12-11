<#import "/spring.ftl" as spring>
<#include "fragments/header.ftl">
<!DOCTYPE html>
<html lang="uk">
<head>
    <@head/>
</head>
<body>
<div class="wrapper">
    <@topbar/>

    <h1><@spring.message "label.addBook"/></h1>

    <#if validationErrors?? && (validationErrors?size > 0)>
        <div class="errors">
            <strong><@spring.message "msg.validationErrors"!"Помилки валідації:"/></strong>
            <ul>
                <#list validationErrors?keys as k>
                    <li>${k}: ${validationErrors[k]}</li>
                </#list>
            </ul>
        </div>
    </#if>
    <#if errorMessage??>
        <div class="errors">${errorMessage}</div>
    </#if>

    <form class="card form-card" action="/books" method="post">
        <input type="hidden" name="id" value="${book.id()!0}">

        <label for="title"><@spring.message "label.title"/></label>
        <input id="title" type="text" name="title" value="${book.title()!''}" required maxlength="255" placeholder="${springMacroRequestContext.getMessage('placeholder.title')}">

        <label for="author"><@spring.message "label.author"/></label>
        <input id="author" type="text" name="author" value="${book.author()!''}" required maxlength="255" placeholder="${springMacroRequestContext.getMessage('placeholder.author')}">

        <label for="pubYear"><@spring.message "label.year"/></label>
        <input id="pubYear" type="number" name="pubYear" value="${book.pubYear()!''}" min="1" max="2100" placeholder="${springMacroRequestContext.getMessage('placeholder.year')}">

        <label for="description"><@spring.message "label.description"/></label>
        <textarea id="description" name="description" maxlength="2000" placeholder="${springMacroRequestContext.getMessage('placeholder.description')}">${book.description()!''}</textarea>

        <div class="actions">
            <button type="submit"><@spring.message "action.save"/></button>
            <a class="button" href="/books"><@spring.message "action.cancel"/></a>
        </div>
    </form>
</div>
</body>
</html>
