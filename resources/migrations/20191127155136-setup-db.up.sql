CREATE TABLE users 
(
	user_id    integer auto_increment primary key,
	name       varchar(255) not null,
	email      varchar(255) not null,
	admin      BOOLEAN default false,
	last_login TIMESTAMP
);
--;;

CREATE TABLE projects 
(
	project_id  integer auto_increment primary key,
	name        varchar(255) not null,
	description varchar(512) not null,
	image_url   varchar(512)
);
--;;

CREATE TABLE repos 
(
	repo_id     integer primary key,
	project_id  integer not null,
	name        varchar(255) not null,
	description varchar(512) not null,
	url         varchar(512) not null,
  foreign key (project_id) references projects(project_id)
);
--;;

CREATE TABLE issues
(
  issue_id  integer auto_increment primary key,
  url       varchar(512) not null,
  title     varchar(255) not null,
  time      timestamp    not null,
  repo_id   integer      not null,
  author    integer      not null,
  foreign key (repo_id) references repos(repo_id),
  foreign key (author) references users(user_id)
);
--;;

CREATE TABLE branches
(
	branch_id integer auto_increment primary key,
	name      varchar(255) not null,
	repo_id   integer not null,
  foreign key (repo_id) references repos(repo_id)
);
--;;

CREATE TABLE tags
(
	tag_id      integer auto_increment primary key,
	name        varchar(255) not null,
	description varchar(255) not null,
	project_id  integer not null,
  foreign key (project_id) references projects(project_id)
);
--;;

CREATE TABLE labels 
(
	label_id    integer primary key,
	node_id     varchar(255) not null,
	url         varchar(512) not null,
	name        varchar(255) not null,
	color       varchar(255) not null,
	is_default  BOOLEAN not null,
	description varchar(512) not null,
	repo_id     integer not null, 
	tag_id      integer not null,
  foreign key (repo_id) references repos(repo_id),
  foreign key (tag_id) references tags(tag_id)
);
--;;

CREATE TABLE issues_labels 
(
	label_id integer not null,
	issue_id integer not null,
  foreign key (label_id) references labels(label_id),
  foreign key (issue_id) references issues(issue_id),
	primary key (label_id,issue_id)
);
--;;

CREATE TABLE zeus_user 
(
	zeus_id integer auto_increment primary key,
	user_id integer not null,
  foreign key (user_id) references users(user_id)
);
--;;

CREATE TABLE github_user 
(
	github_id  integer auto_increment primary key,
	html_url   varchar(512) not null,
	avatar_url varchar(512) not null,
	user_id    integer not null ,
  foreign key (user_id) references users(user_id)
);
--;;

CREATE TABLE repository_providers
(
  provider_id integer auto_increment primary key,
  name        varchar(255) unique,
  access_token  varchar(255) not null
)
