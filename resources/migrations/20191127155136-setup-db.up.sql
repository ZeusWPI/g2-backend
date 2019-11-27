CREATE TABLE issues
(
  issue_id  integer auto_increment primary key,
  url       varchar(255) not null,
  title     varchar(255) not null,
  time      timestamp    not null,
  repo_id   integer      not null,
  author    integer      not null
);
--;;

CREATE TABLE repos 
(
	 project_id  integer auto_increment primary key,
	 git_id      integer not null,
	 name        varchar(255) not null,
	 description varchar(255) not null,
	 url         varchar(255) not null,
	 repo_id     integer not null
);
--;;

CREATE TABLE branches
(
	branch_id integer auto_increment primary key,
	name varchar(255) not null,
	repo_id varchar(255) not null
);
--;;

CREATE TABLE projects 
(
	project_id integer auto_increment primary key,
	name varchar(255) not null,
	description varchar(255) not null,
	image_url varchar(255) not null
);
--;;

CREATE TABLE labels 
(
	label_id integer auto_increment primary key,
	node_id varchar(255) not null,
	url varchar(255) not null,
	name varchar(255) not null,
	color varchar(255) not null,
	is_default BOOLEAN not null,
	description varchar(255) not null,
	repo_id integer not null,
	tag_id integer not null
);
--;;

CREATE TABLE issues_labels 
(
	label_id integer not null,
	issue_id integer not null,
	primary key (label_id,issue_id)
);
--;;

CREATE TABLE tags (
	tag_id integer auto_increment primary key,
	name varchar(255) not null,
	description varchar(255) not null,
	project_id integer not null
);
--;;

CREATE TABLE users 
(
	user_id integer auto_increment primary key,
	name varchar(255) not null,
	email varchar(255) not null,
	admin bool default false,
	last_login TIMESTAMP
);
--;;

CREATE TABLE zeus_user 
(
	zeus_id integer auto_increment primary key,
	user_id integer not null
);
--;;

CREATE TABLE github_user (
	github_id integer auto_increment primary key,
	html_url varchar(512) not null,
	avatar_url varchar(512) not null,
	user_id integer not null
);
