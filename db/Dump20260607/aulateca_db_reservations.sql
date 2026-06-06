-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: aulateca_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `reservations`
--

DROP TABLE IF EXISTS `reservations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reservations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `resource_id` int NOT NULL,
  `slot_id` int NOT NULL,
  `reservation_date` date NOT NULL,
  `observations` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_reservation` (`resource_id`,`slot_id`,`reservation_date`),
  UNIQUE KEY `UKa1d9acpi66yca4ka71x34j2uu` (`resource_id`,`slot_id`,`reservation_date`),
  KEY `user_id` (`user_id`),
  KEY `slot_id` (`slot_id`),
  CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`),
  CONSTRAINT `reservations_ibfk_3` FOREIGN KEY (`slot_id`) REFERENCES `time_slots` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reservations`
--

LOCK TABLES `reservations` WRITE;
/*!40000 ALTER TABLE `reservations` DISABLE KEYS */;
INSERT INTO `reservations` VALUES (1,6,16,4,'2026-06-25','Nos hacen falta palas y bolas.'),(2,6,22,3,'2026-06-12',''),(18,7,16,1,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(19,7,16,2,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(20,7,16,3,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(21,7,16,5,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(22,7,16,6,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(23,7,16,7,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(24,7,16,8,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(25,7,16,9,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(27,7,16,11,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(28,7,16,12,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(29,7,16,13,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(30,7,16,14,'2026-06-25','Bloqueo por torneo intensivo de Pádel (Prueba SQL)'),(33,8,5,7,'2026-06-18',''),(34,6,16,10,'2026-06-25',''),(35,6,18,10,'2026-06-23','');
/*!40000 ALTER TABLE `reservations` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-07  0:26:10
