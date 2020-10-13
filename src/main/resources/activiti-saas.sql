/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.4.26
 Source Server Type    : MySQL
 Source Server Version : 50644
 Source Host           : 192.168.4.26:3306
 Source Schema         : zhfw-activiti-saas

 Target Server Type    : MySQL
 Target Server Version : 50644
 File Encoding         : 65001

 Date: 09/01/2020 17:32:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ACT_EVT_LOG
-- ----------------------------
DROP TABLE IF EXISTS `ACT_EVT_LOG`;
CREATE TABLE `ACT_EVT_LOG`  (
  `LOG_NR_` bigint(20) NOT NULL AUTO_INCREMENT,
  `TYPE_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TIME_STAMP_` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `USER_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DATA_` longblob,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `LOCK_TIME_` timestamp(3) ,
  `IS_PROCESSED_` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`LOG_NR_`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_GE_BYTEARRAY
-- ----------------------------
DROP TABLE IF EXISTS `ACT_GE_BYTEARRAY`;
CREATE TABLE `ACT_GE_BYTEARRAY`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DEPLOYMENT_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `BYTES_` longblob,
  `GENERATED_` tinyint(4)  ,
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_FK_BYTEARR_DEPL`(`DEPLOYMENT_ID_`) USING BTREE,
  CONSTRAINT `ACT_GE_BYTEARRAY_ibfk_1` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `ACT_RE_DEPLOYMENT` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_GE_PROPERTY
-- ----------------------------
DROP TABLE IF EXISTS `ACT_GE_PROPERTY`;
CREATE TABLE `ACT_GE_PROPERTY`  (
  `NAME_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `VALUE_` varchar(300) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `REV_` int(11)  ,
  PRIMARY KEY (`NAME_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_HI_ACTINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_ACTINST`;
CREATE TABLE `ACT_HI_ACTINST`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ACT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CALL_PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `ACT_NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `ACT_TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `ASSIGNEE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `START_TIME_` datetime(3)  ,
  `END_TIME_` datetime(3)  ,
  `DURATION_` bigint(20)  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_ACT_INST_START`(`START_TIME_`) USING BTREE,
  INDEX `ACT_IDX_HI_ACT_INST_END`(`END_TIME_`) USING BTREE,
  INDEX `ACT_IDX_HI_ACT_INST_PROCINST`(`PROC_INST_ID_`, `ACT_ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_ACT_INST_EXEC`(`EXECUTION_ID_`, `ACT_ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_HI_ATTACHMENT
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_ATTACHMENT`;
CREATE TABLE `ACT_HI_ATTACHMENT`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `USER_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DESCRIPTION_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `URL_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CONTENT_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TIME_` datetime(3)  ,
  PRIMARY KEY (`ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_HI_COMMENT
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_COMMENT`;
CREATE TABLE `ACT_HI_COMMENT`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TIME_` datetime(3)  ,
  `USER_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `ACTION_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `MESSAGE_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `FULL_MSG_` longblob,
  PRIMARY KEY (`ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_HI_DETAIL
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_DETAIL`;
CREATE TABLE `ACT_HI_DETAIL`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `ACT_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `VAR_TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `REV_` int(11)  ,
  `TIME_` datetime(3)  ,
  `BYTEARRAY_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DOUBLE_` double  ,
  `LONG_` bigint(20)  ,
  `TEXT_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TEXT2_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_DETAIL_PROC_INST`(`PROC_INST_ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_DETAIL_ACT_INST`(`ACT_INST_ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_DETAIL_TIME`(`TIME_`) USING BTREE,
  INDEX `ACT_IDX_HI_DETAIL_NAME`(`NAME_`) USING BTREE,
  INDEX `ACT_IDX_HI_DETAIL_TASK_ID`(`TASK_ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_HI_IDENTITYLINK
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_IDENTITYLINK`;
CREATE TABLE `ACT_HI_IDENTITYLINK`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `GROUP_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `USER_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_IDENT_LNK_USER`(`USER_ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_IDENT_LNK_TASK`(`TASK_ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_IDENT_LNK_PROCINST`(`PROC_INST_ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_HI_PROCINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_PROCINST`;
CREATE TABLE `ACT_HI_PROCINST`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `BUSINESS_KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `START_TIME_` datetime(3)  ,
  `END_TIME_` datetime(3)  ,
  `DURATION_` bigint(20)  ,
  `START_USER_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `START_ACT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `END_ACT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `SUPER_PROCESS_INSTANCE_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DELETE_REASON_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE,
  UNIQUE INDEX `PROC_INST_ID_`(`PROC_INST_ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_PRO_INST_END`(`END_TIME_`) USING BTREE,
  INDEX `ACT_IDX_HI_PRO_I_BUSKEY`(`BUSINESS_KEY_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_HI_TASKINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_TASKINST`;
CREATE TABLE `ACT_HI_TASKINST`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_DEF_KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PARENT_TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DESCRIPTION_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `OWNER_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `ASSIGNEE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `START_TIME_` datetime(3)  ,
  `CLAIM_TIME_` datetime(3)  ,
  `END_TIME_` datetime(3)  ,
  `DURATION_` bigint(20)  ,
  `DELETE_REASON_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PRIORITY_` int(11)  ,
  `DUE_DATE_` datetime(3)  ,
  `FORM_KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CATEGORY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_TASK_INST_PROCINST`(`PROC_INST_ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_HI_VARINST
-- ----------------------------
DROP TABLE IF EXISTS `ACT_HI_VARINST`;
CREATE TABLE `ACT_HI_VARINST`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `VAR_TYPE_` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `REV_` int(11)  ,
  `BYTEARRAY_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DOUBLE_` double  ,
  `LONG_` bigint(20)  ,
  `TEXT_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TEXT2_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CREATE_TIME_` datetime(3) ,
  `LAST_UPDATED_TIME_` datetime(3),
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_PROCVAR_PROC_INST`(`PROC_INST_ID_`) USING BTREE,
  INDEX `ACT_IDX_HI_PROCVAR_NAME_TYPE`(`NAME_`, `VAR_TYPE_`) USING BTREE,
  INDEX `ACT_IDX_HI_PROCVAR_TASK_ID`(`TASK_ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_ID_GROUP
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_GROUP`;
CREATE TABLE `ACT_ID_GROUP`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
  `REV_` int(11)  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_ID_INFO
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_INFO`;
CREATE TABLE `ACT_ID_INFO`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
  `REV_` int(11)  ,
  `USER_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TYPE_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `VALUE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PASSWORD_` longblob,
  `PARENT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_ID_MEMBERSHIP
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_MEMBERSHIP`;
CREATE TABLE `ACT_ID_MEMBERSHIP`  (
  `USER_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
  `GROUP_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
  PRIMARY KEY (`USER_ID_`, `GROUP_ID_`) USING BTREE,
  INDEX `ACT_FK_MEMB_GROUP`(`GROUP_ID_`) USING BTREE,
  CONSTRAINT `ACT_ID_MEMBERSHIP_ibfk_1` FOREIGN KEY (`GROUP_ID_`) REFERENCES `ACT_ID_GROUP` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_ID_MEMBERSHIP_ibfk_2` FOREIGN KEY (`USER_ID_`) REFERENCES `ACT_ID_USER` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_ID_USER
-- ----------------------------
DROP TABLE IF EXISTS `ACT_ID_USER`;
CREATE TABLE `ACT_ID_USER`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '',
  `REV_` int(11)  ,
  `FIRST_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `LAST_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EMAIL_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PWD_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PICTURE_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_PROCDEF_INFO
-- ----------------------------
DROP TABLE IF EXISTS `ACT_PROCDEF_INFO`;
CREATE TABLE `ACT_PROCDEF_INFO`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `INFO_JSON_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE,
  UNIQUE INDEX `ACT_UNIQ_INFO_PROCDEF`(`PROC_DEF_ID_`) USING BTREE,
  INDEX `ACT_IDX_INFO_PROCDEF`(`PROC_DEF_ID_`) USING BTREE,
  INDEX `ACT_FK_INFO_JSON_BA`(`INFO_JSON_ID_`) USING BTREE,
  CONSTRAINT `ACT_PROCDEF_INFO_ibfk_1` FOREIGN KEY (`INFO_JSON_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_PROCDEF_INFO_ibfk_2` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RE_DEPLOYMENT
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RE_DEPLOYMENT`;
CREATE TABLE `ACT_RE_DEPLOYMENT`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CATEGORY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
 `DEPLOY_TIME_` timestamp(3) ,
  PRIMARY KEY (`ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RE_MODEL
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RE_MODEL`;
CREATE TABLE `ACT_RE_MODEL`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CATEGORY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CREATE_TIME_` timestamp(3)  ,
  `LAST_UPDATE_TIME_` timestamp(3)  ,
  `VERSION_` int(11)  ,
  `META_INFO_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DEPLOYMENT_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EDITOR_SOURCE_VALUE_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EDITOR_SOURCE_EXTRA_VALUE_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_FK_MODEL_SOURCE`(`EDITOR_SOURCE_VALUE_ID_`) USING BTREE,
  INDEX `ACT_FK_MODEL_SOURCE_EXTRA`(`EDITOR_SOURCE_EXTRA_VALUE_ID_`) USING BTREE,
  INDEX `ACT_FK_MODEL_DEPLOYMENT`(`DEPLOYMENT_ID_`) USING BTREE,
  CONSTRAINT `ACT_RE_MODEL_ibfk_1` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `ACT_RE_DEPLOYMENT` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RE_MODEL_ibfk_2` FOREIGN KEY (`EDITOR_SOURCE_VALUE_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RE_MODEL_ibfk_3` FOREIGN KEY (`EDITOR_SOURCE_EXTRA_VALUE_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RE_PROCDEF
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RE_PROCDEF`;
CREATE TABLE `ACT_RE_PROCDEF`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `CATEGORY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `VERSION_` int(11) NOT NULL,
  `DEPLOYMENT_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `RESOURCE_NAME_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DGRM_RESOURCE_NAME_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DESCRIPTION_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `HAS_START_FORM_KEY_` tinyint(4)  ,
  `HAS_GRAPHICAL_NOTATION_` tinyint(4)  ,
  `SUSPENSION_STATE_` int(11)  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  PRIMARY KEY (`ID_`) USING BTREE,
  UNIQUE INDEX `ACT_UNIQ_PROCDEF`(`KEY_`, `VERSION_`, `TENANT_ID_`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RU_EVENT_SUBSCR
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_EVENT_SUBSCR`;
CREATE TABLE `ACT_RU_EVENT_SUBSCR`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `EVENT_TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `EVENT_NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `ACTIVITY_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CONFIGURATION_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `CREATED_` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_EVENT_SUBSCR_CONFIG_`(`CONFIGURATION_`) USING BTREE,
  INDEX `ACT_FK_EVENT_EXEC`(`EXECUTION_ID_`) USING BTREE,
  CONSTRAINT `ACT_RU_EVENT_SUBSCR_ibfk_1` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RU_EXECUTION
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_EXECUTION`;
CREATE TABLE `ACT_RU_EXECUTION`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `BUSINESS_KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PARENT_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `SUPER_EXEC_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `ACT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `IS_ACTIVE_` tinyint(4)  ,
  `IS_CONCURRENT_` tinyint(4)  ,
  `IS_SCOPE_` tinyint(4)  ,
  `IS_EVENT_SCOPE_` tinyint(4)  ,
  `SUSPENSION_STATE_` int(11)  ,
  `CACHED_ENT_STATE_` int(11)  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `LOCK_TIME_` timestamp(3) ,
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_EXEC_BUSKEY`(`BUSINESS_KEY_`) USING BTREE,
  INDEX `ACT_FK_EXE_PROCINST`(`PROC_INST_ID_`) USING BTREE,
  INDEX `ACT_FK_EXE_PARENT`(`PARENT_ID_`) USING BTREE,
  INDEX `ACT_FK_EXE_SUPER`(`SUPER_EXEC_`) USING BTREE,
  INDEX `ACT_FK_EXE_PROCDEF`(`PROC_DEF_ID_`) USING BTREE,
  CONSTRAINT `ACT_RU_EXECUTION_ibfk_1` FOREIGN KEY (`PARENT_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RU_EXECUTION_ibfk_2` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RU_EXECUTION_ibfk_3` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ACT_RU_EXECUTION_ibfk_4` FOREIGN KEY (`SUPER_EXEC_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RU_IDENTITYLINK
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_IDENTITYLINK`;
CREATE TABLE `ACT_RU_IDENTITYLINK`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `GROUP_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `USER_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_IDENT_LNK_USER`(`USER_ID_`) USING BTREE,
  INDEX `ACT_IDX_IDENT_LNK_GROUP`(`GROUP_ID_`) USING BTREE,
  INDEX `ACT_IDX_ATHRZ_PROCEDEF`(`PROC_DEF_ID_`) USING BTREE,
  INDEX `ACT_FK_TSKASS_TASK`(`TASK_ID_`) USING BTREE,
  INDEX `ACT_FK_IDL_PROCINST`(`PROC_INST_ID_`) USING BTREE,
  CONSTRAINT `ACT_RU_IDENTITYLINK_ibfk_1` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RU_IDENTITYLINK_ibfk_2` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RU_IDENTITYLINK_ibfk_3` FOREIGN KEY (`TASK_ID_`) REFERENCES `ACT_RU_TASK` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RU_JOB
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_JOB`;
CREATE TABLE `ACT_RU_JOB`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `LOCK_EXP_TIME_` timestamp(3)  ,
  `LOCK_OWNER_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EXCLUSIVE_` tinyint(1)  ,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROCESS_INSTANCE_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `RETRIES_` int(11)  ,
  `EXCEPTION_STACK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `EXCEPTION_MSG_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DUEDATE_` timestamp(3)  ,
  `REPEAT_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `HANDLER_TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `HANDLER_CFG_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_FK_JOB_EXCEPTION`(`EXCEPTION_STACK_ID_`) USING BTREE,
  CONSTRAINT `ACT_RU_JOB_ibfk_1` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RU_TASK
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_TASK`;
CREATE TABLE `ACT_RU_TASK`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_DEF_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PARENT_TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DESCRIPTION_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_DEF_KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `OWNER_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `ASSIGNEE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DELEGATION_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PRIORITY_` int(11)  ,
  `CREATE_TIME_` timestamp(3)  ,
  `DUE_DATE_` datetime(3)  ,
  `CATEGORY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `SUSPENSION_STATE_` int(11)  ,
  `TENANT_ID_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '',
  `FORM_KEY_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_TASK_CREATE`(`CREATE_TIME_`) USING BTREE,
  INDEX `ACT_FK_TASK_EXE`(`EXECUTION_ID_`) USING BTREE,
  INDEX `ACT_FK_TASK_PROCINST`(`PROC_INST_ID_`) USING BTREE,
  INDEX `ACT_FK_TASK_PROCDEF`(`PROC_DEF_ID_`) USING BTREE,
  CONSTRAINT `ACT_RU_TASK_ibfk_1` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RU_TASK_ibfk_2` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `ACT_RE_PROCDEF` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RU_TASK_ibfk_3` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for ACT_RU_VARIABLE
-- ----------------------------
DROP TABLE IF EXISTS `ACT_RU_VARIABLE`;
CREATE TABLE `ACT_RU_VARIABLE`  (
  `ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `REV_` int(11)  ,
  `TYPE_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `NAME_` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,
  `EXECUTION_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `PROC_INST_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TASK_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `BYTEARRAY_ID_` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `DOUBLE_` double  ,
  `LONG_` bigint(20)  ,
  `TEXT_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  `TEXT2_` varchar(4000) CHARACTER SET utf8 COLLATE utf8_bin  ,
  PRIMARY KEY (`ID_`) USING BTREE,
  INDEX `ACT_IDX_VARIABLE_TASK_ID`(`TASK_ID_`) USING BTREE,
  INDEX `ACT_FK_VAR_EXE`(`EXECUTION_ID_`) USING BTREE,
  INDEX `ACT_FK_VAR_PROCINST`(`PROC_INST_ID_`) USING BTREE,
  INDEX `ACT_FK_VAR_BYTEARRAY`(`BYTEARRAY_ID_`) USING BTREE,
  CONSTRAINT `ACT_RU_VARIABLE_ibfk_1` FOREIGN KEY (`BYTEARRAY_ID_`) REFERENCES `ACT_GE_BYTEARRAY` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RU_VARIABLE_ibfk_2` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `ACT_RU_VARIABLE_ibfk_3` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `ACT_RU_EXECUTION` (`ID_`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for act_approve_agent
-- ----------------------------
DROP TABLE IF EXISTS `act_approve_agent`;
CREATE TABLE `act_approve_agent`  (
  `id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `agent_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   COMMENT '代理人ID',
  `authorizer_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci   COMMENT '授权人ID',
  `agent_term` int(1)   COMMENT '0：持续代理   1：代理一段时间',
  `agent_strart_time` datetime(0)   ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '代理开始时间',
  `agent_end_time` datetime(0)   ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '代理结束时间',
  `status` int(1)   COMMENT '代理状态（0：开启  1：关闭）',
  `create_time` datetime(0)   ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '创建人',
  `update_time` datetime(0)   ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '最新更新时间',
  `update_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT '' COMMENT '最新更新人',
  `del_flag` int(1)   COMMENT '删除标记',
  `create_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门',
  `tenant_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '租户ID',
  `create_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门IDS',
  `update_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门',
  `update_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门IDS',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for act_approve_file
-- ----------------------------
DROP TABLE IF EXISTS `act_approve_file`;
CREATE TABLE `act_approve_file`  (
  `id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `proc_inst_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '流程实例id',
  `task_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '任务id',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '附件名称',
  `file_address` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '附件下载地址',
  `create_time` datetime(0)   ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '创建人',
  `update_time` datetime(0)   ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '最新更新时间',
  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT '' COMMENT '最新更新人',
  `tenant_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '租户ID',
  `create_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门',
  `create_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门IDS',
  `update_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门',
  `update_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门IDS'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Table structure for t_act_business
-- ----------------------------
DROP TABLE IF EXISTS `t_act_business`;
CREATE TABLE `t_act_business`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `result` int(11)   COMMENT '结果状态    0未提交默认 1处理中 2通过 3驳回 4退回',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '申请标题',
  `status` int(11)   COMMENT '状态 0草稿默认 1处理中 2结束',
  `table_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '关联业务表id',
  `business_type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '流程类型： 1.合同流程  2.制度流程 3.授权流程',
  `proc_def_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '流程定义id',
  `proc_inst_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '流程实例id',
  `user_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建用户id',
  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人',
  `create_time` datetime(0)   COMMENT '创建时间',
  `del_flag` int(11)   COMMENT '删除标记',
  `update_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人',
  `update_time` datetime(0)   COMMENT '更新时间',
  `apply_time` datetime(0)   COMMENT '提交申请时间',
  `create_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门',
  `tenant_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '租户ID',
  `create_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门IDS',
  `update_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门',
  `update_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门IDS',
  `back_flow` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '回退之后，下次提交到哪一个节点，空表示按照流程，不空则表示按照指定节点走',
  `retreat_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '回退人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_act_category
-- ----------------------------
DROP TABLE IF EXISTS `t_act_category`;
CREATE TABLE `t_act_category`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建者',
  `create_time` datetime(0)   COMMENT '创建时间',
  `del_flag` int(11)   COMMENT '删除标志 默认0',
  `update_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新者',
  `update_time` datetime(0)   COMMENT '更新时间',
  `parent_flag` bit(1)   COMMENT '是否为父节点(含子节点) 默认false',
  `parent_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '父id',
  `sort_order` decimal(10, 2)   COMMENT '排序值',
  `status` int(11)   COMMENT '是否启用 0启用 -1禁用',
  `title` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '分类名称',
  `type` int(11)   COMMENT '类型 0分组 1分类',
  `create_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门',
  `tenant_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '租户ID',
  `create_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门IDS',
  `update_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门',
  `update_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门IDS',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_act_model
-- ----------------------------
DROP TABLE IF EXISTS `t_act_model`;
CREATE TABLE `t_act_model`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建者',
  `create_time` datetime(0)   COMMENT '创建时间',
  `del_flag` int(11)   COMMENT '删除标志 默认0',
  `update_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新者',
  `update_time` datetime(0)   COMMENT '更新时间',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '描述/备注',
  `model_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '标识',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '模型名称',
  `version` int(11)   COMMENT '版本',
  `create_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门',
  `tenant_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '租户ID',
  `create_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门IDS',
  `update_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门',
  `update_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门IDS',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_act_node
-- ----------------------------
DROP TABLE IF EXISTS `t_act_node`;
CREATE TABLE `t_act_node`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建者',
  `create_time` datetime(0)   COMMENT '创建时间',
  `del_flag` int(11)   COMMENT '删除标志 默认0',
  `update_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新者',
  `update_time` datetime(0)   COMMENT '更新时间',
  `node_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '节点id',
  `role_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '角色id',
  `type` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '节点关联类型 0角色 1用户',
  `user_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '用户id',
  `relate_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci  ,
  `create_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门',
  `tenant_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '租户ID',
  `create_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门IDS',
  `update_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门',
  `update_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门IDS',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for t_act_process
-- ----------------------------
DROP TABLE IF EXISTS `t_act_process`;
CREATE TABLE `t_act_process`  (
  `id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建者',
  `create_time` datetime(0)   COMMENT '创建时间',
  `del_flag` int(11)   COMMENT '删除标志 默认0',
  `update_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新者',
  `update_time` datetime(0)   COMMENT '更新时间',
  `category_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '所属分类',
  `deployment_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '部署id',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '描述/备注',
  `diagram_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '流程图片名',
  `latest` bit(1)   COMMENT '最新版本',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '流程名称',
  `process_key` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '流程标识名称',
  `status` int(11)   COMMENT '流程状态 部署后默认1激活',
  `version` int(11)   COMMENT '版本',
  `xml_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT 'xml文件名',
  `business_table` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '关联业务表名',
  `route_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '关联前端表单路由名',
  `create_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门',
  `tenant_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '租户ID',
  `create_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '创建人部门IDS',
  `update_department_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门',
  `update_department_ids` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci   COMMENT '更新人部门IDS',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = MyISAM CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
