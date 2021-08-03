-- Update repository

# --- !Ups
ALTER TABLE `repository` ADD `local_import` TinyInt(1) DEFAULT 0;

# --- !Downs
ALTER TABLE `repository` DROP `local_import`;