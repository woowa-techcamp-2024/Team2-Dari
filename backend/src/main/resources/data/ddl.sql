drop table if exists checkin;
drop table if exists festival;
drop table if exists member;
drop table if exists purchase;
drop table if exists ticket;
drop table if exists ticket_stock;

create table checkin
(
    is_checked   bit    not null,
    checkin_id   bigint not null auto_increment,
    checkin_time datetime(6),
    created_at   datetime(6) not null,
    festival_id  bigint not null,
    member_id    bigint not null,
    ticket_id    bigint not null,
    updated_at   datetime(6) not null,
    primary key (checkin_id)
) engine=InnoDB;

create table festival
(
    is_deleted                  bit           not null,
    admin_id                    bigint        not null,
    created_at                  datetime(6) not null,
    festival_end_time           datetime not null,
    festival_id                 bigint        not null auto_increment,
    festival_start_time         datetime not null,
    updated_at                  datetime(6) not null,
    festival_title              varchar(100)  not null,
    festival_description        varchar(2000) not null,
    festival_img                varchar(255),
    festival_progress_status    enum ('COMPLETED','ONGOING','UPCOMING') not null,
    festival_publication_status enum ('DRAFT','PUBLISHED') not null,
    primary key (festival_id)
) engine=InnoDB;

create table member
(
    is_deleted  bit          not null,
    created_at  datetime(6) not null,
    member_id   bigint       not null auto_increment,
    updated_at  datetime(6) not null,
    email       varchar(255) not null unique,
    member_name varchar(255) not null,
    profile_img varchar(255),
    primary key (member_id)
) engine=InnoDB;

create table purchase
(
    created_at      datetime(6) not null,
    member_id       bigint not null,
    purchase_id     bigint not null auto_increment,
    purchase_time   datetime(6) not null,
    ticket_id       bigint not null,
    updated_at      datetime(6) not null,
    purchase_status enum ('PURCHASED','REFUNDED') not null,
    primary key (purchase_id)
) engine=InnoDB;

create table ticket
(
    ticket_id       bigint       not null auto_increment,
    is_deleted      bit          not null,
    ticket_quantity integer      not null,
    created_at      datetime(6) not null,
    end_refund_time datetime not null,
    end_sale_time   datetime not null,
    festival_id     bigint       not null,
    start_sale_time datetime not null,
    ticket_price    bigint       not null,
    updated_at      datetime(6) not null,
    ticket_detail   varchar(255) not null,
    ticket_name     varchar(255) not null,

    primary key (ticket_id)
) engine=InnoDB;

create table ticket_stock
(
    ticket_purchase_id bigint  not null auto_increment,
    ticket_stock       integer not null unique,
    created_at         datetime(6) not null,
    ticket_id          bigint  not null,
    updated_at         datetime(6) not null,

    primary key (ticket_purchase_id)
) engine=InnoDB;