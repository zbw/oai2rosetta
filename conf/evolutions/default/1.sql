# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table record (
  record_id                     integer auto_increment not null,
  identifier                    varchar(255),
  id                            varchar(255),
  repository_repository_id      integer,
  title                         varchar(255),
  metadata                      TEXT,
  logcreated                    datetime(6),
  logmodified                   datetime(6),
  loguser                       varchar(255),
  sip_id                        bigint,
  status                        integer,
  sip_status                    varchar(255),
  sip_modul                     varchar(255),
  sip_active                    varchar(255),
  errormsg                      varchar(255),
  constraint pk_record primary key (record_id)
);

create table repository (
  repository_id                 integer auto_increment not null,
  id                            varchar(255),
  title                         varchar(255),
  oai_url                       varchar(255),
  oai_title                     varchar(255),
  oai_mapping                   TEXT,
  dcingest                      varchar(255),
  nomimetypes                   varchar(255),
  metadata_prefix               varchar(255),
  resources_prefix              varchar(255),
  bitstreamsearch               varchar(255),
  bitstreamreplace              varchar(255),
  extract_zip                   tinyint(1) default 0,
  xml_redirect                  tinyint(1) default 0,
  local_import                  tinyint(1) default 0,
  cms                           varchar(255),
  cmsfield                      varchar(255),
  mastercollection              varchar(255),
  completecollectionpath        tinyint(1) default 0,
  collectionxpath               varchar(255),
  pds_url                       varchar(255),
  deposit_wsdl_url              varchar(255),
  deposit_wsdl_endpoint         varchar(255),
  producer_wsdl_url             varchar(255),
  producer_wsdl_endpoint        varchar(255),
  sipstatus_wsdl_url            varchar(255),
  sipstatus_wsdl_endpoint       varchar(255),
  material_flow_id              varchar(255),
  producer_id                   varchar(255),
  deposit_set_id                varchar(255),
  user_name                     varchar(255),
  institution                   varchar(255),
  password                      varchar(255),
  ftp_host                      varchar(255),
  ftp_user                      varchar(255),
  ftp_port                      varchar(255),
  ftp_dir                       varchar(255),
  ftp_key                       varchar(255),
  ftp_max                       varchar(255),
  joblimit                      integer,
  active                        tinyint(1) default 0,
  source_mdformat               varchar(255),
  constraint pk_repository primary key (repository_id)
);

create table resource (
  id                            bigint auto_increment not null,
  orig_file                     varchar(255),
  local_file                    varchar(255),
  description                   varchar(255),
  mime                          varchar(255),
  record_record_id              integer,
  constraint pk_resource primary key (id)
);

create table user (
  email                         varchar(255) not null,
  name                          varchar(255),
  password                      varchar(255),
  constraint pk_user primary key (email)
);

alter table record add constraint fk_record_repository_repository_id foreign key (repository_repository_id) references repository (repository_id) on delete restrict on update restrict;
create index ix_record_repository_repository_id on record (repository_repository_id);

alter table resource add constraint fk_resource_record_record_id foreign key (record_record_id) references record (record_id) on delete restrict on update restrict;
create index ix_resource_record_record_id on resource (record_record_id);


# --- !Downs

alter table record drop foreign key fk_record_repository_repository_id;
drop index ix_record_repository_repository_id on record;

alter table resource drop foreign key fk_resource_record_record_id;
drop index ix_resource_record_record_id on resource;

drop table if exists record;

drop table if exists repository;

drop table if exists resource;

drop table if exists user;

