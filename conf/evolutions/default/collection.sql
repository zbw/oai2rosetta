ALTER TABLE  `repository` ADD  `mastercollection` VARCHAR( 255 ) NULL AFTER  `cmsfield`;
ALTER TABLE  `repository` ADD  `collectionxpath` VARCHAR( 255 ) NULL AFTER  `mastercollection`;
ALTER TABLE  `repository` ADD  `completecollectionpath` TINYINT( 1 ) NULL DEFAULT  '0' AFTER  `collectionxpath`