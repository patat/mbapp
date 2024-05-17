create table movies
(
    id                serial primary key,
    tmdb_id           int,
    title             varchar not null,
    overview          varchar not null,
    poster_path       varchar not null,
    created_at        timestamp default current_timestamp
);
