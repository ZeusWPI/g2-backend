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

-- :name update-user! :! :n
-- :doc updates an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

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
SELECT project_id, projects.name as name, GROUP_CONCAT(repo_id SEPARATOR ',') as repo_ids 
from projects INNER JOIN repos using(project_id) 
where project_id = :project_id
GROUP By project_id, name;

-- :name get-projects :? :*
SELECT project_id, projects.name as name, GROUP_CONCAT(repo_id SEPARATOR ',') as repo_ids 
FROM projects INNER JOIN repos using(project_id)
GROUP BY project_id, projects.name;

-- :name create-project! :insert :raw
INSERT INTO projects
(name, description)
VALUES (:name, :description)

-- :name delete-project! :! :1
DELETE FROM projects
WHERE project_id = :id

/*
  Projects and Repositories
*/

-- :name link-repo-to-project! :! :1
UPDATE repos
SET project_id = :project_id
WHERE repo_id = :repo_id
