<#import "/spring.ftl" as spring>
<#macro head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><@spring.message "title.books"/></title>
    <link rel="stylesheet" href="/css/styles.css">
</#macro>

<#macro topbar currentId="" showCatalog=true currentPath="/books">
    <#-- Determine authentication state with fallbacks to avoid null errors -->
    <#-- Resolve current user name from Spring MVC context -->
    <#assign principalName = "">
    <#if currentUser?? && currentUser.username()?has_content>
        <#assign principalName = currentUser.username() />
    </#if>
    <#if !principalName?has_content && currentUserName?? && currentUserName?has_content>
        <#assign principalName = currentUserName />
    </#if>
    <#assign isAuthenticated = principalName?has_content || (isAuthenticated?? && isAuthenticated)>

    <div class="topbar">
    <#-- Determine base path for language switches -->
    <#assign langPath = currentPath?default("/books")>
    <#if currentId?has_content>
        <#assign langPath = "/books/" + currentId>
    </#if>

        <div class="nav-links nav-left">
            <#if showCatalog>
                <a href="/books" aria-label="Books list"><@spring.message "title.books"/></a>
            </#if>
            <a href="${langPath}?lang=uk"><@spring.message "ui.lang.ua"/></a>
            <a href="${langPath}?lang=en"><@spring.message "ui.lang.en"/></a>
            <a href="${langPath}?lang=fr"><@spring.message "ui.lang.fr"/></a>
        </div>
        <div class="nav-links nav-right">
            <#if isAuthenticated>
                <span class="user-chip">
                    <@spring.message "label.loggedInAs"/> ${principalName}
                </span>
                <form action="/logout" method="post" class="inline-form">
                    <button type="submit" class="button danger-outline"><@spring.message "action.logout"/></button>
                </form>
            <#else>
                <a href="/login"><@spring.message "action.login"/></a>
                <a href="/register"><@spring.message "action.register"/></a>
            </#if>
        </div>
    </div>
</#macro>
