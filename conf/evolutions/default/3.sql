
# --- !Ups

ALTER TABLE `repository` ADD `bitstreamsearch` VARCHAR(255) NULL DEFAULT NULL;
ALTER TABLE `repository` ADD `bitstreamreplace` VARCHAR(255) NULL DEFAULT NULL;

# --- !Downs

ALTER TABLE `repository` DROP `bitstreamsearch`;
ALTER TABLE `repository` DROP `bitstreamreplace`;