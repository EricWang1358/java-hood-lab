#!/usr/bin/env node
/**
 * Java Hood Lab local study companion server & feedback sync CLI.
 * Zero external dependencies (Node.js 18+ built-ins only).
 *
 * Usage:
 *   node study.mjs                  Start server on http://127.0.0.1:8310 and open browser
 *   node study.mjs --no-open        Start server without opening browser
 *   node study.mjs --port 4320      Start server on custom port
 *   node study.mjs --sync-pages     Inject study-notes.js into index.html & pages/*.html
 *   node study.mjs --render-md      Regenerate feedback/questions.md from feedback/questions.json
 */
import http from "node:http";
import fs from "node:fs/promises";
import path from "node:path";
import { spawn } from "node:child_process";
import { fileURLToPath } from "node:url";

const ROOT = path.dirname(fileURLToPath(import.meta.url));
const FEEDBACK_DIR = path.join(ROOT, "feedback");
const JSON_PATH = path.join(FEEDBACK_DIR, "questions.json");
const MD_PATH = path.join(FEEDBACK_DIR, "questions.md");
const PAGES_DIR = path.join(ROOT, "pages");
const INDEX_PATH = path.join(ROOT, "index.html");

const APP_ID = "java-hood-lab-study";
const DEFAULT_PORT = 8310;
const VALID_TAGS = new Set(["unclear", "deepen", "example", "errata"]);
const TAG_LABELS = {
  unclear: "没看懂",
  deepen: "想深挖",
  example: "补例子",
  errata: "疑似有误",
};

const MIME_TYPES = {
  ".html": "text/html; charset=utf-8",
  ".js": "application/javascript; charset=utf-8",
  ".mjs": "application/javascript; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".md": "text/markdown; charset=utf-8",
  ".txt": "text/plain; charset=utf-8",
  ".java": "text/plain; charset=utf-8",
  ".sql": "text/plain; charset=utf-8",
  ".svg": "image/svg+xml",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".webp": "image/webp",
  ".ico": "image/x-icon",
};

let writeQueue = Promise.resolve();

function enqueueWrite(task) {
  const next = writeQueue.then(task, task);
  writeQueue = next.catch(() => {});
  return next;
}

function nowIso() {
  return new Date().toISOString();
}

function makeId() {
  return `q-${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 6)}`;
}

function cleanStr(val, maxLen = 4000) {
  if (val == null) return "";
  return String(val).replace(/\r\n/g, "\n").trim().slice(0, maxLen);
}

function toPosInt(val, fallback = 1) {
  const n = Number(val);
  return Number.isFinite(n) && n >= 1 ? Math.floor(n) : fallback;
}

function toNonNegInt(val, fallback = 0) {
  const n = Number(val);
  return Number.isFinite(n) && n >= 0 ? Math.floor(n) : fallback;
}

function normalizeItem(raw, existing = null, stamp = nowIso()) {
  const src = raw && typeof raw === "object" ? raw : {};
  const tag = VALID_TAGS.has(src.tag) ? src.tag : (existing?.tag || "unclear");
  const status = src.status === "resolved" ? "resolved" : (src.status === "open" ? "open" : (existing?.status || "open"));
  return {
    id: cleanStr(src.id, 80) || existing?.id || makeId(),
    lessonId: cleanStr(src.lessonId, 120) || existing?.lessonId || "",
    lessonTitle: cleanStr(src.lessonTitle, 240) || existing?.lessonTitle || "",
    course: cleanStr(src.course, 120) || existing?.course || "",
    slide: src.slide != null ? toPosInt(src.slide, 1) : (existing?.slide || 1),
    slideTotal: src.slideTotal != null ? toPosInt(src.slideTotal, 1) : (existing?.slideTotal || 1),
    slideTitle: cleanStr(src.slideTitle, 240) || existing?.slideTitle || "",
    beat: cleanStr(src.beat, 80) || existing?.beat || "",
    moment: src.moment != null ? toNonNegInt(src.moment, 0) : (existing?.moment ?? 0),
    teacherLine: src.teacherLine !== undefined ? cleanStr(src.teacherLine, 1200) : (existing?.teacherLine || ""),
    quote: src.quote !== undefined ? cleanStr(src.quote, 1200) : (existing?.quote || ""),
    tag,
    note: src.note !== undefined ? cleanStr(src.note, 4000) : (existing?.note || ""),
    status,
    resolution: src.resolution !== undefined ? cleanStr(src.resolution, 4000) : (existing?.resolution || ""),
    createdAt: cleanStr(src.createdAt, 64) || existing?.createdAt || stamp,
    updatedAt: stamp,
  };
}

function emptyDb(stamp = nowIso()) {
  return { version: 1, updatedAt: stamp, items: [] };
}

function formatTime(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return String(iso);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function blockquoteLines(text) {
  return String(text || "")
    .replace(/\r\n/g, "\n")
    .split("\n")
    .map((line) => `  > ${line}`)
    .join("\n");
}

function inlineText(text) {
  return String(text || "").replace(/\r\n/g, "\n").replace(/\n+/g, "\n  ");
}

export function renderMarkdown(db) {
  const items = Array.isArray(db?.items) ? db.items : [];
  const openItems = items
    .filter((it) => it.status !== "resolved")
    .sort((a, b) => String(b.createdAt || "").localeCompare(String(a.createdAt || "")));
  const resolvedItems = items
    .filter((it) => it.status === "resolved")
    .sort((a, b) => String(b.updatedAt || "").localeCompare(String(a.updatedAt || "")));

  const lines = [
    "# 学习疑问与教材修订清单",
    "",
    "> 本文件与 [`questions.json`](./questions.json) 由页内「记疑问」面板与 `study.mjs` 自动同步维护。",
    "> 读完一批课程后，可以直接把本清单（或对 AI 说「按 `feedback/questions.json` 里的待处理疑问优化教材或新增补充课」）交给 AI 处理；AI 改完 `lessons/*.json` 后，将对应条目 `status` 设为 `\"resolved\"`、补充 `resolution` 并运行 `node study.mjs --render-md` 即可同步更新本页。",
    "",
    `- **统计**：待处理 **${openItems.length}** 条 · 已处理 **${resolvedItems.length}** 条 · 最近更新：${formatTime(db?.updatedAt) || "—"}`,
    "",
    "## 待处理疑问 (Open)",
    "",
  ];

  const pushItem = (it) => {
    const tagLabel = TAG_LABELS[it.tag] || it.tag || "疑问";
    const statusLabel = it.status === "resolved" ? "已处理" : "待处理";
    const titleText = it.lessonTitle || it.lessonId || "未命名课程";
    const slidePart = `第 ${it.slide}/${it.slideTotal || "?"} 页`;
    lines.push(`### [${tagLabel}] ${titleText}（\`${it.lessonId}\` ${slidePart}）`);
    lines.push("");
    lines.push(
      `- 跳转：[打开第 ${it.slide} 页](../pages/${it.lessonId}.html#${it.slide}) · 状态：${statusLabel} · 记录于：${formatTime(it.createdAt)} · ID：\`${it.id}\``
    );
    const locParts = [];
    if (it.beat) locParts.push(`阶段「${it.beat}」`);
    if (it.slideTitle) locParts.push(it.slideTitle);
    locParts.push(`第 ${(Number(it.moment) || 0) + 1} 步`);
    lines.push(`- 位置：${locParts.join(" / ")}`);
    if (it.quote) {
      lines.push("- 选中原文/代码：");
      lines.push(blockquoteLines(it.quote));
    }
    if (it.teacherLine) {
      lines.push("- 当时讲解：");
      lines.push(blockquoteLines(it.teacherLine));
    }
    lines.push(`- **我的疑问**：${inlineText(it.note)}`);
    if (it.status === "resolved" || it.resolution) {
      lines.push(`- **AI 处理说明**：${inlineText(it.resolution || "（已处理）")}`);
    }
    lines.push("");
  };

  if (openItems.length === 0) {
    lines.push("暂无待处理疑问。", "");
  } else {
    openItems.forEach(pushItem);
  }

  lines.push("## 已处理归档 (Resolved)", "");
  if (resolvedItems.length === 0) {
    lines.push("暂无已处理记录。", "");
  } else {
    resolvedItems.forEach(pushItem);
  }

  return `${lines.join("\n").trimEnd()}\n`;
}

async function atomicWrite(targetPath, content) {
  await fs.mkdir(path.dirname(targetPath), { recursive: true });
  const tmpPath = `${targetPath}.${process.pid}.${Date.now()}.tmp`;
  await fs.writeFile(tmpPath, content, "utf8");
  await fs.rename(tmpPath, targetPath);
}

async function readDbUnsafe() {
  try {
    const raw = await fs.readFile(JSON_PATH, "utf8");
    const parsed = JSON.parse(raw);
    const items = Array.isArray(parsed.items)
      ? parsed.items.map((it) => normalizeItem(it, it, cleanStr(it.updatedAt, 64) || nowIso()))
      : [];
    return {
      version: 1,
      updatedAt: cleanStr(parsed.updatedAt, 64) || nowIso(),
      items,
    };
  } catch (err) {
    if (err && err.code === "ENOENT") {
      return emptyDb();
    }
    throw err;
  }
}

async function ensureFeedbackFiles() {
  return enqueueWrite(async () => {
    let created = false;
    try {
      await fs.access(JSON_PATH);
    } catch {
      const db = emptyDb();
      await atomicWrite(JSON_PATH, `${JSON.stringify(db, null, 2)}\n`);
      await atomicWrite(MD_PATH, renderMarkdown(db));
      created = true;
    }
    if (!created) {
      try {
        await fs.access(MD_PATH);
      } catch {
        const db = await readDbUnsafe();
        await atomicWrite(MD_PATH, renderMarkdown(db));
      }
    }
    return created;
  });
}

async function saveDbUnsafe(db) {
  const jsonText = `${JSON.stringify(db, null, 2)}\n`;
  const mdText = renderMarkdown(db);
  await atomicWrite(JSON_PATH, jsonText);
  await atomicWrite(MD_PATH, mdText);
  return db;
}

async function injectScriptTag(filePath, scriptSrc) {
  const raw = await fs.readFile(filePath, "utf8");
  if (/study-notes\.js/i.test(raw)) {
    return false;
  }
  const eol = raw.includes("\r\n") ? "\r\n" : "\n";
  const tag = `<script src="${scriptSrc}" defer></script>`;
  const idx = raw.toLowerCase().lastIndexOf("</body>");
  if (idx === -1) {
    throw new Error(`Cannot find </body> in ${filePath}`);
  }
  const before = raw.slice(0, idx);
  const after = raw.slice(idx);
  const needLeadingEol = before.length > 0 && !before.endsWith("\n") && !before.endsWith("\r");
  const updated = `${before}${needLeadingEol ? eol : ""}${tag}${eol}${after}`;
  await fs.writeFile(filePath, updated, "utf8");
  return true;
}

async function runSyncPages() {
  let indexChanged = false;
  if (await injectScriptTag(INDEX_PATH, "./study-notes.js")) {
    indexChanged = true;
  }
  const pageFiles = (await fs.readdir(PAGES_DIR)).filter((f) => f.endsWith(".html")).sort();
  let pagesUpdated = 0;
  for (const name of pageFiles) {
    const changed = await injectScriptTag(path.join(PAGES_DIR, name), "../study-notes.js");
    if (changed) pagesUpdated += 1;
  }
  const createdFeedback = await ensureFeedbackFiles();
  console.log(
    `[study.mjs --sync-pages] index.html: ${indexChanged ? "updated" : "already synced"}; pages/*.html: ${pagesUpdated}/${pageFiles.length} updated; feedback files: ${createdFeedback ? "initialized" : "preserved"}.`
  );
}

async function runRenderMd() {
  await enqueueWrite(async () => {
    const db = await readDbUnsafe();
    try {
      await fs.access(JSON_PATH);
    } catch {
      await atomicWrite(JSON_PATH, `${JSON.stringify(db, null, 2)}\n`);
    }
    await atomicWrite(MD_PATH, renderMarkdown(db));
    console.log(`[study.mjs --render-md] Rendered ${MD_PATH} (${db.items.length} items).`);
  });
}

function sendJson(res, status, payload) {
  const body = JSON.stringify(payload);
  res.writeHead(status, {
    "Content-Type": "application/json; charset=utf-8",
    "Cache-Control": "no-store",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type",
  });
  res.end(body);
}

function readJsonBody(req, maxBytes = 2 * 1024 * 1024) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    let total = 0;
    req.on("data", (chunk) => {
      total += chunk.length;
      if (total > maxBytes) {
        reject(Object.assign(new Error("Payload too large"), { statusCode: 413 }));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on("end", () => {
      try {
        const text = Buffer.concat(chunks).toString("utf8");
        resolve(text ? JSON.parse(text) : {});
      } catch {
        reject(Object.assign(new Error("Invalid JSON body"), { statusCode: 400 }));
      }
    });
    req.on("error", reject);
  });
}

async function handleApi(req, res) {
  if (req.method === "OPTIONS") {
    res.writeHead(204, {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
      "Access-Control-Allow-Headers": "Content-Type",
    });
    res.end();
    return;
  }

  if (req.method === "GET") {
    const db = await enqueueWrite(async () => {
      try {
        await fs.access(JSON_PATH);
        return await readDbUnsafe();
      } catch {
        const empty = emptyDb();
        await saveDbUnsafe(empty);
        return empty;
      }
    });
    sendJson(res, 200, { app: APP_ID, ...db });
    return;
  }

  if (req.method === "POST") {
    let payload;
    try {
      payload = await readJsonBody(req);
    } catch (err) {
      sendJson(res, err.statusCode || 400, { error: err.message || "Bad request" });
      return;
    }

    const action = payload && payload.action;
    if (!action) {
      sendJson(res, 400, { error: "Missing action" });
      return;
    }

    const result = await enqueueWrite(async () => {
      const db = await readDbUnsafe();
      const stamp = nowIso();

      if (action === "upsert") {
        const rawItem = payload.item;
        if (!rawItem || typeof rawItem !== "object") {
          return { status: 400, body: { error: "Missing item" } };
        }
        const existingIdx = rawItem.id ? db.items.findIndex((x) => x.id === rawItem.id) : -1;
        const existing = existingIdx >= 0 ? db.items[existingIdx] : null;
        const normalized = normalizeItem(rawItem, existing, stamp);
        if (!normalized.lessonId || !normalized.note) {
          return { status: 400, body: { error: "lessonId and note are required" } };
        }
        if (existingIdx >= 0) {
          db.items[existingIdx] = normalized;
        } else {
          db.items.unshift(normalized);
        }
        db.updatedAt = stamp;
        await saveDbUnsafe(db);
        return { status: 200, body: { app: APP_ID, saved: normalized, ...db } };
      }

      if (action === "status") {
        const id = cleanStr(payload.id, 80);
        const idx = db.items.findIndex((x) => x.id === id);
        if (idx < 0) {
          return { status: 404, body: { error: "Question not found" } };
        }
        const nextStatus = payload.status === "resolved" ? "resolved" : "open";
        const cur = db.items[idx];
        db.items[idx] = {
          ...cur,
          status: nextStatus,
          resolution: payload.resolution !== undefined ? cleanStr(payload.resolution, 4000) : cur.resolution,
          updatedAt: stamp,
        };
        db.updatedAt = stamp;
        await saveDbUnsafe(db);
        return { status: 200, body: { app: APP_ID, saved: db.items[idx], ...db } };
      }

      if (action === "delete") {
        const id = cleanStr(payload.id, 80);
        const before = db.items.length;
        db.items = db.items.filter((x) => x.id !== id);
        if (db.items.length === before) {
          return { status: 404, body: { error: "Question not found" } };
        }
        db.updatedAt = stamp;
        await saveDbUnsafe(db);
        return { status: 200, body: { app: APP_ID, deleted: id, ...db } };
      }

      if (action === "merge") {
        const incoming = Array.isArray(payload.items) ? payload.items : [];
        let merged = 0;
        const byId = new Map(db.items.map((it) => [it.id, it]));
        for (const raw of incoming) {
          if (!raw || typeof raw !== "object") continue;
          const id = cleanStr(raw.id, 80);
          if (!id) continue;
          const prev = byId.get(id);
          const candidate = normalizeItem(raw, prev || null, cleanStr(raw.updatedAt, 64) || stamp);
          if (!candidate.lessonId || !candidate.note) continue;
          if (!prev) {
            byId.set(id, candidate);
            merged += 1;
          } else if (String(candidate.updatedAt || "") > String(prev.updatedAt || "")) {
            byId.set(id, candidate);
            merged += 1;
          }
        }
        if (merged > 0 || db.items.length === 0) {
          db.items = Array.from(byId.values()).sort((a, b) =>
            String(b.createdAt || "").localeCompare(String(a.createdAt || ""))
          );
          db.updatedAt = stamp;
          await saveDbUnsafe(db);
        }
        return { status: 200, body: { app: APP_ID, merged, ...db } };
      }

      return { status: 400, body: { error: `Unknown action: ${action}` } };
    });

    sendJson(res, result.status, result.body);
    return;
  }

  sendJson(res, 405, { error: "Method not allowed" });
}

async function handleStatic(req, res, urlPath) {
  if (req.method !== "GET" && req.method !== "HEAD") {
    res.writeHead(405, { "Content-Type": "text/plain; charset=utf-8" });
    res.end("Method Not Allowed");
    return;
  }

  let decoded;
  try {
    decoded = decodeURIComponent(urlPath);
  } catch {
    res.writeHead(400, { "Content-Type": "text/plain; charset=utf-8" });
    res.end("Bad URL");
    return;
  }

  if (decoded === "/" || decoded === "") {
    decoded = "/index.html";
  }

  const rel = decoded.replace(/^\/+/, "");
  const segments = rel.split("/");
  if (segments.some((s) => s === ".." || s.startsWith("."))) {
    res.writeHead(403, { "Content-Type": "text/plain; charset=utf-8" });
    res.end("Forbidden");
    return;
  }

  const target = path.resolve(ROOT, rel);
  if (target !== ROOT && !target.startsWith(ROOT + path.sep)) {
    res.writeHead(403, { "Content-Type": "text/plain; charset=utf-8" });
    res.end("Forbidden");
    return;
  }

  let stat;
  try {
    stat = await fs.stat(target);
  } catch {
    if (decoded === "/favicon.ico") {
      res.writeHead(204, { "Cache-Control": "no-store" });
      res.end();
      return;
    }
    res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
    res.end("Not Found");
    return;
  }

  if (stat.isDirectory()) {
    res.writeHead(404, { "Content-Type": "text/plain; charset=utf-8" });
    res.end("Not Found");
    return;
  }

  const ext = path.extname(target).toLowerCase();
  const mime = MIME_TYPES[ext] || "application/octet-stream";

  if (ext === ".html") {
    let html = await fs.readFile(target, "utf8");
    if (!/study-notes\.js/i.test(html)) {
      const idx = html.toLowerCase().lastIndexOf("</body>");
      if (idx !== -1) {
        html = `${html.slice(0, idx)}<script src="/study-notes.js" defer></script>\n${html.slice(idx)}`;
      }
    }
    const buf = Buffer.from(html, "utf8");
    res.writeHead(200, {
      "Content-Type": mime,
      "Content-Length": buf.length,
      "Cache-Control": "no-store",
    });
    if (req.method === "HEAD") res.end();
    else res.end(buf);
    return;
  }

  const buf = await fs.readFile(target);
  res.writeHead(200, {
    "Content-Type": mime,
    "Content-Length": buf.length,
    "Cache-Control": "no-store",
  });
  if (req.method === "HEAD") res.end();
  else res.end(buf);
}

function probeExistingServer(port) {
  return new Promise((resolve) => {
    const req = http.get(`http://127.0.0.1:${port}/api/feedback`, { timeout: 800 }, (res) => {
      let body = "";
      res.setEncoding("utf8");
      res.on("data", (chunk) => {
        if (body.length < 16384) body += chunk;
      });
      res.on("end", () => {
        try {
          const parsed = JSON.parse(body);
          resolve(parsed && parsed.app === APP_ID);
        } catch {
          resolve(false);
        }
      });
    });
    req.on("timeout", () => {
      req.destroy();
      resolve(false);
    });
    req.on("error", () => resolve(false));
  });
}

function openBrowser(url) {
  return new Promise((resolve) => {
    let settled = false;
    let timer = null;
    const done = () => {
      if (settled) return;
      settled = true;
      if (timer) clearTimeout(timer);
      resolve();
    };
    try {
      let cmd;
      let args;
      if (process.platform === "win32") {
        cmd = "rundll32.exe";
        args = ["url.dll,FileProtocolHandler", url];
      } else if (process.platform === "darwin") {
        cmd = "open";
        args = [url];
      } else {
        cmd = "xdg-open";
        args = [url];
      }
      const child = spawn(cmd, args, {
        detached: true,
        stdio: "ignore",
        windowsHide: true,
      });
      child.once("close", done);
      child.once("error", done);
      timer = setTimeout(done, 1000);
      timer.unref();
      child.unref();
    } catch {
      // Non-fatal: user can open URL manually.
      done();
    }
  });
}

function listenOnPort(server, port) {
  return new Promise((resolve, reject) => {
    const onError = (err) => {
      server.off("listening", onListening);
      reject(err);
    };
    const onListening = () => {
      server.off("error", onError);
      resolve();
    };
    server.once("error", onError);
    server.once("listening", onListening);
    server.listen(port, "127.0.0.1");
  });
}

async function startServer({ explicitPort, noOpen }) {
  await ensureFeedbackFiles();

  const startPort = explicitPort ?? DEFAULT_PORT;
  const maxPort = explicitPort != null ? startPort : startPort + 20;

  for (let p = startPort; p <= maxPort; p += 1) {
    if (await probeExistingServer(p)) {
      const url = `http://127.0.0.1:${p}/`;
      console.log(`学习伴读服务已在运行：${url}`);
      console.log(`疑问记录将自动写入：${JSON_PATH}`);
      if (!noOpen) await openBrowser(url);
      return;
    }

    const server = http.createServer(async (req, res) => {
      try {
        const parsed = new URL(req.url || "/", `http://127.0.0.1:${p}`);
        if (parsed.pathname === "/api/feedback") {
          await handleApi(req, res);
        } else {
          await handleStatic(req, res, parsed.pathname);
        }
      } catch (err) {
        console.error("[study.mjs] Request error:", err);
        if (!res.headersSent) {
          res.writeHead(500, { "Content-Type": "text/plain; charset=utf-8" });
        }
        res.end("Internal Server Error");
      }
    });

    try {
      await listenOnPort(server, p);
      const url = `http://127.0.0.1:${p}/`;
      console.log(`Java Hood Lab 本地伴读服务已启动：${url}`);
      console.log(`页内记下的所有疑问会自动保存到：`);
      console.log(`  - ${JSON_PATH}`);
      console.log(`  - ${MD_PATH}`);
      console.log(`按 Ctrl+C 可停止服务。`);
      if (!noOpen) openBrowser(url);
      return;
    } catch (err) {
      if (err && err.code === "EADDRINUSE" && explicitPort == null) {
        continue;
      }
      throw err;
    }
  }

  throw new Error(`No available port found in range ${startPort}..${maxPort}`);
}

function parseArgs(argv) {
  const opts = { syncPages: false, renderMd: false, noOpen: false, explicitPort: null };
  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === "--sync-pages") opts.syncPages = true;
    else if (arg === "--render-md") opts.renderMd = true;
    else if (arg === "--no-open") opts.noOpen = true;
    else if (arg === "--port") {
      const val = Number(argv[i + 1]);
      if (!Number.isInteger(val) || val < 1 || val > 65535) {
        throw new Error(`Invalid --port value: ${argv[i + 1]}`);
      }
      opts.explicitPort = val;
      i += 1;
    } else if (arg === "-h" || arg === "--help") {
      console.log("Usage: node study.mjs [--sync-pages] [--render-md] [--no-open] [--port <N>]");
      process.exit(0);
    } else {
      throw new Error(`Unknown argument: ${arg}`);
    }
  }
  return opts;
}

async function main() {
  const opts = parseArgs(process.argv.slice(2));
  if (opts.syncPages) {
    await runSyncPages();
    return;
  }
  if (opts.renderMd) {
    await runRenderMd();
    return;
  }
  await startServer(opts);
}

function normalizeEntryPath(p) {
  const resolved = path.resolve(p);
  return process.platform === "win32" ? resolved.toLowerCase() : resolved;
}

const isEntry =
  Boolean(process.argv[1]) &&
  normalizeEntryPath(process.argv[1]) === normalizeEntryPath(fileURLToPath(import.meta.url));
if (isEntry) {
  main().catch((err) => {
    console.error("[study.mjs] Fatal:", err.message || err);
    process.exit(1);
  });
}
