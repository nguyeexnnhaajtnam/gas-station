# Handoff: Gas Station — UI redesign (App shell, Company, Station, Dashboard)

## Overview
Redesign of the Angular gas station management frontend: app shell (sidebar + topbar + Company/Station context switcher), Company selection, Station selection (async open), Dashboard foundation, and reusable operational patterns (pump card, tank level, transaction table, status badge, loading / empty / error states).

## About the design files
`prototype.html` is a **design reference built in HTML** — open it in a browser to see intended look and behavior. It is NOT production code. Recreate it in the existing Angular app using its current architecture, services, routing and API models. Do not copy the HTML/JS.

Sample company/station records in the prototype are only there to show layout (long Vietnamese names, missing phone/email). In Angular, all data comes from the existing APIs. The "Mẫu thành phần" page contains illustrative numbers — never ship those.

The prototype's Tweaks (bottom-right toggle in the design tool) simulate: data state (Có dữ liệu / Đang tải / Trống / Lỗi), station-open failure, and starting with context selected.

## Fidelity
**High-fidelity.** Colors, type, spacing, borders and states are final. Match them closely.

## Hard rules (from product brief)
- Do not change backend, REST contracts, SeenPro adapter, auth, guards, CompanyContext, StationContext, routing logic or existing API calls.
- No fake business numbers on Dashboard / pumps / tanks / transactions. Unimplemented APIs → empty/placeholder states.
- Never display or store: passwords, PHPSESSID, SeenPro cookies/credentials, `gl`, `al`, `opt`, `view.php`, `menu.php`, `quanlycuahang.php`.
- Don't invent station ONLINE/OFFLINE. If backend has no reliable status → neutral "Chưa xác định".
- No new state libraries. One icon set (Lucide-style line icons; use `lucide-angular` or inline SVG if nothing exists).

---

## Design tokens
Put these in global CSS variables (e.g. `styles.scss :root`).

### Color
| Token | Value | Use |
|---|---|---|
| bg | `#f3f2f2` | App background |
| surface | `#f8f4f4` (neutral-100) | Tables, cards, inputs, menus |
| surface-2 | `#eae7e7` (neutral-200) | Row hover, avatar bg, skeleton |
| line-soft | `#d7d3d3` (neutral-300) | Row separators, card borders |
| line | `rgba(32,30,29,.40)` | Inputs, section rules (2px), table header rule |
| text | `#201e1d` | Primary text; 2px top rule of tables |
| text-2 | `#605d5d` (neutral-700) | Subtitles, labels, table headers |
| text-3 | `#7d7979` (neutral-600) | Metadata |
| text-muted | `#9b9797` (neutral-500) | "Chưa có", placeholders |
| accent | `#ec3013` | Primary button, active nav marker |
| accent-hover | `#dd2b0f` / active `#ae1800` | Primary button states |
| accent-text | `#ae1800` | Links, accent text on light bg |
| sidebar-bg | `#2d2b2b` | Sidebar |
| sidebar-line | `#444141` | Sidebar dividers |
| sidebar-text | `#d7d3d3`; muted `#bab6b6`; group label `#9b9797` | |
| success | `#2e7d4f` | Online / normal / hoàn tất |
| warning | `#a15c00` | Cần chú ý / tồn thấp |
| danger | `#ae1800` | Offline / lỗi |
| unknown | `#9b9797` | Chưa xác định / ngừng hoạt động |
| error-banner | bg `#fff2ef`, border `#ffc4b8`, text `#7c1405` | Inline error |

### Type
Font: **Be Vietnam Pro** 400/500/600/700 (Google Fonts). Body 14px / 1.5.
| Role | Size / weight |
|---|---|
| Page title (h1) | 24px / 700, letter-spacing -0.01em |
| Section title (h2) | 16px / 700 |
| Row primary (company name) | 15px / 600 |
| Station name | 14.5px / 700, as returned (uppercase) |
| Body / cells | 14px (transactions 13.5px) |
| Meta / sub-labels | 12.5px / 400 |
| Table header | 12.5px / 600, sentence case |
| Group label (sidebar) | 11px / 600, uppercase, letter-spacing .08em |
| KPI value | 28px / 700 |
Numbers: `font-variant-numeric: tabular-nums`. Format with `vi-VN`: `320.000 ₫`, `12,48 L`, `25.650 ₫/L`, date `30/09/2026 08:14`.

### Spacing / shape
- Spacing scale: 4, 8, 12, 16, 20, 24, 32. Page padding 24px top, 32px sides. Vertical gap between page blocks 24px.
- **Radius 0 everywhere** (Modernist). Only exception: spinner circle.
- Borders: 1px for controls/cards; **2px** rules for section headers and top of tables.
- Shadow only on floating menus/toasts: `0 12px 32px rgba(45,43,43,.22)`.
- Focus: `outline: 2px solid #ec3013; outline-offset: 2px` on `:focus-visible`.
- Disabled: opacity .45 (nav items .38).

### Layout dimensions
- Sidebar 248px; collapsed 64px (auto-collapse < 1180px, manual toggle). Sticky full height.
- Topbar 56px, sticky, bottom border 2px `line`.
- Control height: 38px (page toolbar), 36px (filters, nav items), 34px (row buttons), 32px (icon buttons).

---

## App shell

### Sidebar
- Header 56px: 32×32 accent square with fuel icon (white) + "Gas Station" (15/700 white) / "Quản lý cửa hàng" (12px muted). Bottom 1px sidebar-line.
- If no station: hint box (margin 16px 12px 0, 1px sidebar-line border, 12.5px) with lock icon: "Chọn công ty và trạm để mở các chức năng vận hành."
- Groups (gap 16px), label 11px uppercase:
  - TỔNG QUAN: Tổng quan
  - VẬN HÀNH: Theo dõi trụ bơm · Giao dịch · Ca bán hàng
  - NHIÊN LIỆU: Bồn bể · Kho nhiên liệu · Giá nhiên liệu
  - KINH DOANH: Khách hàng · Cấp nhiên liệu · Công nợ · Hóa đơn
  - BÁO CÁO: Báo cáo
  - HỆ THỐNG: Công ty / Đại lý · Người dùng · Cài đặt (always enabled)
- All non-HỆ THỐNG items are **disabled until StationContext exists** (opacity .38, cursor not-allowed, tooltip "… · cần chọn trạm").
- Item: 36px, padding 0 12px, icon 18px + label 14px. Hover bg `rgba(255,255,255,.06)`. Active: bg `rgba(255,255,255,.08)`, text white 600, icon `#ff9783`, 2px accent bar on left edge (inset 8px top/bottom). Station page counts as active "Công ty / Đại lý".
- Footer: collapse toggle ("Thu gọn"). Collapsed mode shows icons only with `title` tooltips.

### Topbar
- Left: breadcrumb 13.5px, chevron separators; earlier crumbs text-3 and clickable, last crumb text 600.
  - Company page: `Công ty / Đại lý`
  - Station page: `Công ty / Đại lý › {company} › Trạm xăng`
  - Station screens: `{company} › {station short name} › {page}`
- Right: **context switcher** — one bordered group (38px, surface bg, 1px `line`) with two segments divided by 1px line:
  - `[building icon] Công ty (11px text-3) / {name} (13px 600) ▾`
  - `[fuel icon] Trạm / {short name} ▾`
  - Empty values show "Chưa chọn" in text-muted. Station segment disabled until company chosen.
  - Max width per segment 260px (170px under 1180px), ellipsis.
  - Dropdowns (320–340px, surface, 1px line, shadow): company list with initials + check on current + "Xem tất cả đơn vị"; station list (short name + "Mã trạm: …"), selecting runs the same open-station flow.
  - Station short name = name without "CỬA HÀNG XĂNG DẦU " prefix, title-cased (display only).
- Notification icon button (placeholder), avatar 34×34 square `#444141` white initials; user menu: name, email, "Tài khoản của tôi", "Đăng xuất" (use existing logout).
- Click outside closes menus.

---

## Company page (`Công ty / Đại lý`)
- Header: h1 "Công ty / Đại lý", subtitle "Chọn đơn vị để tiếp tục quản lý hệ thống".
- Toolbar (gap 12, wraps): search (max 420px, 38px, left search icon, placeholder "Tìm theo tên, mã đơn vị, điện thoại…"), count "3 đơn vị" (13px text-3; while searching "1 / 3 đơn vị"), spacer, primary "+ Thêm đơn vị" (nowrap; label left-aligned). Search is client-side, accent/diacritic-insensitive.
- Table: wrapper `overflow-x:auto; overflow-y:hidden`, top 2px `text` rule, surface bg; table min-width 1040px.
  - Columns: Tên đơn vị (min 300px) · Mã đơn vị · Điện thoại · Email · Trạng thái · actions (96px).
  - Name cell: 36×36 initials square (surface-2, 12px 700) + name 15/600 + "Chọn để tiếp tục quản lý" (12.5 text-3, nowrap).
  - Null phone/email → "Chưa có" in text-muted.
  - Status: 8×8 square dot + label ("Đang hoạt động" success / "Ngừng hoạt động" unknown) — only if API provides it.
  - Actions: 32px "more" icon button (secondary, stopPropagation) + chevron.
  - Row padding 12px 16px, 1px line-soft separator, hover surface-2, whole row clickable → select company (existing CompanyContext) → Station page.
- Never show password / legacy fields.

## Station page (`Trạm xăng`)
- Header: h1 "Trạm xăng", subtitle "Chọn trạm để bắt đầu quản lý vận hành · **{company}**".
- Toolbar: search "Tìm kiếm trạm…", count "3 trạm", secondary "+ Thêm trạm" disabled with small "Sắp có" tag (only if no real action exists; otherwise wire it).
- Table (same wrapper rules, min-width 1040):
  - Trạm (min 320px): 40×40 bordered square with initials of short name + NAME (14.5/700) + "Mã trạm: {code}".
  - Liên hệ: phone (or "Chưa có số điện thoại" muted) / address 12.5px (or "Chưa có địa chỉ").
  - Email (nowrap, "Chưa có" muted).
  - Trạng thái: neutral outlined 8px square + "Chưa xác định" unless backend gives reliable status.
  - Action: primary "Chọn trạm →" (34px, min-width 132px, left-aligned label).
- **Open-station interaction**:
  1. Click row or button → call existing POST station-selection endpoint.
  2. That row: bg surface-2, button becomes outlined with spinner + "Đang mở trạm…". Other rows opacity .5; all buttons disabled (no duplicate click). Topbar station segment shows spinner.
  3. Navigate to Dashboard **only after success**, then update StationContext; toast "Đã mở trạm {short}".
  4. On failure: inline banner above toolbar (error-banner colors, alert icon, dismiss ×) "Không thể mở trạm. Vui lòng thử lại." + toast. Never mention SeenPro/PHP/session.

## Dashboard (`Tổng quan vận hành`)
- Header row: h1 "Tổng quan vận hành"; line below: **STATION NAME** · address · "Mã trạm: …". Right: "Hôm nay, {dd/MM/yyyy}" + 34px refresh icon button.
- KPI strip: grid `repeat(auto-fit, minmax(200px,1fr))`, cells separated by 1px line-soft (gap trick), top 2px text rule. Cell padding 14px 20px 16px, min-height 104px: label 13/500 text-2 + unit right (12px muted: ₫ / lít / lượt / trụ), value 28/700. No data → "—" (muted) + "Chưa có dữ liệu". KPIs: Doanh thu hôm nay · Sản lượng · Giao dịch · Trụ hoạt động.
- Sections, each with header row (h2 16/700, bottom 2px `line`, 8px padding) + optional right link (13.5/500 accent-text with arrow):
  1. Tình trạng trụ bơm → link "Theo dõi trụ bơm". Body: pump cards grid or empty state.
  2. Doanh thu / Sản lượng → segmented Hôm nay / 7 ngày / 30 ngày (selected = text bg, bg-colored text). Chart area 240px with faint horizontal lines every 48px; empty text left-aligned.
  3. Two columns `repeat(auto-fit, minmax(360px,1fr))`, gap 24: Cảnh báo · Bồn nhiên liệu (link "Bồn bể").
- Empty copy: "Chưa có dữ liệu trụ bơm." / "Chưa có dữ liệu doanh thu và sản lượng." / "Chưa có dữ liệu cảnh báo." / "Chưa có dữ liệu bồn." each with one-line explanation.
- Loading: skeleton blocks in every slot (pulse animation opacity 1→.45, 1.4s). Error: whole body replaced with ErrorState "Không thể tải dữ liệu tổng quan." + Thử lại + subtle request id.

## Other station screens
- Giao dịch: filter row (labeled fields: Thời gian, Trụ bơm, Nhiên liệu, Trạng thái; 36px) + search; table with sticky header, columns Mã bơm · Trụ bơm · Nhiên liệu · Đơn giá · Số lít · Thành tiền · Thời gian · Khách hàng · Hóa đơn · Trạng thái (numeric columns right-aligned, min-width 1100, max-height 560 scroll). Empty: "Chưa có giao dịch."
- Theo dõi trụ bơm, Bồn bể, others: page header + empty state. Not-yet-built screens: "Chức năng đang được phát triển."

---

## Reusable components (Angular, standalone)
`AppShell`, `Sidebar`, `Topbar`, `ContextSwitcher`, `Breadcrumb`, `PageHeader`, `SearchField`, `DataTable` styles, `StatusBadge`, `EmptyState`, `ErrorState`, `Skeleton`, `KpiStrip`, `SectionHeader`, `PumpCard`, `TankLevel`, `Toast`.

- **StatusBadge**: 8×8 square dot + 13px label; colors success/warning/danger/unknown only. Unknown uses an outlined square.
- **EmptyState**: left-aligned; 28px line icon (muted), title 16/600 (inline variant 14.5/600), one-line sub 13.5 text-2, optional secondary action. No illustrations.
- **ErrorState**: alert icon danger, title, "Kiểm tra kết nối mạng rồi thử lại…", secondary "Thử lại", request id 11.5px muted if available.
- **PumpCard** (grid `minmax(230px,1fr)`, gap 12): surface, 1px line-soft, **3px top strip** by state (success = đang bơm, danger = offline/lỗi, line-soft = rảnh). Rows: `TRỤ 01` 13/700 tracking .06em + connection badge · fuel 12.5 · state 15/600 · amount 24/700 · `12,48 L · 25.650 ₫/L` · footer `Tổng: … L` above 1px rule. Show only states the backend supports.
- **TankLevel** (row): name/fuel | `8.450 / 12.000 L` + note | 8px bar (track surface-2, fill `#444141`, warning color when low) | percent 15/700.
- **Toast**: bottom-right 24px, text-colored bg, light text, icon, 3.2s.

## Responsive
- ≥1180px: full sidebar. <1180px: icon sidebar, context segments 170px.
- Toolbars wrap; tables scroll horizontally (never squeeze name columns).
- Targets: 1440, 1280, 1024.

## Files
- `prototype.html` — self-contained interactive reference (open in browser). Use the in-sidebar "Mẫu thành phần" page for component patterns.
- `PROMPT_CLAUDE_CODE.md` — prompt to paste into Claude Code.
