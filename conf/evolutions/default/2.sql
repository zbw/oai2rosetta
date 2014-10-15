# add a limit to repository

# --- !Ups
ALTER TABLE  repository ADD  joblimit integer;

# --- !Downs
ALTER TABLE  repository drop joblimit;

