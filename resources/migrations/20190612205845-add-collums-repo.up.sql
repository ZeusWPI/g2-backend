CREATE TABLE projects
(project_id integer primary key,
 name varchar not null,
 description varchar not null);
--;;
CREATE TABLE repos
(repo_id integer primary key,
 git_id integer not null,
 name varchar not null,
 description varchar,
 url varchar unique,
 project_id integer,
 foreign key(project_id) references projects);
--;;
CREATE TABLE branches
(branch_id integer primary key,
 name varchar not null,
 repo_id integer not null,
 foreign key(repo_id) references repos);
--;;
CREATE TABLE issues
(issue_id integer primary key,
 url varchar not null,
 title varchar not null,
 author varchar not null,
 time varchar not null,
 repo_id integer not null,
 project_id integer not null,
 foreign key(repo_id) references repos,
 foreign key(project_id) references projects);
