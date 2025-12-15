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

    <#assign adminFlag = false>
    <#if isAdmin?? && isAdmin>
        <#assign adminFlag = true>
    <#elseif request?has_content && request.isUserInRole?has_content && request.isUserInRole("ADMIN")>
        <#assign adminFlag = true>
    </#if>

    <header class="page-header">
        <h1><@spring.message "title.books"/></h1>
        <#if adminFlag>
            <a class="button primary" href="/books/new"><@spring.message "label.addBook"/></a>
        </#if>
    </header>

    <#if bookCreated?? && bookCreated>
        <div class="flash success"><@spring.message "msg.bookCreated"/></div>
    </#if>

    <form class="search" action="/books" method="get">
        <input type="text" name="q" placeholder="${springMacroRequestContext.getMessage('placeholder.search')}" value="${q!''}">
        <button type="submit"><@spring.message "action.search"/></button>
    </form>

    <#if !books?? || books?size == 0>
        <div class="empty"><@spring.message "label.noBooks"/></div>
    <#else>
        <table class="data-table">
            <thead>
            <tr>
                <th><@spring.message "label.title"/></th>
                <th><@spring.message "label.author"/></th>
                <th><@spring.message "label.year"/></th>
                <th><@spring.message "label.description"/></th>
                <th><@spring.message "label.actions"/></th>
            </tr>
            </thead>
            <tbody>
            <#list books as book>
                <tr>
                    <td>${book.title()}</td>
                    <td>${book.author()}</td>
                    <td><#if book.pubYear()??>${book.pubYear()?c}<#else>—</#if></td>
                    <td title="${book.description()!''}">
                        <#assign desc = book.description()!''>
                        <#if (desc?length > 140)>
                            ${desc?substring(0, 140)}...
                        <#else>
                            ${desc}
                        </#if>
                    </td>
                    <td class="actions">
                        <a class="button primary" href="/books/${book.id()}"><@spring.message "action.view"/></a>
                        <#if adminFlag>
                            <form class="inline-form" action="/books/${book.id()}/delete" method="post">
                                <button type="submit" class="button danger-outline"><@spring.message "action.delete"/></button>
                            </form>
                        </#if>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
    </#if>
</div>
</body>
</html>
