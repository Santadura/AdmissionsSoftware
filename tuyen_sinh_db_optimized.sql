CREATE DATABASE IF NOT EXISTS tuyen_sinh_db_optimized CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tuyen_sinh_db_optimized;

-- 1. Xóa các bảng nếu đã tồn tại 
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `xt_nguyenvongxettuyen`;
DROP TABLE IF EXISTS `xt_diemcongxetuyen`;
DROP TABLE IF EXISTS `xt_diemthixettuyen`;
DROP TABLE IF EXISTS `xt_nganh_tohop`;
DROP TABLE IF EXISTS `xt_thisinh`; 
DROP TABLE IF EXISTS `xt_nganh`;
DROP TABLE IF EXISTS `xt_tohop_monthi`;
DROP TABLE IF EXISTS `xt_bangquydoi`;

-- 2. Bảng quy đổi điểm
CREATE TABLE `xt_bangquydoi` (
  `idqd` int NOT NULL AUTO_INCREMENT,
  `d_phuongthuc` varchar(45) DEFAULT NULL,
  `d_tohop` varchar(45) DEFAULT NULL,
  `d_mon` varchar(45) DEFAULT NULL,
  `d_diema` decimal(6,2) DEFAULT NULL,
  `d_diemb` decimal(6,2) DEFAULT NULL,
  `d_diemc` decimal(6,2) DEFAULT NULL,
  `d_diemd` decimal(6,2) DEFAULT NULL,
  `d_maquydoi` varchar(45) DEFAULT NULL,
  `d_phanvi` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`idqd`),
  UNIQUE KEY `d_maquydoi_UNIQUE` (`d_maquydoi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng Thí sinh
CREATE TABLE `xt_thisinh` (
  `idthisinh` int NOT NULL AUTO_INCREMENT,
  `cccd` char(12) NOT NULL,
  `sobaodanh` varchar(45) DEFAULT NULL,
  `ho` varchar(100) DEFAULT NULL,
  `ten` varchar(100) DEFAULT NULL,
  `ngay_sinh` DATE DEFAULT NULL,
  `dien_thoai` varchar(20) DEFAULT NULL,
  `gioi_tinh` varchar(10) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `noi_sinh` varchar(50) DEFAULT NULL,
  `doi_tuong` varchar(45) DEFAULT NULL,
  `khu_vuc` varchar(45) DEFAULT NULL,
  `nam_tuyen_sinh` year NOT NULL,
  `updated_at` date DEFAULT NULL,
  PRIMARY KEY (`idthisinh`),
  UNIQUE KEY `cccd_UNIQUE` (`cccd`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng Users
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','USER') NOT NULL DEFAULT 'USER',
  `enabled` tinyint(1) NOT NULL DEFAULT '1',
  `thisinh_id` int DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_thisinh_id` (`thisinh_id`),
  CONSTRAINT `fk_users_thisinh` FOREIGN KEY (`thisinh_id`) REFERENCES `xt_thisinh` (`idthisinh`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng Tổ hợp môn
CREATE TABLE `xt_tohop_monthi` (
  `idtohop` int NOT NULL AUTO_INCREMENT,
  `matohop` varchar(45) NOT NULL,
  `mon1` varchar(50) NOT NULL,
  `mon2` varchar(50) NOT NULL,
  `mon3` varchar(50) NOT NULL,
  `tentohop` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`idtohop`),
  UNIQUE KEY `matohop_UNIQUE` (`matohop`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng Ngành
CREATE TABLE `xt_nganh` (
  `idnganh` int NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) NOT NULL,
  `tennganh` varchar(100) NOT NULL,
  `n_tohopgoc` varchar(3) DEFAULT NULL,
  `n_chitieu` int NOT NULL DEFAULT '0',
  `n_diemsan` decimal(5,2) DEFAULT NULL,
  `n_diemtrungtuyen` decimal(5,2) DEFAULT NULL,
  `n_tuyenthang` varchar(1) DEFAULT NULL,
  `n_dgnl` varchar(1) DEFAULT NULL,
  `n_thpt` varchar(1) DEFAULT NULL,
  `n_vsat` varchar(1) DEFAULT NULL,
  `sl_xtt` int DEFAULT NULL,
  `sl_dgnl` int DEFAULT NULL,
  `sl_vsat` int DEFAULT NULL,
  `sl_thpt` int DEFAULT NULL,
  `nam_tuyen_sinh` year NOT NULL,
  PRIMARY KEY (`idnganh`),
  UNIQUE KEY `manganh_nam_UNIQUE` (`manganh`, `nam_tuyen_sinh`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng Ngành - Tổ Hợp
CREATE TABLE `xt_nganh_tohop` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nganh_id` int NOT NULL,
  `matohop` varchar(45) NOT NULL,
  `th_mon1` varchar(50) DEFAULT NULL,
  `hsmon1` tinyint DEFAULT 1,
  `th_mon2` varchar(50) DEFAULT NULL,
  `hsmon2` tinyint DEFAULT 1,
  `th_mon3` varchar(50) DEFAULT NULL,
  `hsmon3` tinyint DEFAULT 1,
  `dolech` decimal(6,2) DEFAULT NULL,
  `tb_keys` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `key_UNIQUE` (`tb_keys`),
  CONSTRAINT `fk_nganhtohop_nganh` FOREIGN KEY (`nganh_id`) REFERENCES `xt_nganh` (`idnganh`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_nganhtohop_tohop` FOREIGN KEY (`matohop`) REFERENCES `xt_tohop_monthi` (`matohop`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng Điểm thi
CREATE TABLE `xt_diemthixettuyen` (
  `iddiemthi` int NOT NULL AUTO_INCREMENT,
  `thisinh_cccd` char(12) NOT NULL, 
  `sobaodanh` varchar(45) DEFAULT NULL,
  `d_phuongthuc` varchar(45) NOT NULL,
  `TO` decimal(5,2) DEFAULT '0.00', 
  `LI` decimal(5,2) DEFAULT NULL,
  `HO` decimal(5,2) DEFAULT NULL,
  `SI` decimal(5,2) DEFAULT NULL,
  `SU` decimal(5,2) DEFAULT NULL,
  `DI` decimal(5,2) DEFAULT NULL,
  `VA` decimal(5,2) DEFAULT NULL,
  `N1_THI` decimal(5,2) DEFAULT NULL,
  `N1_CC` decimal(5,2) DEFAULT NULL,
  `CNCN` decimal(5,2) DEFAULT NULL,
  `CNNN` decimal(5,2) DEFAULT NULL,
  `TI` decimal(5,2) DEFAULT NULL,
  `KTPL` decimal(5,2) DEFAULT NULL,
  `NL1` decimal(5,2) DEFAULT NULL,
  `NK1` decimal(5,2) DEFAULT NULL,
  `NK2` decimal(5,2) DEFAULT NULL,
  `NK3` decimal(5,2) DEFAULT NULL,
  `NK4` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`iddiemthi`),
  UNIQUE KEY `uk_cccd_phuongthuc` (`thisinh_cccd`,`d_phuongthuc`),
  CONSTRAINT `fk_diemthi_thisinh` FOREIGN KEY (`thisinh_cccd`) REFERENCES `xt_thisinh` (`cccd`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng Điểm Cộng
CREATE TABLE `xt_diemcongxetuyen` (
  `iddiemcong` int unsigned NOT NULL AUTO_INCREMENT,
  `thisinh_cccd` char(12) NOT NULL,
  `nganh_id` int DEFAULT NULL,
  `matohop` varchar(10) DEFAULT NULL,
  `phuongthuc` varchar(45) DEFAULT NULL,
  `diemCC` decimal(5,2) DEFAULT NULL,
  `diemUtxt` decimal(5,2) DEFAULT NULL,
  `diemTong` decimal(5,2) DEFAULT NULL,
  `ghichu` text,
  `dc_keys` varchar(45) NOT NULL,
  PRIMARY KEY (`iddiemcong`),
  UNIQUE KEY `dc_keys_UNIQUE` (`dc_keys`),
  CONSTRAINT `fk_diemcong_thisinh` FOREIGN KEY (`thisinh_cccd`) REFERENCES `xt_thisinh` (`cccd`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_diemcong_nganh` FOREIGN KEY (`nganh_id`) REFERENCES `xt_nganh` (`idnganh`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Bảng Nguyện vọng
CREATE TABLE `xt_nguyenvongxettuyen` (
  `idnv` int NOT NULL AUTO_INCREMENT,
  `thisinh_cccd` char(12) NOT NULL,
  `nv_nganh_id` int NOT NULL,
  `nv_tt` int NOT NULL,
  `diem_thxt` decimal(5,2) DEFAULT NULL,
  `diem_utqd` decimal(5,2) DEFAULT NULL,
  `diem_cong` decimal(5,2) DEFAULT NULL,
  `diem_xettuyen` decimal(5,2) DEFAULT NULL,
  `nv_ketqua` varchar(45) DEFAULT NULL,
  `nv_keys` varchar(45) DEFAULT NULL,
  `tt_phuongthuc` varchar(45) DEFAULT NULL,
  `tt_thm` varchar(45) DEFAULT NULL,
  `nv_matohop` varchar(45) DEFAULT NULL,
  `nv_rank` int DEFAULT NULL,
  `diem_cc` decimal(5,2) DEFAULT NULL,
  `diem_utxt` decimal(5,2) DEFAULT NULL,
  PRIMARY KEY (`idnv`),
  UNIQUE KEY `nv_keys_UNIQUE` (`nv_keys`),
  CONSTRAINT `fk_nguyenvong_thisinh` FOREIGN KEY (`thisinh_cccd`) REFERENCES `xt_thisinh` (`cccd`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_nguyenvong_nganh` FOREIGN KEY (`nv_nganh_id`) REFERENCES `xt_nganh` (`idnganh`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE xt_tohop_monthi MODIFY mon1 VARCHAR(50) NOT NULL;
ALTER TABLE xt_tohop_monthi MODIFY mon2 VARCHAR(50) NOT NULL;
ALTER TABLE xt_tohop_monthi MODIFY mon3 VARCHAR(50) NOT NULL;

ALTER TABLE xt_nganh_tohop MODIFY th_mon1 VARCHAR(50);
ALTER TABLE xt_nganh_tohop MODIFY th_mon2 VARCHAR(50);
ALTER TABLE xt_nganh_tohop MODIFY th_mon3 VARCHAR(50);

CREATE INDEX idx_candidate_cccd ON xt_thisinh(cccd);
