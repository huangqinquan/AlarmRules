/*
Navicat MySQL Data Transfer

Source Server         : 140
Source Server Version : 50621
Source Host           : 172.16.1.140:33060
Source Database       : test_pretreatment

Target Server Type    : MYSQL
Target Server Version : 50621
File Encoding         : 65001

Date: 2017-12-25 09:50:24
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for pre_filter_rules
-- ----------------------------
DROP TABLE IF EXISTS `pre_filter_rules`;
CREATE TABLE `pre_filter_rules` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exp` text COMMENT '过滤规则表达式，&& || !',
  `keyExp` text,
  `enable` tinyint(4) DEFAULT '1' COMMENT '是否生效',
  `expireTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '过期时间',
  `remark` varchar(2000) DEFAULT NULL COMMENT '备注：设置原因',
  `exportCount` bigint(20) DEFAULT '0' COMMENT '已导出数据量',
  `addUserId` bigint(20) DEFAULT NULL,
  `updateUserId` bigint(20) DEFAULT NULL,
  `addTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updateTime` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `nodeid` varchar(100) DEFAULT NULL,
  `opid` varchar(100) DEFAULT NULL,
  `taskid` varchar(100) DEFAULT NULL,
  `opcode` varchar(100) DEFAULT NULL,
  `paraNumber` varchar(100) DEFAULT NULL,
  `optype` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `t_enable` (`enable`)
) ENGINE=InnoDB AUTO_INCREMENT=2984 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of pre_filter_rules
-- ----------------------------
INSERT INTO `pre_filter_rules` VALUES ('1', 'page_action_type:07 && page_type:01', '厦门 || 中国 || 天', '0', null, null, '1003', '1', null, '2017-04-16 22:43:55', '2017-09-28 12:04:43', '110000______', 'wzgz_110000______1492324679337_6677891492324679336', 'wzgz_110000______1492324679337', 'addKeywordMonitor', '2', 'Control');
INSERT INTO `pre_filter_rules` VALUES ('2971', 'TRUE', '楼 && 丑', '1', '2017-12-23 18:20:58', null, '0', '1', null, '2017-12-23 18:20:58', '2017-12-23 18:20:58', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2973', 'TRUE', '喜欢 && 毛线', '1', '2017-12-23 18:21:51', null, '0', '1', null, '2017-12-23 18:21:51', '2017-12-23 18:21:51', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2974', 'TRUE', '晒太阳 && 一点点', '1', '2017-12-23 20:34:41', null, '0', '1', null, '2017-12-23 20:34:41', '2017-12-23 20:34:41', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2975', 'TRUE', '波兰 || 非洲', '1', '2017-12-23 20:44:55', null, '0', '1', null, '2017-12-23 20:44:55', '2017-12-23 20:44:55', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2976', 'TRUE', '我 &&(学区房||道德)', '1', '2017-12-23 21:04:01', null, '0', '1', null, '2017-12-23 21:04:01', '2017-12-23 21:04:01', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2977', 'page_action_type:07', 'TRUE', '1', '2017-12-23 21:08:45', null, '0', '1', null, '2017-12-23 21:08:45', '2017-12-23 21:08:45', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2978', 'userid:禹寺大神', 'TRUE', '1', '2017-12-23 21:20:36', null, '0', '1', null, '2017-12-23 21:20:36', '2017-12-23 21:20:36', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2979', 'topic_id:205625488', 'TRUE', '1', '2017-12-23 21:22:45', null, '0', '1', null, '2017-12-23 21:22:45', '2017-12-23 21:22:45', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2980', 'userid:hotman0000 || userid:智联招聘', '计划生育||世界大战', '1', '2017-12-23 21:23:51', null, '0', '1', null, '2017-12-23 21:23:51', '2017-12-23 21:23:51', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2981', 'site_board_name:百度贴吧-孟州吧 && userid:583169596jj', 'TRUE', '1', '2017-12-23 22:13:38', null, '0', '1', null, '2017-12-23 22:13:38', '2017-12-23 22:13:38', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2982', 'TRUE', '583169596jj', '1', '2017-12-23 22:22:00', null, '0', '1', null, '2017-12-23 22:22:00', '2017-12-23 22:22:00', null, null, null, null, null, null);
INSERT INTO `pre_filter_rules` VALUES ('2983', 'TRUE', '5878659096', '1', '2017-12-23 22:27:30', null, '0', '1', null, '2017-12-23 22:27:30', '2017-12-23 22:27:30', null, null, null, null, null, null);

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `userid` int(11) NOT NULL,
  `ftphost` varchar(12) DEFAULT NULL,
  `ftpdir` text,
  `ftpusername` varchar(20) DEFAULT NULL,
  `ftppassword` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of t_user
-- ----------------------------
INSERT INTO `t_user` VALUES ('1', '172.16.1.140', '/home/ftpadmin', 'ftpadmin', 'my2018');
INSERT INTO `t_user` VALUES ('2', '172.16.1.138', '/home/alarmftp', 'ftpadmin', 'my2018');
