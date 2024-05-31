create table movies_battles_relation
(
    primary key (movie_id, battle_id),
    movie_id bigint, foreign key (movie_id) references movies(id),
    battle_id bigint, foreign key (battle_id) references battles(id)

);
