<#import "/spring.ftl" as spring>
<#include "fragments/header.ftl">
<!DOCTYPE html>
<html lang="uk">
<head>
    <@head/>
</head>
<body>
<div class="wrapper" data-book-id="${book.id()}">
    <@topbar currentId=book.id()/>

    <a href="/books" class="back-link">&larr; <@spring.message "action.back"/></a>

<h1>${book.title()}</h1>
<div class="card meta">
    <div><strong><@spring.message "label.author"/>:</strong> ${book.author()}</div>
    <div><strong><@spring.message "label.year"/>:</strong> <#if book.pubYear()??>${book.pubYear()?c}<#else>—</#if></div>
    <div><strong><@spring.message "label.description"/>:</strong> ${book.description()!""}</div>
    </div>

    <section class="comment-form card">
        <h3><@spring.message "label.addComment"/></h3>
        <form class="comment-form" action="/books/${book.id()}/comments" method="post">
            <input type="text" name="author" placeholder="${springMacroRequestContext.getMessage('placeholder.author')}" maxlength="64" required>
            <textarea name="text" placeholder="${springMacroRequestContext.getMessage('placeholder.comment')}" maxlength="1000" required></textarea>
            <button type="submit" class="button primary"><@spring.message "action.submit"/></button>
        </form>
    </section>

    <#if commentSuccess??>
        <div class="flash success auto-hide"><@spring.message "msg.commentAdded"/></div>
    </#if>
    <#if commentError??>
        <div class="flash error">${commentError}</div>
    </#if>

    <section class="comment-list card">
        <h2><@spring.message "label.comments"/></h2>
        <#if !comments?? || comments?size == 0>
            <div><@spring.message "label.noComments"/></div>
        <#else>
            <#list comments as c>
                <div class="comment">
                    <div class="comment-meta">
                        <strong>${c.author()}</strong>
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
                        <#if c.userId()??>
                            <span class="small">
                                <a href="/users/${c.userId()}/comments"><@spring.message "label.authorCommentsLink"/></a>
                            </span>
                        </#if>
                    </div>
                    <div>${c.text()}</div>
                    <form action="/comments/${c.id()}" method="post" style="margin-top:0.5rem;">
                        <input type="hidden" name="bookId" value="${book.id()}">
                        <button type="submit" class="button danger"><@spring.message "action.delete"/></button>
                    </form>
                </div>
            </#list>
        </#if>
    </section>
</div>
<script>
    setTimeout(() => {
        document.querySelectorAll('.flash.auto-hide').forEach(el => el.style.display = 'none');
    }, 3000);
</script>
</body>
</html>
