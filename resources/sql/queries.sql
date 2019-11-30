/*
  We try to hold onto the ANSI standard. Any deviations from this standard should be documented above the query.

Global deviations
-----------------

Autoincrementing primary keys: The standard is pretty bad and verbose. Sqlite autoincrements 'primary key integers'. MySQL does the same using 'SERIAL primary key'
*/


-- :name create-user! :insert :raw
-- :doc creates a new user record
INSERT INTO users
(name, zeus_id, access_token)
VALUES (:name, :zeus_id, :access_token)

-- :name get-user :? :1
-- :doc retrieves a user record given the id
SELECT * FROM users
WHERE id = :id

-- :name get-user-on-zeusid :? :1
SELECT * FROM users
WHERE zeus_id = :zeus_id

-- :name delete-user! :! :n
-- :doc deletes a user record given the id
DELETE FROM users
WHERE id = :id

/*
  Repository Providers
*/

-- :name create-repo-provider! :! :n
-- :doc creates a new link to a repository provider
INSERT INTO repository_providers
(name, access_token)
VALUES (:name, :access_token)

-- :name update-repo-provider-access-token! :! :n
UPDATE repository_providers
SET access_token = :access_token
WHERE name = :name

-- :name get-repo-provider :? :1
-- :doc retrieves a link to a repo provider
SELECT * FROM repository_providers
WHERE name = :name

-- :name get-all-repo-providers :? :*
SELECT * FROM repository_providers

/*
  Repositories
*/

-- :name create-repo! :insert :raw
INSERT INTO repos
(git_id, name, description, url)
VALUES (:git_id, :name, :description, :url)

-- :name get-repos :? :*
SELECT * FROM repos

-- :name get-repo :? :1
SELECT * FROM repos
WHERE repo_id = :repo_id

-- :name update-repo! :! :n
UPDATE repos
SET name = :name, description = :description, url = :url
WHERE git_id = :git_id

/*
  Projects
*/

-- :name get-project :? :1
SELECT project_id, p.name as name, p.description, p.image_url, GROUP_CONCAT(repo_id SEPARATOR ',') as repo_ids
from projects p LEFT JOIN repos using(project_id)
where project_id = :project_id
GROUP BY project_id, name;

-- :name get-projects :? :*
SELECT project_id, p.name as name, p.description, p.image_url, GROUP_CONCAT(repo_id SEPARATOR ',') as repo_ids
FROM projects p LEFT JOIN repos using(project_id)
GROUP BY project_id, p.name;

-- :name create-project! :insert :raw
INSERT INTO projects
(name, description)
VALUES (:name, :description);

-- :name delete-project! :! :1
DELETE FROM projects
WHERE project_id = :id;

-- :name update-project-image! :! :1
UPDATE projects
SET image_url = :image_url
WHERE project_id = :id;
/*
  Projects and Repositories
*/

-- :name link-repo-to-project! :! :1
UPDATE repos
SET project_id = :project_id
WHERE repo_id = :repo_id;


/* LABELS */
-- :name create-label! :insert :raw
INSERT INTO labels
(git_id, name, description, url, color, repo_id)
VALUES (:git_id, :name, :description, :url, :color, :repo_id)

  -- :name get-labels :? :*
SELECT * FROM labels

  -- :name get-label :? :1
SELECT * FROM labels
 WHERE label_id = :label_id

-- :name update-label! :! :n
UPDATE labels
SET name = :name, description = :description, url = :url, color = :color
WHERE git_id = :git_id


/* ---- ISSUES ----- */

-- :name create-issue! :insert :raw
INSERT INTO issues
(git_id, url, title, time, repo_id, author)
VALUES (:git_id, :url, :title, :time, :repo_id, :author)

-- :name get-issues :? :*
SELECT * FROM issues

-- :name get-issue :? :1
SELECT * FROM isuess
WHERE issue_id = :issue_id

-- :name update-issue! :! :n
UPDATE issues
SET url = :url, title = :title, time = :time, author = :author
WHERE git_id = :git_id
