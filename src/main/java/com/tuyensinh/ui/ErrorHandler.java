package com.tuyensinh.ui;

public class ErrorHandler {
    public static String getFriendlyMessage(Exception ex) {
        StringBuilder fullMsgBuilder = new StringBuilder();
        Throwable t = ex;
        while (t != null) {
            fullMsgBuilder.append(t.getMessage()).append(" ");
            t = t.getCause();
        }
        String fullMsg = fullMsgBuilder.toString();

        // Kiểm tra lỗi Khóa ngoại (quan trọng nhất)
        if (fullMsg.contains("foreign key constraint fails")) {
            if (fullMsg.contains("fk_nganhtohop_tohop")) {
                return "Lỗi: Tổ hợp môn bạn nhập chưa có trong danh mục. Vui lòng thêm Tổ hợp này trước.";
            }
            if (fullMsg.contains("fk_nganhtohop_nganh") || fullMsg.contains("nganh_id")) {
                return "Lỗi: Mã ngành bạn nhập không tồn tại. Vui lòng kiểm tra lại danh mục Ngành.";
            }
            return "Lỗi: Dữ liệu liên quan (Ngành hoặc Tổ hợp) không tồn tại trong hệ thống.";
        }

        // Kiểm tra lỗi trùng dữ liệu
        if (fullMsg.contains("Duplicate entry")) {
            return "Lỗi: Bản ghi này đã tồn tại (trùng mã ngành và tổ hợp).";
        }

        // Kiểm tra lỗi kết nối bị đóng nhưng có thể do lỗi dữ liệu trước đó
        if (fullMsg.contains("is closed") || fullMsg.contains("Connection") || fullMsg.contains("Session")) {
            return "Lỗi: Không thể thực hiện thao tác. Vui lòng kiểm tra lại mã ngành/tổ hợp đã tồn tại trong danh mục chưa.";
        }

        if (fullMsg.contains("Data truncation") || fullMsg.contains("Data too long")) {
            return "Lỗi: Dữ liệu nhập vào quá dài hoặc không đúng định dạng.";
        }

        return "Lỗi: " + (ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi không xác định.");
    }
}
