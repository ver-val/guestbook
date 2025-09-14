const form = document.getElementById('f');
const commentsList = document.getElementById('comments');

async function loadComments() {
    try {
        const res = await fetch('/comments');
        if (res.ok) {
            const data = await res.json();
            commentsList.innerHTML = '';
            data?.forEach(c => {
                const li = document.createElement('li');
                li.innerHTML = `<strong>${c.author}</strong> <small>(${new Date(c.createdAt).toLocaleString()})</small><br>${c.text}`;
                commentsList.append(li);
            });
        }
    } catch (e) {
        console.error('Помилка при завантаженні коментарів', e);
    }
}

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new URLSearchParams();
    formData.append('author', form.author.value);
    formData.append('text', form.text.value);

    try {
        const res = await fetch('/comments', {
            method: 'POST',
            body: formData
        });

        if (res.status === 204) {
            form.reset();
            loadComments();
        } else if (res.status === 400) {
            alert('Помилка: перевірте довжину і заповненість полів');
        } else {
            alert('Помилка сервера');
        }
    } catch (e) {
        console.error(e);
        alert('Помилка при відправці коментаря');
    }
});

loadComments();
