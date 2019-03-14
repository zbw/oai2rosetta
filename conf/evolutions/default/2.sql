
# --- !Ups

ALTER TABLE  record ADD INDEX  `ix_identifier#` (  `identifier` );
ALTER TABLE  record ADD INDEX  `ix_status` (  `status` );

# --- !Downs

drop index ix_identifier# on record;
drop index ix_status on record;