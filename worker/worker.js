const MAX_SLOTS = 88;
const PART_SIZE = 90 * 1024 * 1024;

const HTML = `<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>视频槽位 88</title>
<style>
* { margin:0; padding:0; box-sizing:border-box; }
body { background:#0d1117; color:#e6edf3; font-family:-apple-system,"PingFang SC","Microsoft YaHei",sans-serif; min-height:100vh; }
header { padding:20px 24px 12px; display:flex; flex-wrap:wrap; align-items:baseline; gap:12px; }
header h1 { font-size:22px; font-weight:700; }
header .sub { color:#8b949e; font-size:13px; }
header .stats { margin-left:auto; color:#8b949e; font-size:13px; }
header .stats b { color:#58a6ff; }
main { padding:8px 24px 40px; }
.grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(210px,1fr)); gap:14px; }
.cell { position:relative; aspect-ratio:16/10; border-radius:10px; overflow:hidden; background:#161b22; border:1px solid #21262d; }
.cell.empty { display:flex; flex-direction:column; align-items:center; justify-content:center; gap:8px; border:1.5px dashed #30363d; cursor:pointer; transition:border-color .15s, background .15s; }
.cell.empty:hover { border-color:#58a6ff; background:#1a2130; }
.cell.empty .plus { font-size:34px; color:#3d444d; line-height:1; }
.cell.empty:hover .plus { color:#58a6ff; }
.cell.empty .num { color:#6e7681; font-size:13px; }
.cell .cover { width:100%; height:100%; object-fit:cover; display:block; cursor:pointer; }
.cell .nogif { width:100%; height:100%; display:flex; align-items:center; justify-content:center; cursor:pointer; background:#161b22; }
.cell .nogif .play { font-size:34px; color:#30363d; }
.badge { position:absolute; top:6px; left:6px; background:rgba(13,17,23,.82); color:#e6edf3; font-size:12px; padding:2px 8px; border-radius:20px; z-index:2; }
.more { position:absolute; top:6px; right:6px; z-index:2; background:rgba(13,17,23,.82); border:none; color:#e6edf3; width:28px; height:28px; border-radius:50%; font-size:16px; cursor:pointer; display:flex; align-items:center; justify-content:center; }
.more:hover { background:#21262d; }
.titlebar { position:absolute; left:0; right:0; bottom:0; padding:16px 10px 8px; background:linear-gradient(transparent, rgba(0,0,0,.85)); font-size:13px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.overlay { display:none; position:fixed; inset:0; background:rgba(0,0,0,.7); z-index:10; align-items:center; justify-content:center; }
.overlay.show { display:flex; }
.modal { background:#161b22; border:1px solid #30363d; border-radius:12px; padding:22px; width:min(440px, 92vw); }
.modal h3 { font-size:16px; margin-bottom:16px; }
.modal label { display:block; font-size:13px; color:#8b949e; margin:12px 0 6px; }
.modal input[type=text] { width:100%; background:#0d1117; border:1px solid #30363d; border-radius:8px; padding:9px 12px; color:#e6edf3; font-size:14px; outline:none; }
.modal input[type=text]:focus { border-color:#58a6ff; }
.btn { display:inline-flex; align-items:center; justify-content:center; gap:6px; border:none; border-radius:8px; padding:9px 16px; font-size:14px; cursor:pointer; background:#21262d; color:#e6edf3; }
.btn:hover { background:#30363d; }
.btn.primary { background:#1f6feb; color:#fff; }
.btn.primary:hover { background:#388bfd; }
.btn.block { width:100%; }
.pick { display:flex; align-items:center; gap:10px; }
.pick .fake { flex:1; background:#0d1117; border:1px dashed #30363d; border-radius:8px; padding:9px 12px; font-size:13px; color:#8b949e; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.fileinfo { font-size:12px; color:#8b949e; margin-top:6px; }
.coverprev { margin-top:10px; display:none; }
.coverprev img { max-width:100%; max-height:160px; border-radius:8px; border:1px solid #30363d; }
.progress { margin-top:12px; display:none; }
.progress .bar { height:8px; background:#21262d; border-radius:4px; overflow:hidden; }
.progress .fill { height:100%; width:0; background:#1f6feb; transition:width .2s; }
.progress .txt { font-size:12px; color:#8b949e; margin-top:5px; }
.modal .row { display:flex; gap:10px; margin-top:18px; justify-content:flex-end; }
.modal video { width:100%; max-height:70vh; border-radius:8px; background:#000; }
.menu { position:fixed; z-index:20; background:#161b22; border:1px solid #30363d; border-radius:10px; padding:6px; min-width:140px; display:none; box-shadow:0 8px 24px rgba(0,0,0,.5); }
.menu.show { display:block; }
.menu button { display:block; width:100%; text-align:left; background:none; border:none; color:#e6edf3; padding:9px 12px; border-radius:7px; font-size:14px; cursor:pointer; }
.menu button:hover { background:#21262d; }
.menu button.del { color:#f85149; }
#toast { position:fixed; bottom:24px; left:50%; transform:translateX(-50%); background:#30363d; color:#e6edf3; padding:10px 18px; border-radius:8px; font-size:14px; display:none; z-index:30; max-width:90vw; }
#toast.err { background:#b62324; }
.loading { text-align:center; color:#8b949e; padding:60px 0; }
</style>
</head>
<body>
<header>
  <h1>视频槽位 88</h1>
  <span class="sub">tv.988199.xyz</span>
  <span class="stats" id="stats"></span>
</header>
<main>
  <div class="grid" id="grid"><div class="loading">加载中...</div></div>
</main>

<div class="overlay" id="uploadModal">
  <div class="modal">
    <h3 id="umTitle">上传视频</h3>
    <div class="pick">
      <button class="btn" id="pickBtn">选择文件</button>
      <div class="fake" id="pickName">未选择视频文件</div>
      <input type="file" id="fileInput" accept="video/*" hidden>
    </div>
    <div class="fileinfo" id="fileInfo"></div>
    <label for="titleInput">标题（留空则使用文件名）</label>
    <input type="text" id="titleInput" maxlength="100" placeholder="视频标题">
    <div class="coverprev" id="coverPrev"><label>封面（随机抽取）</label><img id="coverImg" alt="封面预览"></div>
    <div class="progress" id="progWrap"><div class="bar"><div class="fill" id="progFill"></div></div><div class="txt" id="progTxt"></div></div>
    <div class="row">
      <button class="btn" id="umCancel">取消</button>
      <button class="btn primary" id="umStart" disabled>开始上传</button>
    </div>
  </div>
</div>

<div class="overlay" id="editModal">
  <div class="modal">
    <h3>编辑标题</h3>
    <input type="text" id="editInput" maxlength="100">
    <div class="row">
      <button class="btn" id="editCancel">取消</button>
      <button class="btn primary" id="editSave">保存</button>
    </div>
  </div>
</div>

<div class="overlay" id="playModal">
  <div class="modal" style="width:min(860px,94vw)">
    <video id="playVideo" controls autoplay></video>
  </div>
</div>

<div class="overlay" id="codeModal">
  <div class="modal">
    <h3>访问口令</h3>
    <input type="text" id="codeInput" placeholder="请输入访问口令">
    <div class="row">
      <button class="btn" id="codeCancel">取消</button>
      <button class="btn primary" id="codeSave">保存</button>
    </div>
  </div>
</div>

<div class="menu" id="slotMenu"></div>
<div id="toast"></div>

<script>
(function(){
"use strict";
var SLOTS = 88;
var PART = 90 * 1024 * 1024;
var videos = {};
var pendingSlot = 0;
var pickedFile = null, coverBlob = null;
var editSlot = 0;

function $(id){ return document.getElementById(id); }
function toast(msg, err){ var t = $("toast"); t.textContent = msg; t.className = err ? "err" : ""; t.style.display = "block"; clearTimeout(t._to); t._to = setTimeout(function(){ t.style.display = "none"; }, 3000); }
function fmtSize(n){ if (n >= 1073741824) return (n/1073741824).toFixed(2) + " GB"; if (n >= 1048576) return (n/1048576).toFixed(1) + " MB"; return Math.max(1, Math.round(n/1024)) + " KB"; }
function esc(s){ var d = document.createElement("div"); d.textContent = s == null ? "" : String(s); return d.innerHTML; }
function getCode(){ return localStorage.getItem("vs88code") || ""; }
function requireCode(then){
  if (getCode()) { then(); return; }
  $("codeModal").classList.add("show");
  $("codeInput").value = "";
  $("codeModal")._then = then;
}

function api(method, url, body, isJson){
  var h = { "X-Access-Code": getCode() };
  if (isJson) h["Content-Type"] = "application/json";
  return fetch(url, { method: method, headers: h, body: body || undefined })
    .then(function(r){ return r.text().then(function(t){
      var d = null; try { d = t ? JSON.parse(t) : null; } catch(e){}
      if (!r.ok) { var msg = d && d.error ? d.error : (r.status + " " + r.statusText); if (r.status === 401) localStorage.removeItem("vs88code"); throw new Error(msg); }
      return d;
    }); });
}

function loadVideos(){
  $("grid").innerHTML = '<div class="loading">加载中...</div>';
  api("GET", "/api/videos").then(function(list){
    videos = {};
    for (var i = 0; i < list.length; i++) videos[list[i].slot] = list[i];
    render();
  }).catch(function(e){ $("grid").innerHTML = '<div class="loading">加载失败：' + esc(e.message) + '</div>'; });
}

function render(){
  var g = $("grid");
  var html = "";
  var totalSize = 0, used = 0;
  for (var n = 1; n <= SLOTS; n++) {
    var v = videos[n];
    if (!v) {
      html += '<div class="cell empty" data-slot="' + n + '"><div class="plus">+</div><div class="num">槽位 ' + n + '</div></div>';
    } else {
      used++; totalSize += (v.size || 0);
      var cover = v.coverUrl ? '<img class="cover" loading="lazy" src="' + esc(v.coverUrl) + '" alt="">' : '<div class="nogif"><span class="play">&#9654;</span></div>';
      html += '<div class="cell" data-slot="' + n + '">' +
        '<span class="badge">' + n + '</span>' +
        '<button class="more" data-slot="' + n + '" title="管理">&#8943;</button>' +
        cover +
        '<div class="titlebar" title="' + esc(v.title) + '">' + esc(v.title) + '</div>' +
        '</div>';
    }
  }
  g.innerHTML = html;
  $("stats").innerHTML = "已用 <b>" + used + "</b>/" + SLOTS + " 个槽位 · 共 " + fmtSize(totalSize);
  bindGrid();
}

function bindGrid(){
  var cells = document.querySelectorAll(".cell.empty");
  for (var i = 0; i < cells.length; i++) {
    cells[i].addEventListener("click", function(){ openUpload(parseInt(this.getAttribute("data-slot"))); });
  }
  var covers = document.querySelectorAll(".cover, .nogif");
  for (var j = 0; j < covers.length; j++) {
    covers[j].addEventListener("click", function(){
      var slot = parseInt(this.parentNode.getAttribute("data-slot"));
      var v = videos[slot]; if (!v) return;
      $("playVideo").src = v.videoUrl;
      $("playModal").classList.add("show");
    });
  }
  var mores = document.querySelectorAll(".more");
  for (var k = 0; k < mores.length; k++) {
    mores[k].addEventListener("click", function(ev){
      ev.stopPropagation();
      openMenu(parseInt(this.getAttribute("data-slot")), this);
    });
  }
}

function openMenu(slot, anchor){
  var m = $("slotMenu");
  m.innerHTML = "";
  var acts = [["更换视频", function(){ openUpload(slot); }], ["编辑标题", function(){ openEdit(slot); }], ["删除", function(){ doDelete(slot); }]];
  for (var i = 0; i < acts.length; i++) {
    (function(a){
      var b = document.createElement("button");
      b.textContent = a[0];
      if (a[0] === "删除") b.className = "del";
      b.addEventListener("click", function(){ m.classList.remove("show"); a[1](); });
      m.appendChild(b);
    })(acts[i]);
  }
  var r = anchor.getBoundingClientRect();
  m.classList.add("show");
  var mw = m.offsetWidth, mh = m.offsetHeight;
  var left = Math.min(r.right - mw, window.innerWidth - mw - 8);
  var top = r.bottom + 6 + mh > window.innerHeight ? r.top - mh - 6 : r.bottom + 6;
  m.style.left = Math.max(8, left) + "px";
  m.style.top = top + "px";
}
document.addEventListener("click", function(ev){
  var m = $("slotMenu");
  if (m.classList.contains("show") && !m.contains(ev.target) && !ev.target.classList.contains("more")) m.classList.remove("show");
});

function openUpload(slot){
  requireCode(function(){
    pendingSlot = slot;
    pickedFile = null; coverBlob = null;
    $("umTitle").textContent = "槽位 " + slot + " · 上传视频";
    $("pickName").textContent = "未选择视频文件";
    $("fileInfo").textContent = "";
    $("titleInput").value = "";
    $("coverPrev").style.display = "none";
    $("coverImg").removeAttribute("src");
    $("progWrap").style.display = "none";
    $("progFill").style.width = "0";
    $("progFill").style.background = "#1f6feb";
    $("progTxt").textContent = "";
    $("umStart").disabled = true;
    $("fileInput").value = "";
    $("uploadModal").classList.add("show");
  });
}

$("pickBtn").addEventListener("click", function(){ $("fileInput").click(); });
$("fileInput").addEventListener("change", function(){
  var f = this.files && this.files[0];
  if (!f) return;
  pickedFile = f; coverBlob = null;
  $("pickName").textContent = f.name;
  $("fileInfo").textContent = fmtSize(f.size) + (f.type ? " · " + f.type : "");
  var base = f.name.replace(/\\.[^.]+$/, "");
  if (!$("titleInput").value) $("titleInput").value = base;
  $("coverPrev").style.display = "none";
  $("umStart").disabled = true;
  $("progWrap").style.display = "block";
  $("progFill").style.width = "100%";
  $("progFill").style.background = "#8b949e";
  $("progTxt").textContent = "正在抽取封面帧...";
  extractCover(f).then(function(blob){
    $("progFill").style.background = "#1f6feb";
    $("progFill").style.width = "0";
    $("progTxt").textContent = "";
    if (blob) {
      coverBlob = blob;
      $("coverImg").src = URL.createObjectURL(blob);
      $("coverPrev").style.display = "block";
    } else {
      toast("未能抽取封面，将使用默认占位图", false);
    }
    $("umStart").disabled = false;
  });
});

function extractCover(file){
  return new Promise(function(resolve){
    var url = URL.createObjectURL(file);
    var v = document.createElement("video");
    v.muted = true; v.playsInline = true; v.preload = "auto";
    v.src = url;
    var done = false;
    var to = setTimeout(function(){ finish(null); }, 9000);
    function finish(x){
      if (done) return; done = true;
      clearTimeout(to);
      try { URL.revokeObjectURL(url); } catch(e){}
      try { v.pause(); v.removeAttribute("src"); v.load(); } catch(e){}
      resolve(x);
    }
    v.onloadedmetadata = function(){
      var d = v.duration;
      var t;
      if (!isFinite(d) || d <= 0) t = 0.5;
      else t = Math.min(d * (0.15 + Math.random() * 0.7), Math.max(0.1, d - 0.1));
      try { v.currentTime = t; } catch(e) { finish(null); }
    };
    v.onseeked = function(){
      try {
        var vw = v.videoWidth, vh = v.videoHeight;
        if (!vw || !vh) { finish(null); return; }
        var cw = Math.min(640, vw);
        var ch = Math.round(cw * vh / vw);
        var c = document.createElement("canvas");
        c.width = cw; c.height = ch;
        c.getContext("2d").drawImage(v, 0, 0, cw, ch);
        c.toBlob(function(b){ finish(b || null); }, "image/jpeg", 0.82);
      } catch(e) { finish(null); }
    };
    v.onerror = function(){ finish(null); };
  });
}

function extOf(name){
  var m = /\\.([A-Za-z0-9]{1,5})$/.exec(name || "");
  return m ? m[1].toLowerCase() : "mp4";
}

$("umCancel").addEventListener("click", function(){ $("uploadModal").classList.remove("show"); });

$("umStart").addEventListener("click", function(){
  if (!pickedFile) return;
  var file = pickedFile, slot = pendingSlot;
  $("umStart").disabled = true;

  function setProg(p, txt){ $("progFill").style.width = p + "%"; if (txt !== undefined) $("progTxt").textContent = txt; }
  function fail(e){ toast("上传失败：" + e.message, true); setProg(0, ""); $("umStart").disabled = false; }

  var up = file.size <= PART ? uploadDirect(slot, file, setProg) : uploadMultipart(slot, file, setProg);
  up.then(function(d){
    return ensureCover(slot).then(function(coverKey){
      setProg(100, "保存信息...");
      return api("POST", "/api/slots/" + slot, JSON.stringify({
        title: $("titleInput").value.trim(), key: d.key, coverKey: coverKey, originalName: file.name
      }), true);
    });
  }).then(function(){
    $("uploadModal").classList.remove("show");
    toast("槽位 " + slot + " 上传成功");
    loadVideos();
  }).catch(fail);
});

function uploadDirect(slot, file, setProg){
  return new Promise(function(resolve, reject){
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/api/slots/" + slot + "/upload?filename=" + encodeURIComponent(file.name || "video.mp4"));
    xhr.setRequestHeader("X-Access-Code", getCode());
    xhr.setRequestHeader("Content-Type", file.type || "video/mp4");
    xhr.upload.onprogress = function(ev){ if (ev.lengthComputable) setProg(Math.round(ev.loaded * 95 / ev.total), "上传视频 " + Math.round(ev.loaded * 95 / ev.total) + "%"); };
    xhr.onload = function(){
      try { var d = JSON.parse(xhr.responseText); if (xhr.status >= 200 && xhr.status < 300 && d.key) resolve(d); else reject(new Error(d.error || ("HTTP " + xhr.status))); }
      catch(e){ reject(new Error("响应解析失败")); }
    };
    xhr.onerror = function(){ reject(new Error("网络错误")); };
    xhr.send(file);
  });
}

function uploadMultipart(slot, file, setProg){
  var total = Math.max(1, Math.ceil(file.size / PART));
  return api("POST", "/api/slots/" + slot + "/multipart/init", JSON.stringify({ filename: file.name }), true).then(function(d){
    var key = d.key, uploadId = d.uploadId;
    var parts = [], uploaded = 0, aborted = false;
    function cleanup(){ if (!aborted) { aborted = true; api("POST", "/api/slots/" + slot + "/multipart/abort", JSON.stringify({ key: key, uploadId: uploadId }), true).catch(function(){}); } }
    var p = Promise.resolve();
    var loop = function(n){
      p = p.then(function(){
        if (aborted) throw new Error("已取消");
        var start = (n - 1) * PART;
        var blob = file.slice(start, Math.min(start + PART, file.size));
        return putPart(slot, key, uploadId, n, blob, function(doneBytes){
          setProg(Math.round((uploaded + doneBytes) * 95 / file.size), "上传分片 " + n + "/" + total + " · " + Math.round((uploaded + doneBytes) * 95 / file.size) + "%");
        }).then(function(etag){
          parts.push({ partNumber: n, etag: etag });
          uploaded += blob.size;
        });
      }).catch(function(e){ cleanup(); throw e; });
      return p;
    };
    for (var i = 1; i <= total; i++) loop(i);
    return p.then(function(){
      parts.sort(function(a, b){ return a.partNumber - b.partNumber; });
      return api("POST", "/api/slots/" + slot + "/multipart/complete", JSON.stringify({ key: key, uploadId: uploadId, parts: parts }), true);
    }).then(function(){ return { key: key }; });
  });
}

function putPart(slot, key, uploadId, n, blob, onprog){
  return new Promise(function(resolve, reject){
    var xhr = new XMLHttpRequest();
    var tries = 0;
    function go(){
      tries++;
      xhr.open("PUT", "/api/slots/" + slot + "/multipart/part?key=" + encodeURIComponent(key) + "&uploadId=" + encodeURIComponent(uploadId) + "&part=" + n);
      xhr.setRequestHeader("X-Access-Code", getCode());
      xhr.upload.onprogress = function(ev){ if (ev.lengthComputable) onprog(ev.loaded); };
      xhr.onload = function(){
        if (xhr.status >= 200 && xhr.status < 300) {
          try { resolve(JSON.parse(xhr.responseText).etag); } catch(e){ reject(new Error("分片响应解析失败")); }
        } else if (tries < 2) { go(); }
        else { try { reject(new Error(JSON.parse(xhr.responseText).error || ("HTTP " + xhr.status))); } catch(e){ reject(new Error("HTTP " + xhr.status)); } }
      };
      xhr.onerror = function(){ if (tries < 2) go(); else reject(new Error("网络错误")); };
      xhr.send(blob);
    }
    go();
  });
}

function ensureCover(slot){
  if (!coverBlob) return Promise.resolve(null);
  return api("POST", "/api/slots/" + slot + "/cover", coverBlob, false).then(function(d){ return d.coverKey; });
}

function openEdit(slot){
  var v = videos[slot]; if (!v) return;
  editSlot = slot;
  $("editInput").value = v.title;
  $("editModal").classList.add("show");
}
$("editCancel").addEventListener("click", function(){ $("editModal").classList.remove("show"); });
$("editSave").addEventListener("click", function(){
  var t = $("editInput").value.trim();
  if (!t) { toast("标题不能为空", true); return; }
  api("PATCH", "/api/slots/" + editSlot, JSON.stringify({ title: t }), true).then(function(){
    $("editModal").classList.remove("show");
    toast("已保存");
    loadVideos();
  }).catch(function(e){ toast(e.message, true); });
});

function doDelete(slot){
  requireCode(function(){
    if (!confirm("确定删除槽位 " + slot + " 的视频？此操作不可恢复。")) return;
    api("DELETE", "/api/slots/" + slot).then(function(){
      toast("已删除槽位 " + slot);
      loadVideos();
    }).catch(function(e){ toast(e.message, true); });
  });
}

$("playModal").addEventListener("click", function(ev){
  if (ev.target === this) { $("playVideo").pause(); $("playVideo").removeAttribute("src"); $("playVideo").load(); $("playModal").classList.remove("show"); }
});
$("codeCancel").addEventListener("click", function(){ $("codeModal").classList.remove("show"); });
$("codeSave").addEventListener("click", function(){
  var c = $("codeInput").value.trim();
  if (!c) { toast("口令不能为空", true); return; }
  localStorage.setItem("vs88code", c);
  $("codeModal").classList.remove("show");
  var then = $("codeModal")._then;
  if (then) then();
});

loadVideos();
})();
</script>
</body>
</html>`;

function json(data, status) {
  return new Response(JSON.stringify(data), {
    status: status || 200,
    headers: {
      "Content-Type": "application/json; charset=utf-8",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Headers": "Content-Type, X-Access-Code",
      "Access-Control-Allow-Methods": "GET, POST, PATCH, DELETE, PUT, OPTIONS"
    }
  });
}

function err(msg, status) {
  return json({ error: msg }, status || 400);
}

function authOk(request, env) {
  return request.headers.get("X-Access-Code") === env.ACCESS_CODE;
}

function slotNum(raw) {
  const n = parseInt(raw, 10);
  if (!Number.isInteger(n) || n < 1 || n > MAX_SLOTS) return null;
  return n;
}

function safeExt(name) {
  const m = /\.([A-Za-z0-9]{1,5})$/.exec(name || "");
  const e = m ? m[1].toLowerCase() : "";
  return /^[a-z0-9]{1,5}$/.test(e) ? e : "mp4";
}

function newVideoKey(slot, name) {
  return "videos/slot-" + slot + "-" + Date.now() + "." + safeExt(name);
}

function newCoverKey(slot) {
  return "covers/slot-" + slot + "-" + Date.now() + ".jpg";
}

function keyBelongsToSlot(key, slot) {
  return key === "videos/slot-" + slot + "-" || key.startsWith("videos/slot-" + slot + "-");
}

function cleanTitle(t, fallback) {
  const s = String(t || "").trim().slice(0, 100);
  return s || fallback;
}

function metaOut(m) {
  return {
    slot: m.slot,
    title: m.title,
    size: m.size || 0,
    type: m.type || "",
    uploadedAt: m.uploadedAt || "",
    videoUrl: "/v/" + m.key,
    coverUrl: m.coverKey ? "/c/" + m.coverKey : null
  };
}

async function listVideos(env) {
  const list = await env.META.list({ prefix: "slot:" });
  const items = await Promise.all(list.keys.map(k => env.META.get(k.name)));
  const out = [];
  for (const raw of items) {
    if (!raw) continue;
    try { out.push(metaOut(JSON.parse(raw))); } catch (e) {}
  }
  out.sort((a, b) => a.slot - b.slot);
  return out;
}

function parseRange(header, size) {
  if (!header) return null;
  const m = /^bytes=(\d*)-(\d*)$/.exec(header.trim());
  if (!m) return null;
  let start, end;
  if (m[1] === "" && m[2] !== "") {
    const len = Math.min(parseInt(m[2], 10), size);
    start = size - len;
    end = size - 1;
  } else {
    start = parseInt(m[1], 10);
    end = m[2] === "" ? size - 1 : Math.min(parseInt(m[2], 10), size - 1);
  }
  if (isNaN(start) || start < 0 || start > end || start >= size) return "invalid";
  return { offset: start, end };
}

async function serveMedia(request, env, prefix) {
  const url = new URL(request.url);
  const key = decodeURIComponent(url.pathname.replace(/^\/(v|c)\//, ""));
  if (!key.startsWith(prefix) || key.includes("..")) return err("not found", 404);

  if (prefix === "videos/") {
    const head = await env.BUCKET.head(key);
    if (!head) return err("not found", 404);
    const size = head.size;
    const baseHeaders = {
      "Content-Type": head.httpMetadata && head.httpMetadata.contentType || "application/octet-stream",
      "Accept-Ranges": "bytes",
      "Access-Control-Allow-Origin": "*"
    };

    if (request.method === "HEAD") {
      return new Response(null, { status: 200, headers: Object.assign({ "Content-Length": String(size) }, baseHeaders) });
    }

    const rangeHeader = request.headers.get("Range");
    if (rangeHeader) {
      const r = parseRange(rangeHeader, size);
      if (r === "invalid") return new Response(null, { status: 416, headers: { "Content-Range": "bytes */" + size } });
      if (r) {
        const length = r.end - r.offset + 1;
        const obj = await env.BUCKET.get(key, { range: { offset: r.offset, length } });
        if (!obj) return err("not found", 404);
        const body = await obj.arrayBuffer();
        return new Response(body, {
          status: 206,
          headers: Object.assign(baseHeaders, {
            "Content-Length": String(body.byteLength),
            "Content-Range": "bytes " + r.offset + "-" + r.end + "/" + size,
            "Cache-Control": "no-cache"
          })
        });
      }
    }

    const obj = await env.BUCKET.get(key);
    if (!obj) return err("not found", 404);
    return new Response(obj.body, {
      status: 200,
      headers: Object.assign(baseHeaders, { "Content-Length": String(obj.size), "Cache-Control": "no-cache" })
    });
  }

  const obj = await env.BUCKET.get(key);
  if (!obj) return err("not found", 404);
  return new Response(obj.body, {
    status: 200,
    headers: {
      "Content-Type": obj.httpMetadata && obj.httpMetadata.contentType || "image/jpeg",
      "Cache-Control": "public, max-age=86400",
      "Access-Control-Allow-Origin": "*"
    }
  });
}

async function readJson(request) {
  try { return await request.json(); } catch (e) { return {}; }
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    const path = url.pathname;
    const method = request.method;

    if (method === "OPTIONS") {
      return new Response(null, {
        status: 204,
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, POST, PATCH, DELETE, PUT, OPTIONS",
          "Access-Control-Allow-Headers": "Content-Type, X-Access-Code",
          "Access-Control-Max-Age": "86400"
        }
      });
    }

    if (method === "GET" && (path === "/" || path === "/index.html")) {
      return new Response(HTML, { headers: { "Content-Type": "text/html; charset=utf-8", "Cache-Control": "no-cache" } });
    }

    if (method === "GET" && path === "/api/videos") {
      return json(await listVideos(env));
    }

    if (path.startsWith("/v/")) return serveMedia(request, env, "videos/");
    if (path.startsWith("/c/")) return serveMedia(request, env, "covers/");

    if (path.startsWith("/api/slots/")) {
      const parts = path.split("/").filter(Boolean);
      const slot = slotNum(parts[2]);
      if (!slot) return err("槽位号必须是 1-" + MAX_SLOTS + " 的整数", 400);
      const metaKey = "slot:" + slot;
      const action = parts[3] || "";

      if (method === "POST" && action === "upload") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const filename = url.searchParams.get("filename") || "video.mp4";
        const key = newVideoKey(slot, filename);
        try {
          await env.BUCKET.put(key, request.body, {
            httpMetadata: { contentType: request.headers.get("Content-Type") || "video/mp4" }
          });
        } catch (e) {
          return err("上传失败: " + (e && e.message ? e.message : String(e)), 500);
        }
        return json({ key });
      }

      if (method === "POST" && action === "multipart" && parts[4] === "init") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const body = await readJson(request);
        const key = newVideoKey(slot, body.filename);
        const mp = await env.BUCKET.createMultipartUpload(key, {
          httpMetadata: { contentType: "video/" + safeExt(body.filename) }
        });
        return json({ key, uploadId: mp.uploadId });
      }

      if (method === "PUT" && action === "multipart" && parts[4] === "part") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const key = url.searchParams.get("key") || "";
        const uploadId = url.searchParams.get("uploadId") || "";
        const partNumber = parseInt(url.searchParams.get("part"), 10);
        if (!keyBelongsToSlot(key, slot) || !uploadId || !Number.isInteger(partNumber) || partNumber < 1 || partNumber > 10000) {
          return err("分片参数无效", 400);
        }
        const mp = env.BUCKET.resumeMultipartUpload(key, uploadId);
        const part = await mp.uploadPart(partNumber, request.body);
        return json({ partNumber: part.partNumber, etag: part.etag });
      }

      if (method === "POST" && action === "multipart" && parts[4] === "complete") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const body = await readJson(request);
        const key = String(body.key || "");
        const uploadId = String(body.uploadId || "");
        const list = Array.isArray(body.parts) ? body.parts : [];
        if (!keyBelongsToSlot(key, slot) || !uploadId || list.length === 0) return err("完成参数无效", 400);
        const clean = list.map(p => ({ partNumber: parseInt(p.partNumber, 10), etag: String(p.etag || "") }))
          .filter(p => Number.isInteger(p.partNumber) && p.partNumber >= 1 && p.etag)
          .sort((a, b) => a.partNumber - b.partNumber);
        try {
          const mp = env.BUCKET.resumeMultipartUpload(key, uploadId);
          const obj = await mp.complete(clean);
          return json({ key, size: obj.size });
        } catch (e) {
          return err("合并分片失败: " + (e && e.message ? e.message : String(e)), 500);
        }
      }

      if (method === "POST" && action === "multipart" && parts[4] === "abort") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const body = await readJson(request);
        const key = String(body.key || "");
        const uploadId = String(body.uploadId || "");
        if (!keyBelongsToSlot(key, slot) || !uploadId) return err("参数无效", 400);
        const mp = env.BUCKET.resumeMultipartUpload(key, uploadId);
        try {
          await mp.abort();
          return json({ ok: true });
        } catch (e) {
          return err("终止分片失败: " + (e && e.message ? e.message : String(e)), 500);
        }
      }

      if (method === "POST" && action === "cover") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const key = newCoverKey(slot);
        await env.BUCKET.put(key, request.body, { httpMetadata: { contentType: "image/jpeg" } });
        return json({ coverKey: key });
      }

      if (method === "POST" && action === "") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const body = await readJson(request);
        if (!body.key || !String(body.key).startsWith("videos/")) return err("缺少有效的视频 key");
        const head = await env.BUCKET.head(body.key);
        if (!head) return err("视频对象不存在，请先上传", 400);
        const oldRaw = await env.META.get(metaKey);
        let old = null;
        try { old = oldRaw ? JSON.parse(oldRaw) : null; } catch (e) {}
        const meta = {
          slot,
          title: cleanTitle(body.title, body.originalName || ("槽位 " + slot)),
          key: body.key,
          coverKey: body.coverKey || null,
          size: head.size,
          type: head.httpMetadata && head.httpMetadata.contentType || "",
          uploadedAt: new Date().toISOString()
        };
        await env.META.put(metaKey, JSON.stringify(meta));
        if (old) {
          const stale = [];
          if (old.key && old.key !== meta.key) stale.push(old.key);
          if (old.coverKey && old.coverKey !== meta.coverKey) stale.push(old.coverKey);
          if (stale.length) await env.BUCKET.delete(stale);
        }
        return json(metaOut(meta));
      }

      if (method === "PATCH" && action === "") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const raw = await env.META.get(metaKey);
        if (!raw) return err("槽位为空", 404);
        let meta;
        try { meta = JSON.parse(raw); } catch (e) { return err("数据损坏", 500); }
        const body = await readJson(request);
        meta.title = cleanTitle(body.title, meta.title);
        await env.META.put(metaKey, JSON.stringify(meta));
        return json(metaOut(meta));
      }

      if (method === "DELETE" && action === "") {
        if (!authOk(request, env)) return err("访问口令错误", 401);
        const raw = await env.META.get(metaKey);
        if (!raw) return err("槽位为空", 404);
        let meta = null;
        try { meta = JSON.parse(raw); } catch (e) {}
        const del = [];
        if (meta && meta.key) del.push(meta.key);
        if (meta && meta.coverKey) del.push(meta.coverKey);
        if (del.length) await env.BUCKET.delete(del);
        await env.META.delete(metaKey);
        return json({ ok: true });
      }

      return err("not found", 404);
    }

    return err("not found", 404);
  }
};
