SET NAMES utf8mb4;
USE `tuyen_sinh_db`;

START TRANSACTION;

-- Reset feature tables so the e2e scenario is deterministic.
DELETE FROM `xt_diemcongxettuyen`;
DELETE FROM `xt_nguyenvongxettuyen`;

-- Reset only e2e-owned candidates and majors.
DELETE FROM `xt_thisinhxettuyen25`
WHERE `cccd` LIKE '9000000000%';

DELETE FROM `xt_nganh`
WHERE `manganh` IN ('7480201', '7340101', '7220201');

INSERT INTO `xt_nganh`
    (`manganh`, `tennganh`, `n_tohopgoc`, `n_chitieu`, `n_diemsan`,
     `n_diemtrungtuyen`, `n_tuyenthang`, `n_dgnl`, `n_thpt`, `n_vsat`,
     `sl_xtt`, `sl_dgnl`, `sl_vsat`, `sl_thpt`)
VALUES
    ('7480201', 'Cong nghe thong tin', 'A01', 2, 24.00, NULL, NULL, NULL, 'Y', NULL, NULL, NULL, NULL, NULL),
    ('7340101', 'Quan tri kinh doanh', 'D01', 2, 22.00, NULL, NULL, NULL, 'Y', NULL, NULL, NULL, NULL, NULL),
    ('7220201', 'Ngon ngu Anh', 'D01', 1, 21.00, NULL, NULL, NULL, 'Y', NULL, NULL, NULL, NULL, NULL);

INSERT INTO `xt_thisinhxettuyen25`
    (`cccd`, `sobaodanh`, `ho`, `ten`, `ngay_sinh`, `dien_thoai`, `password`,
     `gioi_tinh`, `email`, `noi_sinh`, `updated_at`, `doi_tuong`, `khu_vuc`)
VALUES
    ('900000000001', 'E2E001', 'Nguyen Van', 'An', '2007-01-10', '0900000001', '123456', 'Nam', 'an.e2e@example.com', 'Ha Noi', CURRENT_DATE, '01', 'KV1'),
    ('900000000002', 'E2E002', 'Tran Thi', 'Binh', '2007-02-11', '0900000002', '123456', 'Nu', 'binh.e2e@example.com', 'Da Nang', CURRENT_DATE, '00', 'KV2'),
    ('900000000003', 'E2E003', 'Le Minh', 'Chi', '2007-03-12', '0900000003', '123456', 'Nam', 'chi.e2e@example.com', 'Can Tho', CURRENT_DATE, '00', 'KV2'),
    ('900000000004', 'E2E004', 'Pham Thu', 'Dung', '2007-04-13', '0900000004', '123456', 'Nu', 'dung.e2e@example.com', 'Hue', CURRENT_DATE, '00', 'KV3'),
    ('900000000005', 'E2E005', 'Hoang Gia', 'Em', '2007-05-14', '0900000005', '123456', 'Nam', 'em.e2e@example.com', 'Hai Phong', CURRENT_DATE, '00', 'KV2'),
    ('900000000006', 'E2E006', 'Vo Quang', 'Gia', '2007-06-15', '0900000006', '123456', 'Nam', 'gia.e2e@example.com', 'Lam Dong', CURRENT_DATE, '00', 'KV1'),
    ('900000000007', 'E2E007', 'Bui Ngoc', 'Han', '2007-07-16', '0900000007', '123456', 'Nu', 'han.e2e@example.com', 'Binh Dinh', CURRENT_DATE, '00', 'KV2'),
    ('900000000008', 'E2E008', 'Dang Mai', 'Vy', '2007-08-17', '0900000008', '123456', 'Nu', 'vy.e2e@example.com', 'Quang Nam', CURRENT_DATE, '00', 'KV3'),
    ('900000000009', 'E2E009', 'Do Thanh', 'Khang', '2007-09-18', '0900000009', '123456', 'Nam', 'khang.e2e@example.com', 'TP HCM', CURRENT_DATE, '00', 'KV2');

INSERT INTO `xt_diemcongxettuyen`
    (`ts_cccd`, `manganh`, `matohop`, `phuongthuc`, `diemCC`, `diemUtxt`, `diemTong`, `ghichu`, `dc_keys`)
VALUES
    ('900000000001', '7480201', 'A01', 'PT2', 1.00, 0.00, 1.00, 'E2E: cong diem uu tien de vao top 2 CNTT', '900000000001_7480201_A01'),
    ('900000000003', '7480201', 'A01', 'PT2', 0.20, 0.00, 0.20, 'E2E: du diem san nhung rot do het chi tieu', '900000000003_7480201_A01'),
    ('900000000004', '7340101', 'D01', 'PT2', 0.30, 0.00, 0.30, 'E2E: van duoi diem san QTKD', '900000000004_7340101_D01'),
    ('900000000005', '7340101', 'D01', 'PT2', 0.50, 0.00, 0.50, 'E2E: vuot diem san QTKD nho diem cong', '900000000005_7340101_D01');

INSERT INTO `xt_nguyenvongxettuyen`
    (`nn_cccd`, `nv_manganh`, `nv_tt`, `diem_thxt`, `diem_utqd`, `diem_cong`,
     `diem_xettuyen`, `nv_ketqua`, `nv_keys`, `tt_phuongthuc`, `tt_thm`)
VALUES
    ('900000000001', '7480201', 1, 26.00000, 0.00000, NULL, NULL, 'chuaxet', '900000000001_7480201_PT2', 'PT2', 'A01'),
    ('900000000002', '7480201', 1, 25.00000, 0.00000, NULL, NULL, 'chuaxet', '900000000002_7480201_PT2', 'PT2', 'A01'),
    ('900000000003', '7480201', 1, 24.50000, 0.00000, NULL, NULL, 'chuaxet', '900000000003_7480201_PT2', 'PT2', 'A01'),
    ('900000000004', '7340101', 1, 20.50000, 0.00000, NULL, NULL, 'chuaxet', '900000000004_7340101_PT2', 'PT2', 'D01'),
    ('900000000005', '7340101', 1, 21.80000, 0.00000, NULL, NULL, 'chuaxet', '900000000005_7340101_PT2', 'PT2', 'D01'),
    ('900000000006', '7480201', 1, 24.20000, 0.00000, NULL, NULL, 'chuaxet', '900000000006_7480201_PT2', 'PT2', 'A01'),
    ('900000000006', '7220201', 2, 22.50000, 0.00000, NULL, NULL, 'chuaxet', '900000000006_7220201_PT2', 'PT2', 'D01'),
    ('900000000007', '9999999', 1, 28.00000, 0.00000, NULL, NULL, 'chuaxet', '900000000007_9999999_PT2', 'PT2', 'A01'),
    ('900000000008', '7480201', 1, NULL, 0.00000, NULL, NULL, 'chuaxet', '900000000008_7480201_PT2', 'PT2', 'A01'),
    ('900000000009', '7340101', 1, 23.00000, 0.00000, NULL, NULL, 'chuaxet', '900000000009_7340101_PT2', 'PT2', 'D01');

COMMIT;
