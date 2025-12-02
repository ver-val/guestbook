(() => {
  const ctx = {
    baseUrl: document.body.dataset.baseUrl || '',
    bookId: resolveBookId(),
    commentList: document.getElementById('commentList'),
    form: document.getElementById('commentForm'),
  };

  if (!ctx.bookId) {
    alert('Не знайдено ідентифікатор книги, оновіть сторінку.');
    return;
  }
  if (!ctx.commentList || !ctx.form) {
    console.warn('Коментарі або форма не знайдені на сторінці.');
    return;
  }

  Array.from(document.querySelectorAll('.delete-btn')).forEach(wireDelete);
  ctx.form.addEventListener('submit', submitComment);

  async function submitComment(e) {
    e.preventDefault();
    const payload = {
      author: ctx.form.author.value,
      text: ctx.form.text.value,
    };
    try {
      const target = `${ctx.baseUrl}/books/${encodeURIComponent(ctx.bookId)}/comments`;
      const res = await fetch(target, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      if (res.status === 201) {
        const comment = await res.json();
        ctx.form.reset();
        appendComment(comment);
        return;
      }
      if (res.status === 400) {
        alert('Помилка: перевірте заповнення полів (author <=64, text <=1000).');
        return;
      }
      if (res.status === 404) {
        alert('Книга не знайдена');
        return;
      }
      alert('Не вдалося додати коментар');
    } catch (err) {
      console.error('Помилка додавання коментаря', err);
      alert('Мережева помилка при додаванні');
    }
  }

  function wireDelete(btn) {
    btn.addEventListener('click', async () => {
      const id = btn.getAttribute('data-comment-id');
      const li = btn.closest('li');
      if (!id || !li) {
        alert('Не знайдено ідентифікатор коментаря.');
        return;
      }
      try {
        const res = await fetch(`${ctx.baseUrl}/comments/${encodeURIComponent(id)}`, { method: 'DELETE' });
        if (res.status === 204) {
          li.remove();
          return;
        }
        if (res.status === 404) {
          alert('Коментар не знайдено.');
          return;
        }
        if (res.status === 400) {
          alert('Некоректний ідентифікатор коментаря.');
          return;
        }
        alert('Помилка видалення');
      } catch (err) {
        console.error('Помилка видалення коментаря', err);
        alert('Мережева помилка при видаленні');
      }
    });
  }

  function appendComment(c) {
    if (!c || !c.id) return;
    ctx.commentList.querySelector('.empty')?.remove();

    const li = document.createElement('li');
    li.className = 'comment-row';
    li.dataset.commentId = c.id;

    li.innerHTML = `
      <div class="comment-text">
        <div class="comment-meta">
          <span class="comment-author">${c.author}</span>
          <span class="comment-time">${formatDate(c.createdAt)}</span>
        </div>
        <div class="comment-body">${escapeHtml(c.text)}</div>
      </div>
      <button type="button" class="delete-btn" data-comment-id="${c.id}" value="${c.id}">Видалити</button>
    `;

    ctx.commentList.appendChild(li);
    wireDelete(li.querySelector('.delete-btn'));
  }

  function resolveBookId() {
    const explicit = document.getElementById('bookIdInput')?.value
      || document.querySelector('.wrapper')?.dataset?.bookId;
    if (explicit && explicit.trim()) return explicit.trim();
    const parts = location.pathname.split('/').filter(Boolean);
    return parts.length ? parts[parts.length - 1] : '';
  }

  function formatDate(raw) {
    if (raw === null || raw === undefined) return '';
    if (typeof raw === 'number') {
      const ts = raw < 1e12 ? raw * 1000 : raw;
      const d = new Date(ts);
      return Number.isNaN(d.getTime()) ? '' : d.toLocaleString([], dateFormatOptions());
    }
    const asString = String(raw);
    if (/^\d+$/.test(asString)) {
      const num = Number(asString);
      const ts = num < 1e12 ? num * 1000 : num;
      const d = new Date(ts);
      return Number.isNaN(d.getTime()) ? asString : d.toLocaleString([], dateFormatOptions());
    }
    const d = new Date(asString);
    return Number.isNaN(d.getTime()) ? asString : d.toLocaleString([], dateFormatOptions());
  }

  function dateFormatOptions() {
    return { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
  }

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.appendChild(document.createTextNode(text ?? ''));
    return div.innerHTML.replace(/\n/g, '<br>');
  }
})();
