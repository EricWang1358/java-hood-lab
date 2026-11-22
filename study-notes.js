/* Java Hood Lab - In-page question & annotation drawer (lesson pages + index hub).
   Dual-mode persistence:
   - Local companion mode (study.mjs / start-study.cmd): auto-writes feedback/questions.json & questions.md
   - Static fallback mode (file:// or GitHub Pages): localStorage + File System Access API / JSON import-export */
(function () {
  "use strict";

  const STORAGE_KEY = "java-hood-lab-feedback-v1";
  const APP_ID = "java-hood-lab-study";
  const TAGS = [
    { id: "unclear", label: "没看懂" },
    { id: "deepen", label: "想深挖" },
    { id: "example", label: "补例子" },
    { id: "errata", label: "疑似有误" },
  ];
  const TAG_MAP = Object.fromEntries(TAGS.map((t) => [t.id, t.label]));

  const isLessonPage = Boolean(document.getElementById("lesson-data") && document.getElementById("stage"));
  const isCatalogPage = Boolean(document.getElementById("catalog"));
  if (!isLessonPage && !isCatalogPage) return;

  const lesson = isLessonPage
    ? (() => {
        try {
          return JSON.parse(document.getElementById("lesson-data").textContent);
        } catch {
          return null;
        }
      })()
    : null;

  function h(tag, props, ...children) {
    const [name, ...cls] = tag.split(".");
    const el = document.createElement(name);
    if (cls.length) el.className = cls.join(" ");
    if (props) {
      for (const [k, v] of Object.entries(props)) {
        if (k === "text") el.textContent = v ?? "";
        else if (k === "html") el.innerHTML = v ?? "";
        else if (k === "on") {
          for (const [ev, fn] of Object.entries(v)) el.addEventListener(ev, fn);
        } else if (k === "attr") {
          for (const [ak, av] of Object.entries(v)) {
            if (av === false || av == null) el.removeAttribute(ak);
            else el.setAttribute(ak, String(av));
          }
        } else if (v !== undefined) {
          el[k] = v;
        }
      }
    }
    for (const child of children.flat()) {
      if (child == null || child === false) continue;
      el.append(typeof child === "string" ? document.createTextNode(child) : child);
    }
    return el;
  }

  function makeId() {
    return `q-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 6)}`;
  }

  function formatTime(iso) {
    if (!iso) return "";
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return String(iso);
    const pad = (n) => String(n).padStart(2, "0");
    return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  function injectStyles() {
    if (document.getElementById("sn-style")) return;
    const css = `
.sn-backdrop[hidden], .sn-drawer[hidden], .sn-sel-btn[hidden] { display: none !important; }
.sn-tool { position: relative; display: inline-flex; align-items: center; gap: .35rem; white-space: nowrap; }
.sn-dot { width: 6px; height: 6px; border-radius: 50%; background: var(--accent, #c03a1f); flex: none; }
.sn-hub-btn { position: relative; display: inline-flex; align-items: center; gap: .35rem; white-space: nowrap; }
.sn-sel-btn {
  position: fixed; z-index: 60; padding: .34rem .72rem; border-radius: 999px;
  border: 1px solid var(--ink, #1e1a15); background: var(--ink, #1e1a15); color: var(--canvas, #f1ede6);
  font: 600 .8rem/1.3 var(--font, system-ui, sans-serif); cursor: pointer;
  box-shadow: 0 8px 20px -6px #00000055;
}
.sn-sel-btn:hover { opacity: .9; }
.sn-backdrop {
  position: fixed; inset: 0; z-index: 70; background: #00000048;
}
.sn-drawer {
  position: fixed; top: 0; right: 0; bottom: 0; z-index: 75;
  width: min(31rem, 100vw); max-width: 100vw;
  background: var(--surface, #faf8f4); color: var(--ink, #1e1a15);
  border-left: 1px solid var(--rule-strong, #2a1c0e52);
  box-shadow: -12px 0 36px -18px #00000060;
  display: grid; grid-template-rows: auto minmax(0, 1fr);
  font-family: var(--font, system-ui, sans-serif);
}
.sn-head {
  display: flex; align-items: center; justify-content: space-between; gap: .6rem;
  padding: .85rem 1.1rem; border-bottom: 1px solid var(--rule, #2a1c0e24);
  background: var(--raised, #ffffff);
}
.sn-head__title { margin: 0; font-size: 1rem; font-weight: 700; display: flex; align-items: center; gap: .5rem; }
.sn-close {
  border: 1px solid var(--rule-strong, #2a1c0e52); background: var(--surface, #faf8f4);
  color: var(--ink, #1e1a15); border-radius: 8px; padding: .24rem .6rem; font-size: .82rem; cursor: pointer;
}
.sn-close:hover { border-color: var(--ink, #1e1a15); }
.sn-body {
  overflow-y: auto; padding: .95rem 1.1rem 1.8rem; display: grid; gap: .95rem; align-content: start;
}
.sn-status {
  display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: .5rem;
  padding: .55rem .75rem; border-radius: 9px; font-size: .78rem; line-height: 1.45;
  background: var(--sunken, #e8e3da); color: var(--ink-2, #453e35);
}
.sn-status.is-disk { border-left: 3px solid var(--ok, #1c6c48); }
.sn-status.is-local { border-left: 3px solid var(--ochre, #8a5a10); }
.sn-status__actions { display: flex; flex-wrap: wrap; gap: .35rem; }
.sn-mini-btn {
  border: 1px solid var(--rule-strong, #2a1c0e52); background: var(--raised, #ffffff);
  color: var(--ink, #1e1a15); border-radius: 7px; padding: .2rem .52rem; font-size: .75rem; cursor: pointer;
}
.sn-mini-btn:hover { border-color: var(--accent, #c03a1f); color: var(--accent-text, #ad4025); }
.sn-form {
  display: grid; gap: .65rem; padding: .85rem; border-radius: 12px;
  background: var(--raised, #ffffff); border: 1px solid var(--rule, #2a1c0e24);
}
.sn-loc {
  display: flex; align-items: center; justify-content: space-between; gap: .5rem; flex-wrap: wrap;
  font-size: .78rem; color: var(--muted, #635b51);
}
.sn-tags { display: flex; flex-wrap: wrap; gap: .35rem; }
.sn-tag-btn {
  border: 1px solid var(--rule-strong, #2a1c0e52); background: var(--surface, #faf8f4);
  color: var(--ink-2, #453e35); border-radius: 999px; padding: .22rem .65rem; font-size: .78rem; cursor: pointer;
}
.sn-tag-btn[aria-pressed="true"] {
  background: var(--ink, #1e1a15); border-color: var(--ink, #1e1a15); color: var(--canvas, #f1ede6); font-weight: 600;
}
.sn-label { font-size: .76rem; color: var(--muted, #635b51); display: grid; gap: .25rem; }
.sn-input, .sn-textarea {
  width: 100%; border: 1px solid var(--rule-strong, #2a1c0e52); border-radius: 8px;
  background: var(--surface, #faf8f4); color: var(--ink, #1e1a15);
  padding: .48rem .65rem; font: inherit; font-size: .86rem; line-height: 1.5;
}
.sn-textarea { min-height: 5.2rem; resize: vertical; }
.sn-input:focus, .sn-textarea:focus { outline: none; border-color: var(--accent, #c03a1f); }
.sn-form__foot { display: flex; justify-content: space-between; align-items: center; gap: .5rem; flex-wrap: wrap; }
.sn-hint { font-size: .74rem; color: var(--faint, #7b7368); }
.sn-primary-btn {
  border: 1px solid var(--ink, #1e1a15); background: var(--ink, #1e1a15); color: var(--canvas, #f1ede6);
  border-radius: 8px; padding: .4rem .88rem; font-size: .84rem; font-weight: 600; cursor: pointer;
}
.sn-primary-btn:hover { opacity: .88; }
.sn-tabs { display: flex; align-items: center; justify-content: space-between; gap: .5rem; flex-wrap: wrap; border-bottom: 1px solid var(--rule, #2a1c0e24); padding-bottom: .45rem; }
.sn-tab-group { display: flex; gap: .35rem; flex-wrap: wrap; }
.sn-tab {
  border: 1px solid transparent; background: none; color: var(--muted, #635b51);
  border-radius: 8px; padding: .24rem .6rem; font-size: .82rem; cursor: pointer;
}
.sn-tab.is-on {
  background: var(--raised, #ffffff); border-color: var(--rule-strong, #2a1c0e52);
  color: var(--ink, #1e1a15); font-weight: 600;
}
.sn-list { display: grid; gap: .65rem; }
.sn-empty {
  padding: 1.1rem; text-align: center; border: 1px dashed var(--rule-strong, #2a1c0e52);
  border-radius: 10px; font-size: .84rem; color: var(--muted, #635b51);
}
.sn-item {
  display: grid; gap: .42rem; padding: .75rem .85rem; border-radius: 10px;
  background: var(--raised, #ffffff); border: 1px solid var(--rule, #2a1c0e24);
  font-size: .85rem; line-height: 1.5;
}
.sn-item.is-resolved { opacity: .78; }
.sn-item__top { display: flex; align-items: center; justify-content: space-between; gap: .4rem; flex-wrap: wrap; }
.sn-item__meta { display: flex; align-items: center; gap: .35rem; flex-wrap: wrap; font-size: .75rem; color: var(--muted, #635b51); }
.sn-pill {
  display: inline-block; padding: .08rem .42rem; border-radius: 6px; font-size: .72rem; font-weight: 600;
  background: var(--sunken, #e8e3da); color: var(--ink-2, #453e35);
}
.sn-pill--here { background: var(--accent-wash, #c03a1f14); color: var(--accent-text, #ad4025); }
.sn-pill--ok { background: var(--ok-wash, #1c6c4817); color: var(--ok, #1c6c48); }
.sn-quote {
  margin: 0; padding: .3rem .55rem; border-left: 2px solid var(--rule-strong, #2a1c0e52);
  background: var(--surface, #faf8f4); color: var(--ink-2, #453e35);
  font-size: .78rem; white-space: pre-wrap; overflow-wrap: anywhere; max-height: 6.5rem; overflow: auto;
}
.sn-note { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; color: var(--ink, #1e1a15); }
.sn-res {
  margin: 0; padding: .35rem .55rem; border-left: 2px solid var(--ok, #1c6c48);
  background: var(--ok-wash, #1c6c4817); font-size: .78rem; color: var(--ink-2, #453e35); white-space: pre-wrap;
}
.sn-item__actions { display: flex; align-items: center; justify-content: space-between; gap: .4rem; flex-wrap: wrap; margin-top: .15rem; }
.sn-link-btn {
  border: 0; background: none; padding: 0; color: var(--accent-text, #ad4025);
  font-size: .78rem; font-weight: 600; cursor: pointer; text-decoration: underline; text-underline-offset: 2px;
}
.sn-card-badges { display: flex; flex-wrap: wrap; gap: .3rem; margin-top: .15rem; }
.sn-card-badge {
  display: inline-flex; align-items: center; gap: .25rem;
  font-size: .72rem; font-weight: 650; padding: .1rem .44rem; border-radius: 6px;
  background: var(--accent-wash, #c03a1f14); color: var(--accent-text, #ad4025);
}
@media (max-width: 860px) {
  .top__tools { gap: .45rem !important; }
  .sn-tool { padding: .24rem .48rem; }
}
`;
    document.head.appendChild(h("style", { id: "sn-style", text: css }));
  }

  function createStore() {
    let mode = "local"; // "disk" when study.mjs API reachable, else "local"
    let db = { version: 1, updatedAt: new Date().toISOString(), items: [] };
    let fileHandle = null;
    let notice = "";
    const listeners = new Set();

    function emit() {
      for (const fn of listeners) {
        try {
          fn();
        } catch {
          // keep UI resilient
        }
      }
    }

    function loadLocal() {
      try {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (!raw) return;
        const parsed = JSON.parse(raw);
        if (parsed && Array.isArray(parsed.items)) {
          db = {
            version: 1,
            updatedAt: parsed.updatedAt || new Date().toISOString(),
            items: parsed.items,
          };
        }
      } catch {
        // ignore broken localStorage
      }
    }

    function saveLocal() {
      try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(db));
      } catch {
        // private mode
      }
    }

    async function writeBoundFileIfAny() {
      if (!fileHandle) return;
      try {
        const writable = await fileHandle.createWritable();
        const clean = {
          version: 1,
          updatedAt: db.updatedAt,
          items: db.items.map(({ _unsynced, ...rest }) => rest),
        };
        await writable.write(`${JSON.stringify(clean, null, 2)}\n`);
        await writable.close();
        notice = "已同步写入你绑定的本地文件";
      } catch (err) {
        notice = `写入本地文件失败：${err?.message || "权限被拒绝"}`;
      }
    }

    async function apiPost(body) {
      const res = await fetch("/api/feedback", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data?.error || `HTTP ${res.status}`);
      if (data && Array.isArray(data.items)) {
        db = { version: 1, updatedAt: data.updatedAt, items: data.items };
        saveLocal();
      }
      return data;
    }

    async function init() {
      loadLocal();
      if (location.protocol === "http:" || location.protocol === "https:") {
        try {
          const res = await fetch("/api/feedback", { cache: "no-store" });
          if (res.ok) {
            const data = await res.json();
            if (data && data.app === APP_ID && Array.isArray(data.items)) {
              mode = "disk";
              const unsynced = db.items.filter((it) => it._unsynced);
              if (unsynced.length > 0) {
                await apiPost({
                  action: "merge",
                  items: unsynced.map(({ _unsynced, ...rest }) => rest),
                });
              } else {
                db = { version: 1, updatedAt: data.updatedAt, items: data.items };
                saveLocal();
              }
              emit();
              return;
            }
          }
        } catch {
          // static server without /api/feedback
        }
      }
      emit();
    }

    async function upsert(itemInput) {
      const now = new Date().toISOString();
      const idx = itemInput.id ? db.items.findIndex((x) => x.id === itemInput.id) : -1;
      const prev = idx >= 0 ? db.items[idx] : null;
      const item = {
        id: itemInput.id || prev?.id || makeId(),
        lessonId: String(itemInput.lessonId ?? prev?.lessonId ?? "").trim(),
        lessonTitle: String(itemInput.lessonTitle ?? prev?.lessonTitle ?? "").trim(),
        course: String(itemInput.course ?? prev?.course ?? "").trim(),
        slide: Number(itemInput.slide ?? prev?.slide ?? 1) || 1,
        slideTotal: Number(itemInput.slideTotal ?? prev?.slideTotal ?? 1) || 1,
        slideTitle: String(itemInput.slideTitle ?? prev?.slideTitle ?? "").trim(),
        beat: String(itemInput.beat ?? prev?.beat ?? "").trim(),
        moment: Number(itemInput.moment ?? prev?.moment ?? 0) || 0,
        teacherLine: String(itemInput.teacherLine ?? prev?.teacherLine ?? "").trim(),
        quote: String(itemInput.quote ?? prev?.quote ?? "").trim(),
        tag: itemInput.tag || prev?.tag || "unclear",
        note: String(itemInput.note ?? prev?.note ?? "").trim(),
        status: itemInput.status || prev?.status || "open",
        resolution: String(itemInput.resolution ?? prev?.resolution ?? "").trim(),
        createdAt: prev?.createdAt || now,
        updatedAt: now,
      };
      if (mode === "disk") {
        try {
          await apiPost({ action: "upsert", item });
          notice = "已写入 feedback/questions.json 与 questions.md";
          emit();
          return;
        } catch {
          mode = "local";
          notice = "伴读服务已断开，暂存于浏览器缓存";
        }
      }
      item._unsynced = true;
      if (idx >= 0) db.items[idx] = item;
      else db.items.unshift(item);
      db.updatedAt = now;
      saveLocal();
      await writeBoundFileIfAny();
      emit();
    }

    async function setStatus(id, status) {
      const now = new Date().toISOString();
      if (mode === "disk") {
        try {
          await apiPost({ action: "status", id, status });
          notice = status === "resolved" ? "已标为已解决并更新到磁盘" : "已重新打开并更新到磁盘";
          emit();
          return;
        } catch {
          mode = "local";
        }
      }
      const idx = db.items.findIndex((x) => x.id === id);
      if (idx < 0) return;
      db.items[idx] = { ...db.items[idx], status, updatedAt: now, _unsynced: true };
      db.updatedAt = now;
      saveLocal();
      await writeBoundFileIfAny();
      emit();
    }

    async function remove(id) {
      const now = new Date().toISOString();
      if (mode === "disk") {
        try {
          await apiPost({ action: "delete", id });
          notice = "已删除并同步更新到磁盘";
          emit();
          return;
        } catch {
          mode = "local";
        }
      }
      db.items = db.items.filter((x) => x.id !== id);
      db.updatedAt = now;
      saveLocal();
      await writeBoundFileIfAny();
      emit();
    }

    async function mergeExternal(items) {
      if (!Array.isArray(items)) return 0;
      const now = new Date().toISOString();
      if (mode === "disk") {
        const res = await apiPost({ action: "merge", items });
        notice = `已合并 ${res.merged || 0} 条记录到 feedback/questions.json`;
        emit();
        return res.merged || 0;
      }
      const byId = new Map(db.items.map((it) => [it.id, it]));
      let added = 0;
      for (const raw of items) {
        if (!raw || !raw.id || !raw.lessonId || !raw.note) continue;
        const prev = byId.get(raw.id);
        if (!prev || String(raw.updatedAt || "") > String(prev.updatedAt || "")) {
          byId.set(raw.id, { ...raw, _unsynced: true });
          added += 1;
        }
      }
      db.items = Array.from(byId.values()).sort((a, b) =>
        String(b.createdAt || "").localeCompare(String(a.createdAt || ""))
      );
      db.updatedAt = now;
      saveLocal();
      await writeBoundFileIfAny();
      notice = `已导入并合并 ${added} 条记录`;
      emit();
      return added;
    }

    async function bindLocalFile() {
      if (typeof window.showOpenFilePicker !== "function" && typeof window.showSaveFilePicker !== "function") {
        notice = "当前浏览器不支持直接绑定文件，请使用「导出 JSON」或双击 start-study.cmd";
        emit();
        return;
      }
      try {
        if (typeof window.showOpenFilePicker === "function") {
          const [handle] = await window.showOpenFilePicker({
            types: [{ description: "Questions JSON", accept: { "application/json": [".json"] } }],
            multiple: false,
          });
          fileHandle = handle;
          const file = await handle.getFile();
          const text = await file.text();
          if (text.trim()) {
            const parsed = JSON.parse(text);
            if (Array.isArray(parsed.items)) {
              await mergeExternal(parsed.items);
            }
          }
          await writeBoundFileIfAny();
        } else {
          fileHandle = await window.showSaveFilePicker({
            suggestedName: "questions.json",
            types: [{ description: "Questions JSON", accept: { "application/json": [".json"] } }],
          });
          await writeBoundFileIfAny();
        }
      } catch (err) {
        if (err?.name !== "AbortError") {
          notice = `绑定取消或失败：${err?.message || "未知错误"}`;
        }
      }
      emit();
    }

    function exportJsonFile() {
      const clean = {
        version: 1,
        updatedAt: db.updatedAt,
        items: db.items.map(({ _unsynced, ...rest }) => rest),
      };
      const blob = new Blob([`${JSON.stringify(clean, null, 2)}\n`], {
        type: "application/json;charset=utf-8",
      });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "questions.json";
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      notice = "已导出 questions.json（可放入 lab/feedback/questions.json）";
      emit();
    }

    function triggerImportJsonFile() {
      const input = h("input", {
        type: "file",
        accept: ".json,application/json",
        on: {
          async change() {
            const file = input.files?.[0];
            if (!file) return;
            try {
              const parsed = JSON.parse(await file.text());
              if (Array.isArray(parsed.items)) {
                await mergeExternal(parsed.items);
              } else {
                notice = "JSON 格式不正确：缺少 items 数组";
                emit();
              }
            } catch {
              notice = "无法解析所选 JSON 文件";
              emit();
            }
          },
        },
      });
      input.click();
    }

    return {
      init,
      subscribe(fn) {
        listeners.add(fn);
        return () => listeners.delete(fn);
      },
      getMode: () => mode,
      getItems: () => db.items,
      getNotice: () => notice,
      hasBoundFile: () => Boolean(fileHandle),
      upsert,
      setStatus,
      remove,
      bindLocalFile,
      exportJsonFile,
      triggerImportJsonFile,
    };
  }

  function buildAiPrompt(items) {
    const open = items.filter((it) => it.status !== "resolved");
    if (open.length === 0) {
      return "请检查 feedback/questions.json，目前没有待处理的疑问。";
    }
    const summary = open
      .slice(0, 12)
      .map(
        (it, idx) =>
          `${idx + 1}. [${TAG_MAP[it.tag] || it.tag}] ${it.lessonId} 第 ${it.slide} 页：${it.note.slice(0, 80)}`
      )
      .join("\n");
    const more = open.length > 12 ? `\n……其余共 ${open.length} 条详见 feedback/questions.json。` : "";
    return [
      `请读取 lab/feedback/questions.json 中所有 status 为 "open" 的 ${open.length} 条学习疑问：`,
      summary + more,
      "",
      "请按标签分类处理：",
      "1)「没看懂 / 疑似有误 / 小例子」直接优化对应 lessons/<id>.json 那一页的讲解或术语并重编网页（只改文字时用 --no-run）；",
      "2) 需要独立实验才能讲透的「想深挖」请按同套规范新增补充课程与实测代码；",
      "3) 全部处理完后，把对应条目的 status 改为 \"resolved\"、补充简明 resolution，并运行 node study.mjs --render-md 同步 feedback/questions.md。",
    ].join("\n");
  }

  async function copyText(text) {
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch {
      const ta = h("textarea", { value: text });
      ta.style.position = "fixed";
      ta.style.opacity = "0";
      document.body.appendChild(ta);
      ta.select();
      let ok = false;
      try {
        ok = document.execCommand("copy");
      } catch {
        ok = false;
      }
      ta.remove();
      return ok;
    }
  }

  function getCurrentLessonContext() {
    if (!lesson) return null;
    let slideIdx = 0;
    let momentIdx = 0;
    try {
      const raw = localStorage.getItem(`lesson:${lesson.id}`);
      if (raw) {
        const parsed = JSON.parse(raw);
        if (parsed?.at && typeof parsed.at.s === "number") {
          slideIdx = Math.max(0, Math.min(parsed.at.s, lesson.slides.length - 1));
          momentIdx = Math.max(0, Number(parsed.at.m) || 0);
        }
      }
    } catch {
      // fallback below
    }
    const countEl = document.getElementById("count");
    const m = countEl && /(\d+)\s*\/\s*(\d+)/.exec(countEl.textContent || "");
    if (m) {
      slideIdx = Math.max(0, Math.min(Number(m[1]) - 1, lesson.slides.length - 1));
    }
    const slideObj = lesson.slides[slideIdx] || {};
    const beatObj = (lesson.beats || []).find((b) => b.id === slideObj.beat);
    const lineEl = document.getElementById("line");
    return {
      lessonId: lesson.id,
      lessonTitle: lesson.title,
      course: lesson.course || "",
      slide: slideIdx + 1,
      slideTotal: lesson.slides.length,
      slideTitle: slideObj.title || "",
      beat: beatObj?.label || slideObj.beat || "",
      moment: momentIdx,
      teacherLine: (lineEl?.textContent || "").trim(),
    };
  }

  function renderStatusBar(store, extraAction) {
    const mode = store.getMode();
    const isDisk = mode === "disk";
    const notice = store.getNotice();
    const label = isDisk
      ? "本地伴读模式：提交后自动写入 feedback/questions.json 与 questions.md"
      : store.hasBoundFile()
        ? "静态模式：已绑定本地 JSON，保存后自动覆盖写入"
        : location.protocol === "file:"
          ? "静态打开模式（暂存浏览器）：推荐双击 start-study.cmd 打开以自动写盘"
          : "静态模式（暂存浏览器）：可绑定本地文件或导出 questions.json";

    const bar = h(
      `div.sn-status.${isDisk ? "is-disk" : "is-local"}`,
      null,
      h("div", null, h("div", { text: label }), notice ? h("div", { text: notice }) : null),
      h(
        "div.sn-status__actions",
        null,
        !isDisk &&
          h("button.sn-mini-btn", {
            type: "button",
            text: store.hasBoundFile() ? "重绑文件" : "绑定本地 JSON",
            on: { click: () => store.bindLocalFile() },
          }),
        h("button.sn-mini-btn", {
          type: "button",
          text: "导出 JSON",
          on: { click: () => store.exportJsonFile() },
        }),
        h("button.sn-mini-btn", {
          type: "button",
          text: "导入",
          on: { click: () => store.triggerImportJsonFile() },
        }),
        extraAction || null
      )
    );
    return bar;
  }

  function initLessonUi(store) {
    const topTools = document.querySelector(".top__tools");
    const themeBtn = document.getElementById("theme");
    if (!topTools) return;

    const btnDot = h("span.sn-dot", { hidden: true });
    const btnText = h("span", { text: "疑问" });
    const openBtn = h(
      "button.tool.sn-tool",
      {
        id: "sn-btn",
        type: "button",
        title: "记录或查看本节疑问（快捷键 Q）",
        on: { click: () => openDrawer() },
      },
      btnDot,
      btnText
    );
    if (themeBtn && themeBtn.parentElement === topTools) {
      topTools.insertBefore(openBtn, themeBtn);
    } else {
      topTools.appendChild(openBtn);
    }

    let pendingQuote = "";
    const selBtn = h("button.sn-sel-btn", {
      type: "button",
      hidden: true,
      text: "＋ 记疑问",
      on: {
        mousedown(e) {
          e.preventDefault();
          const sel = window.getSelection();
          if (sel) pendingQuote = sel.toString().trim().slice(0, 800);
        },
        click() {
          selBtn.hidden = true;
          openDrawer(pendingQuote);
          pendingQuote = "";
        },
      },
    });
    document.body.appendChild(selBtn);

    const backdrop = h("div.sn-backdrop", {
      hidden: true,
      on: {
        click: () => closeDrawer(),
        keydown: (e) => e.stopPropagation(),
      },
    });
    const drawer = h("aside.sn-drawer", {
      hidden: true,
      attr: { role: "dialog", "aria-modal": "true", "aria-label": "学习疑问笔记本" },
      on: {
        keydown(e) {
          if (!isOpen) return;
          e.stopPropagation();
          if (e.key === "Escape") {
            e.preventDefault();
            closeDrawer();
          }
        },
      },
    });
    document.body.append(backdrop, drawer);

    let isOpen = false;
    let selectedTag = "unclear";
    let editingId = null;
    let activeTab = "lesson"; // "lesson" | "all"
    let draftQuote = "";
    let draftNote = "";

    function updateTopButton() {
      const ctx = getCurrentLessonContext();
      const items = store.getItems();
      const lessonOpen = items.filter((it) => it.lessonId === lesson.id && it.status !== "resolved");
      const lessonAll = items.filter((it) => it.lessonId === lesson.id);
      const hereOpen = ctx
        ? lessonOpen.some((it) => Number(it.slide) === Number(ctx.slide))
        : false;
      btnText.textContent = lessonOpen.length > 0 ? `疑问 ${lessonOpen.length}` : lessonAll.length > 0 ? `疑问 (${lessonAll.length})` : "疑问";
      btnDot.hidden = !hereOpen;
    }

    function navigateToLessonSlide(targetLessonId, slideNum, momentNum = 0) {
      const suffix = momentNum > 0 ? `#${slideNum}.end` : `#${slideNum}`;
      if (targetLessonId === lesson.id) {
        try {
          const key = `lesson:${lesson.id}`;
          const prev = JSON.parse(localStorage.getItem(key) || "{}");
          prev.at = { s: Math.max(0, Number(slideNum) - 1), m: momentNum > 0 ? 9999 : 0 };
          localStorage.setItem(key, JSON.stringify(prev));
        } catch {
          // ignore
        }
        location.hash = suffix;
        location.reload();
      } else {
        location.href = `./${encodeURIComponent(targetLessonId)}.html${suffix}`;
      }
    }

    function renderDrawer(opts = {}) {
      if (!isOpen) return;
      const ctx = getCurrentLessonContext();
      const items = store.getItems();
      const lessonItems = items.filter((it) => it.lessonId === lesson.id);
      const allOpenItems = items.filter((it) => it.status !== "resolved");

      let copyHint = "";
      const copyPromptBtn = h("button.sn-mini-btn", {
        type: "button",
        text: "复制 AI 指令",
        on: {
          async click() {
            const ok = await copyText(buildAiPrompt(items));
            copyHint = ok ? "已复制提示词，可直接发给 AI" : "复制失败，请手动查看 feedback/questions.md";
            copyPromptBtn.textContent = copyHint;
            setTimeout(() => {
              copyPromptBtn.textContent = "复制 AI 指令";
            }, 2200);
          },
        },
      });

      const quoteInput = h("textarea.sn-input", {
        rows: 2,
        placeholder: "可空着；或在页面选中文字/代码后点「＋ 记疑问」自动带入",
        value: draftQuote,
        on: {
          input(e) {
            draftQuote = e.target.value;
          },
        },
      });

      const noteInput = h("textarea.sn-textarea", {
        placeholder: "写下你没看懂的地方、想深挖的问题，或觉得不严谨的地方…",
        value: draftNote,
        on: {
          input(e) {
            draftNote = e.target.value;
          },
          keydown(e) {
            if ((e.ctrlKey || e.metaKey) && e.key === "Enter") {
              e.preventDefault();
              submitForm();
            }
          },
        },
      });

      async function submitForm() {
        const trimmedNote = draftNote.trim();
        if (!trimmedNote) {
          noteInput.focus();
          return;
        }
        const liveCtx = getCurrentLessonContext();
        const editingItem = editingId ? items.find((x) => x.id === editingId) : null;
        await store.upsert({
          id: editingId || undefined,
          lessonId: editingItem ? editingItem.lessonId : liveCtx.lessonId,
          lessonTitle: editingItem ? editingItem.lessonTitle : liveCtx.lessonTitle,
          course: editingItem ? editingItem.course : liveCtx.course,
          slide: editingItem ? editingItem.slide : liveCtx.slide,
          slideTotal: editingItem ? editingItem.slideTotal : liveCtx.slideTotal,
          slideTitle: editingItem ? editingItem.slideTitle : liveCtx.slideTitle,
          beat: editingItem ? editingItem.beat : liveCtx.beat,
          moment: editingItem ? editingItem.moment : liveCtx.moment,
          teacherLine: editingItem ? editingItem.teacherLine : liveCtx.teacherLine,
          quote: draftQuote.trim(),
          tag: selectedTag,
          note: trimmedNote,
          status: editingItem ? editingItem.status : "open",
        });
        editingId = null;
        draftQuote = "";
        draftNote = "";
        renderDrawer();
      }

      const form = h(
        "form.sn-form",
        {
          on: {
            submit(e) {
              e.preventDefault();
              submitForm();
            },
          },
        },
        h(
          "div.sn-loc",
          null,
          h("span", {
            text: editingId
              ? "正在编辑已有疑问"
              : `当前定位：第 ${ctx.slide}/${ctx.slideTotal} 页${ctx.beat ? ` · ${ctx.beat}` : ""}`,
          }),
          ctx.teacherLine &&
            h("button.sn-mini-btn", {
              type: "button",
              text: "带入当前讲解句",
              on: {
                click() {
                  draftQuote = ctx.teacherLine;
                  quoteInput.value = draftQuote;
                  noteInput.focus();
                },
              },
            })
        ),
        h(
          "div.sn-tags",
          null,
          TAGS.map((t) =>
            h("button.sn-tag-btn", {
              type: "button",
              text: t.label,
              attr: { "aria-pressed": selectedTag === t.id ? "true" : "false" },
              on: {
                click() {
                  selectedTag = t.id;
                  renderDrawer();
                },
              },
            })
          )
        ),
        h("label.sn-label", null, "引用片段 / 代码（可选）", quoteInput),
        h("label.sn-label", null, editingId ? "修改疑问内容" : "我的疑问 / 建议补充", noteInput),
        h(
          "div.sn-form__foot",
          null,
          h("span.sn-hint", { text: "Ctrl + Enter 快速保存 · Esc 关闭" }),
          h(
            "div",
            { style: "display:flex;gap:.4rem;" },
            editingId &&
              h("button.sn-mini-btn", {
                type: "button",
                text: "取消编辑",
                on: {
                  click() {
                    editingId = null;
                    draftQuote = "";
                    draftNote = "";
                    renderDrawer();
                  },
                },
              }),
            h("button.sn-primary-btn", { type: "submit", text: editingId ? "更新疑问" : "保存疑问" })
          )
        )
      );

      const shownList = activeTab === "lesson" ? lessonItems : allOpenItems;
      const listEl = h(
        "div.sn-list",
        null,
        shownList.length === 0
          ? h("div.sn-empty", {
              text:
                activeTab === "lesson"
                  ? "本节还没有记疑问。看课时遇到卡点或想深挖的点随时记在这里。"
                  : "目前没有全书待处理疑问。",
            })
          : shownList.map((it) => {
              const isHere = it.lessonId === lesson.id && Number(it.slide) === Number(ctx.slide);
              const isResolved = it.status === "resolved";
              return h(
                `article.sn-item${isResolved ? ".is-resolved" : ""}`,
                null,
                h(
                  "div.sn-item__top",
                  null,
                  h(
                    "div.sn-item__meta",
                    null,
                    h("span.sn-pill", { text: TAG_MAP[it.tag] || it.tag }),
                    isHere && h("span.sn-pill.sn-pill--here", { text: "本页" }),
                    isResolved && h("span.sn-pill.sn-pill--ok", { text: "已处理" }),
                    h("span", {
                      text:
                        activeTab === "all"
                          ? `${it.lessonTitle || it.lessonId} · 第 ${it.slide} 页`
                          : `第 ${it.slide} 页${it.slideTitle ? ` · ${it.slideTitle}` : ""}`,
                    })
                  ),
                  h("span.sn-hint", { text: formatTime(it.createdAt) })
                ),
                it.quote && h("blockquote.sn-quote", { text: it.quote }),
                h("p.sn-note", { text: it.note }),
                it.resolution && h("p.sn-res", { text: `AI 处理说明：${it.resolution}` }),
                h(
                  "div.sn-item__actions",
                  null,
                  h("button.sn-link-btn", {
                    type: "button",
                    text: it.lessonId === lesson.id ? `跳到第 ${it.slide} 页` : `去 ${it.lessonId} 第 ${it.slide} 页 →`,
                    on: { click: () => navigateToLessonSlide(it.lessonId, it.slide, it.moment) },
                  }),
                  h(
                    "div",
                    { style: "display:flex;gap:.35rem;flex-wrap:wrap;" },
                    h("button.sn-mini-btn", {
                      type: "button",
                      text: "编辑",
                      on: {
                        click() {
                          editingId = it.id;
                          selectedTag = it.tag || "unclear";
                          draftQuote = it.quote || "";
                          draftNote = it.note || "";
                          renderDrawer({ focusNote: true });
                          const bodyEl = drawer.querySelector(".sn-body");
                          if (bodyEl) bodyEl.scrollTop = 0;
                        },
                      },
                    }),
                    h("button.sn-mini-btn", {
                      type: "button",
                      text: isResolved ? "重开" : "已解决",
                      on: { click: () => store.setStatus(it.id, isResolved ? "open" : "resolved") },
                    }),
                    h("button.sn-mini-btn", {
                      type: "button",
                      text: "删除",
                      on: { click: () => store.remove(it.id) },
                    })
                  )
                )
              );
            })
      );

      drawer.replaceChildren(
        h(
          "header.sn-head",
          null,
          h("h2.sn-head__title", null, "学习疑问", h("span.sn-pill", { text: lesson.id })),
          h("button.sn-close", { type: "button", text: "关闭", on: { click: closeDrawer } })
        ),
        h(
          "div.sn-body",
          null,
          renderStatusBar(store, copyPromptBtn),
          form,
          h(
            "div.sn-tabs",
            null,
            h(
              "div.sn-tab-group",
              null,
              h("button.sn-tab" + (activeTab === "lesson" ? ".is-on" : ""), {
                type: "button",
                text: `本节 (${lessonItems.length})`,
                on: {
                  click() {
                    activeTab = "lesson";
                    renderDrawer();
                  },
                },
              }),
              h("button.sn-tab" + (activeTab === "all" ? ".is-on" : ""), {
                type: "button",
                text: `全部待处理 (${allOpenItems.length})`,
                on: {
                  click() {
                    activeTab = "all";
                    renderDrawer();
                  },
                },
              })
            )
          ),
          listEl
        )
      );

      if (opts.focusNote) {
        requestAnimationFrame(() => noteInput.focus());
      }
    }

    function openDrawer(initialQuote) {
      if (typeof initialQuote === "string" && initialQuote.trim()) {
        draftQuote = initialQuote.trim();
      }
      isOpen = true;
      backdrop.hidden = false;
      drawer.hidden = false;
      renderDrawer({ focusNote: true });
    }

    function closeDrawer() {
      const hadFocus = drawer.contains(document.activeElement);
      isOpen = false;
      backdrop.hidden = true;
      drawer.hidden = true;
      if (hadFocus && typeof openBtn?.focus === "function") {
        openBtn.focus({ preventScroll: true });
      }
    }

    function checkSelection() {
      if (isOpen) {
        selBtn.hidden = true;
        return;
      }
      const sel = window.getSelection();
      if (!sel || sel.isCollapsed) {
        selBtn.hidden = true;
        return;
      }
      const text = sel.toString().trim();
      if (text.length < 2 || text.length > 800) {
        selBtn.hidden = true;
        return;
      }
      const stageEl = document.getElementById("stage");
      const lineEl = document.getElementById("line");
      const anchor = sel.anchorNode;
      const inLessonArea =
        (stageEl && stageEl.contains(anchor)) || (lineEl && lineEl.contains(anchor));
      if (!inLessonArea || drawer.contains(anchor)) {
        selBtn.hidden = true;
        return;
      }
      try {
        const range = sel.getRangeAt(0);
        const rect = range.getBoundingClientRect();
        if (!rect || (rect.width === 0 && rect.height === 0)) {
          selBtn.hidden = true;
          return;
        }
        selBtn.hidden = false;
        const left = Math.max(8, Math.min(rect.right - 90, window.innerWidth - 110));
        const top = rect.top > 48 ? rect.top - 36 : Math.min(rect.bottom + 8, window.innerHeight - 44);
        selBtn.style.left = `${Math.round(left)}px`;
        selBtn.style.top = `${Math.round(top)}px`;
      } catch {
        selBtn.hidden = true;
      }
    }

    document.addEventListener("mouseup", () => setTimeout(checkSelection, 15));
    document.addEventListener("keyup", () => setTimeout(checkSelection, 15));
    document.addEventListener("keydown", (e) => {
      if (e.altKey || e.ctrlKey || e.metaKey) return;
      if (isOpen && e.key === "Escape") {
        e.preventDefault();
        e.stopPropagation();
        closeDrawer();
        return;
      }
      const target = e.target;
      if (target && target.closest?.("input, textarea, select, [contenteditable]")) return;
      if (!isOpen && (e.key === "q" || e.key === "Q" || e.key === "?")) {
        e.preventDefault();
        openDrawer();
      }
    });

    const countEl = document.getElementById("count");
    if (countEl) {
      new MutationObserver(() => {
        updateTopButton();
        if (isOpen && !editingId) renderDrawer();
      }).observe(countEl, { childList: true, characterData: true, subtree: true });
    }

    store.subscribe(() => {
      updateTopButton();
      if (isOpen) renderDrawer();
    });
    updateTopButton();
  }

  function initCatalogUi(store) {
    const actions = document.querySelector(".topbar .actions");
    const themeBtn = document.getElementById("themeBtn");
    if (!actions) return;

    const hubBtn = h("button.btn.sn-hub-btn", {
      id: "sn-hub-btn",
      type: "button",
      text: "我的疑问",
      on: { click: () => openHub() },
    });
    if (themeBtn && themeBtn.parentElement === actions) {
      actions.insertBefore(hubBtn, themeBtn);
    } else {
      actions.appendChild(hubBtn);
    }

    const backdrop = h("div.sn-backdrop", {
      hidden: true,
      on: { click: () => closeHub(), keydown: (e) => e.stopPropagation() },
    });
    const drawer = h("aside.sn-drawer", {
      hidden: true,
      attr: { role: "dialog", "aria-modal": "true", "aria-label": "我的学习疑问看板" },
      on: {
        keydown(e) {
          if (!isOpen) return;
          e.stopPropagation();
          if (e.key === "Escape") {
            e.preventDefault();
            closeHub();
          }
        },
      },
    });
    document.body.append(backdrop, drawer);

    let isOpen = false;
    let statusFilter = "open"; // "open" | "resolved" | "all"
    let tagFilter = "all";

    function updateCardsAndButton() {
      const items = store.getItems();
      const openItems = items.filter((it) => it.status !== "resolved");
      hubBtn.textContent = openItems.length > 0 ? `我的疑问 (${openItems.length})` : "我的疑问";

      const countsByLesson = new Map();
      for (const it of openItems) {
        countsByLesson.set(it.lessonId, (countsByLesson.get(it.lessonId) || 0) + 1);
      }

      document.querySelectorAll("#catalog .card").forEach((card) => {
        const href = card.getAttribute("href") || "";
        const m = /pages\/([^/.?#]+)\.html/.exec(href);
        if (!m) return;
        const lessonId = m[1];
        const count = countsByLesson.get(lessonId) || 0;
        let box = card.querySelector(".sn-card-badges");
        if (count === 0) {
          if (box) box.remove();
          return;
        }
        if (!box) {
          box = h("div.sn-card-badges");
          const foot = card.querySelector(".card__foot");
          if (foot) card.insertBefore(box, foot);
          else card.appendChild(box);
        }
        box.replaceChildren(h("span.sn-card-badge", { text: `${count} 条待处理疑问` }));
      });
    }

    function renderHub() {
      if (!isOpen) return;
      const items = store.getItems();
      const openCount = items.filter((it) => it.status !== "resolved").length;
      const resolvedCount = items.length - openCount;

      const copyBtn = h("button.sn-mini-btn", {
        type: "button",
        text: "复制 AI 处理指令",
        on: {
          async click() {
            const ok = await copyText(buildAiPrompt(items));
            copyBtn.textContent = ok ? "已复制！可发给 AI" : "复制失败";
            setTimeout(() => {
              copyBtn.textContent = "复制 AI 处理指令";
            }, 2200);
          },
        },
      });

      const filtered = items.filter((it) => {
        const matchStatus =
          statusFilter === "all" ||
          (statusFilter === "open" ? it.status !== "resolved" : it.status === "resolved");
        const matchTag = tagFilter === "all" || it.tag === tagFilter;
        return matchStatus && matchTag;
      });

      drawer.replaceChildren(
        h(
          "header.sn-head",
          null,
          h("h2.sn-head__title", null, "我的学习疑问", h("span.sn-pill", { text: `${openCount} 待处理` })),
          h("button.sn-close", { type: "button", text: "关闭", on: { click: closeHub } })
        ),
        h(
          "div.sn-body",
          null,
          renderStatusBar(store, copyBtn),
          h(
            "div.sn-tabs",
            null,
            h(
              "div.sn-tab-group",
              null,
              h("button.sn-tab" + (statusFilter === "open" ? ".is-on" : ""), {
                type: "button",
                text: `待处理 (${openCount})`,
                on: {
                  click() {
                    statusFilter = "open";
                    renderHub();
                  },
                },
              }),
              h("button.sn-tab" + (statusFilter === "resolved" ? ".is-on" : ""), {
                type: "button",
                text: `已处理 (${resolvedCount})`,
                on: {
                  click() {
                    statusFilter = "resolved";
                    renderHub();
                  },
                },
              }),
              h("button.sn-tab" + (statusFilter === "all" ? ".is-on" : ""), {
                type: "button",
                text: `全部 (${items.length})`,
                on: {
                  click() {
                    statusFilter = "all";
                    renderHub();
                  },
                },
              })
            )
          ),
          h(
            "div.sn-tags",
            null,
            [{ id: "all", label: "全部类型" }, ...TAGS].map((t) =>
              h("button.sn-tag-btn", {
                type: "button",
                text: t.label,
                attr: { "aria-pressed": tagFilter === t.id ? "true" : "false" },
                on: {
                  click() {
                    tagFilter = t.id;
                    renderHub();
                  },
                },
              })
            )
          ),
          h(
            "div.sn-list",
            null,
            filtered.length === 0
              ? h("div.sn-empty", {
                  text: "当前筛选条件下没有疑问记录。点开任意一节课件，按 Q 或点击右上角「疑问」即可随时记录。",
                })
              : filtered.map((it) => {
                  const isResolved = it.status === "resolved";
                  const suffix = it.moment > 0 ? `#${it.slide}.end` : `#${it.slide}`;
                  return h(
                    `article.sn-item${isResolved ? ".is-resolved" : ""}`,
                    null,
                    h(
                      "div.sn-item__top",
                      null,
                      h(
                        "div.sn-item__meta",
                        null,
                        h("span.sn-pill", { text: TAG_MAP[it.tag] || it.tag }),
                        isResolved && h("span.sn-pill.sn-pill--ok", { text: "已处理" }),
                        h("span", {
                          text: `${it.lessonTitle || it.lessonId} · 第 ${it.slide} 页`,
                        })
                      ),
                      h("span.sn-hint", { text: formatTime(it.createdAt) })
                    ),
                    it.quote && h("blockquote.sn-quote", { text: it.quote }),
                    h("p.sn-note", { text: it.note }),
                    it.resolution && h("p.sn-res", { text: `AI 处理说明：${it.resolution}` }),
                    h(
                      "div.sn-item__actions",
                      null,
                      h("a.sn-link-btn", {
                        href: `./pages/${encodeURIComponent(it.lessonId)}.html${suffix}`,
                        text: `跳到该课第 ${it.slide} 页 →`,
                      }),
                      h(
                        "div",
                        { style: "display:flex;gap:.35rem;" },
                        h("button.sn-mini-btn", {
                          type: "button",
                          text: isResolved ? "重开" : "已解决",
                          on: { click: () => store.setStatus(it.id, isResolved ? "open" : "resolved") },
                        }),
                        h("button.sn-mini-btn", {
                          type: "button",
                          text: "删除",
                          on: { click: () => store.remove(it.id) },
                        })
                      )
                    )
                  );
                })
          )
        )
      );
    }

    function openHub() {
      isOpen = true;
      backdrop.hidden = false;
      drawer.hidden = false;
      renderHub();
    }

    function closeHub() {
      const hadFocus = drawer.contains(document.activeElement);
      isOpen = false;
      backdrop.hidden = true;
      drawer.hidden = true;
      if (hadFocus && typeof hubBtn?.focus === "function") {
        hubBtn.focus({ preventScroll: true });
      }
    }

    document.addEventListener("keydown", (e) => {
      if (isOpen && e.key === "Escape") {
        e.preventDefault();
        e.stopPropagation();
        closeHub();
      }
    });

    store.subscribe(() => {
      updateCardsAndButton();
      if (isOpen) renderHub();
    });
    updateCardsAndButton();
  }

  injectStyles();
  const store = createStore();
  if (isLessonPage && lesson) initLessonUi(store);
  else if (isCatalogPage) initCatalogUi(store);
  store.init();
})();
