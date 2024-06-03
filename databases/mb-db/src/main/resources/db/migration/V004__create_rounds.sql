create table rounds
(
    id                serial primary key,
    battle_id         bigint, foreign key (battle_id) references battles(id),
    movie1_id         bigint null, foreign key (movie1_id) references movies(id),
    movie2_id         bigint null, foreign key (movie2_id) references movies(id),
    winner_id         bigint null, foreign key (winner_id) references movies(id),
    created_at        timestamp default current_timestamp
);