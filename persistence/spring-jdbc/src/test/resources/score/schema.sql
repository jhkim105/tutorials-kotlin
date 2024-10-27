create table if not exists score
(
    id int auto_increment primary key,
    user_id varchar(50) not null,
    item_code varchar(10) not null,
    score double not null
);
