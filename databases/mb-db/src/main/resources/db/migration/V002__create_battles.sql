create table battles
(
    id                serial primary key,
    winner_id         bigint null, foreign key (winner_id) references movies(id),
    created_at        timestamp default current_timestamp
);
