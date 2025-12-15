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

    <#assign authenticated = (isAuthenticated?? && isAuthenticated) || (currentUser??)>

    <a href="/books" class="back-link">&larr; <@spring.message "action.back"/></a>

<h1>${book.title()}</h1>
<div class="card meta">
    <div><strong><@spring.message "label.author"/>:</strong> ${book.author()}</div>
    <div><strong><@spring.message "label.year"/>:</strong> <#if book.pubYear()??>${book.pubYear()?c}<#else>—</#if></div>
    <div><strong><@spring.message "label.description"/>:</strong> ${book.description()!""}</div>
    </div>

    <#if authenticated>
        <section class="comment-form card">
            <h3><@spring.message "label.addComment"/></h3>
            <form class="comment-form" action="/books/${book.id()}/comments" method="post">
                <#assign authorName = (currentUser.username())!"" />
                <#if authorName?has_content>
                    <div class="comment-author-label">
                        <@spring.message "label.loggedInAs"/> ${authorName}
                    </div>
                    <input type="hidden" name="author" value="${authorName}">
                <#else>
                    <input type="text" name="author" placeholder="${springMacroRequestContext.getMessage('placeholder.author')}" maxlength="64" required>
                </#if>
                <textarea name="text" placeholder="${springMacroRequestContext.getMessage('placeholder.comment')}" maxlength="1000" required></textarea>
                <button type="submit" class="button primary"><@spring.message "action.submit"/></button>
            </form>
        </section>
    </#if>

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
                        <#assign isOwner = (currentUser?? && c.userId()?? && (c.userId() == currentUser.id()))>
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
                    <div class="comment-row">
                        <div class="comment-body">${c.text()}</div>
                        <#assign adminFlag = (isAdmin?? && isAdmin) || (springMacroRequestContext?has_content && springMacroRequestContext.isUserInRole?has_content && springMacroRequestContext.isUserInRole("ADMIN"))>
                        <#if adminFlag || isOwner>
                            <div class="comment-actions">
                                <form action="/comments/${c.id()}" method="post" class="inline-form">
                                    <input type="hidden" name="bookId" value="${book.id()}">
                                    <button type="submit" class="button danger-outline"><@spring.message "action.delete"/></button>
                                </form>
                            </div>
                        </#if>
                    </div>
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
