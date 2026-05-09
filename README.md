# AdmissionsSoftware

Phần mềm tuyển sinh dạng **Admin Desktop Application** được xây dựng bằng **Java Swing**, **Hibernate** và **MySQL** theo yêu cầu bài tập lớn/đồ án môn học [file:1].

## Công nghệ sử dụng

- Java Swing để xây dựng giao diện desktop admin [file:1]
- Hibernate để làm ORM, thao tác với cơ sở dữ liệu [file:1]
- MySQL để lưu trữ dữ liệu hệ thống [file:1]
- Maven để quản lý thư viện và build project

## Chức năng chính

Hệ thống hiện được định hướng theo 9 chức năng admin trong tài liệu yêu cầu [file:1]:

1. Quản lý người dùng: xem danh sách, sửa thông tin, đổi mật khẩu, đổi quyền user/admin, enable/disable [file:1]
2. Quản lý thí sinh: import, xem danh sách, tìm kiếm theo CCCD/họ tên, sửa thông tin [file:1]
3. Quản lý ngành tuyển sinh: import, xem, thêm, sửa, xóa [file:1]
4. Quản lý tổ hợp môn xét tuyển: import, xem, thêm, sửa, xóa [file:1]
5. Quản lý danh sách ngành - tổ hợp [file:1]
6. Quản lý điểm thí sinh: THPT, VSAT, ĐGNL; import, CRUD, thống kê [file:1]
7. Quản lý điểm cộng: import, thêm, sửa, xóa [file:1]
8. Quản lý nguyện vọng và xét tuyển [file:1]
9. Quản lý bảng quy đổi: import, xem, thêm, sửa, xóa, tìm kiếm [file:1]

## Yêu cầu môi trường

Trước khi chạy project, cần cài đặt:

- JDK 17 hoặc mới hơn
- Apache Maven
- MySQL 8.x
- Visual Studio Code hoặc IDE hỗ trợ Java
- Extension Pack for Java nếu dùng VS Code

## Cấu trúc thư mục

```text
tuyen-sinh-admin/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── tuyensinh/
│       │           ├── App.java
│       │           ├── config/
│       │           ├── entity/
│       │           ├── repository/
│       │           ├── service/
│       │           └── ui/
│       │               ├── login/
│       │               ├── dashboard/
│       │               ├── user/
│       │               ├── candidate/
│       │               ├── major/
│       │               ├── combination/
│       │               ├── score/
│       │               ├── bonus/
│       │               ├── aspiration/
│       │               └── conversion/
│       └── resources/
│           ├── hibernate.cfg.xml
│           └── application.properties
└── target/
```

## Cách chạy project

### 1. Clone source code

```bash
git clone https://github.com/Santadura/AdmissionsSoftware.git
cd AdmissionsSoftware
```

Nếu project thật nằm trong thư mục con `tuyen-sinh-admin` thì chuyển vào đúng thư mục chứa `pom.xml` trước khi chạy Maven:

```bash
cd tuyen-sinh-admin
```

### 2. Cài dependency và compile

```bash
mvn compile
```

### 3. Chạy ứng dụng

```bash
mvn exec:java -Dexec.mainClass=com.tuyensinh.App
```

Nếu chưa cấu hình `exec-maven-plugin`, có thể chạy trực tiếp file `App.java` bằng **Run Java** trong VS Code.

## Cấu hình cơ sở dữ liệu

Tạo database MySQL, ví dụ:

```sql
CREATE DATABASE tuyen_sinh_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Sau đó cấu hình thông tin kết nối trong file `src/main/resources/hibernate.cfg.xml`:

- URL kết nối MySQL
- Username
- Password
- Tên database

Ví dụ:

```xml
<property name="hibernate.connection.url">jdbc:mysql://localhost:3306/tuyen_sinh_db</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">123456</property>
```

## Ghi chú

- Project hiện ưu tiên dựng **khung giao diện admin** trước để cả nhóm phát triển tiếp từng module [file:1]
- Phần đang được ưu tiên phát triển đầu tiên là **Quản lý người dùng** theo đúng phân công công việc [file:1]
- Các module còn lại sẽ tiếp tục tích hợp vào dashboard chung theo 9 chức năng của tài liệu [file:1]

## Thành viên thực hiện

Cập nhật danh sách thành viên nhóm tại đây.

## License

Dự án phục vụ mục đích học tập.
