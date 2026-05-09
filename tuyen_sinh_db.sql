-- MySQL dump 10.13  Distrib 8.0.13, for Win64 (x86_64)
--
-- Host: localhost    Database: tuyen_sinh_db
-- ------------------------------------------------------
-- Server version	8.0.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `xt_bangquydoi`
--

DROP TABLE IF EXISTS `xt_bangquydoi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `xt_bangquydoi` (
  `idqd` int(11) NOT NULL AUTO_INCREMENT,
  `d_phuongthuc` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_tohop` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_mon` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_diema` decimal(6,2) DEFAULT NULL,
  `d_diemb` decimal(6,2) DEFAULT NULL,
  `d_diemc` decimal(6,2) DEFAULT NULL,
  `d_diemd` decimal(6,2) DEFAULT NULL,
  `d_maquydoi` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_phanvi` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idqd`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_bangquydoi`
--

LOCK TABLES `xt_bangquydoi` WRITE;
/*!40000 ALTER TABLE `xt_bangquydoi` DISABLE KEYS */;
INSERT INTO `xt_bangquydoi` VALUES (2,'DGNL','A01',NULL,980.00,997.00,25.75,26.10,'DGNL_A01_3','3'),(3,'DGNL','A01',NULL,973.00,983.00,25.35,25.65,'DGNL_A01_4','4'),(4,'DGNL','A01',NULL,962.00,972.00,25.05,25.25,'DGNL_A01_5','5'),(5,'DGNL','A01',NULL,954.00,961.00,24.85,25.00,'DGNL_A01_6','6'),(6,'DGNL','A01',NULL,946.00,953.00,24.60,24.75,'DGNL_A01_7','7'),(7,'DGNL','A01',NULL,939.00,945.00,24.30,24.50,'DGNL_A01_8','8'),(8,'DGNL','A01',NULL,932.00,938.00,24.05,24.25,'DGNL_A01_9','9'),(9,'DGNL','A01',NULL,926.00,931.00,24.00,24.20,'DGNL_A01_10','10'),(10,'DGNL','A01',NULL,919.00,925.00,23.80,23.95,'DGNL_A01_11','11'),(11,'DGNL','A01',NULL,913.00,918.00,23.55,23.75,'DGNL_A01_12','12');
/*!40000 ALTER TABLE `xt_bangquydoi` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_diemcongxettuyen`
--

DROP TABLE IF EXISTS `xt_diemcongxettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `xt_diemcongxettuyen` (
  `iddiemcong` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `ts_cccd` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `manganh` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `matohop` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phuongthuc` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `diemCC` decimal(6,2) DEFAULT NULL,
  `diemUtxt` decimal(6,2) DEFAULT NULL,
  `diemTong` decimal(6,2) DEFAULT NULL,
  `ghichu` text COLLATE utf8mb4_unicode_ci,
  `dc_keys` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`iddiemcong`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_diemcongxettuyen`
--

LOCK TABLES `xt_diemcongxettuyen` WRITE;
/*!40000 ALTER TABLE `xt_diemcongxettuyen` DISABLE KEYS */;
INSERT INTO `xt_diemcongxettuyen` VALUES (1,'056307010216','7340101','B00','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_B00'),(2,'056307010216','7340101','X22','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_X22'),(3,'056307010216','7340101','B02','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_B02'),(4,'056307010216','7340101','B01','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_B01'),(5,'056307010216','7340101','A07','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_A07'),(6,'056307010216','7340101','A06','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_A06'),(7,'056307010216','7340101','A05','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_A05'),(8,'056307010216','7340101','A04','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_A04'),(9,'056307010216','7340101','A03','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_A03'),(10,'056307010216','7340101','A02','PT4',NULL,NULL,1.00,NULL,'056307010216_7340101_A02');
/*!40000 ALTER TABLE `xt_diemcongxettuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_diemthixettuyen`
--

DROP TABLE IF EXISTS `xt_diemthixettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `xt_diemthixettuyen` (
  `iddiemthi` int(11) NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sobaodanh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `d_phuongthuc` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TO` decimal(8,2) DEFAULT NULL,
  `LI` decimal(8,2) DEFAULT NULL,
  `HO` decimal(8,2) DEFAULT NULL,
  `SI` decimal(8,2) DEFAULT NULL,
  `SU` decimal(8,2) DEFAULT NULL,
  `DI` decimal(8,2) DEFAULT NULL,
  `VA` decimal(8,2) DEFAULT NULL,
  `N1_THI` decimal(8,2) DEFAULT NULL,
  `N1_CC` decimal(8,2) DEFAULT NULL,
  `CNCN` decimal(8,2) DEFAULT NULL,
  `CNNN` decimal(8,2) DEFAULT NULL,
  `TI` decimal(8,2) DEFAULT NULL,
  `KTPL` decimal(8,2) DEFAULT NULL,
  `NL1` decimal(8,2) DEFAULT NULL,
  `NK1` decimal(8,2) DEFAULT NULL,
  `NK2` decimal(8,2) DEFAULT NULL,
  PRIMARY KEY (`iddiemthi`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_diemthixettuyen`
--

LOCK TABLES `xt_diemthixettuyen` WRITE;
/*!40000 ALTER TABLE `xt_diemthixettuyen` DISABLE KEYS */;
INSERT INTO `xt_diemthixettuyen` VALUES (2,'001207000445',NULL,'0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,0.00,0.00,0.00,0.00,0.00,754.00,NULL,NULL),(3,'001207005157',NULL,'0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,0.00,0.00,0.00,0.00,0.00,533.00,NULL,NULL),(4,'001207006913',NULL,'0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,0.00,0.00,0.00,0.00,0.00,747.00,NULL,NULL),(5,'001207008593',NULL,'4',4.38,6.03,0.00,0.00,0.00,0.00,7.59,7.00,7.00,0.00,0.00,0.00,0.00,772.00,NULL,NULL),(6,'001207008830',NULL,'3',5.16,0.00,0.00,0.00,0.00,0.00,7.53,7.18,7.18,0.00,0.00,0.00,0.00,833.00,NULL,NULL),(7,'001207009704',NULL,'0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,9.00,0.00,0.00,0.00,0.00,794.00,NULL,NULL),(8,'001207011459',NULL,'0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,0.00,0.00,0.00,0.00,0.00,743.00,NULL,NULL),(9,'001207012341',NULL,'4',6.07,0.00,0.00,0.00,8.77,9.64,9.31,NULL,0.00,0.00,0.00,0.00,0.00,749.00,NULL,NULL),(10,'001207012439',NULL,'0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,0.00,0.00,0.00,0.00,0.00,960.00,NULL,NULL),(11,'001207012684',NULL,'0',0.00,0.00,0.00,0.00,0.00,0.00,0.00,NULL,0.00,0.00,0.00,0.00,0.00,756.00,NULL,NULL);
/*!40000 ALTER TABLE `xt_diemthixettuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nganh`
--

DROP TABLE IF EXISTS `xt_nganh`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `xt_nganh` (
  `idnganh` int(11) NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tennganh` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_tohopgoc` varchar(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_chitieu` int(11) DEFAULT NULL,
  `n_diemsan` decimal(10,2) DEFAULT NULL,
  `n_diemtrungtuyen` decimal(10,2) DEFAULT NULL,
  `n_tuyenthang` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_dgnl` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_thpt` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `n_vsat` varchar(1) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sl_xtt` int(11) DEFAULT NULL,
  `sl_dgnl` int(11) DEFAULT NULL,
  `sl_vsat` int(11) DEFAULT NULL,
  `sl_thpt` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idnganh`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nganh`
--

LOCK TABLES `xt_nganh` WRITE;
/*!40000 ALTER TABLE `xt_nganh` DISABLE KEYS */;
INSERT INTO `xt_nganh` VALUES (1,'7140114','Quản lý giáo dục','D01',40,17.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(2,'7140201','Giáo dục Mầm non','M01',200,20.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(3,'7140202','Giáo dục Tiểu học','M01',200,21.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(4,'7140205','Giáo dục chính trị','C01',10,23.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(5,'7140209','Sư phạm Toán học','A00',40,24.50,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(6,'7140211','Sư phạm Vật lý','A00',10,24.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(7,'7140212','Sư phạm Hóa học','A00',10,24.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(8,'7140213','Sư phạm Sinh học','B00',10,23.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(9,'7140217','Sư phạm Ngữ văn','C01',50,24.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL),(10,'7140218','Sư phạm Lịch sử','C00',10,25.00,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `xt_nganh` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nganh_tohop`
--

DROP TABLE IF EXISTS `xt_nganh_tohop`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `xt_nganh_tohop` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `manganh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `matohop` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `th_mon1` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hsmon1` tinyint(4) DEFAULT NULL,
  `th_mon2` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hsmon2` tinyint(4) DEFAULT NULL,
  `th_mon3` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hsmon3` tinyint(4) DEFAULT NULL,
  `tb_keys` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `N1` tinyint(1) DEFAULT NULL,
  `TO` tinyint(1) DEFAULT NULL,
  `LI` tinyint(1) DEFAULT NULL,
  `HO` tinyint(1) DEFAULT NULL,
  `SI` tinyint(1) DEFAULT NULL,
  `VA` tinyint(1) DEFAULT NULL,
  `SU` tinyint(1) DEFAULT NULL,
  `DI` tinyint(1) DEFAULT NULL,
  `TI` tinyint(1) DEFAULT NULL,
  `KHAC` tinyint(1) DEFAULT NULL,
  `KTPL` tinyint(1) DEFAULT NULL,
  `dolech` decimal(6,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nganh_tohop`
--

LOCK TABLES `xt_nganh_tohop` WRITE;
/*!40000 ALTER TABLE `xt_nganh_tohop` DISABLE KEYS */;
INSERT INTO `xt_nganh_tohop` VALUES (1,'7140114','B03','TO',3,'VA',3,'SI',1,'7140114_B03',0,1,0,0,1,1,0,0,0,0,0,NULL),(2,'7140114','C01','TO',3,'VA',3,'LI',1,'7140114_C01',0,1,1,0,0,1,0,0,0,0,0,NULL),(3,'7140114','C02','TO',3,'VA',3,'HO',1,'7140114_C02',0,1,0,1,0,1,0,0,0,0,0,NULL),(4,'7140114','C03','TO',3,'VA',3,'SU',1,'7140114_C03',0,1,0,0,0,1,1,0,0,0,0,NULL),(5,'7140114','C04','TO',3,'VA',3,'DI',1,'7140114_C04',0,1,0,0,0,1,0,1,0,0,0,NULL),(6,'7140114','D01','TO',3,'VA',3,'N1',1,'7140114_D01',1,1,0,0,0,1,0,0,0,0,0,NULL),(7,'7140114','X01','TO',3,'VA',3,'KTPL',1,'7140114_X01',0,1,0,0,0,1,0,0,0,0,1,NULL),(8,'7140114','X02','TO',3,'VA',3,'TI',1,'7140114_X02',0,1,0,0,0,1,0,0,1,0,0,NULL),(9,'7140114','X03','TO',3,'VA',3,'CNCN',1,'7140114_X03',0,1,0,0,0,1,0,0,0,1,0,NULL),(10,'7140114','X04','TO',3,'VA',3,'CNNN',1,'7140114_X04',0,1,0,0,0,1,0,0,0,1,0,NULL);
/*!40000 ALTER TABLE `xt_nganh_tohop` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_nguyenvongxettuyen`
--

DROP TABLE IF EXISTS `xt_nguyenvongxettuyen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `xt_nguyenvongxettuyen` (
  `idnv` int(11) NOT NULL AUTO_INCREMENT,
  `nn_cccd` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nv_manganh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nv_tt` int(11) DEFAULT NULL,
  `diem_thxt` decimal(10,5) DEFAULT NULL,
  `diem_utqd` decimal(10,5) DEFAULT NULL,
  `diem_cong` decimal(6,2) DEFAULT NULL,
  `diem_xettuyen` decimal(10,5) DEFAULT NULL,
  `nv_ketqua` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `nv_keys` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tt_phuongthuc` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tt_thm` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idnv`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_nguyenvongxettuyen`
--

LOCK TABLES `xt_nguyenvongxettuyen` WRITE;
/*!40000 ALTER TABLE `xt_nguyenvongxettuyen` DISABLE KEYS */;
INSERT INTO `xt_nguyenvongxettuyen` VALUES (2,'001207004846','7810202',5,16.52000,0.25000,NULL,16.77000,'duisan','001207004846_7810202_PT2','PT2',NULL),(3,'001207005157','7510301',25,15.70000,0.00000,NULL,15.70000,'duisan','001207005157_7510301_PT2','PT2',NULL),(4,'001207005157','7510302',26,15.70000,0.00000,NULL,15.70000,'duisan','001207005157_7510302_PT2','PT2',NULL),(5,'001207005157','7520201',27,15.70000,0.00000,NULL,15.70000,'duisan','001207005157_7520201_PT2','PT2',NULL),(6,'001207006913','7220201',6,19.89000,0.00000,NULL,21.89000,'yes','001207006913_7220201_PT2','PT2',NULL),(7,'001207008593','7310401',4,20.25000,0.00000,NULL,20.25000,'yes','001207008593_7310401_PT2','PT2',NULL),(8,'001207008830','7310401',2,21.18000,0.00000,NULL,21.18000,'yes','001207008830_7310401_PT2','PT2',NULL),(9,'001207009704','7340101',8,20.54000,0.00000,NULL,22.04000,'yes','001207009704_7340101_PT2','PT2',NULL),(10,'001207012341','7380101',1,19.93000,0.00000,NULL,19.93000,'yes','001207012341_7380101_PT2','PT2',NULL),(11,'001207012439','7480201',9,23.83000,0.21000,NULL,24.04000,'yes','001207012439_7480201_PT2','PT2',NULL);
/*!40000 ALTER TABLE `xt_nguyenvongxettuyen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_thisinhxettuyen25`
--

DROP TABLE IF EXISTS `xt_thisinhxettuyen25`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `xt_thisinhxettuyen25` (
  `idthisinh` int(11) NOT NULL AUTO_INCREMENT,
  `cccd` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sobaodanh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ho` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ten` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ngay_sinh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dien_thoai` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gioi_tinh` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `noi_sinh` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` date DEFAULT NULL,
  `doi_tuong` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `khu_vuc` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idthisinh`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_thisinhxettuyen25`
--

LOCK TABLES `xt_thisinhxettuyen25` WRITE;
/*!40000 ALTER TABLE `xt_thisinhxettuyen25` DISABLE KEYS */;
/*!40000 ALTER TABLE `xt_thisinhxettuyen25` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xt_tohop_monthi`
--

DROP TABLE IF EXISTS `xt_tohop_monthi`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `xt_tohop_monthi` (
  `idtohop` int(11) NOT NULL AUTO_INCREMENT,
  `matohop` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mon1` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mon2` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mon3` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tentohop` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`idtohop`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xt_tohop_monthi`
--

LOCK TABLES `xt_tohop_monthi` WRITE;
/*!40000 ALTER TABLE `xt_tohop_monthi` DISABLE KEYS */;
INSERT INTO `xt_tohop_monthi` VALUES (2,'A01','TO','LI','N1','Toán, Vật lí, Tiếng Anh'),(5,'B00','TO','HO','SI','Toán, Hóa học, Sinh học'),(6,'C00','VA','SU','DI','Ngữ văn, Lịch sử, Địa lí'),(7,'C03','TO','VA','SU','Toán, Lịch sử, Ngữ văn'),(8,'C04','TO','VA','DI','Toán, Địa lí, Ngữ văn'),(9,'D19','VA','SU','GD','Văn - Sử - GDCD'),(10,'D01','TO','VA','N1','Toán, Tiếng Anh, Ngữ văn'),(11,'H00','VA','NK3','NK4','Ngữ văn, Hình họa, Trang trí'),(12,'M01','VA','NK1','NK2','Ngữ văn, Kể chuyện - Đọc diễn cảm, Hát - Nhạc'),(13,'M02','TO','NK1','NK2','Toán, Kể chuyện - Đọc diễn cảm, Hát - Nhạc'),(14,'N01','VA','NK5','NK6','Ngữ văn, Hát - Nhạc cụ, Xướng âm - Thẩm âm, Tiết tấu');
/*!40000 ALTER TABLE `xt_tohop_monthi` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-09 15:57:46
