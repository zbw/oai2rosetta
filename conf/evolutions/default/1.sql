# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table record (
  record_id                 integer auto_increment not null,
  identifier                varchar(255),
  id                        varchar(255),
  repository_repository_id  integer,
  title                     varchar(255),
  metadata                  varchar(255),
  logcreated                datetime,
  logmodified               datetime,
  loguser                   varchar(255),
  sip_id                    bigint,
  status                    integer,
  sip_status                varchar(255),
  sip_modul                 varchar(255),
  sip_active                varchar(255),
  errormsg                  varchar(255),
  constraint pk_record primary key (record_id))
;

create table repository (
  repository_id             integer auto_increment not null,
  id                        varchar(255),
  title                     varchar(255),
  oai_url                   varchar(255),
  oai_title                 varchar(255),
  oai_mapping               varchar(255),
  dcingest                  varchar(255),
  nomimetypes               varchar(255),
  metadata_prefix           varchar(255),
  resources_prefix          varchar(255),
  cms                       varchar(255),
  cmsfield                  varchar(255),
  mastercollection          varchar(255),
  completecollectionpath    tinyint(1) default 0,
  pds_url                   varchar(255),
  deposit_wsdl_url          varchar(255),
  deposit_wsdl_endpoint     varchar(255),
  producer_wsdl_url         varchar(255),
  producer_wsdl_endpoint    varchar(255),
  sipstatus_wsdl_url        varchar(255),
  sipstatus_wsdl_endpoint   varchar(255),
  material_flow_id          varchar(255),
  producer_id               varchar(255),
  deposit_set_id            varchar(255),
  user_name                 varchar(255),
  institution               varchar(255),
  password                  varchar(255),
  ftp_host                  varchar(255),
  ftp_user                  varchar(255),
  ftp_port                  varchar(255),
  ftp_dir                   varchar(255),
  ftp_key                   varchar(255),
  ftp_max                   varchar(255),
  joblimit                  integer,
  active                    tinyint(1) default 0,
  source_mdformat           varchar(255),
  constraint pk_repository primary key (repository_id))
;

create table resource (
  id                        bigint auto_increment not null,
  orig_file                 varchar(255),
  local_file                varchar(255),
  description               varchar(255),
  mime                      varchar(255),
  record_record_id          integer,
  constraint pk_resource primary key (id))
;

create table user (
  email                     varchar(255) not null,
  name                      varchar(255),
  password                  varchar(255),
  constraint pk_user primary key (email))
;

alter table record add constraint fk_record_repository_1 foreign key (repository_repository_id) references repository (repository_id) on delete restrict on update restrict;
create index ix_record_repository_1 on record (repository_repository_id);
alter table resource add constraint fk_resource_record_2 foreign key (record_record_id) references record (record_id) on delete restrict on update restrict;
create index ix_resource_record_2 on resource (record_record_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table record;

drop table repository;

drop table resource;

drop table user;

SET FOREIGN_KEY_CHECKS=1;

