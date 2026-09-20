
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `api_categoria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_categoria` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_categoria` WRITE;
/*!40000 ALTER TABLE `api_categoria` DISABLE KEYS */;
INSERT INTO `api_categoria` VALUES (5,'Alimentos'),(6,'Accesorios');
/*!40000 ALTER TABLE `api_categoria` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_consulta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_consulta` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(254) NOT NULL,
  `mensaje` longtext NOT NULL,
  `leida` tinyint(1) NOT NULL,
  `creado_en` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_consulta` WRITE;
/*!40000 ALTER TABLE `api_consulta` DISABLE KEYS */;
INSERT INTO `api_consulta` VALUES (5,'Laura Su├írez','interesado@mail.test','Quisiera saber si atienden animales ex├│ticos los fines de semana.',0,'2026-09-18 21:39:19.090958');
/*!40000 ALTER TABLE `api_consulta` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_mascota`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_mascota` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `especie` varchar(20) NOT NULL,
  `raza` varchar(50) NOT NULL,
  `peso` decimal(5,2) NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  `creado_en` datetime(6) NOT NULL,
  `actualizado_en` datetime(6) NOT NULL,
  `dueno_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `api_mascota_dueno_id_9473ea3f_fk_api_usuario_id` (`dueno_id`),
  CONSTRAINT `api_mascota_dueno_id_9473ea3f_fk_api_usuario_id` FOREIGN KEY (`dueno_id`) REFERENCES `api_usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_mascota` WRITE;
/*!40000 ALTER TABLE `api_mascota` DISABLE KEYS */;
INSERT INTO `api_mascota` VALUES (10,'Rocco','perro','Labrador',28.50,'2021-10-14','2026-09-18 21:39:19.026257','2026-09-18 21:39:19.026257',11),(11,'M├¡a','gato','Siam├®s',4.20,'2024-04-01','2026-09-18 21:39:19.031704','2026-09-18 21:39:19.031704',11),(12,'Toby','perro','Caniche',7.80,'2023-06-06','2026-09-18 21:39:19.036451','2026-09-18 21:39:19.036451',12);
/*!40000 ALTER TABLE `api_mascota` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_producto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_producto` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` longtext NOT NULL,
  `precio` decimal(10,2) NOT NULL,
  `stock` int unsigned NOT NULL,
  `imagen` varchar(200) NOT NULL,
  `categoria_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `api_producto_categoria_id_c2e48405_fk_api_categoria_id` (`categoria_id`),
  CONSTRAINT `api_producto_categoria_id_c2e48405_fk_api_categoria_id` FOREIGN KEY (`categoria_id`) REFERENCES `api_categoria` (`id`),
  CONSTRAINT `api_producto_chk_1` CHECK ((`stock` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_producto` WRITE;
/*!40000 ALTER TABLE `api_producto` DISABLE KEYS */;
INSERT INTO `api_producto` VALUES (5,'Alimento balanceado adulto 15 kg','Alimento seco para perros adultos.',24500.00,30,'',5),(6,'Collar reflectivo','Collar regulable con banda reflectiva.',6800.00,45,'',6);
/*!40000 ALTER TABLE `api_producto` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_servicio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_servicio` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` longtext NOT NULL,
  `duracion_minutos` int unsigned NOT NULL,
  `activo` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`),
  CONSTRAINT `api_servicio_chk_1` CHECK ((`duracion_minutos` >= 0))
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_servicio` WRITE;
/*!40000 ALTER TABLE `api_servicio` DISABLE KEYS */;
INSERT INTO `api_servicio` VALUES (11,'Consulta general','Revisi├│n cl├¡nica general.',30,1),(12,'Vacunaci├│n','Aplicaci├│n de vacunas del plan sanitario.',20,1),(13,'Ba├▒o y peluquer├¡a','Higiene y corte.',60,1),(14,'Control post operatorio','Seguimiento tras una cirug├¡a.',30,1);
/*!40000 ALTER TABLE `api_servicio` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_turno`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_turno` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha` datetime(6) NOT NULL,
  `estado` varchar(15) NOT NULL,
  `observaciones` longtext NOT NULL,
  `creado_en` datetime(6) NOT NULL,
  `mascota_id` bigint NOT NULL,
  `servicio_id` bigint NOT NULL,
  `veterinario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `api_turno_mascota_id_24462537_fk_api_mascota_id` (`mascota_id`),
  KEY `api_turno_servicio_id_b7550be4_fk_api_servicio_id` (`servicio_id`),
  KEY `api_turno_veterinario_id_fd823114_fk_api_usuario_id` (`veterinario_id`),
  CONSTRAINT `api_turno_mascota_id_24462537_fk_api_mascota_id` FOREIGN KEY (`mascota_id`) REFERENCES `api_mascota` (`id`),
  CONSTRAINT `api_turno_servicio_id_b7550be4_fk_api_servicio_id` FOREIGN KEY (`servicio_id`) REFERENCES `api_servicio` (`id`),
  CONSTRAINT `api_turno_veterinario_id_fd823114_fk_api_usuario_id` FOREIGN KEY (`veterinario_id`) REFERENCES `api_usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_turno` WRITE;
/*!40000 ALTER TABLE `api_turno` DISABLE KEYS */;
INSERT INTO `api_turno` VALUES (11,'2026-09-24 00:39:15.112823','cancelado','','2026-09-18 21:39:19.066441',10,11,NULL),(12,'2026-09-30 21:39:15.112823','confirmado','','2026-09-18 21:39:19.072458',10,13,10),(13,'2026-10-08 21:39:15.112823','confirmado','','2026-09-18 21:39:19.077766',11,12,10),(14,'2026-09-03 21:39:15.112823','completado','Control de rutina sin novedades.','2026-09-18 21:39:19.082468',11,11,10),(15,'2026-09-26 21:39:15.112823','cancelado','','2026-09-18 21:39:19.087257',12,11,NULL);
/*!40000 ALTER TABLE `api_turno` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_usuario` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `password` varchar(128) NOT NULL,
  `last_login` datetime(6) DEFAULT NULL,
  `is_superuser` tinyint(1) NOT NULL,
  `email` varchar(254) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(20) NOT NULL,
  `direccion` varchar(150) NOT NULL,
  `rol` varchar(15) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `is_staff` tinyint(1) NOT NULL,
  `date_joined` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_usuario` WRITE;
/*!40000 ALTER TABLE `api_usuario` DISABLE KEYS */;
INSERT INTO `api_usuario` VALUES (1,'pbkdf2_sha256$1200000$wylVeaRPPLodR9a22etQ4y$ndfVdfPreXt28sdvCevRB0Bfe/xDWQY14jDqIuQJ7yc=','2026-09-18 21:39:41.184315',1,'admin@petshop.test','Administrador','3510000000','','admin',1,1,'2026-09-18 18:19:01.981995'),(10,'pbkdf2_sha256$1200000$8RQVMU2Z2SoF2kPjBaU6Iq$Rxqvjz4LBd2LTvc1Teq+fC5legnoad4JxJiEIxbzlUc=','2026-09-18 21:39:40.316894',0,'vet@petshop.test','Dra. G├│mez','3511111111','','veterinario',1,0,'2026-09-18 21:39:16.092850'),(11,'pbkdf2_sha256$1200000$cXBE8ob3IoBSbXA7T7mMOa$ccB7Vz5MC6fxdnfd+ks/nn2A/q7bvoV8AVHkFrXPjps=','2026-09-19 01:18:22.149635',0,'cliente@petshop.test','Juan P├®rez','3512222222','Av. Siempreviva 742','cliente',1,0,'2026-09-18 21:39:17.054347'),(12,'pbkdf2_sha256$1200000$S7SHyhvphm5lTr5Xn1SjfC$fPeyriyr/oqy381Cl9mRSB709XQo172jac6tZKNvs90=',NULL,0,'cliente2@petshop.test','Ana L├│pez','3513333333','','cliente',1,0,'2026-09-18 21:39:18.025269');
/*!40000 ALTER TABLE `api_usuario` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_usuario_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_usuario_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `group_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `api_usuario_groups_usuario_id_group_id_d9500af0_uniq` (`usuario_id`,`group_id`),
  KEY `api_usuario_groups_group_id_a1787217_fk_auth_group_id` (`group_id`),
  CONSTRAINT `api_usuario_groups_group_id_a1787217_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`),
  CONSTRAINT `api_usuario_groups_usuario_id_7c19c78d_fk_api_usuario_id` FOREIGN KEY (`usuario_id`) REFERENCES `api_usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_usuario_groups` WRITE;
/*!40000 ALTER TABLE `api_usuario_groups` DISABLE KEYS */;
/*!40000 ALTER TABLE `api_usuario_groups` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_usuario_user_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_usuario_user_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `usuario_id` bigint NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `api_usuario_user_permiss_usuario_id_permission_id_7f855256_uniq` (`usuario_id`,`permission_id`),
  KEY `api_usuario_user_per_permission_id_0ae209ef_fk_auth_perm` (`permission_id`),
  CONSTRAINT `api_usuario_user_per_permission_id_0ae209ef_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  CONSTRAINT `api_usuario_user_per_usuario_id_598fe587_fk_api_usuar` FOREIGN KEY (`usuario_id`) REFERENCES `api_usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_usuario_user_permissions` WRITE;
/*!40000 ALTER TABLE `api_usuario_user_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `api_usuario_user_permissions` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_vacuna`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_vacuna` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `descripcion` longtext NOT NULL,
  `frecuencia` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_vacuna` WRITE;
/*!40000 ALTER TABLE `api_vacuna` DISABLE KEYS */;
INSERT INTO `api_vacuna` VALUES (10,'Antirr├íbica','Prevenci├│n de la rabia.','anual'),(11,'Qu├¡ntuple','Moquillo, hepatitis, parvovirus, parainfluenza y leptospirosis.','anual'),(12,'Triple felina','Rinotraqueitis, calicivirus y panleucopenia.','anual'),(13,'Antiparasitaria','Desparasitaci├│n interna.','cada 6 meses');
/*!40000 ALTER TABLE `api_vacuna` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `api_vacunacion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `api_vacunacion` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `fecha_aplicacion` date NOT NULL,
  `proxima_dosis` date DEFAULT NULL,
  `creado_en` datetime(6) NOT NULL,
  `mascota_id` bigint NOT NULL,
  `vacuna_id` bigint NOT NULL,
  `veterinario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `api_vacunacion_mascota_id_29cef4a9_fk_api_mascota_id` (`mascota_id`),
  KEY `api_vacunacion_vacuna_id_1a56ae53_fk_api_vacuna_id` (`vacuna_id`),
  KEY `api_vacunacion_veterinario_id_32bb1286_fk_api_usuario_id` (`veterinario_id`),
  CONSTRAINT `api_vacunacion_mascota_id_29cef4a9_fk_api_mascota_id` FOREIGN KEY (`mascota_id`) REFERENCES `api_mascota` (`id`),
  CONSTRAINT `api_vacunacion_vacuna_id_1a56ae53_fk_api_vacuna_id` FOREIGN KEY (`vacuna_id`) REFERENCES `api_vacuna` (`id`),
  CONSTRAINT `api_vacunacion_veterinario_id_32bb1286_fk_api_usuario_id` FOREIGN KEY (`veterinario_id`) REFERENCES `api_usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `api_vacunacion` WRITE;
/*!40000 ALTER TABLE `api_vacunacion` DISABLE KEYS */;
INSERT INTO `api_vacunacion` VALUES (10,'2026-03-22','2027-03-22','2026-09-18 21:39:19.041541',10,10,10),(11,'2025-10-13','2026-10-13','2026-09-18 21:39:19.048428',10,11,10),(12,'2026-06-10','2027-06-10','2026-09-18 21:39:19.055156',11,12,10),(13,'2026-07-20','2027-07-20','2026-09-18 21:39:19.061055',12,10,10),(14,'2026-09-01','2027-09-01','2026-09-18 21:39:33.026944',11,13,10);
/*!40000 ALTER TABLE `api_vacunacion` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `auth_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `auth_group` WRITE;
/*!40000 ALTER TABLE `auth_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_group` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `auth_group_permissions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_group_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` int NOT NULL,
  `permission_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_group_permissions_group_id_permission_id_0cd325b0_uniq` (`group_id`,`permission_id`),
  KEY `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` (`permission_id`),
  CONSTRAINT `auth_group_permissio_permission_id_84c5c92e_fk_auth_perm` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`),
  CONSTRAINT `auth_group_permissions_group_id_b120cbf9_fk_auth_group_id` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `auth_group_permissions` WRITE;
/*!40000 ALTER TABLE `auth_group_permissions` DISABLE KEYS */;
/*!40000 ALTER TABLE `auth_group_permissions` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `auth_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auth_permission` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `content_type_id` int NOT NULL,
  `codename` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `auth_permission_content_type_id_codename_01ab375a_uniq` (`content_type_id`,`codename`),
  CONSTRAINT `auth_permission_content_type_id_2f476e4b_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `auth_permission` WRITE;
/*!40000 ALTER TABLE `auth_permission` DISABLE KEYS */;
INSERT INTO `auth_permission` VALUES (1,'Can add log entry',1,'add_logentry'),(2,'Can change log entry',1,'change_logentry'),(3,'Can delete log entry',1,'delete_logentry'),(4,'Can view log entry',1,'view_logentry'),(5,'Can add permission',3,'add_permission'),(6,'Can change permission',3,'change_permission'),(7,'Can delete permission',3,'delete_permission'),(8,'Can view permission',3,'view_permission'),(9,'Can add group',2,'add_group'),(10,'Can change group',2,'change_group'),(11,'Can delete group',2,'delete_group'),(12,'Can view group',2,'view_group'),(13,'Can add content type',4,'add_contenttype'),(14,'Can change content type',4,'change_contenttype'),(15,'Can delete content type',4,'delete_contenttype'),(16,'Can view content type',4,'view_contenttype'),(17,'Can add session',5,'add_session'),(18,'Can change session',5,'change_session'),(19,'Can delete session',5,'delete_session'),(20,'Can view session',5,'view_session'),(21,'Can add Blacklisted Token',6,'add_blacklistedtoken'),(22,'Can change Blacklisted Token',6,'change_blacklistedtoken'),(23,'Can delete Blacklisted Token',6,'delete_blacklistedtoken'),(24,'Can view Blacklisted Token',6,'view_blacklistedtoken'),(25,'Can add Outstanding Token',7,'add_outstandingtoken'),(26,'Can change Outstanding Token',7,'change_outstandingtoken'),(27,'Can delete Outstanding Token',7,'delete_outstandingtoken'),(28,'Can view Outstanding Token',7,'view_outstandingtoken'),(29,'Can add categor├¡a',8,'add_categoria'),(30,'Can change categor├¡a',8,'change_categoria'),(31,'Can delete categor├¡a',8,'delete_categoria'),(32,'Can view categor├¡a',8,'view_categoria'),(33,'Can add consulta',9,'add_consulta'),(34,'Can change consulta',9,'change_consulta'),(35,'Can delete consulta',9,'delete_consulta'),(36,'Can view consulta',9,'view_consulta'),(37,'Can add servicio',12,'add_servicio'),(38,'Can change servicio',12,'change_servicio'),(39,'Can delete servicio',12,'delete_servicio'),(40,'Can view servicio',12,'view_servicio'),(41,'Can add vacuna',15,'add_vacuna'),(42,'Can change vacuna',15,'change_vacuna'),(43,'Can delete vacuna',15,'delete_vacuna'),(44,'Can view vacuna',15,'view_vacuna'),(45,'Can add usuario',14,'add_usuario'),(46,'Can change usuario',14,'change_usuario'),(47,'Can delete usuario',14,'delete_usuario'),(48,'Can view usuario',14,'view_usuario'),(49,'Can add mascota',10,'add_mascota'),(50,'Can change mascota',10,'change_mascota'),(51,'Can delete mascota',10,'delete_mascota'),(52,'Can view mascota',10,'view_mascota'),(53,'Can add producto',11,'add_producto'),(54,'Can change producto',11,'change_producto'),(55,'Can delete producto',11,'delete_producto'),(56,'Can view producto',11,'view_producto'),(57,'Can add turno',13,'add_turno'),(58,'Can change turno',13,'change_turno'),(59,'Can delete turno',13,'delete_turno'),(60,'Can view turno',13,'view_turno'),(61,'Can add vacunaci├│n',16,'add_vacunacion'),(62,'Can change vacunaci├│n',16,'change_vacunacion'),(63,'Can delete vacunaci├│n',16,'delete_vacunacion'),(64,'Can view vacunaci├│n',16,'view_vacunacion');
/*!40000 ALTER TABLE `auth_permission` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `django_admin_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_admin_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `action_time` datetime(6) NOT NULL,
  `object_id` longtext,
  `object_repr` varchar(200) NOT NULL,
  `action_flag` smallint unsigned NOT NULL,
  `change_message` longtext NOT NULL,
  `content_type_id` int DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `django_admin_log_content_type_id_c4bce8eb_fk_django_co` (`content_type_id`),
  KEY `django_admin_log_user_id_c564eba6_fk_api_usuario_id` (`user_id`),
  CONSTRAINT `django_admin_log_content_type_id_c4bce8eb_fk_django_co` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`),
  CONSTRAINT `django_admin_log_user_id_c564eba6_fk_api_usuario_id` FOREIGN KEY (`user_id`) REFERENCES `api_usuario` (`id`),
  CONSTRAINT `django_admin_log_chk_1` CHECK ((`action_flag` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `django_admin_log` WRITE;
/*!40000 ALTER TABLE `django_admin_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `django_admin_log` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `django_content_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_content_type` (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_label` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `django_content_type_app_label_model_76bd3d3b_uniq` (`app_label`,`model`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `django_content_type` WRITE;
/*!40000 ALTER TABLE `django_content_type` DISABLE KEYS */;
INSERT INTO `django_content_type` VALUES (1,'admin','logentry'),(8,'api','categoria'),(9,'api','consulta'),(10,'api','mascota'),(11,'api','producto'),(12,'api','servicio'),(13,'api','turno'),(14,'api','usuario'),(15,'api','vacuna'),(16,'api','vacunacion'),(2,'auth','group'),(3,'auth','permission'),(4,'contenttypes','contenttype'),(5,'sessions','session'),(6,'token_blacklist','blacklistedtoken'),(7,'token_blacklist','outstandingtoken');
/*!40000 ALTER TABLE `django_content_type` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `django_migrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_migrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `applied` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `django_migrations` WRITE;
/*!40000 ALTER TABLE `django_migrations` DISABLE KEYS */;
INSERT INTO `django_migrations` VALUES (1,'contenttypes','0001_initial','2026-09-18 18:18:41.370966'),(2,'contenttypes','0002_remove_content_type_name','2026-09-18 18:18:41.526601'),(3,'auth','0001_initial','2026-09-18 18:18:42.033225'),(4,'auth','0002_alter_permission_name_max_length','2026-09-18 18:18:42.186444'),(5,'auth','0003_alter_user_email_max_length','2026-09-18 18:18:42.209410'),(6,'auth','0004_alter_user_username_opts','2026-09-18 18:18:42.233005'),(7,'auth','0005_alter_user_last_login_null','2026-09-18 18:18:42.250293'),(8,'auth','0006_require_contenttypes_0002','2026-09-18 18:18:42.259429'),(9,'auth','0007_alter_validators_add_error_messages','2026-09-18 18:18:42.272984'),(10,'auth','0008_alter_user_username_max_length','2026-09-18 18:18:42.286271'),(11,'auth','0009_alter_user_last_name_max_length','2026-09-18 18:18:42.301536'),(12,'auth','0010_alter_group_name_max_length','2026-09-18 18:18:42.334201'),(13,'auth','0011_update_proxy_permissions','2026-09-18 18:18:42.352039'),(14,'auth','0012_alter_user_first_name_max_length','2026-09-18 18:18:42.364487'),(15,'api','0001_initial','2026-09-18 18:18:44.754046'),(16,'admin','0001_initial','2026-09-18 18:18:45.146868'),(17,'admin','0002_logentry_remove_auto_add','2026-09-18 18:18:45.169654'),(18,'admin','0003_logentry_add_action_flag_choices','2026-09-18 18:18:45.186454'),(19,'sessions','0001_initial','2026-09-18 18:18:45.281989'),(20,'token_blacklist','0001_initial','2026-09-18 18:18:45.577120'),(21,'token_blacklist','0002_outstandingtoken_jti_hex','2026-09-18 18:18:45.691161'),(22,'token_blacklist','0003_auto_20171017_2007','2026-09-18 18:18:45.726543'),(23,'token_blacklist','0004_auto_20171017_2013','2026-09-18 18:18:45.853962'),(24,'token_blacklist','0005_remove_outstandingtoken_jti','2026-09-18 18:18:45.953681'),(25,'token_blacklist','0006_auto_20171017_2113','2026-09-18 18:18:45.998293'),(26,'token_blacklist','0007_auto_20171017_2214','2026-09-18 18:18:46.451918'),(27,'token_blacklist','0008_migrate_to_bigautofield','2026-09-18 18:18:47.048686'),(28,'token_blacklist','0010_fix_migrate_to_bigautofield','2026-09-18 18:18:47.094742'),(29,'token_blacklist','0011_linearizes_history','2026-09-18 18:18:47.105414'),(30,'token_blacklist','0012_alter_outstandingtoken_user','2026-09-18 18:18:47.138138'),(31,'token_blacklist','0013_alter_blacklistedtoken_options_and_more','2026-09-18 18:18:47.186337');
/*!40000 ALTER TABLE `django_migrations` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `django_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `django_session` (
  `session_key` varchar(40) NOT NULL,
  `session_data` longtext NOT NULL,
  `expire_date` datetime(6) NOT NULL,
  PRIMARY KEY (`session_key`),
  KEY `django_session_expire_date_a5c62663` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `django_session` WRITE;
/*!40000 ALTER TABLE `django_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `django_session` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `token_blacklist_blacklistedtoken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token_blacklist_blacklistedtoken` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blacklisted_at` datetime(6) NOT NULL,
  `token_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_id` (`token_id`),
  CONSTRAINT `token_blacklist_blacklistedtoken_token_id_3cc7fe56_fk` FOREIGN KEY (`token_id`) REFERENCES `token_blacklist_outstandingtoken` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `token_blacklist_blacklistedtoken` WRITE;
/*!40000 ALTER TABLE `token_blacklist_blacklistedtoken` DISABLE KEYS */;
INSERT INTO `token_blacklist_blacklistedtoken` VALUES (1,'2026-09-18 21:03:39.289652',7),(2,'2026-09-18 21:36:36.304006',14);
/*!40000 ALTER TABLE `token_blacklist_blacklistedtoken` ENABLE KEYS */;
UNLOCK TABLES;
DROP TABLE IF EXISTS `token_blacklist_outstandingtoken`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `token_blacklist_outstandingtoken` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` longtext NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `expires_at` datetime(6) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `jti` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `token_blacklist_outstandingtoken_jti_hex_d9bdf6f7_uniq` (`jti`),
  KEY `token_blacklist_outs_user_id_83bc629a_fk_api_usuar` (`user_id`),
  CONSTRAINT `token_blacklist_outs_user_id_83bc629a_fk_api_usuar` FOREIGN KEY (`user_id`) REFERENCES `api_usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

LOCK TABLES `token_blacklist_outstandingtoken` WRITE;
/*!40000 ALTER TABLE `token_blacklist_outstandingtoken` DISABLE KEYS */;
INSERT INTO `token_blacklist_outstandingtoken` VALUES (1,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM2MDQzNywiaWF0IjoxNzg5NzU1NjM3LCJqdGkiOiI0ZDQ4YWY0M2U4MTk0ZWViODRlNWIxMWQ3NWFjYmE3MSIsInVzZXJfaWQiOiIzIn0.oLFcznTou93c6nbdAgdXx27RlN0mMKHkNOM7Usb6qeA','2026-09-18 18:20:37.773930','2026-09-25 18:20:37.000000',NULL,'4d48af43e8194eeb84e5b11d75acba71'),(2,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM2MDU0MCwiaWF0IjoxNzg5NzU1NzQwLCJqdGkiOiI4YTE4YjY3MTM0ZTM0MzM1YmNhNmE2NzlkZDBjOTU5ZCIsInVzZXJfaWQiOiIyIn0.bvwz_HWbKFvHfP5syCk3mTJkZJRAvV3oeGGlaPxTwoc','2026-09-18 18:22:20.694071','2026-09-25 18:22:20.000000',NULL,'8a18b67134e34335bca6a679dd0c959d'),(3,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM2OTU1MywiaWF0IjoxNzg5NzY0NzUzLCJqdGkiOiJhM2NkZjZiNjc3OTQ0MDAwYTEzOWQ0M2M5OTM5MzBkOCIsInVzZXJfaWQiOiIzIn0.9Yf6hlcAqJBPUQ5RL0TyzzKDl2tyyMhv5ZJ8DHQHHpU','2026-09-18 20:52:33.081265','2026-09-25 20:52:33.000000',NULL,'a3cdf6b677944000a139d43c993930d8'),(4,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MDIxNCwiaWF0IjoxNzg5NzY1NDE0LCJqdGkiOiIxZDZlODM3MDMzYTE0MTlhOTE3MTY4ZDJkNzEyNzlhZCIsInVzZXJfaWQiOiIzIn0.O-gw041sFTtbnpyAhQOIbDw6TMnFLgiGQDbTUXijy-4','2026-09-18 21:03:34.467019','2026-09-25 21:03:34.000000',NULL,'1d6e837033a1419a917168d2d71279ad'),(5,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MDIxNSwiaWF0IjoxNzg5NzY1NDE1LCJqdGkiOiJlMjc1MDc2MzRjODY0ZDg5YWE4NDE5OTdhMGM3ZmYxYyIsInVzZXJfaWQiOiIyIn0.oGhssDE1JKEYZV_AuDwmuPW-AkVrkQoQ_HobIHK_2j8','2026-09-18 21:03:35.544591','2026-09-25 21:03:35.000000',NULL,'e27507634c864d89aa841997a0c7ff1c'),(6,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MDIxNywiaWF0IjoxNzg5NzY1NDE3LCJqdGkiOiI3OGU0YmYxNjdlNWU0YjA5ODYwODc0NjNkNWY3ZDI4NCIsInVzZXJfaWQiOiIxIn0.utiDaC3oHNO8JaIwFrxCi_k9vAKQd7XuALa3yBfZUy0','2026-09-18 21:03:37.132759','2026-09-25 21:03:37.000000',1,'78e4bf167e5e4b0986087463d5f7d284'),(7,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MDIxOSwiaWF0IjoxNzg5NzY1NDE5LCJqdGkiOiJlNGY0ODg2ZjQ3OWI0NTQwODlmMWI4MGZhYzZiMGQ2OSIsInVzZXJfaWQiOiIzIn0.3pLqvX7Fipq0FrubQezOGcqIpDcdacfk1-GeZOx3fLo','2026-09-18 21:03:39.248993','2026-09-25 21:03:39.000000',NULL,'e4f4886f479b454089f1b80fac6b0d69'),(8,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MDI0MCwiaWF0IjoxNzg5NzY1NDQwLCJqdGkiOiI4MmIxYWZhZjc4ZDE0ODFjODFhNGNiNjRmNTU5MDY5YyIsInVzZXJfaWQiOiIzIn0.6N9NN2D58K_7XYSOmPesJuM6kgp7158e1emXn2kY-UE','2026-09-18 21:04:00.251795','2026-09-25 21:04:00.000000',NULL,'82b1afaf78d1481c81a4cb64f559069c'),(9,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MDI0MSwiaWF0IjoxNzg5NzY1NDQxLCJqdGkiOiIyZjI4ZDA5NDI2NTk0MjUyYTE1YWI0ZjFhZTIwYmQ3NSIsInVzZXJfaWQiOiIxIn0.YVD7P47_AfIanErGnQ4ycmMoEVn4-lGjmvKyi7_p2LI','2026-09-18 21:04:01.259006','2026-09-25 21:04:01.000000',1,'2f28d09426594252a15ab4f1ae20bd75'),(10,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MDI0MiwiaWF0IjoxNzg5NzY1NDQyLCJqdGkiOiJiOTUzNzk0MjNhMjA0OTA1OTFmMzg1MmQ2OWJhYjQzNCIsInVzZXJfaWQiOiIyIn0.pfAn6FLqrIvsvfHiSZBp2cJ0LrV0kSD1Nl0lg_-3Glg','2026-09-18 21:04:02.234396','2026-09-25 21:04:02.000000',NULL,'b95379423a20490591f3852d69bab434'),(11,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjE5MSwiaWF0IjoxNzg5NzY3MzkxLCJqdGkiOiIwYjVhNWFhNjY3Y2U0ZjQ3YTYyYThjMTNkZWI3ZjcyMSIsInVzZXJfaWQiOiI3In0.skbwMyuXqtrQYQbRPT9Ai5bRnowldTMUpl1Anj2_GH4','2026-09-18 21:36:31.874619','2026-09-25 21:36:31.000000',NULL,'0b5a5aa667ce4f47a62a8c13deb7f721'),(12,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjE5MywiaWF0IjoxNzg5NzY3MzkzLCJqdGkiOiJmNzgzODQyZDJkYjE0MzkxOGQxZGI4MjllYmM3YmI4ZSIsInVzZXJfaWQiOiI2In0.6EA5IQk9u4xxYGeL0mi2OgCVotDWdGTiwlRFJ3c1TDE','2026-09-18 21:36:33.004114','2026-09-25 21:36:33.000000',NULL,'f783842d2db143918d1db829ebc7bb8e'),(13,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjE5NCwiaWF0IjoxNzg5NzY3Mzk0LCJqdGkiOiJjZDdhNTc2NmI2ZGI0MzZlYWIyOTAwYTY0YjYxNzUzOCIsInVzZXJfaWQiOiIxIn0.XOqdryjomAdl8O5nmA99mBkkomj415eP_47LJTGoJag','2026-09-18 21:36:34.036560','2026-09-25 21:36:34.000000',1,'cd7a5766b6db436eab2900a64b617538'),(14,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjE5NiwiaWF0IjoxNzg5NzY3Mzk2LCJqdGkiOiIwYjlmNWJhZjdlZTA0NDI0OTU1ZTkzNjc3MDVjYWVmNyIsInVzZXJfaWQiOiI3In0.u7fJ38bsVWqzWJnw8h0h7hnI40611q-w0nYnQ97cS4Q','2026-09-18 21:36:36.258201','2026-09-25 21:36:36.000000',NULL,'0b9f5baf7ee04424955e9367705caef7'),(15,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjIwMiwiaWF0IjoxNzg5NzY3NDAyLCJqdGkiOiJmMjRlMGNhOWQ2NTM0YjcxOWY5YzliYjNhOWExYzU1NCIsInVzZXJfaWQiOiI3In0.IwLiOhXozgAenh8I8NpRtUrVAYFrAbXMpbuPo_YYsoM','2026-09-18 21:36:42.292818','2026-09-25 21:36:42.000000',NULL,'f24e0ca9d6534b719f9c9bb3a9a1c554'),(16,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjIwMywiaWF0IjoxNzg5NzY3NDAzLCJqdGkiOiIyYzY0ZGYxZDZjZTI0MDkwYTQxYzVjODRkZWI5OTVmMyIsInVzZXJfaWQiOiIxIn0.MAKCvqlk_0TLLg1sFVviJvzf3WaEv23PTocrlr5LawU','2026-09-18 21:36:43.306845','2026-09-25 21:36:43.000000',1,'2c64df1d6ce24090a41c5c84deb995f3'),(17,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjIwNCwiaWF0IjoxNzg5NzY3NDA0LCJqdGkiOiIwOGVhNmM4NWE5YzI0NTRhODA0N2MxOWY0NjUzY2VhZCIsInVzZXJfaWQiOiI2In0.mdSQTQ0aIgckHmNgrUU6bO8uXbkWeDyLqr5EhO0C9PM','2026-09-18 21:36:44.281516','2026-09-25 21:36:44.000000',NULL,'08ea6c85a9c2454a8047c19f4653cead'),(18,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjM3MSwiaWF0IjoxNzg5NzY3NTcxLCJqdGkiOiJhN2M4YTVkMGRjZjE0OTZjYjU3ZTcxYzNhYzdlY2Q0YiIsInVzZXJfaWQiOiIxMSJ9.hwAr30q1e2YWex7XuKq2uhhChIVhgz3UJK4mZ3wvMoQ','2026-09-18 21:39:31.181142','2026-09-25 21:39:31.000000',11,'a7c8a5d0dcf1496cb57e71c3ac7ecd4b'),(19,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjM3MiwiaWF0IjoxNzg5NzY3NTcyLCJqdGkiOiI4NzFlYWQwMGJhN2Y0YTMyYjBlZjJmZjBkZmI1MDEwYyIsInVzZXJfaWQiOiIxIn0.3sbN04CXp6fS_cUJTZ4MewQb50E7UUM-xLuK58NZozY','2026-09-18 21:39:32.105975','2026-09-25 21:39:32.000000',1,'871ead00ba7f4a32b0ef2ff0dfb5010c'),(20,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjM3MiwiaWF0IjoxNzg5NzY3NTcyLCJqdGkiOiIzNDk4NWVlOWJjNWM0YmJhODQyYWM3NjAxN2Y0ZjA0MiIsInVzZXJfaWQiOiIxMCJ9.n_er622lHx3BGY15pz7MbV4LjkUkHlqcpuGh5kpDL2Y','2026-09-18 21:39:32.994254','2026-09-25 21:39:32.000000',10,'34985ee9bc5c4bba842ac76017f4f042'),(21,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjM3OSwiaWF0IjoxNzg5NzY3NTc5LCJqdGkiOiJmNGRmMzBhNjA4N2U0Nzk3OGU2MTg2YzNhOWZiMTBjYSIsInVzZXJfaWQiOiIxMSJ9.mC60P8hOe3QMfw_p81gH-Kt-vDpDJToP-ip1L1dLFr4','2026-09-18 21:39:39.383533','2026-09-25 21:39:39.000000',11,'f4df30a6087e47978e6186c3a9fb10ca'),(22,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjM4MCwiaWF0IjoxNzg5NzY3NTgwLCJqdGkiOiI5N2ZiMTAyZjcxMzA0NjM3YTUwNGFiY2M3MmUyODM5YyIsInVzZXJfaWQiOiIxMCJ9.F648O7izKLDECnapYb3f82VD3S5DINt2q_3VWawNl_o','2026-09-18 21:39:40.290333','2026-09-25 21:39:40.000000',10,'97fb102f71304637a504abcc72e2839c'),(23,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM3MjM4MSwiaWF0IjoxNzg5NzY3NTgxLCJqdGkiOiJkNjlmNzdkZGRjNzY0MDRhYThiYzEzNGM4NTUxMDhlNiIsInVzZXJfaWQiOiIxIn0.J-C2WFnvbEkyhjZBmBGTN40OxjPr5o9gnWk-E0-hDdE','2026-09-18 21:39:41.164089','2026-09-25 21:39:41.000000',1,'d69f77dddc76404aa8bc134c855108e6'),(24,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM4NDk1NiwiaWF0IjoxNzg5NzgwMTU2LCJqdGkiOiIyMzBkZDNlNTljNzA0ODVkYWYzNGNhMDJkNjUzN2FkMSIsInVzZXJfaWQiOiIxMSJ9.zoPpOArZM02x1SBs_1A6nJ0mP3J-bK1KEdf0GTpxwIk','2026-09-19 01:09:16.166926','2026-09-26 01:09:16.000000',11,'230dd3e59c70485daf34ca02d6537ad1'),(25,'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoicmVmcmVzaCIsImV4cCI6MTc5MDM4NTUwMiwiaWF0IjoxNzg5NzgwNzAyLCJqdGkiOiIwYTljNTdkMGFkNTQ0Nzc2ODE5YjVlZDkwNTc0MjgxNCIsInVzZXJfaWQiOiIxMSJ9.cxQmJnXbBMbH2xUmxZDGWqAorWdNRJmeGaENX1VC8ok','2026-09-19 01:18:22.126342','2026-09-26 01:18:22.000000',11,'0a9c57d0ad544776819b5ed905742814');
/*!40000 ALTER TABLE `token_blacklist_outstandingtoken` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

