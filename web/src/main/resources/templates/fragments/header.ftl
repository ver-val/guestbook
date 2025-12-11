<#import "/spring.ftl" as spring>
<#macro head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><@spring.message "title.books"/></title>
    <link rel="stylesheet" href="/css/styles.css">
</#macro>

<#macro topbar currentId="">
    <div class="topbar">
        <div class="nav-links">
            <a href="/books" aria-label="Books list"><@spring.message "title.books"/></a>
            <#if currentId?has_content>
                <a href="/books/${currentId}?lang=uk"><@spring.message "ui.lang.ua"/></a>
                <a href="/books/${currentId}?lang=en"><@spring.message "ui.lang.en"/></a>
                <a href="/books/${currentId}?lang=fr"><@spring.message "ui.lang.fr"/></a>
            <#else>
                <a href="/books?lang=uk"><@spring.message "ui.lang.ua"/></a>
                <a href="/books?lang=en"><@spring.message "ui.lang.en"/></a>
                <a href="/books?lang=fr"><@spring.message "ui.lang.fr"/></a>
            </#if>
        </div>
    </div>
</#macro>
