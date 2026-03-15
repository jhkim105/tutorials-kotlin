
    create table Company (
        id varchar(50) not null,
        name varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table user (
        id varchar(50) not null,
        description longtext,
        memo text,
        name varchar(255),
        password varchar(255),
        stringList varchar(1000),
        userType varchar(10),
        username varchar(255),
        company_id varchar(50),
        primary key (id)
    ) engine=InnoDB;

    create index IDXgj2fy3dcix7ph7k8684gka40c 
       on user (name);

    alter table user 
       add constraint UK_sb8bbouer5wak8vyiiy4pf2bx unique (username);

    alter table user 
       add constraint FKmuovcfrifs6wdb171q6r8gk4y 
       foreign key (company_id) 
       references Company (id);
