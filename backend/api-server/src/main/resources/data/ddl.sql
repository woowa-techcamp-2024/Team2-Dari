create table if not exists twodari.checkin
(
    is_checked   bit         not null,
    checkin_id   bigint auto_increment
        primary key,
    checkin_time datetime(6) null,
    created_at   datetime(6) not null,
    festival_id  bigint      not null,
    member_id    bigint      not null,
    ticket_id    bigint      not null,
    updated_at   datetime(6) not null
);

create index checkin_festival_id_index
    on twodari.checkin (festival_id);

create index checkin_member_id_index
    on twodari.checkin (member_id);

create index checkin_ticket_id_index
    on twodari.checkin (ticket_id);

create table if not exists twodari.festival
(
    is_deleted                  bit                                       not null,
    admin_id                    bigint                                    not null,
    created_at                  datetime(6)                               null,
    festival_end_time           datetime(6)                               not null,
    festival_id                 bigint auto_increment
        primary key,
    festival_start_time         datetime(6)                               not null,
    updated_at                  datetime(6)                               null,
    festival_title              varchar(100)                              not null,
    festival_description        varchar(2000)                             not null,
    festival_img                varchar(255)                              null,
    festival_progress_status    enum ('COMPLETED', 'ONGOING', 'UPCOMING') not null,
    festival_publication_status enum ('DRAFT', 'PUBLISHED')               not null
);

create index FKdyfiny3xeeh1t7n7w3v30gyf7
    on twodari.festival (admin_id);

create index festival_admin_id_index
    on twodari.festival (admin_id);

create table if not exists twodari.member
(
    is_deleted  bit          not null,
    created_at  datetime(6)  null,
    member_id   bigint auto_increment
        primary key,
    updated_at  datetime(6)  null,
    email       varchar(255) not null,
    member_name varchar(255) not null,
    profile_img varchar(255) null,
    constraint UKmbmcqelty0fbrvxp1q58dn57t
        unique (email)
);

create table if not exists twodari.purchase
(
    created_at      datetime(6)                    not null,
    member_id       bigint                         not null,
    purchase_id     bigint auto_increment
        primary key,
    purchase_time   datetime(6)                    not null,
    ticket_id       bigint                         not null,
    updated_at      datetime(6)                    not null,
    purchase_status enum ('PURCHASED', 'REFUNDED') not null
);

create index purchase_member_id_index
    on twodari.purchase (member_id);

create index purchase_member_id_index_2
    on twodari.purchase (member_id);

create index purchase_ticket_id_index
    on twodari.purchase (ticket_id);

create table if not exists twodari.ticket
(
    is_deleted      bit          not null,
    ticket_quantity int          not null,
    created_at      datetime(6)  null,
    end_refund_time datetime(6)  not null,
    end_sale_time   datetime(6)  not null,
    festival_id     bigint       not null,
    start_sale_time datetime(6)  not null,
    ticket_id       bigint auto_increment
        primary key,
    ticket_price    bigint       not null,
    updated_at      datetime(6)  null,
    ticket_detail   varchar(255) not null,
    ticket_name     varchar(255) not null
);

create index ticket_festival_id_index
    on twodari.ticket (festival_id);

create table if not exists twodari.ticket_stock
(
    ticket_stock_id        bigint auto_increment
        primary key,
    ticket_id              bigint      not null,
    ticket_stock_member_id bigint      null,
    created_at             datetime(6) not null,
    updated_at             datetime(6) not null
);

create index ticket_stock_ticket_id_ticket_stock_member_id_index
    on twodari.ticket_stock (ticket_id, ticket_stock_member_id);

