Bạn là Senior Product Designer + Senior Angular Frontend Engineer.

Trong repo này có thư mục `design_handoff_gas_station_ui/`:
- `README.md` — spec thiết kế chi tiết (tokens, layout, từng màn hình, trạng thái, component).
- `prototype.html` — bản mẫu tương tác, mở bằng trình duyệt để xem.

Nhiệm vụ: redesign UI của ứng dụng Angular hiện có cho khớp với spec và prototype. Đây là việc làm lại UI/UX, KHÔNG viết lại ứng dụng.

Bắt buộc:
- Không đổi backend, REST API, SeenPro adapter, auth, auth guard, routing, CompanyContext, StationContext, logic chọn công ty/trạm hiện có.
- Không tạo số liệu giả cho dashboard/trụ bơm/bồn/giao dịch — API chưa có thì dùng empty/loading state như spec.
- Không bao giờ hiển thị/log/lưu: mật khẩu, PHPSESSID, cookie/credential SeenPro, gl/al/opt, view.php, menu.php, quanlycuahang.php.
- Không thêm thư viện state management. Dùng một bộ icon duy nhất (lucide-angular nếu chưa có).
- Prototype chỉ là tài liệu tham khảo — không copy HTML/JS của nó vào project.

Thực hiện theo thứ tự:
1. Đọc kiến trúc Angular hiện tại (routes, services, models Company/Station, context, styles, shared components). Báo cáo ngắn: cấu trúc UI, component có thể tái dùng, file định sửa, dependency định thêm. Dừng lại chờ tôi xác nhận.
2. Thêm design tokens (README → "Design tokens") vào global styles; font Be Vietnam Pro.
3. AppShell, Sidebar (khóa chức năng khi chưa chọn trạm), Topbar, ContextSwitcher, Breadcrumb.
4. Trang Công ty / Đại lý.
5. Trang Trạm xăng + luồng mở trạm async (loading, chặn bấm lặp, chỉ điều hướng khi backend báo thành công, lỗi inline + toast).
6. Dashboard foundation (KPI strip, các section, empty/loading/error).
7. Component dùng lại: StatusBadge, EmptyState, ErrorState, Skeleton, PumpCard, TankLevel, style bảng giao dịch, Toast.
8. Chạy `ng build` (development), sửa lỗi.
9. Tự rà lại theo checklist: độ rộng sidebar, trạng thái active, chiều cao topbar, context rõ ràng, mật độ bảng, căn cột số phải, tên tiếng Việt dài, thiếu SĐT/email, trạng thái chưa xác định, 1440/1280/1024, cuộn ngang bảng.

Cuối cùng báo cáo: file đã sửa, component tạo/refactor, tokens, thay đổi từng trang, responsive, các state, dependency thêm, kết quả build, việc còn lại.
