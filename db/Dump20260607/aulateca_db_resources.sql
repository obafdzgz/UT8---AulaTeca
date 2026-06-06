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
-- Table structure for table `resources`
--

DROP TABLE IF EXISTS `resources`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resources` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `type_id` int DEFAULT NULL,
  `status_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `type_id` (`type_id`),
  KEY `status_id` (`status_id`),
  CONSTRAINT `resources_ibfk_1` FOREIGN KEY (`type_id`) REFERENCES `resource_types` (`id`),
  CONSTRAINT `resources_ibfk_2` FOREIGN KEY (`status_id`) REFERENCES `resource_status` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resources`
--

LOCK TABLES `resources` WRITE;
/*!40000 ALTER TABLE `resources` DISABLE KEYS */;
INSERT INTO `resources` VALUES (1,'Aula 101','Aula estándar planta baja. Capacidad para 30 alumnos.',2,1),(2,'Aula 101','Aula estándar planta baja. Capacidad para 30 alumnos.',2,1),(3,'Aula 102','Aula estándar planta baja con pizarra digital interactiva (PDI).',2,1),(4,'Aula 204 (Informática)','Aula de ordenadores de sobremesa. 25 puestos fijos.',2,2),(5,'Aula de Plástica','Espacio adaptado con mesas amplias y fregaderos.',2,1),(6,'Aula de Desdoble A','Aula pequeña para grupos reducidos o apoyo. Capacidad 15 alumnos.',2,1),(7,'Carrito A (Chromebooks)','30 ordenadores Chromebook. Ideal para la ESO.',5,1),(8,'Carrito B (Windows)','25 portátiles Windows 11 para ciclos formativos.',5,1),(9,'Carrito C (Tablets)','20 tablets Android con fundas protectoras para Infantil/Primaria.',5,3),(10,'Carrito D (Mixto)','15 ordenadores antiguos para tareas de ofimática básica.',5,1),(11,'Carrito E (Préstamo)','10 portátiles de altas prestaciones reservados para profesorado.',5,1),(12,'Pabellón Cubierto','Cancha interior principal. Pista de parqué y gradas.',3,1),(13,'Pista Exterior 1 (Fútbol)','Cancha exterior de fútbol sala con césped artificial.',3,2),(14,'Pista Exterior 2 (Baloncesto)','Cancha descubierta con canastas reglamentarias.',3,1),(15,'Gimnasio Interior','Sala diáfana con colchonetas, espalderas y material de psicomotricidad.',3,1),(16,'Pista de Pádel','Única pista de pádel del recinto. Suelo sintético.',3,1),(17,'Laboratorio de Biología','Equipado con microscopios, modelos anatómicos y material de disección.',7,1),(18,'Laboratorio de Química','Dispone de campana extractora, reactivos y material de vidrio.',7,1),(19,'Laboratorio de Física','Mesas electrificadas y kits de mecánica/óptica.',7,3),(20,'Laboratorio de Idiomas','Cabinas insonorizadas con cascos y micrófonos.',7,1),(21,'Taller de Tecnología','Espacio con herramientas de carpintería, soldadura e impresoras 3D.',7,1),(22,'Proyector Portátil EPSON 1','Proyector estándar con conexión HDMI y VGA. Incluye maletín.',6,1),(23,'Proyector Portátil EPSON 2','Proyector estándar con conexión HDMI.',6,2),(24,'Proyector BENQ','Resolución 4K, ideal para proyecciones en el Salón de Actos.',6,1),(25,'Proyector Mini','Proyector de tiro corto, muy pequeño.',6,1),(26,'Proyector de Repuesto','Proyector antiguo usado solo para emergencias si falla el de un aula.',6,1),(27,'Sala de Profesores','Espacio común para el descanso y reuniones informales del claustro.',8,1),(28,'Sala de Visitas','Despacho anexo a dirección para atención a familias.',8,1),(29,'Sala de Juntas','Sala grande con mesa ovalada y pantalla para reuniones de departamento.',8,2),(30,'Sala de Orientación','Espacio privado para tutorías y atención psicológica.',8,1),(31,'Sala del AMPA','Local cedido a la asociación de padres y madres de alumnos.',8,1);
/*!40000 ALTER TABLE `resources` ENABLE KEYS */;
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
