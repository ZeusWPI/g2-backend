/*
  We try to hold onto the ANSI standard. Any deviations from this standard should be documented above the query.

Global deviations
-----------------

Autoincrementing primary keys: The standard is pretty bad and verbose. Sqlite autoincrements 'primary key integers'. MySQL does the same using 'SERIAL primary key'
*/

/*
    Generic tag table
 */
-- :name create-tag! :insert :raw
INSERT INTO tags ()
VALUES ();

-- :name get-tag :? :1
SELECT *
FROM :i:table
WHERE tag_id = :tag_id;

-- :name get-tags :? :*
SELECT *
FROM :i:table;

-- :name get-tags-linked-with-tag :? :*
SELECT *
FROM tag_relations
         INNER JOIN :i:table
ON child_id = tag_id
WHERE parent_id = :tag_id;

-- :name get-tags-count-linked-with-tag :? :1
SELECT count(*) as count
FROM tag_relations
         INNER JOIN :i:table
ON child_id = tag_id
WHERE parent_id = :tag_id;

-- :name link-tag! :! :n
INSERT INTO tag_relations
    (parent_id, child_id)
VALUES (:parent_id, :child_id);

-- :name unlink-tag! :! :n
DELETE
FROM tag_relations
WHERE parent_id = :parent_id
  and child_id = :child_id;

/*
    User stuff
 */

-- :name create-user! :insert :raw
INSERT INTO users
    (name, email, admin, last_login)
VALUES (:name, :email, :admin, :last_login);

-- :name get-user :? :1
SELECT *
FROM users
         LEFT OUTER JOIN zeus_users using (user_id)
WHERE user_id = :user_id;

-- :name get-user-on-zeusid :? :1
SELECT *
FROM users
         LEFT JOIN zeus_users using (user_id)
WHERE zeus_id = :zeus_id;

-- :name get-users :? :*
SELECT *
FROM users
         LEFT OUTER JOIN zeus_users using (user_id);

-- :name delete-user! :! :n
DELETE
FROM users
WHERE user_id = :user_id;

/*
  Repository Providers
*/

-- :name create-repo-provider! :! :n
-- :doc creates a new link to a repository provider
INSERT INTO repository_providers
    (name, access_token)
VALUES (:name, :access_token);

-- :name update-repo-provider-access-token! :! :n
UPDATE repository_providers
SET access_token = :access_token
WHERE name = :name;

-- :name get-repo-provider :? :1
-- :doc retrieves a link to a repo provider
SELECT *
FROM repository_providers
WHERE name = :name;

-- :name get-all-repo-providers :? :*
SELECT *
FROM repository_providers;

/*
  Repositories
*/

-- :name create-repo! :insert :raw
INSERT INTO repos
    (tag_id, git_id, name, description, url)
VALUES (:tag_id, :git_id, :name, :description, :url);

-- :name get-repos :? :*
SELECT repos.tag_id                                as id,
       repos.name                                  as name,
       repos.description,
       url,
       group_concat(named_tags.name SEPARATOR ',') as default_tags
FROM repos
         LEFT JOIN repo_default_tag_mapping ON repos.tag_id = repo_default_tag_mapping.repo_id
         LEFT JOIN named_tags ON repo_default_tag_mapping.tag_id = named_tags.tag_id
GROUP BY repos.tag_id;

-- :name get-repo :? :1
SELECT repos.tag_id                                as id,
       repos.name                                  as name,
       repos.description,
       url,
       group_concat(named_tags.name SEPARATOR ',') as default_tags
FROM repos
         JOIN repo_default_tag_mapping ON repos.tag_id = repo_default_tag_mapping.repo_id
         JOIN named_tags ON repo_default_tag_mapping.tag_id = named_tags.tag_id
WHERE repo_id = :repo_id
GROUP BY repos.tag_id;


-- :name update-repo! :! :n
UPDATE repos
SET name        = :name,
    description = :description,
    url         = :url
WHERE git_id = :git_id;

/*
  Projects
*/

-- :name get-project :? :1
SELECT tag_id as id, p.name as name, p.description, p.image_url
from projects p
where tag_id = :project_id;

-- :name get-projects :? :*
SELECT tag_id as id, p.name as name, p.description, p.image_url
FROM projects p
         LEFT JOIN repos using (tag_id)
GROUP BY tag_id, p.name;

-- :name get-project-repos :? :*
SELECT *
FROM projects
         JOIN tag_relations on projects.tag_id = tag_relations.parent_id
         JOIN repos on tag_relations.child_id = repos.tag_id
WHERE repos.tag_id = :project_id;

-- :name create-project! :insert :raw
-- :command :execute
-- :result: :affected
INSERT INTO projects
    (tag_id, name, description)
VALUES (:tag_id, :name, :description);

-- :name update-project! :! :1
UPDATE projects
SET name        = :name,
    description = :description,
    image_url   = :image
WHERE tag_id = :project_id;

-- :name delete-project! :! :1
DELETE
FROM projects
WHERE tag_id = :id;

-- :name update-project-image! :! :1
UPDATE projects
SET image_url = :image_url
WHERE tag_id = :id;
/*
  Projects and Repositories
*/

-- :name link-repo-to-project! :! :1
UPDATE repos
SET project_id = :project_id
WHERE tag_id = :repo_id;

/* ---- LABELS ---- */

-- :name create-label! :insert :raw
INSERT INTO labels
    (git_id, name, description, url, color, repo_id)
VALUES (:git_id, :name, :description, :url, :color, :repo_id);

-- :name get-labels :? :*
SELECT *
FROM labels;

-- :name get-label :? :1
SELECT *
FROM labels
WHERE label_id = :label_id;

-- :name update-label! :! :n
UPDATE labels
SET name        = :name,
    description = :description,
    url         = :url,
    color       = :color
WHERE git_id = :git_id;

-- :name get-project-labels :? :*
SELECT *
from labels
         INNER JOIN repos using (repo_id)
         INNER JOIN projects using (project_id)
WHERE project_id = :project_id;


/* ---- ISSUES ----- */

-- :name create-issue! :insert :raw
INSERT INTO issues
    (git_id, url, title, time, repo_id, author)
VALUES (:git_id, :url, :title, :time, :repo_id, :author);

-- :name get-issues :? :*
SELECT *
FROM issues;

-- :name get-issue :? :1
SELECT *
FROM issues
WHERE issue_id = :issue_id;

-- :name update-issue! :! :n
UPDATE issues
SET url    = :url,
    title  = :title,
    time   = :time,
    author = :author
WHERE git_id = :git_id;

-- :name get-project-issues :? :*
SELECT *
from issues
         INNER JOIN repos using (repo_id)
         INNER JOIN projects using (project_id)
WHERE project_id = :project_id;


/* ---- BRANCHES ---- */

-- :name create-branch! :insert :raw
INSERT INTO branches
    (commit_sha, name, repo_id)
VALUES (:commit_sha, :name, :repo_id);

-- :name get-branches :? :*
SELECT *
FROM branches;

-- :name get-branch :? :1
SELECT *
FROM branches
WHERE branch_id = :branch_id;

-- :name update-branch! :! :n
UPDATE branches
SET name = :name
WHERE commit_sha = :commit_sha;

-- :name get-project-branches :? :*
SELECT *
from branches
         INNER JOIN repos using (repo_id)
         INNER JOIN projects using (project_id)
WHERE project_id = :project_id;
