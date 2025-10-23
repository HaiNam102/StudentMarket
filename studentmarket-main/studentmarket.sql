CREATE DATABASE IF NOT EXISTS `studentmarket`;
USE `studentmarket`;

-- =====================================================
-- CREATE TABLES
-- =====================================================

-- USERS
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `google_id` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `given_name` varchar(255) DEFAULT NULL,
  `family_name` varchar(255) DEFAULT NULL,
  `gender` varchar(20) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `picture` varchar(255) DEFAULT NULL,
  `verified_email` TINYINT DEFAULT '0',
  `provider` varchar(255) DEFAULT NULL,
  `joined_at` date DEFAULT NULL,
  `last_seen_at`  DATETIME DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `google_id` (`google_id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`),
  CONSTRAINT `chk_users_gender` CHECK ((`gender` in (_utf8mb4'MALE',_utf8mb4'FEMALE',_utf8mb4'OTHER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ROLE
CREATE TABLE IF NOT EXISTS `role` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- USER_ROLE
CREATE TABLE IF NOT EXISTS `user_role` (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  `assigned_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `fk_userrole_role` (`role_id`),
  CONSTRAINT `fk_userrole_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_userrole_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- PARENT_CATEGORIES
CREATE TABLE IF NOT EXISTS `parent_categories` (
  `parent_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `icon_path` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- CHILD_CATEGORIES
CREATE TABLE IF NOT EXISTS `child_categories` (
  `child_id` int NOT NULL AUTO_INCREMENT,
  `parent_id` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`child_id`),
  KEY `fk_child_parent` (`parent_id`),
  CONSTRAINT `fk_child_parent` FOREIGN KEY (`parent_id`) REFERENCES `parent_categories` (`parent_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- PRODUCTS
CREATE TABLE IF NOT EXISTS `products` (
  `product_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `chose_user_id` int DEFAULT NULL,
  `parent_id` int DEFAULT NULL,
  `child_id` int DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `description` TEXT DEFAULT NULL,
  `price` decimal(38,2) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expiration_date` datetime DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `is_hot` TINYINT DEFAULT '0',
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`product_id`),
  KEY `fk_product_user` (`user_id`),
  KEY `idx_products_parent` (`parent_id`),
  KEY `idx_products_child` (`child_id`),
  KEY `fk_products_chose_user` (`chose_user_id`),
  CONSTRAINT `fk_product_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_products_child` FOREIGN KEY (`child_id`) REFERENCES `child_categories` (`child_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_products_chose_user` FOREIGN KEY (`chose_user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_products_parent` FOREIGN KEY (`parent_id`) REFERENCES `parent_categories` (`parent_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- PRODUCT_SPECS
CREATE TABLE IF NOT EXISTS `product_specs` (
  `product_id`  BIGINT UNSIGNED NOT NULL,
  `origin`      VARCHAR(100) DEFAULT NULL,
  `material`    VARCHAR(100) DEFAULT NULL,
  `color`       VARCHAR(50)  DEFAULT NULL,
  `accessories` TEXT,
  `updated_at`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`product_id`),
  CONSTRAINT `fk_specs_product`
    FOREIGN KEY (`product_id`) REFERENCES `products`(`product_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- FAVORITES
CREATE TABLE IF NOT EXISTS `favorites` (
  `favorite_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` bigint unsigned NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`favorite_id`),
  UNIQUE KEY `uq_fav_user_product` (`user_id`,`product_id`),
  KEY `fk_fav_product` (`product_id`),
  CONSTRAINT `fk_fav_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- FOLLOWS
CREATE TABLE IF NOT EXISTS `follows` (
  `follower_id` int NOT NULL,
  `followee_id` int NOT NULL,
  `followed_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`follower_id`,`followee_id`),
  KEY `fk_follow_followee` (`followee_id`),
  CONSTRAINT `fk_follow_followee` FOREIGN KEY (`followee_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_follow_follower` FOREIGN KEY (`follower_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- PRODUCT_REGISTRATIONS
CREATE TABLE IF NOT EXISTS `product_registrations` (
  `registration_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `product_id` bigint unsigned NOT NULL,
  `seller_id` int NOT NULL,
  `requester_id` int NOT NULL,
  `intent` enum('BUY','RECEIVE') NOT NULL,
  `note` varchar(500) DEFAULT NULL,
  `status` enum('PENDING','ACCEPTED','REJECTED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `chosen_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`registration_id`),
  UNIQUE KEY `uq_pr_one_active` (`product_id`,`requester_id`,`intent`,`status`),
  KEY `fk_pr_requester` (`requester_id`),
  KEY `idx_pr_seller_status` (`seller_id`,`status`,`created_at`),
  KEY `idx_pr_product` (`product_id`),
  CONSTRAINT `fk_pr_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_pr_requester` FOREIGN KEY (`requester_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_pr_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TRANSACTIONS
CREATE TABLE IF NOT EXISTS `transactions` (
  `transaction_id` INT NOT NULL AUTO_INCREMENT,
  `buyer_id` INT DEFAULT NULL,
  `seller_id` INT DEFAULT NULL,
  `status` ENUM('CANCELLED','COMPLETED','IN_PROGRESS','REQUESTING') NOT NULL DEFAULT 'REQUESTING',
  `note` VARCHAR(255) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `completed_at` DATETIME DEFAULT NULL,
  `cancelled_at` DATETIME DEFAULT NULL,
  `total_amount` DECIMAL(12,2) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`),
  KEY `fk_tx_buyer` (`buyer_id`),
  KEY `fk_tx_seller` (`seller_id`),
  KEY `idx_tx_status_created` (`status`, `created_at`),
  CONSTRAINT `fk_tx_buyer` FOREIGN KEY (`buyer_id`)
    REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_tx_seller` FOREIGN KEY (`seller_id`)
    REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- TRANSACTION_DETAILS
CREATE TABLE IF NOT EXISTS `transaction_details` (
  `transaction_id` INT NOT NULL,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `quantity` INT NOT NULL,
  `price_per_unit` DECIMAL(10,2) NOT NULL,
  `subtotal` DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (`transaction_id`, `product_id`),
  KEY `fk_txd_product` (`product_id`),
  CONSTRAINT `fk_txd_product` FOREIGN KEY (`product_id`)
    REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_txd_tx` FOREIGN KEY (`transaction_id`)
    REFERENCES `transactions` (`transaction_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- REPORT
CREATE TABLE IF NOT EXISTS `report` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `reporter_id` int DEFAULT NULL,
  `reported_user_id` int DEFAULT NULL,
  `product_id` bigint unsigned DEFAULT NULL,
  `reason` text,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  KEY `fk_report_reporter` (`reporter_id`),
  KEY `fk_report_reported` (`reported_user_id`),
  KEY `fk_report_product` (`product_id`),
  CONSTRAINT `fk_report_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_report_reported` FOREIGN KEY (`reported_user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- REVIEW
CREATE TABLE IF NOT EXISTS `review` (
  `review_id` INT NOT NULL AUTO_INCREMENT,
  `transaction_id` INT DEFAULT NULL,
  `reviewer_id` INT DEFAULT NULL,
  `reviewee_id` INT DEFAULT NULL,
  `rating` INT NOT NULL,
  `comment` TINYTEXT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`review_id`),
  KEY `fk_review_tx` (`transaction_id`),
  KEY `fk_review_reviewer` (`reviewer_id`),
  KEY `fk_review_reviewee` (`reviewee_id`),
  CONSTRAINT `fk_review_reviewee` FOREIGN KEY (`reviewee_id`)
    REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_review_reviewer` FOREIGN KEY (`reviewer_id`)
    REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_review_tx` FOREIGN KEY (`transaction_id`)
    REFERENCES `transactions` (`transaction_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_rating` CHECK ((`rating` BETWEEN 1 AND 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- REWARDPOINTS
CREATE TABLE IF NOT EXISTS `rewardpoints` (
  `reward_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `total_points` int DEFAULT '0',
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`reward_id`),
  KEY `fk_reward_user` (`user_id`),
  CONSTRAINT `fk_reward_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- LOGIN_HISTORY
CREATE TABLE IF NOT EXISTS `login_history` (
  `log_id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `login_at` datetime DEFAULT NULL,
  `device` varchar(100) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `fk_login_user` (`user_id`),
  CONSTRAINT `fk_login_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- MESSAGE
CREATE TABLE IF NOT EXISTS `message` (
  `message_id` int NOT NULL AUTO_INCREMENT,
  `sender_id` int DEFAULT NULL,
  `recipient_id` int DEFAULT NULL,
  `content` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `sent_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`message_id`),
  KEY `fk_msg_sender` (`sender_id`),
  KEY `fk_msg_receiver` (`recipient_id`),
  CONSTRAINT `fk_msg_receiver` FOREIGN KEY (`recipient_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_msg_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- NOTIFICATIONS
CREATE TABLE IF NOT EXISTS `notifications` (
  `notification_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `actor_user_id` int DEFAULT NULL,
  `product_id` bigint unsigned DEFAULT NULL,
  `registration_id` bigint unsigned DEFAULT NULL,
  `type` enum('NEW_REQUEST','COMPLETED') NOT NULL,
  `title` enum('REG_CREATED','REG_UPDATED','REG_ACCEPTED','REG_REJECTED','REG_CANCELLED') NOT NULL,
  `message` varchar(500) DEFAULT NULL,
  `is_read` TINYINT NOT NULL DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `fk_notif_actor` (`actor_user_id`),
  KEY `fk_notif_product` (`product_id`),
  KEY `fk_notif_reg` (`registration_id`),
  KEY `idx_notif_user_unread` (`user_id`,`is_read`,`created_at`),
  CONSTRAINT `fk_notif_actor` FOREIGN KEY (`actor_user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_notif_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_notif_reg` FOREIGN KEY (`registration_id`) REFERENCES `product_registrations` (`registration_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- SPRING SESSION
CREATE TABLE IF NOT EXISTS SPRING_SESSION (
  PRIMARY_ID CHAR(36) NOT NULL,
  SESSION_ID CHAR(36) NOT NULL,
  CREATION_TIME BIGINT NOT NULL,
  LAST_ACCESS_TIME BIGINT NOT NULL,
  MAX_INACTIVE_INTERVAL INT NOT NULL,
  EXPIRY_TIME BIGINT NOT NULL,
  PRINCIPAL_NAME VARCHAR(100),
  PRIMARY KEY (PRIMARY_ID),
  UNIQUE KEY SPRING_SESSION_IX1 (SESSION_ID),
  KEY SPRING_SESSION_IX2 (EXPIRY_TIME),
  KEY SPRING_SESSION_IX3 (PRINCIPAL_NAME)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- SPRING SESSION ATTRIBUTES
CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (
  SESSION_PRIMARY_ID CHAR(36) NOT NULL,
  ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
  ATTRIBUTE_BYTES BLOB NOT NULL,
  PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
  CONSTRAINT SPRING_SESSION_FK FOREIGN KEY (SESSION_PRIMARY_ID)
    REFERENCES SPRING_SESSION (PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- FEEDBACK
CREATE TABLE IF NOT EXISTS feedback (
  feedback_id     BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id         INT NOT NULL,
  issue_type      ENUM('BUG','FEATURE','UIUX','OTHER') NOT NULL,
  content         TEXT NOT NULL,
  attachment_path VARCHAR(255) DEFAULT NULL,
  status          ENUM('NEW','OPEN','DONE') DEFAULT 'NEW',
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (feedback_id),
  CONSTRAINT fk_fb_user FOREIGN KEY (user_id)
    REFERENCES users(user_id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ADDRESSES
CREATE TABLE IF NOT EXISTS `addresses` (
  `address_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `addr_type` enum('PRODUCT','USER') NOT NULL,
  `product_id` bigint unsigned DEFAULT NULL,
  `user_id` int DEFAULT NULL,
  `province` varchar(100) NOT NULL,
  `ward` varchar(100) NOT NULL,
  `address_detail` varchar(255) NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`address_id`),
  KEY `fk_addr_product` (`product_id`),
  KEY `fk_addr_user` (`user_id`),
  CONSTRAINT `fk_addr_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_addr_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- PRODUCT IMAGES
CREATE TABLE IF NOT EXISTS `product_images` (
  `image_id`   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT UNSIGNED NOT NULL,
  `is_cover`   TINYINT      NOT NULL DEFAULT 0,
  `sort_order` INT             NOT NULL DEFAULT 0,
  `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`image_id`),
  KEY `idx_pi_product` (`product_id`),
  CONSTRAINT `fk_pi_product` FOREIGN KEY (`product_id`)
    REFERENCES `products` (`product_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =====================================================
-- SEED DATA (đúng thứ tự để không lỗi FK)
-- =====================================================

-- 1) Users (tạo admin trước để dùng cho các FK/role)
INSERT INTO `users`
(`user_id`,`google_id`,`email`,`full_name`,`given_name`,`family_name`,`gender`,`date_of_birth`,`phone`,`password`,`username`,`address`,`picture`,`verified_email`,`provider`)
VALUES
(1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'$2a$10$VCu56P7uGSlZzWbbf1BbhOkze8m7pY2WBN/mjWLeFUpnzE23AxJZG','admin',NULL,NULL,0,NULL)
AS new
ON DUPLICATE KEY UPDATE username = new.username;
-- 2) Role
INSERT INTO `role` (`role_id`,`role_name`,`description`) VALUES
(1,'USER','Quyền mặc định cho người dùng thường'),
(2,'ADMIN','Quyền quản trị hệ thống')
AS new(role_id, role_name, description)
ON DUPLICATE KEY UPDATE description = new.description;

-- 3) Gán quyền cho user đã tồn tại (user_id=1)
INSERT INTO `user_role` (`user_id`,`role_id`,`assigned_at`) VALUES
(1,2,'2025-10-02 18:26:06.275215'),
(1,1,NULL)
AS new(user_id, role_id, assigned_at)
ON DUPLICATE KEY UPDATE assigned_at = new.assigned_at;

-- 4) Parent categories
INSERT INTO `parent_categories` (`parent_id`,`name`,`icon_path`,`description`,`created_at`,`updated_at`) VALUES 
(1,'Sách & Tài liệu học tập','/image/header/document.png',NULL,'2025-10-03 08:13:01','2025-10-03 15:38:05'),
(2,'Đồ điện tử & Công nghệ','/image/header/responsive.png',NULL,'2025-10-03 08:13:01','2025-10-03 15:38:05'),
(3,'Đồ dùng học tập','/image/header/stationery.png',NULL,'2025-10-03 08:13:01','2025-10-03 15:38:05'),
(4,'Đồ gia dụng','/image/header/sofa.png',NULL,'2025-10-03 08:13:01','2025-10-03 15:38:05'),
(5,'Thời trang & Phụ kiện','/image/header/laundry.png',NULL,'2025-10-03 08:13:01','2025-10-03 15:38:05'),
(6,'Đồ thể thao & Giải trí','/image/header/sports.png',NULL,'2025-10-03 08:13:01','2025-10-03 15:38:05'),
(7,'Khác','/image/header/other.png',NULL,'2025-10-03 08:13:01','2025-10-03 15:38:05')
AS new(parent_id,name,icon_path,description,created_at,updated_at)
ON DUPLICATE KEY UPDATE name=new.name, icon_path=new.icon_path;

-- 5) Child categories
INSERT INTO `child_categories` (`child_id`,`parent_id`,`name`,`description`,`created_at`,`updated_at`) VALUES 
(1,1,'Giáo trình',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(2,1,'Sách tham khảo',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(3,1,'Sách ngoại ngữ',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(4,1,'Truyện, tiểu thuyết',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(5,2,'Điện thoại & Tablet',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(6,2,'Laptop & PC',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(7,2,'Phụ kiện & Linh kiện',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(8,2,'Thiết bị khác',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(9,3,'Vở & Sổ tay',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(10,3,'Bút & Thước',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(11,3,'Máy tính cầm tay',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(12,3,'Dụng cụ vẽ',NULL,'2025-10-03 08:13:01','2025-10-03 08:13:01'),
(13,4,'Đồ bếp',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(14,4,'Điện gia dụng',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(15,4,'Nội thất',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(16,4,'Đèn học & Đèn ngủ',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(17,4,'Chén, ly, muỗng, đũa',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(18,5,'Quần áo',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(19,5,'Giày dép',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(20,5,'Balo & Túi xách',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(21,5,'Đồng hồ & Trang sức',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(22,6,'Xe đạp & Ván trượt',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(23,6,'Gym & Yoga',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(24,6,'Bóng & Vợt',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(25,6,'Nhạc cụ',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(26,6,'Boardgame & Đồ chơi',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(27,7,'Đồ decor & Trang trí',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(28,7,'Thú cưng',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(29,7,'Vé sự kiện & Vé xe',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(30,7,'Sưu tầm',NULL,'2025-10-05 18:49:19','2025-10-05 18:49:19'),
(31,7,'Phương tiện',NULL,'2025-10-05 18:53:43','2025-10-05 18:53:43')
AS new(child_id,parent_id,name,description,created_at,updated_at)
ON DUPLICATE KEY UPDATE name=new.name, parent_id=new.parent_id;