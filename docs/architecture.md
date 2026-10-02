# Kiến trúc hệ thống quản lý trạm xăng

## Mục tiêu

Ứng dụng là một modular monolith: Angular gọi duy nhất REST API Spring Boot; backend chọn nguồn dữ liệu qua các provider. PostgreSQL lưu dữ liệu do hệ thống mới sở hữu. SeenPro chỉ là nguồn đọc tạm thời và không định hình domain hay API công khai.

```text
Angular -> /api/v1 -> Application service -> Provider interface
                                                |-- SeenPro provider (hiện tại)
                                                `-- Database provider (tương lai)
```

## Biên giới module

- `auth`: nền tảng định danh và hợp đồng đăng nhập. Flyway tạo user/role; JWT chưa được phát hành cho đến khi chính sách xác thực được chốt.
- `company`: danh sách company/dealer và context đầu tiên sau admin login; không chứa hoặc ánh xạ legacy password.
- `station`, `transaction`, `tank`: domain, application service, provider interface và API riêng.
- `dashboard`: tổng hợp các use case hiện có; không truy cập SeenPro trực tiếp.
- `pump`: model trạng thái thời gian thực độc lập với transport.
- `integration.seenpro`: toàn bộ session, HTTP, legacy model, parser, mapper và provider adapter.
- `infrastructure`: cấu hình security, persistence và framework.

Domain và public DTO không import bất kỳ kiểu nào từ `integration.seenpro`. Application service chỉ biết provider interface. Việc đổi sang PostgreSQL sẽ thêm implementation được chọn bởi cấu hình, không thay API Angular.

Luồng điều hướng đã xác nhận là `login → companies → stations → station activation (302 → menu.php)`. Dashboard và các module vận hành dùng station context đã được lưu trong cùng PHP session; không login hoặc chọn lại station. Dashboard tổng hợp qua các provider theo nguồn và trả partial result khi parser của một nguồn chưa sẵn sàng.

## SeenPro tạm thời

Cookie được giữ trong `CookieManager` ở backend và không được trả về frontend. Base URL SeenPro đến từ cấu hình môi trường; credential của public login chỉ đi qua bộ nhớ tới adapter, không được lưu hoặc log. POST đăng nhập duy nhất tới `checklogin.php` đã được xác nhận và không được retry; sau đó backend GET trang verification bằng cùng cookie store. Verifier chỉ trả `AUTHENTICATED` khi đồng thời có response thành công và các marker HTML đã xác nhận; HTTP 200 hoặc cookie riêng lẻ không đủ. Failed-login và session-expiry vẫn trả `UNVERIFIED` cho đến khi có fixture đã làm sạch.

Cache Caffeine gộp các request giống nhau. TTL mặc định 30 giây là cấu hình demo; trước production cần tách TTL theo station, tank, price, realtime và transaction.

## PostgreSQL và migration

Flyway hiện chỉ quản lý user, role và audit của hệ thống mới. Không có lịch sử SeenPro giả hoặc migration business data. Migration tương lai đi qua dữ liệu thô bất biến, normalizer và validation rồi mới ghi PostgreSQL. Khi hoàn tất, các database provider thay SeenPro provider.

## Realtime tương lai

`PumpRealtime` và `PumpRealtimeProvider` là hợp đồng trung lập. `OnlineAggregator` lấy một snapshot từ provider, cache ngắn hạn và đưa vào duy nhất Dashboard response. Angular không gọi các endpoint polling legacy.

SeenPro adapter phải bootstrap từ `online.php` và JavaScript cùng trang để tạo `PumpDescriptor` trước khi polling. Descriptor chứa `maCot`, tên trụ, `maNhienLieu`, `standardizedMAC`, `master`, `slave`, `user` và các identifier bổ sung đã được xác nhận. Không identifier nào được hardcode hoặc suy ra theo thứ tự trụ.

Hiện repo chưa có fixture `online.php`/JavaScript hay capture request polling. Bootstrap parser vì vậy fail-fast và báo chính xác identifier thiếu. `SeenProOnlineClient` nhận `PumpDescriptor` nhưng chưa phát request tới `gettienhome.php`, `getlithome.php`, `getgiahome.php`, `gettotal.php`, `getconnectstate.php`, `getpumpstate.php` cho đến khi xác nhận HTTP method, parameter mapping và response correlation key. Dashboard nhận partial state thay vì dữ liệu suy đoán.

Đường đi mục tiêu là gateway → MQTT broker → `MqttPumpRealtimeProvider` → `OnlineAggregator` → Dashboard. Việc thay SeenPro bằng MQTT không thay Dashboard service, REST contract hoặc Angular component.
