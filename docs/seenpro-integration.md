# Theo dõi tích hợp SeenPro

Không mục nào được đánh dấu `CONFIRMED` nếu chưa có request/response hoặc fixture đã được kiểm chứng. SeenPro chỉ là nguồn dữ liệu đọc tạm thời trong giai đoạn phát triển/demo; domain và REST API mới không phụ thuộc mô hình SeenPro.

| Feature | SeenPro page/endpoint | Method | Response | Adapter | Status |
|---|---|---|---|---|---|
| Login/session | `checklogin.php`, verification `view.php` | POST form, sau đó GET | HTML xác thực | Cookie giữ hoàn toàn ở backend | PARTIAL |
| Company/dealer list | authenticated company-level `view.php` | GET | HTML server-rendered | `SeenProCompanyParser` → `SeenProCompanyMapper` | CONFIRMED |
| Station list | `view.php` company → station | GET | HTML server-rendered | `SeenProStationParser` → `SeenProStationMapper` | CONFIRMED |
| Pump Code History | `theodoibanhang.php` | GET | HTML server-rendered | Isolated behind `SeenProPumpCodeAdapter` | PARTIAL |
| Tanks | Trang bồn server-rendered | Chưa xác nhận | HTML | Chưa có parser | PARTIAL |
| Realtime | `waittimeupdate.php`, `giaupdate.php` | Chưa xác nhận | Chưa xác nhận | Không gọi trước khi có trace | NOT_TRACED |

## Luồng Company → Station

```text
Angular
  → GET /api/v1/companies/{companyId}/stations
  → StationService
  → StationProvider
  → SeenProStationProvider
  → GET /view.php?gl=3&al={companyAccount}&opt=v
  → server-rendered station HTML
  → SeenProStationParser
  → SeenProStationMapper
  → Station domain/DTO
  → Angular
```

Các tham số `gl`, `al`, `opt`, URL legacy, account legacy và HTML selector chỉ tồn tại trong `integration.seenpro`. API công khai chỉ nhận `companyId`. Provider giải mã tham chiếu của Company hiện có tại boundary tích hợp; không hardcode account cụ thể.

Password xuất hiện trong HTML SeenPro bị bỏ qua hoàn toàn: parser không đọc vào model, mapper/domain/API không có trường password, log không chứa HTML body. Cookie/PHPSESSID và credentials cũng không rời backend.

Danh sách station được cache ở application service bằng Caffeine theo `companyId`. TTL mặc định 30 giây và có thể cấu hình qua `CACHE_SPEC`. Không dùng Redis và không gọi lại SeenPro khi Angular chỉ rerender.

Tích hợp Station hiện chỉ đọc. Không gọi link sửa, quản lý hay bất kỳ form mutation nào. Không migration dữ liệu lịch sử và không lưu dữ liệu station SeenPro vào PostgreSQL. Sau này `DatabaseStationProvider` sẽ thay `SeenProStationProvider` mà không đổi Angular API.

## Kích hoạt ngữ cảnh Station

Luồng phân cấp và kích hoạt đã được xác nhận từ HTTP trace:

```text
Admin
  → GET view.php?gl=4&al={admin}&opt=v
  → Company list
  → GET view.php?gl=3&al={companyAccount}&opt=v
  → Station list
  → POST /api/v1/stations/{stationId}/select       (API mới)
  → resolve stationId thành legacy reference      (chỉ trong integration.seenpro)
  → GET view.php?gl=2&al={stationAccount}&opt=v   (cùng PHP session)
  → HTTP 302 Location: menu.php
  → active SeenPro station context
  → menu.php
  → quanlycuahang.php
  → các module vận hành theo trạm
```

Behavior `gl=2` trả `302 → menu.php` là `CONFIRMED`. `SeenProStationContextManager` không follow redirect cho operation này và chỉ công nhận đúng status/location trên. HTTP 200, login page hoặc redirect khác không được xem là thành công. Cấu trúc biến session PHP nội bộ chưa biết và không cần reverse-engineer.

`stationId` là ID opaque của hệ thống mới, không được dùng làm giá trị `al`. Trong adapter tạm thời, `SeenProStationReferenceRegistry` lưu ánh xạ `stationId → legacy station account` khi danh sách station được provider đọc. Angular và Station DTO không nhận mapping này. Operation select không cache vì nó thay đổi server-side PHP session context.

### Giới hạn concurrency hiện tại

`InMemorySeenProSessionManager` là Spring singleton và chỉ có một `CookieManager`/`PHPSESSID` cho toàn bộ backend process. Vì vậy ngữ cảnh station hiện là mutable state dùng chung: hai người dùng chọn hai trạm có thể ghi đè ngữ cảnh của nhau. Đây chỉ phù hợp development/demo và **không an toàn multi-user production**. Chưa thêm session pool phức tạp trong adapter tạm thời; kiến trúc production dài hạn sẽ dùng backend/database/MQTT mới.

Khi SeenPro session hết hạn, activation trả lỗi tích hợp hiện có. Chưa tự động login lại vì response expiry/login-page chính xác chưa được trace đầy đủ.

## Dashboard sau khi chọn Station

Dashboard dùng station context đã được lưu trong cùng PHP session sau `302 → menu.php`. Nó không login lại, không chọn lại company và không gọi lại operation station activation.

```text
DashboardController
  → DashboardService
  → DashboardProvider (composite)
      → StoreInfoProvider   → quanlycuahang.php
      → ReportProvider      → baocao.php
      → TankProvider        → khohang.php
      → PriceProvider       → quanlygia.php
      → PumpCodeHistoryProvider → SeenProPumpCodeAdapter → theodoibanhang.php
```

Mỗi nguồn được cô lập. Một parser chưa hoàn thiện không làm `/api/v1/dashboard/summary` trả 503; response trả `partial=true`, metric chưa xác minh là `null`, và liệt kê `unavailableSources`. Không thay `null` bằng số 0 vì như vậy sẽ tạo dữ liệu nghiệp vụ giả.

`theodoibanhang.php` là chi tiết triển khai legacy cho module nghiệp vụ Mã bơm. Tên endpoint và query parameter SeenPro không xuất hiện trong REST/domain hoặc Angular.

## Online bootstrap

```text
online.php + referenced JavaScript
  → SeenProOnlineBootstrapProvider
  → PumpDescriptor list
  → SeenProOnlineClient
  → polling endpoints
  → SeenProPumpRealtimeMapper
  → OnlineAggregator
  → PumpRealtime
  → Dashboard
```

Bootstrap chỉ tạo descriptor khi các identifier bắt buộc xuất hiện trong cùng một object pump: `maCot`, `pumpName`, `maNhienLieu`, `standardizedMAC`, `master`, `slave`, `user`. Không ghép identifier từ các object khác nhau và không suy luận theo vị trí.

Polling đang fail-fast trước network call vì còn thiếu capture xác nhận `pollingHttpMethod`, `pollingParameterMapping` và `pollingResponseCorrelationKey`. Cần cung cấp sanitized HTML của `online.php`, toàn bộ JavaScript cùng origin được trang tham chiếu, và một request/response mẫu cho mỗi polling endpoint. Không gửi cookie, credential hoặc dữ liệu khách hàng thật.

## Trace còn cần

1. Fixture đăng nhập thất bại và response khi session hết hạn để hoàn tất phân loại `REJECTED`/expiry.
2. HTML giao dịch đã làm sạch gồm trang đầu, trang kế và trường hợp rỗng.
3. HTML danh sách/trạng thái bồn bể và request chỉ đọc tương ứng.

Không gửi password hoặc cookie thật. Fixture phải giữ cấu trúc markup/encoding nhưng thay toàn bộ dữ liệu nhận diện bằng dữ liệu giả.
