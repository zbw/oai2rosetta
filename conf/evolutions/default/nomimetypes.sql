ALTER TABLE  `repository` ADD  `nomimetypes` VARCHAR( 255 ) NULL AFTER  `oai_mapping`;
ALTER TABLE  `repository` ADD  `metadata_prefix` VARCHAR( 255 ) NOT NULL AFTER  `oai_mapping`     ;
ALTER TABLE  `repository` ADD  `resources_prefix` VARCHAR( 255 ) NOT NULL AFTER  `metadataPrefix`;
ALTER TABLE  `record` CHANGE  `metadata`  `metadata` TEXT NULL DEFAULT NULL ;
ALTER TABLE  `repository` CHANGE  `oai_mapping`  `oai_mapping` TEXT CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL;
ALTER TABLE  `repository` ADD  `cms` VARCHAR( 255 ) NULL DEFAULT NULL AFTER  `resources_prefix`;
ALTER TABLE  `repository` ADD  `cmsfield` VARCHAR( 255 ) NULL DEFAULT NULL AFTER  `cms`;
ALTER TABLE  `repository` CHANGE  `metadata_prefix`  `metadata_prefix` VARCHAR( 255 ) NULL;
ALTER TABLE  `repository` CHANGE  `resources_prefix`  `resources_prefix` VARCHAR( 255 ) NULL;