CREATE TABLE tags
(
    id integer auto_increment primary key
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE tag_relations
(
    parent_id integer not null,
    child_id  integer not null,
    foreign key (parent_id) references tags (id),
    foreign key (child_id) references tags (id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE projects
(
    tag_id      integer      not null unique,
    name        varchar(191) not null unique,
    description varchar(512),
    image_url   varchar(512),
    foreign key (tag_id) references tags (id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE repos
(
    tag_id      integer      not null unique,
    git_id      varchar(191) not null unique,
    repo_type   ENUM ('gitlab', 'github'),
    name        varchar(191) not null,
    description varchar(512),
    url         varchar(512) not null,
    foreign key (tag_id) references tags (id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE repo_default_tag_mapping
(
    repo_id integer not null,
    tag_id  integer not null,
    foreign key (repo_id) references repos (tag_id),
    foreign key (tag_id) references tags (id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE issues
(
    tag_id  integer      not null unique,
    git_id  varchar(191) not null unique,
    url     varchar(512) not null,
    title   varchar(191) not null,
    time    timestamp    not null,
    repo_id integer      not null,
    author  integer      not null,
    foreign key (tag_id) references tags (id),
    foreign key (repo_id) references repos (tag_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE branches
(
    tag_id     integer      not null unique,
    commit_sha varchar(191) not null,
    name       varchar(191) not null,
    repo_id    integer      not null,
    foreign key (tag_id) references tags (id),
    foreign key (repo_id) references repos (tag_id)
) CHARACTER SET utf8mb4;
--;;

-- named tags which are not projects, like language specific, ... To enable filtering over g2,
CREATE TABLE named_tags
(
    tag_id      integer      not null unique,
    name        varchar(191) not null unique,
    color       varchar(191),
    type        varchar(191),
    description varchar(191),
    foreign key (tag_id) references tags (id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE repository_labels
(
    label_id    integer auto_increment primary key,
    git_id      varchar(191) not null unique,
    url         varchar(512),
    name        varchar(191) not null,
    color       varchar(191) default '#FFFFFF',
    description varchar(512),
    repo_id     integer      not null,
    foreign key (repo_id) references repos (tag_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE g2_repo_label_mapping
(
    named_tag_id  integer not null,
    repo_label_id integer not null,
    auto_export   boolean default true,
    auto_import   boolean default false,
    foreign key (named_tag_id) references named_tags (tag_id),
    foreign key (repo_label_id) references repository_labels (label_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE issues_labels
(
    label_id integer not null,
    issue_id integer not null,
    foreign key (label_id) references repository_labels (label_id),
    foreign key (issue_id) references issues (tag_id),
    primary key (label_id, issue_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE users
(
    user_id    integer auto_increment primary key,
    name       varchar(191) not null,
    email      varchar(191),
    admin      BOOLEAN default false,
    last_login TIMESTAMP
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE zeus_users
(
    zeus_id integer primary key,
    user_id integer not null,
    foreign key (user_id) references users (user_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE github_user
(
    github_id  integer primary key,
    html_url   varchar(512) not null,
    avatar_url varchar(512),
    user_id    integer      not null,
    foreign key (user_id) references users (user_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE repository_providers
(
    provider_id  integer auto_increment primary key,
    name         varchar(191),
    access_token varchar(191) not null
) CHARACTER SET utf8mb4;
