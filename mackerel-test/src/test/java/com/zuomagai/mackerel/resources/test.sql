CREATE TABLE `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


INSERT INTO test.t_user (id, name, age) VALUES(1, 's', 10);
INSERT INTO test.t_user (id, name, age) VALUES(2, 'ss', 11);
INSERT INTO test.t_user (id, name, age) VALUES(3, 'ssy', 12);
