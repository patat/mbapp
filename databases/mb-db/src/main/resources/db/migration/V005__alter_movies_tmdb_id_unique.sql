alter table movies
alter column tmdb_id set not null,
add constraint unique_tmdb_id unique (tmdb_id);
