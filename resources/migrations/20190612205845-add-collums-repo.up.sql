CREATE TABLE projects
(
    project_id  integer      not null auto_increment primary key,
    name        varchar(255) not null,
    description varchar(512) not null
);
--;;
CREATE TABLE repos
(
    repo_id     integer      not null auto_increment primary key,
    git_id      integer      not null,
    name        varchar(255) not null,
    description varchar(512),
    url         varchar(255) unique,
    project_id  integer,
    foreign key (project_id) references projects (project_id)
);
--;;
CREATE TABLE branches
(
    branch_id integer      not null auto_increment primary key,
    name      varchar(255) not null,
    repo_id   integer      not null,
    foreign key (repo_id) references repos (repo_id)
);
--;;
CREATE TABLE issues
(
    issue_id   integer      not null auto_increment primary key,
    url        varchar(255) not null,
    title      varchar(255) not null,
    author     varchar(255) not null,
    time       timestamp    not null,
    repo_id    integer      not null,
    project_id integer      not null,
    foreign key (repo_id) references repos (repo_id),
    foreign key (project_id) references projects (project_id)
);
