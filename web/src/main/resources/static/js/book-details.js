window.deleteComment = function deleteComment(bookId, commentId) {
    fetch(`/api/comments?bookId=${bookId}&commentId=${commentId}`, {
        method: 'DELETE',
        headers: { 'Accept': 'application/json' }
    }).then(async (response) => {
        if (!response.ok) {
            let msg = 'Delete failed';
            try {
                const body = await response.json();
                if (body && body.error) {
                    msg = body.error;
                }
            } catch (_) {
                // ignore parsing errors
            }
            alert(msg);
            return;
        }
        window.location.reload();
    });
};

window.addEventListener('DOMContentLoaded', () => {
    setTimeout(() => {
        document.querySelectorAll('.flash.auto-hide').forEach(el => {
            el.style.display = 'none';
        });
    }, 3000);
});
