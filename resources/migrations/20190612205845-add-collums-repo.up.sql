CREATE TABLE projects
(project_id integer primary key autoincrement,
 name varchar not null,
 description varchar not null);
--;;
CREATE TABLE repos
(id integer primary key autoincrement,
 git_id integer not null,
 name varchar not null,
 description varchar,
 url varchar unique,
 project_id integer,
 foreign key(project_id) references projects);
--;;
CREATE TABLE branches
(branch_id integer primary key autoincrement,
 name varchar not null,
 repo_id integer not null,
 foreign key(repo_id) references repo);
--;;
CREATE TABLE issues
(issue_id integer primary key autoincrement,
 url varchar not null,
 title varchar not null,
 author varchar not null,
 time varchar not null,
 repo_id integer not null,
 project_id integer not null,
 foreign key(repo_id) references repo,
 foreign key(project_id) references project);
