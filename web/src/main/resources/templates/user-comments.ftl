<#import "/spring.ftl" as spring>
<#include "fragments/header.ftl">
<!DOCTYPE html>
<html lang="uk">
<head>
    <@head/>
</head>
<body>
<div class="wrapper">
    <#assign safePath = "/users/" + (userId!"") + "/comments">
    <@topbar currentPath=safePath/>

    <h1><@spring.message "label.userComments"/>: ${username}</h1>
    <a href="/books" class="back-link">&larr; <@spring.message "action.back"/></a>

    <#if !comments?? || comments?size == 0>
        <div class="empty"><@spring.message "label.noUserComments"/></div>
    <#else>
        <div class="card">
            <#list comments as c>
                <div class="comment">
                    <div class="comment-meta">
                        <span class="small">
                            <#assign created = c.createdAt()>
                            <#if created?is_date>
                                ${created?string("yyyy-MM-dd HH:mm")}
                            <#elseif created?is_string && (created?length >= 16)>
                                ${created?substring(0,16)?replace("T"," ")}
                            <#else>
                                ${created!""}
                            </#if>
                        </span>
                        <span><@spring.message "label.title"/>: <a href="/books/${c.bookId()}">#${c.bookId()}</a></span>
                    </div>
                    <div>${c.text()}</div>
                    <hr>
                </div>
            </#list>
        </div>
    </#if>
</div>
</body>
</html>
