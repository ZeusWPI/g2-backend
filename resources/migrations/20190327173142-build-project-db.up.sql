CREATE TABLE repo
(repo_id integer primary key autoincrement,
 git_id integer not null,
 url varchar unique,
 project_id integer,
 foreign key(project_id) references project);
--;;
CREATE TABLE branches
(branch_id integer primary key autoincrement,
 name varchar not null,
 repo_id integer not null,
 foreign key(repo_id) references repo);
--;;
CREATE TABLE commit_branch
(link_id integer primary key autoincrement,
 branch_id integer not null,
 commit_id integer not null,
 foreign key(branch_id) references branches,
 foreign key(commit_id) references commits);
--;;
CREATE TABLE project
(project_id integer primary key autoincrement,
 name varchar not null,
 description varchar not null,
 parent_id integer,
 foreign key(parent_id) references project(project_id));
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
--;;
CREATE TABLE pull_request
(request_id integer primary key autoincrement,
 author varchar not null,
 branch_id integer not null,
 repo_id integer not null,
 foreign key(branch_id) references branches,
 foreign key(repo_id) references repo);
--;;
CREATE TABLE commits
(commit_id integer primary key autoincrement,
 auteur varchar not null,
 time varchar not null,
 message varchar not null,
 url varchar not null);
--;;
insert into project(name, description) values ('g2', 'yay - g2');
