CREATE TABLE users
(
	user_id    integer auto_increment primary key,
	name       varchar(255) not null,
	email      varchar(255),
	admin      BOOLEAN default false,
	last_login TIMESTAMP
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE projects
(
	project_id  integer auto_increment primary key,
	name        varchar(255) not null,
	description varchar(512),
	image_url   varchar(512)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE repos
(
	repo_id     integer auto_increment primary key,
  git_id      varchar(255) not null,
  repo_type   ENUM('gitlab', 'github'),
	project_id  integer,
	name        varchar(255) not null,
	description varchar(512),
	url         varchar(512) not null,
  foreign key (project_id) references projects(project_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE issues
(
  issue_id  integer auto_increment primary key,
  git_id    varchar(255) not null,
  url       varchar(512) not null,
  title     varchar(255) not null,
  time      timestamp    not null,
  repo_id   integer      not null,
  author    integer      not null,
  foreign key (repo_id) references repos(repo_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE branches
(
	branch_id integer auto_increment primary key,
  commit_sha varchar(255) not null,
	name      varchar(255) not null,
	repo_id   integer not null,
  foreign key (repo_id) references repos(repo_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE tags
(
	tag_id      integer auto_increment primary key,
	name        varchar(255) not null,
	description varchar(255),
	project_id  integer not null,
  foreign key (project_id) references projects(project_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE labels
(
	label_id    integer auto_increment primary key,
  git_id      varchar(255) not null,
	url         varchar(512),
	name        varchar(255) not null,
	color       varchar(255) default "#FFFFFF",
	description varchar(512),
	repo_id     integer not null,
	tag_id      integer,
  foreign key (repo_id) references repos(repo_id),
  foreign key (tag_id) references tags(tag_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE issues_labels
(
	label_id integer not null,
	issue_id integer not null,
  foreign key (label_id) references labels(label_id),
  foreign key (issue_id) references issues(issue_id),
	primary key (label_id,issue_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE zeus_users
(
	zeus_id integer primary key,
	user_id integer not null,
  foreign key (user_id) references users(user_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE github_user
(
	github_id  integer primary key,
	html_url   varchar(512) not null,
	avatar_url varchar(512),
	user_id    integer not null,
  foreign key (user_id) references users(user_id)
) CHARACTER SET utf8mb4;
--;;

CREATE TABLE repository_providers
(
  provider_id   integer auto_increment primary key,
  name          varchar(255),
  access_token  varchar(255) not null
) CHARACTER SET utf8mb4;
